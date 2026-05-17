package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.FieldButton
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class FieldButtonScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun fieldButton_variants_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        FieldButton(
                            text = "Kopi/Kafe",
                            label = "Plan Item *",
                            subText = "Makan di Luar · Wants",
                            leadingIcon = Icons.Default.Category,
                            onClick = {},
                        )
                        FieldButton(
                            text = "GoPay",
                            label = "Akun *",
                            subText = "Saldo: Rp 280.000",
                            leadingIcon = Icons.Default.AccountBalance,
                            onClick = {},
                        )
                        FieldButton(
                            text = "15 Mei 2026",
                            label = "Tanggal",
                            subText = "2 hari lalu",
                            leadingIcon = Icons.Default.CalendarToday,
                            onClick = {},
                        )
                        FieldButton(
                            text = "Pilih kategori",
                            onClick = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun fieldButton_variants_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        FieldButton(
                            text = "GoPay",
                            label = "Akun *",
                            subText = "Saldo: Rp 280.000",
                            leadingIcon = Icons.Default.AccountBalance,
                            onClick = {},
                        )
                        FieldButton(
                            text = "15 Mei 2026",
                            label = "Tanggal",
                            subText = "2 hari lalu",
                            leadingIcon = Icons.Default.CalendarToday,
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}
