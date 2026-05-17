package com.gustiadhitya.sakuwise.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gustiadhitya.sakuwise.core.designsystem.component.DefaultSakuwiseTabs
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTabBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.feature.asset.AssetsHubRoute
import com.gustiadhitya.sakuwise.feature.asset.assetsHubScreen
import com.gustiadhitya.sakuwise.feature.dashboard.DashboardRoute
import com.gustiadhitya.sakuwise.feature.dashboard.dashboardScreen
import com.gustiadhitya.sakuwise.feature.plan.PlanRoute
import com.gustiadhitya.sakuwise.feature.plan.planScreen
import com.gustiadhitya.sakuwise.feature.settings.MeRoute
import com.gustiadhitya.sakuwise.feature.settings.meScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavGraph(modifier: Modifier = Modifier) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()

    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tabRoutes = listOf(DashboardRoute, PlanRoute, AssetsHubRoute, MeRoute)

    val selectedTab = when {
        navBackStackEntry?.destination?.route?.contains("DashboardRoute") == true -> 0
        navBackStackEntry?.destination?.route?.contains("PlanRoute") == true -> 1
        navBackStackEntry?.destination?.route?.contains("AssetsHubRoute") == true -> 2
        navBackStackEntry?.destination?.route?.contains("MeRoute") == true -> 3
        else -> 0
    }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = homeNavController,
            startDestination = DashboardRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = SakuwiseSpacing.xxxxxxl + SakuwiseSpacing.l),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            dashboardScreen()
            planScreen()
            assetsHubScreen()
            meScreen()
        }

        SwTabBar(
            tabs = DefaultSakuwiseTabs,
            selectedIndex = selectedTab,
            onTabSelected = { idx ->
                homeNavController.navigate(tabRoutes[idx]) {
                    popUpTo(homeNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onFabClick = { showAddSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState = sheetState,
            ) {
                Box(
                    modifier = Modifier.padding(SakuwiseSpacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Tambah Transaksi — TODO",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
