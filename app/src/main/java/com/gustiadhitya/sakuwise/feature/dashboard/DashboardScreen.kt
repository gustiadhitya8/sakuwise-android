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
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
    // Default routes the Backup banner CTA to the Me tab. Callers can override
    // to deep-link directly into Backup settings (preferred).
    onBackupTap: () -> Unit = onNavigateToMe,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    DashboardContent(
        state = state,
        onNavigateToPlan = onNavigateToPlan,
        onNavigateToAssets = onNavigateToAssets,
        onNavigateToMe = onNavigateToMe,
        onBackupTap = onBackupTap,
        onMarkNotificationsSeen = viewModel::markNotificationsSeen,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onNavigateToPlan: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToMe: () -> Unit,
    onBackupTap: () -> Unit,
    onMarkNotificationsSeen: () -> Unit = {},
) {
    val sw = SwTheme.colors
    var hide by remember { mutableStateOf(false) }
    val nickname = state.nickname.ifBlank { "Teman" }
    val initial = (nickname.firstOrNull()?.uppercase() ?: "S")

    // PRD §7.13 — overspending banner fires when ANY allocation has used > plan.
    // We compute it here so DashboardAlloc + the dedicated banner stay in sync.
    val overspent = state.allocations.any { it.used > it.plan && it.plan > 0L }

    // PRD §7.12 — backup blocker modal at ≥60 days. Session-only dismiss: the
    // user taps "Nanti saja" → modal closes for this app session, reappears on
    // cold launch. Cold launch resets `dismissedBackup60Modal` to false.
    // CRITICAL: Int.MAX_VALUE means "never backed up" — i.e. fresh install. The
    // user might be 5 minutes old; popping a "Backup expired" blocker modal is
    // wrong. Only fire on a REAL elapsed-day count >= 60. The dashboard banner
    // (the lighter notice) handles the never-backed-up case below at >30.
    var dismissedBackup60Modal by remember { mutableStateOf(false) }
    val showBackup60Modal = !dismissedBackup60Modal &&
        state.backupOverdueDays >= 60 &&
        state.backupOverdueDays != Int.MAX_VALUE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = SwSpace.bottomBarClear),
    ) {
        // Bell badge lights up when there's at least one notification whose
        // "source timestamp" is newer than the last time the user opened the
        // notification center. Backup-overdue uses lastBackupTimestamp = 0
        // (never) or the actual timestamp; either way, comparing to
        // notificationsLastSeenAt collapses to "user hasn't seen this state."
        val items = com.gustiadhitya.sakuwise.feature.notification
            .rememberDefaultNotifications(
                backupOverdueDays = state.backupOverdueDays,
                onOpenBackup = onBackupTap,
            )
        // Unread = there's a notification AND we haven't acknowledged it yet.
        // For the backup signal the "event time" is functionally "now" until
        // the user backs up, so the badge persists until they tap the bell.
        val unread = items.isNotEmpty() && state.notificationsLastSeenAt < System.currentTimeMillis() - 60_000L
        var showNotifSheet by remember { mutableStateOf(false) }
        DashboardHeader(
            initial = initial,
            onAvatarClick = onNavigateToMe,
            notificationCount = if (unread) items.size else 0,
            onBellClick = { showNotifSheet = true },
        )
        if (showNotifSheet) {
            com.gustiadhitya.sakuwise.feature.notification.NotificationCenterSheet(
                items = items,
                onDismiss = { showNotifSheet = false },
                onMarkAllRead = onMarkNotificationsSeen,
            )
        }
        DashboardGreeting(name = nickname, period = state.period,
            onPeriodTap = onNavigateToPlan)
        Spacer(Modifier.height(8.dp))
        DashboardHero(
            income = state.incomeMonth,
            expense = state.expenseMonth,
            period = state.period,
            hide = hide,
            onToggleHide = { hide = !hide },
        )
        if (overspent) {
            DashboardOverspendBanner(onTap = onNavigateToPlan)
        }
        DashboardAlloc(
            allocations = state.allocations,
            onTap = onNavigateToPlan,
        )
        // Order per screens-dashboard.jsx:384-390 — top spend BEFORE assets link.
        if (state.topCategories.isNotEmpty()) {
            DashboardTopCategories(items = state.topCategories)
        }
        DashboardAssetsLink(
            accountsCount = state.accounts.size,
            accountsTotal = state.accountsTotal,
            hide = hide,
            onTap = onNavigateToAssets,
        )
        DashboardRecentTxns(
            txns = state.recentTransactions,
            accountNameLookup = { id -> state.accounts.firstOrNull { it.id == id }?.name },
            onTapAll = onNavigateToPlan,
        )
        if (state.backupOverdueDays > 30 || state.backupOverdueDays == Int.MAX_VALUE) {
            DashboardBanner(overdueDays = state.backupOverdueDays, onTap = onBackupTap)
        }
    }

    if (showBackup60Modal) {
        BackupOverdueModal(
            overdueDays = state.backupOverdueDays,
            onDismissForSession = { dismissedBackup60Modal = true },
            onBackup = {
                dismissedBackup60Modal = true
                onBackupTap()
            },
        )
    }
}

