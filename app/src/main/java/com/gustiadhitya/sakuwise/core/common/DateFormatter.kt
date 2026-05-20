package com.gustiadhitya.sakuwise.core.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val ID = Locale("id", "ID")

/** Resolve active UI locale (id default). */
private fun activeLocale(): Locale {
    val tag = Locale.getDefault().language.lowercase()
    return if (tag == "en") Locale.ENGLISH else ID
}

private fun absFmt(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", activeLocale())

/** "15 Mei 2026" (id) / "15 May 2026" (en). */
fun LocalDate.toAbsoluteId(): String = format(absFmt())

/**
 * Bare-keys for relative date — callers can map to localized strings.
 * Returns one of: "today", "yesterday", "${n}_days_ago" (n in 2..6),
 * or null if outside 7 days (caller falls back to [toAbsoluteId]).
 */
sealed class RelativeDay {
    object Today : RelativeDay()
    object Yesterday : RelativeDay()
    data class DaysAgo(val n: Int) : RelativeDay()
    data class Absolute(val text: String) : RelativeDay()
}

fun LocalDate.relativeDay(today: LocalDate = LocalDate.now()): RelativeDay {
    val days = ChronoUnit.DAYS.between(this, today).toInt()
    return when {
        days == 0 -> RelativeDay.Today
        days == 1 -> RelativeDay.Yesterday
        days in 2..6 -> RelativeDay.DaysAgo(days)
        else -> RelativeDay.Absolute(toAbsoluteId())
    }
}

/**
 * "Hari ini" / "Kemarin" / "3 hari lalu" (id) — for non-composable contexts.
 * Composable callers should use the locale-aware Composable helpers in
 * RelativeDateText.kt that read from strings.xml.
 */
fun LocalDate.toRelativeOrAbsolute(today: LocalDate = LocalDate.now()): String =
    when (val r = relativeDay(today)) {
        RelativeDay.Today -> if (activeLocale() == Locale.ENGLISH) "Today" else "Hari ini"
        RelativeDay.Yesterday -> if (activeLocale() == Locale.ENGLISH) "Yesterday" else "Kemarin"
        is RelativeDay.DaysAgo ->
            if (activeLocale() == Locale.ENGLISH) "${r.n} days ago" else "${r.n} hari lalu"
        is RelativeDay.Absolute -> r.text
    }
