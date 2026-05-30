package com.gustiadhitya.sakuwise.feature.settings.importexport

import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportRow(
    val date: LocalDate,
    val type: TxnType,
    val kategori: String?,          // raw Category/Kategori column — for plan item lookup + display
    val item: String?,              // raw Item column — for plan item lookup + display
    val note: String?,              // raw Note/Catatan column — stored as transaction note
    val amount: Long,
    val accountName: String?,       // raw Akun/Account column — resolved in ViewModel
    val planItemId: String? = null,        // resolved in ViewModel after DB lookup
    val resolvedAccountId: String? = null, // resolved in ViewModel after account lookup
)

data class ParseResult(
    val rows: List<ImportRow>,
    val skipped: Int,
    val errors: List<String>,
)

object TransactionCsvParser {

    /**
     * Canonical column schema, in order. Export writes exactly these columns
     * in this order, and the import template uses them, so export → import is
     * lossless by construction. The parser accepts either the ID or EN header
     * names (and is order-independent), but keeping export aligned to this
     * single source prevents schema drift. (Item 8, v1.0.4.)
     */
    val CANONICAL_HEADERS_ID = listOf("Tanggal", "Tipe", "Kategori", "Item", "Akun", "Jumlah", "Catatan")
    val CANONICAL_HEADERS_EN = listOf("Date", "Type", "Category", "Item", "Account", "Amount", "Note")

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

        // Strip parenthetical suffixes so "Catatan (Opsional)" matches as "catatan"
        val headerRow = parseCsvLine(lines.first()).map { it.trim().lowercase().substringBefore("(").trim() }

        // Bilingual column lookup — Indonesian first, English alias second
        fun col(vararg names: String) = headerRow.indexOfFirst { it in names }

        val dateIdx     = col("tanggal", "date")
        val typeIdx     = col("tipe", "type")
        val kategoriIdx = col("kategori", "category")
        val itemIdx     = col("item")   // same word in both ID and EN
        val amountIdx   = col("jumlah", "amount")
        val noteIdx     = col("catatan", "note")
        val akunIdx     = col("akun", "account")

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
            val akun     = if (akunIdx     >= 0) cols.getOrNull(akunIdx    )?.trim()?.ifBlank { null } else null

            rows.add(ImportRow(date, type, kategori, item, note, amount, akun))
        }

        return ParseResult(rows, skipped, errors)
    }

    // Generates a UTF-8 BOM CSV template with bilingual headers and two example rows.
    fun template(): ByteArray {
        val lines = listOf(
            CANONICAL_HEADERS_ID.joinToString(";"),
            "20260503;Expense;Bulanan Gusti;Makan;BCA;45000;ShopeeFood: Egg Roll, Basgor, Nugger",
            "20260503;Expense;Bulanan Gusti;Kopi;Gopay;18000;Lawson: Caffe Latte",
            "20260501;Income;;;BCA;5000000;Gaji Mei",
        )
        return ("﻿" + lines.joinToString("\n")).toByteArray(Charsets.UTF_8)
    }

    private fun parseAmount(s: String): Long? =
        s.replace(Regex("[RpIDR ,.]"), "").trim().toLongOrNull()

    private fun parseDate(s: String): LocalDate? {
        for (fmt in DATE_FORMATS) {
            // Intentional: probe each supported format; a parse miss is expected
            // control flow (we fall through to the next format), NOT a swallowed
            // crash. Returns null if none match, which the caller handles.
            try {
                return LocalDate.parse(s, fmt)
            } catch (_: java.time.format.DateTimeParseException) {
                // try next format
            }
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
