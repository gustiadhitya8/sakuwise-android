package com.gustiadhitya.sakuwise.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toRelativeOrAbsolute
import com.gustiadhitya.sakuwise.feature.plan.displayName as allocDisplayName
import com.gustiadhitya.sakuwise.feature.transaction.ui.displayName as accountTypeDisplayName
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCategoryDot
import com.gustiadhitya.sakuwise.core.designsystem.components.SwSectionLabel
import com.gustiadhitya.sakuwise.core.designsystem.icons.Lockup
import com.gustiadhitya.sakuwise.core.designsystem.icons.LogoDaun
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.PlanPeriod
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.ui.RupiahSign
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.dashboard.viewmodel.DashboardUiState
import com.gustiadhitya.sakuwise.feature.dashboard.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DashboardScreen(
    onNavigateToPlan: () -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToMe: () -> Unit = {},
    onBackupTap: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    DashboardContent(
        state = state,
        onNavigateToPlan = onNavigateToPlan,
        onNavigateToAssets = onNavigateToAssets,
        onNavigateToMe = onNavigateToMe,
        onBackupTap = onBackupTap,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onNavigateToPlan: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToMe: () -> Unit,
    onBackupTap: () -> Unit,
) {
    val sw = SwTheme.colors
    var hide by remember { mutableStateOf(false) }
    val nickname = state.nickname.ifBlank { "Teman" }
    val initial = (nickname.firstOrNull()?.uppercase() ?: "S")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = SwSpace.bottomBarClear),
    ) {
        DashboardHeader(initial = initial, onAvatarClick = onNavigateToMe)
        DashboardGreeting(name = nickname, period = state.period)
        Spacer(Modifier.height(8.dp))
        DashboardHero(
            income = state.incomeMonth,
            expense = state.expenseMonth,
            period = state.period,
            hide = hide,
            onToggleHide = { hide = !hide },
        )
        DashboardAlloc(
            allocations = state.allocations,
            onTap = onNavigateToPlan,
        )
        DashboardAssetsLink(
            accountsCount = state.accounts.size,
            accountsTotal = state.accountsTotal,
            hide = hide,
            onTap = onNavigateToAssets,
        )
        if (state.topCategories.isNotEmpty()) {
            DashboardTopCategories(items = state.topCategories)
        }
        DashboardRecentTxns(
            txns = state.recentTransactions,
            accountNameLookup = { id -> state.accounts.firstOrNull { it.id == id }?.name },
            onTapAll = onNavigateToPlan,
        )
        if (state.backupOverdueDays > 30 || state.backupOverdueDays == Int.MAX_VALUE) {
            DashboardBanner(overdueDays = state.backupOverdueDays, onTap = onBackupTap)
        }
    }
}

