package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.common.format.RupiahFormatter
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

@Composable
fun RupiahText(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    short: Boolean = false,
) {
    val formatted = if (short) RupiahFormatter.formatShort(amount) else RupiahFormatter.format(amount)
    val a11yLabel = rupiahA11yDescription(amount)
    Text(
        text = formatted,
        style = style.merge(TextStyle(fontFeatureSettings = "tnum")),
        modifier = modifier.semantics { contentDescription = a11yLabel },
    )
}

// A11Y-013: expand short suffixes to full verbal form for screen readers
internal fun rupiahA11yDescription(amount: Long): String {
    val abs = kotlin.math.abs(amount)
    val sign = if (amount < 0) "minus " else ""
    return when {
        abs >= 1_000_000_000L -> {
            val m = abs / 1_000_000_000L
            val sisa = abs % 1_000_000_000L
            val sisaJt = sisa / 1_000_000L
            if (sisaJt > 0) "${sign}Rupiah $m miliar $sisaJt juta"
            else "${sign}Rupiah $m miliar"
        }
        abs >= 1_000_000L -> {
            val jt = abs / 1_000_000L
            val sisa = abs % 1_000_000L
            val sisaRb = sisa / 1_000L
            if (sisaRb > 0) "${sign}Rupiah $jt juta $sisaRb ribu"
            else "${sign}Rupiah $jt juta"
        }
        abs >= 1_000L -> {
            val rb = abs / 1_000L
            val sisa = abs % 1_000L
            if (sisa > 0) "${sign}Rupiah $rb ribu $sisa"
            else "${sign}Rupiah $rb ribu"
        }
        else -> "${sign}Rupiah $abs"
    }
}

@Preview(showBackground = true)
@Composable
private fun RupiahTextPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                RupiahText(amount = 4_700_000L, style = MaterialTheme.typography.headlineLarge)
                RupiahText(amount = 1_500_000L)
                RupiahText(amount = 5_200_000L, short = true)
                RupiahText(amount = 28_000L, short = true)
                RupiahText(amount = -10_800_000L, short = true)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RupiahTextPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                RupiahText(amount = 4_700_000L, style = MaterialTheme.typography.headlineLarge)
                RupiahText(amount = -10_800_000L, short = true)
            }
        }
    }
}
