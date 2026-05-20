package com.gustiadhitya.sakuwise.feature.settings.export

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PictureAsPdf
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
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun ExportPdfScreen(
    onBack: () -> Unit,
    viewModel: ExportPdfViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var period by remember { mutableStateOf(ExportPeriod.CurrentMonth) }

    SimpleSettingsScreen(
        title = stringResource(R.string.export_pdf_title),
        onBack = onBack,
    ) {
        val sw = SwTheme.colors

        Text(
            stringResource(R.string.export_pdf_intro),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))

        // ── Period selector ─────────────────────────────────────
        Text(
            stringResource(R.string.export_pdf_period_label).uppercase(),
            color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                PeriodRow(
                    label = stringResource(R.string.export_pdf_period_current_month),
                    selected = period == ExportPeriod.CurrentMonth,
                    onClick = { period = ExportPeriod.CurrentMonth },
                )
                PeriodRow(
                    label = stringResource(R.string.export_pdf_period_last_30d),
                    selected = period == ExportPeriod.Last30Days,
                    onClick = { period = ExportPeriod.Last30Days },
                )
                PeriodRow(
                    label = stringResource(R.string.export_pdf_period_this_year),
                    selected = period == ExportPeriod.ThisYear,
                    onClick = { period = ExportPeriod.ThisYear },
                )
                PeriodRow(
                    label = stringResource(R.string.export_pdf_period_all_time),
                    selected = period == ExportPeriod.AllTime,
                    onClick = { period = ExportPeriod.AllTime },
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        // ── Action / state cards ────────────────────────────────
        when (val s = state) {
            is ExportState.Idle -> {
                SwButton(
                    text = stringResource(R.string.export_pdf_action),
                    onClick = { viewModel.export(period) },
                    variant = SwButtonVariant.Primary,
                    leading = {
                        Icon(
                            Icons.Outlined.PictureAsPdf,
                            contentDescription = null,
                            tint = sw.onPrimary,
                        )
                    },
                )
            }
            is ExportState.Working -> {
                SwButton(
                    text = stringResource(R.string.export_pdf_action_working),
                    onClick = {},
                    variant = SwButtonVariant.Primary,
                    enabled = false,
                )
            }
            is ExportState.Ready -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(sw.successSoft)
                        .padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null,
                                tint = sw.success,
                            )
                            Spacer(Modifier.height(0.dp))
                            Text(
                                "  " + stringResource(R.string.export_pdf_ready_title),
                                color = sw.success,
                                style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.export_pdf_ready_body),
                            color = sw.ink,
                            style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwButton(
                    text = stringResource(R.string.export_pdf_open),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(s.uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.export_pdf_open),
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                    variant = SwButtonVariant.Primary,
                )
                Spacer(Modifier.height(8.dp))
                SwButton(
                    text = stringResource(R.string.export_pdf_share),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, s.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(R.string.export_pdf_share),
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                    variant = SwButtonVariant.Outline,
                )
                Spacer(Modifier.height(8.dp))
                SwButton(
                    text = stringResource(R.string.export_pdf_again),
                    onClick = { viewModel.clear() },
                    variant = SwButtonVariant.Ghost,
                )
            }
            is ExportState.Failure -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(sw.dangerSoft)
                        .padding(20.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = sw.danger,
                            )
                            Text(
                                "  " + stringResource(R.string.export_pdf_failed_title),
                                color = sw.danger,
                                style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            s.message,
                            color = sw.ink,
                            style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwButton(
                    text = stringResource(R.string.export_pdf_retry),
                    onClick = { viewModel.export(period) },
                    variant = SwButtonVariant.Primary,
                )
                Spacer(Modifier.height(8.dp))
                SwButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { viewModel.clear() },
                    variant = SwButtonVariant.Ghost,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.export_pdf_footnote),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 11.sp),
        )
    }
}

@Composable
private fun PeriodRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            label,
            color = sw.ink,
            style = SwType.LabelStrong.copy(
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) sw.primary else sw.track)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                if (selected) "✓" else " ",
                color = if (selected) sw.onPrimary else sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}
