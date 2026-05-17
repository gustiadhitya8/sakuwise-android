package com.gustiadhitya.sakuwise.core.common.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateFormatterTest {

    private val may15 = LocalDate.of(2026, 5, 15)
    private val may17 = LocalDate.of(2026, 5, 17)

    @Test
    fun `format full date in Indonesian`() {
        assertEquals("15 Mei 2026", DateFormatter.format(may15))
    }

    @Test
    fun `formatShort date in Indonesian`() {
        assertEquals("15 Mei 2026", DateFormatter.formatShort(may15))
    }

    @Test
    fun `formatRelative same day is Hari ini`() {
        assertEquals("Hari ini", DateFormatter.formatRelative(may17, today = may17))
    }

    @Test
    fun `formatRelative yesterday is Kemarin`() {
        val yesterday = may17.minusDays(1)
        assertEquals("Kemarin", DateFormatter.formatRelative(yesterday, today = may17))
    }

    @Test
    fun `formatRelative 3 days ago`() {
        val threeDaysAgo = may17.minusDays(3)
        assertEquals("3 hari lalu", DateFormatter.formatRelative(threeDaysAgo, today = may17))
    }

    @Test
    fun `formatRelative 6 days ago`() {
        val sixDaysAgo = may17.minusDays(6)
        assertEquals("6 hari lalu", DateFormatter.formatRelative(sixDaysAgo, today = may17))
    }

    @Test
    fun `formatRelative older than 6 days falls back to full format`() {
        val old = LocalDate.of(2026, 5, 1)
        assertEquals("1 Mei 2026", DateFormatter.formatRelative(old, today = may17))
    }

    @Test
    fun `format January shows Januari`() {
        val jan1 = LocalDate.of(2026, 1, 1)
        assertEquals("1 Januari 2026", DateFormatter.format(jan1))
    }

    @Test
    fun `format December shows Desember`() {
        val dec25 = LocalDate.of(2026, 12, 25)
        assertEquals("25 Desember 2026", DateFormatter.format(dec25))
    }
}
