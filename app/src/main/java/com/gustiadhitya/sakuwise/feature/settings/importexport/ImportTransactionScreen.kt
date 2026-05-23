package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Intent
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var updateMode by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.parseFile(it) }
    }

    SimpleSettingsScreen(title = "Import Transaksi", onBack = onBack) {

        Text(
            "Import transaksi dari file CSV (delimiter titik koma). Format: Tanggal;Tipe;Kategori;Item;Akun;Jumlah;Catatan.",
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            // ── Idle ──────────────────────────────────────────────────
            is ImportUiState.Idle -> {
                SwButton(
                    text = "Pilih File CSV",
                    onClick = { launcher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")) },
                    leading = { Icon(Icons.Outlined.FileOpen, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(10.dp))
                SwButton(
                    text = "Unduh Template CSV",
                    onClick = {
                        runCatching {
                            val uri = viewModel.shareTemplate()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Simpan Template").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    variant = SwButtonVariant.Outline,
                    leading = { Icon(Icons.Outlined.Download, null, tint = sw.primary, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(16.dp))
                SwCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Format CSV yang diperlukan:", color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(4.dp))
                        FormatHintRow("Tanggal", "YYYYMMDD  (mis. 20260503)")
                        FormatHintRow("Tipe", "Expense / Income / Transfer")
                        FormatHintRow("Kategori", "Nama kategori di Plan (opsional)")
                        FormatHintRow("Item", "Nama item di bawah Kategori (opsional)")
                        FormatHintRow("Akun", "Nama akun/dompet (opsional)")
                        FormatHintRow("Jumlah", "Angka saja, tanpa Rp atau titik")
                        FormatHintRow("Catatan", "Deskripsi tambahan (opsional)")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Header bisa bahasa Indonesia atau English (Date/Tanggal, dll.).",
                            color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                }
                }


            // ── Parsing ───────────────────────────────────────────────
            is ImportUiState.Parsing -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                        color = sw.primary, trackColor = sw.track)
                }
                Text("Membaca file…", color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            // ── Preview ───────────────────────────────────────────────
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

                // Unresolved plan items warning with list
                if (s.unresolvedItems > 0) {
                    Spacer(Modifier.height(8.dp))
                    val sw2 = SwTheme.colors
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(sw2.warningSoft).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Outlined.Warning, null, tint = sw2.warning, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("${s.unresolvedItems} baris tidak cocok dengan item di Plan",
                                    color = sw2.warning,
                                    style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
                                Text("Transaksi tetap diimpor tapi tidak terhubung ke anggaran. Pastikan nama Kategori dan Item sama persis.",
                                    color = sw2.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                            }
                        }
                        s.unresolvedItemDescriptions.take(8).forEach { desc ->
                            Text("• $desc", color = sw2.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(start = 26.dp))
                        }
                        if (s.unresolvedItemDescriptions.size > 8)
                            Text("• ... dan ${s.unresolvedItemDescriptions.size - 8} lainnya",
                                color = sw2.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                                modifier = Modifier.padding(start = 26.dp))
                    }
                }

                // Unresolved account names warning
                if (s.unresolvedAccounts > 0) {
                    Spacer(Modifier.height(8.dp))
                    WarningChip("${s.unresolvedAccounts} nama akun tidak ditemukan",
                        "Baris tersebut akan menggunakan Akun Cadangan di bawah. Pastikan nama akun sama persis dengan yang ada di aplikasi.")
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

                // Fallback account picker — shown when some rows have no resolved account
                if (s.needsFallbackAccount) {
                    val label = if (s.unresolvedAccounts > 0) "AKUN CADANGAN" else "PILIH AKUN TUJUAN"
                    val sub   = if (s.unresolvedAccounts > 0)
                        "Digunakan untuk baris tanpa kolom Akun atau yang tidak cocok"
                    else "Semua transaksi akan masuk ke akun ini"
                    Text(label, color = sw.inkSubtle,
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                    Text(sub, color = sw.inkMuted,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp),
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
                }

                // Preview table (first 10)
                Text("PRATINJAU (${minOf(s.rows.size, 10)} dari ${s.rows.size})", color = sw.inkSubtle,
                    style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

                SwCard(padding = PaddingValues(0.dp)) {
                    Column {
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

                // Update mode toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.surface)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Mode Perbarui", color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                        Text("Perbarui catatan transaksi yang sudah ada",
                            color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    Switch(
                        checked = updateMode,
                        onCheckedChange = { updateMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = sw.onPrimary,
                            checkedTrackColor = sw.primary,
                            uncheckedThumbColor = sw.inkSubtle,
                            uncheckedTrackColor = sw.track,
                        ),
                    )
                }
                Spacer(Modifier.height(10.dp))

                val canImport = !s.needsFallbackAccount || selectedAccountId != null
                val importLabel = if (updateMode) "Perbarui ${s.rows.size} Transaksi" else "Import ${s.rows.size} Transaksi"
                SwButton(
                    text = importLabel,
                    onClick = { viewModel.importRows(s.rows, selectedAccountId, updateMode) },
                    enabled = canImport,
                    leading = { Icon(Icons.Outlined.UploadFile, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
                )
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Pilih File Lain", onClick = { viewModel.reset() }, variant = SwButtonVariant.Ghost)
            }

            // ── Importing ─────────────────────────────────────────────
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

            // ── Done ──────────────────────────────────────────────────
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
                        if (s.imported > 0)
                            Text("${s.imported} transaksi berhasil diimpor.", color = sw.ink,
                                style = SwType.Body.copy(fontSize = 13.sp))
                        if (s.updated > 0)
                            Text("${s.updated} transaksi diperbarui.", color = sw.ink,
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

            // ── Error ─────────────────────────────────────────────────
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
private fun WarningChip(title: String, body: String) {
    val sw = SwTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(sw.warningSoft).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Outlined.Warning, null, tint = sw.warning, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = sw.warning,
                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
            Text(body, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
    }
}

@Composable
private fun FormatHintRow(col: String, desc: String) {
    val sw = SwTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(col, color = sw.primary,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.width(72.dp))
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
        Text("Tgl", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(44.dp))
        Text("Kategori · Item", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f))
        Text("Akun", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(56.dp))
        Text("Jumlah", color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun PreviewDataRow(row: ImportRow) {
    val sw = SwTheme.colors
    val dtFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM")
    val categoryLabel = when {
        row.kategori != null && row.item != null -> "${row.kategori} · ${row.item}"
        row.kategori != null -> row.kategori
        row.note != null -> row.note
        else -> "—"
    }
    val accountResolved = row.resolvedAccountId != null
    val accountLabel = row.accountName ?: "—"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(row.date.format(dtFmt), color = sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.width(44.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(categoryLabel, color = sw.ink,
                style = SwType.Caption.copy(fontSize = 11.sp),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (row.note != null) {
                Text(row.note, color = sw.inkMuted,
                    style = SwType.Caption.copy(fontSize = 10.sp),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (row.kategori != null && row.planItemId == null) {
                Text("tidak cocok Plan", color = sw.warning,
                    style = SwType.Caption.copy(fontSize = 10.sp))
            }
        }
        Text(accountLabel,
            color = if (!accountResolved && row.accountName != null) sw.warning else sw.inkSubtle,
            style = SwType.Caption.copy(fontSize = 10.sp),
            modifier = Modifier.width(56.dp),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(row.amount.toRupiahShort(),
            color = if (row.type == com.gustiadhitya.sakuwise.core.domain.model.TxnType.Income) sw.success else sw.ink,
            style = SwType.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold))
    }
}
