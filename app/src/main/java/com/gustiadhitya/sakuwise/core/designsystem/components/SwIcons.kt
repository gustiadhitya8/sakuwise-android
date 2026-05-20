package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun SwAccountIcon(
    icon: ImageVector = Icons.Outlined.AccountBalanceWallet,
    sizeDp: Int = 40,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape((sizeDp * 0.3f).dp))
            .background(sw.primaryContainer),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = sw.onPrimaryContainer,
            modifier = Modifier.size((sizeDp * 0.5f).dp),
        )
    }
}

/**
 * Circle dot with first-letter glyph. Color is derived from the category name
 * (poor-man's hash) — matches the prototype's behavior.
 */
@Composable
fun SwCategoryDot(
    name: String,
    sizeDp: Int = 36,
    color: Color? = null,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    val palette = listOf(sw.primary, sw.accent, sw.info, sw.warning, sw.success)
    val fill = color ?: run {
        val idx = (name.fold(0) { s, ch -> s + ch.code } % palette.size).coerceAtLeast(0)
        palette[idx]
    }
    val letter = (name.firstOrNull()?.uppercase() ?: "?")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape((sizeDp * 0.3f).dp))
            .background(fill.copy(alpha = 0.12f)),
    ) {
        Text(
            letter,
            color = fill,
            style = SwType.H3.copy(fontSize = (sizeDp * 0.42f).sp),
        )
    }
}
