package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Parsing : ImportUiState
    data class Preview(
        val rows: List<ImportRow>,
        val skipped: Int,
        val errors: List<String>,
        val unresolvedItems: Int,   // rows where (kategori, item) didn't match any plan item
    ) : ImportUiState
    data class Importing(val done: Int, val total: Int) : ImportUiState
    data class Done(val imported: Int, val skipped: Int, val duplicates: Int = 0) : ImportUiState
    data class Err(val message: String) : ImportUiState
}

/** Lightweight reference used for (date, kategori, item) → planItemId resolution. */
private data class PlanItemRef(
    val planStart: LocalDate,
    val planEnd: LocalDate,
    val categoryNameLower: String,
    val itemNameLower: String,
    val planItemId: String,
)

@HiltViewModel
class ImportTransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val planRepo: PlanRepository,
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
                val parsed = withContext(Dispatchers.Default) { TransactionCsvParser.parse(text) }

                // Resolve (date, kategori, item) → planItemId for rows that have kategori + item
                val lookup = withContext(Dispatchers.IO) { buildPlanItemLookup() }
                val resolved = parsed.rows.map { row ->
                    if (row.kategori != null && row.item != null) {
                        val pid = lookup.firstOrNull { ref ->
                            !row.date.isBefore(ref.planStart) &&
                            !row.date.isAfter(ref.planEnd) &&
                            ref.categoryNameLower == row.kategori.trim().lowercase() &&
                            ref.itemNameLower     == row.item.trim().lowercase()
                        }?.planItemId
                        row.copy(planItemId = pid)
                    } else row
                }

                val unresolvedItems = resolved.count {
                    it.kategori != null && it.item != null && it.planItemId == null
                }

                parsed.copy(rows = resolved) to unresolvedItems
            }.fold(
                onSuccess = { (result, unresolved) ->
                    _state.value = when {
                        result.rows.isEmpty() && result.errors.isNotEmpty() ->
                            ImportUiState.Err(result.errors.first())
                        result.rows.isEmpty() ->
                            ImportUiState.Err("Tidak ada baris valid ditemukan di file ini")
                        else ->
                            ImportUiState.Preview(result.rows, result.skipped, result.errors, unresolved)
                    }
                },
                onFailure = { e ->
                    _state.value = ImportUiState.Err(e.message ?: "Gagal membaca file")
                },
            )
        }
    }

    fun importRows(rows: List<ImportRow>, accountId: String) {
        val parseSkipped = (_state.value as? ImportUiState.Preview)?.skipped ?: 0
        _state.value = ImportUiState.Importing(0, rows.size)
        viewModelScope.launch {
            var dupeSkipped = 0
            var imported = 0
            withContext(Dispatchers.IO) {
                // Dedup: load existing transactions for the date range
                val minDate = rows.minOf { it.date }
                val maxDate = rows.maxOf { it.date }
                val existing = transactionRepo.observeBetween(minDate, maxDate).first()

                // Dedup key: prefer (date, amount, type, planItemId) when planItemId is set,
                // else (date, amount, type, note)
                val existingKeys = existing.map { t ->
                    if (t.planItemId != null)
                        "${t.date}|${t.amount}|${t.type}|pid:${t.planItemId}"
                    else
                        "${t.date}|${t.amount}|${t.type}|note:${t.note?.trim()?.lowercase()}"
                }.toHashSet()

                rows.forEachIndexed { i, row ->
                    val key = if (row.planItemId != null)
                        "${row.date}|${row.amount}|${row.type}|pid:${row.planItemId}"
                    else
                        "${row.date}|${row.amount}|${row.type}|note:${row.note?.trim()?.lowercase()}"

                    if (key in existingKeys) {
                        dupeSkipped++
                    } else {
                        transactionRepo.upsert(
                            Transaction(
                                id               = UUID.randomUUID().toString(),
                                date             = row.date,
                                amount           = row.amount,
                                type             = row.type,
                                sourceAccountId  = accountId,
                                destAccountId    = null,
                                transferFee      = null,
                                planItemId       = row.planItemId,
                                debtId           = null,
                                photoBlob        = null,
                                incomeCategoryId = null,
                                note             = row.note,
                                createdAt        = System.currentTimeMillis(),
                            ),
                        )
                        imported++
                    }
                    _state.value = ImportUiState.Importing(i + 1, rows.size)
                }
            }
            _state.value = ImportUiState.Done(imported, parseSkipped + dupeSkipped, dupeSkipped)
        }
    }

    /** Writes the CSV template to cache and returns a shareable URI. */
    fun shareTemplate(): Uri {
        val dir  = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "sakuwise_template.csv")
        file.writeBytes(TransactionCsvParser.template())
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun reset() { _state.value = ImportUiState.Idle }

    /** Builds a flat list of (planStart, planEnd, kategori, item, planItemId) for all plans. */
    private suspend fun buildPlanItemLookup(): List<PlanItemRef> {
        val refs = mutableListOf<PlanItemRef>()
        val plans = planRepo.observeAll().first()
        for (plan in plans) {
            val allocations = planRepo.observeAllocations(plan.id).first()
            for (alloc in allocations) {
                val categories = planRepo.observeCategories(alloc.id).first()
                for (cat in categories) {
                    val items = planRepo.observePlanItems(cat.id).first()
                    for (pi in items) {
                        refs.add(
                            PlanItemRef(
                                planStart         = plan.start,
                                planEnd           = plan.end,
                                categoryNameLower = cat.name.trim().lowercase(),
                                itemNameLower     = pi.name.trim().lowercase(),
                                planItemId        = pi.id,
                            ),
                        )
                    }
                }
            }
        }
        return refs
    }
}
