package com.gustiadhitya.sakuwise.feature.asset

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object AssetsHubRoute

fun NavGraphBuilder.assetsHubScreen() {
    composable<AssetsHubRoute> {
        AssetsHubScreen()
    }
}
