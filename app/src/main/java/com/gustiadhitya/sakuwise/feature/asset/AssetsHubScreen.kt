package com.gustiadhitya.sakuwise.feature.asset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwSectionLabel
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AssetsHubViewModel

@Composable
fun AssetsHubScreen(
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToGold: () -> Unit = {},
    onNavigateToLand: () -> Unit = {},
    onNavigateToDeposit: () -> Unit = {},
    onNavigateToDebt: () -> Unit = {},
    viewModel: AssetsHubViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val nw = state.netWorth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = SwSpace.bottomBarClear),
    ) {
        Text(stringResource(R.string.assets_title), color = sw.ink,
            style = SwType.H1.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = SwSpace.pageH, top = 8.dp, bottom = 12.dp))

        // Net Worth hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SwSpace.pageH)
                .clip(RoundedCornerShape(22.dp))
                .background(sw.primary),
        ) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp)) {
                LogoDaun(
                    sizeDp = 160,
                    bg = sw.onPrimary.copy(alpha = 0.10f),
                    leaf = sw.primary.copy(alpha = 0.10f),
                    vein = sw.onPrimary.copy(alpha = 0.10f),
                )
            }
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp)) {
                Text(stringResource(R.string.assets_total_wealth), color = sw.onPrimary.copy(alpha = 0.78f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = nw.total, color = sw.onPrimary, style = SwType.AmountXL)
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                ) {
                    val totalPos = (nw.accountsTotal + nw.goldTotal + nw.landTotal + nw.depositTotal)
                        .coerceAtLeast(1L)
                    if (nw.accountsTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.accountsTotal.toFloat() / totalPos)
                        .background(sw.accent))
                    if (nw.goldTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.goldTotal.toFloat() / totalPos)
                        .background(sw.warning))
                    if (nw.landTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.landTotal.toFloat() / totalPos)
                        .background(sw.info))
                    if (nw.depositTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.depositTotal.toFloat() / totalPos)
                        .background(sw.accent.copy(alpha = 0.7f)))
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // NetWorth trend chart (NetWorthTrendCard from proto) — real data
        NetWorthTrendCard(seriesAll = state.netWorthTrend)
        Spacer(Modifier.height(14.dp))

        SwSectionLabel(stringResource(R.string.assets_class_section), modifier = Modifier.padding(horizontal = SwSpace.pageH))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AssetClassCard(stringResource(R.string.assets_class_accounts),
                stringResource(R.string.assets_class_active_format, state.accounts.size),
                state.accountsTotal, sw.primary, Icons.Outlined.AccountBalanceWallet,
                Modifier.weight(1f), onClick = onNavigateToAccounts)
            AssetClassCard(stringResource(R.string.assets_class_gold),
                stringResource(R.string.assets_class_count_format, state.gold.size),
                nw.goldTotal, sw.warning, Icons.Outlined.Diamond,
                Modifier.weight(1f), onClick = onNavigateToGold)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AssetClassCard(stringResource(R.string.assets_class_land),
                stringResource(R.string.assets_class_count_format, state.land.size),
                nw.landTotal, sw.info, Icons.Outlined.Landscape,
                Modifier.weight(1f), onClick = onNavigateToLand)
            AssetClassCard(stringResource(R.string.assets_class_deposit),
                stringResource(R.string.assets_class_count_format, state.deposits.size),
                nw.depositTotal, sw.accent, Icons.Outlined.Savings,
                Modifier.weight(1f), onClick = onNavigateToDeposit)
        }
        Spacer(Modifier.height(16.dp))

        // Debt — always visible so user can drill in to add the first one
        SwSectionLabel(stringResource(R.string.assets_debt_section), modifier = Modifier.padding(horizontal = SwSpace.pageH))
        SwCard(
            modifier = Modifier.padding(horizontal = SwSpace.pageH),
            onClick = onNavigateToDebt,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.dangerSoft),
                ) { Icon(Icons.Outlined.Link, null, tint = sw.danger, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (nw.debtsTotal > 0) stringResource(R.string.assets_debt_i_owe)
                        else stringResource(R.string.assets_debt_title),
                        color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        if (nw.debtsTotal > 0) stringResource(R.string.assets_debt_outstanding)
                        else stringResource(R.string.assets_debt_empty),
                        color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp),
                    )
                }
                if (nw.debtsTotal > 0) {
                    RupiahText(value = nw.debtsTotal, short = true,
                        style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = sw.danger)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * NetWorthTrendCard — line+area chart of total net worth over time.
 * Series is computed from real account transactions + current asset/debt
 * values via ComputeNetWorthTrendUseCase. See its docstring for the formula.
 */
@Composable
private fun NetWorthTrendCard(seriesAll: List<Pair<java.time.LocalDate, Long>>) {
    val sw = SwTheme.colors
    var period by remember { mutableStateOf("6M") }
    val periods = listOf("3M", "6M", "1Y", stringResource(R.string.assets_trend_period_all))
    val series = seriesAll.map { it.second }
    val window = when (period) {
        "3M" -> series.takeLast(3); "6M" -> series.takeLast(6)
        "1Y" -> series.takeLast(12); else -> series
    }
    val delta = if (window.size >= 2) window.last() - window.first() else 0L
    val deltaPct = if (window.isNotEmpty() && window.first() != 0L)
        (delta * 100.0 / window.first()) else 0.0

    Column(
        modifier = Modifier
            .padding(horizontal = SwSpace.pageH)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(18.dp))
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.assets_trend_label), color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.TrendingUp, null,
                    tint = if (delta >= 0) sw.success else sw.danger,
                    modifier = Modifier.size(12.dp))
                Text(
                    "${if (delta >= 0) "+" else "−"} " +
                        kotlin.math.abs(delta).toRupiahShort(prefix = "Rp ") +
                        " · ${if (delta >= 0) "+" else "−"}" +
                        "%.1f".format(kotlin.math.abs(deltaPct)) + "%",
                    color = if (delta >= 0) sw.success else sw.danger,
                    style = SwType.Caption.copy(fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            if (window.size < 2) return@Canvas
            val maxV = window.max().toFloat()
            val minV = window.min().toFloat()
            val range = (maxV - minV).coerceAtLeast(1f)
            val w = size.width; val h = size.height
            val xs = window.indices.map { it * w / (window.size - 1) }
            val ys = window.map { h - ((it - minV) / range) * h * 0.85f - h * 0.05f }
            val area = Path().apply {
                moveTo(xs[0], h)
                for (i in xs.indices) lineTo(xs[i], ys[i])
                lineTo(xs.last(), h); close()
            }
            drawPath(area, brush = Brush.verticalGradient(
                listOf(sw.primary.copy(alpha = 0.22f), Color.Transparent),
            ))
            val line = Path().apply {
                moveTo(xs[0], ys[0])
                for (i in 1 until xs.size) lineTo(xs[i], ys[i])
            }
            drawPath(line, color = sw.primary,
                style = Stroke(width = 2.2f * density, cap = StrokeCap.Round))
            for (i in xs.indices) {
                val isLast = i == xs.size - 1
                drawCircle(color = sw.primary,
                    radius = if (isLast) 4f * density else 2.5f * density,
                    center = Offset(xs[i], ys[i]))
            }
        }
        Spacer(Modifier.height(8.dp))
        // Period selector (pill toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(sw.bg)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            periods.forEach { p ->
                val active = period == p
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) sw.surface else Color.Transparent)
                        .clickable { period = p }
                        .padding(vertical = 6.dp),
                ) {
                    Text(p,
                        color = if (active) sw.ink else sw.inkMuted,
                        style = SwType.LabelStrong.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ))
                }
            }
        }
    }
}

@Composable
private fun AssetClassCard(
    title: String,
    sub: String,
    value: Long,
    tint: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val sw = SwTheme.colors
    SwCard(modifier = modifier, padding = PaddingValues(14.dp), onClick = onClick) {
        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(tint.copy(alpha = 0.15f)),
            ) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(title, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text(sub, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
            Spacer(Modifier.height(8.dp))
            RupiahText(value = value, short = true,
                style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = sw.ink)
        }
    }
}
