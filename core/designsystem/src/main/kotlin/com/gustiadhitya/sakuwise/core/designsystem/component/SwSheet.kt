package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.designsystem.theme.H2Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SakuwiseShapes.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(bottom = SakuwiseSpacing.xxxl)) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = SakuwiseSpacing.l, end = SakuwiseSpacing.xs, bottom = SakuwiseSpacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = H2Style.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SwSheetPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            var show by remember { mutableStateOf(true) }
            if (show) {
                SwSheet(
                    title = "Aksi Plan",
                    onDismiss = { show = false },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = SakuwiseSpacing.l),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwSheetPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            var show by remember { mutableStateOf(true) }
            if (show) {
                SwSheet(
                    title = "Aksi Akun",
                    onDismiss = { show = false },
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = SakuwiseSpacing.l),
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
