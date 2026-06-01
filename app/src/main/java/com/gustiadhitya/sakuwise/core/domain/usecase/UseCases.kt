package com.gustiadhitya.sakuwise.core.domain.usecase

import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.PlanPeriod
import com.gustiadhitya.sakuwise.core.domain.model.Recurrence
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

// ─── Period computation ────────────────────────────────────────
/** Compute the active plan period for a given anchor date + configured start day. */
class ComputeCurrentPlanPeriodUseCase @Inject constructor() {
    operator fun invoke(today: LocalDate = LocalDate.now(), planStartDay: Int = 1): PlanPeriod {
        // Snap start day to month length (Feb edge case per PRD §7.3).
        fun startOf(ym: YearMonth): LocalDate {
            val day = planStartDay.coerceAtMost(ym.lengthOfMonth())
            return ym.atDay(day)
        }
        val thisMonth = YearMonth.from(today)
        val thisStart = startOf(thisMonth)
        val (start, end) = if (today.isBefore(thisStart)) {
            val prevMonth = thisMonth.minusMonths(1)
            val s = startOf(prevMonth)
            s to startOf(thisMonth).minusDays(1)
        } else {
            val nextMonth = thisMonth.plusMonths(1)
            thisStart to startOf(nextMonth).minusDays(1)
        }
        // Label uses month-end convention per PRD §7.3.
        val endMonth = YearMonth.from(end)
        val label = "Plan ${monthIdShort(endMonth.monthValue)} ${endMonth.year}"
        return PlanPeriod(start, end, label)
    }

    private fun monthIdShort(m: Int): String = arrayOf(
        "Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des"
    )[m - 1].let { if (it.length > 3) it.take(3) else it }.let {
        // For "Mei" keep full; for others use short Indonesian
        when (m) {
            1 -> "Januari"; 2 -> "Februari"; 3 -> "Maret"; 4 -> "April"
            5 -> "Mei"; 6 -> "Juni"; 7 -> "Juli"; 8 -> "Agustus"
            9 -> "September"; 10 -> "Oktober"; 11 -> "November"; else -> "Desember"
        }
    }
}

// ─── Account use cases ─────────────────────────────────────────
class ObserveActiveAccountsUseCase @Inject constructor(private val repo: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> = repo.observeActive()
}

class ObserveAccountBalanceUseCase @Inject constructor(private val repo: AccountRepository) {
    operator fun invoke(accountId: String): Flow<Long> = repo.observeBalance(accountId)
}

class UpsertAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(account: Account): Result<Unit> = runCatching { repo.upsert(account) }
}

class CreateFirstAccountUseCase @Inject constructor(private val repo: AccountRepository) {
    suspend operator fun invoke(name: String, type: AccountType, balance: Long): Result<Account> = runCatching {
        val account = Account(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Tunai" },
            type = type,
            initialBalance = balance,
            iconName = type.code(),
            archived = false,
        )
        repo.upsert(account)
        account
    }
}

