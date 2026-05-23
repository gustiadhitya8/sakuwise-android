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
    /**
     * Compressed JPEG (q=70, max edge ≤1600px) of the captured receipt. Per
     * PRD §7.11 the photo must be persisted as a BLOB on the resulting
     * Expense Transaction so the user can re-verify the receipt later.
     */
    val photoBlob: ByteArray? = null,
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

    // Primary keywords identify the canonical "total" amount line. Secondary
    // keywords ("tunai" cash, "bayar" payment) usually equal or exceed the
    // total — so they win on tiebreak unless we score them lower.
    private val primaryKeywords = listOf("total", "grand", "jumlah", "subtotal")
    private val secondaryKeywords = listOf("tunai", "bayar")
    // Exclude lines that mention change-given amounts ("kembalian" / "change")
    // and per-line discounts — those would otherwise win the "largest amount"
    // tiebreaker against the real total.
    private val excludeKeywords = listOf("kembalian", "change", "diskon", "discount")
    // Allow "Rp" prefix (optional), then digits with . , or space group separators
    // (e.g. "53.000", "53,000", "53 000"). Minimum 3 digits to avoid catching
    // table row numbers / qty.
    private val moneyRegex = Regex(
        """(?:Rp\s*)?([0-9]{1,3}(?:[.,\s][0-9]{3})+|[0-9]{4,})""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Total-extraction heuristic, robust to OCR's frequent split of "TOTAL …
     * Rp X" into two visual lines because of wide whitespace columns:
     *
     *  1. Walk every line, drop any line containing "kembalian"/"change" etc.
     *     (those are change-given amounts, not the total).
     *  2. For each remaining line, find all money matches and score them:
     *      • +1000 if the SAME line has a total keyword
     *      • +500  if the PREVIOUS line had a total keyword (handles the
     *        "TOTAL\nRp 53.000" split)
     *      • +0    otherwise (still eligible — receipts without explicit
     *        TOTAL labels exist)
     *  3. Pick the highest score. On ties, take the largest amount —
     *     receipts almost always have the total as the biggest figure once
     *     change/discount lines are excluded.
     */
    private fun extractTotal(lines: List<String>): Long? {
        data class Candidate(val amount: Long, val score: Int)
        val out = mutableListOf<Candidate>()
        // Track which kind of keyword sat on the previous line — needed for
        // ML Kit's frequent split of "TOTAL\nRp X".
        var prevPrimary = false
        var prevSecondary = false
        lines.forEach { line ->
            val lower = line.lowercase(Locale.ROOT)
            if (excludeKeywords.any { kw -> lower.contains(kw) }) {
                prevPrimary = false; prevSecondary = false
                return@forEach
            }
            val hasPrimary = primaryKeywords.any { kw -> lower.contains(kw) }
            val hasSecondary = !hasPrimary && secondaryKeywords.any { kw -> lower.contains(kw) }
            val score = when {
                hasPrimary -> 2000
                prevPrimary -> 1000
                hasSecondary -> 500
                prevSecondary -> 250
                else -> 0
            }
            moneyRegex.findAll(line).forEach { m ->
                val raw = m.groupValues[1]
                val amount = raw.replace(".", "").replace(",", "").replace(" ", "")
                    .toLongOrNull() ?: return@forEach
                if (amount >= 1000L) out += Candidate(amount, score)
            }
            prevPrimary = hasPrimary
            prevSecondary = hasSecondary
        }
        if (out.isEmpty()) return null
        val maxScore = out.maxOf { it.score }
        return out.filter { it.score == maxScore }.maxOf { it.amount }
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
