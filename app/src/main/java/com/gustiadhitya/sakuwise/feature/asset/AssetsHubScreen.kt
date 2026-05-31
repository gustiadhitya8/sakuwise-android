package com.gustiadhitya.sakuwise.feature.asset

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
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

/**
 * Light spring-green accent reserved for the Akun (accounts) class on the
 * Aset hero breakdown. Stays in the brand green family but is several stops
 * lighter than the deep-forest hero (primaryHero / PrimaryContainerDark),
 * so it pops with high contrast in both light and dark mode. It's also
 * clearly distinguishable from the Deposito mint (sw.accent) which is a
 * more pastel mint — these two greens read as different hues, not
 * "two shades of the same green."
 */
private val AccountsAccent = Color(0xFF86EFAC)

@OptIn(ExperimentalLayoutApi::class)
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
            // Eye toggle is now persisted via DataStore (balancesHidden) so
            // the masked state survives app restarts and stays in sync with
            // the dashboard hero's toggle.
            val hideTotal = state.balancesHidden
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.assets_total_wealth),
                        color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                        modifier = Modifier.weight(1f))
                    androidx.compose.foundation.layout.Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f))
                            .clickable { viewModel.toggleBalancesHidden() },
                    ) {
                        Icon(
                            if (hideTotal) androidx.compose.material.icons.Icons.Outlined.VisibilityOff
                            else androidx.compose.material.icons.Icons.Outlined.Visibility,
                            contentDescription = if (hideTotal)
                                stringResource(R.string.assets_total_show)
                            else stringResource(R.string.assets_total_hide),
                            tint = sw.onPrimaryHero,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Total — alone on its line. Growth pill goes BELOW per proto.
                if (hideTotal) {
                    Text("Rp ••••••",
                        color = sw.onPrimaryHero,
                        style = SwType.AmountXL)
                } else {
                    RupiahText(value = nw.total, color = sw.onPrimaryHero, style = SwType.AmountXL)
                }
                // Growth pill — per screens-assets.jsx, sits BELOW the total
                // with marginTop:8 + bg = rgba(255,255,255,0.16). Inline-flex.
                // Always renders so the layout matches the prototype; falls
                // back to a zero-growth "since today" pill when the snapshot
                // table doesn't yet have >= 2 distinct days of data.
                Spacer(Modifier.height(8.dp))
                val series = state.netWorthTrend
                val first = series.firstOrNull()?.second ?: 0L
                val last = series.lastOrNull()?.second ?: 0L
                val deltaPct =
                    if (first != 0L && series.size >= 2) (last - first) * 100.0 / first
                    else 0.0
                val sign = if (deltaPct >= 0) "+" else "−"
                val sinceLabel = if (series.isNotEmpty()) {
                    // Use the oldest real monthly point as the "since" reference.
                    // Even with only 1 month of data, this is more informative
                    // than a plain "since today" label.
                    series.first().first.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMM yy", java.util.Locale.getDefault()),
                    )
                } else {
                    stringResource(R.string.assets_hub_since_today)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.TrendingUp, null,
                        tint = sw.onPrimaryHero,
                        modifier = Modifier.size(11.dp),
                    )
                    Text(
                        stringResource(R.string.assets_hub_since_month_format, sign,
                            "%.1f".format(kotlin.math.abs(deltaPct)), sinceLabel),
                        color = sw.onPrimaryHero,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                    )
                }
                Spacer(Modifier.height(16.dp))
                val totalPos = (nw.accountsTotal + nw.goldTotal + nw.landTotal + nw.depositTotal)
                    .coerceAtLeast(1L)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                ) {
                    // Each segment uses weight() so fractions are relative to
                    // the full row width, giving a true 100% stacked bar.
                    val akunColor = AccountsAccent
                    if (nw.accountsTotal > 0) Box(Modifier.weight(nw.accountsTotal.toFloat()).fillMaxHeight().background(akunColor))
                    if (nw.goldTotal > 0) Box(Modifier.weight(nw.goldTotal.toFloat()).fillMaxHeight().background(sw.warning))
                    if (nw.landTotal > 0) Box(Modifier.weight(nw.landTotal.toFloat()).fillMaxHeight().background(sw.info))
                    if (nw.depositTotal > 0) Box(Modifier.weight(nw.depositTotal.toFloat()).fillMaxHeight().background(sw.accent))
                    // Remaining track when all totals are 0
                    if (totalPos == 1L) Box(Modifier.weight(1f).fillMaxHeight())
                }
                Spacer(Modifier.height(6.dp))
                // Legend — proto uses flex-wrap so 4 items reflow onto a
                // second line when the device is too narrow (e.g. Galaxy S22)
                // instead of clipping the last entry off the hero card.
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LegendDot(AccountsAccent, stringResource(R.string.assets_class_accounts))
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
        val totalAssets = (nw.accountsTotal + nw.goldTotal + nw.landTotal + nw.depositTotal)
            .coerceAtLeast(1L)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AssetClassCard(stringResource(R.string.assets_class_accounts),
                stringResource(R.string.assets_class_active_format, state.accounts.size),
                state.accountsTotal, sw.primary, R.drawable.ic_asset_wallet,
                Modifier.weight(1f),
                contributionPct = nw.accountsTotal.toFloat() / totalAssets * 100f,
                balancesHidden = state.balancesHidden,
                onClick = onNavigateToAccounts)
            val goldBuy = state.gold.sumOf { it.buyPrice }
            val goldGrowth = if (goldBuy > 0L)
                ((nw.goldTotal - goldBuy).toFloat() / goldBuy.toFloat()) * 100f
            else null
            AssetClassCard(stringResource(R.string.assets_class_gold),
                stringResource(R.string.assets_class_count_format, state.gold.size),
                nw.goldTotal, sw.warning, R.drawable.ic_asset_gold,
                Modifier.weight(1f),
                growthPct = goldGrowth,
                contributionPct = nw.goldTotal.toFloat() / totalAssets * 100f,
                balancesHidden = state.balancesHidden,
                onClick = onNavigateToGold)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val landBuy = state.land.sumOf { it.buyPrice }
            val landGrowth = if (landBuy > 0L)
                ((nw.landTotal - landBuy).toFloat() / landBuy.toFloat()) * 100f
            else null
            AssetClassCard(stringResource(R.string.assets_class_land),
                stringResource(R.string.assets_class_count_format, state.land.size),
                nw.landTotal, sw.info, R.drawable.ic_asset_land,
                Modifier.weight(1f),
                growthPct = landGrowth,
                contributionPct = nw.landTotal.toFloat() / totalAssets * 100f,
                balancesHidden = state.balancesHidden,
                onClick = onNavigateToLand)
            AssetClassCard(stringResource(R.string.assets_class_deposit),
                stringResource(R.string.assets_class_count_format, state.deposits.size),
                nw.depositTotal, sw.accent, R.drawable.ic_asset_deposit,
                Modifier.weight(1f),
                growthPct = null,
                contributionPct = nw.depositTotal.toFloat() / totalAssets * 100f,
                balancesHidden = state.balancesHidden,
                onClick = onNavigateToDeposit)
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
    val today = remember { java.time.LocalDate.now() }
    val cutoff: java.time.LocalDate? = when (period) {
        "3M" -> today.minusMonths(3)
        "6M" -> today.minusMonths(6)
        "1Y" -> today.minusYears(1)
        else -> null
    }
    // Data is now monthly (one value per calendar month, real snapshots only).
    // Filter to the selected period window.
    val filtered = if (cutoff != null) seriesAll.filter { !it.first.isBefore(cutoff) } else seriesAll
    val window = filtered.map { it.second }
    val windowDates = filtered.map { it.first }
    // Delta: change from oldest to newest data point in the window.
    // Because we use ONLY real snapshot data, this comparison is always valid.
    val delta = if (window.size >= 2) window.last() - window.first() else 0L
    val deltaPct = if (window.size >= 2 && window.first() != 0L)
        (delta * 100.0 / window.first()) else 0.0

    // Empty state: fewer than 2 real monthly data points available.
    // A meaningful line chart needs at least 2 months of real snapshots.
    if (seriesAll.size < 2) {
        Column(
            modifier = Modifier
                .padding(horizontal = SwSpace.pageH)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(18.dp))
                .padding(18.dp),
        ) {
            Text(stringResource(R.string.assets_trend_label), color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp))
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.TrendingUp, null,
                        tint = sw.inkSubtle, modifier = Modifier.size(28.dp))
                    Text(
                        stringResource(R.string.assets_chart_empty),
                        color = sw.inkSubtle,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    if (seriesAll.isNotEmpty()) {
                        val fmtStart = java.time.format.DateTimeFormatter.ofPattern(
                            "MMMM yyyy", java.util.Locale.getDefault())
                        Text(
                            stringResource(R.string.assets_chart_data_since_format, seriesAll.first().first.format(fmtStart)),
                            color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                }
            }
        }
        return
    }

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
            if (window.size >= 2) {
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
        // X-axis labels (MMM yy). Series is always monthly, so labels are
        // spread across calendar months.  Show first / date-range midpoint /
        // last so the three labels are always in different months.
        if (windowDates.size >= 2) {
            val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM yy", java.util.Locale.getDefault())
            val firstDate = windowDates.first()
            val lastDate  = windowDates.last()
            val midDate   = firstDate.plusDays(
                java.time.temporal.ChronoUnit.DAYS.between(firstDate, lastDate) / 2
            )
            val labelDates = when {
                windowDates.size <= 3 -> windowDates
                else -> listOf(firstDate, midDate, lastDate)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                labelDates.forEach { d ->
                    Text(d.format(fmt),
                        color = sw.inkSubtle,
                        style = SwType.LabelSmall.copy(fontSize = 10.sp, fontFeatureSettings = "tnum"))
                }
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
    @androidx.annotation.DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    growthPct: Float? = null,
    contributionPct: Float? = null,
    balancesHidden: Boolean = false,
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
            ) { Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(title, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
            Text(sub, color = sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                if (balancesHidden) {
                    Text("Rp ••••••",
                        color = sw.ink,
                        style = SwType.Amount.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                } else {
                    RupiahText(value = value, short = true,
                        style = SwType.Amount.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = sw.ink)
                }
                if (growthPct != null && growthPct != 0f) {
                    Spacer(Modifier.weight(1f))
                    val pos = growthPct >= 0f
                    Text(
                        (if (pos) "+" else "−") +
                            "%.1f".format(kotlin.math.abs(growthPct)) + "%",
                        color = if (pos) sw.success else sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"),
                    )
                }
            }
            if (contributionPct != null) {
                Spacer(Modifier.height(8.dp))
                val fraction = (contributionPct / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(tint.copy(alpha = 0.15f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(tint),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.assets_pct_of_total_format, contributionPct),
                    color = tint,
                    style = SwType.LabelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                )
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
