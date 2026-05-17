package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

data class SnapshotDataPoint(val date: Long, val value: Long)

enum class SnapshotPeriod(val label: String) {
    THREE_MONTHS("3B"),
    SIX_MONTHS("6B"),
    ONE_YEAR("1T"),
    ALL("Semua"),
}

@Composable
fun SwSnapshotChart(
    dataPoints: List<SnapshotDataPoint>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = SakuwiseSpacing.xxxxxxl,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    selectedPeriod: SnapshotPeriod = SnapshotPeriod.THREE_MONTHS,
    onPeriodSelected: (SnapshotPeriod) -> Unit = {},
) {
    val fillColor = lineColor.copy(alpha = 0.22f)
    val dotColor = lineColor
    val bgColor = MaterialTheme.colorScheme.background

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
        ) {
            if (dataPoints.size < 2) return@Canvas

            val minVal = dataPoints.minOf { it.value }.toFloat()
            val maxVal = dataPoints.maxOf { it.value }.toFloat()
            val range = if (maxVal - minVal < 1f) 1f else maxVal - minVal

            val w = size.width
            val h = size.height
            val padV = 8f   // vertical padding in px

            fun xAt(idx: Int) = idx.toFloat() / (dataPoints.size - 1) * w
            fun yAt(v: Long) = h - padV - (v - minVal) / range * (h - 2 * padV)

            // Area fill path
            val areaPath = Path().apply {
                moveTo(xAt(0), yAt(dataPoints[0].value))
                for (i in 1 until dataPoints.size) {
                    lineTo(xAt(i), yAt(dataPoints[i].value))
                }
                lineTo(xAt(dataPoints.size - 1), h)
                lineTo(xAt(0), h)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = 0f,
                    endY = h,
                ),
            )

            // Line path
            val linePath = Path().apply {
                moveTo(xAt(0), yAt(dataPoints[0].value))
                for (i in 1 until dataPoints.size) {
                    lineTo(xAt(i), yAt(dataPoints[i].value))
                }
            }
            drawPath(
                path = linePath,
                color = dotColor,
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // Intermediate dots (r=2.5) for all but last
            val intermediateR = 5f   // diameter → radius used in drawCircle
            for (i in 0 until dataPoints.size - 1) {
                drawCircle(
                    color = dotColor,
                    radius = 2.5f,
                    center = Offset(xAt(i), yAt(dataPoints[i].value)),
                )
                // White fill center to create ring effect
                drawCircle(
                    color = bgColor,
                    radius = 1.2f,
                    center = Offset(xAt(i), yAt(dataPoints[i].value)),
                )
            }
            // Last dot (r=4) — solid
            val lastIdx = dataPoints.size - 1
            drawCircle(
                color = dotColor,
                radius = 4f,
                center = Offset(xAt(lastIdx), yAt(dataPoints[lastIdx].value)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SakuwiseSpacing.s),
            horizontalArrangement = Arrangement.End,
        ) {
            SnapshotPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                val chipBg = if (isSelected) MaterialTheme.colorScheme.primary
                             else Color.Transparent
                val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .size(SakuwiseSpacing.xxxxxl)
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { onPeriodSelected(period) }
                        .semantics { contentDescription = "${period.label}, periode" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = chipText,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwSnapshotChartPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            var period by remember { mutableStateOf(SnapshotPeriod.THREE_MONTHS) }
            SwSnapshotChart(
                dataPoints = sampleChartData(),
                modifier = Modifier.padding(SakuwiseSpacing.l),
                selectedPeriod = period,
                onPeriodSelected = { period = it },
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwSnapshotChartPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            var period by remember { mutableStateOf(SnapshotPeriod.SIX_MONTHS) }
            SwSnapshotChart(
                dataPoints = sampleChartData(),
                modifier = Modifier.padding(SakuwiseSpacing.l),
                selectedPeriod = period,
                onPeriodSelected = { period = it },
            )
        }
    }
}

internal fun sampleChartData(): List<SnapshotDataPoint> {
    val base = System.currentTimeMillis()
    val day = 86_400_000L
    return listOf(
        SnapshotDataPoint(base - 90 * day, 8_200_000),
        SnapshotDataPoint(base - 75 * day, 9_500_000),
        SnapshotDataPoint(base - 60 * day, 7_800_000),
        SnapshotDataPoint(base - 45 * day, 10_200_000),
        SnapshotDataPoint(base - 30 * day, 11_500_000),
        SnapshotDataPoint(base - 15 * day, 9_900_000),
        SnapshotDataPoint(base,             12_400_000),
    )
}
