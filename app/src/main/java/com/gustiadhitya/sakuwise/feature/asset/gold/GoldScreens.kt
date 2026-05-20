package com.gustiadhitya.sakuwise.feature.asset.gold

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
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Edit
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
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.AssetStatus
import com.gustiadhitya.sakuwise.core.domain.model.GoldAsset
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.settings.sub.PrefMutatorViewModel
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

// ─── ViewModels ─────────────────────────────────────────────────────────────
@HiltViewModel
class GoldListViewModel @Inject constructor(private val repo: GoldRepository) : ViewModel() {
    val items: StateFlow<List<GoldAsset>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@HiltViewModel
class GoldDetailViewModel @Inject constructor(
    private val repo: GoldRepository,
    private val accountRepo: com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository,
    private val markSold: com.gustiadhitya.sakuwise.core.domain.usecase.MarkGoldSoldUseCase,
) : ViewModel() {
    private val _id = MutableStateFlow<String?>(null)
    fun bind(id: String) { if (_id.value != id) _id.value = id }
    val item: StateFlow<GoldAsset?> = kotlinx.coroutines.flow.flow {
        _id.collect { id -> if (id == null) emit(null) else repo.observeById(id).collect { emit(it) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val accounts: StateFlow<List<com.gustiadhitya.sakuwise.core.domain.model.Account>> =
        accountRepo.observeActive()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete() { val id = _id.value ?: return; viewModelScope.launch { repo.delete(id) } }

    fun recordSale(date: LocalDate, price: Long, accountId: String?, note: String?) {
        val id = _id.value ?: return
        viewModelScope.launch { markSold(id, date, price, accountId, note) }
    }
}

@HiltViewModel
class GoldEditViewModel @Inject constructor(private val repo: GoldRepository) : ViewModel() {
    private val _state = MutableStateFlow(GoldEditState())
    val state: StateFlow<GoldEditState> = _state
    /** Always re-fetch from DB so unsaved Tipe picks from earlier visits don't stick. */
    fun load(id: String?) {
        if (id == null) { _state.value = GoldEditState(loaded = true); return }
        viewModelScope.launch {
            val g = repo.observeById(id).first()
            _state.value = if (g == null) GoldEditState(loaded = true)
            else GoldEditState(
                id = g.id, weight = g.weightGram.toString(),
                buyPrice = g.buyPrice.toString(), serial = g.serial.orEmpty(),
                date = g.purchaseDate, note = g.note.orEmpty(), loaded = true,
            )
        }
    }
    fun setWeight(v: String) { _state.value = _state.value.copy(weight = v) }
    fun setBuyPrice(v: String) { _state.value = _state.value.copy(buyPrice = v) }
    fun setSerial(v: String) { _state.value = _state.value.copy(serial = v) }
    fun setDate(v: LocalDate) { _state.value = _state.value.copy(date = v) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }
    fun submit(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            repo.upsert(
                GoldAsset(
                    id = s.id ?: UUID.randomUUID().toString(),
                    purchaseDate = s.date,
                    weightGram = s.weight.toIntOrNull() ?: 0,
                    serial = s.serial.ifBlank { null },
                    buyPrice = s.buyPrice.toLongOrNull() ?: 0L,
                    note = s.note.ifBlank { null },
                    status = AssetStatus.Held,
                    soldDate = null, soldPrice = null,
                ),
            )
            onDone()
        }
    }
}

data class GoldEditState(
    val id: String? = null,
    val weight: String = "",
    val buyPrice: String = "",
    val serial: String = "",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val loaded: Boolean = false,
)

// ─── Screens ────────────────────────────────────────────────────────────────
@Composable
fun GoldListScreen(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    onAdd: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    viewModel: GoldListViewModel = hiltViewModel(),
    mutator: PrefMutatorViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val items by viewModel.items.collectAsState()
    val prefs by main.prefs.collectAsState()
    val pricePerGram = prefs.goldPriceGlobal
    var showPriceSheet by remember { mutableStateOf(false) }
    val totalWeight = items.filter { it.status == AssetStatus.Held }.sumOf { it.weightGram }
    val totalValue = totalWeight * pricePerGram
    val totalBuy = items.filter { it.status == AssetStatus.Held }.sumOf { it.buyPrice }
    val profit = totalValue - totalBuy

    SimpleSettingsScreen(
        title = stringResource(R.string.gold_title), onBack = onBack,
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.warning)
                    .clickable(onClick = onAdd),
            ) { Icon(Icons.Outlined.Add, stringResource(R.string.gold_add_cd), tint = Color.White, modifier = Modifier.size(20.dp)) }
        },
    ) {
        // Hero — per proto screens-assets-detail.jsx LandDetail pattern adapted
        // for Gold: warning bg + WHITE text everywhere, profit/loss shown as a
        // translucent-white pill chip (NOT green/red, which blends with the
        // warning background). Includes an arrow_up_right glyph for direction.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.warning)
                .padding(20.dp),
        ) {
            Column {
                Text(stringResource(R.string.gold_hero_label_format, totalWeight),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = totalValue, color = Color.White, style = SwType.AmountXL)
                Spacer(Modifier.height(10.dp))
                GoldProfitChip(profit = profit, buyPrice = totalBuy.coerceAtLeast(1L))
            }
        }
        Spacer(Modifier.height(14.dp))

