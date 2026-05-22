package com.gustiadhitya.sakuwise.feature.settings.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun ImportTransactionScreen(
    onBack: () -> Unit,
    viewModel: ImportTransactionViewModel = hiltViewModel(),
) {
    val sw      = SwTheme.colors
    val state   by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var selectedAccountId by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.parseFile(it) }
    }

    SimpleSettingsScreen(title = "Import Transaksi", onBack = onBack) {

        Text(
            "Import transaksi dari file CSV. Format kolom: Date, Type, Category, Amount, Note.",
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            // ── Idle ────────────────────────────────────────────────
            is ImportUiState.Idle -> {
                SwButton(
                    text = "Pilih File CSV",
                    onClick = { launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")) },
                    leading = { Icon(Icons.Outlined.FileOpen, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(16.dp))
                SwCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Format CSV yang diperlukan:", color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(4.dp))
                        FormatHintRow("Date", "YYYYMMDD")
                        FormatHintRow("Type", "Income / Expense / Transfer")
                        FormatHintRow("Category", "Optional — category name")
                        FormatHintRow("Amount", "Number only, no Rp or dots")
                        FormatHintRow("Note", "Optional — description")
                    }
                }
            }

            // ── Parsing ─────────────────────────────────────────────
            is ImportUiState.Parsing -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                        color = sw.primary, trackColor = sw.track)
                }
                Text("Membaca file…", color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            // ── Preview ─────────────────────────────────────────────
            is ImportUiState.Preview -> {
                // Summary chip
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(sw.successSoft).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = sw.success, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("${s.rows.size} baris siap diimpor", color = sw.success,
                            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                        if (s.skipped > 0)
                            Text("${s.skipped} baris dilewati (amount kosong/tidak valid)",
                                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                }

                if (s.errors.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    SwCard {
                        Column {
                            Text("Peringatan parsing:", color = sw.warning,
                                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                            s.errors.take(5).forEach { err ->
                                Text("• $err", color = sw.inkMuted,
                                    style = SwType.LabelSmall.copy(fontSize = 11.sp))
                            }
                            if (s.errors.size > 5)
                                Text("... dan ${s.errors.size - 5} lainnya", color = sw.inkMuted,
                                    style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Account picker
                Text("PILIH AKUN TUJUAN", color = sw.inkSubtle,
                    style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

                if (accounts.isEmpty()) {
                    Text("Belum ada akun. Buat akun terlebih dahulu.", color = sw.danger,
                        style = SwType.LabelSmall.copy(fontSize = 12.sp))
                } else {
                    SwCard(padding = PaddingValues(0.dp)) {
                        Column {
                            accounts.forEach { acc ->
                                val selected = selectedAccountId == acc.id
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { selectedAccountId = acc.id }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(20.dp).clip(CircleShape)
                                            .background(if (selected) sw.primary else sw.track)
                                            .border(2.dp, if (selected) sw.primary else sw.border, CircleShape),
                                    ) {
                                        if (selected) Box(Modifier.size(8.dp).clip(CircleShape).background(sw.onPrimary))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(acc.name, color = sw.ink,
                                        style = SwType.LabelStrong.copy(fontSize = 14.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Preview table (first 10)
                Text("PRATINJAU (${minOf(s.rows.size, 10)} dari ${s.rows.size})", color = sw.inkSubtle,
                    style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

                SwCard(padding = PaddingValues(0.dp)) {
                    Column {
                        // Header
                        PreviewHeaderRow()
                        s.rows.take(10).forEach { row -> PreviewDataRow(row) }
                        if (s.rows.size > 10) {
                            Text("... dan ${s.rows.size - 10} transaksi lainnya",
                                color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                SwButton(
                    text = "Import ${s.rows.size} Transaksi",
                    onClick = {
                        val accId = selectedAccountId ?: return@SwButton
                        viewModel.importRows(s.rows, accId)
                    },
                    enabled = selectedAccountId != null,
                    leading = { Icon(Icons.Outlined.UploadFile, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Pilih File Lain", onClick = {
                    viewModel.reset()
                }, variant = SwButtonVariant.Ghost)
            }

            // ── Importing ───────────────────────────────────────────
            is ImportUiState.Importing -> {
                val progress = if (s.total > 0) s.done.toFloat() / s.total else 0f
                Spacer(Modifier.height(8.dp))
                Text("Mengimpor transaksi… ${s.done} / ${s.total}",
                    color = sw.ink, style = SwType.LabelStrong.copy(fontSize = 14.sp))
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = sw.primary, trackColor = sw.track)
                Spacer(Modifier.height(8.dp))
                Text("Mohon tunggu, jangan tutup layar ini.",
                    color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
            }

            // ── Done ────────────────────────────────────────────────
            is ImportUiState.Done -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(sw.successSoft).padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CheckCircle, null, tint = sw.success, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import Selesai!", color = sw.success,
                                style = SwType.H2.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${s.imported} transaksi berhasil diimpor.", color = sw.ink,
                            style = SwType.Body.copy(fontSize = 13.sp))
                        if (s.duplicates > 0)
                            Text("${s.duplicates} duplikat dilewati (sudah ada).", color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 12.sp))
                        if (s.skipped - s.duplicates > 0)
                            Text("${s.skipped - s.duplicates} baris dilewati (data tidak valid).", color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                SwButton(text = "Import File Lain", onClick = { viewModel.reset() }, variant = SwButtonVariant.Outline)
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Selesai", onClick = onBack, variant = SwButtonVariant.Ghost)
            }

            // ── Error ───────────────────────────────────────────────
            is ImportUiState.Err -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(sw.dangerSoft).padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ErrorOutline, null, tint = sw.danger, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Gagal membaca file", color = sw.danger,
                                style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(s.message, color = sw.ink, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                SwButton(text = "Coba Lagi", onClick = {
                    viewModel.reset()
                    launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"))
                })
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Batal", onClick = { viewModel.reset() }, variant = SwButtonVariant.Ghost)
            }
        }
    }
}

@Composable
private fun FormatHintRow(col: String, desc: String) {
    val sw = SwTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(col, color = sw.primary,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.width(80.dp))
        Text(desc, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp))
    }
}

@Composable
private fun PreviewHeaderRow() {
    val sw = SwTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().background(sw.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text("Tanggal", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(72.dp))
        Text("Tipe", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(56.dp))
        Text("Catatan", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f))
        Text("Jumlah", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun PreviewDataRow(row: ImportRow) {
    val sw = SwTheme.colors
    val dtFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
    val typeColor = if (row.type == com.gustiadhitya.sakuwise.core.domain.model.TxnType.Income) sw.success else sw.danger
    val typeLabel = if (row.type == com.gustiadhitya.sakuwise.core.domain.model.TxnType.Income) "Masuk" else "Keluar"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(row.date.format(dtFmt), color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.width(72.dp))
        Text(typeLabel, color = typeColor,
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.width(56.dp))
        Text(row.note ?: "—", color = sw.ink,
            style = SwType.Caption.copy(fontSize = 11.sp),
            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(row.amount.toRupiahShort(), color = sw.ink,
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold))
    }
}
