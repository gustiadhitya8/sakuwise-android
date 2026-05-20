package com.gustiadhitya.sakuwise.feature.asset.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.UpsertAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AccountEditState(
    val id: String? = null,
    val name: String = "",
    val type: AccountType = AccountType.Cash,
    val initialBalance: Long = 0L,
    val archived: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val loaded: Boolean = false,
)

@HiltViewModel
class AccountEditViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val upsert: UpsertAccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountEditState())
    val state: StateFlow<AccountEditState> = _state

    /**
     * Always re-fetch from DB on entry. The earlier short-circuit
     * `if (loaded && id == accountId) return` preserved user's unsaved
     * in-memory edits (e.g. selecting a Tipe button without tapping Simpan)
     * across screen re-entries, leading to "Edit shows wrong Tipe selected"
     * bugs where the picked-but-unsaved state survived the back nav.
     */
    fun loadFor(accountId: String?) {
        if (accountId == null) {
            _state.value = AccountEditState(loaded = true)
            return
        }
        viewModelScope.launch {
            val acc = accountRepo.observeById(accountId).first()
            _state.value = if (acc == null) AccountEditState(loaded = true)
            else AccountEditState(
                id = acc.id, name = acc.name, type = acc.type,
                initialBalance = acc.initialBalance, archived = acc.archived,
                loaded = true,
            )
        }
    }

    fun setName(v: String) { _state.value = _state.value.copy(name = v) }
    fun setType(t: AccountType) { _state.value = _state.value.copy(type = t) }
    fun setBalance(v: Long) { _state.value = _state.value.copy(initialBalance = v) }
    fun setArchived(v: Boolean) { _state.value = _state.value.copy(archived = v) }

    fun submit() {
        val s = _state.value
        if (s.name.isBlank()) return
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val account = Account(
                id = s.id ?: UUID.randomUUID().toString(),
                name = s.name.trim(),
                type = s.type,
                initialBalance = s.initialBalance,
                iconName = s.type.code(),
                archived = s.archived,
            )
            upsert(account)
            _state.value = _state.value.copy(saving = false, saved = true)
        }
    }
}
