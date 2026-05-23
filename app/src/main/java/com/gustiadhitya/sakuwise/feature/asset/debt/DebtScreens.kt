package com.gustiadhitya.sakuwise.feature.asset.debt

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.designsystem.components.SwBar
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.Debt
import com.gustiadhitya.sakuwise.core.domain.model.DebtDirection
import com.gustiadhitya.sakuwise.core.domain.model.DebtPayment
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.AddDebtPaymentUseCase
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class DebtListRow(val debt: Debt, val paid: Long, val outstanding: Long)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DebtListViewModel @Inject constructor(private val repo: DebtRepository) : ViewModel() {
    val items: StateFlow<List<DebtListRow>> = repo.observeAll().flatMapLatest { list ->
        if (list.isEmpty()) flowOf(emptyList()) else combine(
            list.map { d -> repo.observePaidTotal(d.id).map { paid ->
                DebtListRow(d, paid, (d.principal - paid).coerceAtLeast(0L))
            } },
        ) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DebtDetailViewModel @Inject constructor(
    private val repo: DebtRepository,
    private val accountRepo: AccountRepository,
    private val addDebtPayment: AddDebtPaymentUseCase,
) : ViewModel() {
    private val _id = MutableStateFlow<String?>(null)
    fun bind(id: String) { if (_id.value != id) _id.value = id }
    val debt: StateFlow<Debt?> = _id.flatMapLatest { id ->
        if (id == null) flowOf(null) else repo.observeById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val payments: StateFlow<List<DebtPayment>> = _id.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.observePayments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val paid: StateFlow<Long> = _id.flatMapLatest { id ->
        if (id == null) flowOf(0L) else repo.observePaidTotal(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val accounts: StateFlow<List<Account>> = accountRepo.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addPayment(date: LocalDate, amount: Long, accountId: String?, note: String?) {
        val id = _id.value ?: return
        val direction = debt.value?.direction ?: return
        viewModelScope.launch { addDebtPayment(id, direction, date, amount, accountId, note) }
    }
    fun markPaid() {
        viewModelScope.launch {
            val d = debt.value ?: return@launch
            repo.upsert(d.copy(open = false))
        }
    }
}

@HiltViewModel
class DebtEditViewModel @Inject constructor(private val repo: DebtRepository) : ViewModel() {
    private val _s = MutableStateFlow(DebtEditState())
    val state: StateFlow<DebtEditState> = _s
    /** Always re-fetch from DB — see AccountEditViewModel for rationale. */
    fun load(id: String?) {
        if (id == null) { _s.value = DebtEditState(loaded = true); return }
        viewModelScope.launch {
            val d = repo.observeById(id).first()
            _s.value = if (d == null) DebtEditState(loaded = true)
            else DebtEditState(
                id = d.id, counterparty = d.counterparty,
                direction = d.direction,
                principal = d.principal.toString(),
                startDate = d.startDate, dueDate = d.dueDate,
                loaded = true,
            )
        }
    }
    fun set(t: (DebtEditState) -> DebtEditState) { _s.value = t(_s.value) }
    fun submit(onDone: () -> Unit) {
        val s = _s.value
        viewModelScope.launch {
            repo.upsert(
                Debt(
                    id = s.id ?: UUID.randomUUID().toString(),
                    counterparty = s.counterparty,
                    direction = s.direction,
                    principal = s.principal.toLongOrNull() ?: 0L,
                    startDate = s.startDate, dueDate = s.dueDate,
                    open = true, note = null,
                ),
            )
            onDone()
        }
    }
}

data class DebtEditState(
    val id: String? = null,
    val counterparty: String = "",
    val direction: DebtDirection = DebtDirection.IOwe,
    val principal: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate? = null,
    val loaded: Boolean = false,
)

@Composable
fun DebtListScreen(
    onBack: () -> Unit, onItemClick: (String) -> Unit, onAdd: () -> Unit,
    viewModel: DebtListViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val items by viewModel.items.collectAsState()
    val iOwe = items.filter { it.debt.direction == DebtDirection.IOwe }
    val owedToMe = items.filter { it.debt.direction == DebtDirection.OwedToMe }
    SimpleSettingsScreen(
        title = stringResource(R.string.debt_title), onBack = onBack,
        actions = {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(sw.danger).clickable(onClick = onAdd)) {
                Icon(Icons.Outlined.Add, stringResource(R.string.debt_add_cd), tint = Color.White, modifier = Modifier.size(20.dp))
            }
        },
    ) {
        DebtSection(label = stringResource(R.string.debt_section_i_owe), negative = true,
            total = iOwe.sumOf { it.outstanding }, items = iOwe, onItemClick = onItemClick)
        Spacer(Modifier.height(16.dp))
        DebtSection(label = stringResource(R.string.debt_section_owed), negative = false,
            total = owedToMe.sumOf { it.outstanding }, items = owedToMe, onItemClick = onItemClick)
        if (items.isEmpty()) {
            SwCard {
                Text(stringResource(R.string.debt_list_empty),
                    color = sw.inkMuted, style = SwType.Body)
            }
        }
    }
}

@Composable
private fun DebtSection(
    label: String, negative: Boolean, total: Long,
    items: List<DebtListRow>, onItemClick: (String) -> Unit,
) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) {
        Text(label, color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp), modifier = Modifier.weight(1f))
        if (total > 0) {
            Text(
                "${if (negative) "−" else "+"} ",
                color = if (negative) sw.danger else sw.success,
                style = SwType.Amount.copy(fontSize = 12.sp),
            )
            RupiahText(value = total, short = true,
                color = if (negative) sw.danger else sw.success,
                style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
        }
    }
    if (items.isEmpty()) {
        SwCard { Text(stringResource(R.string.gold_dash), color = sw.inkSubtle, style = SwType.Body) }
        return
    }
    Column {
        items.forEach { row ->
            SwCard(modifier = Modifier.padding(vertical = 4.dp),
                onClick = { onItemClick(row.debt.id) }) {
                Column {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text(row.debt.counterparty, color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                            Text(stringResource(R.string.debt_start_format, row.debt.startDate.toAbsoluteId()),
                                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                        RupiahText(value = row.outstanding, short = true,
                            style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = if (negative) sw.danger else sw.success)
                    }
                    Spacer(Modifier.height(6.dp))
                    SwBar(used = row.paid, plan = row.debt.principal.coerceAtLeast(1L),
                        color = if (negative) sw.danger else sw.success)
                }
            }
        }
    }
}

@Composable
fun DebtDetailScreen(
    id: String, onBack: () -> Unit, onEdit: () -> Unit,
    viewModel: DebtDetailViewModel = hiltViewModel(key = "debt-$id"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.bind(id) }
    val d = viewModel.debt.collectAsState().value
    val payments by viewModel.payments.collectAsState()
    val paid by viewModel.paid.collectAsState()
    var addPay by remember { mutableStateOf(false) }
    if (d == null) return SimpleSettingsScreen(stringResource(R.string.debt_detail_default_title), onBack) {
        Text(stringResource(R.string.loading), color = sw.inkMuted, style = SwType.Body)
    }
    val negative = d.direction == DebtDirection.IOwe
    val outstanding = (d.principal - paid).coerceAtLeast(0L)
    val pct = if (d.principal > 0) (paid * 100 / d.principal).toInt() else 0
    SimpleSettingsScreen(
        title = d.counterparty, onBack = onBack,
        actions = {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(sw.primary).clickable(onClick = onEdit)) {
                Text(stringResource(R.string.action_edit), color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(if (negative) sw.danger else sw.success).padding(20.dp)) {
            Column {
                Text(if (negative) stringResource(R.string.debt_outstanding_negative)
                    else stringResource(R.string.debt_outstanding_positive),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = outstanding, color = Color.White, style = SwType.AmountXL)
                Spacer(Modifier.height(10.dp))
                SwBar(used = paid, plan = d.principal.coerceAtLeast(1L), color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(
                        R.string.debt_progress_format,
                        paid.toRupiah(), pct, d.principal.toRupiah(),
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        // Outstanding-balance trend chart (history derived from payment timeline)
        if (payments.isNotEmpty()) {
            DebtOutstandingChart(
                principal = d.principal,
                startDate = d.startDate,
                payments = payments,
            )
            Spacer(Modifier.height(14.dp))
        }
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                DetailRow(stringResource(R.string.debt_direction_label),
                    if (negative) stringResource(R.string.debt_direction_i_owe)
                    else stringResource(R.string.debt_direction_owed_to_me_long))
                DetailRow(stringResource(R.string.debt_field_principal), d.principal.toRupiah())
                DetailRow(stringResource(R.string.debt_field_start), d.startDate.toAbsoluteId())
                DetailRow(stringResource(R.string.debt_field_due), d.dueDate?.toAbsoluteId() ?: stringResource(R.string.gold_dash))
                DetailRow(stringResource(R.string.debt_field_status),
                    if (d.open) stringResource(R.string.debt_status_open) else stringResource(R.string.debt_status_paid))
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.debt_section_payments), color = sw.ink,
                style = SwType.H3.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Text(stringResource(R.string.debt_payment_add), color = sw.primary,
                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable { addPay = true })
        }
        Spacer(Modifier.height(8.dp))
        if (payments.isEmpty()) {
            SwCard { Text(stringResource(R.string.debt_payments_empty), color = sw.inkMuted, style = SwType.Body) }
        } else SwCard(padding = PaddingValues(0.dp)) {
            Column {
                payments.forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(p.date.toAbsoluteId(), color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                            if (p.note != null) Text(p.note, color = sw.inkSubtle,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                        RupiahText(value = p.amount, short = true,
                            style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (d.open) {
            SwButton(text = stringResource(R.string.debt_mark_paid), onClick = { viewModel.markPaid() },
                variant = SwButtonVariant.Outline)
        }
    }
    if (addPay) {
        val accounts by viewModel.accounts.collectAsState()
        AddPaymentSheet(
            direction = d.direction,
            accounts = accounts,
            onSave = { date, amt, accId, note ->
                viewModel.addPayment(date, amt, accId, note); addPay = false
            },
            onDismiss = { addPay = false },
        )
    }
}

@Composable
private fun AddPaymentSheet(
    direction: DebtDirection,
    accounts: List<Account>,
    onSave: (LocalDate, Long, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf<String?>(null) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val account = accounts.firstOrNull { it.id == accountId }
    val accountLabel = if (direction == DebtDirection.IOwe)
        stringResource(R.string.debt_payment_account_payer)
    else stringResource(R.string.debt_payment_account_receiver)
    val accountFallback = stringResource(R.string.debt_payment_account_fallback)
    val effectHint = if (direction == DebtDirection.IOwe)
        stringResource(R.string.debt_payment_effect_expense_format, account?.name ?: accountFallback)
    else
        stringResource(R.string.debt_payment_effect_income_format, account?.name ?: accountFallback)

    SwPickerSheet(title = stringResource(R.string.debt_payment_sheet_title), onDismiss = onDismiss) {
        SwField(value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.debt_payment_amount), prefix = "Rp", rupiah = true, keyboardType = KeyboardType.Number)
        SwField(value = note, onValueChange = { note = it }, label = stringResource(R.string.debt_payment_note))

        Text(
            stringResource(R.string.debt_payment_account_label_format, accountLabel),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(12.dp))
                .clickable { showAccountPicker = true }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                account?.name ?: stringResource(R.string.debt_payment_account_empty),
                color = if (account == null) sw.inkSubtle else sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (account != null) effectHint
            else stringResource(R.string.debt_payment_effect_none),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 11.sp),
        )
        Spacer(Modifier.height(6.dp))
        // Date row
        Text(
            stringResource(R.string.common_date_label),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(12.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(date.toAbsoluteId(), color = sw.ink, style = SwType.Body.copy(fontSize = 14.sp))
        }
        Spacer(Modifier.height(12.dp))
        SwButton(stringResource(R.string.action_save), onClick = {
            onSave(date, amount.toLongOrNull() ?: 0L, accountId, note.ifBlank { null })
        }, enabled = amount.isNotBlank())
    }

    if (showDatePicker) {
        DatePickerSheet(
            selected = date,
            onPick = { date = it },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showAccountPicker) {
        AccountPickerSheet(
            accounts = accounts,
            selectedId = accountId,
            onPick = { accountId = it.id },
            onDismiss = { showAccountPicker = false },
        )
    }
}

@Composable
fun DebtEditScreen(
    id: String?, onClose: () -> Unit,
    viewModel: DebtEditViewModel = hiltViewModel(key = "debt-edit-${id ?: "new"}"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.load(id) }
    val s by viewModel.state.collectAsState()
    SimpleSettingsScreen(
        title = stringResource(if (s.id == null) R.string.debt_new_title else R.string.debt_edit_title),
        onBack = onClose,
        actions = {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.height(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(sw.primary).clickable { viewModel.submit(onClose) }
                    .padding(horizontal = 14.dp)) {
                Text(stringResource(R.string.action_save), color = sw.onPrimary,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
            }
        },
    ) {
        SwField(s.counterparty, { v -> viewModel.set { it.copy(counterparty = v) } },
            label = stringResource(R.string.debt_counterparty_label),
            placeholder = stringResource(R.string.debt_counterparty_placeholder))
        SwField(s.principal, { v -> viewModel.set { it.copy(principal = v.filter { c -> c.isDigit() }) } },
            label = stringResource(R.string.debt_principal_label), prefix = "Rp", rupiah = true,
            keyboardType = KeyboardType.Number)
        Text(stringResource(R.string.debt_direction_label), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                DebtDirection.IOwe to stringResource(R.string.debt_direction_i_owe),
                DebtDirection.OwedToMe to stringResource(R.string.debt_direction_owed_to_me),
            ).forEach { (dir, label) ->
                val active = s.direction == dir
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) sw.primary else sw.surface)
                        .border(1.dp, if (active) sw.primary else sw.border, RoundedCornerShape(10.dp))
                        .clickable { viewModel.set { it.copy(direction = dir) } }) {
                    Text(label, color = if (active) sw.onPrimary else sw.ink,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold))
                }
            }
        }

        // Start date + due date pickers
        var pickStart by remember { mutableStateOf(false) }
        var pickDue by remember { mutableStateOf(false) }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.debt_field_start), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)
            .clip(RoundedCornerShape(12.dp)).background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(12.dp))
            .clickable { pickStart = true }
            .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart) {
            Text(s.startDate.toAbsoluteId(), color = sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp))
        }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.debt_field_due), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)
            .clip(RoundedCornerShape(12.dp)).background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(12.dp))
            .clickable { pickDue = true }
            .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart) {
            Text(
                s.dueDate?.toAbsoluteId() ?: stringResource(R.string.debt_due_optional),
                color = if (s.dueDate == null) sw.inkSubtle else sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        }
        if (s.dueDate != null) {
            Spacer(Modifier.height(6.dp))
            Box(contentAlignment = Alignment.CenterStart,
                modifier = Modifier.clickable { viewModel.set { it.copy(dueDate = null) } }
                    .padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(stringResource(R.string.debt_due_clear),
                    color = sw.primary, style = SwType.LabelSmall.copy(fontSize = 12.sp))
            }
        }
        if (pickStart) {
            DatePickerSheet(
                selected = s.startDate,
                onPick = { d -> viewModel.set { it.copy(startDate = d) } },
                onDismiss = { pickStart = false },
                allowFuture = true,
            )
        }
        if (pickDue) {
            DatePickerSheet(
                selected = s.dueDate ?: LocalDate.now(),
                onPick = { d -> viewModel.set { it.copy(dueDate = d) } },
                onDismiss = { pickDue = false },
                allowFuture = true,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val sw = SwTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(label, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.weight(1f))
        Text(value, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    }
}

/**
 * DebtOutstandingChart — line + area chart of outstanding balance over time.
 *
 * Reconstructs balance history by walking forward from the debt start date with
 * principal, then subtracting each payment in chronological order. The x-axis
 * is uniform per data point (matches the prototype's compact look — no time
 * scale). Useful when the user has 2+ payments; the empty-state is handled at
 * the call site.
 */
@androidx.compose.runtime.Composable
private fun DebtOutstandingChart(
    principal: Long,
    startDate: java.time.LocalDate,
    payments: List<com.gustiadhitya.sakuwise.core.domain.model.DebtPayment>,
) {
    val sw = com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme.colors
    // Sort payments oldest → newest so the cumulative subtract is correct.
    val sorted = payments.sortedBy { it.date }
    var running = principal
    val series = mutableListOf(principal)
    sorted.forEach { p ->
        running = (running - p.amount).coerceAtLeast(0L)
        series += running
    }
    if (series.size < 2) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(18.dp))
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.debt_chart_label),
                color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp),
            )
            Text(
                stringResource(R.string.debt_chart_payments_count_format, payments.size),
                color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 11.sp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val maxV = series.max().toFloat().coerceAtLeast(1f)
            val w = size.width; val h = size.height
            val xs = series.indices.map { it * w / (series.size - 1) }
            val ys = series.map { h - (it / maxV) * h * 0.85f - h * 0.05f }
            val area = Path().apply {
                moveTo(xs[0], h)
                for (i in xs.indices) lineTo(xs[i], ys[i])
                lineTo(xs.last(), h); close()
            }
            drawPath(area, brush = Brush.verticalGradient(
                listOf(sw.danger.copy(alpha = 0.22f), androidx.compose.ui.graphics.Color.Transparent),
            ))
            val line = Path().apply {
                moveTo(xs[0], ys[0])
                for (i in 1 until xs.size) lineTo(xs[i], ys[i])
            }
            drawPath(line, color = sw.danger,
                style = Stroke(width = 2.2f * density, cap = StrokeCap.Round))
            for (i in xs.indices) {
                val isLast = i == xs.size - 1
                drawCircle(color = sw.danger,
                    radius = if (isLast) 4f * density else 2.5f * density,
                    center = Offset(xs[i], ys[i]))
            }
        }
    }
}

