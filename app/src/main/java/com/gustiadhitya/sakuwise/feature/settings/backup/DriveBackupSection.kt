package com.gustiadhitya.sakuwise.feature.settings.backup

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.cloud.DriveBackupEntry
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cloud backup section embedded in [BackupSettingsScreen]. Renders sign-in /
 * upload / restore-list controls for the Drive AppDataFolder.
 */
@Composable
fun DriveBackupSection(
    accountEmail: String?,
    autoBackupEnabled: Boolean,
    lastDriveBackupTimestamp: Long,
    vm: BackupViewModel,
    onOpenDriveRestore: () -> Unit,
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    val driveState by vm.driveState.collectAsState()
    var showDriveBackupPin by remember { mutableStateOf(false) }
    // PIN setup for auto-backup (shown when toggle is turned ON, or "Ubah" tapped)
    var showAutoBackupPinSetup by remember { mutableStateOf(false) }
    var autoBackupPinChangeMode by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.onDriveSignInResult(result.data)
        } else {
            vm.onDriveSignInResult(null)
        }
    }

    SwCard {
        Column {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.primaryContainer),
                ) {
                    Icon(
                        Icons.Outlined.CloudUpload, null,
                        tint = sw.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.drive_section_title),
                        color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    )
                    Text(
                        stringResource(R.string.drive_section_subtitle),
                        color = sw.inkMuted,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (accountEmail == null) {
                // Not signed in
                SwButton(
                    text = stringResource(R.string.drive_connect),
                    onClick = { signInLauncher.launch(vm.buildDriveSignInIntent()) },
                    size = SwButtonSize.Md,
                    leading = {
                        Icon(
                            Icons.Outlined.Link, null,
                            tint = sw.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            } else {
                // Signed in ─────────────────────────────────────────────────

                // Account row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(sw.success),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.drive_signed_in_as, accountEmail),
                        color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (lastDriveBackupTimestamp <= 0L)
                        stringResource(R.string.drive_last_backup_never)
                    else stringResource(
                        R.string.drive_last_backup_format,
                        SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
                            .format(Date(lastDriveBackupTimestamp)),
                    ),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
                Spacer(Modifier.height(12.dp))

                // ① Primary action: backup now (PIN required for encryption)
                SwButton(
                    text = stringResource(R.string.drive_backup_now),
                    onClick = { showDriveBackupPin = true },
                    size = SwButtonSize.Md,
                    enabled = !driveState.busy,
                    leading = {
                        Icon(
                            Icons.Outlined.CloudUpload, null,
                            tint = sw.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                Spacer(Modifier.height(8.dp))

                // ② Secondary action: open restore list
                SwButton(
                    text = stringResource(R.string.drive_restore_open),
                    onClick = {
                        vm.refreshDriveBackups()
                        onOpenDriveRestore()
                    },
                    size = SwButtonSize.Md,
                    variant = SwButtonVariant.Outline,
                    enabled = !driveState.busy,
                    leading = {
                        Icon(
                            Icons.Outlined.Restore, null,
                            tint = sw.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))

                // ③ Auto-backup toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.drive_auto_toggle),
                            color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            stringResource(R.string.drive_auto_toggle_hint),
                            color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Show PIN setup instead of enabling immediately
                                autoBackupPinChangeMode = false
                                showAutoBackupPinSetup = true
                            } else {
                                vm.disableAutoBackup()
                            }
                        },
                    )
                }
                if (autoBackupEnabled) {
                    Spacer(Modifier.height(6.dp))
                    // Status row: last run time
                    val lastAutoLabel = if (lastDriveBackupTimestamp <= 0L)
                        stringResource(R.string.drive_auto_last_never)
                    else stringResource(
                        R.string.drive_auto_last_success,
                        SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
                            .format(Date(lastDriveBackupTimestamp)),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (lastDriveBackupTimestamp > 0L) sw.success else sw.warning),
                        )
                        Text(
                            lastAutoLabel,
                            color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // PIN status chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(sw.successSoft)
                            .clickable {
                                autoBackupPinChangeMode = true
                                showAutoBackupPinSetup = true
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            if (vm.autoBackupPinStored) Icons.Outlined.Lock else Icons.Outlined.Edit,
                            null,
                            tint = sw.success,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            if (vm.autoBackupPinStored) stringResource(R.string.autobackup_pin_stored)
                            else stringResource(R.string.autobackup_pin_none),
                            color = sw.success,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // ④ Sign out (bottom, least prominent)
                Box(
                    modifier = Modifier
                        .clickable { vm.signOutFromDrive() }
                        .padding(vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Logout, null,
                            tint = sw.danger,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            stringResource(R.string.drive_signout),
                            color = sw.danger,
                            style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }

            if (driveState.busy) {
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    color = sw.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            driveState.error?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
            }
            driveState.lastMessage?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, color = sw.success, style = SwType.LabelSmall.copy(fontSize = 12.sp))
            }
        }
    }

    // PIN sheet for Drive "Cadangkan Sekarang" — one-off upload
    if (showDriveBackupPin) {
        DriveBackupPinSheet(
            vm = vm,
            onDismiss = { showDriveBackupPin = false },
        )
    }

    // PIN setup sheet for auto-backup — shown when toggle is turned ON or "Ubah" is tapped
    if (showAutoBackupPinSetup) {
        AutoBackupPinSetupSheet(
            changeMode = autoBackupPinChangeMode,
            onConfirm = { pin ->
                if (autoBackupPinChangeMode) {
                    vm.changeAutoBackupPin(pin)
                } else {
                    vm.enableAutoBackupWithPin(pin)
                }
                showAutoBackupPinSetup = false
            },
            onDismiss = { showAutoBackupPinSetup = false },
        )
    }
}

/**
 * PIN entry sheet shown when the user taps "Cadangkan Sekarang" in the Drive
 * section. Creates a fresh encrypted backup and uploads it directly to Drive —
 * no pre-existing local backup file required.
 */
@Composable
private fun DriveBackupPinSheet(
    vm: BackupViewModel,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    val driveState by vm.driveState.collectAsState()
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    val pinMust6 = stringResource(R.string.pin_must_be_6_digits)

    // Auto-dismiss once upload succeeds
    LaunchedEffect(driveState.lastMessage) {
        if (driveState.lastMessage != null) onDismiss()
    }

    SwPickerSheet(
        title = stringResource(R.string.drive_backup_now),
        onDismiss = onDismiss,
    ) {
        if (driveState.busy) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = sw.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.drive_backup_progress),
                    color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 13.sp),
                )
            }
        } else {
            Text(
                stringResource(R.string.drive_backup_pin_intro),
                color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 13.sp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.drive_backup_pin_label),
                color = sw.inkMuted,
                style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(6.dp))
            PinInput(value = pin, onChange = { pin = it.take(6) })
            Spacer(Modifier.height(12.dp))
            pinError?.let {
                Text(it, color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                Spacer(Modifier.height(8.dp))
            }
            driveState.error?.let {
                Text(it, color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                Spacer(Modifier.height(8.dp))
            }
            SwButton(
                text = stringResource(R.string.drive_backup_confirm),
                onClick = {
                    if (pin.length != 6) { pinError = pinMust6; return@SwButton }
                    pinError = null
                    vm.backupToDriveWithPin(pin.toCharArray())
                },
                enabled = pin.length == 6 && !driveState.busy,
                size = SwButtonSize.Lg,
                leading = {
                    Icon(
                        Icons.Outlined.CloudUpload, null,
                        tint = sw.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
            Spacer(Modifier.height(8.dp))
            SwButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss,
                variant = SwButtonVariant.Ghost,
                size = SwButtonSize.Md,
            )
        }
    }
}

/**
 * Sheet listing existing Drive backups. Tapping "Pulihkan" opens a PIN entry
 * sub-stage; once PIN is in and confirmed, [BackupViewModel.restoreFromDrive]
 * does the download + decrypt + DB swap and the parent screen flips to the
 * existing post-restore success card.
 */
@Composable
fun DriveRestoreSheet(
    vm: BackupViewModel,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    val backups by vm.driveBackups.collectAsState()
    val driveState by vm.driveState.collectAsState()
    val restoreState by vm.restoreState.collectAsState()

    var selected by remember { mutableStateOf<DriveBackupEntry?>(null) }
    var showBackupPinSheet by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var inProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refreshDriveBackups() }

    SwPickerSheet(
        title = stringResource(R.string.drive_list_title),
        onDismiss = onDismiss,
    ) {
        when {
            // ── Success: show inline success card + relaunch button (mirrors local restore) ──
            restoreState is RestoreState.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(sw.successSoft)
                        .padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CheckCircle, null,
                                tint = sw.success,
                                modifier = Modifier.size(28.dp),
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(
                                stringResource(R.string.restore_success_title),
                                color = sw.success,
                                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.restore_success_body),
                            color = sw.ink,
                            style = SwType.Body.copy(fontSize = 13.sp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                SwButton(
                    text = stringResource(R.string.restore_relaunch),
                    onClick = vm::relaunchAfterRestore,
                    size = SwButtonSize.Lg,
                    leading = {
                        Icon(
                            Icons.Outlined.OpenInNew, null,
                            tint = sw.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.restore_footer),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
            }

            // ── List view: pick a backup to restore ──
            selected == null -> {
                if (backups.isEmpty() && !driveState.busy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(sw.surface)
                            .padding(16.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CloudOff, null,
                            tint = sw.inkMuted,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            stringResource(R.string.drive_list_empty),
                            color = sw.inkMuted,
                            style = SwType.Body.copy(fontSize = 13.sp),
                        )
                    }
                } else {
                    backups.forEach { entry ->
                        DriveBackupRow(
                            entry = entry,
                            onRestore = { selected = entry; pin = ""; pinError = null },
                            onDelete = { vm.deleteDriveBackup(entry) },
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
                driveState.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                }
            }

            // ── PIN entry / loading state ──
            else -> {
                val entry = selected!!
                Text(
                    entry.name,
                    color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(8.dp))

                if (inProgress) {
                    // Show progress while downloading + decrypting
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    ) {
                        CircularProgressIndicator(
                            color = sw.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.drive_restore_progress),
                            color = sw.inkMuted,
                            style = SwType.Body.copy(fontSize = 13.sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.drive_restore_progress_hint),
                            color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.drive_restore_intro),
                        color = sw.inkMuted,
                        style = SwType.Body.copy(fontSize = 13.sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.drive_restore_pin_label),
                        color = sw.inkMuted,
                        style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(Modifier.height(6.dp))
                    PinInput(value = pin, onChange = { pin = it.take(6) })
                    Spacer(Modifier.height(12.dp))
                    pinError?.let {
                        Text(it, color = sw.danger, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                        Spacer(Modifier.height(8.dp))
                    }
                    SwButton(
                        text = stringResource(R.string.drive_restore_confirm),
                        onClick = {
                            if (pin.length != 6) return@SwButton
                            inProgress = true
                            pinError = null
                            vm.restoreFromDrive(entry, pin.toCharArray()) { msg ->
                                inProgress = false
                                pinError = msg
                            }
                        },
                        enabled = pin.length == 6,
                        size = SwButtonSize.Lg,
                    )
                    Spacer(Modifier.height(8.dp))
                    SwButton(
                        text = stringResource(R.string.action_cancel),
                        onClick = { selected = null; pin = ""; pinError = null },
                        variant = SwButtonVariant.Ghost,
                        size = SwButtonSize.Md,
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet that collects a 6-digit PIN for auto-backup.
 *
 * When [changeMode] is false this is the initial setup (toggle just turned ON).
 * When [changeMode] is true the user tapped "Ubah" on the PIN chip.
 *
 * Two-step PIN entry (enter + confirm) mirrors the manual backup PIN flow.
 * On confirmation [onConfirm] is called with the PIN array; the sheet is
 * responsible for zeroing it once consumed.
 */
@Composable
private fun AutoBackupPinSetupSheet(
    changeMode: Boolean,
    onConfirm: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var pin by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(AutoPinStage.Enter) }

    SwPickerSheet(
        title = if (changeMode) stringResource(R.string.autobackup_pin_change_title) else stringResource(R.string.autobackup_pin_create_title),
        onDismiss = onDismiss,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().height(96.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(sw.primaryContainer),
            ) {
                Icon(
                    Icons.Outlined.Lock, null,
                    tint = sw.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Text(
            when (stage) {
                AutoPinStage.Enter ->
                    if (changeMode) stringResource(R.string.autobackup_pin_enter_new)
                    else stringResource(R.string.autobackup_pin_enter_create)
                AutoPinStage.Confirm -> stringResource(R.string.autobackup_pin_confirm_prompt)
            },
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        PinInput(
            value = if (stage == AutoPinStage.Enter) pin else pin2,
            onChange = { v ->
                if (stage == AutoPinStage.Enter) pin = v.take(6) else pin2 = v.take(6)
            },
            onComplete = {
                if (stage == AutoPinStage.Enter) {
                    stage = AutoPinStage.Confirm
                } else if (pin == pin2 && pin.length == 6) {
                    onConfirm(pin.toCharArray())
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        if (stage == AutoPinStage.Confirm && pin2.length == 6 && pin != pin2) {
            Text(
                stringResource(R.string.autobackup_pin_mismatch),
                color = sw.danger,
                style = SwType.LabelSmall.copy(fontSize = 12.sp),
            )
            Spacer(Modifier.height(8.dp))
        }
        com.gustiadhitya.sakuwise.core.designsystem.components.SwButton(
            text = if (stage == AutoPinStage.Enter) stringResource(R.string.action_continue) else stringResource(R.string.autobackup_pin_save),
            onClick = {
                if (stage == AutoPinStage.Enter) {
                    if (pin.length == 6) stage = AutoPinStage.Confirm
                } else {
                    if (pin == pin2 && pin.length == 6) onConfirm(pin.toCharArray())
                }
            },
            enabled = when (stage) {
                AutoPinStage.Enter -> pin.length == 6
                AutoPinStage.Confirm -> pin2.length == 6 && pin == pin2
            },
            size = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize.Lg,
        )
        Spacer(Modifier.height(8.dp))
        com.gustiadhitya.sakuwise.core.designsystem.components.SwButton(
            text = stringResource(R.string.action_cancel),
            onClick = onDismiss,
            variant = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant.Ghost,
            size = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize.Md,
        )
        Spacer(Modifier.height(12.dp))
        // Warning box
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(sw.warningSoft)
                .padding(10.dp),
        ) {
            Text(
                stringResource(R.string.autobackup_pin_warning),
                color = sw.ink,
                style = SwType.LabelSmall.copy(fontSize = 11.sp),
            )
        }
    }
}

private enum class AutoPinStage { Enter, Confirm }

@Composable
private fun DriveBackupRow(
    entry: DriveBackupEntry,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(sw.surface)
            .padding(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                entry.name,
                color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            )
            Text(
                SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
                    .format(Date(entry.createdAt)) + " · " +
                    stringResource(R.string.drive_entry_size_format, entry.sizeBytes / 1024),
                color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(sw.primary)
                    .clickable(onClick = onRestore)
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    stringResource(R.string.drive_restore_btn),
                    color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(sw.dangerSoft)
                    .clickable(onClick = onDelete),
            ) {
                Icon(
                    Icons.Outlined.Delete, null,
                    tint = sw.danger,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
