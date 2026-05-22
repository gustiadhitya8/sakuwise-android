package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
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

class ExportTransactionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
) {
    private val dateFmt  = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val monthFmt = DateTimeFormatter.ofPattern("MMM-yyyy")

    suspend operator fun invoke(
        start: LocalDate,
        end: LocalDate,
        format: ExportFormat,
    ): Result<Pair<Uri, Int>> = withContext(Dispatchers.IO) {
        runCatching {
            val txns     = transactionRepo.observeBetween(start, end).first()
            val accounts = accountRepo.observeAll().first()
            val nameById = accounts.associate { it.id to it.name }

            val headers = listOf("Date", "Month", "Type", "Category", "Amount", "Catatan", "Account")
            val rows    = txns.map { buildRow(it, nameById) }

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

    private fun buildRow(t: Transaction, nameById: Map<String, String>): List<String> {
        val note = t.note ?: ""
        val (category, catatan) = if (" – " in note) {
            val p = note.split(" – ", limit = 2); p[0] to p[1]
        } else note to ""

        return listOf(
            t.date.format(dateFmt),
            t.date.format(monthFmt),
            when (t.type) {
                TxnType.Income        -> "Income"
                TxnType.Expense       -> "Expense"
                TxnType.Transfer      -> "Transfer"
                TxnType.DebtInflow    -> "Income"
                TxnType.DebtOutflow   -> "Expense"
                TxnType.Reconciliation-> "Expense"
            },
            category,
            t.amount.toString(),
            catatan,
            nameById[t.sourceAccountId] ?: t.sourceAccountId,
        )
    }

    private fun writeCsv(file: File, headers: List<String>, rows: List<List<String>>) {
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { w ->
                w.write("﻿") // BOM so Excel opens without encoding dialog
                w.write(headers.joinToString(",") { csv(it) })
                w.write("\n")
                for (row in rows) {
                    w.write(row.joinToString(",") { csv(it) })
                    w.write("\n")
                }
            }
        }
    }

    private fun csv(s: String): String {
        if (s.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return s
        return "\"${s.replace("\"", "\"\"")}\""
    }
}
