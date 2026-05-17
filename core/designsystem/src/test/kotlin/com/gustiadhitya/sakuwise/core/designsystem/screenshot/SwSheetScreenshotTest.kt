package com.gustiadhitya.sakuwise.core.designsystem.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.gustiadhitya.sakuwise.core.designsystem.component.FieldButton
import com.gustiadhitya.sakuwise.core.designsystem.theme.H2Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import org.junit.Rule
import org.junit.Test

// ModalBottomSheet renders in a Dialog window outside Paparazzi's capture scope.
// These tests render the sheet surface directly to verify styling (shape, title, content layout).
class SwSheetScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_6)

    @Test
    fun swSheet_surface_light() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = false) {
                Surface(
                    shape = SakuwiseShapes.sheet,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.padding(bottom = SakuwiseSpacing.xxxl)) {
                        SheetHeader(title = "Aksi Plan")
                        Column(
                            modifier = androidx.compose.ui.Modifier.padding(horizontal = SakuwiseSpacing.l),
                            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                        ) {
                            FieldButton(text = "Salin dari bulan lalu", subText = "Ambil item recurring dari Plan April", onClick = {})
                            FieldButton(text = "Terapkan template starter", subText = "Struktur kategori standard Indonesia", onClick = {})
                            FieldButton(text = "Atur persentase untuk plan ini", subText = "Override alokasi 50/30/20", onClick = {})
                        }
                    }
                }
            }
        }
    }

    @Test
    fun swSheet_surface_dark() {
        paparazzi.snapshot {
            SakuwiseTheme(darkTheme = true) {
                Surface(
                    shape = SakuwiseShapes.sheet,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = androidx.compose.ui.Modifier.padding(bottom = SakuwiseSpacing.xxxl)) {
                        SheetHeader(title = "Aksi Akun")
                        Column(
                            modifier = androidx.compose.ui.Modifier.padding(horizontal = SakuwiseSpacing.l),
                            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
                        ) {
                            FieldButton(text = "Edit nama & icon", subText = "Ubah nama atau warna akun", onClick = {})
                            FieldButton(text = "Lihat semua snapshot", subText = "Riwayat lengkap dalam tabel", onClick = {})
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SheetHeader(title: String) {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(start = SakuwiseSpacing.l, end = SakuwiseSpacing.xs, bottom = SakuwiseSpacing.m),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = H2Style.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = androidx.compose.ui.Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Tutup",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = androidx.compose.ui.Modifier
                .size(SakuwiseSpacing.xxxxxl)
                .padding(SakuwiseSpacing.m),
        )
    }
}