        // Global gold price — inline in Gold menu (per prototype)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(12.dp))
                .clickable { showPriceSheet = true }
                .padding(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(sw.warningSoft),
            ) { Icon(Icons.Outlined.AutoAwesome, null, tint = sw.warning, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.gold_price_card_title), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                Text(
                    stringResource(R.string.gold_price_card_sub_format,
                        java.text.NumberFormat.getInstance(java.util.Locale("id","ID")).format(pricePerGram)),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp, fontFeatureSettings = "tnum"),
                )
            }
            Icon(Icons.Outlined.Edit, null, tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(14.dp))

        if (items.isEmpty()) {
            SwCard {
                Text(stringResource(R.string.gold_empty),
                    color = sw.inkMuted, style = SwType.Body)
            }
        } else {
            items.forEach { g ->
                SwCard(
                    modifier = Modifier.padding(vertical = 4.dp),
                    onClick = { onItemClick(g.id) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(sw.warning.copy(alpha = 0.18f)),
                        ) { Icon(Icons.Outlined.Diamond, null, tint = sw.warning, modifier = Modifier.size(22.dp)) }
                        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (g.serial != null)
                                    stringResource(R.string.gold_item_title_with_serial_format, g.weightGram, g.serial)
                                else stringResource(R.string.gold_item_title_format, g.weightGram),
                                color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                            Text(stringResource(R.string.gold_item_buy_date_format, g.purchaseDate.toAbsoluteId()),
                                color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 11.sp))
                        }
                        RupiahText(value = g.weightGram * pricePerGram, short = true,
                            style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    if (showPriceSheet) {
        GoldPriceInlineSheet(
            currentPrice = pricePerGram,
            onSave = { newPrice ->
                mutator.setGoldPrice(newPrice); showPriceSheet = false
            },
            onDismiss = { showPriceSheet = false },
        )
    }
}

@Composable
private fun GoldPriceInlineSheet(
    currentPrice: Long,
    onSave: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var raw by remember { mutableStateOf(currentPrice.toString()) }
    val formatted = raw.toLongOrNull()?.let {
        java.text.NumberFormat.getInstance(java.util.Locale("id","ID")).format(it)
    } ?: ""
    SwPickerSheet(title = stringResource(R.string.gold_price_sheet_title), onDismiss = onDismiss) {
        Text(
            stringResource(R.string.gold_price_sheet_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(12.dp))
        SwField(
            value = formatted,
            onValueChange = { raw = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.gold_price_field_label),
            prefix = "Rp", rupiah = true, suffix = "/ gram",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.warningSoft)
                .border(1.dp, sw.warning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            Text(
                stringResource(R.string.gold_price_tip),
                color = sw.inkMuted, style = SwType.Body.copy(fontSize = 12.sp),
            )
        }
        Spacer(Modifier.height(16.dp))
        SwButton(
            text = stringResource(R.string.action_save),
            onClick = { onSave(raw.toLongOrNull() ?: 0L) },
            enabled = (raw.toLongOrNull() ?: 0L) > 0,
        )
    }
}

@Composable
fun GoldDetailScreen(
    id: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    main: MainViewModel = hiltViewModel(),
    viewModel: GoldDetailViewModel = hiltViewModel(key = "gold-$id"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.bind(id) }
    val item by viewModel.item.collectAsState()
    val prefs by main.prefs.collectAsState()
    val g = item

    SimpleSettingsScreen(
        title = if (g == null) stringResource(R.string.gold_detail_default_title)
        else stringResource(R.string.gold_item_title_format, g.weightGram),
        onBack = onBack,
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.primary)
                    .clickable(onClick = onEdit),
            ) { Text(stringResource(R.string.action_edit), color = sw.onPrimary,
                style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)) }
        },
    ) {
        if (g == null) {
            Text(stringResource(R.string.loading), color = sw.inkMuted, style = SwType.Body)
            return@SimpleSettingsScreen
        }
        val currentValue = g.weightGram * prefs.goldPriceGlobal
        val profit = currentValue - g.buyPrice
        // Hero — see GoldListScreen hero for the proto compliance notes. White-
        // on-warning with a translucent-white profit chip so the gain/loss
        // text doesn't blend into the warning background.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.warning)
                .padding(20.dp),
        ) {
            // Diamond watermark behind the content per proto land-detail
            // pattern (icon at -20/-30 with opacity 0.18).
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 0.dp, bottom = 0.dp),
            ) {
                Icon(
                    Icons.Outlined.Diamond, null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(140.dp),
                )
            }
            Column {
                Text(stringResource(R.string.gold_hero_value_label),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = currentValue, color = Color.White, style = SwType.AmountXL)
                Spacer(Modifier.height(10.dp))
                GoldProfitChip(profit = profit, buyPrice = g.buyPrice.coerceAtLeast(1L))
            }
        }
        Spacer(Modifier.height(14.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                DetailRow(stringResource(R.string.gold_field_weight),
                    stringResource(R.string.gold_field_weight_value_format, g.weightGram))
                DetailRow(stringResource(R.string.gold_field_buy_date), g.purchaseDate.toAbsoluteId())
                DetailRow(stringResource(R.string.gold_field_buy_price), g.buyPrice.toString())
                DetailRow(stringResource(R.string.gold_field_price_per_gram),
                    if (g.weightGram > 0) (g.buyPrice / g.weightGram).toString() else stringResource(R.string.gold_dash))
                DetailRow(stringResource(R.string.gold_field_serial), g.serial ?: stringResource(R.string.gold_dash))
            }
        }
        Spacer(Modifier.height(20.dp))
        var showSellSheet by remember { mutableStateOf(false) }
        if (g.status == com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Held) {
            SwButton(
                text = stringResource(R.string.gold_record_sale),
                onClick = { showSellSheet = true },
                variant = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant.Secondary,
            )
            Spacer(Modifier.height(10.dp))
        } else {
            // Sold summary — read-only
            Box(modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(sw.successSoft)
                .padding(14.dp)) {
                Column {
                    Text(stringResource(R.string.gold_sold_label), color = sw.success,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(
                            R.string.gold_sold_summary_format,
                            g.soldDate?.toAbsoluteId() ?: "?",
                        ),
                        color = sw.success,
                        style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    )
                    g.soldPrice?.let {
                        Spacer(Modifier.height(2.dp))
                        RupiahText(value = it, style = SwType.AmountL.copy(fontSize = 18.sp),
                            color = sw.success)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        SwButton(text = stringResource(R.string.gold_delete), onClick = { viewModel.delete(); onBack() },
            variant = com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant.Danger)
        if (showSellSheet) {
            val accounts by viewModel.accounts.collectAsState()
            RecordSaleSheet(
                defaultPrice = currentValue,
                accounts = accounts,
                onSubmit = { d, price, accId, n ->
                    viewModel.recordSale(d, price, accId, n)
                    showSellSheet = false
                },
                onDismiss = { showSellSheet = false },
            )
        }
    }
}

@Composable
private fun RecordSaleSheet(
    defaultPrice: Long,
    accounts: List<com.gustiadhitya.sakuwise.core.domain.model.Account>,
    onSubmit: (LocalDate, Long, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var price by remember { mutableStateOf(defaultPrice.toString()) }
    var note by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf<String?>(null) }
    var showAcct by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDate by remember { mutableStateOf(false) }
    val account = accounts.firstOrNull { it.id == accountId }
    com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet(
        title = stringResource(R.string.gold_record_sale),
        onDismiss = onDismiss,
    ) {
        Text(stringResource(R.string.gold_sale_intro), color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp))
        Spacer(Modifier.height(12.dp))
        SwField(
            value = price,
            onValueChange = { price = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.gold_sale_price_label),
            prefix = "Rp", rupiah = true,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        )
        SwField(
            value = note, onValueChange = { note = it },
            label = stringResource(R.string.reconcile_note_label),
            placeholder = stringResource(R.string.gold_sale_note_placeholder),
        )

        // Date
        Text(stringResource(R.string.common_date_label), color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(12.dp))
            .clickable { showDate = true }
            .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart) {
            Text(date.toAbsoluteId(), color = sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp))
        }
        Spacer(Modifier.height(6.dp))

        // Account selector
        Text(stringResource(R.string.gold_sale_account_label), color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(12.dp))
            .clickable { showAcct = true }
            .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart) {
            Text(
                account?.name ?: stringResource(R.string.gold_sale_account_empty),
                color = if (account == null) sw.inkSubtle else sw.ink,
                style = SwType.Body.copy(fontSize = 14.sp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (account != null) stringResource(R.string.gold_sale_effect_format, account.name)
            else stringResource(R.string.gold_sale_effect_none),
            color = sw.inkSubtle,
            style = SwType.LabelSmall.copy(fontSize = 11.sp),
        )
        Spacer(Modifier.height(14.dp))
        SwButton(
            text = stringResource(R.string.gold_sale_confirm),
            onClick = { onSubmit(date, price.toLongOrNull() ?: 0L, accountId, note.ifBlank { null }) },
            enabled = (price.toLongOrNull() ?: 0L) > 0,
        )
    }
    if (showAcct) {
        com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet(
            accounts = accounts,
            selectedId = accountId,
            onPick = { accountId = it.id },
            onDismiss = { showAcct = false },
        )
    }
    if (showDate) {
        com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet(
            selected = date,
            onPick = { date = it },
            onDismiss = { showDate = false },
        )
    }
}


