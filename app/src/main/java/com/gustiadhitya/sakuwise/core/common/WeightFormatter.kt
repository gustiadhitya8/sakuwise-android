package com.gustiadhitya.sakuwise.core.common

import java.text.NumberFormat
import java.util.Locale

/**
 * Format a milligram value as an Indonesian-locale gram string.
 * - Whole grams render without trailing decimals: 10000 → "10".
 * - Fractional grams render with up to 3 decimal places, locale comma:
 *   500 → "0,5", 1250 → "1,25", 10500 → "10,5".
 *
 * Use [formatMilliGramsWithUnit] when you want the "gram" suffix.
 */
fun formatMilliGrams(milliGrams: Long, locale: Locale = Locale("id", "ID")): String {
    val fmt = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 3
        isGroupingUsed = true
    }
    return fmt.format(milliGrams / 1000.0)
}

fun formatMilliGramsWithUnit(milliGrams: Long, locale: Locale = Locale("id", "ID")): String =
    "${formatMilliGrams(milliGrams, locale)} gram"

/**
 * Parse a user-entered gram string ("0,5", "0.5", "10", "1.25") to milligrams.
 * Accepts both `,` and `.` as the decimal separator so the field works
 * regardless of keyboard layout. Returns 0L on unparseable input.
 */
fun parseGramsToMilliGrams(input: String): Long {
    val trimmed = input.trim().replace(',', '.')
    if (trimmed.isEmpty()) return 0L
    val grams = trimmed.toDoubleOrNull() ?: return 0L
    // Multiply then round to avoid FP error like 0.1 * 1000 = 99.999...
    return Math.round(grams * 1000.0)
}
