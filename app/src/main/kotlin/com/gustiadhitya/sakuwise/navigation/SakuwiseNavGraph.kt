package com.gustiadhitya.sakuwise.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gustiadhitya.sakuwise.core.designsystem.theme.LocalReduceMotion
import com.gustiadhitya.sakuwise.feature.onboarding.LanguageRoute
import com.gustiadhitya.sakuwise.feature.onboarding.PrivacyRoute
import com.gustiadhitya.sakuwise.feature.onboarding.SplashRoute
import com.gustiadhitya.sakuwise.feature.onboarding.languageScreen
import com.gustiadhitya.sakuwise.feature.onboarding.privacyScreen
import com.gustiadhitya.sakuwise.feature.onboarding.splashScreen

private val NavEasing = CubicBezierEasing(0.2f, 0.7f, 0.3f, 1f)
private const val MediumDuration = 280
private const val ReduceMotionDuration = 100

@Composable
fun SakuwiseNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val reduceMotion = LocalReduceMotion.current

    val enterDuration = if (reduceMotion) ReduceMotionDuration else MediumDuration
    val exitDuration = if (reduceMotion) ReduceMotionDuration else MediumDuration

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
        enterTransition = {
            if (reduceMotion) fadeIn(tween(enterDuration))
            else slideInHorizontally(tween(enterDuration, easing = NavEasing)) { it / 4 } +
                    fadeIn(tween(enterDuration, easing = NavEasing))
        },
        exitTransition = {
            if (reduceMotion) fadeOut(tween(exitDuration))
            else slideOutHorizontally(tween(exitDuration, easing = NavEasing)) { -it / 4 } +
                    fadeOut(tween(exitDuration, easing = NavEasing))
        },
        popEnterTransition = {
            if (reduceMotion) fadeIn(tween(enterDuration))
            else slideInHorizontally(tween(enterDuration, easing = NavEasing)) { -it / 4 } +
                    fadeIn(tween(enterDuration, easing = NavEasing))
        },
        popExitTransition = {
            if (reduceMotion) fadeOut(tween(exitDuration))
            else slideOutHorizontally(tween(exitDuration, easing = NavEasing)) { it / 4 } +
                    fadeOut(tween(exitDuration, easing = NavEasing))
        },
    ) {
        splashScreen(
            onNavigateToOnboarding = {
                navController.navigate(LanguageRoute) {
                    popUpTo<SplashRoute> { inclusive = true }
                }
            },
            onNavigateToHome = {
                navController.navigate(HomeRoute) {
                    popUpTo<SplashRoute> { inclusive = true }
                }
            },
        )

        languageScreen(
            onNext = {
                navController.navigate(PrivacyRoute)
            },
        )

        privacyScreen(
            onDone = {
                navController.navigate(HomeRoute) {
                    popUpTo<SplashRoute> { inclusive = true }
                }
            },
        )

        composable<HomeRoute> {
            HomeNavGraph()
        }
    }
}
