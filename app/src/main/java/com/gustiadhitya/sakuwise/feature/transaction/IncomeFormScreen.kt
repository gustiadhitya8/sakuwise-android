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
import androidx.compose.material.icons.outlined.Category
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
import com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton
import com.gustiadhitya.sakuwise.feature.transaction.ui.IncomeCategoryPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.TxnFormShell
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel

private enum class IncomePicker { Account, Date, Category }

@Composable
fun IncomeFormScreen(
    onClose: () -> Unit,
    viewModel: TxnFormViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.incomeCategories.collectAsState()
    var picker by remember { mutableStateOf<IncomePicker?>(null) }

    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    val account = accounts.firstOrNull { it.id == state.accountId }

    TxnFormShell(
        title = stringResource(R.string.txn_income_title),
        heroBg = sw.success,
        heroFg = sw.onPrimary,
        heroLabel = stringResource(R.string.txn_income_amount_label),
        amount = state.amount,
        onAmountChange = viewModel::setAmount,
        heroSubtitle = account?.let { "ke akun ${it.name}" },
        onCancel = onClose,
        saveLabel = "Simpan",
        saveEnabled = state.amount > 0 && state.accountId != null
            && state.incomeCategoryId != null && !state.saving,
        onSave = viewModel::submitIncome,
    ) {
        FieldButton(
            label = stringResource(R.string.txn_field_category_source),
            value = state.incomeCategoryName.orEmpty(),
            placeholder = stringResource(R.string.txn_field_category_source_placeholder),
            required = true,
            leadingIcon = Icons.Outlined.Category,
            onClick = { picker = IncomePicker.Category },
        )
        FieldButton(
            label = stringResource(R.string.txn_field_account_dest),
            value = account?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_field_account_placeholder),
            required = true,
            leadingIcon = Icons.Outlined.AccountBalanceWallet,
            onClick = { picker = IncomePicker.Account },
        )
        FieldButton(
            label = stringResource(R.string.txn_field_date),
            value = state.date.toAbsoluteId(),
            leadingIcon = Icons.Outlined.CalendarToday,
            onClick = { picker = IncomePicker.Date },
        )
        SwField(
            value = state.note,
            onValueChange = viewModel::setNote,
            label = stringResource(R.string.txn_field_note),
            placeholder = stringResource(R.string.txn_field_note_income_placeholder),
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
                Text(stringResource(R.string.txn_income_recurring_label), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                Text(
                    stringResource(R.string.txn_income_recurring_hint),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
            Switch(
                checked = state.recurringIncome,
                onCheckedChange = viewModel::setRecurringIncome,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = sw.onPrimary,
                    checkedTrackColor = sw.primary,
                ),
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    when (picker) {
        IncomePicker.Account -> AccountPickerSheet(
            accounts = accounts,
            selectedId = state.accountId,
            onPick = { viewModel.setAccount(it.id) },
            onDismiss = { picker = null },
        )
        IncomePicker.Date -> DatePickerSheet(
            selected = state.date,
            onPick = viewModel::setDate,
            onDismiss = { picker = null },
        )
        IncomePicker.Category -> IncomeCategoryPickerSheet(
            categories = categories,
            selectedId = state.incomeCategoryId,
            onPick = { cat -> viewModel.setIncomeCategory(cat.id, cat.name) },
            onDismiss = { picker = null },
        )
        null -> Unit
    }
}
