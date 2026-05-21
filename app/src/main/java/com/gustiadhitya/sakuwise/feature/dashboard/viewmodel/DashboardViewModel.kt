package com.gustiadhitya.sakuwise.feature.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.PlanPeriod
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeCurrentPlanPeriodUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveActiveAccountsUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveCurrentPlanUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObservePeriodTotalsUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveRecentTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AllocationProgress(
    val allocation: Allocation,
    val plan: Long,
    val used: Long,
)

data class TopCategorySpend(
    val name: String,
    val amount: Long,
)

data class DashboardUiState(
    val nickname: String = "",
    val period: PlanPeriod? = null,
    val incomeMonth: Long = 0L,
    val expenseMonth: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val accountsTotal: Long = 0L,
    val allocations: List<AllocationProgress> = emptyList(),
    val topCategories: List<TopCategorySpend> = emptyList(),
    val backupOverdueDays: Int = 0,
    /** Epoch ms when user last opened the notification center. The bell's
     *  red dot only lights up when there's a notification whose source
     *  timestamp is newer than this value — opening the sheet sets it to
     *  now() so the dot disappears. */
    val notificationsLastSeenAt: Long = 0L,
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository,
    private val accountRepo: AccountRepository,
    private val planRepo: PlanRepository,
    private val transactionRepo: TransactionRepository,
    private val observeRecent: ObserveRecentTransactionsUseCase,
    private val observeAccounts: ObserveActiveAccountsUseCase,
    private val observeCurrentPlan: ObserveCurrentPlanUseCase,
    private val observePeriodTotals: ObservePeriodTotalsUseCase,
    private val computePlanPeriod: ComputeCurrentPlanPeriodUseCase,
) : ViewModel() {

    private val periodFlow = prefsRepo.prefs.map { prefs ->
        computePlanPeriod(planStartDay = prefs.planPeriodStartDay)
    }

    /** Per-allocation plan/used aggregated by summing all categories+items under each. */
    private val allocationsFlow = observeCurrentPlan().flatMapLatest { plan ->
        if (plan == null) flowOf(emptyList()) else {
            planRepo.observeAllocations(plan.id).flatMapLatest { allocs ->
                if (allocs.isEmpty()) flowOf(emptyList())
                else combine(allocs.map { a -> allocationProgressFlow(a) }) { it.toList() }
            }
        }
    }

    private fun allocationProgressFlow(alloc: Allocation) =
        planRepo.observeCategories(alloc.id).flatMapLatest { cats ->
            if (cats.isEmpty()) flowOf(AllocationProgress(alloc, 0L, 0L))
            else combine(cats.map { cat -> categoryTotalFlow(cat) }) { catTotals ->
                val plan = catTotals.sumOf { (p, _) -> p }
                val used = catTotals.sumOf { (_, u) -> u }
                AllocationProgress(alloc, plan, used)
            }
        }

    private fun categoryTotalFlow(cat: com.gustiadhitya.sakuwise.core.domain.model.Category) =
        planRepo.observePlanItems(cat.id).flatMapLatest { items ->
            if (items.isEmpty()) flowOf(Pair(cat.plannedAmount ?: 0L, 0L))
            else combine(items.map { pi -> planRepo.observePlanItemUsed(pi.id).map { pi to it } }) { arr ->
                val planSum = cat.plannedAmount ?: arr.sumOf { (pi, _) -> pi.plannedAmount }
                val usedSum = arr.sumOf { (_, u) -> u }
                Pair(planSum, usedSum)
            }
        }

    private val topCategoriesFlow = periodFlow.flatMapLatest { period ->
        transactionRepo.observeTopExpenseCategories(period.start, period.end, limit = 5)
    }

    val state: StateFlow<DashboardUiState> = combine(
        prefsRepo.prefs,
        periodFlow.flatMapLatest { period -> observePeriodTotals(period) },
        observeAccounts(),
        accountRepo.observeTotalBalance(),
        observeRecent(6),
        periodFlow,
        allocationsFlow,
        topCategoriesFlow,
    ) { args ->
        val prefs = args[0] as com.gustiadhitya.sakuwise.core.datastore.UserPreferences
        @Suppress("UNCHECKED_CAST")
        val totals = args[1] as Pair<Long, Long>
        @Suppress("UNCHECKED_CAST")
        val accounts = args[2] as List<Account>
        val accountsTotal = args[3] as Long
        @Suppress("UNCHECKED_CAST")
        val txns = args[4] as List<Transaction>
        val period = args[5] as PlanPeriod
        @Suppress("UNCHECKED_CAST")
        val allocs = args[6] as List<AllocationProgress>
        @Suppress("UNCHECKED_CAST")
        val tops = args[7] as List<com.gustiadhitya.sakuwise.core.domain.repository.TopExpenseCategory>
        DashboardUiState(
            nickname = prefs.userNickname,
            period = period,
            incomeMonth = totals.first,
            expenseMonth = totals.second,
            recentTransactions = txns,
            accounts = accounts,
            accountsTotal = accountsTotal,
            allocations = allocs,
            topCategories = tops.map { TopCategorySpend(name = it.name, amount = it.total) },
            backupOverdueDays = computeOverdueDays(prefs.lastBackupTimestamp),
            notificationsLastSeenAt = prefs.notificationsLastSeenAt,
            loading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(),
    )

    private fun computeOverdueDays(lastBackupMs: Long): Int {
        if (lastBackupMs == 0L) return Int.MAX_VALUE
        val diffMs = System.currentTimeMillis() - lastBackupMs
        return (diffMs / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    /** Persist the "seen" timestamp so the bell badge clears. Called when
     *  the user opens the notification center sheet. */
    fun markNotificationsSeen() {
        viewModelScope.launch {
            prefsRepo.markNotificationsSeenNow(System.currentTimeMillis())
        }
    }
}
