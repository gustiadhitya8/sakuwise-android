package com.gustiadhitya.sakuwise.feature.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRelativeOrAbsolute
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.core.domain.model.AllocationId
import com.gustiadhitya.sakuwise.core.domain.model.IncomeCategory
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.plan.displayName
import java.time.LocalDate

private fun AccountType.icon(): ImageVector = when (this) {
    AccountType.Cash -> Icons.Outlined.Payments
    AccountType.Bank -> Icons.Outlined.AccountBalance
    AccountType.EWallet, AccountType.Other -> Icons.Outlined.AccountBalanceWallet
}

/** Localized account-type label. Uses stringResource so it flips per current locale. */
@Composable
fun AccountType.displayName(): String = stringResource(
    when (this) {
        AccountType.Cash -> R.string.account_type_cash
        AccountType.Bank -> R.string.account_type_bank
        AccountType.EWallet -> R.string.account_type_ewallet
        AccountType.Other -> R.string.account_type_other
    },
)

/** Generic shared sheet shell — drag handle, title, scrollable body. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwPickerSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sw = SwTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = sw.surface,
        contentColor = sw.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sw.borderStrong),
            )
        },
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)) {
            Text(title, color = sw.ink,
                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Compact filter chip used inside picker sheets. Active = filled primary.
 */
