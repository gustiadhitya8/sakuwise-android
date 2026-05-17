package com.gustiadhitya.sakuwise.feature.plan

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object PlanRoute

fun NavGraphBuilder.planScreen() {
    composable<PlanRoute> {
        PlanScreen()
    }
}
