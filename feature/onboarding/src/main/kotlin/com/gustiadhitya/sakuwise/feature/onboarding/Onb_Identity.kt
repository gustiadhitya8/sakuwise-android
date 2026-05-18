package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

private val IdentityShieldSize: Dp = 90.dp
private val IdentityBadgeSize: Dp = 56.dp
private val IdentityBadgeIconSize: Dp = 28.dp

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

    val promptTitle = stringResource(R.string.onb_identity_biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.onb_identity_biometric_prompt_subtitle)
    val promptCancel = stringResource(R.string.onb_identity_biometric_prompt_cancel)

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
                            .setTitle(promptTitle)
                            .setSubtitle(promptSubtitle)
                            .setNegativeButtonText(promptCancel)
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
        title = stringResource(R.string.onb_identity_title),
        subtitle = stringResource(R.string.onb_identity_subtitle),
        heroContent = { IdentityHero() },
        actionLabel = stringResource(R.string.onb_identity_cta),
        onAction = onNext,
        actionEnabled = nickname.isNotBlank() && pin.length == 6,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SwField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = stringResource(R.string.onb_identity_name_label),
                placeholder = stringResource(R.string.onb_identity_name_placeholder),
                hint = stringResource(R.string.onb_identity_name_hint),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            Text(
                text = stringResource(R.string.onb_identity_pin_label),
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
                text = stringResource(R.string.onb_identity_pin_hint),
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
private fun IdentityHero() {
    OnbHeroSquircle {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SakuwiseIcons.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(IdentityShieldSize),
            )
            Box(
                modifier = Modifier
                    .size(IdentityBadgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = -SakuwiseSpacing.m, y = -SakuwiseSpacing.m)
                    .clip(SakuwiseShapes.card)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SakuwiseIcons.Me,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(IdentityBadgeIconSize),
                )
            }
        }
    }
}

@Composable
private fun BiometricToggleCard(
    enabled: Boolean,
    available: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val toggleDesc = stringResource(R.string.onb_identity_biometric_toggle_desc)
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
                text = stringResource(R.string.onb_identity_biometric_title),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
            Text(
                text = if (available) stringResource(R.string.onb_identity_biometric_available)
                else stringResource(R.string.onb_identity_biometric_unavailable),
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
                contentDescription = toggleDesc
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
