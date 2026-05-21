package com.gustiadhitya.sakuwise.feature.plan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import androidx.compose.ui.res.stringResource
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.rememberNotificationPermissionRequester
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.AllocationId
import com.gustiadhitya.sakuwise.core.domain.model.PlanItem
import com.gustiadhitya.sakuwise.core.domain.model.Recurrence
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.plan.viewmodel.PlanViewModel
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet

@Composable
fun PlanScreen(viewModel: PlanViewModel = hiltViewModel()) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val allPlans by viewModel.allPlans.collectAsState()
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    var addToCategory by remember { mutableStateOf<String?>(null) }
    var addCategoryToAlloc by remember { mutableStateOf<String?>(null) }
    var editItem by remember { mutableStateOf<PlanItem?>(null) }
    var deleteCategoryConfirm by remember { mutableStateOf<Pair<String, String>?>(null) }
    var actionSheetOpen by remember { mutableStateOf(false) }
    var incomeSheetOpen by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var monthPickerOpen by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf<AllocationId?>(null) } // null = Semua

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sw.bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = SwSpace.bottomBarClear),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SwSpace.pageH)
                .padding(top = 6.dp, bottom = 12.dp),
        ) {
            Text(stringResource(R.string.plan_title), color = sw.ink,
                style = SwType.H1.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.surface)
                    .border(1.dp, sw.border, RoundedCornerShape(12.dp))
                    .clickable { actionSheetOpen = true },
            ) { Icon(Icons.Outlined.MoreHoriz, "Aksi plan", tint = sw.ink, modifier = Modifier.size(20.dp)) }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = SwSpace.pageH, vertical = 4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(sw.primaryContainer)
                .clickable { monthPickerOpen = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Outlined.CalendarToday, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(14.dp))
            Text(
                state.plan?.label ?: stringResource(R.string.plan_no_plan),
                color = sw.onPrimaryContainer,
                style = SwType.Caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            )
            Icon(Icons.Outlined.ChevronRight, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.height(10.dp))

        // Per prototype screens-plan.jsx:53-70 — Pemasukan Diharapkan card BEFORE
        // the filter chips. Layout: section label + amount on the left, large
        // primaryContainer Edit pill on the right, SwBar, then a "Terpakai X /
        // dari Y" footer row.
        Column(modifier = Modifier.padding(horizontal = SwSpace.pageH)) {
            SwCard {
                Column {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.plan_expected_income),
                                color = sw.inkSubtle,
                                style = SwType.SectionLabel.copy(fontSize = 11.sp))
                            Spacer(Modifier.height(6.dp))
                            RupiahText(
                                value = state.plan?.expectedIncome ?: 0L,
                                // Proto size=26 weight=700; SwType.AmountL is
                                // already in that ballpark — pin explicitly so
                                // it doesn't drift if AmountL tweaks later.
                                style = SwType.AmountL.copy(fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold),
                                color = sw.ink,
                            )
                        }
                        // Big primaryContainer edit pill (32×32 r10) per proto
                        // line 60. Was previously a tiny inline 14sp icon.
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(sw.primaryContainer)
                                .clickable { incomeSheetOpen = true },
                        ) {
                            Icon(Icons.Outlined.Edit, "Ubah pemasukan",
                                tint = sw.onPrimaryContainer,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    val totalUsed = state.allocations.sumOf { it.used }
                    val totalPlan = state.allocations.sumOf { it.plan }.coerceAtLeast(1L)
                    SwBar(used = totalUsed, plan = totalPlan)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Terpakai ", color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 12.sp))
                            RupiahText(value = totalUsed, short = true,
                                style = SwType.Amount.copy(fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"),
                                color = sw.ink)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("dari ", color = sw.inkMuted,
                                style = SwType.LabelSmall.copy(fontSize = 12.sp))
                            RupiahText(value = totalPlan, short = true,
                                style = SwType.Amount.copy(fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"),
                                color = sw.ink)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Filter chips moved below the income card per prototype.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = SwSpace.pageH)
                .horizontalScroll(rememberScrollState()),
        ) {
            FilterChip(label = stringResource(R.string.plan_filter_all),
                selected = filter == null,
                accent = sw.ink, onClick = { filter = null })
            FilterChip(label = stringResource(R.string.plan_filter_needs),
                selected = filter == AllocationId.Needs,
                accent = sw.primary, onClick = { filter = AllocationId.Needs })
            FilterChip(label = stringResource(R.string.plan_filter_wants),
                selected = filter == AllocationId.Wants,
                accent = sw.accent, onClick = { filter = AllocationId.Wants })
            FilterChip(label = stringResource(R.string.plan_filter_invest),
                selected = filter == AllocationId.Invest,
                accent = sw.info, onClick = { filter = AllocationId.Invest })
        }
        Spacer(Modifier.height(14.dp))

        Column(modifier = Modifier.padding(horizontal = SwSpace.pageH)) {
            val visibleAllocations = if (filter == null) state.allocations
            else state.allocations.filter { AllocationId.fromName(it.allocation.name) == filter }
            visibleAllocations.forEach { row ->
                val a = row.allocation
                val allocId = AllocationId.fromName(a.name)
                val allocColor = when (allocId) {
                    AllocationId.Needs -> sw.primary
                    AllocationId.Wants -> sw.accent
                    AllocationId.Invest -> sw.info
                }
                val allocLabel = allocId.displayName()
                val allocUsed = row.categories.sumOf { it.used }
                val allocPlan = row.categories.sumOf { it.plan }
                // Allocation header per screens-plan.jsx:86-95: dot + name +
                // pct% on the left, used / plan on the right (tnum, used bolded
                // ink). Was missing the right-side totals before.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 18.dp, bottom = 10.dp,
                        start = 4.dp, end = 4.dp),
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(allocColor))
                    Text(allocLabel, color = sw.ink,
                        style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    Text("${a.targetPct}%", color = sw.inkSubtle,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"))
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RupiahText(value = allocUsed, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                            color = sw.ink)
                        Text(" / ", color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 12.sp))
                        RupiahText(value = allocPlan, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp,
                                fontFeatureSettings = "tnum"),
                            color = sw.inkMuted)
                    }
                }
                row.categories.forEach { cat ->
                    val isOpen = expanded[cat.category.id] != false
                    CategoryCard(
                        name = cat.category.name,
                        plan = cat.plan, used = cat.used,
                        allocColor = allocColor,
                        items = cat.items,
                        expanded = isOpen,
                        onToggle = { expanded[cat.category.id] = !isOpen },
                        onDeleteCategory = { deleteCategoryConfirm = cat.category.id to cat.category.name },
                        onEditItem = { pi -> editItem = pi.item },
                        onAddItem = { addToCategory = cat.category.id },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                // Always-visible add-category button — recovers from full delete
                Spacer(Modifier.height(4.dp))
                DashedAddCategoryButton(
                    text = stringResource(R.string.alloc_add_category_format, allocLabel),
                    accentColor = allocColor,
                    onClick = { addCategoryToAlloc = a.id },
                )
            }
            if (state.allocations.isEmpty() && !state.loading) {
                SwCard {
                    Column {
                        Text(
                            stringResource(R.string.plan_empty_no_allocs),
                            color = sw.inkMuted, style = SwType.Body,
                        )
                        Spacer(Modifier.height(12.dp))
                        SwButton(
                            text = stringResource(R.string.plan_apply_starter_btn),
                            onClick = { viewModel.applyStarterTemplateToCurrentPlan() },
                        )
                    }
                }
            }
        }
    }

    if (addCategoryToAlloc != null) {
        AddCategorySheet(
            onSave = { name ->
                viewModel.addCategoryToAllocation(addCategoryToAlloc!!, name)
                addCategoryToAlloc = null
            },
            onDismiss = { addCategoryToAlloc = null },
        )
    }
    if (deleteCategoryConfirm != null) {
        val (catId, catName) = deleteCategoryConfirm!!
        ConfirmDeleteCategorySheet(
            categoryName = catName,
            onConfirm = {
                viewModel.deleteCategory(catId)
                deleteCategoryConfirm = null
            },
            onDismiss = { deleteCategoryConfirm = null },
        )
    }
    // POST_NOTIFICATIONS: prompt lazily when a recurring plan item is saved.
    // Reminder scheduling is what *needs* the permission — asking on cold start
    // is premature and asking later (after save) is fine because the worker
    // posts the next reminder ≥1 day out.
    val requestNotifPerm = rememberNotificationPermissionRequester(onResult = {})
    if (addToCategory != null) {
        EditPlanItemSheet(
            existing = null,
            onSave = { _, name, amount, recurrence ->
                viewModel.addPlanItem(addToCategory!!, name, amount, recurrence)
                if (recurrence != Recurrence.OneOff) requestNotifPerm()
                addToCategory = null
            },
            onDelete = null,
            onDismiss = { addToCategory = null },
        )
    }
    if (editItem != null) {
        EditPlanItemSheet(
            existing = editItem,
            onSave = { item, name, amount, recurrence ->
                val wasRecurring = item!!.recurrence != Recurrence.OneOff
                viewModel.updatePlanItem(
                    item.copy(name = name, plannedAmount = amount, recurrence = recurrence),
                )
                if (!wasRecurring && recurrence != Recurrence.OneOff) requestNotifPerm()
                editItem = null
            },
            onDelete = { item -> viewModel.deletePlanItem(item.id); editItem = null },
            onDismiss = { editItem = null },
        )
    }
    var allocEditorOpen by remember { mutableStateOf(false) }
    if (actionSheetOpen) {
        PlanActionSheet(
            onApplyStarter = { viewModel.applyStarterTemplateToCurrentPlan(); actionSheetOpen = false },
            onSetIncome = { actionSheetOpen = false; incomeSheetOpen = true },
            onEditAllocations = { actionSheetOpen = false; allocEditorOpen = true },
            onRegenerateNext = { viewModel.regenerateNextPeriodPlan(); actionSheetOpen = false },
            onRegenerateIncomes = { viewModel.regenerateRecurringIncomesNow(); actionSheetOpen = false },
            onResetPlan = { actionSheetOpen = false; confirmReset = true },
            onDismiss = { actionSheetOpen = false },
        )
    }
    if (allocEditorOpen) {
        PerPlanAllocationEditorSheet(
            allocations = state.allocations.map { it.allocation },
            onSave = { updates ->
                viewModel.updateAllocationPcts(updates)
                allocEditorOpen = false
            },
            onDismiss = { allocEditorOpen = false },
        )
    }
    if (incomeSheetOpen) {
        EditExpectedIncomeSheet(
            current = state.plan?.expectedIncome ?: 0L,
            onSave = { amount -> viewModel.setExpectedIncomeAmount(amount); incomeSheetOpen = false },
            onDismiss = { incomeSheetOpen = false },
        )
    }
    if (confirmReset) {
        ConfirmResetSheet(
            onConfirm = { viewModel.resetCurrentPlan(); confirmReset = false },
            onDismiss = { confirmReset = false },
        )
    }
    if (monthPickerOpen) {
        MonthPickerSheet(
            plans = allPlans,
            activeId = state.plan?.id,
            onPick = { id ->
                viewModel.setViewedPlan(id)
                monthPickerOpen = false
            },
            onCreateNext = {
                viewModel.regenerateNextPeriodPlan()
                monthPickerOpen = false
            },
            onDismiss = { monthPickerOpen = false },
        )
    }
    // Toast-style feedback for actions that previously appeared dead
    val planCreated by viewModel.planCreatedResult.collectAsState()
    val recurringRes by viewModel.recurringResult.collectAsState()
    androidx.compose.runtime.LaunchedEffect(planCreated) {
        if (planCreated != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearPlanCreatedResult()
        }
    }
    androidx.compose.runtime.LaunchedEffect(recurringRes) {
        if (recurringRes != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearRecurringResult()
        }
    }
    if (planCreated != null || recurringRes != null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(sw.ink)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                val msg = when {
                    planCreated != null -> stringResource(R.string.plan_created_toast_format, planCreated!!)
                    recurringRes != null && recurringRes!! > 0 ->
                        stringResource(R.string.plan_recurring_result_format, recurringRes!!)
                    recurringRes != null -> stringResource(R.string.plan_recurring_result_none)
                    else -> ""
                }
                Text(msg, color = sw.bg, style = SwType.LabelStrong.copy(fontSize = 13.sp))
            }
        }
    }
}

