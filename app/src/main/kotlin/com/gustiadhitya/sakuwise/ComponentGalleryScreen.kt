package com.gustiadhitya.sakuwise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.gustiadhitya.sakuwise.core.designsystem.brand.DaunMark
import com.gustiadhitya.sakuwise.core.designsystem.brand.Lockup
import com.gustiadhitya.sakuwise.core.designsystem.brand.Wordmark
import com.gustiadhitya.sakuwise.core.designsystem.component.DefaultSakuwiseTabs
import com.gustiadhitya.sakuwise.core.designsystem.component.SnapshotDataPoint
import com.gustiadhitya.sakuwise.core.designsystem.component.SnapshotPeriod
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAccountIcon
import com.gustiadhitya.sakuwise.core.designsystem.component.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.component.SwCategoryDot
import com.gustiadhitya.sakuwise.core.designsystem.component.SwChip
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRow
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRowVariant
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSnapshotChart
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTabBar
import com.gustiadhitya.sakuwise.core.designsystem.component.SwToggle
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTopBar
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentGalleryScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var biometricOn by remember { mutableStateOf(true) }
    var notifOn by remember { mutableStateOf(false) }
    var chartPeriod by remember { mutableStateOf(SnapshotPeriod.THREE_MONTHS) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = SakuwiseSpacing.xxxxxxl + SakuwiseSpacing.l),
            ) {
                SwTopBar(title = "Component Gallery", subtitle = "M5a – M5d")

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SakuwiseSpacing.l),
                    verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                ) {
                    // ── Brand ──────────────────────────────────────────────
                    GallerySection("Brand")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.l),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DaunMark(size = SakuwiseSpacing.xxxxxl)
                        Column {
                            Wordmark()
                            Spacer(Modifier.height(SakuwiseSpacing.xs))
                            Lockup()
                        }
                    }

                    // ── Chart ──────────────────────────────────────────────
                    GallerySection("SwSnapshotChart")
                    SwSnapshotChart(
                        dataPoints = galleryChartData(),
                        selectedPeriod = chartPeriod,
                        onPeriodSelected = { chartPeriod = it },
                    )

                    // ── Buttons ────────────────────────────────────────────
                    GallerySection("SwButton")
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwButton(text = "Simpan", onClick = {}, modifier = Modifier.fillMaxWidth())
                        SwButton(text = "Batal", onClick = {}, variant = SwButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
                        SwButton(text = "Hapus", onClick = {}, variant = SwButtonVariant.Danger, modifier = Modifier.fillMaxWidth())
                    }

                    // ── Chips ──────────────────────────────────────────────
                    GallerySection("SwChip")
                    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwChip(text = "Semua", selected = true, onClick = {})
                        SwChip(text = "Pengeluaran", selected = false, onClick = {})
                        SwChip(text = "Pemasukan", selected = false, onClick = {})
                    }

                    // ── Bar ────────────────────────────────────────────────
                    GallerySection("SwBar")
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwBar(progress = 0.75f, label = "Makan & Minum")
                        SwBar(progress = 0.40f, label = "Transportasi")
                        SwBar(progress = 0.15f, label = "Hiburan")
                    }

                    // ── Account icon ───────────────────────────────────────
                    GallerySection("SwAccountIcon")
                    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwAccountIcon(icon = SakuwiseIcons.Bank, contentDescription = "Bank")
                        SwAccountIcon(icon = SakuwiseIcons.Cash, contentDescription = "Tunai")
                        SwAccountIcon(icon = SakuwiseIcons.Wallet, contentDescription = "Dompet")
                        SwAccountIcon(icon = SakuwiseIcons.Deposit, contentDescription = "Deposito")
                    }

                    // ── Category dot ───────────────────────────────────────
                    GallerySection("SwCategoryDot")
                    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        listOf("Kopi", "Warteg", "BBM", "Belanja", "Gaji", "Resto", "Sushi", "Investasi")
                            .forEach { SwCategoryDot(name = it) }
                    }

                    // ── Settings rows ──────────────────────────────────────
                    GallerySection("SwSettingsRow")
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs)) {
                        SwSettingsRow(icon = Icons.Outlined.Language, label = "Bahasa", value = "Bahasa Indonesia", onClick = {})
                        SwSettingsRow(icon = Icons.Outlined.Lock, label = "PIN & Biometrik", value = "Aktif", onClick = {})
                        SwSettingsRow(icon = Icons.Filled.Backup, label = "Backup", value = "34 hari lalu", variant = SwSettingsRowVariant.Warning, onClick = {})
                        SwSettingsRow(icon = Icons.Filled.Delete, label = "Reset", sub = "Hapus semua data", variant = SwSettingsRowVariant.Danger, onClick = {})
                        SwSettingsRow(icon = Icons.Outlined.Info, label = "Tentang", value = "v1.0", onClick = {})
                    }

                    // ── Toggle ─────────────────────────────────────────────
                    GallerySection("SwToggle")
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs)) {
                        SwToggle(checked = biometricOn, onCheckedChange = { biometricOn = it }, label = "Biometrik aktif")
                        SwToggle(checked = notifOn, onCheckedChange = { notifOn = it }, label = "Notifikasi pengingat")
                    }

                    // ── Icons ──────────────────────────────────────────────
                    GallerySection("SakuwiseIcons (${allIcons.size})")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        allIcons.forEach { (name, vector) ->
                            IconTile(name = name, vector = vector)
                        }
                    }

                    Spacer(Modifier.height(SakuwiseSpacing.xxxl))
                }
            }

            SwTabBar(
                tabs = DefaultSakuwiseTabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                onFabClick = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun GallerySection(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(SakuwiseSpacing.m))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = SakuwiseSpacing.xs),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun IconTile(name: String, vector: ImageVector) {
    Column(
        modifier = Modifier
            .size(SakuwiseSpacing.xxxxxxl)
            .then(
                Modifier
                    .size(SakuwiseSpacing.xxxxxxl)
                    .padding(SakuwiseSpacing.xs),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = vector,
            contentDescription = name,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(SakuwiseSpacing.xxl),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

private fun galleryChartData(): List<SnapshotDataPoint> {
    val now = System.currentTimeMillis()
    val day = 86_400_000L
    return listOf(
        SnapshotDataPoint(now - 90 * day, 8_200_000),
        SnapshotDataPoint(now - 75 * day, 9_500_000),
        SnapshotDataPoint(now - 60 * day, 7_800_000),
        SnapshotDataPoint(now - 45 * day, 10_200_000),
        SnapshotDataPoint(now - 30 * day, 11_500_000),
        SnapshotDataPoint(now - 15 * day, 9_900_000),
        SnapshotDataPoint(now,             12_400_000),
    )
}

private val allIcons: List<Pair<String, ImageVector>> by lazy {
    listOf(
        "Home" to SakuwiseIcons.Home,
        "Plan" to SakuwiseIcons.Plan,
        "Plus" to SakuwiseIcons.Plus,
        "Assets" to SakuwiseIcons.Assets,
        "Me" to SakuwiseIcons.Me,
        "HomeFilled" to SakuwiseIcons.HomeFilled,
        "PlanFilled" to SakuwiseIcons.PlanFilled,
        "AssetsFilled" to SakuwiseIcons.AssetsFilled,
        "MeFilled" to SakuwiseIcons.MeFilled,
        "Back" to SakuwiseIcons.Back,
        "Close" to SakuwiseIcons.Close,
        "More" to SakuwiseIcons.More,
        "Search" to SakuwiseIcons.Search,
        "ChevronRight" to SakuwiseIcons.ChevronRight,
        "ChevronDown" to SakuwiseIcons.ChevronDown,
        "ChevronUp" to SakuwiseIcons.ChevronUp,
        "Edit" to SakuwiseIcons.Edit,
        "Trash" to SakuwiseIcons.Trash,
        "Copy" to SakuwiseIcons.Copy,
        "Check" to SakuwiseIcons.Check,
        "Camera" to SakuwiseIcons.Camera,
        "Calendar" to SakuwiseIcons.Calendar,
        "Filter" to SakuwiseIcons.Filter,
        "Bell" to SakuwiseIcons.Bell,
        "Shield" to SakuwiseIcons.Shield,
        "Eye" to SakuwiseIcons.Eye,
        "EyeOff" to SakuwiseIcons.EyeOff,
        "Expense" to SakuwiseIcons.Expense,
        "Income" to SakuwiseIcons.Income,
        "Transfer" to SakuwiseIcons.Transfer,
        "Cash" to SakuwiseIcons.Cash,
        "Bank" to SakuwiseIcons.Bank,
        "Wallet" to SakuwiseIcons.Wallet,
        "Gold" to SakuwiseIcons.Gold,
        "Land" to SakuwiseIcons.Land,
        "Deposit" to SakuwiseIcons.Deposit,
        "Link" to SakuwiseIcons.Link,
        "Receipt" to SakuwiseIcons.Receipt,
        "ArrowUpRight" to SakuwiseIcons.ArrowUpRight,
        "ArrowDownLeft" to SakuwiseIcons.ArrowDownLeft,
        "Swap" to SakuwiseIcons.Swap,
        "Leaf" to SakuwiseIcons.Leaf,
        "Warning" to SakuwiseIcons.Warning,
        "Info" to SakuwiseIcons.Info,
        "Sparkle" to SakuwiseIcons.Sparkle,
    )
}
