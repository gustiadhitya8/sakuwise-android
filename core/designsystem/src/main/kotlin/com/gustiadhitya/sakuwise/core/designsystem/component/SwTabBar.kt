package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

data class SwTabItem(
    val label: String,
    val filledIcon: ImageVector,
    val outlineIcon: ImageVector,
)

private val NavBarHeight = 64.dp
private val FabSize = 56.dp
private val FabLift = 16.dp   // how much FAB extends above the nav bar top

@Composable
fun SwTabBar(
    tabs: List<SwTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(tabs.size == 4) { "SwTabBar requires exactly 4 tab items" }

    // Ordinal positions (1-indexed, 5 total; FAB occupies slot 3)
    val ordinals = listOf(1, 2, 4, 5)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NavBarHeight + FabLift),
    ) {
        // Nav bar surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(NavBarHeight)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalDivider(
                    thickness = SakuwiseSpacing.borderThin,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Tabs 0..1 (Beranda, Plan)
                    tabs.take(2).forEachIndexed { idx, tab ->
                        TabNavItem(
                            tab = tab,
                            selected = selectedIndex == idx,
                            ordinal = ordinals[idx],
                            onClick = { onTabSelected(idx) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Center spacer — FAB occupies slot 3 visually
                    Spacer(modifier = Modifier.weight(1f))
                    // Tabs 2..3 (Aset, Saya)
                    tabs.drop(2).forEachIndexed { relIdx, tab ->
                        val absIdx = relIdx + 2
                        TabNavItem(
                            tab = tab,
                            selected = selectedIndex == absIdx,
                            ordinal = ordinals[absIdx],
                            onClick = { onTabSelected(absIdx) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // FAB — elevated, centered, rises FabLift above nav bar top
        val fabColor = MaterialTheme.colorScheme.primary
        val fabContentColor = MaterialTheme.colorScheme.onPrimary
        Box(
            modifier = Modifier
                .size(FabSize)
                .align(Alignment.TopCenter)
                .shadow(elevation = 6.dp, shape = SakuwiseShapes.card, clip = false)
                .clip(SakuwiseShapes.card)
                .background(fabColor)
                .clickable(role = Role.Button, onClick = onFabClick)
                .semantics { contentDescription = "Tambah transaksi" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = fabContentColor,
                modifier = Modifier.size(SakuwiseSpacing.xxl),
            )
        }
    }
}

@Composable
private fun TabNavItem(
    tab: SwTabItem,
    selected: Boolean,
    ordinal: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (selected) tab.filledIcon else tab.outlineIcon
    val isSelected = selected
    val a11yDesc = if (selected) "${tab.label}, tab terpilih, $ordinal dari 5"
                   else "${tab.label}, tab, $ordinal dari 5"

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = SakuwiseSpacing.s)
            .semantics {
                contentDescription = a11yDesc
                this.selected = isSelected
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(SakuwiseSpacing.xxl),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

// Default tabs for previews and gallery (icons will be replaced with custom ones in M5d)
val DefaultSakuwiseTabs = listOf(
    SwTabItem("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    SwTabItem("Plan", Icons.Filled.List, Icons.Outlined.List),
    SwTabItem("Aset", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    SwTabItem("Saya", Icons.Filled.Person, Icons.Outlined.Person),
)

@Preview(showBackground = true)
@Composable
private fun SwTabBarPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                // Content placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                )
                var selected by remember { mutableIntStateOf(0) }
                SwTabBar(
                    tabs = DefaultSakuwiseTabs,
                    selectedIndex = selected,
                    onTabSelected = { selected = it },
                    onFabClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwTabBarPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                )
                var selected by remember { mutableIntStateOf(3) }
                SwTabBar(
                    tabs = DefaultSakuwiseTabs,
                    selectedIndex = selected,
                    onTabSelected = { selected = it },
                    onFabClick = {},
                )
            }
        }
    }
}
