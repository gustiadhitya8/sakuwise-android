package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.common.isReducedMotion
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import kotlin.math.min

/**
 * SwBar — progress bar with optional dual-segment overflow when used > plan.
 * Spec: 6–8dp height, radius = height, fill animation 400 ms cubic-ish.
 */
@Composable
fun SwBar(
    used: Long,
    plan: Long,
    modifier: Modifier = Modifier,
    color: Color? = null,
    heightDp: Int = 8,
    animate: Boolean = true,
) {
    val sw = SwTheme.colors
    val tint = color ?: sw.primary
    val pct = if (plan > 0L) min(used.toFloat() / plan.toFloat(), 1f) else 0f
    val over = plan > 0L && used > plan
    val overflowPct = if (over) min(((used - plan).toFloat() / plan.toFloat()), 0.30f) else 0f
    val shape = RoundedCornerShape(heightDp.dp)

    // A11Y-012: when the user has Animator scale = 0, collapse fills to instant.
    val reduceMotion = isReducedMotion()
    val duration = if (animate && !reduceMotion) 400 else 0
    val animPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(duration),
        label = "swbar-fill",
    )
    val animOver by animateFloatAsState(
        targetValue = overflowPct,
        animationSpec = tween(duration),
        label = "swbar-over",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(shape)
            .background(sw.track),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animPct)
                    .background(if (over) sw.danger else tint),
            )
            if (over && animOver > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animOver / (1f - animPct).coerceAtLeast(0.0001f))
                        .background(sw.danger.copy(alpha = 0.7f)),
                )
            }
        }
    }
}
