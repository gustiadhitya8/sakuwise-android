package com.gustiadhitya.sakuwise.feature.dashboard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

fun NavGraphBuilder.dashboardScreen() {
    composable<DashboardRoute> {
        DashboardScreen()
    }
}
