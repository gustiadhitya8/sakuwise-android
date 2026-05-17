package com.gustiadhitya.sakuwise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.crypto.BackupCryptoService
import com.gustiadhitya.sakuwise.core.domain.usecase.account.ObserveAccountsUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.account.UpsertAccountUseCase
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.AccountStatus
import com.gustiadhitya.sakuwise.core.model.AccountType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DataLayerTestViewModel @Inject constructor(
    private val observeAccountsUseCase: ObserveAccountsUseCase,
    private val upsertAccountUseCase: UpsertAccountUseCase,
    private val backupCryptoService: BackupCryptoService,
) : ViewModel() {

    val accountCount: StateFlow<Int> = observeAccountsUseCase()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    var encryptDecryptResult: String = ""
        private set

    fun insertDummyAccount() {
        viewModelScope.launch {
            upsertAccountUseCase(
                Account(
                    id = UUID.randomUUID().toString(),
                    name = "Dummy ${System.currentTimeMillis() % 1000}",
                    type = AccountType.CASH,
                    initialBalance = 0L,
                    color = null,
                    icon = null,
                    status = AccountStatus.ACTIVE,
                    createdAt = Instant.now(),
                )
            )
        }
    }

    fun testEncryptDecrypt(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val payload = "Sakuwise backup test payload".toByteArray()
            val pin = "testpin123"
            val encrypted = backupCryptoService.encryptBackup(payload, pin)
            val decrypted = backupCryptoService.decryptBackup(encrypted, pin)
            val wrongPin = backupCryptoService.decryptBackup(encrypted, "wrongpin")
            val result = if (
                decrypted.isSuccess &&
                decrypted.getOrNull()?.contentEquals(payload) == true &&
                wrongPin.isFailure
            ) "Match ✅" else "Mismatch ❌"
            onResult(result)
        }
    }
}