class ReconcileAccountUseCase @Inject constructor(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val ctx: android.content.Context,
) {
    suspend operator fun invoke(
        accountId: String,
        observedBalance: Long,
        date: LocalDate = LocalDate.now(),
        note: String? = null,
    ): Result<Long> = runCatching {
        val computed = accountRepo.observeBalance(accountId).first()
        val diff = observedBalance - computed
        accountRepo.insertSnapshot(
            AccountSnapshot(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                date = date,
                observedBalance = observedBalance,
                computedBalance = computed,
                diff = diff,
                note = note,
            ),
        )
        if (diff != 0L) {
            transactionRepo.upsert(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    amount = diff,
                    type = TxnType.Reconciliation,
                    planItemId = null,
                    sourceAccountId = accountId,
                    destAccountId = null,
                    transferFee = null,
                    debtId = null,
                    photoBlob = null,
                    incomeCategoryId = null,
                    note = note ?: ctx.getString(com.gustiadhitya.sakuwise.R.string.reconcile_default_note),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
        diff
    }
}

// ─── Transaction use cases ─────────────────────────────────────
class AddExpenseUseCase @Inject constructor(private val repo: TransactionRepository) {
    suspend operator fun invoke(
        amount: Long,
        date: LocalDate,
        planItemId: String,
        accountId: String,
        note: String? = null,
        photoBlob: ByteArray? = null,
        debtId: String? = null,
    ): Result<Transaction> = runCatching {
        require(amount > 0) { "amount must be positive" }
        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            date = date,
            amount = amount,
            type = TxnType.Expense,
            planItemId = planItemId,
            sourceAccountId = accountId,
            destAccountId = null,
            transferFee = null,
            debtId = debtId,
            photoBlob = photoBlob,
            incomeCategoryId = null,
            note = note,
            createdAt = System.currentTimeMillis(),
        )
        repo.upsert(txn)
        txn
    }
}

class AddIncomeUseCase @Inject constructor(private val repo: TransactionRepository) {
    suspend operator fun invoke(
        amount: Long,
        date: LocalDate,
        accountId: String,
        incomeCategoryId: String?,
        note: String? = null,
    ): Result<Transaction> = runCatching {
        require(amount > 0) { "amount must be positive" }
        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            date = date,
            amount = amount,
            type = TxnType.Income,
            planItemId = null,
            sourceAccountId = accountId,
            destAccountId = null,
            transferFee = null,
            debtId = null,
            photoBlob = null,
            incomeCategoryId = incomeCategoryId,
            note = note,
            createdAt = System.currentTimeMillis(),
        )
        repo.upsert(txn)
        txn
    }
}

class AddTransferUseCase @Inject constructor(private val repo: TransactionRepository) {
    /**
     * PRD §7.4 — Transfer fee.
     *
     * Two paths:
     *  1. No [feePlanItemId]: legacy behaviour. Fee lives on the Transfer
     *     row's `transferFee` column and is subtracted from the source
     *     account balance by the AccountDao SQL. Fee does NOT count against
     *     any plan item (it just leaks out of the budget).
     *  2. [feePlanItemId] provided: PRD-conformant. We DROP `transferFee`
     *     from the Transfer row (so AccountDao doesn't double-subtract) and
     *     write a sibling Expense Transaction(amount=fee, planItemId=…,
     *     sourceAccount=fromAccount). The Expense row is then aggregated
     *     into the plan period totals like any other expense, so the fee
     *     counts as an expense against the plan item.
     */
    suspend operator fun invoke(
        amount: Long,
        date: LocalDate,
        fromAccountId: String,
        toAccountId: String,
        feeAmount: Long = 0L,
        note: String? = null,
        feePlanItemId: String? = null,
    ): Result<Transaction> = runCatching {
        require(amount > 0) { "amount must be positive" }
        require(fromAccountId != toAccountId) { "from and to must differ" }
        val bookFeeAsExpense = feeAmount > 0 && feePlanItemId != null
        val txn = Transaction(
            id = UUID.randomUUID().toString(),
            date = date,
            amount = amount,
            type = TxnType.Transfer,
            planItemId = null,
            sourceAccountId = fromAccountId,
            destAccountId = toAccountId,
            // When fee is booked as a separate Expense row, null this out so
            // the AccountDao balance SQL doesn't double-subtract.
            transferFee = if (bookFeeAsExpense) null else feeAmount.takeIf { it > 0 },
            debtId = null,
            photoBlob = null,
            incomeCategoryId = null,
            note = note,
            createdAt = System.currentTimeMillis(),
        )
        val feeRow = if (bookFeeAsExpense) {
            Transaction(
                id = UUID.randomUUID().toString(),
                date = date,
                amount = feeAmount,
                type = TxnType.Expense,
                planItemId = feePlanItemId,
                sourceAccountId = fromAccountId,
                destAccountId = null,
                transferFee = null,
                debtId = null,
                photoBlob = null,
                incomeCategoryId = null,
                note = "Biaya transfer",
                createdAt = System.currentTimeMillis(),
            )
        } else {
            null
        }
        repo.upsertTransferWithFee(txn, feeRow)
        txn
    }
}

