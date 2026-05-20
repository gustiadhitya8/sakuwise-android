package com.gustiadhitya.sakuwise.feature.asset.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ObserveActiveAccountsUseCase
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

data class AccountWithBalance(val account: Account, val balance: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountsListViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val observeAccounts: ObserveActiveAccountsUseCase,
) : ViewModel() {

    val accounts: StateFlow<List<AccountWithBalance>> = observeAccounts().flatMapLatest { list ->
        if (list.isEmpty()) flowOf(emptyList()) else {
            combine(list.map { acc -> accountRepo.observeBalance(acc.id).map { acc to it } }) { arr ->
                arr.map { (a, bal) -> AccountWithBalance(a, bal) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
