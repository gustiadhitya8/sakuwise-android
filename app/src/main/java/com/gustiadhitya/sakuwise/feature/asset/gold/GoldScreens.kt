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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.gustiadhitya.sakuwise.app.MainViewModel
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.outlined.PhotoCamera
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
                id = g.id,
                weight = com.gustiadhitya.sakuwise.core.common.formatMilliGrams(g.weightMilliGram),
                buyPrice = g.buyPrice.toString(), serial = g.serial.orEmpty(),
                date = g.purchaseDate, note = g.note.orEmpty(),
                kind = g.kind,
                loaded = true,
            )
        }
    }
    fun setWeight(v: String) { _state.value = _state.value.copy(weight = v) }
    fun setBuyPrice(v: String) { _state.value = _state.value.copy(buyPrice = v) }
    fun setSerial(v: String) { _state.value = _state.value.copy(serial = v) }
    fun setDate(v: LocalDate) { _state.value = _state.value.copy(date = v) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v) }
    fun setKind(k: com.gustiadhitya.sakuwise.core.domain.model.GoldKind) {
        _state.value = _state.value.copy(kind = k)
    }
    fun submit(onDone: () -> Unit) {
        val s = _state.value
        viewModelScope.launch {
            repo.upsert(
                GoldAsset(
                    id = s.id ?: UUID.randomUUID().toString(),
                    purchaseDate = s.date,
                    weightMilliGram = com.gustiadhitya.sakuwise.core.common.parseGramsToMilliGrams(s.weight),
                    kind = s.kind,
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
    val kind: com.gustiadhitya.sakuwise.core.domain.model.GoldKind =
        com.gustiadhitya.sakuwise.core.domain.model.GoldKind.Physical,
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
    val totalWeightMg = items.filter { it.status == AssetStatus.Held }.sumOf { it.weightMilliGram }
    val totalWeightLabel = com.gustiadhitya.sakuwise.core.common.formatMilliGrams(totalWeightMg)
    // Kind-aware total: digital and physical use separate per-gram prices
    // because the local spread on each market is usually different.
    fun priceFor(g: GoldAsset): Long =
        if (g.kind == com.gustiadhitya.sakuwise.core.domain.model.GoldKind.Digital) prefs.goldPriceDigital
        else prefs.goldPriceGlobal
    val totalValue = items.filter { it.status == AssetStatus.Held }.sumOf { it.valueAt(priceFor(it)) }
    val totalBuy = items.filter { it.status == AssetStatus.Held }.sumOf { it.buyPrice }
    val profit = totalValue - totalBuy
    var sortMode by remember {
        mutableStateOf(com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC)
    }
    val sortedItems = remember(items, sortMode, prefs.goldPriceGlobal, prefs.goldPriceDigital) {
        items.sortedWith(
            when (sortMode) {
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC ->
                    compareByDescending { it.purchaseDate }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_ASC ->
                    compareBy { it.purchaseDate }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_DESC ->
                    compareByDescending { it.valueAt(priceFor(it)) }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_ASC ->
                    compareBy { it.valueAt(priceFor(it)) }
            },
        )
    }

    SimpleSettingsScreen(
        title = stringResource(R.string.gold_title), onBack = onBack,
        actions = {
            com.gustiadhitya.sakuwise.core.designsystem.components.SwSortMenu(
                options = com.gustiadhitya.sakuwise.core.designsystem.components.assetSortOptions(),
                selected = sortMode,
                onPick = { sortMode = it },
                modifier = Modifier.padding(end = 8.dp),
            )
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
        // Hero per proto screens-assets.jsx:368-388 — saffron background
        // (radius 20, padding 18) with the gold watermark anchored bottom-
        // right at offset(-20, -20) and size 140 so it intentionally bleeds
        // past the card edge under the rounded clip.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                // Use warningSoft in dark mode so the bright-amber card doesn't
                // scream against the near-black bg. Light mode stays full amber.
                .background(if (sw.isDark) sw.warningSoft else sw.warning)
                .padding(18.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_gold),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(72.dp),
                )
            }
            Column {
                Text(stringResource(R.string.gold_hero_label_format, totalWeightLabel),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = totalValue, color = Color.White,
                    style = SwType.AmountXL.copy(fontSize = 30.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold))
                if (totalBuy > 0L) {
                    Spacer(Modifier.height(8.dp))
                    val pct = (profit.toFloat() / totalBuy.toFloat()) * 100f
                    val sign = if (profit >= 0L) "+" else "−"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            stringResource(
                                R.string.gold_hero_profit_format,
                                sign, kotlin.math.abs(profit).toRupiahShort(prefix = ""),
                                sign, "%.1f".format(kotlin.math.abs(pct)),
                            ),
                            color = Color.White,
                            style = SwType.LabelStrong.copy(fontSize = 11.sp, lineHeight = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                        )
                    }
                }
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
                val idFmt = java.text.NumberFormat.getInstance(java.util.Locale("id","ID"))
                Text(
                    stringResource(
                        R.string.gold_price_card_sub_dual_format,
                        idFmt.format(prefs.goldPriceGlobal),
                        idFmt.format(prefs.goldPriceDigital),
                    ),
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
            // "{N} BATCH DIPEGANG" section label per proto.
            val heldCount = items.count { it.status == AssetStatus.Held }
            Text(
                stringResource(R.string.gold_batches_section_format, heldCount),
                color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp,
                    fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(4.dp))
            // Single SwCard with all rows + dividers — proto pattern.
            SwCard(padding = PaddingValues(0.dp)) {
                Column {
                    sortedItems.forEachIndexed { i, g ->
                        val perValue = g.valueAt(priceFor(g))
                        val weightLabel = com.gustiadhitya.sakuwise.core.common.formatMilliGrams(g.weightMilliGram)
                        val growth = if (g.buyPrice > 0L)
                            ((perValue - g.buyPrice).toFloat() / g.buyPrice.toFloat()) * 100f
                        else 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(g.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            // 56×56 r16 chip with warningSoft bg per proto.
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(sw.warningSoft),
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_gold),
                                    contentDescription = null,
                                    tint = sw.warning, modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (g.serial != null)
                                        stringResource(R.string.gold_item_title_with_serial_format, weightLabel, g.serial)
                                    else stringResource(R.string.gold_item_title_format, weightLabel),
                                    color = sw.ink,
                                    style = SwType.LabelStrong.copy(fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold))
                                Text(stringResource(R.string.gold_item_buy_date_format,
                                    g.purchaseDate.toAbsoluteId()),
                                    color = sw.inkMuted,
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                RupiahText(value = perValue,
                                    style = SwType.Amount.copy(fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"))
                                if (g.buyPrice > 0L) {
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
                        if (i < sortedItems.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp)
                                .padding(start = 84.dp)
                                .background(sw.border))
                        }
                    }
                }
            }
        }
    }

    if (showPriceSheet) {
        GoldPriceInlineSheet(
            physicalPrice = prefs.goldPriceGlobal,
            digitalPrice = prefs.goldPriceDigital,
            onSave = { newPhysical, newDigital ->
                mutator.setGoldPrice(newPhysical)
                mutator.setGoldPriceDigital(newDigital)
                showPriceSheet = false
            },
            onDismiss = { showPriceSheet = false },
        )
    }
}

@Composable
private fun GoldPriceInlineSheet(
    physicalPrice: Long,
    digitalPrice: Long,
    onSave: (Long, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var rawPhysical by remember { mutableStateOf(physicalPrice.toString()) }
    var rawDigital by remember { mutableStateOf(digitalPrice.toString()) }
    SwPickerSheet(title = stringResource(R.string.gold_price_sheet_title), onDismiss = onDismiss) {
        Text(
            stringResource(R.string.gold_price_sheet_intro),
            color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
        )
        Spacer(Modifier.height(12.dp))
        SwField(
            value = if (rawPhysical == "0") "" else rawPhysical,
            onValueChange = { rawPhysical = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.gold_price_field_label_physical),
            prefix = "Rp", rupiah = true, suffix = "/ gram",
            keyboardType = KeyboardType.Number,
        )
        Spacer(Modifier.height(10.dp))
        SwField(
            value = if (rawDigital == "0") "" else rawDigital,
            onValueChange = { rawDigital = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.gold_price_field_label_digital),
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
            onClick = {
                onSave(rawPhysical.toLongOrNull() ?: 0L, rawDigital.toLongOrNull() ?: 0L)
            },
            enabled = (rawPhysical.toLongOrNull() ?: 0L) > 0 &&
                (rawDigital.toLongOrNull() ?: 0L) > 0,
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
        else stringResource(
            R.string.gold_item_title_format,
            com.gustiadhitya.sakuwise.core.common.formatMilliGrams(g.weightMilliGram),
        ),
        onBack = onBack,
        actions = {
            // Proto screens-assets.jsx:466 — transparent 40×40 r12 with a
            // MoreHoriz glyph. Tap currently routes to edit; long-press is
            // available for delete via the action sheet at the bottom.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit),
            ) { Icon(Icons.Outlined.MoreHoriz, stringResource(R.string.action_edit),
                tint = sw.ink, modifier = Modifier.size(22.dp)) }
        },
    ) {
        if (g == null) {
            Text(stringResource(R.string.loading), color = sw.inkMuted, style = SwType.Body)
            return@SimpleSettingsScreen
        }
        val priceForKind =
            if (g.kind == com.gustiadhitya.sakuwise.core.domain.model.GoldKind.Digital) prefs.goldPriceDigital
            else prefs.goldPriceGlobal
        val currentValue = g.valueAt(priceForKind)
        val profit = currentValue - g.buyPrice
        val weightLabel = com.gustiadhitya.sakuwise.core.common.formatMilliGrams(g.weightMilliGram)
        // Gold detail hero — same proto pattern (watermark bottom-right with
        // overflow). 34sp amount per the detail screen's slightly more
        // generous spec.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (sw.isDark) sw.warningSoft else sw.warning)
                .padding(18.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_gold),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(72.dp),
                )
            }
            Column {
                Text(stringResource(R.string.gold_hero_value_label),
                    color = Color.White.copy(alpha = 0.85f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp, lineHeight = 14.sp,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = currentValue, color = Color.White,
                    style = SwType.AmountXL.copy(fontSize = 30.sp, lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold))
                Spacer(Modifier.height(10.dp))
                GoldProfitChip(profit = profit, buyPrice = g.buyPrice.coerceAtLeast(1L))
            }
        }
        Spacer(Modifier.height(14.dp))
        // Per proto: "DETAIL" section label above the card.
        Text(stringResource(R.string.gold_section_detail),
            color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp,
                fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
        Spacer(Modifier.height(4.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                val pricePerGram =
                    if (g.weightMilliGram > 0L) g.buyPrice * 1000L / g.weightMilliGram else 0L
                DetailRow(stringResource(R.string.gold_field_weight),
                    stringResource(R.string.gold_field_weight_value_format, weightLabel))
                DetailRow(stringResource(R.string.gold_field_buy_date), g.purchaseDate.toAbsoluteId())
                DetailRow(stringResource(R.string.gold_field_buy_price), g.buyPrice.toRupiah())
                DetailRow(stringResource(R.string.gold_field_price_per_gram),
                    if (g.weightMilliGram > 0L) pricePerGram.toRupiah() else stringResource(R.string.gold_dash))
                DetailRow(stringResource(R.string.gold_field_serial),
                    g.serial ?: stringResource(R.string.gold_dash), last = true)
            }
        }
        Spacer(Modifier.height(20.dp))
        var showSellSheet by remember { mutableStateOf(false) }
        if (g.status == com.gustiadhitya.sakuwise.core.domain.model.AssetStatus.Held) {
            // Proto: Secondary "Tambah foto" with camera icon, then Outline
            // "Catat penjualan" with arrow_up_right.
            SwButton(
                text = stringResource(R.string.gold_add_photo),
                onClick = { /* photo flow handled via edit screen */ onEdit() },
                variant = SwButtonVariant.Secondary,
                leading = { Icon(Icons.Outlined.PhotoCamera, null,
                    tint = sw.ink, modifier = Modifier.size(16.dp)) },
            )
            Spacer(Modifier.height(10.dp))
            SwButton(
                text = stringResource(R.string.gold_record_sale),
                onClick = { showSellSheet = true },
                variant = SwButtonVariant.Outline,
                leading = { Icon(Icons.AutoMirrored.Outlined.CallMade, null,
                    tint = sw.ink, modifier = Modifier.size(16.dp)) },
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
        // Gold type — Physical (ANTAM/UBS/bar) vs Digital (Pegadaian/Pluang).
        // Each kind reads its own per-gram price set on the Emas list screen.
        Text(stringResource(R.string.gold_kind_label), color = sw.inkMuted,
            style = SwType.Caption.copy(fontSize = 12.sp),
            modifier = Modifier.padding(bottom = 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            com.gustiadhitya.sakuwise.core.domain.model.GoldKind.entries.forEach { k ->
                val selected = state.kind == k
                val label = if (k == com.gustiadhitya.sakuwise.core.domain.model.GoldKind.Physical)
                    stringResource(R.string.gold_kind_physical)
                else stringResource(R.string.gold_kind_digital)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) sw.warningSoft else sw.surface)
                        .border(
                            1.5.dp,
                            if (selected) sw.warning else sw.border,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { viewModel.setKind(k) }
                        .padding(horizontal = 12.dp),
                ) {
                    Text(label, color = if (selected) sw.warning else sw.ink,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        SwField(value = state.weight, onValueChange = viewModel::setWeight,
            label = stringResource(R.string.gold_edit_weight_label), suffix = "gram",
            keyboardType = KeyboardType.Number)
        SwField(value = state.buyPrice, onValueChange = viewModel::setBuyPrice,
            label = stringResource(R.string.gold_edit_buy_price_label), prefix = "Rp", rupiah = true,
            keyboardType = KeyboardType.Number)
        // Purchase date — backdating lets the user log gold they bought last
        // year. allowFuture = false because future-dated purchases would
        // confuse the "since-buy" profit chip on the detail screen.
        var datePickerOpen by remember { mutableStateOf(false) }
        com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton(
            label = stringResource(R.string.gold_edit_date_label),
            value = state.date.toAbsoluteId(),
            leadingIcon = Icons.Outlined.CalendarToday,
            onClick = { datePickerOpen = true },
        )
        SwField(value = state.serial, onValueChange = viewModel::setSerial,
            label = stringResource(R.string.gold_edit_serial_label),
            placeholder = stringResource(R.string.gold_edit_serial_placeholder))
        SwField(value = state.note, onValueChange = viewModel::setNote,
            label = stringResource(R.string.gold_edit_note_label))

        if (datePickerOpen) {
            com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet(
                selected = state.date,
                onPick = viewModel::setDate,
                onDismiss = { datePickerOpen = false },
            )
        }
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
            // Darkened tint instead of white 0.18 — white-on-white-on-saffron
            // had near-zero contrast. Black 0.22 darkens the saffron so the
            // white profit text reads clearly.
            .background(Color.Black.copy(alpha = 0.22f))
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
private fun DetailRow(label: String, value: String, last: Boolean = false) {
    val sw = SwTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(label, color = sw.inkMuted,
                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium))
            Spacer(Modifier.weight(1f))
            Text(value, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
        }
        if (!last) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
        }
    }
}