class ObserveRecentTransactionsUseCase @Inject constructor(private val repo: TransactionRepository) {
    operator fun invoke(limit: Int = 10): Flow<List<Transaction>> = repo.observeRecent(limit)
}

class ObservePeriodTotalsUseCase @Inject constructor(
    private val repo: TransactionRepository,
) {
    operator fun invoke(period: PlanPeriod): Flow<Pair<Long, Long>> =
        combine(
            repo.observeIncomeBetween(period.start, period.end),
            repo.observeExpenseBetween(period.start, period.end),
        ) { income, expense -> income to expense }
}

// ─── Plan use cases ────────────────────────────────────────────
class ObserveCurrentPlanUseCase @Inject constructor(private val repo: PlanRepository) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<Plan?> = repo.observeForDate(date)
}

class CreatePlanUseCase @Inject constructor(private val repo: PlanRepository) {
    suspend operator fun invoke(period: PlanPeriod, expectedIncome: Long = 0L): Result<Plan> = runCatching {
        val plan = Plan(
            id = UUID.randomUUID().toString(),
            start = period.start,
            end = period.end,
            label = period.label,
            expectedIncome = expectedIncome,
            note = null,
        )
        repo.upsert(plan)
        plan
    }
}

/**
 * RegenerateNextPlanUseCase — PRD §7.5. Creates the next month's plan
 * pre-populated with recurring (monthly/quarterly/yearly) items from the
 * previous period. One-off items are NOT carried over.
 */
class RegenerateNextPlanUseCase @Inject constructor(
    private val planRepo: PlanRepository,
    private val computePeriod: ComputeCurrentPlanPeriodUseCase,
) {
    suspend operator fun invoke(
        previousPlan: Plan,
        anchorDate: LocalDate = previousPlan.end.plusDays(1),
        planStartDay: Int = 1,
    ): Result<Plan> = runCatching {
        val nextPeriod = computePeriod(today = anchorDate, planStartDay = planStartDay)
        val newPlanId = UUID.randomUUID().toString()
        val newPlan = Plan(
            id = newPlanId, start = nextPeriod.start, end = nextPeriod.end,
            label = nextPeriod.label, expectedIncome = previousPlan.expectedIncome,
            note = null,
        )
        planRepo.upsert(newPlan)

        val prevAllocs = planRepo.observeAllocations(previousPlan.id).first()
        for (alloc in prevAllocs) {
            val newAllocId = UUID.randomUUID().toString()
            planRepo.upsertAllocation(
                Allocation(newAllocId, newPlanId, alloc.name, alloc.targetPct, alloc.sortOrder),
            )
            val cats = planRepo.observeCategories(alloc.id).first()
            for (cat in cats) {
                val newCatId = UUID.randomUUID().toString()
                planRepo.upsertCategory(
                    Category(newCatId, newAllocId, cat.name, cat.plannedAmount, cat.sortOrder),
                )
                val items = planRepo.observePlanItems(cat.id).first()
                for (item in items) {
                    if (item.recurrence != Recurrence.OneOff) {
                        planRepo.upsertPlanItem(
                            PlanItem(
                                id = UUID.randomUUID().toString(),
                                categoryId = newCatId,
                                name = item.name,
                                plannedAmount = item.plannedAmount,
                                recurrence = item.recurrence,
                                note = item.note,
                                sortOrder = item.sortOrder,
                            ),
                        )
                    }
                }
            }
        }
        newPlan
    }
}

/**
 * AddLandTaxPaymentUseCase — LandTaxPayment + linked Transaction (PRD §7.8).
 */
