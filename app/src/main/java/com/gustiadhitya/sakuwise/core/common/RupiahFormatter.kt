package com.gustiadhitya.sakuwise.core.common

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

private val ID = Locale("id", "ID")

/** Resolve the active UI locale (defaults to ID). */
private fun activeLocale(): Locale {
    val tag = Locale.getDefault().language.lowercase()
    return if (tag == "en") Locale.ENGLISH else ID
}

/** "Rp 1.500.000" (id) / "Rp 1,500,000" (en) — thousands sep follows active locale. */
fun Long.toRupiah(prefix: String = "Rp "): String =
    prefix + NumberFormat.getInstance(activeLocale()).format(this.absoluteValue)

/**
 * "Rp 1.5 jt" / "Rp 850rb" / "Rp 1.2 M" (id) — "Rp 1.5M" / "Rp 850K" / "Rp 1.2B" (en).
 */
fun Long.toRupiahShort(prefix: String = "Rp "): String {
    val v = this.absoluteValue
    val en = activeLocale() == Locale.ENGLISH
    return when {
        v >= 1_000_000_000L -> "$prefix${round1(v / 1_000_000_000.0)} ${if (en) "B" else "M"}"
        v >= 1_000_000L     -> "$prefix${round1(v / 1_000_000.0)} ${if (en) "M" else "jt"}"
        v >= 1_000L         -> "$prefix${v / 1000}${if (en) "K" else "rb"}"
        else                -> "$prefix$v"
    }
}

/** TalkBack-friendly: "Rp 1 juta 500 ribu" (id) / "Rp 1 million 500 thousand" (en). A11Y-013. */
fun Long.toRupiahSpoken(): String {
    val v = this.absoluteValue
    if (v == 0L) return "Rp 0"
    val en = activeLocale() == Locale.ENGLISH
    val billion = v / 1_000_000_000L
    val million = (v / 1_000_000L) % 1_000L
    val thousand = (v / 1_000L) % 1_000L
    val rest = v % 1_000L
    val parts = mutableListOf<String>()
    if (billion > 0) parts += "$billion ${if (en) "billion" else "miliar"}"
    if (million > 0) parts += "$million ${if (en) "million" else "juta"}"
    if (thousand > 0) parts += "$thousand ${if (en) "thousand" else "ribu"}"
    if (rest > 0) parts += "$rest"
    return "Rp " + parts.joinToString(" ")
}

private fun round1(value: Double): String {
    val r = (value * 10.0).roundToLong() / 10.0
    return if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
}
