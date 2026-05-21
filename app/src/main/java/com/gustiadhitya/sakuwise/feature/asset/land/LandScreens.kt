package com.gustiadhitya.sakuwise.feature.asset.land

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Landscape
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
import androidx.compose.ui.draw.clipToBounds
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
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Add
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AssetStatus
import com.gustiadhitya.sakuwise.core.domain.model.LandAsset
import com.gustiadhitya.sakuwise.core.domain.model.LandTaxPayment
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.AddLandTaxPaymentUseCase
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LandListViewModel @Inject constructor(private val repo: LandRepository) : ViewModel() {
    val items: StateFlow<List<LandAsset>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class LandDetailViewModel @Inject constructor(
    private val repo: LandRepository,
    private val accountRepo: AccountRepository,
    private val addTaxPayment: AddLandTaxPaymentUseCase,
) : ViewModel() {
    private val _id = MutableStateFlow<String?>(null)
    fun bind(id: String) { if (_id.value != id) _id.value = id }
    val item: StateFlow<LandAsset?> = _id.flatMapLatest { id ->
        if (id == null) flowOf(null) else repo.observeById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val taxes: StateFlow<List<LandTaxPayment>> = _id.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.observeTaxPayments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val accounts: StateFlow<List<Account>> = accountRepo.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addTax(date: LocalDate, amount: Long, accountId: String?, note: String?) {
        val id = _id.value ?: return
        viewModelScope.launch { addTaxPayment(id, date, amount, accountId, note) }
    }
    fun delete() { val id = _id.value ?: return; viewModelScope.launch { repo.delete(id) } }
}

@HiltViewModel
class LandEditViewModel @Inject constructor(private val repo: LandRepository) : ViewModel() {
    private val _s = MutableStateFlow(LandEditState())
    val state: StateFlow<LandEditState> = _s
    /** Always re-fetch from DB — see AccountEditViewModel for rationale. */
    fun load(id: String?) {
        if (id == null) { _s.value = LandEditState(loaded = true); return }
        viewModelScope.launch {
            val l = repo.observeById(id).first()
            _s.value = if (l == null) LandEditState(loaded = true)
            else LandEditState(
                id = l.id, name = l.name, location = l.location,
                sertifikat = l.sertifikatId, size = l.sizeM2.toString(),
                buyPrice = l.buyPrice.toString(),
                currentValue = l.currentValue?.toString() ?: "",
                purchaseDate = l.purchaseDate,
                loaded = true,
            )
        }
    }
    fun set(t: (LandEditState) -> LandEditState) { _s.value = t(_s.value) }
    fun submit(onDone: () -> Unit) {
        val s = _s.value
        viewModelScope.launch {
            repo.upsert(
                LandAsset(
                    id = s.id ?: UUID.randomUUID().toString(),
                    name = s.name, location = s.location, sertifikatId = s.sertifikat,
                    sizeM2 = s.size.toIntOrNull() ?: 0,
                    buyPrice = s.buyPrice.toLongOrNull() ?: 0L,
                    currentValue = s.currentValue.toLongOrNull(),
                    note = null, status = AssetStatus.Held,
                    soldDate = null, soldPrice = null,
                    purchaseDate = s.purchaseDate,
                ),
            )
            onDone()
        }
    }
}

data class LandEditState(
    val id: String? = null, val name: String = "", val location: String = "",
    val sertifikat: String = "", val size: String = "", val buyPrice: String = "",
    val currentValue: String = "",
    val purchaseDate: LocalDate = LocalDate.now(),
    val loaded: Boolean = false,
)

@Composable
fun LandListScreen(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    onAdd: () -> Unit,
    viewModel: LandListViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val items by viewModel.items.collectAsState()
    val total = items.filter { it.status == AssetStatus.Held }
        .sumOf { it.currentValue ?: it.buyPrice }
    SimpleSettingsScreen(
        title = stringResource(R.string.land_title), onBack = onBack,
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(sw.info).clickable(onClick = onAdd),
            ) { Icon(Icons.Outlined.Add, stringResource(R.string.land_add_cd), tint = Color.White, modifier = Modifier.size(20.dp)) }
        },
    ) {
        // Land list hero — same proto watermark pattern.
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(sw.info).padding(18.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_land),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 30.dp)
                        .size(140.dp),
                )
            }
            Column {
                Text(stringResource(R.string.land_hero_total),
                    color = Color.White.copy(alpha = 0.78f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = total, color = Color.White,
                    style = SwType.AmountXL.copy(fontSize = 30.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold))
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.land_hero_sub_format, items.size),
                    color = Color.White.copy(alpha = 0.75f),
                    style = SwType.LabelSmall.copy(fontSize = 12.sp, lineHeight = 14.sp))
            }
        }
        Spacer(Modifier.height(14.dp))
        if (items.isEmpty()) {
            SwCard {
                Text(stringResource(R.string.land_empty),
                    color = sw.inkMuted, style = SwType.Body)
            }
        } else {
            // Single SwCard with rows per proto.
            SwCard(padding = PaddingValues(0.dp)) {
                Column {
                    items.forEachIndexed { i, l ->
                        val value = l.currentValue ?: l.buyPrice
                        val growth = if (l.buyPrice > 0L)
                            ((value - l.buyPrice).toFloat() / l.buyPrice.toFloat()) * 100f
                        else 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(l.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                                    .background(sw.info.copy(alpha = 0.15f)),
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_land),
                                    contentDescription = null,
                                    tint = sw.info, modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                            Column(Modifier.weight(1f)) {
                                Text(l.name, color = sw.ink,
                                    style = SwType.LabelStrong.copy(fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold))
                                Text(stringResource(R.string.land_item_sub_format,
                                    l.location, l.sizeM2),
                                    color = sw.inkMuted,
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                RupiahText(value = value,
                                    style = SwType.Amount.copy(fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"))
                                if (l.currentValue != null && l.buyPrice > 0L) {
                                    val pos = growth >= 0f
                                    Text(
                                        (if (pos) "+" else "−") +
                                            "%.1f".format(kotlin.math.abs(growth)) + "%",
                                        color = if (pos) sw.success else sw.danger,
                                        style = SwType.LabelSmall.copy(fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFeatureSettings = "tnum"),
                                    )
                                }
                            }
                        }
                        if (i < items.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp)
                                .padding(start = 84.dp)
                                .background(sw.border))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LandDetailScreen(
    id: String, onBack: () -> Unit, onEdit: () -> Unit,
    viewModel: LandDetailViewModel = hiltViewModel(key = "land-$id"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.bind(id) }
    val item by viewModel.item.collectAsState()
    val taxes by viewModel.taxes.collectAsState()
    var addTax by remember { mutableStateOf(false) }
    val l = item ?: return SimpleSettingsScreen(stringResource(R.string.land_detail_default_title), onBack) {
        Text(stringResource(R.string.loading), color = sw.inkMuted, style = SwType.Body)
    }
    SimpleSettingsScreen(
        title = l.name, onBack = onBack,
        actions = {
            // Proto: transparent 40×40 r12 MoreHoriz glyph (no filled CTA).
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit),
            ) { Icon(Icons.Outlined.MoreHoriz, stringResource(R.string.action_edit),
                tint = sw.ink, modifier = Modifier.size(22.dp)) }
        },
    ) {
        // Hero — per proto screens-assets-detail.jsx:84-103. Info-blue bg,
        // landscape watermark at bottom-right (opacity 0.18), and a
        // translucent-white profit chip showing gain since buy.
        val value = l.currentValue ?: l.buyPrice
        val profit = value - l.buyPrice
        val pctProfit = if (l.buyPrice > 0) (profit.toFloat() / l.buyPrice.toFloat()) * 100f else 0f
        // Land detail hero — same proto watermark pattern.
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(sw.info).padding(18.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_land),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 30.dp)
                        .size(140.dp),
                )
            }
            Column {
                Text(stringResource(R.string.land_detail_value_label),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp, lineHeight = 14.sp))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = value, color = Color.White,
                    style = SwType.AmountXL.copy(fontSize = 30.sp, lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold))
                if (profit != 0L) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            // Black tint matches gold detail — white text on
                            // info blue needs a darker chip to read.
                            .background(Color.Black.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            stringResource(
                                R.string.land_since_purchase_format,
                                (if (profit >= 0) "+ " else "− ") + kotlin.math.abs(profit).toRupiahShort(),
                                (if (pctProfit >= 0f) "+" else "−"),
                                "%.1f".format(kotlin.math.abs(pctProfit)),
                            ),
                            color = Color.White,
                            style = SwType.LabelStrong.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        // "DETAIL" label per proto SW_SectionLabel.
        Text(stringResource(R.string.gold_section_detail), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
        Spacer(Modifier.height(4.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                DetailRow(stringResource(R.string.land_field_location), l.location)
                DetailRow(stringResource(R.string.land_field_size),
                    stringResource(R.string.land_field_size_value_format, l.sizeM2))
                DetailRow(stringResource(R.string.land_field_certificate), l.sertifikatId)
                DetailRow(stringResource(R.string.land_field_buy_price), l.buyPrice.toRupiah())
                DetailRow(stringResource(R.string.land_edit_date_label),
                    l.purchaseDate.toAbsoluteId(),
                    last = l.currentValue == null)
                if (l.currentValue != null) {
                    DetailRow(stringResource(R.string.land_field_current_value),
                        l.currentValue.toRupiah(), last = true)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // "PEMBAYARAN PAJAK (PBB)" label with "+ Tambah" link on the right.
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(stringResource(R.string.land_section_tax).uppercase(), color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { addTax = true }
                    .padding(horizontal = 4.dp, vertical = 4.dp)) {
                Icon(Icons.Outlined.Add, null, tint = sw.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(width = 2.dp, height = 1.dp))
                Text(stringResource(R.string.land_tax_add), color = sw.primary,
                    style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (taxes.isEmpty()) {
            SwCard { Text(stringResource(R.string.land_tax_empty), color = sw.inkMuted, style = SwType.Body) }
        } else SwCard(padding = PaddingValues(0.dp)) {
            Column {
                val totalTax = taxes.sumOf { it.amount }
                taxes.forEachIndexed { idx, t ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        // Receipt chip 36×36 r10 in warningSoft + warning fg.
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(sw.warningSoft)) {
                            Icon(Icons.Outlined.Receipt, null, tint = sw.warning,
                                modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.note ?: stringResource(R.string.land_tax_default_note), color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                            Text(t.date.toAbsoluteId(), color = sw.inkSubtle,
                                style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                        RupiahText(value = t.amount,
                            style = SwType.Amount.copy(fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
                    }
                    if (idx < taxes.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                    }
                }
                // "TOTAL DIBAYAR" footer row in bg (slightly inset visually).
                Box(Modifier.fillMaxWidth().background(sw.bg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.land_tax_total).uppercase(),
                            color = sw.inkSubtle,
                            style = SwType.SectionLabel.copy(fontSize = 11.sp,
                                fontWeight = FontWeight.Bold))
                        Spacer(Modifier.weight(1f))
                        RupiahText(value = totalTax,
                            style = SwType.Amount.copy(fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        // Delete moved to the more-menu in production; proto shows no in-page
        // destructive button. Keep it as a Ghost-tinted small link so power
        // users still have a path, but it doesn't dominate the layout.
        Text(stringResource(R.string.land_delete), color = sw.danger,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.clickable { viewModel.delete(); onBack() }
                .padding(horizontal = 4.dp, vertical = 6.dp))
    }
    if (addTax) {
        val accounts by viewModel.accounts.collectAsState()
        AddTaxSheet(
            accounts = accounts,
            onSave = { d, amt, accId, n ->
                viewModel.addTax(d, amt, accId, n); addTax = false
            },
            onDismiss = { addTax = false },
        )
    }
}

@Composable
private fun AddTaxSheet(
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

    SwPickerSheet(title = stringResource(R.string.land_tax_sheet_title), onDismiss = onDismiss) {
        SwField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.land_tax_amount), prefix = "Rp", rupiah = true, keyboardType = KeyboardType.Number)
        SwField(value = note, onValueChange = { note = it },
            label = stringResource(R.string.reconcile_note_label),
            placeholder = stringResource(R.string.land_tax_note_placeholder))

        // Date row
        Text(
            stringResource(R.string.common_date_label),
            color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
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
            Text(
                date.toAbsoluteId(),
                color = sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        }
        Spacer(Modifier.height(6.dp))

        Text(
            stringResource(R.string.land_tax_account_label),
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
                account?.name ?: stringResource(R.string.land_tax_account_empty),
                color = if (account == null) sw.inkSubtle else sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (account != null) stringResource(R.string.land_tax_effect_format, account.name)
            else stringResource(R.string.land_tax_effect_none),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 11.sp),
        )
        Spacer(Modifier.height(12.dp))
        SwButton(stringResource(R.string.action_save), onClick = {
            onSave(date, amount.toLongOrNull() ?: 0L, accountId, note.ifBlank { null })
        }, enabled = amount.isNotBlank())
    }

    if (showAccountPicker) {
        AccountPickerSheet(
            accounts = accounts,
            selectedId = accountId,
            onPick = { accountId = it.id },
            onDismiss = { showAccountPicker = false },
        )
    }
    if (showDatePicker) {
        DatePickerSheet(
            selected = date,
            onPick = { date = it },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
fun LandEditScreen(
    id: String?, onClose: () -> Unit,
    viewModel: LandEditViewModel = hiltViewModel(key = "land-edit-${id ?: "new"}"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.load(id) }
    val s by viewModel.state.collectAsState()
    SimpleSettingsScreen(
        title = if (s.id == null) stringResource(R.string.land_edit_new_title) else stringResource(R.string.land_edit_edit_title),
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
        SwField(s.name, { v -> viewModel.set { it.copy(name = v) } },
            label = stringResource(R.string.land_edit_name), placeholder = stringResource(R.string.land_edit_name_placeholder))
        SwField(s.location, { v -> viewModel.set { it.copy(location = v) } },
            label = stringResource(R.string.land_field_location))
        SwField(s.sertifikat, { v -> viewModel.set { it.copy(sertifikat = v) } },
            label = stringResource(R.string.land_edit_certificate))
        SwField(s.size, { v -> viewModel.set { it.copy(size = v.filter { c -> c.isDigit() }) } },
            label = stringResource(R.string.land_field_size), suffix = "m²", keyboardType = KeyboardType.Number)
        SwField(s.buyPrice, { v -> viewModel.set { it.copy(buyPrice = v.filter { c -> c.isDigit() }) } },
            label = stringResource(R.string.land_field_buy_price), prefix = "Rp", rupiah = true, keyboardType = KeyboardType.Number)
        // Purchase date — placed right after buy price since it's part of
        // "when/how much I acquired this". Backdate-friendly (allowFuture=
        // false in the picker by default) so users can log property they
        // bought months or years ago.
        var datePickerOpen by remember { mutableStateOf(false) }
        com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton(
            label = stringResource(R.string.land_edit_date_label),
            value = s.purchaseDate.toAbsoluteId(),
            leadingIcon = Icons.Outlined.CalendarToday,
            onClick = { datePickerOpen = true },
        )
        SwField(s.currentValue, { v -> viewModel.set { it.copy(currentValue = v.filter { c -> c.isDigit() }) } },
            label = stringResource(R.string.land_edit_current_value_label), prefix = "Rp", rupiah = true,
            keyboardType = KeyboardType.Number,
            hint = stringResource(R.string.land_edit_current_value_hint))

        if (datePickerOpen) {
            com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet(
                selected = s.purchaseDate,
                onPick = { picked -> viewModel.set { it.copy(purchaseDate = picked) } },
                onDismiss = { datePickerOpen = false },
            )
        }
    }
}

// DetailRow per proto screens-assets.jsx:512-521 — 13/14 sized text with a
// 1dp border between rows (suppressed via `last=true` on the final entry).
@Composable
private fun DetailRow(label: String, value: String, last: Boolean = false) {
    val sw = SwTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(label, color = sw.inkMuted,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium))
            Spacer(Modifier.weight(1f))
            Text(value, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
        }
        if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
    }
}
