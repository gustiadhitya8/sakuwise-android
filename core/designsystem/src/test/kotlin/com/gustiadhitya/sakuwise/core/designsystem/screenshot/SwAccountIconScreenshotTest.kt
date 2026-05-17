package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwAccountIcon
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwAccountIconScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swAccountIcon_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "Mandiri")
                        SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "BCA")
                        SwAccountIcon(icon = Icons.Outlined.CreditCard, contentDescription = "GoPay")
                        SwAccountIcon(icon = Icons.Outlined.Savings, contentDescription = "Tunai")
                        SwAccountIcon(icon = Icons.Outlined.ShoppingBag, contentDescription = "Belanja")
                    }
                }
            }
        }
    }

    @Test
    fun swAccountIcon_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                    ) {
                        SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "Mandiri")
                        SwAccountIcon(icon = Icons.Outlined.CreditCard, contentDescription = "GoPay")
                        SwAccountIcon(icon = Icons.Outlined.Savings, contentDescription = "Tunai")
                    }
                }
            }
        }
    }
}
