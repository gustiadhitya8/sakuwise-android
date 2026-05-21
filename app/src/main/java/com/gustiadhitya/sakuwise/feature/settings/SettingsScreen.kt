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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
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
    onNavigateToTheme: () -> Unit = {},
    onReplayOnboarding: () -> Unit = {},
    mainVm: MainViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by mainVm.prefs.collectAsState()
    val initial = (prefs.userNickname.firstOrNull()?.uppercase() ?: "S")
    val lastBackupLabel = if (prefs.lastBackupTimestamp == 0L) stringResource(R.string.settings_last_backup_never)
        else {
            val daysAgo = ((System.currentTimeMillis() - prefs.lastBackupTimestamp) / 86_400_000L).toInt()
            stringResource(R.string.settings_days_ago_format, daysAgo)
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
                .background(sw.primaryHero)
                .clickable(onClick = onNavigateToProfile),
        ) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 24.dp, y = 24.dp)) {
                LogoDaun(
                    sizeDp = 120,
                    bg = sw.onPrimaryHero.copy(alpha = 0.12f),
                    leaf = sw.primaryHero.copy(alpha = 0.12f),
                    vein = sw.onPrimaryHero.copy(alpha = 0.12f),
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
                    Text(initial, color = sw.onPrimaryHero,
                        fontSize = 22.sp, lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(width = 14.dp, height = 1.dp))
                Column(Modifier.weight(1f)) {
                    Text(prefs.userNickname.ifBlank { "Teman" },
                        color = sw.onPrimaryHero,
                        style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Text(
                        "${if (prefs.language == "id") "Bahasa Indonesia" else "English"} · " +
                            if (prefs.biometricEnabled) stringResource(R.string.settings_lang_biometric) else stringResource(R.string.settings_lang_pin_only),
                        color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.LabelSmall.copy(fontSize = 12.sp),
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = sw.onPrimaryHero, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        SettingsGroup(label = stringResource(R.string.settings_group_plan)) {
            SettingsRow(stringResource(R.string.settings_default_alloc),
                "${prefs.needsPct} · ${prefs.wantsPct} · ${prefs.investPct}",
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                onClick = onNavigateToAllocation)
            SettingsRow(stringResource(R.string.settings_period_start),
                stringResource(R.string.settings_period_day_format, prefs.planPeriodStartDay),
                icon = Icons.Outlined.CalendarToday,
                onClick = onNavigateToPeriodStart)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_security)) {
            SettingsRow(stringResource(R.string.settings_pin_bio),
                value = "",
                icon = Icons.Outlined.Shield,
                badge = if (prefs.biometricEnabled) activeStr else null,
                onClick = onNavigateToPin)
            SettingsRow(stringResource(R.string.settings_autolock),
                minutesStr,
                icon = Icons.Outlined.Lock,
                onClick = onNavigateToAutoLock)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_backup)) {
            // Backup row uses warning tile + warning sub when there's a real
            // last-backup timestamp; "Belum pernah" stays neutral.
            val backupWarn = prefs.lastBackupTimestamp != 0L
            SettingsRow(stringResource(R.string.settings_backup_restore), lastBackupLabel,
                icon = Icons.Outlined.CloudUpload,
                tone = if (backupWarn) RowTone.Warning else RowTone.Neutral,
                onClick = onNavigateToBackup)
            SettingsRow(stringResource(R.string.settings_export_pdf),
                stringResource(R.string.settings_export_pdf_sub),
                icon = Icons.Outlined.Description,
                onClick = onNavigateToExport)
            SettingsRow(stringResource(R.string.settings_export_reset),
                stringResource(R.string.settings_export_reset_sub),
                icon = Icons.Outlined.Delete,
                tone = RowTone.Danger, onClick = onNavigateToReset)
        }
        SettingsGroup(label = stringResource(R.string.settings_group_app)) {
            SettingsRow(stringResource(R.string.settings_language),
                if (prefs.language == "id") "Bahasa Indonesia" else "English",
                icon = Icons.Outlined.Language,
                onClick = onNavigateToLanguage)
            SettingsRow(stringResource(R.string.settings_theme),
                stringResource(
                    when (prefs.themeMode) {
                        "dark" -> R.string.theme_opt_dark
                        "light" -> R.string.theme_opt_light
                        else -> R.string.theme_opt_system
                    },
                ),
                icon = Icons.Outlined.DarkMode,
                onClick = onNavigateToTheme)
            SettingsRow(stringResource(R.string.settings_replay_onboarding),
                stringResource(R.string.settings_replay_onboarding_sub),
                icon = Icons.Outlined.Replay,
                onClick = onReplayOnboarding)
            SettingsRow(stringResource(R.string.settings_donate),
                stringResource(R.string.settings_donate_sub),
                icon = Icons.Outlined.AutoAwesome,
                onClick = onNavigateToDonate)
            SettingsRow(stringResource(R.string.settings_about), "v1.0",
                icon = Icons.Outlined.Info,
                onClick = onNavigateToAbout)
        }
        // Footer per proto 51-settings-hub.png — version + tagline.
        Spacer(Modifier.height(20.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH),
        ) {
            Text(stringResource(R.string.settings_footer_line1),
                color = sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.settings_footer_line2),
                color = sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
        }
        Spacer(Modifier.height(20.dp))
    }
}

private enum class RowTone { Neutral, Warning, Danger }

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
    tone: RowTone = if (danger) RowTone.Danger else if (warning) RowTone.Warning else RowTone.Neutral,
    badge: String? = null,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val (tileBg, tileFg, valueColor) = when (tone) {
        RowTone.Danger -> Triple(sw.dangerSoft, sw.danger, sw.danger)
        RowTone.Warning -> Triple(sw.warningSoft, sw.warning, sw.warning)
        RowTone.Neutral -> Triple(sw.primaryContainer, sw.onPrimaryContainer, sw.inkMuted)
    }
    val labelColor = if (tone == RowTone.Danger) sw.danger else sw.ink
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
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
            if (value.isNotEmpty()) {
                Text(value, color = valueColor,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp,
                        fontWeight = if (tone != RowTone.Neutral) FontWeight.SemiBold
                        else FontWeight.Normal))
            }
        }
        if (badge != null) {
            // Soft green badge per proto "Aktif" pill (51-settings-hub.png).
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(sw.successSoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(badge, color = sw.success,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp,
                        fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.size(width = 6.dp, height = 1.dp))
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}