/** Localized display name for an allocation — Bahasa/English based on current locale. */
@Composable
fun AllocationId.displayName(): String = stringResource(
    when (this) {
        AllocationId.Needs -> R.string.alloc_needs
        AllocationId.Wants -> R.string.alloc_wants
        AllocationId.Invest -> R.string.alloc_invest
    },
)

@Composable
private fun FilterChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (selected) accent else sw.surface)
            .border(
                1.dp,
                if (selected) accent else sw.border,
                RoundedCornerShape(99.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (selected) sw.onPrimary else sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun MonthPickerSheet(
    plans: List<com.gustiadhitya.sakuwise.core.domain.model.Plan>,
    activeId: String?,
    onPick: (String) -> Unit,
    onCreateNext: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.sheet_plan_month_picker_title), onDismiss = onDismiss) {
        // "Create next month" CTA pinned at top so users can build out future
        // periods without leaving the picker. Each click chains forward
        // (Jun → Jul → Aug …) per the regenerateNextPeriodPlan fix.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.primary)
                .clickable(onClick = onCreateNext)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null,
                    tint = sw.onPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(width = 10.dp, height = 1.dp))
                Text(
                    stringResource(R.string.plan_month_picker_create_next),
                    color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (plans.isEmpty()) {
            Text(stringResource(R.string.plan_month_picker_empty),
                color = sw.inkMuted, style = SwType.Body)
            return@SwPickerSheet
        }
        Column {
            plans.sortedByDescending { it.start }.forEach { plan ->
                val active = plan.id == activeId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) sw.primaryContainer else Color.Transparent)
                        .clickable { onPick(plan.id) }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                ) {
                    Icon(Icons.Outlined.CalendarToday, null,
                        tint = if (active) sw.onPrimaryContainer else sw.inkSubtle,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(width = 10.dp, height = 1.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plan.label,
                            color = if (active) sw.onPrimaryContainer else sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                        Text("${plan.start} → ${plan.end}",
                            color = if (active) sw.onPrimaryContainer.copy(alpha = 0.7f) else sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    }
                    if (active) {
                        Text("AKTIF",
                            color = sw.onPrimaryContainer,
                            style = SwType.SectionLabel.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Periode aktif ditentukan otomatis dari Tanggal Mulai Periode di Pengaturan. " +
                    "Plan lama bersifat read-only.",
                color = sw.inkSubtle, style = SwType.LabelSmall.copy(fontSize = 11.sp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

// ─── CategoryCard ─── Per prototype screens-plan.jsx:184-232. The entire
// expand/collapse panel lives inside ONE SwCard:
//   • header row: title + used/plan + 6dp tinted bar + footer line, chevron
//     on the right (rotates when expanded — we swap icons since AnimatedRotate
//     isn't worth the dep).
//   • when expanded: 1px border-top divider, then each item as a row with a
//     subtle 33%-alpha bottom border, and a plain "+ Tambah item" link.
// Delete-category is reachable via long-press on the header (proto shows no
// per-card delete affordance; the global 3-dots action sheet covers reset).
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    name: String, plan: Long, used: Long,
    allocColor: Color,
    items: List<com.gustiadhitya.sakuwise.feature.plan.viewmodel.PlanItemRow>,
    expanded: Boolean, onToggle: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEditItem: (com.gustiadhitya.sakuwise.feature.plan.viewmodel.PlanItemRow) -> Unit,
    onAddItem: () -> Unit,
) {
    val sw = SwTheme.colors
    val over = used > plan
    val pct = if (plan > 0) ((used.toFloat() / plan.toFloat()) * 100f).toInt() else 0
    SwCard(padding = PaddingValues(0.dp)) {
        Column(Modifier.fillMaxWidth()) {
            // Header (whole row tap = toggle, long-press = delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onToggle,
                        onLongClick = onDeleteCategory,
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(name, color = sw.ink,
                            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        RupiahText(value = used, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                            color = if (over) sw.danger else sw.ink)
                        Text(" / ", color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        RupiahText(value = plan, short = true,
                            style = SwType.Amount.copy(fontSize = 12.sp,
                                fontFeatureSettings = "tnum"),
                            color = sw.inkSubtle)
                    }
                    Spacer(Modifier.height(8.dp))
                    SwBar(used = used, plan = plan.coerceAtLeast(1L), color = allocColor, heightDp = 6)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${items.size} item · $pct%", color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 10.sp,
                                fontFeatureSettings = "tnum"))
                        if (over) {
                            Text("Over ${(used - plan).toRupiahShort()}",
                                color = sw.danger,
                                style = SwType.LabelSmall.copy(fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.KeyboardArrowDown,
                    null, tint = sw.inkSubtle, modifier = Modifier.size(20.dp),
                )
            }
            if (expanded) {
                // Top border divider, then padded item list (proto: padding
                // '4px 16px 12px', borderTop 1px c.border).
                Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)) {
                    items.forEach { pi ->
                        PlanItemRowProto(
                            name = pi.item.name,
                            plan = pi.item.plannedAmount,
                            used = pi.used,
                            allocColor = allocColor,
                            recurrence = pi.item.recurrence,
                            onClick = { onEditItem(pi) },
                        )
                    }
                    AddItemLink(onClick = onAddItem, primary = sw.primary)
                }
            }
        }
    }
}

@Composable
private fun DashedAddCategoryButton(text: String, accentColor: Color, onClick: () -> Unit) {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(text, color = accentColor,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun AddCategorySheet(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    SwPickerSheet(title = stringResource(R.string.sheet_plan_add_category_title), onDismiss = onDismiss) {
        SwField(value = name, onValueChange = { name = it },
            label = "Nama kategori", placeholder = "Mis. Tempat Tinggal")
        Spacer(Modifier.height(16.dp))
        SwButton(text = "Simpan",
            onClick = { onSave(name.trim()) },
            enabled = name.isNotBlank())
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

@Composable
private fun ConfirmDeleteCategorySheet(
    categoryName: String, onConfirm: () -> Unit, onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.sheet_plan_delete_category_title), onDismiss = onDismiss) {
        Text(
            "Hapus kategori \"$categoryName\" dan semua plan item di dalamnya. " +
                "Tindakan ini tidak bisa dibatalkan.",
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        SwButton(text = "Ya, hapus kategori", onClick = onConfirm,
            variant = SwButtonVariant.Danger,
            leading = { Icon(Icons.Outlined.Delete, null,
                tint = Color.White, modifier = Modifier.size(16.dp)) })
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

// ─── PlanItemRowProto ─── Per prototype screens-plan.jsx:234-261.
// No indent guide line, just name + recurrence chip + used/plan + thin bar,
// with a 33%-alpha bottom divider between rows (matches `border-bottom:
// `${c.border}55``). Padding 10dp top/bottom.
@Composable
private fun PlanItemRowProto(
    name: String, plan: Long, used: Long,
    allocColor: Color,
    recurrence: Recurrence,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f))
            if (recurrence != Recurrence.OneOff) {
                val chipText = when (recurrence) {
                    Recurrence.Monthly -> stringResource(R.string.recurrence_chip_monthly)
                    Recurrence.Quarterly -> stringResource(R.string.recurrence_chip_quarterly)
                    Recurrence.Yearly -> stringResource(R.string.recurrence_chip_yearly)
                    else -> ""
                }
                Text(
                    "⟳ $chipText",
                    color = sw.inkSubtle,
                    style = SwType.LabelSmall.copy(fontSize = 10.sp),
                )
                Spacer(Modifier.width(6.dp))
            }
            RupiahText(value = used, short = true,
                style = SwType.Amount.copy(fontSize = 12.sp,
                    fontWeight = if (used > plan) FontWeight.Bold else FontWeight.SemiBold),
                color = if (used > plan) sw.danger else sw.ink)
            Text(" / ", color = sw.inkSubtle, style = SwType.LabelSmall.copy(fontSize = 10.sp))
            RupiahText(value = plan, short = true,
                style = SwType.Amount.copy(fontSize = 12.sp), color = sw.inkSubtle)
        }
        Spacer(Modifier.height(6.dp))
        SwBar(used = used, plan = plan.coerceAtLeast(1L), color = allocColor, heightDp = 4)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border.copy(alpha = 0.33f)))
    }
}

// Plain centered "+ Tambah item" link, primary color — proto has NO dashed
// border for the in-card add (only the outer "+ Tambah kategori" is dashed).
@Composable
private fun AddItemLink(onClick: () -> Unit, primary: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 10.dp, bottom = 4.dp),
    ) {
        Icon(Icons.Outlined.Add, null, tint = primary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.plan_add_item),
            color = primary,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun PlanActionSheet(
    onApplyStarter: () -> Unit,
    onSetIncome: () -> Unit,
    onEditAllocations: () -> Unit,
    onRegenerateNext: () -> Unit,
    onRegenerateIncomes: () -> Unit,
    onResetPlan: () -> Unit,
    onDismiss: () -> Unit,
) {
    SwPickerSheet(title = stringResource(R.string.plan_action_sheet_title), onDismiss = onDismiss) {
        ActionRow(Icons.Outlined.MonetizationOn,
            stringResource(R.string.plan_action_set_income),
            stringResource(R.string.plan_action_set_income_sub), onClick = onSetIncome)
        ActionRow(Icons.Outlined.AutoAwesome,
            stringResource(R.string.plan_action_apply_starter),
            stringResource(R.string.plan_action_apply_starter_sub), onClick = onApplyStarter)
        ActionRow(Icons.Outlined.Tune,
            stringResource(R.string.plan_action_edit_allocations),
            stringResource(R.string.plan_action_edit_allocations_sub),
            onClick = onEditAllocations)
        ActionRow(Icons.Outlined.CalendarToday,
            stringResource(R.string.plan_action_regen_next),
            stringResource(R.string.plan_action_regen_next_sub),
            onClick = onRegenerateNext)
        ActionRow(Icons.Outlined.MonetizationOn,
            stringResource(R.string.plan_action_regen_incomes),
            stringResource(R.string.plan_action_regen_incomes_sub),
            onClick = onRegenerateIncomes)
        ActionRow(Icons.Outlined.RestartAlt,
            stringResource(R.string.plan_action_reset),
            stringResource(R.string.plan_action_reset_sub),
            danger = true, onClick = onResetPlan)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector, title: String, subtitle: String,
    danger: Boolean = false, onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val tint = if (danger) sw.danger else sw.ink
    val bgTint = if (danger) sw.dangerSoft else sw.primaryContainer
    val fgTint = if (danger) sw.danger else sw.onPrimaryContainer
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgTint),
        ) { Icon(icon, null, tint = fgTint, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = tint,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
            Text(subtitle, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp))
        }
    }
}

@Composable
private fun EditPlanItemSheet(
    existing: PlanItem?,
    onSave: (PlanItem?, String, Long, Recurrence) -> Unit,
    onDelete: ((PlanItem) -> Unit)?,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var amount by remember { mutableStateOf(existing?.plannedAmount?.toString() ?: "") }
    var rec by remember { mutableStateOf(existing?.recurrence ?: Recurrence.Monthly) }

    SwPickerSheet(
        title = stringResource(
            if (existing == null) R.string.plan_item_add_title
            else R.string.plan_item_edit_title,
        ),
        onDismiss = onDismiss,
    ) {
        SwField(value = name, onValueChange = { name = it },
            label = stringResource(R.string.plan_item_name_label),
            placeholder = stringResource(R.string.plan_item_name_placeholder))
        SwField(
            value = amount,
            onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
            label = stringResource(R.string.plan_item_amount_label),
            prefix = "Rp", rupiah = true, placeholder = "0",
            keyboardType = KeyboardType.Number,
        )
        Text(stringResource(R.string.plan_item_recurrence_label), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Recurrence.OneOff, Recurrence.Monthly, Recurrence.Quarterly, Recurrence.Yearly)
                .forEach { r ->
                    val active = rec == r
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) sw.primary else sw.surface)
                            .border(1.dp, if (active) sw.primary else sw.border, RoundedCornerShape(10.dp))
                            .clickable { rec = r },
                    ) {
                        Text(
                            stringResource(when (r) {
                                Recurrence.OneOff -> R.string.recurrence_oneoff
                                Recurrence.Monthly -> R.string.recurrence_monthly
                                Recurrence.Quarterly -> R.string.recurrence_quarterly
                                Recurrence.Yearly -> R.string.recurrence_yearly
                            }),
                            color = if (active) sw.onPrimary else sw.ink,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
        }
        Spacer(Modifier.height(16.dp))
        SwButton(
            text = "Simpan",
            onClick = { onSave(existing, name, amount.toLongOrNull() ?: 0L, rec) },
            enabled = name.isNotBlank() && amount.isNotBlank(),
        )
        // Reminder schedule controls — visible only for saved items with a
        // recurring schedule. Tapping "Atur pengingat" opens a small picker
        // so the user can choose day-of-month + time; WorkManager then fires
        // at that local time once per 30 days. "Batalkan" cancels the unique
        // work for this plan item.
        if (existing != null && rec != Recurrence.OneOff) {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val scheduledMsg = stringResource(R.string.reminder_toast_scheduled)
            val canceledMsg = stringResource(R.string.reminder_toast_canceled)
            val deniedMsg = stringResource(R.string.reminder_toast_denied)
            var showWhenSheet by remember { mutableStateOf(false) }
            var pendingDay by remember { mutableStateOf(1) }
            var pendingHour by remember { mutableStateOf(9) }
            val requestNotif = com.gustiadhitya.sakuwise.core.common.rememberNotificationPermissionRequester { granted ->
                if (granted) {
                    com.gustiadhitya.sakuwise.core.work.RecurringPaymentReminderWorker.scheduleMonthly(
                        ctx, existing.id,
                        title = ctx.getString(R.string.reminder_notif_title),
                        body = ctx.getString(R.string.reminder_notif_body_format, existing.name),
                        dayOfMonth = pendingDay,
                        hourOfDay = pendingHour,
                    )
                    val fmt = String.format("%02d:00", pendingHour)
                    android.widget.Toast.makeText(
                        ctx,
                        ctx.getString(R.string.reminder_toast_scheduled_fmt, pendingDay, fmt),
                        android.widget.Toast.LENGTH_LONG,
                    ).show()
                } else {
                    android.widget.Toast.makeText(ctx, deniedMsg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SwButton(
                    text = stringResource(R.string.plan_item_schedule_reminder),
                    onClick = { showWhenSheet = true },
                    variant = SwButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                )
                SwButton(
                    text = stringResource(R.string.plan_item_cancel_reminder),
                    onClick = {
                        com.gustiadhitya.sakuwise.core.work.RecurringPaymentReminderWorker.cancelFor(ctx, existing.id)
                        android.widget.Toast.makeText(ctx, canceledMsg, android.widget.Toast.LENGTH_SHORT).show()
                    },
                    variant = SwButtonVariant.Ghost,
                    modifier = Modifier.weight(1f),
                )
            }
            if (showWhenSheet) {
                ReminderWhenSheet(
                    initialDay = pendingDay,
                    initialHour = pendingHour,
                    onConfirm = { d, h ->
                        pendingDay = d; pendingHour = h
                        showWhenSheet = false
                        requestNotif()
                    },
                    onDismiss = { showWhenSheet = false },
                )
            }
            // Suppress unused warning for scheduledMsg — kept for now in case
            // we replace the formatted toast above with the plain one again.
            @Suppress("UNUSED_VARIABLE") val _u = scheduledMsg
        }
        if (existing != null && onDelete != null) {
            Spacer(Modifier.height(8.dp))
            SwButton(
                text = "Hapus item",
                onClick = { onDelete(existing) },
                variant = SwButtonVariant.Danger,
                leading = { Icon(Icons.Outlined.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
            )
        }
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

@Composable
private fun EditExpectedIncomeSheet(
    current: Long,
    onSave: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var amount by remember { mutableStateOf(if (current == 0L) "" else current.toString()) }
    SwPickerSheet(title = stringResource(R.string.sheet_plan_expected_income_title), onDismiss = onDismiss) {
        Text(
            "Total pemasukan yang kamu ekspektasikan di periode ini. " +
                "Dipakai sebagai baseline progress bar plan.",
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(12.dp))
        SwField(
            value = amount,
            onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
            label = "Pemasukan diharapkan",
            prefix = "Rp", rupiah = true, placeholder = "0",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(16.dp))
        SwButton(
            text = "Simpan",
            onClick = { onSave(amount.toLongOrNull() ?: 0L) },
            enabled = amount.isNotBlank(),
        )
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

@Composable
private fun ConfirmResetSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val sw = SwTheme.colors
    SwPickerSheet(title = stringResource(R.string.sheet_plan_reset_title), onDismiss = onDismiss) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(sw.dangerSoft),
        ) { Icon(Icons.Outlined.RestartAlt, null, tint = sw.danger, modifier = Modifier.size(32.dp)) }
        Text(
            "Semua kategori dan plan item di periode ini akan dihapus.\n\n" +
                "Transaksi yang sudah dicatat tidak terhapus, tapi hilang link-nya ke plan item lama.",
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(4.dp))
        Text("Tindakan ini tidak bisa dibatalkan.",
            color = sw.danger,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))
        SwButton(text = "Ya, reset plan", onClick = onConfirm, variant = SwButtonVariant.Danger)
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

/**
 * Per-plan allocation editor (PRD §7.3) — distinct from the default in Settings.
 *
 * Users adjust the 3 allocation percentages independently of the Settings default;
 * the change applies only to the current plan period. A sum != 100 is allowed
 * to save (the dashboard math is rebased to the actual sum), but a warning hint
 * surfaces so it's intentional.
 */
@Composable
private fun PerPlanAllocationEditorSheet(
    allocations: List<com.gustiadhitya.sakuwise.core.domain.model.Allocation>,
    onSave: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    val initial = remember(allocations) { allocations.associate { it.id to it.targetPct } }
    val edits = remember(allocations) {
        mutableStateMapOf<String, Int>().apply { putAll(initial) }
    }
    val sum = edits.values.sum()
    com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet(
        title = stringResource(R.string.plan_alloc_editor_title),
        onDismiss = onDismiss,
    ) {
        Text(
            stringResource(R.string.plan_alloc_editor_intro),
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(14.dp))
        allocations.forEach { alloc ->
            val current = edits[alloc.id] ?: alloc.targetPct
            val allocLabel = com.gustiadhitya.sakuwise.core.domain.model.AllocationId
                .fromName(alloc.name).displayName()
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(allocLabel, color = sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f))
                    // Number stepper — −/value/+
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(sw.surface)
                                .border(1.dp, sw.border, CircleShape)
                                .clickable { edits[alloc.id] = (current - 5).coerceAtLeast(0) },
                        ) { Text("−", color = sw.ink, style = SwType.LabelStrong) }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(width = 56.dp, height = 32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(sw.primaryContainer),
                        ) {
                            Text("$current%", color = sw.onPrimaryContainer,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold))
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(sw.surface)
                                .border(1.dp, sw.border, CircleShape)
                                .clickable { edits[alloc.id] = (current + 5).coerceAtMost(100) },
                        ) { Text("+", color = sw.ink, style = SwType.LabelStrong) }
                    }
                }
                // Track bar
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(sw.track)) {
                    Box(modifier = Modifier
                        .fillMaxWidth(current / 100f)
                        .height(6.dp)
                        .background(sw.primary))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Sum hint
        val ok = sum == 100
        Text(
            stringResource(
                if (ok) R.string.plan_alloc_sum_ok_format
                else R.string.plan_alloc_sum_warn_format,
                sum,
            ),
            color = if (ok) sw.success else sw.warning,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
        Spacer(Modifier.height(14.dp))
        SwButton(
            text = stringResource(R.string.action_save),
            onClick = {
                onSave(edits.toMap())
            },
        )
    }
}

/**
 * Small picker: day-of-month (1..28) + hour-of-day (0..23). Stays simple —
 * two horizontal scrollers of chips, then a Save button. We deliberately
 * cap the day to 28 so months without 29-31 don't silently skip a cycle.
 */
@Composable
private fun ReminderWhenSheet(
    initialDay: Int,
    initialHour: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var day by remember { mutableStateOf(initialDay.coerceIn(1, 28)) }
    var hour by remember { mutableStateOf(initialHour.coerceIn(0, 23)) }
    SwPickerSheet(title = stringResource(R.string.reminder_when_title), onDismiss = onDismiss) {
        Text(stringResource(R.string.reminder_when_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.reminder_when_day_label),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            (1..28).forEach { d ->
                val active = d == day
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 40.dp, height = 36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) sw.primary else sw.surface)
                        .border(1.dp, if (active) sw.primary else sw.border, RoundedCornerShape(10.dp))
                        .clickable { day = d },
                ) {
                    Text(d.toString(),
                        color = if (active) sw.onPrimary else sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.reminder_when_hour_label),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            (0..23).forEach { h ->
                val active = h == hour
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 56.dp, height = 36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) sw.primary else sw.surface)
                        .border(1.dp, if (active) sw.primary else sw.border, RoundedCornerShape(10.dp))
                        .clickable { hour = h },
                ) {
                    Text(String.format("%02d:00", h),
                        color = if (active) sw.onPrimary else sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        SwButton(
            text = stringResource(R.string.reminder_when_confirm),
            onClick = { onConfirm(day, hour) },
        )
        Spacer(Modifier.height(8.dp))
        SwButton(
            text = stringResource(R.string.action_cancel),
            onClick = onDismiss,
            variant = SwButtonVariant.Ghost,
        )
    }
}
