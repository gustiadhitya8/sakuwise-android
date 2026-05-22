package com.gustiadhitya.sakuwise.feature.settings.importexport

import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportRow(
    val date: LocalDate,
    val type: TxnType,
    val note: String?,
    val amount: Long,
)

data class ParseResult(
    val rows: List<ImportRow>,
    val skipped: Int,
    val errors: List<String>,
)

object TransactionCsvParser {

    private val DATE_FORMATS = listOf(
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd/M/yyyy"),
    )

    fun parse(csvText: String): ParseResult {
        // Strip BOM if present
        val text = csvText.trimStart('﻿')
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult(emptyList(), 0, listOf("File kosong"))

        val headerRow = parseCsvLine(lines.first()).map { it.trim().lowercase() }
        val dateIdx    = headerRow.indexOfFirst { it == "date" }
        val typeIdx    = headerRow.indexOfFirst { it == "type" }
        val categoryIdx= headerRow.indexOfFirst { it == "category" }
        val amountIdx  = headerRow.indexOfFirst { it == "amount" }
        val catatanIdx = headerRow.indexOfFirst { it == "note" || it == "catatan" }

        if (dateIdx < 0 || typeIdx < 0 || amountIdx < 0) {
            return ParseResult(
                emptyList(), 0,
                listOf("Header tidak valid. Kolom wajib: Date, Type, Amount"),
            )
        }

        val rows   = mutableListOf<ImportRow>()
        val errors = mutableListOf<String>()
        var skipped = 0

        for ((i, line) in lines.drop(1).withIndex()) {
            val lineNo = i + 2
            val cols = parseCsvLine(line)

            // Amount
            val amountRaw = cols.getOrNull(amountIdx)?.trim()
            if (amountRaw.isNullOrBlank()) { skipped++; continue }
            val amount = parseAmount(amountRaw)
            if (amount == null || amount <= 0) { skipped++; continue }

            // Date
            val dateRaw = cols.getOrNull(dateIdx)?.trim() ?: ""
            val date = parseDate(dateRaw)
            if (date == null) {
                errors.add("Baris $lineNo: format tanggal tidak dikenali: \"$dateRaw\"")
                skipped++; continue
            }

            // Type
            val typeRaw = cols.getOrNull(typeIdx)?.trim()?.lowercase() ?: ""
            val type = when (typeRaw) {
                "income"   -> TxnType.Income
                "expense"  -> TxnType.Expense
                "transfer" -> TxnType.Transfer
                else       -> { skipped++; continue }
            }

            // Note = merge Category + Catatan
            val category = if (categoryIdx >= 0) cols.getOrNull(categoryIdx)?.trim()?.ifBlank { null } else null
            val catatan  = if (catatanIdx  >= 0) cols.getOrNull(catatanIdx )?.trim()?.ifBlank { null } else null
            val note = listOfNotNull(category, catatan).joinToString(" – ").ifBlank { null }

            rows.add(ImportRow(date, type, note, amount))
        }

        return ParseResult(rows, skipped, errors)
    }

    // Handle "22000", "1300000", "Rp27.000.000", "1,300,000"
    private fun parseAmount(s: String): Long? =
        s.replace(Regex("[RpIDR ,.]"), "").trim().toLongOrNull()

    private fun parseDate(s: String): LocalDate? {
        for (fmt in DATE_FORMATS) {
            try { return LocalDate.parse(s, fmt) } catch (_: Exception) {}
        }
        return null
    }

    // RFC-4180-ish CSV parser: handles quoted fields with embedded commas/quotes
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes  -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"'); i++ // escaped quote
                    } else {
                        inQuotes = false
                    }
                }
                ch == ',' && !inQuotes -> { result.add(sb.toString()); sb.clear() }
                else -> sb.append(ch)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
