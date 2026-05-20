package com.gustiadhitya.sakuwise.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToAutoLock: () -> Unit = {},
    onNavigateToPeriodStart: () -> Unit = {},
    onNavigateToAllocation: () -> Unit = {},
    onNavigateToPin: () -> Unit = {},
    onNavigateToGoldPrice: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToDonate: () -> Unit = {},
    onNavigateToReset: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onReplayOnboarding: () -> Unit = {},
    mainVm: MainViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by mainVm.prefs.collectAsState()
    val initial = (prefs.userNickname.firstOrNull()?.uppercase() ?: "S")
    val lastBackupLabel = if (prefs.lastBackupTimestamp == 0L) "Belum pernah"
        else {
            val daysAgo = ((System.currentTimeMillis() - prefs.lastBackupTimestamp) / 86_400_000L).toInt()
            "$daysAgo hari lalu"
        }
    val activeStr = stringResource(R.string.settings_active)
    val inactiveStr = stringResource(R.string.settings_inactive)
    val minutesStr = stringResource(R.string.settings_minutes_format, prefs.autoLockMinutes)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = SwSpace.bottomBarClear),
    ) {
        Text(stringResource(R.string.settings_title), color = sw.ink,
            style = SwType.H1.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = SwSpace.pageH, top = 8.dp, bottom = 12.dp))

        // Profile hero card — tap → ProfileSettings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SwSpace.pageH)
                .clip(RoundedCornerShape(20.dp))
                .background(sw.primary)
                .clickable(onClick = onNavigateToProfile),
        ) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 24.dp, y = 24.dp)) {
                LogoDaun(
                    sizeDp = 120,
                    bg = sw.onPrimary.copy(alpha = 0.12f),
                    leaf = sw.primary.copy(alpha = 0.12f),
                    vein = sw.onPrimary.copy(alpha = 0.12f),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                ) {
                    Text(initial, color = sw.onPrimary,
                        fontSize = 22.sp, lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(width = 14.dp, height = 1.dp))
                Column(Modifier.weight(1f)) {
                    Text(prefs.userNickname.ifBlank { "Teman" },
                        color = sw.onPrimary,
                        style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Text(
                        "${if (prefs.language == "id") "Bahasa Indonesia" else "English"} · " +
                            if (prefs.biometricEnabled) "Biometrik aktif" else "PIN saja",
                        color = sw.onPrimary.copy(alpha = 0.78f),
                        style = SwType.LabelSmall.copy(fontSize = 12.sp),
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = sw.onPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        SettingsGroup(label = stringResource(R.string.settings_group_plan)) {
            SettingsRow(stringResource(R.string.settings_default_alloc),
                "${prefs.needsPct} · ${prefs.wantsPct} · ${prefs.investPct}",
                icon = Icons.Outlined.PieChart,
                onClick = onNavigateToAllocation)
            SettingsRow(stringResource(R.string.settings_period_start),
                "Tanggal ${prefs.planPeriodStartDay}",
                icon = Icons.Outlined.CalendarToday,
                onClick = onNavigateToPeriodStart)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_security)) {
            SettingsRow(stringResource(R.string.settings_pin_bio),
                if (prefs.biometricEnabled) activeStr else inactiveStr,
                icon = Icons.Outlined.Shield,
                onClick = onNavigateToPin)
            SettingsRow(stringResource(R.string.settings_autolock),
                minutesStr,
                icon = Icons.Outlined.Lock,
                onClick = onNavigateToAutoLock)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_backup)) {
            SettingsRow(stringResource(R.string.settings_backup_restore), lastBackupLabel,
                icon = Icons.Outlined.CloudUpload,
                onClick = onNavigateToBackup)
            SettingsRow(stringResource(R.string.settings_export_pdf),
                stringResource(R.string.settings_export_pdf_sub),
                icon = Icons.Outlined.Description,
                onClick = onNavigateToExport)
            SettingsRow(stringResource(R.string.settings_export_reset),
                stringResource(R.string.settings_export_reset_sub),
                icon = Icons.Outlined.DeleteForever,
                danger = true, onClick = onNavigateToReset)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_app)) {
            SettingsRow(stringResource(R.string.settings_language),
                if (prefs.language == "id") "Bahasa Indonesia" else "English",
                icon = Icons.Outlined.Language,
                onClick = onNavigateToLanguage)
            SettingsRow(stringResource(R.string.settings_replay_onboarding),
                stringResource(R.string.settings_replay_onboarding_sub),
                icon = Icons.Outlined.Replay,
                onClick = onReplayOnboarding)
            SettingsRow(stringResource(R.string.settings_donate),
                stringResource(R.string.settings_donate_sub),
                icon = Icons.Outlined.Favorite,
                onClick = onNavigateToDonate)
            SettingsRow(stringResource(R.string.settings_about), "v1.0",
                icon = Icons.Outlined.Info,
                onClick = onNavigateToAbout)
        }
    }
}

@Composable
private fun SettingsGroup(label: String, content: @Composable () -> Unit) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH, vertical = 8.dp)) {
        Text(label.uppercase(), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    danger: Boolean = false,
    warning: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val labelColor = when {
        danger -> sw.danger
        warning -> sw.warning
        else -> sw.ink
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // A11Y-004 minimum tap target; padding already brings 2-line rows
            // to ~62dp but 1-line variants would drop to ~48dp without this.
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        // Leading 40dp icon tile per prototype screens-settings.jsx — every
        // settings row carries a category glyph in a primaryContainer-bg
        // squircle. Danger rows tint the tile danger soft + danger fg.
        if (icon != null) {
            val tileBg = if (danger) sw.dangerSoft else sw.primaryContainer
            val tileFg = if (danger) sw.danger else sw.onPrimaryContainer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileBg),
            ) { Icon(icon, null, tint = tileFg, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, color = labelColor,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Text(value, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}
