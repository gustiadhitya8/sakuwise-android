package com.gustiadhitya.sakuwise.core.common.format

import java.util.Locale
import kotlin.math.abs

private val idLocale = Locale("id", "ID")

object RupiahFormatter {

    fun format(value: Long): String {
        val abs = abs(value)
        val formatted = buildString {
            val s = abs.toString()
            val start = s.length % 3
            if (start > 0) append(s.substring(0, start))
            var i = start
            while (i < s.length) {
                if (isNotEmpty()) append('.')
                append(s.substring(i, i + 3))
                i += 3
            }
        }
        val sign = if (value < 0) "-" else ""
        return "${sign}Rp $formatted"
    }

    fun formatShort(value: Long): String {
        val abs = abs(value)
        val sign = if (value < 0) "-" else ""
        return when {
            abs >= 1_000_000_000L -> {
                val m = abs / 1_000_000_000.0
                val s = if (m % 1 == 0.0) "${m.toLong()} M" else "%.1f M".format(m)
                "${sign}Rp $s"
            }
            abs >= 1_000_000L -> {
                val jt = abs / 1_000_000.0
                val s = if (jt % 1 == 0.0) "${jt.toLong()} jt" else "%.1f jt".format(jt)
                "${sign}Rp $s"
            }
            abs >= 1_000L -> {
                val rb = abs / 1_000.0
                val s = if (rb % 1 == 0.0) "${rb.toLong()}rb" else "%.1f rb".format(rb)
                "${sign}Rp $s"
            }
            else -> format(value)
        }
    }
}
