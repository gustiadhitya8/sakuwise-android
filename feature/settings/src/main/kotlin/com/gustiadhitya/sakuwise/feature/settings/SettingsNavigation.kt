package com.gustiadhitya.sakuwise.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object MeRoute

fun NavGraphBuilder.meScreen() {
    composable<MeRoute> {
        MeScreen()
    }
}
