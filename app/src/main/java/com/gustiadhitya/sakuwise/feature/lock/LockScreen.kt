package com.gustiadhitya.sakuwise.feature.lock

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.icons.Wordmark
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    lockVm: LockViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    val prefs by main.prefs.collectAsState()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var biometricTried by remember { mutableStateOf(false) }
    val hasPin = remember { lockVm.hasPin() }

    // Auto-trigger biometric on first compose if enabled
    LaunchedEffect(prefs.biometricEnabled) {
        if (prefs.biometricEnabled && !biometricTried && canUseBiometric(ctx)) {
            biometricTried = true
            promptBiometric(
                ctx = ctx,
                onSuccess = onUnlock,
                onError = { msg -> error = msg },
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().background(sw.bg).padding(32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(Modifier.height(80.dp))
            LogoDaun(sizeDp = 96)
            Spacer(Modifier.height(16.dp))
            Wordmark(sizeSp = 24)
            Spacer(Modifier.height(40.dp))
            Text(
                stringResource(R.string.lock_welcome_back),
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.lock_hint),
                color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 13.sp),
            )
            Spacer(Modifier.height(40.dp))
            val usePassphrase = prefs.usePassphrase
            if (usePassphrase) {
                com.gustiadhitya.sakuwise.core.designsystem.components.SwField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = null,
                    placeholder = stringResource(R.string.passphrase_field_placeholder),
                    password = true,
                )
            } else {
                PinInput(value = pin, onChange = { pin = it.take(6) })
            }
            Spacer(Modifier.height(12.dp))
            if (error != null) {
                Text(error!!, color = sw.danger,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                Spacer(Modifier.height(8.dp))
            }
            // First-launch path: no credential stored yet — first valid input sets it.
            // Otherwise verify against the stored Argon2id hash.
            val pinSixDigits = stringResource(R.string.lock_pin_six_digits)
            val passShortFmt = stringResource(R.string.passphrase_min_hint_format, 8)
            val pinWrong = stringResource(R.string.lock_pin_wrong)
            val minOk = if (usePassphrase) pin.length >= 8 else pin.length == 6
            SwButton(
                text = if (hasPin) stringResource(R.string.lock_unlock)
                       else stringResource(R.string.lock_set_pin_and_unlock),
                onClick = {
                    if (!minOk) {
                        error = if (usePassphrase) passShortFmt else pinSixDigits
                        return@SwButton
                    }
                    if (!hasPin) { lockVm.setPin(pin); onUnlock(); return@SwButton }
                    if (lockVm.verifyPin(pin)) onUnlock() else error = pinWrong
                },
                enabled = minOk,
            )
            if (prefs.biometricEnabled && canUseBiometric(ctx)) {
                Spacer(Modifier.height(8.dp))
                SwButton(
                    text = stringResource(R.string.lock_use_biometric),
                    variant = SwButtonVariant.Ghost,
                    onClick = {
                        promptBiometric(
                            ctx = ctx, onSuccess = onUnlock,
                            onError = { msg -> error = msg },
                        )
                    },
                )
            }
        }
    }
}

private fun canUseBiometric(ctx: Context): Boolean {
    val mgr = BiometricManager.from(ctx)
    val result = mgr.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
    )
    return result == BiometricManager.BIOMETRIC_SUCCESS
}

private fun promptBiometric(
    ctx: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val activity = ctx as? FragmentActivity ?: run {
        onError("Biometrik tidak didukung di context ini.")
        return
    }
    val executor = androidx.core.content.ContextCompat.getMainExecutor(ctx)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            // Cancellation is not an error worth showing to the user
            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                onError(errString.toString())
            }
        }
    }
    val prompt = BiometricPrompt(activity, executor, callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Buka Sakuwise")
        .setSubtitle("Gunakan biometrik untuk membuka aplikasi.")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        .build()
    prompt.authenticate(info)
}