@Composable
private fun FilterPill(label: String, active: Boolean, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) sw.primary else sw.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Text(
            label,
            color = if (active) sw.onPrimary else sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun AccountPickerSheet(
    accounts: List<Account>,
    selectedId: String?,
    excludeId: String? = null,
    onPick: (Account) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.picker_choose_account), onDismiss = onDismiss) {
        LazyColumn {
            items(accounts.filter { it.id != excludeId }) { acc ->
                val isSelected = acc.id == selectedId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPick(acc); onDismiss() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(sw.primaryContainer),
                    ) {
                        Icon(acc.type.icon(), null, tint = sw.onPrimaryContainer, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                    Column(Modifier.weight(1f)) {
                        Text(acc.name, color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
                        Text(acc.type.displayName(), color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    if (isSelected) {
                        Icon(Icons.Outlined.Check, null, tint = sw.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DebtPickerSheet(
    debts: List<com.gustiadhitya.sakuwise.core.domain.model.Debt>,
    paidPerDebt: Map<String, Long>,
    selectedId: String?,
    onPick: (com.gustiadhitya.sakuwise.core.domain.model.Debt) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.sheet_picker_debt_title), onDismiss = onDismiss) {
        if (debts.isEmpty()) {
            Text(
                "Belum ada hutang terbuka. Tambah dulu di tab Aset → Hutang.",
                color = sw.inkMuted, style = SwType.Body,
            )
            return@SwPickerSheet
        }
        LazyColumn {
            items(debts, key = { it.id }) { debt ->
                val isSelected = debt.id == selectedId
                val paid = paidPerDebt[debt.id] ?: 0L
                val outstanding = (debt.principal - paid).coerceAtLeast(0L)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPick(debt); onDismiss() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(debt.counterparty, color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text("Sisa ${outstanding.toRupiahShort()}",
                            color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    if (isSelected) {
                        Icon(Icons.Outlined.Check, null, tint = sw.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

data class PlanItemRowOption(
    val id: String,
    val name: String,
    val categoryName: String,
    val allocationName: String,
    val plan: Long,
    val used: Long,
)

@Composable
fun PlanItemPickerSheet(
    grouped: List<PlanItemRowOption>,
    selectedId: String?,
    onPick: (PlanItemRowOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    // Active allocation filter chip — Semua / Needs / Wants / Investment.
    // The selection scrolls horizontally to handle small viewports.
    val filterState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<AllocationId?>(null)
    }
    val filter = filterState.value
    SwPickerSheet(title = stringResource(R.string.picker_choose_plan_item), onDismiss = onDismiss) {
        if (grouped.isEmpty()) {
            Text(
                stringResource(R.string.picker_no_plan_items),
                color = sw.inkMuted, style = SwType.Body,
            )
            return@SwPickerSheet
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill(
                label = stringResource(R.string.plan_filter_all),
                active = filter == null,
                onClick = { filterState.value = null },
            )
            listOf(
                AllocationId.Needs to stringResource(R.string.plan_filter_needs),
                AllocationId.Wants to stringResource(R.string.plan_filter_wants),
                AllocationId.Invest to stringResource(R.string.plan_filter_invest),
            ).forEach { (alloc, label) ->
                FilterPill(label = label, active = filter == alloc, onClick = { filterState.value = alloc })
            }
        }
        val visible = if (filter == null) grouped
        else grouped.filter { AllocationId.fromName(it.allocationName) == filter }
        if (visible.isEmpty()) {
            Text(
                stringResource(R.string.picker_no_plan_items_for_filter),
                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(vertical = 8.dp),
            )
            return@SwPickerSheet
        }
        LazyColumn {
            visible.groupBy { it.allocationName }.forEach { (alloc, items) ->
                item(key = "header-$alloc") {
                    Text(AllocationId.fromName(alloc).displayName().uppercase(),
                        color = sw.inkSubtle,
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                }
                items(items, key = { it.id }) { item ->
                    val isSelected = item.id == selectedId
                    val itemAllocLabel = AllocationId.fromName(item.allocationName).displayName()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(item); onDismiss() }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                            Text("${item.categoryName} · $itemAllocLabel",
                                color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                        RupiahText(value = item.used, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        Text(" / ", color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        RupiahText(value = item.plan, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp),
                            color = sw.inkSubtle)
                        if (isSelected) {
                            Spacer(Modifier.size(width = 8.dp, height = 1.dp))
                            Icon(Icons.Outlined.Check, null, tint = sw.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Material 3 calendar date picker.
 *
 * Allows past dates and today's date; the (current) txn forms scope to historical
 * entries. Pass [allowFuture]=true for forms where scheduling makes sense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerSheet(
    selected: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    allowFuture: Boolean = false,
) {
    val sw = SwTheme.colors
    val todayUtcMillis = LocalDate.now()
        .atStartOfDay(java.time.ZoneOffset.UTC)
        .toInstant().toEpochMilli()
    val selectedUtcMillis = selected
        .atStartOfDay(java.time.ZoneOffset.UTC)
        .toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedUtcMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                allowFuture || utcTimeMillis <= todayUtcMillis
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    val picked = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                    onPick(picked)
                }
                onDismiss()
            }) {
                Text(stringResource(R.string.action_save), color = sw.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = sw.inkMuted)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = sw.surface,
        ),
    ) {
        DatePicker(
            state = state,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = sw.surface,
                selectedDayContainerColor = sw.primary,
                selectedDayContentColor = sw.onPrimary,
                todayDateBorderColor = sw.primary,
                todayContentColor = sw.primary,
            ),
        )
    }
}

/** Icon hint per income category name — matches prototype 38-form-income-category-picker.png. */
private fun IncomeCategory.icon(): ImageVector {
    val n = name.lowercase()
    return when {
        "gaji" in n -> Icons.Outlined.TrendingUp
        "bonus" in n || "thr" in n -> Icons.Outlined.AutoAwesome
        "samping" in n -> Icons.Outlined.NorthEast
        else -> Icons.Outlined.MoreHoriz
    }
}

@Composable
fun IncomeCategoryPickerSheet(
    categories: List<IncomeCategory>,
    selectedId: String?,
    onPick: (IncomeCategory) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.sheet_picker_income_category_title), onDismiss = onDismiss) {
        if (categories.isEmpty()) {
            Text("Belum ada kategori sumber.", color = sw.inkMuted, style = SwType.Body)
            return@SwPickerSheet
        }
        LazyColumn {
            items(categories, key = { it.id }) { cat ->
                val isSelected = cat.id == selectedId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) sw.primaryContainer.copy(alpha = 0.5f)
                            else androidx.compose.ui.graphics.Color.Transparent,
                        )
                        .clickable { onPick(cat); onDismiss() }
                        .padding(vertical = 10.dp, horizontal = 10.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(sw.primaryContainer),
                    ) {
                        Icon(cat.icon(), null, tint = sw.onPrimaryContainer,
                            modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(width = 14.dp, height = 1.dp))
                    Text(cat.name, color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Outlined.Check, null, tint = sw.primary,
                            modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
