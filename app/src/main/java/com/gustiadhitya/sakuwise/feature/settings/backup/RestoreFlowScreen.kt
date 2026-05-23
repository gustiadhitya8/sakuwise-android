package com.gustiadhitya.sakuwise.feature.settings.backup

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Restore
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen
import java.io.File
import java.io.FileOutputStream

@Composable
fun RestoreFlowScreen(
    onBack: () -> Unit,
    onRestored: () -> Unit,
    vm: BackupViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    val restoreState by vm.restoreState.collectAsState()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var inProgress by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            fileName = uri.lastPathSegment?.substringAfterLast('/') ?: uri.toString()
        }
    }

    SimpleSettingsScreen(title = stringResource(R.string.restore_title), onBack = onBack) {
        if (restoreState is RestoreState.Success) {
            RestoreSuccessCard(onRelaunch = vm::relaunchAfterRestore)
            return@SimpleSettingsScreen
        }
        Text(
            stringResource(R.string.restore_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))

        // File picker row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(sw.surface)
                .padding(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.primaryContainer),
            ) { Icon(Icons.Outlined.ContentCopy, null, tint = sw.onPrimaryContainer, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            Column(Modifier.weight(1f)) {
                Text(fileName ?: stringResource(R.string.restore_no_file), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                Text(
                    if (fileName != null) stringResource(R.string.restore_change_file)
                    else stringResource(R.string.restore_choose_file),
                    color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(sw.primary)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    if (fileName == null) stringResource(R.string.restore_btn_pick)
                    else stringResource(R.string.restore_btn_change),
                    color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { launcher.launch(arrayOf("*/*")) },
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Text(stringResource(R.string.restore_pin_label), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        Spacer(Modifier.height(6.dp))
        PinInput(
            value = pin,
            onChange = { pin = it.take(6) },
            onComplete = {
                val uri = selectedUri ?: return@PinInput
                if (inProgress) return@PinInput
                inProgress = true; error = null
                val tmp = java.io.File(ctx.cacheDir, "restore-tmp.sakuwise")
                ctx.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(tmp).use { out -> input.copyTo(out) }
                }
                vm.restoreFromFile(
                    file = tmp, pin = pin.toCharArray(),
                    onError = { msg -> inProgress = false; error = msg },
                )
            },
        )
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text(error!!, color = sw.danger,
                style = SwType.LabelSmall.copy(fontSize = 12.sp))
            Spacer(Modifier.height(8.dp))
        }

        SwButton(
            text = if (inProgress) stringResource(R.string.restore_btn_decrypting)
            else stringResource(R.string.restore_btn_restore),
            onClick = {
                val uri = selectedUri ?: return@SwButton
                inProgress = true
                error = null
                // Copy URI to a temp file so BackupService can read it directly
                val tmp = File(ctx.cacheDir, "restore-tmp.sakuwise")
                ctx.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tmp).use { out -> input.copyTo(out) }
                }
                vm.restoreFromFile(
                    file = tmp, pin = pin.toCharArray(),
                    onError = { msg -> inProgress = false; error = msg },
                )
            },
            enabled = selectedUri != null && pin.length == 6 && !inProgress,
            size = SwButtonSize.Lg,
            leading = { Icon(Icons.Outlined.Restore, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(sw.dangerSoft)
                .padding(14.dp),
        ) {
            Text(
                stringResource(R.string.restore_warning),
                color = sw.danger,
                style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun RestoreSuccessCard(onRelaunch: () -> Unit) {
    val sw = SwTheme.colors
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.successSoft)
                .padding(20.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = sw.success,
                        modifier = Modifier.size(28.dp))
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
            onClick = onRelaunch,
            size = SwButtonSize.Lg,
            leading = {
                Icon(Icons.Outlined.OpenInNew, null, tint = sw.onPrimary,
                    modifier = Modifier.size(18.dp))
            },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.restore_footer),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
        )
    }
}

