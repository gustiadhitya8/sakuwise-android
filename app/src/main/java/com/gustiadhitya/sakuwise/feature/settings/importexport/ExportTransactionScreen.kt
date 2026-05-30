package com.gustiadhitya.sakuwise.feature.settings.importexport

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.settings.export.ExportPeriod
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun ExportTransactionScreen(
    onBack: () -> Unit,
    viewModel: ExportTransactionViewModel = hiltViewModel(),
) {
    val sw      = SwTheme.colors
    val state   by viewModel.state.collectAsState()
    val context = LocalContext.current

    var period by remember { mutableStateOf(ExportPeriod.CurrentMonth) }
    var format by remember { mutableStateOf(ExportFormat.Csv) }

    SimpleSettingsScreen(title = stringResource(R.string.export_title), onBack = onBack) {

        Text(
            "Export semua transaksi ke file CSV atau XLSX untuk dibuka di Excel / Google Sheets.",
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))

        // ── Format selector ─────────────────────────────────────
        Text(stringResource(R.string.export_format_label), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormatChip(
                label = "CSV",
                icon = { Icon(Icons.Outlined.Description, null, tint = it, modifier = Modifier.size(16.dp)) },
                selected = format == ExportFormat.Csv,
                onClick = { format = ExportFormat.Csv },
                modifier = Modifier.weight(1f),
            )
            FormatChip(
                label = "XLSX",
                icon = { Icon(Icons.Outlined.GridOn, null, tint = it, modifier = Modifier.size(16.dp)) },
                selected = format == ExportFormat.Xlsx,
                onClick = { format = ExportFormat.Xlsx },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(18.dp))

        // ── Period selector ─────────────────────────────────────
        Text(stringResource(R.string.export_period_label), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))

        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                ExportPeriodRow("Bulan Ini", period == ExportPeriod.CurrentMonth) { period = ExportPeriod.CurrentMonth }
                ExportPeriodRow("30 Hari Terakhir", period == ExportPeriod.Last30Days) { period = ExportPeriod.Last30Days }
                ExportPeriodRow("Tahun Ini", period == ExportPeriod.ThisYear) { period = ExportPeriod.ThisYear }
                ExportPeriodRow("Semua Waktu", period == ExportPeriod.AllTime) { period = ExportPeriod.AllTime }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── Action / state ──────────────────────────────────────
        when (val s = state) {
            is ExportTxnState.Idle -> {
                SwButton(
                    text = "Export ${format.name}",
                    onClick = { viewModel.export(period, format) },
                    leading = {
                        Icon(Icons.Outlined.TableChart, null,
                            tint = sw.onPrimary, modifier = Modifier.size(18.dp))
                    },
                )
            }

            is ExportTxnState.Working -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = sw.primary,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.export_preparing), color = sw.inkMuted,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp))
                }
            }

            is ExportTxnState.Ready -> {
                val mime = if (s.format == ExportFormat.Csv) "text/csv"
                           else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(sw.successSoft).padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Description, null,
                                tint = sw.success, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.export_ready_title), color = sw.success,
                                style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.export_ready_body_format, s.count, s.format.name),
                            color = sw.ink, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwButton(
                    text = "Bagikan File",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = mime
                            putExtra(Intent.EXTRA_STREAM, s.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(intent, "Bagikan File").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    leading = {
                        Icon(Icons.Outlined.Share, null,
                            tint = sw.onPrimary, modifier = Modifier.size(18.dp))
                    },
                )
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Buka File",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(s.uri, mime)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(intent, "Buka dengan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    variant = SwButtonVariant.Outline,
                )
                Spacer(Modifier.height(8.dp))
                SwButton(text = "Export Ulang", onClick = { viewModel.clear() }, variant = SwButtonVariant.Ghost)
            }

            is ExportTxnState.Failure -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(sw.dangerSoft).padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.ErrorOutline, null,
                                tint = sw.danger, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.export_error_title), color = sw.danger,
                                style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(s.message, color = sw.ink, style = SwType.LabelSmall.copy(fontSize = 12.sp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwButton(text = "Coba Lagi", onClick = { viewModel.export(period, format) })
                Spacer(Modifier.height(8.dp))
                SwButton(text = stringResource(R.string.action_cancel), onClick = { viewModel.clear() }, variant = SwButtonVariant.Ghost)
            }
        }
    }
}

@Composable
private fun FormatChip(
    label: String,
    icon: @Composable (tint: androidx.compose.ui.graphics.Color) -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    val bg     = if (selected) sw.primary else sw.surface
    val border = if (selected) sw.primary else sw.border
    val fg     = if (selected) sw.onPrimary else sw.inkMuted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        icon(fg)
        Spacer(Modifier.width(6.dp))
        Text(label, color = fg,
            style = SwType.LabelStrong.copy(fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium))
    }
}

@Composable
private fun ExportPeriodRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(label, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(50))
                .background(if (selected) sw.primary else sw.track)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(if (selected) "✓" else " ",
                color = if (selected) sw.onPrimary else sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
        }
    }
}
