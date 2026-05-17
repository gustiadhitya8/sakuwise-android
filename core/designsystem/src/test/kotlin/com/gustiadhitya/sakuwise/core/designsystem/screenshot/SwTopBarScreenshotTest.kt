package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTopBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwTopBarScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swTopBar_variants_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwTopBar(title = "Beranda")
                        SwTopBar(title = "Beranda", subtitle = "Plan Mei · sisa 16 hari")
                        SwTopBar(title = "Pengeluaran", onBack = {})
                        SwTopBar(
                            title = "Pengeluaran",
                            onBack = {},
                            rightAction = {
                                SwButton(
                                    text = "Simpan",
                                    onClick = {},
                                    size = SwButtonSize.Sm,
                                    fillWidth = false,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun swTopBar_variants_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                        SwTopBar(title = "Beranda", subtitle = "Plan Mei · sisa 16 hari")
                        SwTopBar(title = "Pengeluaran", onBack = {})
                    }
                }
            }
        }
    }
}
