package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.designsystem.component.PinInput
import com.gustiadhitya.sakuwise.core.designsystem.component.SwField
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

@Composable
fun Onb_Identity(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        val available = biometricManager.canAuthenticate(BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
        viewModel.setBiometricAvailable(available)
        onDispose {}
    }

    Onb_IdentityContent(
        nickname = uiState.nickname,
        pin = uiState.pin,
        biometricEnabled = uiState.biometricEnabled,
        biometricAvailable = uiState.biometricAvailable,
        onNicknameChange = viewModel::setNickname,
        onPinChange = viewModel::setPin,
        onBiometricToggle = { shouldEnable ->
            if (shouldEnable) {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    val executor = ContextCompat.getMainExecutor(context)
                    val callback = object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult,
                        ) {
                            viewModel.setBiometricEnabled(true)
                        }
                    }
                    BiometricPrompt(activity, executor, callback).authenticate(
                        BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Aktifkan biometrik")
                            .setSubtitle("Konfirmasi untuk menggunakan sidik jari / wajah")
                            .setNegativeButtonText("Batal")
                            .build()
                    )
                }
            } else {
                viewModel.setBiometricEnabled(false)
            }
        },
        onNext = { viewModel.confirmIdentity(onNext) },
        modifier = modifier,
    )
}

@Composable
internal fun Onb_IdentityContent(
    nickname: String,
    pin: String,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    onNicknameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingShell(
        stepIndex = 1,
        totalSteps = 4,
        title = "Atur identitas singkat",
        subtitle = "Cuma untuk sapaan dan keamanan. Tidak ada data ke server, ini semua tetap di HP kamu.",
        heroContent = {
            Icon(
                imageVector = SakuwiseIcons.Me,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(SakuwiseSpacing.xxxl),
            )
        },
        actionLabel = "Lanjut",
        onAction = onNext,
        actionEnabled = nickname.isNotBlank() && pin.length == 6,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SwField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = "Nama panggilan",
                placeholder = "Contoh: Gusti",
                hint = "Dipakai untuk sapaan di Beranda.",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            Text(
                text = "PIN 6 digit (cadangan biometrik)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Spacer(Modifier.height(SakuwiseSpacing.s))
            PinInput(
                value = pin,
                onValueChange = onPinChange,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(SakuwiseSpacing.xs))
            Text(
                text = "Dipakai kalau biometrik gagal. Bisa diganti kapan saja di Pengaturan.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SakuwiseTokens.current.inkSubtle,
                ),
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            BiometricToggleCard(
                enabled = biometricEnabled,
                available = biometricAvailable,
                onToggle = onBiometricToggle,
            )
        }
    }
}

@Composable
private fun BiometricToggleCard(
    enabled: Boolean,
    available: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SakuwiseShapes.lg)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(SakuwiseSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = SakuwiseIcons.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(SakuwiseSpacing.xl),
        )
        Spacer(Modifier.width(SakuwiseSpacing.m))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Buka pakai biometrik",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
            Text(
                text = if (available) "Sidik jari / wajah — lebih cepat dari ketik PIN."
                else "Biometrik tidak tersedia di device ini.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SakuwiseTokens.current.inkSubtle,
                ),
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            enabled = available,
            modifier = Modifier.semantics {
                contentDescription = "Toggle biometrik"
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IdentityPreviewLight() {
    SakuwiseTheme {
        Onb_IdentityContent(
            nickname = "Gusti",
            pin = "123456",
            biometricEnabled = true,
            biometricAvailable = true,
            onNicknameChange = {},
            onPinChange = {},
            onBiometricToggle = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IdentityPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Onb_IdentityContent(
            nickname = "",
            pin = "",
            biometricEnabled = false,
            biometricAvailable = false,
            onNicknameChange = {},
            onPinChange = {},
            onBiometricToggle = {},
            onNext = {},
        )
    }
}