@Composable
private fun DashboardHeader(initial: String, onAvatarClick: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SwSpace.pageH, end = SwSpace.pageH, top = 8.dp, bottom = 12.dp),
    ) {
        Lockup(sizeSp = 22)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(sw.primaryContainer)
                .clickable(onClick = onAvatarClick),
        ) {
            Text(
                initial,
                color = sw.onPrimaryContainer,
                fontSize = 14.sp, lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DashboardGreeting(name: String, period: PlanPeriod?) {
    val sw = SwTheme.colors
    val hour = LocalTime.now().hour
    val greet = stringResource(when {
        hour < 11 -> R.string.dashboard_greeting_morning
        hour < 15 -> R.string.dashboard_greeting_noon
        hour < 19 -> R.string.dashboard_greeting_afternoon
        else -> R.string.dashboard_greeting_evening
    })
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SwSpace.pageH)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.weight(1f)) {
                Text("$greet,", color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium))
                Text(name, color = sw.ink,
                    style = SwType.H1.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(sw.surface)
                    .border(1.dp, sw.border, RoundedCornerShape(14.dp))
                    .clickable {},
            ) {
                Icon(Icons.Outlined.NotificationsNone,
                    stringResource(R.string.dashboard_notifications),
                    tint = sw.ink, modifier = Modifier.size(20.dp))
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(sw.danger)
                        .border(2.dp, sw.surface, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(sw.primaryContainer)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Outlined.CalendarToday, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(14.dp))
            val label = period?.let {
                stringResource(R.string.dashboard_period_format, it.label, it.daysLeft)
            } ?: stringResource(R.string.dashboard_no_plan)
            Text(label, color = sw.onPrimaryContainer,
                style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun DashboardHero(
    income: Long,
    expense: Long,
    period: PlanPeriod?,
    hide: Boolean,
    onToggleHide: () -> Unit,
) {
    val sw = SwTheme.colors
    val remaining = income - expense
    val daysLeft = period?.daysLeft?.coerceAtLeast(1) ?: 1
    val dailyLeft = remaining / daysLeft

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SwSpace.pageH, vertical = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(sw.primary),
    ) {
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 36.dp, y = 36.dp)) {
            LogoDaun(
                sizeDp = 180,
                bg = sw.onPrimary.copy(alpha = 0.10f),
                leaf = sw.primary.copy(alpha = 0.10f),
                vein = sw.onPrimary.copy(alpha = 0.10f),
            )
        }
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "SISA ANGGARAN",
                        color = sw.onPrimary.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    )
                    Spacer(Modifier.height(2.dp))
                    if (hide) {
                        Text("••••••••", color = sw.onPrimary.copy(alpha = 0.85f), style = SwType.AmountXL)
                    } else {
                        RupiahText(value = remaining, color = sw.onPrimary, style = SwType.AmountXL)
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(onClick = onToggleHide),
                ) {
                    Icon(
                        if (hide) Icons.Outlined.VisibilityOff else Icons.Outlined.RemoveRedEye,
                        contentDescription = if (hide) "Tampilkan saldo" else "Sembunyikan saldo",
                        tint = sw.onPrimary, modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                ) { Icon(Icons.Outlined.AutoAwesome, null, tint = sw.accent, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Anggaran harian", color = sw.onPrimary.copy(alpha = 0.78f),
                        style = SwType.Caption.copy(fontSize = 12.sp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (hide) "•••••" else dailyLeft.toRupiah(),
                            color = sw.onPrimary,
                            style = SwType.AmountL.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        )
                        Text(" / hari", color = sw.onPrimary.copy(alpha = 0.6f),
                            style = SwType.Body.copy(fontSize = 14.sp))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                HeroMetric("Pemasukan", income, "+", sw.accent, hide, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.18f)))
                HeroMetric("Pengeluaran", expense, "−", sw.onPrimary, hide, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: Long, sign: String, tint: Color, hide: Boolean, modifier: Modifier = Modifier) {
    val sw = SwTheme.colors
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(label, color = sw.onPrimary.copy(alpha = 0.7f),
            style = SwType.Caption.copy(fontSize = 11.sp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(sign, color = tint.copy(alpha = 0.7f),
                style = SwType.Amount.copy(fontSize = 17.sp))
            Spacer(Modifier.width(2.dp))
            Text(
                if (hide) "•••" else value.toRupiahShort(),
                color = tint,
                style = SwType.Amount.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun DashboardAlloc(
    allocations: List<com.gustiadhitya.sakuwise.feature.dashboard.viewmodel.AllocationProgress>,
    onTap: () -> Unit,
) {
    val sw = SwTheme.colors
    if (allocations.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH).padding(bottom = 14.dp)) {
        com.gustiadhitya.sakuwise.core.designsystem.components.SwSectionLabel(
            text = "Alokasi",
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onTap)) {
                    Text("Detail", color = sw.primary,
                        style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                    Icon(Icons.Outlined.ChevronRight, null, tint = sw.primary, modifier = Modifier.size(14.dp))
                }
            },
        )
        SwCard {
            Column {
                allocations.forEachIndexed { i, row ->
                    if (i > 0) Spacer(Modifier.height(14.dp))
                    val a = row.allocation
                    val allocId = com.gustiadhitya.sakuwise.core.domain.model.AllocationId.fromName(a.name)
                    val allocColor = when (allocId) {
                        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Needs -> sw.primary
                        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Wants -> sw.accent
                        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Invest -> sw.info
                    }
                    val allocLabel = allocId.allocDisplayName()
                    val over = row.used > row.plan
                    val pct = if (row.plan > 0) (row.used.toFloat() / row.plan.toFloat()) * 100 else 0f
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(allocColor))
                            Text(allocLabel, color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                            Text("${a.targetPct}%", color = sw.inkSubtle,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"))
                        }
                        RupiahText(value = row.used, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                            color = if (over) sw.danger else sw.ink)
                        Text(" / ", color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        RupiahText(value = row.plan, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp, fontFeatureSettings = "tnum"),
                            color = sw.inkSubtle)
                    }
                    Spacer(Modifier.height(6.dp))
                    com.gustiadhitya.sakuwise.core.designsystem.components.SwBar(
                        used = row.used, plan = row.plan.coerceAtLeast(1L), color = allocColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${pct.toInt()}% terpakai", color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 10.sp, fontFeatureSettings = "tnum"))
                        if (over) {
                            Text("Over ${(row.used - row.plan).toRupiahShort()}",
                                color = sw.danger,
                                style = SwType.LabelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardAssetsLink(
    accountsCount: Int,
    accountsTotal: Long,
    hide: Boolean,
    onTap: () -> Unit,
) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH).padding(bottom = 14.dp)) {
        SwCard(padding = PaddingValues(0.dp), onClick = onTap) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.primaryContainer),
                ) {
                    Icon(
                        Icons.Outlined.AccountBalanceWallet, null,
                        tint = sw.onPrimaryContainer, modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.size(width = 14.dp, height = 1.dp))
                Column(Modifier.weight(1f)) {
                    Text("Aset & Kekayaan", color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    Text(
                        "$accountsCount akun aktif" +
                            (if (hide) "" else " · ${accountsTotal.toRupiahShort()} di akun") +
                            " · detail di tab Aset",
                        color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp),
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = sw.inkSubtle, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DashboardAccountsStrip(
    accountsWithBalance: List<com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountWithBalance>,
    hide: Boolean,
    total: Long,
    onTap: () -> Unit,
) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SwSpace.pageH)
                .padding(top = 4.dp, bottom = 4.dp),
        ) {
            Text("AKUN", color = sw.inkSubtle, style = SwType.SectionLabel)
            if (!hide) {
                Text(total.toRupiahShort(), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = SwSpace.pageH),
        ) {
            items(accountsWithBalance) { ab ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(sw.surface)
                        .border(1.dp, sw.border, RoundedCornerShape(16.dp))
                        .clickable(onClick = onTap)
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(sw.primaryContainer),
                        ) {
                            Icon(
                                Icons.Outlined.AccountBalanceWallet, null,
                                tint = sw.onPrimaryContainer, modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            ab.account.type.accountTypeDisplayName().uppercase(),
                            color = sw.inkSubtle,
                            style = SwType.SectionLabel.copy(fontSize = 9.sp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(ab.account.name, color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (hide) {
                        Text("•••••", color = sw.primary,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
                    } else {
                        RupiahText(value = ab.balance, short = true,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                            color = sw.primary)
                    }
                }
            }
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 60.dp, height = 90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, sw.borderStrong, RoundedCornerShape(16.dp))
                        .clickable(onClick = onTap),
                ) {
                    Icon(
                        Icons.Outlined.Add, "Tambah akun",
                        tint = sw.inkMuted, modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardRecentTxns(
    txns: List<Transaction>,
    accountNameLookup: (String) -> String?,
    onTapAll: () -> Unit,
) {
    val sw = SwTheme.colors
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH).padding(bottom = 14.dp)) {
        SwSectionLabel(
            text = "Transaksi Terbaru",
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onTapAll)) {
                    Text("Semua", color = sw.primary,
                        style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                    Icon(Icons.Outlined.ChevronRight, null, tint = sw.primary, modifier = Modifier.size(14.dp))
                }
            },
        )
        if (txns.isEmpty()) {
            SwCard {
                Text(
                    "Belum ada transaksi. Tap tombol + untuk catat pengeluaran pertamamu.",
                    color = sw.inkMuted,
                    style = SwType.Body,
                )
            }
        } else {
            SwCard(padding = PaddingValues(0.dp)) {
                Column {
                    txns.forEachIndexed { i, t ->
                        val divider = i < txns.size - 1
                        val tone = when (t.type) {
                            TxnType.Income -> sw.success
                            TxnType.Transfer -> sw.info
                            else -> sw.ink
                        }
                        val sign = when (t.type) {
                            TxnType.Income -> RupiahSign.Positive
                            TxnType.Expense, TxnType.DebtOutflow -> RupiahSign.Negative
                            else -> RupiahSign.None
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            SwCategoryDot(
                                name = t.note ?: t.type.code(),
                                sizeDp = 38,
                                color = if (tone == sw.ink) null else tone,
                            )
                            Spacer(Modifier.width(12.dp))
                            val fallbackLabel = stringResource(when (t.type) {
                                TxnType.Income -> R.string.txntype_income
                                TxnType.Expense -> R.string.txntype_expense
                                TxnType.Transfer -> R.string.txntype_transfer
                                TxnType.DebtInflow -> R.string.txntype_debt_inflow
                                TxnType.DebtOutflow -> R.string.txntype_debt_outflow
                                TxnType.Reconciliation -> R.string.txntype_reconciliation
                            })
                            Column(Modifier.weight(1f)) {
                                Text(t.note ?: fallbackLabel,
                                    color = sw.ink,
                                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(t.date.toRelativeOrAbsolute(), color = sw.inkSubtle,
                                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                                    Box(Modifier.size(2.dp).clip(CircleShape).background(sw.inkSubtle))
                                    Text(accountNameLookup(t.sourceAccountId) ?: "—",
                                        color = sw.inkSubtle,
                                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                                }
                            }
                            RupiahText(
                                value = t.amount,
                                sign = sign,
                                short = true,
                                style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                color = tone,
                            )
                        }
                        if (divider) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardBanner(overdueDays: Int, onTap: () -> Unit) {
    val sw = SwTheme.colors
    val label = if (overdueDays == Int.MAX_VALUE) "Belum pernah backup"
                else "Backup tertunda $overdueDays hari"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SwSpace.pageH, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(sw.warningSoft)
            .border(1.dp, sw.warning.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(sw.warning),
        ) { Icon(Icons.Outlined.Shield, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
            Text("Amankan data uangmu — backup sekarang.",
                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(sw.warning)
                .clickable(onClick = onTap)
                .padding(horizontal = 12.dp),
        ) {
            Text("Backup", color = Color.White,
                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun DashboardTopCategories(
    items: List<com.gustiadhitya.sakuwise.feature.dashboard.viewmodel.TopCategorySpend>,
) {
    val sw = SwTheme.colors
    val maxAmount = items.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.dashboard_top_expenses), color = sw.ink,
                style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Text(stringResource(R.string.dashboard_active_period), color = sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
        Spacer(Modifier.height(8.dp))
        SwCard(padding = PaddingValues(14.dp)) {
            Column {
                items.forEachIndexed { i, item ->
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}.", color = sw.inkSubtle,
                            style = SwType.LabelStrong.copy(fontSize = 12.sp),
                            modifier = Modifier.width(20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                            Spacer(Modifier.height(4.dp))
                            // mini bar — proportional to max
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(sw.border),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(
                                            (item.amount.toFloat() / maxAmount.toFloat())
                                                .coerceIn(0f, 1f),
                                        )
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(sw.danger),
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        RupiahText(value = item.amount, short = true,
                            style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = sw.danger)
                    }
                }
            }
        }
    }
}
