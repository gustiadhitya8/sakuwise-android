package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwCategoryDot
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwCategoryDotScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    private val sampleNames = listOf(
        "Kopi Kenangan",
        "Warteg Bahari",
        "Pertamina",
        "Belanja",
        "Gaji",
        "Restoran",
        "Sushi Tei",
        "Investasi",
    )

    @Test
    fun swCategoryDot_palette_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        sampleNames.forEach { SwCategoryDot(name = it) }
                    }
                }
            }
        }
    }

    @Test
    fun swCategoryDot_palette_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        sampleNames.forEach { SwCategoryDot(name = it) }
                    }
                }
            }
        }
    }
}
