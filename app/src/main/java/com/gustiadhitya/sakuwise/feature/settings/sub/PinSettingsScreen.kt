package com.gustiadhitya.sakuwise.feature.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.lock.LockViewModel
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet

@Composable
fun PinSettingsScreen(
    onBack: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
    lockVm: LockViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val prefs by main.prefs.collectAsState()
    var changePinSheet by remember { mutableStateOf(false) }
    val hasPin = remember(changePinSheet) { lockVm.hasPin() }

    SimpleSettingsScreen(title = stringResource(R.string.pin_settings_title), onBack = onBack) {
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.pin_settings_bio_title), color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.pin_settings_bio_sub),
                            color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    Switch(
                        checked = prefs.biometricEnabled,
                        onCheckedChange = { mutator.setBiometric(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = sw.onPrimary,
                            checkedTrackColor = sw.primary,
                        ),
                    )
                }
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().height(1.dp).background(sw.border),
                )
                // Passphrase mode toggle — when on, ChangePinSheet uses a
                // textual passphrase instead of a 6-digit PIN. Disabling
                // doesn't migrate existing credentials; the user must re-set.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.pin_settings_passphrase_title), color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.pin_settings_passphrase_sub),
                            color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    Switch(
                        checked = prefs.usePassphrase,
                        onCheckedChange = { mutator.setUsePassphrase(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = sw.onPrimary,
                            checkedTrackColor = sw.primary,
                        ),
                    )
                }
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxWidth().height(1.dp).background(sw.border),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { changePinSheet = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(if (hasPin) stringResource(R.string.pin_settings_change_title)
                        else stringResource(R.string.pin_settings_set_title),
                            color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text(
                            if (hasPin) stringResource(R.string.pin_settings_change_sub)
                            else stringResource(R.string.pin_settings_set_sub),
                            color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    Text(if (hasPin) stringResource(R.string.pin_settings_change_btn)
                        else stringResource(R.string.pin_settings_set_btn),
                        color = sw.primary,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(sw.primaryContainer)
                .padding(16.dp),
        ) {
            Text(
                stringResource(R.string.pin_settings_info),
                color = sw.onPrimaryContainer,
                style = SwType.Body.copy(fontSize = 12.sp),
            )
        }
    }

    if (changePinSheet) {
        ChangePinSheet(
            requireCurrent = hasPin,
            usePassphrase = prefs.usePassphrase,
            verifyCurrent = { lockVm.verifyPin(it) },
            onSave = { newPin -> lockVm.setPin(newPin); changePinSheet = false },
            onDismiss = { changePinSheet = false },
        )
    }
}

@Composable
private fun ChangePinSheet(
    requireCurrent: Boolean,
    usePassphrase: Boolean,
    verifyCurrent: (String) -> Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var step by remember {
        mutableStateOf(if (requireCurrent) PinStep.Current else PinStep.New)
    }
    var current by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val minLen = if (usePassphrase) 8 else 6
    fun valueOk(v: String) = if (usePassphrase) v.length >= minLen else v.length == 6
    val errWrong = stringResource(R.string.pin_sheet_err_wrong)
    val errMismatch = stringResource(R.string.pin_sheet_err_mismatch)
    SwPickerSheet(
        title = when (step) {
            PinStep.Current -> stringResource(R.string.pin_sheet_verify_title)
            PinStep.New -> stringResource(R.string.pin_sheet_new_title)
            PinStep.Confirm -> stringResource(R.string.pin_sheet_confirm_title)
        },
        onDismiss = onDismiss,
    ) {
        when (step) {
            PinStep.Current -> {
                SecretField(usePassphrase, current) { current = if (usePassphrase) it else it.take(6) }
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp))
                }
                Spacer(Modifier.height(12.dp))
                SwButton(stringResource(R.string.pin_sheet_next), enabled = valueOk(current), onClick = {
                    if (verifyCurrent(current)) { step = PinStep.New; error = null }
                    else error = errWrong
                })
            }
            PinStep.New -> {
                SecretField(usePassphrase, newPin) { newPin = if (usePassphrase) it else it.take(6) }
                if (usePassphrase && newPin.isNotEmpty() && newPin.length < minLen) {
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.passphrase_min_hint_format, minLen),
                        color = sw.inkSubtle,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                }
                Spacer(Modifier.height(12.dp))
                SwButton(stringResource(R.string.pin_sheet_next), enabled = valueOk(newPin), onClick = {
                    step = PinStep.Confirm; error = null
                })
            }
            PinStep.Confirm -> {
                SecretField(usePassphrase, confirm) { confirm = if (usePassphrase) it else it.take(6) }
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error!!, color = sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp))
                }
                Spacer(Modifier.height(12.dp))
                SwButton(stringResource(R.string.pin_sheet_save), enabled = valueOk(confirm), onClick = {
                    if (confirm == newPin) onSave(newPin)
                    else error = errMismatch
                })
            }
        }
    }
}

@Composable
private fun SecretField(usePassphrase: Boolean, value: String, onChange: (String) -> Unit) {
    if (!usePassphrase) {
        PinInput(value = value, onChange = onChange)
    } else {
        com.gustiadhitya.sakuwise.core.designsystem.components.SwField(
            value = value,
            onValueChange = onChange,
            label = stringResource(R.string.passphrase_field_label),
            placeholder = stringResource(R.string.passphrase_field_placeholder),
            password = true,
        )
    }
}

private enum class PinStep { Current, New, Confirm }