@Composable
fun GoldEditScreen(
    id: String?,
    onClose: () -> Unit,
    viewModel: GoldEditViewModel = hiltViewModel(key = "gold-edit-${id ?: "new"}"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.load(id) }
    val state by viewModel.state.collectAsState()
    SimpleSettingsScreen(
        title = if (state.id == null) stringResource(R.string.gold_edit_new_title)
        else stringResource(R.string.gold_edit_edit_title),
        onBack = onClose,
        actions = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(sw.primary)
                    .clickable { viewModel.submit(onClose) }
                    .padding(horizontal = 14.dp),
            ) { Text(stringResource(R.string.action_save), color = sw.onPrimary,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)) }
        },
    ) {
        SwField(value = state.weight, onValueChange = viewModel::setWeight,
            label = stringResource(R.string.gold_edit_weight_label), suffix = "gram",
            keyboardType = KeyboardType.Number)
        SwField(value = state.buyPrice, onValueChange = viewModel::setBuyPrice,
            label = stringResource(R.string.gold_edit_buy_price_label), prefix = "Rp", rupiah = true,
            keyboardType = KeyboardType.Number)
        SwField(value = state.serial, onValueChange = viewModel::setSerial,
            label = stringResource(R.string.gold_edit_serial_label),
            placeholder = stringResource(R.string.gold_edit_serial_placeholder))
        SwField(value = state.note, onValueChange = viewModel::setNote,
            label = stringResource(R.string.gold_edit_note_label))
    }
}

/**
 * Per proto screens-assets-detail.jsx:97-100 — translucent-white pill that
 * floats inside the warning-tinted hero. Uses white text + trend arrow so the
 * gain/loss reads cleanly on the saffron background (green/red on warning
 * blends and disappears).
 */
@Composable
private fun GoldProfitChip(profit: Long, buyPrice: Long) {
    val pct = if (buyPrice > 0) (profit.toFloat() / buyPrice.toFloat()) * 100f else 0f
    val sign = if (profit >= 0) "+" else "−"
    val pctSign = if (pct >= 0f) "+" else "−"
    val abs = kotlin.math.abs(profit)
    val absPct = kotlin.math.abs(pct)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            if (profit >= 0) androidx.compose.material.icons.Icons.AutoMirrored.Outlined.TrendingUp
            else androidx.compose.material.icons.Icons.AutoMirrored.Outlined.TrendingDown,
            null, tint = Color.White, modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.size(width = 6.dp, height = 1.dp))
        Text(
            "$sign ${abs.toRupiahShort()} · $pctSign${"%.1f".format(absPct)}%",
            color = Color.White,
            style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(label, color = sw.inkMuted,
            style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.weight(1f))
        Text(value, color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
    }
}
