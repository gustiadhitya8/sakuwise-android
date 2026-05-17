package com.gustiadhitya.sakuwise.core.common.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RupiahFormatterTest {

    @Test
    fun `format zero`() {
        assertEquals("Rp 0", RupiahFormatter.format(0L))
    }

    @Test
    fun `format hundreds`() {
        assertEquals("Rp 500", RupiahFormatter.format(500L))
    }

    @Test
    fun `format thousands`() {
        assertEquals("Rp 1.500", RupiahFormatter.format(1_500L))
    }

    @Test
    fun `format millions with dot separator`() {
        assertEquals("Rp 1.500.000", RupiahFormatter.format(1_500_000L))
    }

    @Test
    fun `format billions`() {
        assertEquals("Rp 1.000.000.000", RupiahFormatter.format(1_000_000_000L))
    }

    @Test
    fun `format negative value`() {
        assertEquals("-Rp 50.000", RupiahFormatter.format(-50_000L))
    }

    @Test
    fun `formatShort thousands shows rb`() {
        assertEquals("Rp 850rb", RupiahFormatter.formatShort(850_000L))
    }

    @Test
    fun `formatShort millions shows jt`() {
        assertEquals("Rp 1.5 jt", RupiahFormatter.formatShort(1_500_000L))
    }

    @Test
    fun `formatShort exact million shows whole jt`() {
        assertEquals("Rp 2 jt", RupiahFormatter.formatShort(2_000_000L))
    }

    @Test
    fun `formatShort billions shows M`() {
        assertEquals("Rp 1.5 M", RupiahFormatter.formatShort(1_500_000_000L))
    }

    @Test
    fun `formatShort below thousand uses full format`() {
        assertEquals("Rp 999", RupiahFormatter.formatShort(999L))
    }

    @Test
    fun `formatShort negative millions`() {
        assertEquals("-Rp 2 jt", RupiahFormatter.formatShort(-2_000_000L))
    }
}
