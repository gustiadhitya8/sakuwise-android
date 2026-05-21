package com.gustiadhitya.sakuwise.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * "Plan May 2026" / "Plan Mei 2026" — formatted from the *active per-app
 * locale* read off the Activity configuration rather than JVM `Locale
 * .getDefault()`. Per-app locale set via AppCompatDelegate / LocaleManager
 * propagates to the Configuration but not always to the JVM default, so
 * locale-sensitive formatters anchored to `Locale.getDefault()` (e.g.
 * pre-existing `monthIdShort`) silently keep producing the old language.
 */
@Composable
fun planPeriodLabel(end: LocalDate): String {
    val locale = LocalConfiguration.current.locales.get(0)
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }
    return "Plan " + end.format(formatter)
}
