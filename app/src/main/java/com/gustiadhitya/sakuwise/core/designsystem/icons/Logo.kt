package com.gustiadhitya.sakuwise.core.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwFontFamily
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme

/**
 * LogoA_Daun — Sakuwise mark. A squircle "saku" pouch with a leaf inscribed
 * and a mid-vein + 4 side-veins. Ported from proto/brand/logos.jsx (viewBox 64).
 *
 * `bg`   — squircle fill (set to Color.Transparent to render just the leaf)
 * `leaf` — leaf body fill
 * `vein` — vein stroke colour (typically same as `bg` so veins "carve through")
 */
@Composable
fun LogoDaun(
    sizeDp: Int = 56,
    modifier: Modifier = Modifier,
    bg: Color? = null,
    leaf: Color? = null,
    vein: Color? = null,
) {
    val sw = SwTheme.colors
    val bgC = bg ?: sw.primary
    val leafC = leaf ?: sw.onPrimary
    val veinC = vein ?: bgC

    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        // Squircle (matches rx=18 on 64-viewbox → 0.281 × size)
        if (bgC.alpha > 0f) {
            val corner = w * 0.281f
            val rect = androidx.compose.ui.geometry.RoundRect(
                left = w * 0.0625f, top = h * 0.0625f,
                right = w * 0.9375f, bottom = h * 0.9375f,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            )
            val sq = Path().apply { addRoundRect(rect) }
            drawPath(sq, color = bgC)
        }

        // Leaf body — C-curves from top to bottom and back
        // viewBox 64: M 32 13 C 50 20 50 44 32 51 C 14 44 14 20 32 13 Z
        fun nx(v: Float) = w * (v / 64f)
        fun ny(v: Float) = h * (v / 64f)
        val leafPath = Path().apply {
            moveTo(nx(32f), ny(13f))
            cubicTo(nx(50f), ny(20f), nx(50f), ny(44f), nx(32f), ny(51f))
            cubicTo(nx(14f), ny(44f), nx(14f), ny(20f), nx(32f), ny(13f))
            close()
            fillType = PathFillType.NonZero
        }
        drawPath(leafPath, color = leafC)

        // Mid-vein
        val vMid = Path().apply {
            moveTo(nx(32f), ny(16f))
            lineTo(nx(32f), ny(49f))
        }
        drawPath(
            vMid,
            color = veinC,
            style = Stroke(width = w * (2.5f / 64f), cap = StrokeCap.Round),
        )
        // 4 side veins (opacity 0.45 per spec)
        val veinFade = veinC.copy(alpha = veinC.alpha * 0.45f)
        fun sideVein(sx: Float, sy: Float, cx: Float, cy: Float, ex: Float, ey: Float) {
            val p = Path().apply {
                moveTo(nx(sx), ny(sy))
                quadraticTo(nx(cx), ny(cy), nx(ex), ny(ey))
            }
            drawPath(p, color = veinFade,
                style = Stroke(width = w * (2f / 64f), cap = StrokeCap.Round))
        }
        sideVein(32f, 26f, 38f, 28f, 41f, 32f)
        sideVein(32f, 36f, 38f, 38f, 41f, 42f)
        sideVein(32f, 26f, 26f, 28f, 23f, 32f)
        sideVein(32f, 36f, 26f, 38f, 23f, 42f)
    }
}

@Composable
fun Wordmark(
    sizeSp: Int = 22,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            "Saku",
            color = sw.ink,
            fontSize = sizeSp.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.025).em,
            fontFamily = SwFontFamily,
        )
        Text(
            "wise",
            color = sw.primary,
            fontSize = sizeSp.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.025).em,
            fontFamily = SwFontFamily,
        )
    }
}

@Composable
fun Lockup(sizeSp: Int = 22, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        LogoDaun(sizeDp = (sizeSp * 1.4f).toInt())
        Spacer(Modifier.width((sizeSp * 0.35f).dp))
        Wordmark(sizeSp = sizeSp)
    }
}
