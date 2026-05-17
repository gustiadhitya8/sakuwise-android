package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwChip
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwChipScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swChip_states_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                            SwChip(text = "Semua", selected = false, onClick = {})
                            SwChip(text = "Needs", selected = true, onClick = {})
                            SwChip(text = "Wants", selected = false, onClick = {})
                            SwChip(text = "Investment", selected = false, onClick = {})
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s)) {
                            SwChip(text = "Semua", selected = true, onClick = {})
                            SwChip(text = "Needs", selected = false, onClick = {})
                            SwChip(text = "Wants", selected = false, onClick = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swChip_states_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwChip(text = "Semua", selected = false, onClick = {})
                        SwChip(text = "Needs", selected = true, onClick = {})
                        SwChip(text = "Wants", selected = false, onClick = {})
                    }
                }
            }
        }
    }
}
