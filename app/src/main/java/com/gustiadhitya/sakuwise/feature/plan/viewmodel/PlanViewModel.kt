package com.gustiadhitya.sakuwise.feature.plan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Category
import com.gustiadhitya.sakuwise.core.domain.model.Plan
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.Recurrence
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ApplyStarterTemplateUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ComputeCurrentPlanPeriodUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.CreatePlanUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveCurrentPlanUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.RegenerateNextPlanUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.ResetPlanUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.SetExpectedIncomeUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.SetupDefaultAllocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PlanItemRow(val item: PlanItem, val used: Long)
data class CategoryRow(
    val category: Category,
    val items: List<PlanItemRow>,
    val plan: Long,
    val used: Long,
)
data class AllocationRow(
    val allocation: Allocation,
    val categories: List<CategoryRow>,
    val plan: Long,
    val used: Long,
)

data class PlanScreenState(
    val plan: Plan? = null,
    val allocations: List<AllocationRow> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepo: PlanRepository,
    private val transactionRepo: TransactionRepository,
    private val prefsRepo: UserPreferencesRepository,
    private val observeCurrentPlan: ObserveCurrentPlanUseCase,
    private val setExpectedIncome: SetExpectedIncomeUseCase,
    private val resetPlan: ResetPlanUseCase,
    private val applyStarter: ApplyStarterTemplateUseCase,
    private val regenerateNextPlan: RegenerateNextPlanUseCase,
    private val regenerateRecurringIncomes: com.gustiadhitya.sakuwise.core.domain.usecase.RegenerateRecurringIncomesUseCase,
    private val createPlan: CreatePlanUseCase,
    private val setupDefaultAllocations: SetupDefaultAllocationsUseCase,
    private val computePlanPeriod: ComputeCurrentPlanPeriodUseCase,
) : ViewModel() {

    /** All plans (current + history) — used by the month-picker sheet. */
    val allPlans: StateFlow<List<Plan>> = planRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * When non-null, the Plan tab renders this plan instead of the auto-computed
     * current one. Declared here (before `state`) because `state` reads it.
     */
    private val _viewedPlanId = MutableStateFlow<String?>(null)
    val viewedPlanId: StateFlow<String?> = _viewedPlanId
    fun setViewedPlan(id: String?) { _viewedPlanId.value = id }

    // When _viewedPlanId is null we follow the current period. When set, we
    // observe that specific plan instead. This lets MonthPickerSheet switch
    // the viewed plan without changing the device clock.
    val state: StateFlow<PlanScreenState> = kotlinx.coroutines.flow.combine(
        observeCurrentPlan(),
        _viewedPlanId,
    ) { current, viewedId -> current to viewedId }.flatMapLatest { (current, viewedId) ->
        val pickedFlow = if (viewedId == null) flowOf(current)
        else planRepo.observeById(viewedId)
        pickedFlow.flatMapLatest { plan ->
            if (plan == null) flowOf(PlanScreenState(loading = false)) else {
                planRepo.observeAllocations(plan.id).flatMapLatest { allocs ->
                    if (allocs.isEmpty()) flowOf(PlanScreenState(plan = plan, loading = false))
                    else combine(allocs.map { alloc -> allocationRowFlow(alloc) }) { rows ->
                        PlanScreenState(plan = plan, allocations = rows.toList(), loading = false)
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanScreenState())

    private fun allocationRowFlow(alloc: Allocation) =
        planRepo.observeCategories(alloc.id).flatMapLatest { cats ->
            if (cats.isEmpty()) flowOf(AllocationRow(alloc, emptyList(), 0L, 0L))
            else combine(cats.map { cat -> categoryRowFlow(cat) }) { rows ->
                val cs = rows.toList()
                AllocationRow(
                    allocation = alloc,
                    categories = cs,
                    plan = cs.sumOf { it.plan },
                    used = cs.sumOf { it.used },
                )
            }
        }

    private fun categoryRowFlow(cat: Category) =
        planRepo.observePlanItems(cat.id).flatMapLatest { items ->
            if (items.isEmpty()) flowOf(CategoryRow(cat, emptyList(), cat.plannedAmount ?: 0L, 0L))
            else combine(items.map { pi -> piRowFlow(pi) }) { rows ->
                val list = rows.toList()
                CategoryRow(
                    category = cat,
                    items = list,
                    plan = list.sumOf { it.item.plannedAmount },
                    used = list.sumOf { it.used },
                )
            }
        }

    private fun piRowFlow(item: PlanItem) =
        planRepo.observePlanItemUsed(item.id).map { used -> PlanItemRow(item, used) }

    fun addPlanItem(categoryId: String, name: String, amount: Long, recurrence: Recurrence) {
        viewModelScope.launch {
            val existing = planRepo.observePlanItems(categoryId).first()
            val sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
            planRepo.upsertPlanItem(
                PlanItem(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    name = name,
                    plannedAmount = amount,
                    recurrence = recurrence,
                    note = null,
                    sortOrder = sortOrder,
                ),
            )
        }
    }

    fun updatePlanItem(item: PlanItem) {
        viewModelScope.launch { planRepo.upsertPlanItem(item) }
    }

    fun deletePlanItem(id: String) {
        viewModelScope.launch { planRepo.deletePlanItem(id) }
    }

    fun setExpectedIncomeAmount(amount: Long) {
        viewModelScope.launch {
            val plan = state.value.plan ?: return@launch
            setExpectedIncome(plan, amount)
        }
    }

    fun resetCurrentPlan() {
        viewModelScope.launch {
            val plan = state.value.plan ?: return@launch
            resetPlan(plan.id)
        }
    }

    fun addCategoryToAllocation(allocationId: String, name: String) {
        viewModelScope.launch {
            planRepo.upsertCategory(
                Category(
                    id = UUID.randomUUID().toString(),
                    allocationId = allocationId,
                    name = name,
                    plannedAmount = null,
                    sortOrder = 99,
                ),
            )
        }
    }

    /**
     * Update allocation percentages for THIS plan only (PRD §7.3).
     *
     * Pass any subset by id → new targetPct. Other allocations are left alone.
     * Caller is expected to ensure the visible 3-allocation set sums to 100.
     */
    fun updateAllocationPcts(updates: Map<String, Int>) {
        viewModelScope.launch {
            val plan = state.value.plan ?: return@launch
            val allocs = planRepo.observeAllocations(plan.id).first()
            updates.forEach { (id, pct) ->
                val a = allocs.firstOrNull { it.id == id } ?: return@forEach
                planRepo.upsertAllocation(a.copy(targetPct = pct))
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            // Cascade: delete items first, then the category
            val items = planRepo.observePlanItems(categoryId).first()
            for (it in items) planRepo.deletePlanItem(it.id)
            planRepo.deleteCategory(categoryId)
        }
    }

    /** Rename an existing category (id + allocationId + items are preserved). */
    fun renameCategory(category: Category, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == category.name) return
        viewModelScope.launch {
            planRepo.upsertCategory(category.copy(name = trimmed))
        }
    }

    fun moveCategoryUp(category: Category) {
        viewModelScope.launch {
            val siblings = planRepo.observeCategories(category.allocationId).first()
                .sortedBy { it.sortOrder }
                .mapIndexed { i, cat -> cat.copy(sortOrder = i) }
            val idx = siblings.indexOfFirst { it.id == category.id }
            if (idx <= 0) return@launch
            val prev = siblings[idx - 1]
            val curr = siblings[idx]
            planRepo.upsertCategory(curr.copy(sortOrder = prev.sortOrder))
            planRepo.upsertCategory(prev.copy(sortOrder = curr.sortOrder))
        }
    }

    fun moveCategoryDown(category: Category) {
        viewModelScope.launch {
            val siblings = planRepo.observeCategories(category.allocationId).first()
                .sortedBy { it.sortOrder }
                .mapIndexed { i, cat -> cat.copy(sortOrder = i) }
            val idx = siblings.indexOfFirst { it.id == category.id }
            if (idx < 0 || idx >= siblings.size - 1) return@launch
            val next = siblings[idx + 1]
            val curr = siblings[idx]
            planRepo.upsertCategory(curr.copy(sortOrder = next.sortOrder))
            planRepo.upsertCategory(next.copy(sortOrder = curr.sortOrder))
        }
    }

    fun movePlanItemUp(item: PlanItem) {
        viewModelScope.launch {
            // Normalize to unique indices first — guards against all-zero sortOrders
            val siblings = planRepo.observePlanItems(item.categoryId).first()
                .sortedBy { it.sortOrder }
                .mapIndexed { i, pi -> pi.copy(sortOrder = i) }
            val idx = siblings.indexOfFirst { it.id == item.id }
            if (idx <= 0) return@launch
            val prev = siblings[idx - 1]
            val curr = siblings[idx]
            planRepo.upsertPlanItem(curr.copy(sortOrder = prev.sortOrder))
            planRepo.upsertPlanItem(prev.copy(sortOrder = curr.sortOrder))
        }
    }

    fun movePlanItemDown(item: PlanItem) {
        viewModelScope.launch {
            val siblings = planRepo.observePlanItems(item.categoryId).first()
                .sortedBy { it.sortOrder }
                .mapIndexed { i, pi -> pi.copy(sortOrder = i) }
            val idx = siblings.indexOfFirst { it.id == item.id }
            if (idx < 0 || idx >= siblings.size - 1) return@launch
            val next = siblings[idx + 1]
            val curr = siblings[idx]
            planRepo.upsertPlanItem(curr.copy(sortOrder = next.sortOrder))
            planRepo.upsertPlanItem(next.copy(sortOrder = curr.sortOrder))
        }
    }

    fun movePlanItemToCategory(item: PlanItem, targetCategoryId: String) {
        viewModelScope.launch {
            val targetItems = planRepo.observePlanItems(targetCategoryId).first()
            val newSortOrder = (targetItems.maxOfOrNull { it.sortOrder } ?: -1) + 1
            planRepo.upsertPlanItem(
                item.copy(categoryId = targetCategoryId, sortOrder = newSortOrder),
            )
        }
    }

    fun applyStarterTemplateToCurrentPlan() {
        viewModelScope.launch {
            // First-launch path: if no Plan entity exists for the current period,
            // create one and seed the 3 default allocations before applying the template.
            val plan = state.value.plan ?: run {
                val prefs = prefsRepo.prefs.first()
                val period = computePlanPeriod(planStartDay = prefs.planPeriodStartDay)
                createPlan(period, expectedIncome = 0L).getOrNull() ?: return@launch
            }
            val allocations = planRepo.observeAllocations(plan.id).first().ifEmpty {
                setupDefaultAllocations(plan.id).getOrNull().orEmpty()
            }
            if (allocations.isNotEmpty()) applyStarter(plan.id, allocations)
        }
    }

    /**
     * "Start from scratch" path — creates the plan + the 3 default allocation
     * buckets (Kebutuhan / Keinginan / Investasi) but NO categories. Used by
     * the empty-state CTA so the user can immediately add their first category
     * via the existing "+ Tambah kategori" buttons that are scoped per
     * allocation. Without this step the per-allocation add buttons can't render
     * because there are no allocation containers yet.
     */
    fun setupEmptyPlanWithAllocations(onReady: () -> Unit = {}) {
        viewModelScope.launch {
            val plan = state.value.plan ?: run {
                val prefs = prefsRepo.prefs.first()
                val period = computePlanPeriod(planStartDay = prefs.planPeriodStartDay)
                createPlan(period, expectedIncome = 0L).getOrNull() ?: return@launch
            }
            val existing = planRepo.observeAllocations(plan.id).first()
            if (existing.isEmpty()) {
                setupDefaultAllocations(plan.id).getOrNull()
            }
            onReady()
        }
    }

    /**
     * PRD §7.5 — generate the NEXT period's plan after the latest existing one.
     *
     * Bug fix: previously used `state.value.plan` (the *current* plan) as the
     * base, so clicking twice tried to create the same next-month plan twice
     * (duplicate row, no visible progress). Now we always base off the latest
     * plan by end-date, so chained clicks generate Jun → Jul → Aug …
     */
    fun regenerateNextPeriodPlan() {
        viewModelScope.launch {
            val all = allPlans.value
            val base = all.maxByOrNull { it.end } ?: state.value.plan ?: return@launch
            val startDay = prefsRepo.prefs.first().planPeriodStartDay
            val result = regenerateNextPlan(previousPlan = base, planStartDay = startDay)
            _planCreatedResult.value = result.getOrNull()?.label
        }
    }

    private val _planCreatedResult = MutableStateFlow<String?>(null)
    val planCreatedResult: StateFlow<String?> = _planCreatedResult
    fun clearPlanCreatedResult() { _planCreatedResult.value = null }

    private val _recurringResult = MutableStateFlow<Int?>(null)
    val recurringResult: StateFlow<Int?> = _recurringResult

    fun regenerateRecurringIncomesNow() {
        viewModelScope.launch {
            val n = regenerateRecurringIncomes().getOrNull() ?: 0
            _recurringResult.value = n
        }
    }
    fun clearRecurringResult() { _recurringResult.value = null }
}
