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
        val unresolvedItems: Int,
        val unresolvedItemDescriptions: List<String>, // "Kategori · Item (dd/MM)" for display
        val unresolvedAccounts: Int,
        val needsFallbackAccount: Boolean,
    ) : ImportUiState
    data class Importing(val done: Int, val total: Int) : ImportUiState
    data class Done(val imported: Int, val skipped: Int, val duplicates: Int = 0, val updated: Int = 0) : ImportUiState
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

                // Resolve accountName → accountId
                val allAccounts = accountRepo.observeAll().first()
                val accountByName = allAccounts.associateBy { it.name.trim().lowercase() }
                val withAccounts = resolved.map { row ->
                    if (row.accountName != null) {
                        row.copy(resolvedAccountId = accountByName[row.accountName.trim().lowercase()]?.id)
                    } else row
                }

                val unresolvedItems    = withAccounts.count { it.kategori != null && it.item != null && it.planItemId == null }
                val unresolvedAccounts = withAccounts.count { it.accountName != null && it.resolvedAccountId == null }
                val needsFallback      = withAccounts.any { it.resolvedAccountId == null }

                parsed.copy(rows = withAccounts) to Triple(unresolvedItems, unresolvedAccounts, needsFallback)
            }.fold(
                onSuccess = { (result, counts) ->
                    val (unresolvedItems, unresolvedAccounts, needsFallback) = counts
                    val dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
                    val unresolvedItemDescriptions = result.rows
                        .filter { it.kategori != null && it.item != null && it.planItemId == null }
                        .map { "${it.kategori} · ${it.item} (${it.date.format(dateFmt)})" }
                        .distinct()
                    _state.value = when {
                        result.rows.isEmpty() && result.errors.isNotEmpty() ->
                            ImportUiState.Err(result.errors.first())
                        result.rows.isEmpty() ->
                            ImportUiState.Err("Tidak ada baris valid ditemukan di file ini")
                        else ->
                            ImportUiState.Preview(
                                result.rows, result.skipped, result.errors,
                                unresolvedItems, unresolvedItemDescriptions,
                                unresolvedAccounts, needsFallback,
                            )
                    }
                },
                onFailure = { e ->
                    _state.value = ImportUiState.Err(e.message ?: "Gagal membaca file")
                },
            )
        }
    }

    /** [fallbackAccountId] is used for rows that have no resolved account from the Akun column.
     *  [updateMode] = true → update note on matching existing transactions instead of skipping. */
    fun importRows(rows: List<ImportRow>, fallbackAccountId: String?, updateMode: Boolean = false) {
        val parseSkipped = (_state.value as? ImportUiState.Preview)?.skipped ?: 0
        _state.value = ImportUiState.Importing(0, rows.size)
        viewModelScope.launch {
            var dupeSkipped = 0
            var imported = 0
            var updated = 0
            withContext(Dispatchers.IO) {
                val minDate = rows.minOf { it.date }
                val maxDate = rows.maxOf { it.date }
                val existing = transactionRepo.observeBetween(minDate, maxDate).first()

                // Include account in dedup key so same transaction on different wallets isn't deduplicated
                val existingByKey = existing.associateBy { t ->
                    val acct = t.sourceAccountId
                    if (t.planItemId != null)
                        "$acct|${t.date}|${t.amount}|${t.type}|pid:${t.planItemId}"
                    else
                        "$acct|${t.date}|${t.amount}|${t.type}|note:${t.note?.trim()?.lowercase()}"
                }

                // In update mode, also build a note-agnostic lookup for non-planItemId rows
                // so that existing null-note transactions can be matched and their note updated.
                // Uses ArrayDeque so duplicate base-key rows each consume a distinct match.
                val existingByBaseKey: MutableMap<String, ArrayDeque<com.gustiadhitya.sakuwise.core.domain.model.Transaction>> = mutableMapOf()
                if (updateMode) {
                    existing
                        .filter { it.planItemId == null && it.note.isNullOrBlank() }
                        .forEach { t ->
                            val baseKey = "${t.sourceAccountId}|${t.date}|${t.amount}|${t.type}"
                            existingByBaseKey.getOrPut(baseKey) { ArrayDeque() }.addLast(t)
                        }
                }

                rows.forEachIndexed { i, row ->
                    val accountId = row.resolvedAccountId ?: fallbackAccountId ?: run {
                        // No account to assign — skip rather than corrupt data
                        dupeSkipped++
                        _state.value = ImportUiState.Importing(i + 1, rows.size)
                        return@forEachIndexed
                    }
                    val key = if (row.planItemId != null)
                        "$accountId|${row.date}|${row.amount}|${row.type}|pid:${row.planItemId}"
                    else
                        "$accountId|${row.date}|${row.amount}|${row.type}|note:${row.note?.trim()?.lowercase()}"

                    // Exact match first; in update mode fall back to note-agnostic base key
                    val existingTxn = existingByKey[key] ?: if (updateMode && row.planItemId == null) {
                        val baseKey = "$accountId|${row.date}|${row.amount}|${row.type}"
                        existingByBaseKey[baseKey]?.removeFirstOrNull()
                    } else null
                    when {
                        existingTxn != null && updateMode -> {
                            transactionRepo.upsert(existingTxn.copy(note = row.note))
                            updated++
                        }
                        existingTxn != null -> dupeSkipped++
                        else -> {
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
                    }
                    _state.value = ImportUiState.Importing(i + 1, rows.size)
                }
            }
            _state.value = ImportUiState.Done(imported, parseSkipped + dupeSkipped, dupeSkipped, updated)
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

    fun deleteAllTransactions() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { transactionRepo.deleteAll() }
            _state.value = ImportUiState.Idle
        }
    }

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
