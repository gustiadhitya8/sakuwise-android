package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRow
import com.gustiadhitya.sakuwise.core.designsystem.component.SwSettingsRowVariant
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwSettingsRowScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swSettingsRow_variants_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(vertical = SakuwiseSpacing.s),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
                    ) {
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
                            icon = Icons.Outlined.Lock,
                            label = "PIN & Biometrik",
                            value = "Aktif",
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
                }
            }
        }
    }

    @Test
    fun swSettingsRow_variants_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(vertical = SakuwiseSpacing.s),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
                    ) {
                        SwSettingsRow(
                            icon = Icons.Outlined.Language,
                            label = "Bahasa",
                            value = "Bahasa Indonesia",
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
                    }
                }
            }
        }
    }
}
