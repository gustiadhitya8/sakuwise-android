package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

enum class SwTab(val labelRes: Int, val outlineRes: Int, val filledRes: Int) {
    Home(R.string.tab_home, R.drawable.ic_tab_home_outline, R.drawable.ic_tab_home_filled),
    Plan(R.string.tab_plan, R.drawable.ic_tab_plan_outline, R.drawable.ic_tab_plan_filled),
    Assets(R.string.tab_assets, R.drawable.ic_tab_assets_outline, R.drawable.ic_tab_assets_filled),
    Me(R.string.tab_me, R.drawable.ic_tab_me_outline, R.drawable.ic_tab_me_filled),
}

/**
 * SwTabBar — 5-slot bottom navigation with a center FAB that floats 16dp
 * above the bar.
 *
 * Layout note (avoids the "white line crosses the FAB" artifact): the bar
 * surface is drawn inside an inner Column, and the FAB is rendered as a
 * *sibling overlay* in the outer Box. Drawing the FAB after the surface
 * means its solid fill covers the bg↔surface color seam regardless of
 * how shadows or anti-aliasing render.
 */
@Composable
fun SwTabBar(
    active: SwTab,
    onSelect: (SwTab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    val left = listOf(SwTab.Home, SwTab.Plan)
    val right = listOf(SwTab.Assets, SwTab.Me)
    val addLabel = stringResource(R.string.dashboard_add_txn)

    // Outer Box hosts the bar + FAB overlay; the bar starts ~16dp below the
    // box top so the FAB has room to float above the surface.
    Box(modifier = modifier.fillMaxWidth()) {
        // Bar surface (skipping the top 16dp so the FAB sits in clean bg)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(sw.surface),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                left.forEach { tab ->
                    TabItem(tab, active == tab, onClick = { onSelect(tab) }, modifier = Modifier.weight(1f))
                }
                // empty weighted slot reserved for the FAB
                Box(Modifier.weight(1f))
                right.forEach { tab ->
                    TabItem(tab, active == tab, onClick = { onSelect(tab) }, modifier = Modifier.weight(1f))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars),
            )
        }

        // FAB overlay — drawn after the bar so its solid fill covers any
        // bg↔surface color seam underneath.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false,
                    ambientColor = sw.primary,
                    spotColor = sw.primary,
                )
                .clip(RoundedCornerShape(18.dp))
                .background(sw.primary)
                .clickable(onClick = onAdd)
                .semantics { contentDescription = addLabel },
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null,
                tint = sw.onPrimary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: SwTab,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    val tint = if (active) sw.primary else sw.inkSubtle
    val label = stringResource(tab.labelRes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .semantics { contentDescription = "$label, tab" },
    ) {
        Icon(
            painter = painterResource(if (active) tab.filledRes else tab.outlineRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            label,
            color = tint,
            // lineHeight = fontSize keeps the label tight inside its line box
            // (avoids visual offset against the icon above).
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