/**
 * PRD §7.12 — modal blocker for backups ≥ 60 days overdue.
 *
 * Two routes out of the modal:
 *  • "Nanti saja" → session-only dismiss (re-fires on cold launch).
 *  • "Backup Sekarang" → dismiss + invoke [onBackup] → routes user into the
 *    Backup settings flow.
 *
 * The modal is intentionally non-blocking for the PIN unlock — it renders
 * inside DashboardScreen which only mounts AFTER unlock.
 */
@Composable
private fun BackupOverdueModal(
    overdueDays: Int,
    onDismissForSession: () -> Unit,
    onBackup: () -> Unit,
) {
    val sw = SwTheme.colors
    val daysLabel = if (overdueDays == Int.MAX_VALUE) "—" else overdueDays.toString()
    AlertDialog(
        onDismissRequest = onDismissForSession,
        icon = {
            Icon(Icons.Outlined.Shield, null, tint = sw.warning, modifier = Modifier.size(28.dp))
        },
        title = {
            Text(
                stringResource(R.string.dashboard_backup60_title),
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Text(
                stringResource(R.string.dashboard_backup60_body_format, daysLabel),
                color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        },
        confirmButton = {
            TextButton(onClick = onBackup) {
                Text(stringResource(R.string.dashboard_backup60_cta),
                    color = sw.primary,
                    style = SwType.LabelStrong.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissForSession) {
                Text(stringResource(R.string.dashboard_backup60_later),
                    color = sw.inkMuted)
            }
        },
        containerColor = sw.surface,
    )
}

/**
 * PRD §7.13 — overspending banner. Renders above the alloc bars when any
 * allocation's actual spend has crossed its planned amount. Tap → Plan tab.
 */
@Composable
private fun DashboardOverspendBanner(onTap: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SwSpace.pageH, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(sw.dangerSoft)
            .border(1.dp, sw.danger.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(sw.danger),
        ) {
            Icon(Icons.Outlined.WarningAmber, null,
                tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.dashboard_overspend_title),
                color = sw.ink,
                style = SwType.LabelStrong.copy(
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                stringResource(R.string.dashboard_overspend_body),
                color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp),
            )
        }
        Icon(Icons.Outlined.ChevronRight, null,
            tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DashboardHeader(
    initial: String,
    onAvatarClick: () -> Unit,
    notificationCount: Int = 0,
    onBellClick: () -> Unit = onAvatarClick,
) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SwSpace.pageH, end = SwSpace.pageH, top = 8.dp, bottom = 12.dp),
    ) {
        Lockup(sizeSp = 22)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Notification bell — proto 06-dashboard.png has it between brand
            // and avatar with a tiny red dot when there's something to flag.
            // Tap currently routes to the same target as the avatar (Saya
            // tab) since a dedicated notification center is V1.1+.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(sw.surface)
                    .border(1.dp, sw.border, CircleShape)
                    .clickable(onClick = onBellClick),
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Outlined.NotificationsNone,
                    contentDescription = stringResource(R.string.dashboard_notifications),
                    tint = sw.ink, modifier = Modifier.size(20.dp),
                )
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(sw.danger),
                    )
                }
            }
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
}

