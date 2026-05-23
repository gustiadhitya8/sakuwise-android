package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun SwTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    transparent: Boolean = false,
    actions: (@Composable () -> Unit)? = null,
) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(if (transparent) Color.Transparent else sw.bg)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
    ) {
        if (onBack != null) {
            // A11Y-004: visible 40x40 but min interactive 48x48
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Kembali",
                    tint = sw.ink,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 19.sp),
                maxLines = 1,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = sw.inkMuted,
                    style = SwType.Caption.copy(fontSize = 12.sp),
                )
            }
        }
        actions?.invoke()
    }
}
