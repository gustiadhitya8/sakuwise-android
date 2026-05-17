package com.gustiadhitya.sakuwise.core.common.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val idLocale = Locale("id", "ID")

private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale)
private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", idLocale)

object DateFormatter {

    fun format(date: LocalDate): String = date.format(fullDateFormatter)

    fun formatShort(date: LocalDate): String = date.format(shortDateFormatter)

    fun formatRelative(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val days = ChronoUnit.DAYS.between(date, today)
        return when {
            days == 0L -> "Hari ini"
            days == 1L -> "Kemarin"
            days in 2..6 -> "$days hari lalu"
            else -> format(date)
        }
    }
}
