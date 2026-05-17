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
import com.gustiadhitya.sakuwise.core.designsystem.component.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwBarScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swBar_progress_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        listOf(0f, 0.25f, 0.67f, 0.81f, 1.0f, 1.2f, 1.5f).forEach { p ->
                            Text(
                                text = "${(p * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            SwBar(
                                progress = p,
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                label = "${(p * 100).toInt()}%",
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swBar_progress_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        listOf(0.67f, 1.0f, 1.2f).forEach { p ->
                            Text(
                                text = "${(p * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            SwBar(
                                progress = p,
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                label = "${(p * 100).toInt()}%",
                            )
                        }
                    }
                }
            }
        }
    }
}
