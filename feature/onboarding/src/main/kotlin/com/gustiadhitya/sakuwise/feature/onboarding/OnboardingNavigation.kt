package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object LanguageRoute

@Serializable
data object PrivacyRoute

fun NavGraphBuilder.splashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    composable<SplashRoute> {
        SplashScreen(
            onNavigateToOnboarding = onNavigateToOnboarding,
            onNavigateToHome = onNavigateToHome,
        )
    }
}

fun NavGraphBuilder.languageScreen(onNext: () -> Unit) {
    composable<LanguageRoute> {
        Onb_Language(onNext = onNext)
    }
}

fun NavGraphBuilder.privacyScreen(onDone: () -> Unit) {
    composable<PrivacyRoute> {
        Onb_Privacy(onDone = onDone)
    }
}
