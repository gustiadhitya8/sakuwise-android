package com.gustiadhitya.sakuwise.feature.transaction.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class IndonesianReceiptParserTest {

    private val parser = IndonesianReceiptParser()

    @Test
    fun `total extracted when TOTAL and amount on same line`() {
        val text = """
            KOPI KENANGAN
            21/05/2026
            Es Kopi Susu 28.000
            Croffle 25.000
            TOTAL Rp 53.000
            Tunai Rp 60.000
            Kembalian Rp 7.000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(53_000L, draft.totalAmount)
    }

    @Test
    fun `total extracted when TOTAL and amount on SEPARATE lines (ML Kit split)`() {
        // Real ML Kit output for a wide-column receipt: TOTAL keyword and the
        // Rp amount end up on different lines because of large whitespace.
        val text = """
            KOPI KENANGAN
            21/05/2026 14:30
            Es Kopi Susu
            28.000
            Croffle
            25.000
            TOTAL
            Rp 53.000
            Tunai
            Rp 60.000
            Kembalian
            Rp 7.000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(53_000L, draft.totalAmount)
    }

    @Test
    fun `kembalian is excluded from total candidates`() {
        // If we naively took the max amount, "Kembalian" (change) would
        // hijack the total. The exclude list must filter it out.
        val text = """
            WARUNG MAKAN
            21/05/2026
            Nasi Goreng 35.000
            TOTAL Rp 35.000
            Tunai Rp 100.000
            Kembalian Rp 65.000
        """.trimIndent()
        val draft = parser.parse(text)
        // Tunai (100k) is the largest non-keyword non-kembalian — but TOTAL
        // line has highest score so 35k wins.
        assertEquals(35_000L, draft.totalAmount)
    }

    @Test
    fun `merchant extracted from first text line`() {
        val text = """
            KOPI KENANGAN
            Jl. Sudirman No. 12
            TOTAL Rp 53.000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals("KOPI KENANGAN", draft.merchant)
    }

    @Test
    fun `date extracted in DDMMYYYY format`() {
        val text = """
            KOPI KENANGAN
            21/05/2026 14:30
            TOTAL Rp 53.000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(LocalDate.of(2026, 5, 21), draft.date)
    }

    @Test
    fun `confidence is High when all three fields parsed`() {
        val text = """
            KOPI KENANGAN
            21/05/2026
            TOTAL Rp 53.000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(OcrConfidence.High, draft.confidence)
    }

    @Test
    fun `total returns null when no money amounts present`() {
        val text = """
            KOPI KENANGAN
            Terima kasih
        """.trimIndent()
        val draft = parser.parse(text)
        assertNull(draft.totalAmount)
    }

    @Test
    fun `large amounts without thousands separator are parsed`() {
        // 4+ digits with no separator should still match (e.g. "53000")
        val text = """
            WARUNG
            TOTAL Rp 53000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(53_000L, draft.totalAmount)
    }

    @Test
    fun `space-separated thousands also parsed`() {
        // Some Indonesian receipts use space groupings ("53 000")
        val text = """
            WARUNG
            TOTAL Rp 53 000
        """.trimIndent()
        val draft = parser.parse(text)
        assertEquals(53_000L, draft.totalAmount)
    }
}
