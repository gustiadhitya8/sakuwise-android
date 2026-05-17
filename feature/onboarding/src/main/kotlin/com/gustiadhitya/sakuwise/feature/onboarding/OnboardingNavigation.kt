package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

fun NavGraphBuilder.splashScreen(onSplashComplete: () -> Unit) {
    composable<SplashRoute> {
        SplashScreen(onSplashComplete = onSplashComplete)
    }
}
