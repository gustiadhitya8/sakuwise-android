package com.gustiadhitya.sakuwise.feature.transaction

import androidx.compose.foundation.background
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
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton
import com.gustiadhitya.sakuwise.feature.transaction.ui.TxnFormShell
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel

private enum class TransferPicker { From, To, Date }

@Composable
fun TransferFormScreen(
    onClose: () -> Unit,
    viewModel: TxnFormViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
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
            leadingIcon = Icons.Outlined.AccountBalanceWallet,
            onClick = { picker = TransferPicker.From },
        )
        FieldButton(
            label = stringResource(R.string.txn_transfer_to),
            value = to?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_transfer_to_placeholder),
            required = true,
            leadingIcon = Icons.Outlined.AccountBalanceWallet,
            onClick = { picker = TransferPicker.To },
        )
        FieldButton(
            label = stringResource(R.string.txn_transfer_swap),
            value = "↔",
            leadingIcon = Icons.Outlined.SwapHoriz,
            onClick = viewModel::swap,
        )
        FieldButton(
            label = stringResource(R.string.txn_field_date),
            value = state.date.toAbsoluteId(),
            leadingIcon = Icons.Outlined.CalendarToday,
            onClick = { picker = TransferPicker.Date },
        )
        // Transfer fee field — per prototype, fee is treated as expense
        SwField(
            value = if (state.transferFee == 0L) "" else state.transferFee.toString(),
            onValueChange = { v -> viewModel.setFee(v.filter { it.isDigit() }.toLongOrNull() ?: 0L) },
            label = stringResource(R.string.txn_transfer_fee),
            prefix = "Rp", placeholder = "0",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        )
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
