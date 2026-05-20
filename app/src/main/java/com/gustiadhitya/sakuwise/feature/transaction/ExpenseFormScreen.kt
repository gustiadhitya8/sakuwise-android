package com.gustiadhitya.sakuwise.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DebtPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton
import com.gustiadhitya.sakuwise.feature.transaction.ui.PlanItemPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.TxnFormShell
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel

private enum class ExpensePicker { Account, PlanItem, Date, Debt }

@Composable
fun ExpenseFormScreen(
    onClose: () -> Unit,
    viewModel: TxnFormViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val planItems by viewModel.planItemOptions.collectAsState()
    val openDebts by viewModel.openOwedDebts.collectAsState()
    var picker by remember { mutableStateOf<ExpensePicker?>(null) }

    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    val account = accounts.firstOrNull { it.id == state.accountId }

    TxnFormShell(
        title = stringResource(R.string.txn_expense_title),
        heroBg = sw.danger,
        heroFg = sw.onPrimary,
        heroLabel = stringResource(R.string.txn_expense_amount_label),
        amount = state.amount,
        onAmountChange = viewModel::setAmount,
        heroSubtitle = state.planItemName?.let { "Plan item · $it" },
        onCancel = onClose,
        saveLabel = "Simpan",
        saveEnabled = state.amount > 0 && state.accountId != null && state.planItemId != null && !state.saving,
        onSave = viewModel::submitExpense,
    ) {
        FieldButton(
            label = stringResource(R.string.txn_field_plan_item),
            value = state.planItemName.orEmpty(),
            placeholder = stringResource(R.string.txn_field_plan_item_placeholder),
            required = true,
            leadingIcon = Icons.Outlined.Checklist,
            onClick = { picker = ExpensePicker.PlanItem },
        )
        FieldButton(
            label = stringResource(R.string.txn_field_account),
            value = account?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_field_account_placeholder),
            required = true,
            leadingIcon = Icons.Outlined.AccountBalanceWallet,
            onClick = { picker = ExpensePicker.Account },
        )
        FieldButton(
            label = stringResource(R.string.txn_field_date),
            value = state.date.toAbsoluteId(),
            leadingIcon = Icons.Outlined.CalendarToday,
            onClick = { picker = ExpensePicker.Date },
        )
        SwField(
            value = state.note,
            onValueChange = viewModel::setNote,
            label = stringResource(R.string.txn_field_note),
            placeholder = stringResource(R.string.txn_field_note_expense_placeholder),
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(sw.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.txn_link_debt_label), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                Text(
                    state.debtLabel?.let {
                        stringResource(R.string.txn_link_debt_attached_format, it)
                    } ?: stringResource(R.string.txn_link_debt_hint),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
            Switch(
                checked = state.debtId != null,
                onCheckedChange = { on ->
                    if (on) picker = ExpensePicker.Debt
                    else viewModel.setDebt(null, null)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = sw.onPrimary,
                    checkedTrackColor = sw.primary,
                ),
            )
        }
        if (state.debtId != null) {
            FieldButton(
                label = "Hutang",
                value = state.debtLabel.orEmpty(),
                placeholder = "Pilih hutang…",
                leadingIcon = Icons.Outlined.Link,
                onClick = { picker = ExpensePicker.Debt },
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    when (picker) {
        ExpensePicker.Account -> AccountPickerSheet(
            accounts = accounts,
            selectedId = state.accountId,
            onPick = { viewModel.setAccount(it.id) },
            onDismiss = { picker = null },
        )
        ExpensePicker.PlanItem -> PlanItemPickerSheet(
            grouped = planItems,
            selectedId = state.planItemId,
            onPick = { viewModel.setPlanItem(it.id, it.name) },
            onDismiss = { picker = null },
        )
        ExpensePicker.Date -> DatePickerSheet(
            selected = state.date,
            onPick = viewModel::setDate,
            onDismiss = { picker = null },
        )
        ExpensePicker.Debt -> DebtPickerSheet(
            debts = openDebts,
            paidPerDebt = emptyMap(), // outstanding shown as principal until V1.1 aggregates
            selectedId = state.debtId,
            onPick = { d -> viewModel.setDebt(d.id, d.counterparty) },
            onDismiss = { picker = null },
        )
        null -> Unit
    }
}
