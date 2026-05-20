package com.gustiadhitya.sakuwise.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun BackupSettingsScreen(
    onBack: () -> Unit,
    onOpenRestore: () -> Unit = {},
    main: MainViewModel = hiltViewModel(),
    vm: BackupViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    val state by vm.state.collectAsState()
    var pinFlowOpen by remember { mutableStateOf(false) }
    var driveRestoreOpen by remember { mutableStateOf(false) }

    val daysAgo = if (prefs.lastBackupTimestamp == 0L) -1
        else ((System.currentTimeMillis() - prefs.lastBackupTimestamp) / 86_400_000L).toInt()

    SimpleSettingsScreen(title = stringResource(R.string.backup_title), onBack = onBack) {
        // Status hero
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (daysAgo < 0 || daysAgo > 30) sw.warningSoft else sw.successSoft)
                .padding(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (daysAgo < 0 || daysAgo > 30) sw.warning else sw.success),
            ) { Icon(Icons.Outlined.Shield, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        daysAgo < 0 -> stringResource(R.string.backup_status_never)
                        daysAgo == 0 -> stringResource(R.string.backup_status_today)
                        else -> stringResource(R.string.backup_status_overdue_format, daysAgo)
                    },
                    color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
                Text(
                    stringResource(R.string.backup_status_body),
                    color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        SwButton(
            text = stringResource(R.string.backup_now),
            onClick = { pinFlowOpen = true; vm.reset() },
            size = SwButtonSize.Lg,
            leading = { Icon(Icons.Outlined.ContentCopy, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
        )
        Spacer(Modifier.height(10.dp))
        SwButton(
            text = stringResource(R.string.backup_restore),
            onClick = onOpenRestore,
            size = SwButtonSize.Md,
            variant = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant.Outline,
        )
        Spacer(Modifier.height(16.dp))

        SwCard {
            Column {
                Text(stringResource(R.string.backup_how_title), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                BackupStep(1, stringResource(R.string.backup_step1_title),
                    stringResource(R.string.backup_step1_body))
                BackupStep(2, stringResource(R.string.backup_step2_title),
                    stringResource(R.string.backup_step2_body))
                BackupStep(3, stringResource(R.string.backup_step3_title),
                    stringResource(R.string.backup_step3_body))
                BackupStep(4, stringResource(R.string.backup_step4_title),
                    stringResource(R.string.backup_step4_body))
            }
        }

        Spacer(Modifier.height(16.dp))
        // REQ-2: cloud backup section (Google Drive, AppDataFolder scope)
        DriveBackupSection(
            accountEmail = prefs.driveAccountEmail,
            autoBackupEnabled = prefs.driveBackupEnabled,
            lastDriveBackupTimestamp = prefs.lastDriveBackupTimestamp,
            vm = vm,
            onOpenDriveRestore = { driveRestoreOpen = true },
        )

        if (pinFlowOpen) {
            BackupPinFlowSheet(
                state = state,
                onSubmitPin = vm::startBackup,
                onDismiss = { pinFlowOpen = false; vm.reset() },
            )
        }

        if (driveRestoreOpen) {
            DriveRestoreSheet(
                vm = vm,
                onDismiss = { driveRestoreOpen = false },
            )
        }
    }
}

@Composable
private fun BackupStep(num: Int, title: String, body: String) {
    val sw = SwTheme.colors
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(sw.primary),
        ) {
            Text("$num", color = sw.onPrimary,
                fontSize = 12.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(width = 10.dp, height = 1.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
            Text(body, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
    }
}
