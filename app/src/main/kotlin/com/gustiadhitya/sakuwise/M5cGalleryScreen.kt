package com.gustiadhitya.sakuwise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Savings
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
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.component.DefaultSakuwiseTabs
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAccountIcon
import com.gustiadhitya.sakuwise.core.designsystem.component.SwCategoryDot
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRow
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRowVariant
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTabBar
import com.gustiadhitya.sakuwise.core.designsystem.component.SwToggle
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTopBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing

private val TabBarTotalHeight = 80.dp  // NavBarHeight(64) + FabLift(16)

@Composable
fun M5cGalleryScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var notifEnabled by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = TabBarTotalHeight),
            ) {
                SwTopBar(title = "M5c Components", subtitle = "Specialized Components")

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SakuwiseSpacing.l),
                    verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                ) {
                    GalleryLabel("SwAccountIcon")
                    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "Mandiri")
                        SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "BCA")
                        SwAccountIcon(icon = Icons.Outlined.CreditCard, contentDescription = "GoPay")
                        SwAccountIcon(icon = Icons.Outlined.Savings, contentDescription = "Tunai")
                    }

                    GalleryLabel("SwCategoryDot")
                    Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        listOf("Kopi Kenangan", "Warteg Bahari", "Pertamina", "Belanja", "Gaji", "Restoran", "Sushi Tei", "Investasi")
                            .forEach { SwCategoryDot(name = it) }
                    }

                    GalleryLabel("SwSettingsRow")
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs)) {
                        SwSettingsRow(
                            icon = Icons.Outlined.Language,
                            label = "Bahasa",
                            value = "Bahasa Indonesia",
                            onClick = {},
                        )
                        SwSettingsRow(
                            icon = Icons.Outlined.CalendarToday,
                            label = "Tanggal Mulai Periode",
                            value = "Tanggal 1",
                            onClick = {},
                        )
                        SwSettingsRow(
                            icon = Icons.Filled.Backup,
                            label = "Backup & Pemulihan",
                            value = "34 hari lalu",
                            variant = SwSettingsRowVariant.Warning,
                            onClick = {},
                        )
                        SwSettingsRow(
                            icon = Icons.Filled.Delete,
                            label = "Export & Reset",
                            sub = "Hapus semua data, mulai dari nol",
                            variant = SwSettingsRowVariant.Danger,
                            onClick = {},
                        )
                        SwSettingsRow(
                            icon = Icons.Outlined.Info,
                            label = "Tentang Sakuwise",
                            value = "v1.0",
                            onClick = {},
                        )
                    }

                    GalleryLabel("SwToggle")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
                    ) {
                        SwToggle(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            label = "Biometrik aktif",
                        )
                        SwToggle(
                            checked = notifEnabled,
                            onCheckedChange = { notifEnabled = it },
                            label = "Notifikasi pengingat",
                        )
                    }

                    GalleryLabel("SwTabBar (lihat bawah — interaktif)")
                    Text(
                        text = "Tab terpilih: ${listOf("Beranda", "Plan", "Aset", "Saya")[selectedTab]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.padding(bottom = SakuwiseSpacing.xxxl),
                    )
                }
            }

            // Tab bar pinned at bottom
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
private fun GalleryLabel(title: String) {
    Text(
        text = title,
        style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.padding(top = SakuwiseSpacing.m),
    )
}
