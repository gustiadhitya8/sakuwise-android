package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.RupiahText
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAmount
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAmountDisplay
import com.gustiadhitya.sakuwise.core.designsystem.theme.H1Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class RupiahTextScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun rupiahText_formats_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        // Full format — tabular num alignment
                        RupiahText(amount = 4_700_000L, style = H1Style)
                        RupiahText(amount = 9_999_999L, style = H1Style)
                        RupiahText(amount = 1_500_000L)
                        RupiahText(amount = 28_000L)
                        RupiahText(amount = 500L)
                        // Short format
                        RupiahText(amount = 5_200_000L, short = true)
                        RupiahText(amount = 10_800_000L, short = true)
                        RupiahText(amount = 980_000L, short = true)
                        RupiahText(amount = -28_000L, short = true)
                        // SwAmount wrapper
                        SwAmount(amount = 15_500_000L)
                        SwAmount(
                            amount = -10_800_000L,
                            display = SwAmountDisplay.Short,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun rupiahText_formats_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        RupiahText(amount = 4_700_000L, style = H1Style)
                        RupiahText(amount = 5_200_000L, short = true)
                        SwAmount(amount = -10_800_000L, display = SwAmountDisplay.Short)
                    }
                }
            }
        }
    }
}
