package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

enum class SwSettingsRowVariant { Default, Warning, Danger }

private val IconContainerSize = SakuwiseSpacing.xxxxl   // 40dp
private val IconSize = SakuwiseSpacing.xl               // 20dp

@Composable
fun SwSettingsRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    value: String? = null,
    variant: SwSettingsRowVariant = SwSettingsRowVariant.Default,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val tokens = SakuwiseTokens.current
    var isFocused by remember { mutableStateOf(false) }

    val iconBg = when (variant) {
        SwSettingsRowVariant.Default -> MaterialTheme.colorScheme.primaryContainer
        SwSettingsRowVariant.Warning -> tokens.warningSoft
        SwSettingsRowVariant.Danger -> tokens.dangerSoft
    }
    val iconTint = when (variant) {
        SwSettingsRowVariant.Default -> MaterialTheme.colorScheme.onPrimaryContainer
        SwSettingsRowVariant.Warning -> tokens.warning
        SwSettingsRowVariant.Danger -> tokens.danger
    }
    val labelColor = when (variant) {
        SwSettingsRowVariant.Danger -> tokens.danger
        else -> MaterialTheme.colorScheme.onSurface
    }
    val valueColor = when (variant) {
        SwSettingsRowVariant.Warning -> tokens.warning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SakuwiseSpacing.xxxxxxl)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) Modifier.border(
                    width = SakuwiseSpacing.borderFocus,
                    color = MaterialTheme.colorScheme.primary,
                    shape = SakuwiseShapes.md,
                ) else Modifier
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = SakuwiseSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(IconContainerSize)
                .clip(SakuwiseShapes.sm)
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(IconSize),
            )
        }
        Spacer(Modifier.width(SakuwiseSpacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor,
            )
            if (sub != null) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (value != null) {
            Spacer(Modifier.width(SakuwiseSpacing.s))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = valueColor,
            )
        }
        if (showChevron) {
            Spacer(Modifier.width(SakuwiseSpacing.xs))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SakuwiseSpacing.xxl),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwSettingsRowPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
            ) {
                SwSettingsRow(
                    icon = Icons.Outlined.Language,
                    label = "Bahasa",
                    value = "Bahasa Indonesia",
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Outlined.CalendarToday,
                    label = "Tanggal Mulai Periode",
                    value = "Tanggal 1",
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Filled.Backup,
                    label = "Backup & Pemulihan",
                    value = "34 hari lalu",
                    variant = SwSettingsRowVariant.Warning,
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Filled.Delete,
                    label = "Export & Reset",
                    sub = "Hapus semua data, mulai dari nol",
                    variant = SwSettingsRowVariant.Danger,
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Outlined.Info,
                    label = "Tentang Sakuwise",
                    value = "v1.0",
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwSettingsRowPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
            ) {
                SwSettingsRow(
                    icon = Icons.Outlined.Language,
                    label = "Bahasa",
                    value = "Bahasa Indonesia",
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Filled.Backup,
                    label = "Backup & Pemulihan",
                    value = "34 hari lalu",
                    variant = SwSettingsRowVariant.Warning,
                    onClick = {},
                )
                SwSettingsRow(
                    icon = Icons.Filled.Delete,
                    label = "Export & Reset",
                    sub = "Hapus semua data, mulai dari nol",
                    variant = SwSettingsRowVariant.Danger,
                    onClick = {},
                )
            }
        }
    }
}
