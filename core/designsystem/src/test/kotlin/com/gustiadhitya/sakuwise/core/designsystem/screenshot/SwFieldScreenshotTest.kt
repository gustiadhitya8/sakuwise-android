package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

class SwFieldScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swField_states_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        SwField(
                            value = "",
                            onValueChange = {},
                            label = "Nama",
                            placeholder = "Masukkan nama",
                        )
                        SwField(
                            value = "Kopi Kenangan",
                            onValueChange = {},
                            label = "Deskripsi",
                            placeholder = "Masukkan deskripsi",
                        )
                        SwField(
                            value = "",
                            onValueChange = {},
                            label = "Jumlah *",
                            placeholder = "0",
                            prefix = "Rp",
                            isError = true,
                            hint = "Jumlah tidak boleh kosong",
                        )
                        SwField(
                            value = "1500000",
                            onValueChange = {},
                            label = "Jumlah",
                            prefix = "Rp",
                            suffix = "IDR",
                        )
                    }
                }
            }
        }
    }

    @Test
    fun swField_states_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = androidx.compose.ui.Modifier.padding(SakuwiseSpacing.l),
                        verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
                    ) {
                        SwField(
                            value = "",
                            onValueChange = {},
                            label = "Nama",
                            placeholder = "Masukkan nama",
                        )
                        SwField(
                            value = "Kopi Kenangan",
                            onValueChange = {},
                            label = "Deskripsi",
                        )
                        SwField(
                            value = "",
                            onValueChange = {},
                            label = "Jumlah *",
                            placeholder = "0",
                            prefix = "Rp",
                            isError = true,
                            hint = "Jumlah tidak boleh kosong",
                        )
                    }
                }
            }
        }
    }
}
