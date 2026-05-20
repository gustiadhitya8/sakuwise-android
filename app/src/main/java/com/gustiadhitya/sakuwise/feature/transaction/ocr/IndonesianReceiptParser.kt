package com.gustiadhitya.sakuwise.feature.transaction.ocr

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class OcrConfidence { High, Medium, Low }

data class ReceiptDraft(
    val merchant: String?,
    val date: LocalDate?,
    val totalAmount: Long?,
    val confidence: OcrConfidence,
)

/**
 * IndonesianReceiptParser — extracts merchant / date / total from a block of
 * receipt text per Tech Solution §11.3.
 *
 * Heuristics:
 *  - **Total**: looks for the largest Rupiah-formatted number on a line
 *    containing keywords "total", "tunai", "bayar", "jumlah".
 *  - **Date**: matches common Indonesian date formats (DD/MM/YYYY, DD-MM-YYYY,
 *    "DD Mei YYYY", "DD/MM/YY"), assumed local date.
 *  - **Merchant**: first non-empty, non-numeric line at the top of the receipt.
 */
@Singleton
class IndonesianReceiptParser @Inject constructor() {

    fun parse(text: String): ReceiptDraft {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val merchant = extractMerchant(lines)
        val date = extractDate(text)
        val total = extractTotal(lines)
        val signals = listOf(merchant != null, date != null, total != null).count { it }
        val confidence = when (signals) {
            3 -> OcrConfidence.High
            2 -> OcrConfidence.Medium
            else -> OcrConfidence.Low
        }
        return ReceiptDraft(merchant, date, total, confidence)
    }

    private fun extractMerchant(lines: List<String>): String? = lines.firstOrNull { line ->
        // Skip lines that are predominantly digits / Rp formatting
        val digitRatio = line.count { it.isDigit() }.toFloat() / line.length.coerceAtLeast(1)
        line.length in 3..40 && digitRatio < 0.3f && !line.contains("Rp", ignoreCase = true)
    }?.uppercase(Locale.ROOT)

    private val totalKeywords = listOf("total", "tunai", "bayar", "jumlah", "subtotal")
    private val moneyRegex = Regex("""(?:Rp\s*)?([0-9]{1,3}(?:[.,][0-9]{3})*|[0-9]+)""", RegexOption.IGNORE_CASE)

    private fun extractTotal(lines: List<String>): Long? {
        val candidates = lines.filter { line ->
            val lower = line.lowercase(Locale.ROOT)
            totalKeywords.any { kw -> lower.contains(kw) }
        }.ifEmpty { lines }

        return candidates
            .flatMap { line -> moneyRegex.findAll(line).map { it.groupValues[1] } }
            .mapNotNull { raw -> raw.replace(".", "").replace(",", "").toLongOrNull() }
            .filter { it >= 1000L } // skip qty/page numbers
            .maxOrNull()
    }

    private val datePatterns = listOf(
        Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})"""),
    )

    private val idMonthMap = mapOf(
        "januari" to 1, "februari" to 2, "maret" to 3, "april" to 4,
        "mei" to 5, "juni" to 6, "juli" to 7, "agustus" to 8,
        "september" to 9, "oktober" to 10, "november" to 11, "desember" to 12,
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "jun" to 6,
        "jul" to 7, "agu" to 8, "sep" to 9, "okt" to 10, "nov" to 11, "des" to 12,
    )
    private val idDateRegex = Regex("""(\d{1,2})\s+([a-zA-Z]+)\s+(\d{2,4})""")

    private fun extractDate(text: String): LocalDate? {
        for (pattern in datePatterns) {
            pattern.find(text)?.let { m ->
                val d = m.groupValues[1].toIntOrNull() ?: return@let
                val mo = m.groupValues[2].toIntOrNull() ?: return@let
                val y = m.groupValues[3].toIntOrNull()?.let { if (it < 100) 2000 + it else it } ?: return@let
                return runCatching { LocalDate.of(y, mo, d) }.getOrNull()
            }
        }
        idDateRegex.find(text)?.let { m ->
            val d = m.groupValues[1].toIntOrNull() ?: return@let
            val mo = idMonthMap[m.groupValues[2].lowercase(Locale.ROOT)] ?: return@let
            val y = m.groupValues[3].toIntOrNull()?.let { if (it < 100) 2000 + it else it } ?: return@let
            return runCatching { LocalDate.of(y, mo, d) }.getOrNull()
        }
        return null
    }
}
