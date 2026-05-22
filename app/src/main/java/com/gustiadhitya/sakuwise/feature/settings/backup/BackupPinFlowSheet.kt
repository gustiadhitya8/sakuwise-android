package com.gustiadhitya.sakuwise.feature.settings.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet

@Composable
fun BackupPinFlowSheet(
    state: BackupUiState,
    onSubmitPin: (CharArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var pin by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(PinStage.Enter) }

    SwPickerSheet(
        title = when (state.stage) {
            Stage.Encrypting -> stringResource(R.string.backup_pin_title_encrypting)
            Stage.Done -> stringResource(R.string.backup_pin_title_done)
            else -> if (stage == PinStage.Enter) stringResource(R.string.backup_pin_title_create)
                else stringResource(R.string.backup_pin_title_confirm)
        },
        onDismiss = onDismiss,
    ) {
        when (state.stage) {
            Stage.Idle, Stage.PickLocation -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(sw.primaryContainer),
                    ) { Icon(Icons.Outlined.Shield, null, tint = sw.primary, modifier = Modifier.size(40.dp)) }
                }
                Text(
                    if (stage == PinStage.Enter) stringResource(R.string.backup_pin_intro_enter)
                    else stringResource(R.string.backup_pin_intro_confirm),
                    color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
                )
                Spacer(Modifier.height(16.dp))
                PinInput(
                    value = if (stage == PinStage.Enter) pin else pin2,
                    onChange = { v ->
                        if (stage == PinStage.Enter) pin = v.take(6) else pin2 = v.take(6)
                    },
                    onComplete = {
                        if (stage == PinStage.Enter) stage = PinStage.Confirm
                        else if (pin == pin2) onSubmitPin(pin.toCharArray())
                    },
                )
                Spacer(Modifier.height(12.dp))
                if (state.errorMessage != null) {
                    Text(state.errorMessage, color = sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    Spacer(Modifier.height(8.dp))
                }
                if (stage == PinStage.Confirm && pin2.length == 6 && pin != pin2) {
                    Text(stringResource(R.string.backup_pin_err_mismatch), color = sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    Spacer(Modifier.height(8.dp))
                }
                SwButton(
                    text = if (stage == PinStage.Enter) stringResource(R.string.backup_pin_next)
                        else stringResource(R.string.backup_pin_confirm_start),
                    onClick = {
                        if (stage == PinStage.Enter) stage = PinStage.Confirm
                        else if (pin == pin2 && pin.length == 6) onSubmitPin(pin.toCharArray())
                    },
                    enabled = when (stage) {
                        PinStage.Enter -> pin.length == 6
                        PinStage.Confirm -> pin2.length == 6 && pin == pin2
                    },
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.warningSoft)
                        .padding(12.dp),
                ) {
                    Text(
                        stringResource(R.string.backup_pin_warning),
                        color = sw.ink, style = SwType.LabelSmall.copy(fontSize = 12.sp),
                    )
                }
            }
            Stage.Encrypting -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = sw.primary,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    stringResource(R.string.backup_encrypting_body),
                    color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
                )
            }
            Stage.Done -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(sw.successSoft),
                    ) { Icon(Icons.Outlined.Check, null, tint = sw.success, modifier = Modifier.size(40.dp)) }
                }
                Text(stringResource(R.string.backup_done_title), color = sw.ink,
                    style = SwType.H2.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.backup_done_size_format,
                        state.resultFilePath?.substringAfterLast('/').orEmpty(),
                        state.resultFileSize / 1024),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp, fontFeatureSettings = "tnum"),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    state.resultFilePath ?: "",
                    color = sw.inkSubtle,
                    style = SwType.LabelSmall.copy(fontSize = 10.sp),
                )
                Spacer(Modifier.height(16.dp))
                SwButton(text = stringResource(R.string.backup_done_finish), onClick = onDismiss)
            }
        }
    }
}

private enum class PinStage { Enter, Confirm }
