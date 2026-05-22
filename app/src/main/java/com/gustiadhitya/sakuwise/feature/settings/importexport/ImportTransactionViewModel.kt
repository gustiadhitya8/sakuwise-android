package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Parsing : ImportUiState
    data class Preview(
        val rows: List<ImportRow>,
        val skipped: Int,
        val errors: List<String>,
    ) : ImportUiState
    data class Importing(val done: Int, val total: Int) : ImportUiState
    data class Done(val imported: Int, val skipped: Int) : ImportUiState
    data class Err(val message: String) : ImportUiState
}

@HiltViewModel
class ImportTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    val accounts: StateFlow<List<Account>> = accountRepo.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun parseFile(uri: Uri) {
        _state.value = ImportUiState.Parsing
        viewModelScope.launch {
            runCatching {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.readText()
                        ?: error("Tidak bisa membuka file")
                }
                withContext(Dispatchers.Default) { TransactionCsvParser.parse(text) }
            }.fold(
                onSuccess = { result ->
                    _state.value = when {
                        result.rows.isEmpty() && result.errors.isNotEmpty() ->
                            ImportUiState.Err(result.errors.first())
                        result.rows.isEmpty() ->
                            ImportUiState.Err("Tidak ada baris valid ditemukan di file ini")
                        else ->
                            ImportUiState.Preview(result.rows, result.skipped, result.errors)
                    }
                },
                onFailure = { e ->
                    _state.value = ImportUiState.Err(e.message ?: "Gagal membaca file")
                },
            )
        }
    }

    fun importRows(rows: List<ImportRow>, accountId: String) {
        val skipped = (_state.value as? ImportUiState.Preview)?.skipped ?: 0
        _state.value = ImportUiState.Importing(0, rows.size)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                rows.forEachIndexed { i, row ->
                    transactionRepo.upsert(
                        Transaction(
                            id               = UUID.randomUUID().toString(),
                            date             = row.date,
                            amount           = row.amount,
                            type             = row.type,
                            sourceAccountId  = accountId,
                            destAccountId    = null,
                            transferFee      = null,
                            planItemId       = null,
                            debtId           = null,
                            photoBlob        = null,
                            incomeCategoryId = null,
                            note             = row.note,
                            createdAt        = System.currentTimeMillis(),
                        ),
                    )
                    _state.value = ImportUiState.Importing(i + 1, rows.size)
                }
            }
            _state.value = ImportUiState.Done(rows.size, skipped)
        }
    }

    fun reset() { _state.value = ImportUiState.Idle }
}
