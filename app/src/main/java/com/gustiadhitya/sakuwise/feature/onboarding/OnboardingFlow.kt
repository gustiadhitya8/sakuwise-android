package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.feature.onboarding.viewmodel.OnboardingViewModel

@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val state by viewModel.state.collectAsState()

    // Seed state.lang from the active per-app locale once. The default in
    // OnboardingUiState is "id"; if the user never explicitly taps the radio
    // (because the picker already visually shows the active locale checked),
    // finish() would otherwise commit "id" and clobber a pre-set EN locale.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val active = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .takeIf { it.isNotEmpty() }
            ?: if (android.os.Build.VERSION.SDK_INT >= 33) {
                androidx.core.os.LocaleListCompat.getAdjustedDefault()
                    .toLanguageTags().split(",").firstOrNull().orEmpty()
            } else ""
        val tag = active.substringBefore('-').lowercase()
        if (tag == "en" || tag == "id") {
            if (viewModel.state.value.lang != tag) {
                viewModel.update { it.copy(lang = tag) }
            }
        }
    }
    // OnboardingUi adapter for the existing step screens (which take OnboardingUi).
    val ui = OnboardingUi(
        lang = state.lang,
        nickname = state.nickname,
        pin = state.pin,
        biometric = state.biometric,
        accountName = state.accountName,
        accountType = state.accountType,
        accountBalance = state.accountBalance,
    )
    val ctx = LocalContext.current
    val onChange: (OnboardingUi) -> Unit = { next ->
        viewModel.update {
            it.copy(
                lang = next.lang,
                nickname = next.nickname,
                pin = next.pin,
                biometric = next.biometric,
                accountName = next.accountName,
                accountType = next.accountType,
                accountBalance = next.accountBalance,
            )
        }
        // Always apply the picked locale — DON'T gate on langChanged. On cold
        // start the displayed locale may be the system locale even though
        // DataStore says "id", so user's "tap to confirm" must still write
        // through to AppCompatDelegate. Idempotent at the AppCompat layer.
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current != next.lang) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next.lang))
        }
    }
    val finish = { viewModel.finish() }

    // System back during onboarding: step back to the previous step instead of
    // silently killing the activity. On step 0 (splash) back is a no-op since
    // the splash auto-advances. On step 1 (first interactive step) we still
    // no-op rather than quit — user must finish onboarding or kill the app
    // via the OS task switcher.
    BackHandler(enabled = step > 1) {
        step -= 1
    }

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280))) togetherWith
                (slideOutHorizontally(tween(280)) { -it / 4 } + fadeOut(tween(120)))
        },
        label = "onboarding-step",
    ) { current ->
        when (current) {
            0 -> SplashScreen(onDone = { step = 1 })
            1 -> OnbLanguageScreen(state = ui, onChange = onChange, onNext = { step = 2 })
            2 -> OnbIdentityScreen(state = ui, onChange = onChange, onNext = { step = 3 })
            3 -> OnbPrivacyScreen(onNext = { step = 4 })
            4 -> OnbFirstAccountScreen(state = ui, onChange = onChange, onDone = finish)
        }
    }
}
