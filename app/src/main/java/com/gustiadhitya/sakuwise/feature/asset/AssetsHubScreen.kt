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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
                .background(sw.primaryHero),
        ) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp)) {
                LogoDaun(
                    sizeDp = 160,
                    bg = sw.onPrimaryHero.copy(alpha = 0.10f),
                    leaf = sw.primaryHero.copy(alpha = 0.10f),
                    vein = sw.onPrimaryHero.copy(alpha = 0.10f),
                )
            }
            // Local mask state per prototype eye toggle.
            var hideTotal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.assets_total_wealth),
                        color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                        modifier = Modifier.weight(1f))
                    // Eye toggle mirrors the dashboard "hide saldo" affordance.
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f))
                            .clickable { hideTotal = !hideTotal },
                    ) {
                        Icon(
                            if (hideTotal) androidx.compose.material.icons.Icons.Outlined.VisibilityOff
                            else androidx.compose.material.icons.Icons.Outlined.Visibility,
                            null,
                            tint = sw.onPrimaryHero,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (hideTotal) {
                        Text("Rp ••••••",
                            color = sw.onPrimaryHero,
                            style = SwType.AmountXL)
                    } else {
                        RupiahText(value = nw.total, color = sw.onPrimaryHero, style = SwType.AmountXL)
                    }
                    // YTD delta pill per screens-dashboard.jsx:220 — only render when we
                    // have enough trend history to compute a meaningful first vs last.
                    val series = state.netWorthTrend
                    if (series.size >= 2) {
                        val first = series.first().second
                        val last = series.last().second
                        if (first != 0L) {
                            val deltaPct = (last - first) * 100.0 / first
                            val sign = if (deltaPct >= 0) "+" else "−"
                            val firstMonth = series.first().first.format(
                                java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.getDefault()),
                            )
                            Box(modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(sw.successSoft)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center) {
                                Text(
                                    "$sign${"%.1f".format(kotlin.math.abs(deltaPct))}% sejak $firstMonth",
                                    color = sw.success,
                                    style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"),
                                )
                            }
                        }
                    }
                }
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
                    // Slice palette per screens-dashboard.jsx:220-225 — Akun=primary,
                    // Emas=warning, Properti=info, Deposito=accent.
                    if (nw.accountsTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.accountsTotal.toFloat() / totalPos)
                        .background(sw.primary))
                    if (nw.goldTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.goldTotal.toFloat() / totalPos)
                        .background(sw.warning))
                    if (nw.landTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.landTotal.toFloat() / totalPos)
                        .background(sw.info))
                    if (nw.depositTotal > 0) Box(Modifier.fillMaxHeight()
                        .fillMaxWidth(nw.depositTotal.toFloat() / totalPos)
                        .background(sw.accent))
                }
                Spacer(Modifier.height(10.dp))
                // Legend row — explains each slice color from the bar above.
                // Simple Row + horizontal scroll keeps it inside the card on
                // narrow screens without pulling in experimental FlowRow.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    LegendDot(sw.primary, stringResource(R.string.assets_class_accounts))
                    LegendDot(sw.warning, stringResource(R.string.assets_class_gold))
                    LegendDot(sw.info, stringResource(R.string.assets_class_land))
                    LegendDot(sw.accent, stringResource(R.string.assets_class_deposit))
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
    SwCard(modifier = modifier, padding = PaddingValues(0.dp), onClick = onClick) {
        Box {
            // Watermark icon in the bottom-right corner of each asset class
            // card — per prototype screens-assets.jsx. Subtle, tinted, oversized.
            Icon(
                icon,
                contentDescription = null,
                tint = tint.copy(alpha = 0.10f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 14.dp, y = 14.dp)
                    .size(96.dp),
            )
            Column(modifier = Modifier.padding(14.dp)) {
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
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label,
            color = sw.onPrimaryHero.copy(alpha = 0.85f),
            style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium))
    }
}
