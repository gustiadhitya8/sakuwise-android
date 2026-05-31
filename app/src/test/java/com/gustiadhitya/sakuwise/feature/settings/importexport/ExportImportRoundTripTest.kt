package com.gustiadhitya.sakuwise.feature.settings.importexport

import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Item 8 (v1.0.4) — export ↔ import column parity / lossless round-trip.
 *
 * The exporter writes [TransactionCsvParser.CANONICAL_HEADERS_ID] in order;
 * these tests feed a row in exactly that column order back through the parser
 * and assert every field survives. Run in both ID and EN header modes to prove
 * the cross-language caveat from the handoff (a file exported in ID mode must
 * still import when the app is in EN mode, and vice-versa).
 *
 * Pure JVM — TransactionCsvParser.parse has no Android dependencies.
 */
class ExportImportRoundTripTest {

    private val testStrings = TransactionCsvParser.ParserStrings(
        emptyFile = "File kosong",
        invalidHeader = "Header tidak valid",
        badDateTemplate = "Baris %1\$d: format tanggal tidak dikenali: \"%2\$s\"",
    )

    // Column order = Tanggal;Tipe;Kategori;Item;Akun;Jumlah;Catatan
    private fun row(vararg cols: String) = cols.joinToString(";")

    private fun csv(headers: List<String>, vararg rows: String) =
        "﻿" + (listOf(headers.joinToString(";")) + rows).joinToString("\n")

    @Test
    fun expenseRow_roundTrips_idHeaders() {
        val text = csv(
            TransactionCsvParser.CANONICAL_HEADERS_ID,
            row("20260503", "Expense", "Makan", "ShopeeFood", "BCA", "45000", "Egg Roll, Basgor"),
        )

        val result = TransactionCsvParser.parse(text, testStrings)

        assertEquals(0, result.errors.size)
        assertEquals(1, result.rows.size)
        val r = result.rows.first()
        assertEquals(LocalDate.of(2026, 5, 3), r.date)
        assertEquals(TxnType.Expense, r.type)
        assertEquals("Makan", r.kategori)
        assertEquals("ShopeeFood", r.item)
        assertEquals("BCA", r.accountName)
        assertEquals(45_000L, r.amount)
        assertEquals("Egg Roll, Basgor", r.note)
    }

    @Test
    fun sameData_parsesIdentically_underEnHeaders() {
        // A file exported with EN headers must import the same as ID headers.
        val idText = csv(
            TransactionCsvParser.CANONICAL_HEADERS_ID,
            row("20260501", "Income", "Gaji", "", "BCA", "5000000", "Gaji Mei"),
        )
        val enText = csv(
            TransactionCsvParser.CANONICAL_HEADERS_EN,
            row("20260501", "Income", "Gaji", "", "BCA", "5000000", "Gaji Mei"),
        )

        val id = TransactionCsvParser.parse(idText, testStrings).rows.single()
        val en = TransactionCsvParser.parse(enText, testStrings).rows.single()

        assertEquals(id.date, en.date)
        assertEquals(id.type, en.type)
        assertEquals(id.kategori, en.kategori)
        assertEquals(id.item, en.item)
        assertEquals(id.accountName, en.accountName)
        assertEquals(id.amount, en.amount)
        assertEquals(id.note, en.note)
        // And the actual values are correct.
        assertEquals(TxnType.Income, en.type)
        assertEquals(5_000_000L, en.amount)
    }

    @Test
    fun transferRow_roundTrips() {
        val text = csv(
            TransactionCsvParser.CANONICAL_HEADERS_EN,
            row("20260510", "Transfer", "", "", "Tunai", "200000", "ke BCA"),
        )

        val r = TransactionCsvParser.parse(text, testStrings).rows.single()

        assertEquals(TxnType.Transfer, r.type)
        assertEquals(200_000L, r.amount)
        assertEquals("Tunai", r.accountName)
        assertEquals("ke BCA", r.note)
    }

    @Test
    fun canonicalHeaders_idAndEn_haveSameArity() {
        assertEquals(
            TransactionCsvParser.CANONICAL_HEADERS_ID.size,
            TransactionCsvParser.CANONICAL_HEADERS_EN.size,
        )
    }
}