@Composable
private fun DashboardGreeting(name: String, period: PlanPeriod?, onPeriodTap: () -> Unit = {}) {
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
            // Notifications surface deferred to V1.2 — until then we don't
            // render the bell at all (was dead-click + lying unread dot).
            // Restore when notification history is shipped.
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(sw.primaryContainer)
                .clickable(onClick = onPeriodTap) // tap → Plan tab → user picks/creates other months
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
            // primaryHero stays deep green in BOTH light and dark mode so the
            // hero doesn't look like a light card pasted onto the dark page.
            .background(sw.primaryHero),
    ) {
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 36.dp, y = 36.dp)) {
            LogoDaun(
                sizeDp = 180,
                bg = sw.onPrimaryHero.copy(alpha = 0.10f),
                leaf = sw.primaryHero.copy(alpha = 0.10f),
                vein = sw.onPrimaryHero.copy(alpha = 0.10f),
            )
        }
        Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.dashboard_remaining_budget),
                        color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    )
                    Spacer(Modifier.height(2.dp))
                    if (hide) {
                        Text("••••••••", color = sw.onPrimaryHero.copy(alpha = 0.85f), style = SwType.AmountXL)
                    } else {
                        RupiahText(value = remaining, color = sw.onPrimaryHero, style = SwType.AmountXL)
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
                        tint = sw.onPrimaryHero, modifier = Modifier.size(18.dp),
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
                ) { Icon(Icons.Outlined.AutoAwesome, null, tint = sw.onPrimaryHero, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_daily_budget), color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.Caption.copy(fontSize = 12.sp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (hide) "•••••" else dailyLeft.toRupiah(),
                            color = sw.onPrimaryHero,
                            style = SwType.AmountL.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        )
                        Text(" " + stringResource(R.string.dashboard_per_day), color = sw.onPrimaryHero.copy(alpha = 0.6f),
                            style = SwType.Body.copy(fontSize = 14.sp))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)),
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                // Per prototype screens-dashboard.jsx:87-89:
                //   Pemasukan tint = c.accent (sand), Pengeluaran tint = c.onPrimary (white)
                // Both render as 17sp bold tabular-nums with the sign at opacity 0.7.
                HeroMetric(stringResource(R.string.dashboard_metric_income), income, "+", sw.accent, hide, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.18f)))
                HeroMetric(stringResource(R.string.dashboard_metric_expense), expense, "−", sw.onPrimaryHero, hide, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: Long, sign: String, tint: Color, hide: Boolean, modifier: Modifier = Modifier) {
    val sw = SwTheme.colors
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        // Per proto screens-dashboard.jsx:97 — 11sp, opacity 0.7, fontWeight 500.
        Text(label, color = sw.onPrimaryHero.copy(alpha = 0.7f),
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium))
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(sign, color = tint.copy(alpha = 0.7f),
                style = SwType.Amount.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"))
            Spacer(Modifier.width(2.dp))
            Text(
                if (hide) "•••" else value.toRupiahShort(),
                color = tint,
                style = SwType.Amount.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"),
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
                    Text(stringResource(R.string.dashboard_assets_card_title), color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    Text(
                        if (hide) stringResource(R.string.dashboard_assets_card_sub_hidden_format, accountsCount)
                        else stringResource(R.string.dashboard_assets_card_sub_format, accountsCount, accountsTotal.toRupiahShort()),
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
                        Icons.Outlined.Add, stringResource(R.string.dashboard_add_account_cd),
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
            text = stringResource(R.string.dashboard_recent_txns),
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onTapAll)) {
                    Text(stringResource(R.string.dashboard_action_all), color = sw.primary,
                        style = SwType.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                    Icon(Icons.Outlined.ChevronRight, null, tint = sw.primary, modifier = Modifier.size(14.dp))
                }
            },
        )
        if (txns.isEmpty()) {
            SwCard {
                Text(
                    stringResource(R.string.dashboard_empty_txns),
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
    val label = if (overdueDays == Int.MAX_VALUE) stringResource(R.string.dashboard_backup_never_label)
                else stringResource(R.string.dashboard_backup_overdue_days_format, overdueDays)
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
            // 13sp per proto screens-dashboard.jsx:332 (was 16sp, made banner
            // taller than proto and unbalanced the dashboard bottom).
            Text(label, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
            Text(stringResource(R.string.dashboard_backup_banner_sub_never),
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
            Text(stringResource(R.string.dashboard_backup_banner_cta), color = Color.White,
                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
        }
    }
}

/**
 * Top-expense categories tile. Per prototype screens-dashboard.jsx:154-170:
 * - SectionLabel "Pengeluaran Teratas" (uppercase) — matches other dashboard sections
 * - No rank prefix
 * - 6dp bar tinted to a per-rank palette (primary/accent/warning/info/danger)
 * - Right-aligned amount in the SAME palette color
 */
@Composable
private fun DashboardTopCategories(
    items: List<com.gustiadhitya.sakuwise.feature.dashboard.viewmodel.TopCategorySpend>,
) {
    val sw = SwTheme.colors
    val maxAmount = items.maxOfOrNull { it.amount }?.coerceAtLeast(1L) ?: 1L
    val palette = listOf(sw.primary, sw.accent, sw.warning, sw.info, sw.danger)
    Column(modifier = Modifier.padding(horizontal = SwSpace.pageH, vertical = 8.dp)) {
        SwSectionLabel(text = stringResource(R.string.dashboard_top_expenses))
        SwCard(padding = PaddingValues(14.dp)) {
            Column {
                items.forEachIndexed { i, item ->
                    val tint = palette[i % palette.size]
                    if (i > 0) Spacer(Modifier.height(10.dp))
                    // Per proto screens-dashboard.jsx:158-167 — name + amount sit
                    // on a single row ABOVE the bar; amount color = ink (NOT the
                    // bar tint) so the row reads cleanly. 13sp name w500,
                    // 12sp amount w600. Bar 6dp tinted by rank.
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                item.name, color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                            )
                            RupiahText(
                                value = item.amount, short = true,
                                style = SwType.Amount.copy(fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum"),
                                color = sw.ink,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(sw.track),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (item.amount.toFloat() / maxAmount.toFloat())
                                            .coerceIn(0f, 1f),
                                    )
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(tint),
                            )
                        }
                    }
                }
            }
        }
    }
}
