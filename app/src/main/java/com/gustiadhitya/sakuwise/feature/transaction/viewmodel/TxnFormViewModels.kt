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
    ) {
        _state.value = _state.value.copy(
            amount = amount ?: _state.value.amount,
            date = date ?: _state.value.date,
            note = listOfNotNull(merchant, _state.value.note.ifBlank { null }).joinToString(" · "),
        )
    }
    fun swap() {
        val s = _state.value
        _state.value = s.copy(accountId = s.destAccountId, destAccountId = s.accountId)
    }

    fun submitExpense() {
        val s = _state.value
        if (s.amount <= 0 || s.accountId == null || s.planItemId == null) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            addExpense(
                amount = s.amount, date = s.date, planItemId = s.planItemId,
                accountId = s.accountId, note = s.note.ifBlank { null },
                debtId = s.debtId,
            )
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
            addIncome(
                amount = s.amount, date = s.date, accountId = s.accountId,
                incomeCategoryId = s.incomeCategoryId, note = noteOut,
            )
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }

    fun submitTransfer() {
        val s = _state.value
        if (s.amount <= 0 || s.accountId == null || s.destAccountId == null) return
        if (s.accountId == s.destAccountId) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            addTransfer(
                amount = s.amount, date = s.date,
                fromAccountId = s.accountId, toAccountId = s.destAccountId,
                feeAmount = s.transferFee, note = s.note.ifBlank { null },
            )
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }
}
