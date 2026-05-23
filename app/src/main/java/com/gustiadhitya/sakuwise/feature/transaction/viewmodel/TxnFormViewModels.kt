package com.gustiadhitya.sakuwise.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Debt
import com.gustiadhitya.sakuwise.core.domain.model.DebtDirection
import com.gustiadhitya.sakuwise.core.domain.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.AddExpenseUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.AddIncomeUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.AddTransferUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveCurrentPlanUseCase
import com.gustiadhitya.sakuwise.feature.transaction.ui.PlanItemRowOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TxnFormState(
    val editingId: String? = null,
    val amount: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val accounts: List<Account> = emptyList(),
    val accountId: String? = null,
    val destAccountId: String? = null,   // transfer only
    val planItemId: String? = null,      // expense only
    val planItemName: String? = null,
    // Allocation of the picked plan item — drives hero tint on expense form
    // (Needs=primary, Wants=accent, Invest=info). Per prototype screens-addtxn.jsx:62.
    val planItemAllocation: com.gustiadhitya.sakuwise.core.domain.model.AllocationId? = null,
    val transferFee: Long = 0L,
    val debtId: String? = null,          // expense only
    val debtLabel: String? = null,
    val incomeCategoryId: String? = null,    // income only
    val incomeCategoryName: String? = null,
    val recurringIncome: Boolean = false, // income only — marker for V1.1 auto-gen
    val note: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    // Expense only — JPEG BLOB of the attached receipt photo (PRD §7.4 + §7.11).
    // Populated either by OCR prefill or by the camera/gallery picker on the
    // standalone Expense form. Compressed to ≤ ~200 KB upstream.
    val photoBlob: ByteArray? = null,
    // Transfer only — when fee > 0 and a plan item is picked, the fee is booked
    // as a separate Expense child txn against this plan item (PRD §7.4).
    val feePlanItemId: String? = null,
    val feePlanItemName: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TxnFormViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val debtRepo: DebtRepository,
    private val addExpense: AddExpenseUseCase,
    private val addIncome: AddIncomeUseCase,
    private val addTransfer: AddTransferUseCase,
    private val planRepo: PlanRepository,
    private val transactionRepo: TransactionRepository,
    private val observeCurrentPlan: ObserveCurrentPlanUseCase,
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = accountRepo.observeActive().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )

    /** Open debts the user owes — eligible for "tautkan ke hutang" on expense form. */
    val openOwedDebts: StateFlow<List<Debt>> = debtRepo.observeAll().map { list ->
        list.filter { it.open && it.direction == DebtDirection.IOwe }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Income categories — picker source for the income form's "Kategori Sumber". */
    val incomeCategories: StateFlow<List<IncomeCategory>> =
        transactionRepo.observeIncomeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Flat list of plan items grouped by allocation/category. Used by the
     * PlanItemPickerSheet on the expense form. Built by combining the current
     * plan's allocations → categories → items.
     */
    val planItemOptions: StateFlow<List<PlanItemRowOption>> = observeCurrentPlan()
        .flatMapLatest { plan ->
            if (plan == null) flowOf(emptyList()) else {
                planRepo.observeAllocations(plan.id).flatMapLatest { allocations ->
                    if (allocations.isEmpty()) flowOf(emptyList()) else {
                        // For each allocation, flatten its categories→items.
                        val perAllocFlows = allocations.map { alloc ->
                            planRepo.observeCategories(alloc.id).flatMapLatest { cats ->
                                if (cats.isEmpty()) flowOf<List<PlanItemRowOption>>(emptyList())
                                else combine(
                                    cats.map { cat ->
                                        planRepo.observePlanItems(cat.id).map { items ->
                                            items.map { pi ->
                                                PlanItemRowOption(
                                                    id = pi.id, name = pi.name,
                                                    categoryName = cat.name,
                                                    allocationName = alloc.name,
                                                    plan = pi.plannedAmount, used = 0L,
                                                )
                                            }
                                        }
                                    },
                                ) { lists -> lists.toList().flatten() }
                            }
                        }
                        combine(perAllocFlows) { arrays -> arrays.toList().flatten() }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(TxnFormState())
    val state: StateFlow<TxnFormState> = _state

    fun setAmount(v: Long) { _state.value = _state.value.copy(amount = v) }
    fun setDate(d: LocalDate) { _state.value = _state.value.copy(date = d) }
    fun setAccount(id: String) { _state.value = _state.value.copy(accountId = id) }
    fun setDestAccount(id: String) { _state.value = _state.value.copy(destAccountId = id) }
    fun setPlanItem(id: String, name: String, allocation: com.gustiadhitya.sakuwise.core.domain.model.AllocationId? = null) {
        _state.value = _state.value.copy(planItemId = id, planItemName = name, planItemAllocation = allocation)
    }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }
    fun setFee(v: Long) { _state.value = _state.value.copy(transferFee = v) }
    fun setDebt(id: String?, label: String?) {
        _state.value = _state.value.copy(debtId = id, debtLabel = label)
    }
    fun setRecurringIncome(b: Boolean) {
        _state.value = _state.value.copy(recurringIncome = b)
    }
    fun setIncomeCategory(id: String, name: String) {
        _state.value = _state.value.copy(incomeCategoryId = id, incomeCategoryName = name)
    }
    /**
     * Apply an OCR-parsed receipt draft to the current state. Account and
     * plan item still need user input — those aren't derivable from a receipt.
     */
    fun prefillFromOcrDraft(
        amount: Long?,
        date: LocalDate?,
        merchant: String?,
        photoBlob: ByteArray? = null,
    ) {
        _state.value = _state.value.copy(
            amount = amount ?: _state.value.amount,
            date = date ?: _state.value.date,
            note = listOfNotNull(merchant, _state.value.note.ifBlank { null }).joinToString(" · "),
            // OCR-supplied JPEG. If null (helper failed) we leave any existing
            // photoBlob alone so manual picker selections aren't clobbered.
            photoBlob = photoBlob ?: _state.value.photoBlob,
        )
    }

    /** Standalone Expense form: attach (or clear) a receipt JPEG. */
    fun setPhotoBlob(bytes: ByteArray?) {
        _state.value = _state.value.copy(photoBlob = bytes)
    }

    /** Transfer form: assign / clear the plan item the transfer fee charges to. */
    fun setFeePlanItem(id: String?, name: String?) {
        _state.value = _state.value.copy(feePlanItemId = id, feePlanItemName = name)
    }
    fun swap() {
        val s = _state.value
        _state.value = s.copy(accountId = s.destAccountId, destAccountId = s.accountId)
    }

    /**
     * Reset the shared form state. MUST be called whenever an overlay closes —
     * otherwise `saved=true` from a previous submit auto-closes the next form,
     * and OCR-prefill leftover state confuses unrelated openings. Keeps the
     * accounts/planItems flows intact (they're upstream).
     */
    fun resetForNewEntry() {
        // Explicitly null out photoBlob + feePlanItem so a previous OCR/manual
        // attach doesn't bleed into the next unrelated form opening. Also
        // clears editingId so a fresh open doesn't accidentally upsert into a
        // stale row.
        _state.value = TxnFormState(accounts = _state.value.accounts)
    }

    /**
     * Edit mode — prefills the form from an existing transaction. The active
     * form (Expense / Income / Transfer) should call this in a LaunchedEffect
     * keyed by [txnId]. Subsequent submit* calls upsert in-place against the
     * same id; [delete] removes the row entirely.
     *
     * Plan-item allocation, debt label, income-category name, and transfer-fee
     * plan-item name are best-effort: we resolve them off the side state we
     * already have (planItemOptions, accounts, openOwedDebts, incomeCategories).
     */
    fun loadExisting(txnId: String) {
        viewModelScope.launch {
            val txn = transactionRepo.getById(txnId) ?: return@launch
            // Side resolves — these flows may not have emitted yet on first
            // launch; we read .value (which has a sensible default) and fall
            // back to the raw id if a name lookup misses.
            val planItem = planItemOptions.value.firstOrNull { it.id == txn.planItemId }
            val incomeCat = incomeCategories.value.firstOrNull { it.id == txn.incomeCategoryId }
            val debt = openOwedDebts.value.firstOrNull { it.id == txn.debtId }
            _state.value = TxnFormState(
                editingId = txn.id,
                amount = txn.amount,
                date = txn.date,
                accounts = _state.value.accounts,
                accountId = txn.sourceAccountId,
                destAccountId = txn.destAccountId,
                planItemId = txn.planItemId,
                planItemName = planItem?.name,
                planItemAllocation = null,
                transferFee = txn.transferFee ?: 0L,
                debtId = txn.debtId,
                debtLabel = debt?.counterparty,
                incomeCategoryId = txn.incomeCategoryId,
                incomeCategoryName = incomeCat?.name,
                recurringIncome = txn.note?.contains("[BERULANG]") == true,
                note = txn.note?.replace("[BERULANG]", "")?.trim().orEmpty(),
                photoBlob = txn.photoBlob,
            )
        }
    }

    /**
     * Delete the currently-edited transaction. Balances + plan-item used
     * totals are reactive to the txn table so they recompute automatically.
     * No-op if not in edit mode.
     */
    fun delete() {
        val id = _state.value.editingId ?: return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            transactionRepo.delete(id)
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    fun submitExpense() {
        val s = _state.value
        if (s.amount <= 0 || s.accountId == null || s.planItemId == null) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            if (s.editingId == null) {
                addExpense(
                    amount = s.amount, date = s.date, planItemId = s.planItemId,
                    accountId = s.accountId, note = s.note.ifBlank { null },
                    photoBlob = s.photoBlob,
                    debtId = s.debtId,
                )
            } else {
                val existing = transactionRepo.getById(s.editingId)
                if (existing != null) {
                    transactionRepo.upsert(
                        existing.copy(
                            amount = s.amount, date = s.date,
                            sourceAccountId = s.accountId,
                            planItemId = s.planItemId,
                            note = s.note.ifBlank { null },
                            photoBlob = s.photoBlob,
                            debtId = s.debtId,
                        ),
                    )
                }
            }
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    fun submitIncome() {
        val s = _state.value
        if (s.amount <= 0 || s.accountId == null || s.incomeCategoryId == null) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val noteOut = listOfNotNull(
                "[BERULANG]".takeIf { s.recurringIncome },
                s.note.ifBlank { null },
            ).joinToString(" ").ifBlank { null }
            if (s.editingId == null) {
                addIncome(
                    amount = s.amount, date = s.date, accountId = s.accountId,
                    incomeCategoryId = s.incomeCategoryId, note = noteOut,
                )
            } else {
                val existing = transactionRepo.getById(s.editingId)
                if (existing != null) {
                    transactionRepo.upsert(
                        existing.copy(
                            amount = s.amount, date = s.date,
                            sourceAccountId = s.accountId,
                            incomeCategoryId = s.incomeCategoryId,
                            note = noteOut,
                        ),
                    )
                }
            }
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    fun submitTransfer() {
        val s = _state.value
        if (s.amount <= 0 || s.accountId == null || s.destAccountId == null) return
        if (s.accountId == s.destAccountId) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            if (s.editingId == null) {
                addTransfer(
                    amount = s.amount, date = s.date,
                    fromAccountId = s.accountId, toAccountId = s.destAccountId,
                    feeAmount = s.transferFee, note = s.note.ifBlank { null },
                    feePlanItemId = s.feePlanItemId,
                )
            } else {
                // Edit-in-place: update the Transfer row's fields. The
                // fee-as-Expense child row (when feePlanItemId is set on
                // create) is NOT auto-updated here — that's a known limit;
                // editing the fee on a transfer that previously booked a
                // child expense will leave the child untouched. Most users
                // edit amount/date/note/accounts, not fee — flag if/when
                // someone asks for it.
                val existing = transactionRepo.getById(s.editingId)
                if (existing != null) {
                    transactionRepo.upsert(
                        existing.copy(
                            amount = s.amount, date = s.date,
                            sourceAccountId = s.accountId,
                            destAccountId = s.destAccountId,
                            transferFee = s.transferFee.takeIf { it > 0 },
                            note = s.note.ifBlank { null },
                        ),
                    )
                }
            }
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }
}
