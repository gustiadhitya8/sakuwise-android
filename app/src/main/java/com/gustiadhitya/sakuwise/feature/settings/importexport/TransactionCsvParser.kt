package com.gustiadhitya.sakuwise.feature.settings.importexport

import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportRow(
    val date: LocalDate,
    val type: TxnType,
    val kategori: String?,   // raw Category/Kategori column — for plan item lookup + display
    val item: String?,       // raw Item column — for plan item lookup + display
    val note: String?,       // raw Note/Catatan column — stored as transaction note
    val amount: Long,
    val planItemId: String? = null, // resolved in ViewModel after DB lookup
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
        val text = csvText.trimStart('﻿')
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult(emptyList(), 0, listOf("File kosong"))

        val headerRow = parseCsvLine(lines.first()).map { it.trim().lowercase() }

        // Bilingual column lookup — Indonesian first, English alias second
        fun col(vararg names: String) = headerRow.indexOfFirst { it in names }

        val dateIdx     = col("tanggal", "date")
        val typeIdx     = col("tipe", "type")
        val kategoriIdx = col("kategori", "category")
        val itemIdx     = col("item")
        val amountIdx   = col("jumlah", "amount")
        val noteIdx     = col("catatan", "note")

        if (dateIdx < 0 || typeIdx < 0 || amountIdx < 0) {
            return ParseResult(
                emptyList(), 0,
                listOf("Header tidak valid. Kolom wajib: Tanggal, Tipe, Jumlah (atau Date, Type, Amount)"),
            )
        }

        val rows   = mutableListOf<ImportRow>()
        val errors = mutableListOf<String>()
        var skipped = 0

        for ((i, line) in lines.drop(1).withIndex()) {
            val lineNo = i + 2
            val cols = parseCsvLine(line)

            val amountRaw = cols.getOrNull(amountIdx)?.trim()
            if (amountRaw.isNullOrBlank()) { skipped++; continue }
            val amount = parseAmount(amountRaw)
            if (amount == null || amount <= 0) { skipped++; continue }

            val dateRaw = cols.getOrNull(dateIdx)?.trim() ?: ""
            val date = parseDate(dateRaw)
            if (date == null) {
                errors.add("Baris $lineNo: format tanggal tidak dikenali: \"$dateRaw\"")
                skipped++; continue
            }

            val typeRaw = cols.getOrNull(typeIdx)?.trim()?.lowercase() ?: ""
            val type = when (typeRaw) {
                "income", "pemasukan"   -> TxnType.Income
                "expense", "pengeluaran" -> TxnType.Expense
                "transfer"              -> TxnType.Transfer
                else -> { skipped++; continue }
            }

            val kategori = if (kategoriIdx >= 0) cols.getOrNull(kategoriIdx)?.trim()?.ifBlank { null } else null
            val item     = if (itemIdx     >= 0) cols.getOrNull(itemIdx    )?.trim()?.ifBlank { null } else null
            val note     = if (noteIdx     >= 0) cols.getOrNull(noteIdx    )?.trim()?.ifBlank { null } else null

            rows.add(ImportRow(date, type, kategori, item, note, amount))
        }

        return ParseResult(rows, skipped, errors)
    }

    // Generates a UTF-8 BOM CSV template with bilingual headers and two example rows.
    fun template(): ByteArray {
        val lines = listOf(
            "Tanggal;Tipe;Kategori;Item;Jumlah;Catatan",
            "20260503;Expense;Bulanan Gusti;Makan;45000;ShopeeFood: Egg Roll, Basgor, Nugger",
            "20260503;Expense;Bulanan Gusti;Kopi;18000;Lawson: Caffe Latte",
            "20260501;Income;;;5000000;Gaji Mei",
        )
        return ("﻿" + lines.joinToString("\n")).toByteArray(Charsets.UTF_8)
    }

    private fun parseAmount(s: String): Long? =
        s.replace(Regex("[RpIDR ,.]"), "").trim().toLongOrNull()

    private fun parseDate(s: String): LocalDate? {
        for (fmt in DATE_FORMATS) {
            try { return LocalDate.parse(s, fmt) } catch (_: Exception) {}
        }
        return null
    }

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
                        sb.append('"'); i++
                    } else {
                        inQuotes = false
                    }
                }
                ch == ';' && !inQuotes -> { result.add(sb.toString()); sb.clear() }
                else -> sb.append(ch)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
