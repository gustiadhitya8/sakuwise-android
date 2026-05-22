package com.gustiadhitya.sakuwise.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val txnRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _typeFilter = MutableStateFlow<TxnType?>(null)
    val typeFilter: StateFlow<TxnType?> = _typeFilter.asStateFlow()

    val transactions: StateFlow<List<Transaction>> =
        combine(_month, _typeFilter) { m, t -> Pair(m, t) }
            .flatMapLatest { (month, type) ->
                txnRepo.observeBetween(month.atDay(1), month.atEndOfMonth()).map { list ->
                    if (type != null) list.filter { it.type == type } else list
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

    fun prevMonth() { _month.value = _month.value.minusMonths(1) }
    fun nextMonth() {
        val now = YearMonth.now()
        if (_month.value < now) _month.value = _month.value.plusMonths(1)
    }
    fun setMonth(month: YearMonth) { _month.value = month.coerceAtMost(YearMonth.now()) }
    fun setTypeFilter(type: TxnType?) { _typeFilter.value = type }
}
