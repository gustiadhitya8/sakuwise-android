package com.gustiadhitya.sakuwise.core.designsystem.brand

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

// Ported from brand/logos.jsx — LogoA_Daun.
// SVG coordinate system: 64×64 viewBox. All dimensions scaled from that space.
@Composable
fun DaunMark(
    size: Dp,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onPrimaryColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val markPx = with(LocalDensity.current) { size.toPx() }
    Canvas(modifier = modifier.size(size)) {
        val s = markPx / 64f

        // Rounded square background: rect x=4 y=4 w=56 h=56 rx=18
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(4f * s, 4f * s),
            size = Size(56f * s, 56f * s),
            cornerRadius = CornerRadius(18f * s, 18f * s),
        )

        // Leaf body (filled with onPrimary)
        val leaf = Path().apply {
            moveTo(32f * s, 13f * s)
            cubicTo(50f * s, 20f * s, 50f * s, 44f * s, 32f * s, 51f * s)
            cubicTo(14f * s, 44f * s, 14f * s, 20f * s, 32f * s, 13f * s)
            close()
        }
        drawPath(leaf, color = onPrimaryColor)

        // Center vein
        drawLine(
            color = primaryColor,
            start = Offset(32f * s, 16f * s),
            end = Offset(32f * s, 49f * s),
            strokeWidth = 2.5f * s,
            cap = StrokeCap.Round,
        )

        // 4 side veins at 45% opacity
        val veinColor = primaryColor.copy(alpha = 0.45f)
        val veinStroke = Stroke(width = 2f * s, cap = StrokeCap.Round)
        listOf(
            Path().apply { moveTo(32f * s, 26f * s); quadraticBezierTo(38f * s, 28f * s, 41f * s, 32f * s) },
            Path().apply { moveTo(32f * s, 36f * s); quadraticBezierTo(38f * s, 38f * s, 41f * s, 42f * s) },
            Path().apply { moveTo(32f * s, 26f * s); quadraticBezierTo(26f * s, 28f * s, 23f * s, 32f * s) },
            Path().apply { moveTo(32f * s, 36f * s); quadraticBezierTo(26f * s, 38f * s, 23f * s, 42f * s) },
        ).forEach { drawPath(it, color = veinColor, style = veinStroke) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DaunMarkPreviewLight() {
    SakuwiseTheme { DaunMark(size = 80.dp) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DaunMarkPreviewDark() {
    SakuwiseTheme(darkTheme = true) { DaunMark(size = 80.dp) }
}
