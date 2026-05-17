package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwToggle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwToggleScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swToggle_states_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwToggle(checked = true, onCheckedChange = {}, label = "Biometrik aktif")
                        SwToggle(checked = false, onCheckedChange = {}, label = "Notifikasi pengingat")
                        SwToggle(checked = false, onCheckedChange = {}, label = "Fitur nonaktif", enabled = false)
                    }
                }
            }
        }
    }

    @Test
    fun swToggle_states_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwToggle(checked = true, onCheckedChange = {}, label = "Biometrik aktif")
                        SwToggle(checked = false, onCheckedChange = {}, label = "Notifikasi pengingat")
                    }
                }
            }
        }
    }
}
