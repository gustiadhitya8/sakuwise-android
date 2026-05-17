package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

// progress: 0f..1f for normal, >1f for over-budget (dual-segment)
@Composable
fun SwBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "",
    primaryColor: Color = MaterialTheme.colorScheme.primary,
) {
    val tokens = SakuwiseTokens.current
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val dangerColor = tokens.danger
    val clampedProgress = progress.coerceAtLeast(0f)
    val a11yDescription = "${(clampedProgress * 100).toInt()}% $label".trim()

    Canvas(
        modifier = modifier
            .height(SakuwiseSpacing.barHeight)
            .semantics { contentDescription = a11yDescription },
    ) {
        val radius = size.height / 2f
        val cr = CornerRadius(radius, radius)

        // Track background
        drawRoundRect(color = trackColor, size = size, cornerRadius = cr)

        if (clampedProgress <= 1f) {
            // Normal: primary fill up to clampedProgress × width
            val fillWidth = size.width * clampedProgress
            if (fillWidth > 0f) {
                drawRoundRect(color = primaryColor, size = Size(fillWidth, size.height), cornerRadius = cr)
            }
        } else {
            // Over-budget: primary fills full width, danger segment continues as overflow
            // Primary: full width
            drawRoundRect(color = primaryColor, size = size, cornerRadius = cr)

            // Danger segment: thin strip on the right representing the excess
            // Normalize overflow against total (so at 120%, danger covers 20/120 = 16.7% of bar)
            val overflowFraction = (clampedProgress - 1f) / clampedProgress
            val dangerWidth = size.width * overflowFraction
            if (dangerWidth > 0f) {
                drawRoundRect(
                    color = dangerColor,
                    topLeft = Offset(size.width - dangerWidth, 0f),
                    size = Size(dangerWidth, size.height),
                    cornerRadius = cr,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwBarPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                SwBar(progress = 0.67f, modifier = Modifier.fillMaxWidth(), label = "Needs 67%")
                SwBar(progress = 0.81f, modifier = Modifier.fillMaxWidth(), label = "Wants 81%")
                SwBar(progress = 1.0f, modifier = Modifier.fillMaxWidth(), label = "Investment 100%")
                SwBar(progress = 1.2f, modifier = Modifier.fillMaxWidth(), label = "Over budget 120%")
                SwBar(progress = 0f, modifier = Modifier.fillMaxWidth(), label = "Empty 0%")
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwBarPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                SwBar(progress = 0.67f, modifier = Modifier.fillMaxWidth(), label = "67%")
                SwBar(progress = 1.2f, modifier = Modifier.fillMaxWidth(), label = "Over budget 120%")
            }
        }
    }
}