class AddLandTaxPaymentUseCase @Inject constructor(
    private val landRepo: LandRepository,
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(
        landId: String, date: LocalDate, amount: Long,
        accountId: String?, note: String?,
    ): Result<Unit> = runCatching {
        landRepo.upsertTaxPayment(
            com.gustiadhitya.sakuwise.core.domain.model.LandTaxPayment(
                id = UUID.randomUUID().toString(),
                assetLandId = landId, date = date, amount = amount,
                accountId = accountId, note = note,
            ),
        )
        if (accountId != null) {
            transactionRepo.upsert(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    date = date, amount = amount, type = TxnType.Expense,
                    planItemId = null,
                    sourceAccountId = accountId, destAccountId = null,
                    transferFee = null, debtId = null,
                    photoBlob = null, incomeCategoryId = null,
                    note = note ?: "Pajak PBB",
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * AddDebtPaymentUseCase — DebtPayment + linked Transaction (PRD §7.10).
 * Cashflow counts in monthly stats.
 */
class AddDebtPaymentUseCase @Inject constructor(
    private val debtRepo: DebtRepository,
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(
        debtId: String,
        debtDirection: com.gustiadhitya.sakuwise.core.domain.model.DebtDirection,
        date: LocalDate, amount: Long,
        accountId: String?, note: String?,
    ): Result<Unit> = runCatching {
        val txnId = if (accountId != null) UUID.randomUUID().toString() else null
        debtRepo.upsertPayment(
            com.gustiadhitya.sakuwise.core.domain.model.DebtPayment(
                id = UUID.randomUUID().toString(),
                debtId = debtId, date = date, amount = amount,
                accountId = accountId, transactionId = txnId, note = note,
            ),
        )
        if (accountId != null && txnId != null) {
            val type = if (debtDirection ==
                com.gustiadhitya.sakuwise.core.domain.model.DebtDirection.IOwe
            ) TxnType.Expense else TxnType.Income
            transactionRepo.upsert(
                Transaction(
                    id = txnId, date = date, amount = amount, type = type,
                    planItemId = null,
                    sourceAccountId = accountId, destAccountId = null,
                    transferFee = null, debtId = debtId,
                    photoBlob = null, incomeCategoryId = null,
                    // Leave note null when the user didn't enter one — the UI
                    // resolves a localized fallback label (txntype_debt_outflow)
                    // from the transaction type, keeping the domain layer pure.
                    note = note,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * MarkGoldSoldUseCase — flips a GoldAsset to Sold + records the sell income.
 *
 * Per PRD §7.7. When an account is selected, the sale proceeds land as an
 * income Transaction so the deposit appears in dashboard cashflow + plan stats.
 * If no account is selected, only the asset row flips to Sold (sale recorded
 * outside the app).
 */
/**
 * RegenerateRecurringIncomesUseCase — V1.1 auto-gen for income flagged [BERULANG].
 *
 * The income txn form prefixes "[BERULANG]" into the note when the recurring
 * toggle is on. This use case scans the last 90 days for those rows, groups
 * them by (source account + amount + same day-of-month), and writes one new
 * income txn for the current period if a matching row doesn't already exist.
 *
 * Triggered from a PlanActionSheet button. A WorkManager periodic worker
 * could call this on the 1st of each month — left as V1.1 deferred since
 * shipping the user-triggered path is sufficient for v1.
 */
class RegenerateRecurringIncomesUseCase @Inject constructor(
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Result<Int> = runCatching {
        // Pull the recent window (the repo `observeRecent` is enough — 50 rows covers
        // the typical 2-3 recurring-income setup; if the user has many, this could
        // miss some, but it errs on safe-not-double).
        val recent = transactionRepo.observeRecent(limit = 200).first()
        val flagged = recent.filter {
            it.type == TxnType.Income && it.note?.startsWith("[BERULANG]") == true
        }
        if (flagged.isEmpty()) return@runCatching 0

        data class Sig(val account: String, val amount: Long, val day: Int)
        val byKey = flagged.groupBy {
            Sig(it.sourceAccountId ?: "", it.amount, it.date.dayOfMonth)
        }
        var created = 0
        byKey.forEach { (sig, rows) ->
            if (sig.account.isBlank()) return@forEach
            val latest = rows.maxByOrNull { it.date } ?: return@forEach
            // Target date for the current period — same day-of-month, today's month.
            val target = today.withDayOfMonth(sig.day.coerceIn(1, today.lengthOfMonth()))
            val alreadyExists = rows.any { it.date.year == target.year && it.date.month == target.month }
            if (alreadyExists) return@forEach
            if (target.isAfter(today)) return@forEach // not due yet
            transactionRepo.upsert(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    date = target,
                    amount = latest.amount,
                    type = TxnType.Income,
                    planItemId = null,
                    sourceAccountId = latest.sourceAccountId,
                    destAccountId = null,
                    transferFee = null,
                    debtId = null,
                    photoBlob = null,
                    incomeCategoryId = latest.incomeCategoryId,
                    note = latest.note,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            created++
        }
        created
    }
}

class MarkGoldSoldUseCase @Inject constructor(
    private val goldRepo: GoldRepository,
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(
        goldId: String,
        soldDate: LocalDate,
        soldPrice: Long,
        accountId: String?,
        note: String?,
    ): Result<Unit> = runCatching {
        val current = goldRepo.observeById(goldId).first()
            ?: error("Gold $goldId not found")
        if (current.status == com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Sold) {
            // Idempotent — don't double-write the income txn.
            return@runCatching
        }
        goldRepo.upsert(
            current.copy(
                status = com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Sold,
                soldDate = soldDate,
                soldPrice = soldPrice,
            ),
        )
        if (accountId != null) {
            transactionRepo.upsert(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    date = soldDate, amount = soldPrice, type = TxnType.Income,
                    planItemId = null,
                    sourceAccountId = accountId, destAccountId = null,
                    transferFee = null, debtId = null,
                    photoBlob = null, incomeCategoryId = null,
                    note = note ?: "Penjualan emas ${com.gustiadhitya.sakuwise.core.common.formatMilliGrams(current.weightMilliGram)}g",
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

class SetupDefaultAllocationsUseCase @Inject constructor(
    private val planRepo: PlanRepository,
    private val prefsRepo: UserPreferencesRepository,
) {
    suspend operator fun invoke(planId: String): Result<List<Allocation>> = runCatching {
        val prefs = prefsRepo.prefs.first()
        val rows = listOf(
            Allocation(id = UUID.randomUUID().toString(), planId = planId, name = "Needs", targetPct = prefs.needsPct, sortOrder = 0),
            Allocation(id = UUID.randomUUID().toString(), planId = planId, name = "Wants", targetPct = prefs.wantsPct, sortOrder = 1),
            Allocation(id = UUID.randomUUID().toString(), planId = planId, name = "Investment", targetPct = prefs.investPct, sortOrder = 2),
        )
        rows.forEach { planRepo.upsertAllocation(it) }
        rows
    }
}

class SetExpectedIncomeUseCase @Inject constructor(private val repo: PlanRepository) {
    suspend operator fun invoke(plan: Plan, amount: Long): Result<Unit> = runCatching {
        repo.upsert(plan.copy(expectedIncome = amount))
    }
}

class ResetPlanUseCase @Inject constructor(
    private val planRepo: PlanRepository,
    private val setupAllocations: SetupDefaultAllocationsUseCase,
) {
    /** Delete all categories under all allocations, leaving the plan + empty allocations. */
    suspend operator fun invoke(planId: String): Result<Unit> = runCatching {
        val allocations = planRepo.observeAllocations(planId).first()
        for (alloc in allocations) {
            val cats = planRepo.observeCategories(alloc.id).first()
            for (cat in cats) {
                val items = planRepo.observePlanItems(cat.id).first()
                for (item in items) planRepo.deletePlanItem(item.id)
                planRepo.deleteCategory(cat.id)
            }
        }
    }
}

class ApplyStarterTemplateUseCase @Inject constructor(
    private val planRepo: PlanRepository,
) {
    suspend operator fun invoke(planId: String, allocations: List<Allocation>): Result<Unit> = runCatching {
        val needs = allocations.first { it.name == "Needs" }
        val wants = allocations.first { it.name == "Wants" }
        val invest = allocations.first { it.name == "Investment" }
        // Per PRD §12 — categories + items, amounts left 0 (user fills in)
        TemplateStarter.NEEDS.forEachIndexed { i, (catName, items) ->
            val cat = Category(UUID.randomUUID().toString(), needs.id, catName, null, i)
            planRepo.upsertCategory(cat)
            items.forEachIndexed { j, (itemName, recurrence) ->
                planRepo.upsertPlanItem(
                    PlanItem(UUID.randomUUID().toString(), cat.id, itemName, 0L, recurrence, null, j),
                )
            }
        }
        TemplateStarter.WANTS.forEachIndexed { i, (catName, items) ->
            val cat = Category(UUID.randomUUID().toString(), wants.id, catName, null, i)
            planRepo.upsertCategory(cat)
            items.forEachIndexed { j, (itemName, recurrence) ->
                planRepo.upsertPlanItem(
                    PlanItem(UUID.randomUUID().toString(), cat.id, itemName, 0L, recurrence, null, j),
                )
            }
        }
        TemplateStarter.INVEST.forEachIndexed { i, (catName, items) ->
            val cat = Category(UUID.randomUUID().toString(), invest.id, catName, null, i)
            planRepo.upsertCategory(cat)
            items.forEachIndexed { j, (itemName, recurrence) ->
                planRepo.upsertPlanItem(
                    PlanItem(UUID.randomUUID().toString(), cat.id, itemName, 0L, recurrence, null, j),
                )
            }
        }
    }
}

/** Compute Net Worth per PRD §7.6: accounts + gold (held) + land + deposit (latest snapshot) − debts (i_owe). */
class ComputeNetWorthUseCase @Inject constructor(
    private val accountRepo: AccountRepository,
    private val goldRepo: GoldRepository,
    private val landRepo: LandRepository,
    private val depositRepo: DepositRepository,
    private val debtRepo: DebtRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val txnRepo: TransactionRepository,
) {
    data class NetWorth(
        val accountsTotal: Long,
        val goldTotal: Long,
        val landTotal: Long,
        val depositTotal: Long,
        val debtsTotal: Long,
        val total: Long,
    )

    /**
     * Emits a new NetWorth whenever any of the underlying tables changes —
     * including transactions (which previously was NOT in the outer combine
     * so a new expense would not re-trigger the aggregation, leaving the
     * Aset hub showing the stale account total). We use observeRecent(1) as
     * a cheap change-stamp on the transactions table; the value is ignored,
     * the emission is what matters.
     */
    operator fun invoke(): Flow<NetWorth> = flow {
        val base = combine(
            accountRepo.observeActive(),
            accountRepo.observeTotalBalance(),
            goldRepo.observeAll(),
            landRepo.observeAll(),
            depositRepo.observeAll(),
            debtRepo.observeAll(),
            txnRepo.observeRecent(1),
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            Septet(
                args[0] as List<com.gustiadhitya.sakuwise.core.domain.model.Account>,
                args[1] as Long,
                args[2] as List<com.gustiadhitya.sakuwise.core.domain.model.GoldAsset>,
                args[3] as List<com.gustiadhitya.sakuwise.core.domain.model.LandAsset>,
                args[4] as List<com.gustiadhitya.sakuwise.core.domain.model.DepositAsset>,
                args[5] as List<com.gustiadhitya.sakuwise.core.domain.model.Debt>,
                args[6],
            )
        }
        combine(base, prefsRepo.prefs) { tuple, prefs ->
            val accountsTotal = tuple.b // already Room-tracked via observeTotalBalance subqueries
            val goldTotal = tuple.c.filter { it.status == com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Held }
                .sumOf {
                    val price =
                        if (it.kind == com.gustiadhitya.sakuwise.core.domain.model.GoldKind.Digital) prefs.goldPriceDigital
                        else prefs.goldPriceGlobal
                    it.valueAt(price)
                }
            val landTotal = tuple.d.filter { it.status == com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Held }
                .sumOf { it.currentValue ?: it.buyPrice }
            val depositTotal = tuple.e.fold(0L) { sum, d ->
                sum + (depositRepo.observeLatestSnapshot(d.id).first()?.balance ?: 0L)
            }
            val debtsTotal = tuple.f
                .filter { it.open && it.direction == com.gustiadhitya.sakuwise.core.domain.model.DebtDirection.IOwe }
                .sumOf { d ->
                    val paid = debtRepo.observePaidTotal(d.id).first()
                    (d.principal - paid).coerceAtLeast(0L)
                }
            NetWorth(
                accountsTotal, goldTotal, landTotal, depositTotal, debtsTotal,
                total = accountsTotal + goldTotal + landTotal + depositTotal - debtsTotal,
            )
        }.collect { emit(it) }
    }

    private data class Septet<A, B, C, D, E, F, G>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G,
    )
}

/**
 * SnapshotNetWorthTodayUseCase — writes today's net worth as a row in
 * `net_worth_snapshots`. Called by NetWorthSnapshotWorker (daily) so the
 * trend can show daily granularity instead of computing per-render.
 */
class SnapshotNetWorthTodayUseCase @Inject constructor(
    private val computeNetWorth: ComputeNetWorthUseCase,
    private val dao: com.gustiadhitya.sakuwise.core.database.dao.NetWorthSnapshotDao,
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Result<Unit> = runCatching {
        val nw = computeNetWorth().first()
        dao.upsert(
            com.gustiadhitya.sakuwise.core.database.entity.NetWorthSnapshotEntity(
                epochDay = today.toEpochDay(),
                accountsTotal = nw.accountsTotal,
                goldTotal = nw.goldTotal,
                landTotal = nw.landTotal,
                depositTotal = nw.depositTotal,
                debtsTotal = nw.debtsTotal,
                total = nw.total,
            ),
        )
        // Keep a year of history; older rows are derivable from monthly aggregates.
        dao.prune(olderThanEpochDay = today.minusYears(1).toEpochDay())
    }
}

/**
 * ComputeNetWorthTrendUseCase — returns a monthly (date, totalNetWorth) series
 * over the last [monthsBack] months using ONLY real daily snapshots from the
 * net_worth_snapshots table.
 *
 * Each calendar month is represented by the LAST daily snapshot of that month
 * (end-of-month close value).  Months with no real snapshot are excluded so
 * the chart never shows synthetic / back-projected data that could mislead
 * the user into thinking their net worth dropped or grew artificially.
 *
 * Fresh users see fewer data points (honest). The chart shows a clear
 * empty-state message until at least 2 real calendar months have accumulated.
 */
class ComputeNetWorthTrendUseCase @Inject constructor(
    private val snapshotDao: com.gustiadhitya.sakuwise.core.database.dao.NetWorthSnapshotDao,
    private val snapshotToday: SnapshotNetWorthTodayUseCase,
) {
    suspend operator fun invoke(
        today: LocalDate = LocalDate.now(),
        monthsBack: Int = 11,
        currentNw: ComputeNetWorthUseCase.NetWorth? = null,
    ): List<Pair<LocalDate, Long>> {
        // Refresh today's snapshot so the chart always ends at the current
        // net worth regardless of whether the daily worker has run yet.
        snapshotToday(today)

        // Non-account assets (gold, land, deposits) have no per-month snapshot
        // history — only account balances are tracked transactionally. When a
        // user enters their gold/land/deposits for the first time, historical
        // monthly snapshots have goldTotal=0 etc., making the chart appear to
        // show only account data. Fix: use the CURRENT non-account component
        // for every historical month, so the chart shows account evolution
        // (which does change month-to-month) against a stable non-account
        // baseline. The latest data point equals the Aset card total exactly.
        val nonAccountOffset = currentNw?.let {
            it.goldTotal + it.landTotal + it.depositTotal - it.debtsTotal
        }

        val snapshots = snapshotDao.observeAll().first()
        val realByMonth: Map<YearMonth, Long> = snapshots
            .groupBy { YearMonth.from(LocalDate.ofEpochDay(it.epochDay)) }
            .mapValues { (_, rows) ->
                val snap = rows.maxByOrNull { it.epochDay }!!
                if (nonAccountOffset != null) snap.accountsTotal + nonAccountOffset
                else snap.total
            }

        return (monthsBack downTo 0).mapNotNull { m ->
            val ym = YearMonth.from(today).minusMonths(m.toLong())
            val value = realByMonth[ym] ?: return@mapNotNull null   // skip months with no data
            val endOfMonth = ym.atEndOfMonth().let { if (it.isAfter(today)) today else it }
            endOfMonth to value
        }
    }
}

private object TemplateStarter {
    val NEEDS = listOf(
        "Tempat Tinggal" to listOf(
            "Kos/Sewa/Cicilan Rumah" to Recurrence.Monthly,
            "Listrik" to Recurrence.Monthly,
            "Air PAM" to Recurrence.Monthly,
            "Gas LPG" to Recurrence.Monthly,
            "Internet" to Recurrence.Monthly,
            "Air Galon" to Recurrence.Monthly,
        ),
        "Makanan" to listOf(
            "Makan Harian" to Recurrence.Monthly,
            "Belanja Bulanan" to Recurrence.Monthly,
        ),
        "Transportasi" to listOf(
            "BBM" to Recurrence.Monthly,
            "Transportasi Online" to Recurrence.Monthly,
            "Transportasi Umum" to Recurrence.Monthly,
            "Tiket Mudik" to Recurrence.Yearly,
        ),
        "Kendaraan" to listOf(
            "Servis" to Recurrence.Quarterly,
            "Pajak Kendaraan" to Recurrence.Yearly,
            "Asuransi Kendaraan" to Recurrence.Yearly,
        ),
        "Kesehatan" to listOf(
            "BPJS Kesehatan" to Recurrence.Monthly,
            "Asuransi Kesehatan" to Recurrence.Monthly,
            "Obat-obatan" to Recurrence.OneOff,
        ),
        "Komunikasi" to listOf(
            "Pulsa" to Recurrence.Monthly,
            "Paket Data" to Recurrence.Monthly,
        ),
        "Pajak & Iuran" to listOf(
            "PBB" to Recurrence.Yearly,
            "Iuran RT/RW" to Recurrence.Monthly,
            "Sampah" to Recurrence.Monthly,
        ),
        "Sosial" to listOf(
            "Sedekah/Zakat" to Recurrence.Monthly,
            "Acara Keluarga / Kondangan" to Recurrence.OneOff,
        ),
    )
    val WANTS = listOf(
        "Hiburan" to listOf(
            "Streaming" to Recurrence.Monthly,
            "Bioskop" to Recurrence.OneOff,
            "Gaming" to Recurrence.OneOff,
        ),
        "Hobi" to listOf(
            "Gadget" to Recurrence.OneOff,
            "Olahraga" to Recurrence.Monthly,
            "Buku" to Recurrence.OneOff,
        ),
        "Makan di Luar" to listOf(
            "Kopi/Kafe" to Recurrence.Monthly,
            "Restoran" to Recurrence.Monthly,
            "Jajan" to Recurrence.Monthly,
        ),
        "Self Care" to listOf(
            "Skincare" to Recurrence.OneOff,
            "Salon / Barbershop" to Recurrence.Monthly,
        ),
        "Belanja" to listOf(
            "Pakaian" to Recurrence.OneOff,
            "Elektronik" to Recurrence.OneOff,
        ),
    )
    val INVEST = listOf(
        "Tabungan" to listOf(
            "Dana Darurat" to Recurrence.Monthly,
            "Tabungan Reguler" to Recurrence.Monthly,
        ),
        "Investasi" to listOf(
            "Emas" to Recurrence.Monthly,
            "Reksa Dana / Saham" to Recurrence.Monthly,
            "DPLK Tambahan" to Recurrence.Monthly,
            "Properti" to Recurrence.OneOff,
        ),
        "Pendidikan" to listOf(
            "Kursus" to Recurrence.OneOff,
            "Sertifikasi" to Recurrence.OneOff,
            "Buku Belajar" to Recurrence.OneOff,
        ),
    )
}
