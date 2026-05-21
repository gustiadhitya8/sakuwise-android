package com.gustiadhitya.sakuwise.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRelativeOrAbsolute
import com.gustiadhitya.sakuwise.core.common.toRupiah
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton
import com.gustiadhitya.sakuwise.feature.transaction.ui.PlanItemPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.TxnFormShell
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel

private enum class TransferPicker { From, To, Date, FeePlanItem }

@Composable
fun TransferFormScreen(
    onClose: () -> Unit,
    viewModel: TxnFormViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val planItems by viewModel.planItemOptions.collectAsState()
    var picker by remember { mutableStateOf<TransferPicker?>(null) }

    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    val from = accounts.firstOrNull { it.id == state.accountId }
    val to = accounts.firstOrNull { it.id == state.destAccountId }
    val subtitle = when {
        from != null && to != null -> "${from.name} → ${to.name}"
        else -> null
    }

    TxnFormShell(
        title = stringResource(R.string.txn_transfer_title),
        heroBg = sw.info,
        heroFg = sw.onPrimary,
        heroLabel = stringResource(R.string.txn_transfer_amount_label),
        amount = state.amount,
        onAmountChange = viewModel::setAmount,
        heroSubtitle = subtitle,
        onCancel = onClose,
        saveLabel = "Simpan",
        saveEnabled = state.amount > 0 && state.accountId != null &&
            state.destAccountId != null && state.accountId != state.destAccountId && !state.saving,
        onSave = viewModel::submitTransfer,
    ) {
        FieldButton(
            label = stringResource(R.string.txn_transfer_from),
            value = from?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_transfer_from_placeholder),
            required = true,
            subtitle = from?.let {
                stringResource(R.string.txn_field_account_balance_format,
                    it.initialBalance.toRupiah())
            },
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, modifier = Modifier.size(16.dp))
                }
            },
            onClick = { picker = TransferPicker.From },
        )
        FieldButton(
            label = stringResource(R.string.txn_transfer_to),
            value = to?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_transfer_to_placeholder),
            required = true,
            subtitle = to?.let {
                stringResource(R.string.txn_field_account_balance_format,
                    it.initialBalance.toRupiah())
            },
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip {
                    Icon(Icons.Outlined.AccountBalanceWallet, null, modifier = Modifier.size(16.dp))
                }
            },
            onClick = { picker = TransferPicker.To },
        )
        // Swap button — compact horizontal row, NOT a field (proto pattern).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.primaryContainer)
                .clickable { viewModel.swap() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Icon(Icons.Outlined.SwapHoriz, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.txn_transfer_swap),
                color = sw.onPrimaryContainer,
                style = SwType.LabelStrong.copy(fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold))
        }
        Spacer(Modifier.height(14.dp))
        FieldButton(
            label = stringResource(R.string.txn_field_date),
            value = state.date.toAbsoluteId(),
            subtitle = state.date.toRelativeOrAbsolute(),
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip {
                    Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(16.dp))
                }
            },
            onClick = { picker = TransferPicker.Date },
        )
        // Transfer fee field — per prototype, fee is treated as expense
        SwField(
            value = if (state.transferFee == 0L) "" else state.transferFee.toString(),
            onValueChange = { v -> viewModel.setFee(v.filter { it.isDigit() }.toLongOrNull() ?: 0L) },
            label = stringResource(R.string.txn_transfer_fee),
            prefix = "Rp", rupiah = true, placeholder = "0",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        )
        // When the user enters a fee, surface a Plan Item picker (PRD §7.4 —
        // fee must count as an expense against the assigned plan item). The
        // picker stays hidden until fee > 0 so the form doesn't gain noise
        // for fee-less transfers.
        if (state.transferFee > 0L) {
            FieldButton(
                label = stringResource(R.string.txn_transfer_fee_plan_item),
                value = state.feePlanItemName.orEmpty(),
                placeholder = stringResource(R.string.txn_transfer_fee_plan_item_placeholder),
                leadingIcon = Icons.Outlined.Checklist,
                onClick = { picker = TransferPicker.FeePlanItem },
            )
        }
        SwField(
            value = state.note,
            onValueChange = viewModel::setNote,
            label = stringResource(R.string.txn_field_note),
            placeholder = stringResource(R.string.txn_field_note_transfer_placeholder),
        )
        if (from != null && to != null && state.amount > 0) {
            Spacer(Modifier.height(8.dp))
            TransferSummary(
                fromName = from.name, toName = to.name,
                amount = state.amount, fee = state.transferFee,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    when (picker) {
        TransferPicker.From -> AccountPickerSheet(
            accounts = accounts,
            selectedId = state.accountId,
            excludeId = state.destAccountId,
            onPick = { viewModel.setAccount(it.id) },
            onDismiss = { picker = null },
        )
        TransferPicker.To -> AccountPickerSheet(
            accounts = accounts,
            selectedId = state.destAccountId,
            excludeId = state.accountId,
            onPick = { viewModel.setDestAccount(it.id) },
            onDismiss = { picker = null },
        )
        TransferPicker.Date -> DatePickerSheet(
            selected = state.date,
            onPick = viewModel::setDate,
            onDismiss = { picker = null },
        )
        TransferPicker.FeePlanItem -> PlanItemPickerSheet(
            grouped = planItems,
            selectedId = state.feePlanItemId,
            onPick = { viewModel.setFeePlanItem(it.id, it.name) },
            onDismiss = { picker = null },
        )
        null -> Unit
    }
}

@Composable
private fun TransferSummary(fromName: String, toName: String, amount: Long, fee: Long) {
    val sw = SwTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(sw.infoSoft)
            .padding(12.dp),
    ) {
        SummaryRow(fromName, "−", amount + fee, sw.danger)
        Spacer(Modifier.height(4.dp))
        SummaryRow(toName, "+", amount, sw.success)
    }
}

@Composable
private fun SummaryRow(name: String, sign: String, value: Long, tint: androidx.compose.ui.graphics.Color) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(name, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f))
        Text(sign, color = tint,
            style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.width(2.dp))
        RupiahText(value = value, short = true,
            style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = tint)
    }
}
