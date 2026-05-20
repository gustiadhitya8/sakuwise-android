package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.feature.onboarding.viewmodel.OnboardingViewModel

@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val state by viewModel.state.collectAsState()
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
    }
    val finish = { viewModel.finish() }

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
