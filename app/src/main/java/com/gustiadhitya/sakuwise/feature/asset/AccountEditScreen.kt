package com.gustiadhitya.sakuwise.feature.asset

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountEditViewModel
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen

@Composable
fun AccountEditScreen(
    accountId: String?,
    onClose: () -> Unit,
    viewModel: AccountEditViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    LaunchedEffect(accountId) { viewModel.loadFor(accountId) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onClose() }

    val typeOptions = listOf(
        Triple(AccountType.Cash, stringResource(R.string.onb_type_cash), Icons.Outlined.Payments),
        Triple(AccountType.Bank, stringResource(R.string.onb_type_bank), Icons.Outlined.AccountBalance),
        Triple(AccountType.EWallet, stringResource(R.string.onb_type_ewallet), Icons.Outlined.AccountBalanceWallet),
    )

    SimpleSettingsScreen(
        title = if (state.id == null) stringResource(R.string.account_edit_new_title)
        else stringResource(R.string.account_edit_edit_title),
        onBack = onClose,
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (state.name.isNotBlank() && !state.saving) sw.primary else sw.primary.copy(alpha = 0.4f))
                    .clickable(enabled = state.name.isNotBlank() && !state.saving, onClick = viewModel::submit)
                    .padding(horizontal = 14.dp),
            ) {
                Text(stringResource(R.string.action_save), color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
            }
        },
    ) {
        SwField(
            value = state.name,
            onValueChange = viewModel::setName,
            label = stringResource(R.string.account_edit_name_label),
            placeholder = stringResource(R.string.account_edit_name_placeholder),
        )
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.account_edit_type_label), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            typeOptions.forEach { (t, label, icon) ->
                val active = state.type == t
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) sw.primaryContainer else sw.surface)
                        .border(1.5.dp, if (active) sw.primary else sw.border, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setType(t) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                ) {
                    Icon(icon, null, tint = sw.ink, modifier = Modifier.size(22.dp))
                    Text(label, color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 12.sp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        SwField(
            value = if (state.initialBalance == 0L) "" else state.initialBalance.toRupiah(prefix = ""),
            onValueChange = { raw ->
                viewModel.setBalance(raw.filter { it.isDigit() }.toLongOrNull() ?: 0L)
            },
            label = if (state.id == null) stringResource(R.string.account_edit_balance_label_new)
            else stringResource(R.string.account_edit_balance_label_existing),
            prefix = "Rp", rupiah = true,
            placeholder = "0",
            hint = if (state.id == null) stringResource(R.string.account_edit_balance_hint_new)
            else stringResource(R.string.account_edit_balance_hint_existing),
            keyboardType = KeyboardType.Number,
        )

        if (state.id != null) {
            Spacer(Modifier.height(14.dp))
            SwCard(padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.account_edit_archive_title), color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text(stringResource(R.string.account_edit_archive_sub),
                            color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    Switch(
                        checked = state.archived,
                        onCheckedChange = viewModel::setArchived,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = sw.onPrimary, checkedTrackColor = sw.primary,
                        ),
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
