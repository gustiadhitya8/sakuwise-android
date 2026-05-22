package com.gustiadhitya.sakuwise.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toRelativeOrAbsolute
import com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCategoryDot
import com.gustiadhitya.sakuwise.core.designsystem.components.SwSectionLabel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwSortMenu
import com.gustiadhitya.sakuwise.core.designsystem.components.assetSortOptions
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.Transaction
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.ui.RupiahSign
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TransactionHistoryViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    onClose: () -> Unit = {},
    onEditTxn: (Transaction) -> Unit = {},
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val month by viewModel.month.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val accountNames by viewModel.accountNames.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val net = monthIncome - monthExpense

    var sortMode by remember { mutableStateOf(AssetSort.DATE_DESC) }
    val sorted = remember(transactions, sortMode) {
        transactions.sortedWith(
            when (sortMode) {
                AssetSort.DATE_DESC -> compareByDescending<Transaction> { it.date }.thenByDescending { it.createdAt }
                AssetSort.DATE_ASC -> compareBy<Transaction> { it.date }.thenBy { it.createdAt }
                AssetSort.AMOUNT_DESC -> compareByDescending { it.amount }
                AssetSort.AMOUNT_ASC -> compareBy { it.amount }
            },
        )
    }

    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id"))
    val nowMonth = YearMonth.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Kembali", tint = sw.ink)
            }
            Text(
                "Riwayat Transaksi",
                style = SwType.H3,
                color = sw.ink,
                modifier = Modifier.weight(1f),
            )
        }

        // Month navigator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SwSpace.pageH, vertical = 4.dp),
        ) {
            IconButton(onClick = { viewModel.prevMonth() }) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "Bulan sebelumnya", tint = sw.ink)
            }
            Text(
                month.format(monthFmt).replaceFirstChar { it.uppercase() },
                style = SwType.LabelStrong.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                color = sw.ink,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            val canGoNext = month < nowMonth
            IconButton(
                onClick = { viewModel.nextMonth() },
                enabled = canGoNext,
            ) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = "Bulan berikutnya",
                    tint = if (canGoNext) sw.ink else sw.inkMuted,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(4.dp))

            // Summary card
            SwCard(modifier = Modifier.padding(horizontal = SwSpace.pageH)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SummaryCell(
                            label = "Pemasukan",
                            value = monthIncome,
                            sign = RupiahSign.Positive,
                            color = sw.success,
                        )
                        SummaryCell(
                            label = "Pengeluaran",
                            value = monthExpense,
                            sign = RupiahSign.Negative,
                            color = sw.danger,
                        )
                        SummaryCell(
                            label = "Saldo Bersih",
                            value = net,
                            sign = if (net >= 0) RupiahSign.Positive else RupiahSign.Negative,
                            color = if (net >= 0) sw.success else sw.danger,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SwSpace.pageH),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(label = "Semua", selected = typeFilter == null, onClick = { viewModel.setTypeFilter(null) })
                FilterChip(label = "Masuk", selected = typeFilter == TxnType.Income, onClick = { viewModel.setTypeFilter(TxnType.Income) })
                FilterChip(label = "Keluar", selected = typeFilter == TxnType.Expense, onClick = { viewModel.setTypeFilter(TxnType.Expense) })
                FilterChip(label = "Transfer", selected = typeFilter == TxnType.Transfer, onClick = { viewModel.setTypeFilter(TxnType.Transfer) })
            }

            Spacer(Modifier.height(12.dp))

            // Transaction list header + sort
            Column(modifier = Modifier.padding(horizontal = SwSpace.pageH).padding(bottom = 14.dp)) {
                SwSectionLabel(
                    text = "Transaksi (${sorted.size})",
                    trailing = if (sorted.isNotEmpty()) {
                        {
                            SwSortMenu(
                                options = assetSortOptions(),
                                selected = sortMode,
                                onPick = { sortMode = it },
                            )
                        }
                    } else null,
                )

                if (sorted.isEmpty()) {
                    SwCard {
                        Text(
                            "Belum ada transaksi bulan ini.",
                            color = sw.inkMuted,
                            style = SwType.Body,
                        )
                    }
                } else {
                    SwCard(padding = PaddingValues(0.dp)) {
                        Column {
                            sorted.forEachIndexed { i, t ->
                                val divider = i < sorted.size - 1
                                val tone = when (t.type) {
                                    TxnType.Income -> sw.success
                                    TxnType.Transfer -> sw.info
                                    else -> sw.ink
                                }
                                val sign = when (t.type) {
                                    TxnType.Income -> RupiahSign.Positive
                                    TxnType.Expense, TxnType.DebtOutflow -> RupiahSign.Negative
                                    else -> RupiahSign.None
                                }
                                val isEditable = t.type == TxnType.Expense ||
                                    t.type == TxnType.Income ||
                                    t.type == TxnType.Transfer
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .let { m -> if (isEditable) m.clickable { onEditTxn(t) } else m }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                ) {
                                    SwCategoryDot(
                                        name = t.note ?: t.type.code(),
                                        sizeDp = 38,
                                        color = if (tone == sw.ink) null else tone,
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    val fallbackLabel = when (t.type) {
                                        TxnType.Income -> "Pemasukan"
                                        TxnType.Expense -> "Pengeluaran"
                                        TxnType.Transfer -> "Transfer"
                                        TxnType.DebtInflow -> "Utang Masuk"
                                        TxnType.DebtOutflow -> "Cicilan Utang"
                                        TxnType.Reconciliation -> "Rekonsiliasi"
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                t.note ?: fallbackLabel,
                                                color = sw.ink,
                                                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f),
                                            )
                                            if (t.photoBlob != null) {
                                                Spacer(Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Outlined.CameraAlt,
                                                    contentDescription = "OCR",
                                                    tint = sw.inkMuted,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                t.date.toRelativeOrAbsolute(),
                                                color = sw.inkSubtle,
                                                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                                            )
                                            Box(
                                                Modifier
                                                    .size(2.dp)
                                                    .clip(CircleShape)
                                                    .background(sw.inkSubtle),
                                            )
                                            Text(
                                                accountNames[t.sourceAccountId] ?: "—",
                                                color = sw.inkSubtle,
                                                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    RupiahText(
                                        value = t.amount,
                                        sign = sign,
                                        short = true,
                                        style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                        color = tone,
                                    )
                                }
                                if (divider) {
                                    Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: Long,
    sign: RupiahSign,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = SwType.LabelSmall.copy(fontSize = 11.sp), color = SwTheme.colors.inkMuted)
        Spacer(Modifier.height(2.dp))
        RupiahText(
            value = value,
            sign = sign,
            short = true,
            style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val bg = if (selected) sw.primary else sw.surface
    val textColor = if (selected) sw.onPrimary else sw.inkMuted
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
            color = textColor,
        )
    }
}
