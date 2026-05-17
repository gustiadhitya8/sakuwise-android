package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwCardScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swCard_variants_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        SwCard(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                            Text("Static card", style = MaterialTheme.typography.bodyMedium)
                        }
                        SwCard(
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                            onClick = {},
                        ) {
                            Text("Clickable card with press feedback", style = MaterialTheme.typography.bodyMedium)
                        }
                        SwCard(
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                            noBorder = true,
                        ) {
                            Text("No border card", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swCard_variants_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        SwCard(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                            Text("Card dark mode", style = MaterialTheme.typography.bodyMedium)
                        }
                        SwCard(
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                            onClick = {},
                        ) {
                            Text("Clickable card dark", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
