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
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
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
    data class CategoryActionsTarget(
        val category: com.gustiadhitya.sakuwise.core.domain.model.Category,
        val isFirst: Boolean,
        val isLast: Boolean,
    )
    var categoryActions by remember { mutableStateOf<CategoryActionsTarget?>(null) }
    var renameCategoryTarget by remember {
        mutableStateOf<com.gustiadhitya.sakuwise.core.domain.model.Category?>(null)
    }
    data class ItemActionsTarget(
        val item: PlanItem,
        val isFirst: Boolean,
        val isLast: Boolean,
    )
    var itemActions by remember { mutableStateOf<ItemActionsTarget?>(null) }
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
                state.plan?.let { com.gustiadhitya.sakuwise.core.common.planPeriodLabel(it.end) }
                    ?: stringResource(R.string.plan_no_plan),
                color = sw.onPrimaryContainer,
                style = SwType.Caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            )
            Icon(Icons.Outlined.ChevronRight, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.height(10.dp))

        Column(modifier = Modifier.padding(horizontal = SwSpace.pageH)) {
            SwCard {
                Column {
                val income = state.plan?.expectedIncome ?: 0L
                val totalUsed = state.allocations.sumOf { it.used }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.plan_expected_income),
                        color = sw.inkSubtle,
                        style = SwType.Caption.copy(fontSize = 11.sp),
                        modifier = Modifier.weight(1f),
                    )
                    RupiahText(
                        value = income,
                        style = SwType.Amount.copy(fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"),
                        color = sw.ink,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(sw.primaryContainer)
                            .clickable { incomeSheetOpen = true },
                    ) {
                        Icon(Icons.Outlined.Edit, "Ubah pemasukan",
                            tint = sw.onPrimaryContainer,
                            modifier = Modifier.size(13.dp))
                    }
                }
                if (state.allocations.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(28.dp))
                        Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                            Text("Rencana", color = sw.inkMuted,
                                style = SwType.Caption.copy(fontSize = 10.sp))
                        }
                        Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                            Text("Aktual", color = sw.inkMuted,
                                style = SwType.Caption.copy(fontSize = 10.sp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    state.allocations.forEach { row ->
                        val a = row.allocation
                        val allocId = AllocationId.fromName(a.name)
                        val dotColor = when (allocId) {
                            AllocationId.Needs -> sw.primary
                            AllocationId.Wants -> sw.accent
                            AllocationId.Invest -> sw.info
                        }
                        val allocAmount = income * a.targetPct / 100L
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(dotColor))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                allocId.displayName(),
                                color = sw.ink,
                                style = SwType.Caption.copy(fontSize = 12.sp),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    "${a.targetPct}%",
                                    color = sw.inkSubtle,
                                    style = SwType.Caption.copy(fontSize = 10.sp,
                                        fontFeatureSettings = "tnum"),
                                )
                            }
                            Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                                RupiahText(
                                    value = allocAmount, short = true,
                                    style = SwType.Amount.copy(fontSize = 12.sp,
                                        fontFeatureSettings = "tnum"),
                                    color = sw.inkMuted,
                                )
                            }
                            Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                                RupiahText(
                                    value = row.used, short = true,
                                    style = SwType.Amount.copy(fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"),
                                    color = sw.ink,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                    Spacer(Modifier.height(6.dp))
                    val totalAllocAmount = state.allocations.sumOf { income * it.allocation.targetPct / 100L }
                    val leftoverPlan = income - totalAllocAmount
                    val leftoverActual = income - totalUsed
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Sisa",
                            color = sw.ink,
                            style = SwType.Caption.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(28.dp))
                        Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                            RupiahText(
                                value = leftoverPlan, short = true,
                                style = SwType.Amount.copy(fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum"),
                                color = sw.inkMuted,
                            )
                        }
                        Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                            RupiahText(
                                value = leftoverActual, short = true,
                                style = SwType.Amount.copy(fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum"),
                                color = if (leftoverActual >= 0L) sw.success else sw.danger,
                            )
                        }
                    }
                }
                } // Column
            }
        }
        Spacer(Modifier.height(12.dp))

        // Filter chips moved below the income card per prototype.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = SwSpace.pageH)
                .horizontalScroll(rememberScrollState()),
        ) {
            // Active chip is always primary per proto SW_Chip (components.jsx:302).
            // The allocation color shows up on the body of each section anyway.
            FilterChip(label = stringResource(R.string.plan_filter_all),
                selected = filter == null,
                accent = sw.primary, onClick = { filter = null })
            FilterChip(label = stringResource(R.string.plan_filter_needs),
                selected = filter == AllocationId.Needs,
                accent = sw.primary, onClick = { filter = AllocationId.Needs })
            FilterChip(label = stringResource(R.string.plan_filter_wants),
                selected = filter == AllocationId.Wants,
                accent = sw.primary, onClick = { filter = AllocationId.Wants })
            FilterChip(label = stringResource(R.string.plan_filter_invest),
                selected = filter == AllocationId.Invest,
                accent = sw.primary, onClick = { filter = AllocationId.Invest })
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp,
                        start = 4.dp, end = 4.dp),
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(allocColor))
                    Text(allocLabel, color = sw.ink,
                        style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                    Text("${a.targetPct}%", color = sw.inkSubtle,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"))
                }
                row.categories.forEachIndexed { catIdx, cat ->
                    val isOpen = expanded[cat.category.id] != false
                    CategoryCard(
                        name = cat.category.name,
                        plan = cat.plan, used = cat.used,
                        allocColor = allocColor,
                        items = cat.items,
                        expanded = isOpen,
                        onToggle = { expanded[cat.category.id] = !isOpen },
                        onCategoryActions = {
                            categoryActions = CategoryActionsTarget(
                                category = cat.category,
                                isFirst = catIdx == 0,
                                isLast = catIdx == row.categories.size - 1,
                            )
                        },
                        onEditItem = { pi -> editItem = pi.item },
                        onItemActions = { pi, isFirst, isLast ->
                            itemActions = ItemActionsTarget(
                                item = pi.item,
                                isFirst = isFirst,
                                isLast = isLast,
                            )
                        },
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
                // Welcoming empty state — user starts BLANK on fresh install
                // (per user feedback). Primary action sets up the 3 alloc
                // buckets only (Kebutuhan/Keinginan/Investasi) and opens the
                // add-category sheet so they can build their plan from
                // scratch. Starter template stays available as the secondary
                // outline button for users who want a quick-start preset.
                SwCard {
                    Column {
                        Text(
                            stringResource(R.string.plan_empty_welcoming_title),
                            color = sw.ink,
                            style = SwType.LabelStrong.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.plan_empty_welcoming_body),
                            color = sw.inkMuted,
                            style = SwType.Body.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        )
                        Spacer(Modifier.height(14.dp))
                        SwButton(
                            text = stringResource(R.string.plan_empty_add_first),
                            onClick = {
                                viewModel.setupEmptyPlanWithAllocations {
                                    // After the 3 allocs exist the per-alloc
                                    // "+ Tambah kategori" buttons render at
                                    // the bottom of each allocation section;
                                    // a deliberate scroll cue is enough.
                                }
                            },
                        )
                        Spacer(Modifier.height(8.dp))
                        SwButton(
                            text = stringResource(R.string.plan_empty_use_starter),
                            onClick = { viewModel.applyStarterTemplateToCurrentPlan() },
                            variant = SwButtonVariant.Outline,
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
    if (categoryActions != null) {
        val target = categoryActions!!
        CategoryActionSheet(
            categoryName = target.category.name,
            isFirst = target.isFirst,
            isLast = target.isLast,
            onEdit = {
                renameCategoryTarget = target.category
                categoryActions = null
            },
            onMoveUp = {
                viewModel.moveCategoryUp(target.category)
                categoryActions = null
            },
            onMoveDown = {
                viewModel.moveCategoryDown(target.category)
                categoryActions = null
            },
            onDelete = {
                deleteCategoryConfirm = target.category.id to target.category.name
                categoryActions = null
            },
            onDismiss = { categoryActions = null },
        )
    }
    if (itemActions != null) {
        val target = itemActions!!
        ItemActionSheet(
            itemName = target.item.name,
            isFirst = target.isFirst,
            isLast = target.isLast,
            onEdit = {
                editItem = target.item
                itemActions = null
            },
            onMoveUp = {
                viewModel.movePlanItemUp(target.item)
                itemActions = null
            },
            onMoveDown = {
                viewModel.movePlanItemDown(target.item)
                itemActions = null
            },
            onDismiss = { itemActions = null },
        )
    }
    if (renameCategoryTarget != null) {
        val cat = renameCategoryTarget!!
        EditCategorySheet(
            initialName = cat.name,
            onSave = { newName ->
                viewModel.renameCategory(cat, newName)
                renameCategoryTarget = null
            },
            onDismiss = { renameCategoryTarget = null },
        )
    }
    // POST_NOTIFICATIONS: prompt lazily when a recurring plan item is saved.
    // Reminder scheduling is what *needs* the permission — asking on cold start
    // is premature and asking later (after save) is fine because the worker
    // posts the next reminder ≥1 day out.
    val requestNotifPerm = rememberNotificationPermissionRequester(onResult = {})
    if (addToCategory != null) {
        val addAllocRow = state.allocations.firstOrNull { a ->
            a.categories.any { it.category.id == addToCategory }
        }
        val income = state.plan?.expectedIncome ?: 0L
        EditPlanItemSheet(
            existing = null,
            allocationName = addAllocRow?.let { AllocationId.fromName(it.allocation.name).displayName() } ?: "",
            allocationBudget = income * (addAllocRow?.allocation?.targetPct ?: 0) / 100L,
            allocationPlanTotal = addAllocRow?.plan ?: 0L,
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
        val editAllocRow = state.allocations.firstOrNull { a ->
            a.categories.any { cat -> cat.items.any { it.item.id == editItem?.id } }
        }
        val income = state.plan?.expectedIncome ?: 0L
        EditPlanItemSheet(
            existing = editItem,
            allocationName = editAllocRow?.let { AllocationId.fromName(it.allocation.name).displayName() } ?: "",
            allocationBudget = income * (editAllocRow?.allocation?.targetPct ?: 0) / 100L,
            allocationPlanTotal = editAllocRow?.plan ?: 0L,
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
                        Text(com.gustiadhitya.sakuwise.core.common.planPeriodLabel(plan.end),
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
                stringResource(R.string.plan_period_active_hint),
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
    onCategoryActions: () -> Unit,
    onEditItem: (com.gustiadhitya.sakuwise.feature.plan.viewmodel.PlanItemRow) -> Unit,
    onItemActions: (com.gustiadhitya.sakuwise.feature.plan.viewmodel.PlanItemRow, Boolean, Boolean) -> Unit,
    onAddItem: () -> Unit,
) {
    val sw = SwTheme.colors
    val over = used > plan
    val pct = if (plan > 0) ((used.toFloat() / plan.toFloat()) * 100f).toInt() else 0
    SwCard(padding = PaddingValues(0.dp)) {
        Column(Modifier.fillMaxWidth()) {
            // Header (whole row tap = toggle, long-press = action sheet
            // with Edit + Delete options).
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onToggle,
                        onLongClick = onCategoryActions,
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
                    items.forEachIndexed { idx, pi ->
                        PlanItemRowProto(
                            name = pi.item.name,
                            plan = pi.item.plannedAmount,
                            used = pi.used,
                            allocColor = allocColor,
                            recurrence = pi.item.recurrence,
                            onClick = { onEditItem(pi) },
                            onLongClick = {
                                onItemActions(pi, idx == 0, idx == items.size - 1)
                            },
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
        SwButton(text = stringResource(R.string.action_save),
            onClick = { onSave(name.trim()) },
            enabled = name.isNotBlank())
        Spacer(Modifier.height(8.dp))
        SwButton(text = "Batal", onClick = onDismiss, variant = SwButtonVariant.Ghost)
    }
}

/**
 * Long-press on a Plan category opens this sheet so the user can pick
 * between renaming or deleting — previously long-press jumped straight
 * to the delete confirmation with no edit path.
 */
@Composable
private fun CategoryActionSheet(
    categoryName: String,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    SwPickerSheet(title = categoryName, onDismiss = onDismiss) {
        ActionSheetRow(icon = Icons.Outlined.Edit, label = stringResource(R.string.plan_category_action_edit), onClick = onEdit)
        if (!isFirst) ActionSheetRow(icon = Icons.Outlined.KeyboardArrowUp, label = "Pindah ke atas", onClick = onMoveUp)
        if (!isLast) ActionSheetRow(icon = Icons.Outlined.KeyboardArrowDown, label = "Pindah ke bawah", onClick = onMoveDown)
        ActionSheetRow(icon = Icons.Outlined.Delete, label = stringResource(R.string.plan_category_action_delete), danger = true, onClick = onDelete)
    }
}

@Composable
private fun ItemActionSheet(
    itemName: String,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDismiss: () -> Unit,
) {
    SwPickerSheet(title = itemName, onDismiss = onDismiss) {
        ActionSheetRow(icon = Icons.Outlined.Edit, label = "Ubah item", onClick = onEdit)
        if (!isFirst) ActionSheetRow(icon = Icons.Outlined.KeyboardArrowUp, label = "Pindah ke atas", onClick = onMoveUp)
        if (!isLast) ActionSheetRow(icon = Icons.Outlined.KeyboardArrowDown, label = "Pindah ke bawah", onClick = onMoveDown)
    }
}

@Composable
private fun ActionSheetRow(
    icon: ImageVector,
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    val tint = if (danger) sw.danger else sw.ink
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = tint,
            style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun EditCategorySheet(
    initialName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    SwPickerSheet(title = stringResource(R.string.sheet_plan_edit_category_title), onDismiss = onDismiss) {
        SwField(value = name, onValueChange = { name = it },
            label = "Nama kategori", placeholder = "Mis. Tempat Tinggal")
        Spacer(Modifier.height(16.dp))
        SwButton(text = stringResource(R.string.action_save),
            onClick = { onSave(name.trim()) },
            enabled = name.isNotBlank() && name.trim() != initialName)
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
            stringResource(R.string.plan_delete_category_body_format, categoryName),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(16.dp))
        SwButton(text = stringResource(R.string.plan_delete_category_confirm), onClick = onConfirm,
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlanItemRowProto(
    name: String, plan: Long, used: Long,
    allocColor: Color,
    recurrence: Recurrence,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val sw = SwTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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
    allocationName: String = "",
    allocationBudget: Long = 0L,
    allocationPlanTotal: Long = 0L,
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
        if (allocationBudget > 0L) {
            val plannedExcludingThis = allocationPlanTotal - (existing?.plannedAmount ?: 0L)
            val sisa = allocationBudget - plannedExcludingThis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(sw.surface)
                    .border(1.dp, sw.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Alokasi $allocationName", color = sw.inkMuted,
                        style = SwType.Caption.copy(fontSize = 10.sp), maxLines = 1)
                    Spacer(Modifier.height(2.dp))
                    RupiahText(value = allocationBudget, short = true,
                        style = SwType.Amount.copy(fontSize = 13.sp, fontFeatureSettings = "tnum"),
                        color = sw.ink)
                }
                Box(Modifier.width(1.dp).height(32.dp).background(sw.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Terencana", color = sw.inkMuted,
                        style = SwType.Caption.copy(fontSize = 10.sp))
                    Spacer(Modifier.height(2.dp))
                    RupiahText(value = plannedExcludingThis, short = true,
                        style = SwType.Amount.copy(fontSize = 13.sp, fontFeatureSettings = "tnum"),
                        color = sw.inkMuted)
                }
                Box(Modifier.width(1.dp).height(32.dp).background(sw.border))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Sisa", color = sw.inkMuted,
                        style = SwType.Caption.copy(fontSize = 10.sp))
                    Spacer(Modifier.height(2.dp))
                    RupiahText(value = sisa, short = true,
                        style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"),
                        color = if (sisa >= 0L) sw.success else sw.danger)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
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
            text = stringResource(R.string.action_save),
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
            val canceledMsg = stringResource(R.string.reminder_toast_canceled)
            val deniedMsg = stringResource(R.string.reminder_toast_denied)
            var showWhenSheet by remember { mutableStateOf(false) }
            var pendingDay by remember { mutableStateOf(1) }
            var pendingHour by remember { mutableStateOf(9) }

            // Observe WorkManager state to know if reminder is currently active.
            val workInfos by WorkManager.getInstance(ctx)
                .getWorkInfosForUniqueWorkFlow(
                    com.gustiadhitya.sakuwise.core.work.RecurringPaymentReminderWorker.uniqueName(existing.id)
                )
                .collectAsState(initial = emptyList())
            val isActive = workInfos.any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }

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

            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
            Spacer(Modifier.height(12.dp))

            val bgColor = if (isActive) sw.success.copy(alpha = 0.12f) else sw.surface
            val borderColor = if (isActive) sw.success.copy(alpha = 0.4f) else sw.border
            val iconTint = if (isActive) sw.success else sw.inkMuted
            val bellIcon = if (isActive) Icons.Outlined.Notifications else Icons.Outlined.NotificationsNone
            val label = if (isActive) "Pengingat Aktif" else stringResource(R.string.plan_item_schedule_reminder)
            val sub = if (isActive) "Ketuk untuk ubah jadwal" else "Ketuk untuk atur jadwal notifikasi"
            val labelColor = if (isActive) sw.success else sw.ink

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { showWhenSheet = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Icon(bellIcon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, color = labelColor,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                    Text(sub, color = sw.inkMuted,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                }
                if (isActive) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(sw.danger.copy(alpha = 0.1f))
                            .clickable {
                                com.gustiadhitya.sakuwise.core.work.RecurringPaymentReminderWorker.cancelFor(ctx, existing.id)
                                android.widget.Toast.makeText(ctx, canceledMsg, android.widget.Toast.LENGTH_SHORT).show()
                            },
                    ) {
                        Icon(Icons.Outlined.Close, null,
                            tint = sw.danger, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Outlined.ChevronRight, null,
                        tint = sw.inkMuted, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
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
        }
        if (existing != null && onDelete != null) {
            Spacer(Modifier.height(8.dp))
            SwButton(
                text = stringResource(R.string.plan_delete_item),
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
            stringResource(R.string.plan_expected_income_body),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(12.dp))
        SwField(
            value = amount,
            onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
            label = stringResource(R.string.plan_expected_income_label),
            prefix = "Rp", rupiah = true, placeholder = "0",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(16.dp))
        SwButton(
            text = stringResource(R.string.action_save),
            onClick = { onSave(amount.toLongOrNull() ?: 0L) },
            enabled = amount.isNotBlank(),
        )
        Spacer(Modifier.height(8.dp))
        SwButton(text = stringResource(R.string.action_cancel), onClick = onDismiss, variant = SwButtonVariant.Ghost)
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
            stringResource(R.string.plan_reset_body),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.plan_reset_irreversible),
            color = sw.danger,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))
        SwButton(text = stringResource(R.string.plan_reset_confirm), onClick = onConfirm, variant = SwButtonVariant.Danger)
        Spacer(Modifier.height(8.dp))
        SwButton(text = stringResource(R.string.action_cancel), onClick = onDismiss, variant = SwButtonVariant.Ghost)
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
