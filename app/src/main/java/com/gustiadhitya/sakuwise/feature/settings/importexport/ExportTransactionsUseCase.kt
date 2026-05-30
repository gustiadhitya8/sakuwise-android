package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class ExportFormat { Csv, Xlsx }

/**
 * Exports transactions to CSV or XLSX using a column schema that is
 * **identical to the import template** so that export → import is lossless.
 *
 * Column order (bilingual — parser accepts both ID and EN headers):
 *   Tanggal  | Tipe      | Kategori | Item | Akun    | Jumlah | Catatan
 *   Date     | Type      | Category | Item | Account | Amount | Note
 *
 * - Tanggal : YYYYMMDD
 * - Tipe    : Expense / Income / Transfer  (parser accepts Pengeluaran/Pemasukan too)
 * - Kategori: plan category name (expense) or income category name (income), empty for transfer
 * - Item    : plan item name (expense only), empty for income/transfer
 * - Akun    : account name
 * - Jumlah  : integer amount, no currency symbol or separators
 * - Catatan : raw note (no hacks)
 */
class ExportTransactionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val planRepo: PlanRepository,
) {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd")

    suspend operator fun invoke(
        start: LocalDate,
        end: LocalDate,
        format: ExportFormat,
    ): Result<Pair<Uri, Int>> = withContext(Dispatchers.IO) {
        runCatching {
            val txns           = transactionRepo.observeBetween(start, end).first()
            val nameById       = accountRepo.observeAll().first().associate { it.id to it.name }
            val planItemLookup = buildPlanItemLookup()
            val incomeLookup   = transactionRepo.observeIncomeCategories().first()
                                     .associate { it.id to it.name }

            // Headers match TransactionCsvParser.template() exactly.
            val headers = listOf("Tanggal", "Tipe", "Kategori", "Item", "Akun", "Jumlah", "Catatan")
            val rows    = txns.map { buildRow(it, nameById, planItemLookup, incomeLookup) }

            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val stamp      = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now())
            val ext        = if (format == ExportFormat.Csv) "csv" else "xlsx"
            val file       = File(exportsDir, "sakuwise_transaksi_$stamp.$ext")

            when (format) {
                ExportFormat.Csv  -> writeCsv(file, headers, rows)
                ExportFormat.Xlsx -> FileOutputStream(file).use { MinimalXlsxWriter.write(headers, rows, it) }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            uri to txns.size
        }
    }

    private fun buildRow(
        t: Transaction,
        nameById: Map<String, String>,
        planItemLookup: Map<String, Pair<String, String>>,
        incomeLookup: Map<String, String>,
    ): List<String> {
        val (kategori, item) = when {
            t.planItemId != null -> planItemLookup[t.planItemId] ?: ("" to "")
            t.incomeCategoryId != null -> (incomeLookup[t.incomeCategoryId] ?: "") to ""
            else -> "" to ""
        }

        val tipe = when (t.type) {
            TxnType.Income         -> "Income"
            TxnType.Expense        -> "Expense"
            TxnType.Transfer       -> "Transfer"
            TxnType.DebtInflow     -> "Income"
            TxnType.DebtOutflow    -> "Expense"
            TxnType.Reconciliation -> "Expense"
        }

        return listOf(
            t.date.format(dateFmt),
            tipe,
            kategori,
            item,
            nameById[t.sourceAccountId] ?: "",
            t.amount.toString(),
            t.note ?: "",
        )
    }

    /**
     * Builds planItemId → (categoryName, itemName) by traversing all plans.
     * Same traversal pattern as ImportTransactionViewModel.buildPlanItemLookup().
     */
    private suspend fun buildPlanItemLookup(): Map<String, Pair<String, String>> {
        val map = mutableMapOf<String, Pair<String, String>>()
        val plans = planRepo.observeAll().first()
        for (plan in plans) {
            val allocations = planRepo.observeAllocations(plan.id).first()
            for (alloc in allocations) {
                val categories = planRepo.observeCategories(alloc.id).first()
                for (cat in categories) {
                    val items = planRepo.observePlanItems(cat.id).first()
                    for (pi in items) {
                        map[pi.id] = cat.name to pi.name
                    }
                }
            }
        }
        return map
    }

    private fun writeCsv(file: File, headers: List<String>, rows: List<List<String>>) {
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { w ->
                w.write("﻿") // BOM so Excel opens without encoding dialog
                w.write(headers.joinToString(";") { csv(it) })
                w.write("\n")
                for (row in rows) {
                    w.write(row.joinToString(";") { csv(it) })
                    w.write("\n")
                }
            }
        }
    }

    private fun csv(s: String): String {
        if (s.none { it == ';' || it == '"' || it == '\n' || it == '\r' }) return s
        return "\"${s.replace("\"", "\"\"")}\""
    }
}
