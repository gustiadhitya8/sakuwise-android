package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

enum class SwButtonVariant { Primary, Secondary, Outline, Ghost, Danger }
enum class SwButtonSize(val h: Int, val padH: Int, val fs: Int) {
    Sm(36, 14, 13),
    Md(48, 18, 15),
    Lg(56, 22, 16),
}

@Composable
fun SwButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SwButtonVariant = SwButtonVariant.Primary,
    size: SwButtonSize = SwButtonSize.Md,
    fullWidth: Boolean = true,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val sw = SwTheme.colors
    val (bg, fg, border) = when (variant) {
        SwButtonVariant.Primary   -> Triple(sw.primary, sw.onPrimary, Color.Transparent)
        SwButtonVariant.Secondary -> Triple(sw.primaryContainer, sw.onPrimaryContainer, Color.Transparent)
        SwButtonVariant.Outline   -> Triple(Color.Transparent, sw.ink, sw.borderStrong)
        SwButtonVariant.Ghost     -> Triple(Color.Transparent, sw.primary, Color.Transparent)
        SwButtonVariant.Danger    -> Triple(sw.danger, Color.White, Color.Transparent)
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed && enabled) 0.97f else 1f
    val alpha = if (!enabled) 0.5f else if (pressed) 0.85f else 1f

    val widthMod = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .then(widthMod)
            .height(size.h.dp)
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(if (border != Color.Transparent) Modifier.border(1.5.dp, border, RoundedCornerShape(14.dp)) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = size.padH.dp),
    ) {
        if (leading != null) leading()
        Text(
            text = text,
            color = fg,
            style = SwType.LabelStrong.copy(fontSize = size.fs.sp),
        )
    }
}
