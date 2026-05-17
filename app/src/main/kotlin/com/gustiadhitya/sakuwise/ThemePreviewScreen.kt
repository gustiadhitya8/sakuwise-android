package com.gustiadhitya.sakuwise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.common.format.DateFormatter
import com.gustiadhitya.sakuwise.core.common.format.RupiahFormatter
import com.gustiadhitya.sakuwise.core.designsystem.theme.AmountLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.AmountStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.AmountXLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.DisplayLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.DisplayMStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.H1Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.H2Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.H3Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseAnimation
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens
import java.time.LocalDate

@Composable
fun ThemePreviewScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(SakuwiseSpacing.l),
            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xxl),
        ) {
            Text("Theme Preview", style = H1Style, color = MaterialTheme.colorScheme.onBackground)

            ColorSwatchSection()
            TypographySection()
            ShapeSection()
            SpacingSection()
            FormatterSection()
        }
    }
}

@Composable
private fun ColorSwatchSection() {
    SectionHeader("Colors")
    val cs = MaterialTheme.colorScheme
    val tokens = SakuwiseTokens.current
    val colorEntries = listOf(
        "primary" to cs.primary,
        "onPrimary" to cs.onPrimary,
        "primaryContainer" to cs.primaryContainer,
        "secondary (accent)" to cs.secondary,
        "background" to cs.background,
        "surface" to cs.surface,
        "surfaceContainerHigh" to cs.surfaceContainerHigh,
        "onBackground" to cs.onBackground,
        "onSurface" to cs.onSurface,
        "onSurfaceVariant" to cs.onSurfaceVariant,
        "outline" to cs.outline,
        "error" to cs.error,
        "successSoft" to tokens.successSoft,
        "warningSoft" to tokens.warningSoft,
        "dangerSoft" to tokens.dangerSoft,
        "infoSoft" to tokens.infoSoft,
    )
    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
        colorEntries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                row.forEach { (name, color) ->
                    ColorSwatch(name = name, color = color, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SakuwiseSpacing.xxxl)
                .clip(SakuwiseShapes.sm)
                .background(color),
        )
        Text(
            text = name,
            style = CaptionStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TypographySection() {
    SectionHeader("Typography — Figtree")
    val styles = listOf(
        "Display L (40)" to DisplayLStyle,
        "Display M (32)" to DisplayMStyle,
        "H1 (26)" to H1Style,
        "H2 (20)" to H2Style,
        "H3 (17)" to H3Style,
        "Body L (16)" to BodyLStyle,
        "Body (14)" to BodyStyle,
        "Caption (12)" to CaptionStyle,
        "Amount XL (36)" to AmountXLStyle,
        "Amount L (22)" to AmountLStyle,
        "Amount (16)" to AmountStyle,
    )
    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
        styles.forEach { (label, style) ->
            Text(
                text = label,
                style = style,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun ShapeSection() {
    SectionHeader("Shapes")
    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
        listOf("xs" to SakuwiseShapes.xs, "sm" to SakuwiseShapes.sm, "md" to SakuwiseShapes.md,
            "lg" to SakuwiseShapes.lg, "xl" to SakuwiseShapes.xl, "2xl" to SakuwiseShapes.xl2)
            .forEach { (name, shape) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(SakuwiseSpacing.xxxl)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Text(name, style = CaptionStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
    }
}

@Composable
private fun SpacingSection() {
    SectionHeader("Spacing")
    val spacings = listOf(
        "xs 4" to SakuwiseSpacing.xs,
        "s 8" to SakuwiseSpacing.s,
        "m 12" to SakuwiseSpacing.m,
        "l 16" to SakuwiseSpacing.l,
        "xl 20" to SakuwiseSpacing.xl,
        "xxl 24" to SakuwiseSpacing.xxl,
        "xxxl 32" to SakuwiseSpacing.xxxl,
        "40" to SakuwiseSpacing.xxxxl,
        "48" to SakuwiseSpacing.xxxxxl,
        "64" to SakuwiseSpacing.xxxxxxl,
    )
    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
        spacings.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(value)
                        .height(SakuwiseSpacing.m)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.width(SakuwiseSpacing.s))
                Text(label, style = CaptionStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FormatterSection() {
    SectionHeader("Formatters")
    val samples = listOf(
        RupiahFormatter.format(1_500_000L),
        RupiahFormatter.format(850_000L),
        RupiahFormatter.formatShort(1_500_000L),
        RupiahFormatter.formatShort(850_000L),
        RupiahFormatter.formatShort(2_000_000_000L),
        DateFormatter.format(LocalDate.of(2026, 5, 15)),
        DateFormatter.formatRelative(LocalDate.now().minusDays(1)),
        DateFormatter.formatRelative(LocalDate.now().minusDays(5)),
    )
    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
        samples.forEach { sample ->
            Text(sample, style = BodyStyle, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = CaptionStyle,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Preview(showBackground = true)
@Composable
private fun ThemePreviewScreenLightPreview() {
    SakuwiseTheme(darkTheme = false) { ThemePreviewScreen() }
}

@Preview(showBackground = true)
@Composable
private fun ThemePreviewScreenDarkPreview() {
    SakuwiseTheme(darkTheme = true) { ThemePreviewScreen() }
}
