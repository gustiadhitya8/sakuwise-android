package com.gustiadhitya.sakuwise.core.database.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class DateConvertersTest {

    private val converters = DateConverters()

    @Test
    fun `LocalDate round-trips through epochDay`() {
        val date = LocalDate.of(2026, 5, 17)
        val epoch = converters.fromLocalDate(date)
        assertEquals(date, converters.toLocalDate(epoch))
    }

    @Test
    fun `null LocalDate converts to null Long`() {
        assertNull(converters.fromLocalDate(null))
        assertNull(converters.toLocalDate(null))
    }

    @Test
    fun `Instant round-trips through epochMillis`() {
        val instant = Instant.ofEpochMilli(1_747_440_000_000L)
        val millis = converters.fromInstant(instant)
        assertEquals(instant, converters.toInstant(millis))
    }

    @Test
    fun `null Instant converts to null Long`() {
        assertNull(converters.fromInstant(null))
        assertNull(converters.toInstant(null))
    }
}
