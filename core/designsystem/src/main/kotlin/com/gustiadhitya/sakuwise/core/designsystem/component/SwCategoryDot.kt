package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

@Composable
fun SwCategoryDot(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = SakuwiseSpacing.xxxxl,
) {
    val (bgColor, letterColor) = categoryColorPair(name)
    Box(
        modifier = modifier
            .size(size)
            .clip(SakuwiseShapes.full)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = letterColor,
            ),
        )
    }
}

@Composable
private fun categoryColorPair(name: String): Pair<Color, Color> {
    val tokens = SakuwiseTokens.current
    val cs = MaterialTheme.colorScheme
    val palette = listOf(
        tokens.successSoft to tokens.success,
        tokens.warningSoft to tokens.warning,
        tokens.dangerSoft to tokens.danger,
        tokens.infoSoft to tokens.info,
        tokens.accentSoft to tokens.accent,
        cs.primaryContainer to cs.onPrimaryContainer,
        cs.secondaryContainer to cs.onSecondaryContainer,
        cs.tertiaryContainer to cs.onTertiaryContainer,
    )
    val idx = (name.hashCode() and Int.MAX_VALUE) % palette.size
    return palette[idx]
}

@Preview(showBackground = true)
@Composable
private fun SwCategoryDotPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwCategoryDot(name = "Kopi Kenangan")
                SwCategoryDot(name = "Warteg Bahari")
                SwCategoryDot(name = "Pertamina")
                SwCategoryDot(name = "Belanja")
                SwCategoryDot(name = "Gaji")
                SwCategoryDot(name = "Restoran")
                SwCategoryDot(name = "Sushi Tei")
                SwCategoryDot(name = "Investasi")
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwCategoryDotPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwCategoryDot(name = "Kopi Kenangan")
                SwCategoryDot(name = "Warteg Bahari")
                SwCategoryDot(name = "Pertamina")
                SwCategoryDot(name = "Belanja")
                SwCategoryDot(name = "Gaji")
            }
        }
    }
}
