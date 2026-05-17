package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwButtonScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swButton_allVariants_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwButtonVariant.entries.forEach { variant ->
                            SwButton(text = variant.name, onClick = {}, variant = variant)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swButton_allVariants_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwButtonVariant.entries.forEach { variant ->
                            SwButton(text = variant.name, onClick = {}, variant = variant)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swButton_sizes_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwButtonSize.entries.forEach { size ->
                            SwButton(text = "Size ${size.name}", onClick = {}, size = size)
                        }
                        SwButton(text = "Disabled", onClick = {}, enabled = false)
                        SwButton(text = "Loading", onClick = {}, loading = true)
                    }
                }
            }
        }
    }
}
