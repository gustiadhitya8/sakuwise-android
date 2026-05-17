package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.DefaultSakuwiseTabs
import com.gustiadhitya.sakuwise.core.designsystem.component.SwTabBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwTabBarScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swTabBar_beranda_selected_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        // Simulate content area above tab bar
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                        )
                        SwTabBar(
                            tabs = DefaultSakuwiseTabs,
                            selectedIndex = 0,
                            onTabSelected = {},
                            onFabClick = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun swTabBar_saya_selected_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                        )
                        // Saya = index 3 (right-most), FAB visible center
                        SwTabBar(
                            tabs = DefaultSakuwiseTabs,
                            selectedIndex = 3,
                            onTabSelected = {},
                            onFabClick = {},
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun swTabBar_states_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column {
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                        )
                        SwTabBar(
                            tabs = DefaultSakuwiseTabs,
                            selectedIndex = 2,
                            onTabSelected = {},
                            onFabClick = {},
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
