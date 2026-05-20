package com.gustiadhitya.sakuwise.feature.asset.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.ReconcileAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailState(
    val account: Account? = null,
    val balance: Long = 0,
    val snapshots: List<AccountSnapshot> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val reconcile: ReconcileAccountUseCase,
) : ViewModel() {

    private val accountId = MutableStateFlow<String?>(null)

    fun bind(id: String) {
        if (accountId.value != id) accountId.value = id
    }

    val state: StateFlow<AccountDetailState> = accountId.flatMapLatest { id ->
        if (id == null) flowOf(AccountDetailState()) else {
            combine(
                accountRepo.observeById(id),
                accountRepo.observeBalance(id),
                accountRepo.observeSnapshots(id),
                transactionRepo.observeForAccount(id),
            ) { account, balance, snapshots, txns ->
                AccountDetailState(account, balance, snapshots, txns.take(25))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountDetailState())

    private val _reconcileMessage = MutableStateFlow<String?>(null)
    val reconcileMessage: StateFlow<String?> = _reconcileMessage

    fun submitReconciliation(observedBalance: Long, note: String?) {
        val id = accountId.value ?: return
        viewModelScope.launch {
            val diff = reconcile(id, observedBalance, note = note).getOrNull()
            _reconcileMessage.value = when {
                diff == null -> "Gagal rekonsiliasi"
                diff == 0L -> "Saldo sudah sesuai"
                diff > 0L -> "Penyesuaian +Rp $diff disimpan"
                else -> "Penyesuaian -Rp ${-diff} disimpan"
            }
        }
    }

    fun clearMessage() { _reconcileMessage.value = null }

    fun updateSnapshotNote(snap: AccountSnapshot, newNote: String?) {
        viewModelScope.launch {
            accountRepo.upsertSnapshot(snap.copy(note = newNote))
        }
    }

    fun deleteSnapshot(snapshotId: String) {
        viewModelScope.launch { accountRepo.deleteSnapshot(snapshotId) }
    }
}
