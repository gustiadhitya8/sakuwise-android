package com.gustiadhitya.sakuwise.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Allocation
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class PlanItemMeta(
    val allocationId: String,
    val allocationName: String,
    val categoryId: String,
    val categoryName: String,
    val itemName: String,
)

data class CategoryOption(
    val id: String,
    val name: String,
    val allocationName: String,
)

data class PlanItemOption(
    val planItemId: String,
    val itemName: String,
    val categoryId: String,
    val categoryName: String,
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val txnRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val planRepo: PlanRepository,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _typeFilter = MutableStateFlow<TxnType?>(null)
    val typeFilter: StateFlow<TxnType?> = _typeFilter.asStateFlow()

    // Extra filters
    private val _selectedAccountIds    = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedAllocationIds = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedCategoryIds   = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedPlanItemIds   = MutableStateFlow<Set<String>>(emptySet())

    val selectedAccountIds:    StateFlow<Set<String>> = _selectedAccountIds.asStateFlow()
    val selectedAllocationIds: StateFlow<Set<String>> = _selectedAllocationIds.asStateFlow()
    val selectedCategoryIds:   StateFlow<Set<String>> = _selectedCategoryIds.asStateFlow()
    val selectedPlanItemIds:   StateFlow<Set<String>> = _selectedPlanItemIds.asStateFlow()

    val accounts: StateFlow<List<Account>> = accountRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Full plan item metadata — rebuilt whenever plan data changes
    private val _planItemMeta = MutableStateFlow<Map<String, PlanItemMeta>>(emptyMap())
    val planItemMeta: StateFlow<Map<String, PlanItemMeta>> = _planItemMeta.asStateFlow()

    private val _allocationOptions = MutableStateFlow<List<Allocation>>(emptyList())
    val allocationOptions: StateFlow<List<Allocation>> = _allocationOptions.asStateFlow()

    private val _categoryOptions = MutableStateFlow<List<CategoryOption>>(emptyList())
    val categoryOptions: StateFlow<List<CategoryOption>> = _categoryOptions.asStateFlow()

    private val _planItemOptions = MutableStateFlow<List<PlanItemOption>>(emptyList())
    val planItemOptions: StateFlow<List<PlanItemOption>> = _planItemOptions.asStateFlow()

    init {
        viewModelScope.launch {
            planRepo.observeAll().collect { plans ->
                val metaMap   = mutableMapOf<String, PlanItemMeta>()
                val allocs    = mutableListOf<Allocation>()
                val cats      = mutableListOf<CategoryOption>()
                val items     = mutableListOf<PlanItemOption>()
                for (plan in plans) {
                    val allocations = planRepo.observeAllocations(plan.id).first()
                    for (alloc in allocations) {
                        if (allocs.none { it.id == alloc.id }) allocs.add(alloc)
                        val categories = planRepo.observeCategories(alloc.id).first()
                        for (cat in categories) {
                            if (cats.none { it.id == cat.id })
                                cats.add(CategoryOption(cat.id, cat.name, alloc.name))
                            val planItems = planRepo.observePlanItems(cat.id).first()
                            for (pi in planItems) {
                                metaMap[pi.id] = PlanItemMeta(
                                    allocationId   = alloc.id,
                                    allocationName = alloc.name,
                                    categoryId     = cat.id,
                                    categoryName   = cat.name,
                                    itemName       = pi.name,
                                )
                                if (items.none { it.planItemId == pi.id })
                                    items.add(PlanItemOption(pi.id, pi.name, cat.id, cat.name))
                            }
                        }
                    }
                }
                _planItemMeta.value = metaMap
                _allocationOptions.value = allocs.sortedBy { it.sortOrder }
                _categoryOptions.value = cats
                _planItemOptions.value = items
            }
        }
    }

    // Combine all filter state into two nested pairs to avoid >5-arg combine
    private val _filterA = combine(_typeFilter, _selectedAccountIds, _selectedAllocationIds) { t, a, al ->
        Triple(t, a, al)
    }
    private val _filterB = combine(_selectedCategoryIds, _selectedPlanItemIds, _planItemMeta) { c, i, m ->
        Triple(c, i, m)
    }

    val transactions: StateFlow<List<Transaction>> =
        combine(_month, _filterA, _filterB) { month, fa, fb -> Triple(month, fa, fb) }
            .flatMapLatest { (month, fa, fb) ->
                val (type, accountIds, allocIds) = fa
                val (categoryIds, planItemIds, meta) = fb
                txnRepo.observeBetween(month.atDay(1), month.atEndOfMonth()).map { list ->
                    list.filter { t ->
                        (type == null || t.type == type) &&
                        (accountIds.isEmpty() || t.sourceAccountId in accountIds) &&
                        (allocIds.isEmpty() || (t.planItemId != null && meta[t.planItemId]?.allocationId in allocIds)) &&
                        (categoryIds.isEmpty() || (t.planItemId != null && meta[t.planItemId]?.categoryId in categoryIds)) &&
                        (planItemIds.isEmpty() || t.planItemId in planItemIds)
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountNames: StateFlow<Map<String, String>> = accountRepo.observeAll()
        .map { accs -> accs.associate { it.id to it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val monthIncome: StateFlow<Long> = _month.flatMapLatest { m ->
        txnRepo.observeIncomeBetween(m.atDay(1), m.atEndOfMonth())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthExpense: StateFlow<Long> = _month.flatMapLatest { m ->
        txnRepo.observeExpenseBetween(m.atDay(1), m.atEndOfMonth())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val extraFilterCount: StateFlow<Int> = combine(
        _selectedAccountIds, _selectedAllocationIds, _selectedCategoryIds, _selectedPlanItemIds,
    ) { accs, allocs, cats, items ->
        accs.size + allocs.size + cats.size + items.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() {
        if (_month.value < YearMonth.now()) _month.value = _month.value.plusMonths(1)
    }
    fun setMonth(month: YearMonth) { _month.value = month.coerceAtMost(YearMonth.now()) }
    fun setTypeFilter(type: TxnType?) { _typeFilter.value = type }

    fun toggleAccountFilter(id: String)    { _selectedAccountIds.toggle(id) }
    fun toggleAllocationFilter(id: String) { _selectedAllocationIds.toggle(id) }
    fun toggleCategoryFilter(id: String)   { _selectedCategoryIds.toggle(id) }
    fun togglePlanItemFilter(id: String)   { _selectedPlanItemIds.toggle(id) }

    fun clearExtraFilters() {
        _selectedAccountIds.value    = emptySet()
        _selectedAllocationIds.value = emptySet()
        _selectedCategoryIds.value   = emptySet()
        _selectedPlanItemIds.value   = emptySet()
    }

    private fun MutableStateFlow<Set<String>>.toggle(id: String) {
        value = value.toMutableSet().apply { if (contains(id)) remove(id) else add(id) }
    }
}
