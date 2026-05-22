package com.gustiadhitya.sakuwise.feature.asset.deposit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Savings
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.DepositAsset
import com.gustiadhitya.sakuwise.core.domain.model.DepositSnapshot
import com.gustiadhitya.sakuwise.core.domain.model.DepositType
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.settings.sub.SimpleSettingsScreen
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet
import androidx.compose.foundation.border
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

data class DepositListRow(
    val deposit: DepositAsset,
    val latestBalance: Long,
    val firstBalance: Long,
    val snapshotCount: Int,
    val latestSnapshotDate: LocalDate?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DepositListViewModel @Inject constructor(private val repo: DepositRepository) : ViewModel() {
    val items: StateFlow<List<DepositListRow>> = repo.observeAll().flatMapLatest { list ->
        if (list.isEmpty()) flowOf(emptyList()) else combine(
            list.map { d ->
                combine(
                    repo.observeLatestSnapshot(d.id),
                    repo.observeSnapshots(d.id),
                ) { latest, snaps ->
                    DepositListRow(
                        deposit = d,
                        latestBalance = latest?.balance ?: 0L,
                        firstBalance = snaps.minByOrNull { it.date }?.balance ?: 0L,
                        snapshotCount = snaps.size,
                        latestSnapshotDate = latest?.date,
                    )
                }
            },
        ) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DepositDetailViewModel @Inject constructor(private val repo: DepositRepository) : ViewModel() {
    private val _id = MutableStateFlow<String?>(null)
    fun bind(id: String) { if (_id.value != id) _id.value = id }
    val item: StateFlow<DepositAsset?> = _id.flatMapLatest { id ->
        if (id == null) flowOf(null) else repo.observeById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val snapshots: StateFlow<List<DepositSnapshot>> = _id.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repo.observeSnapshots(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addSnapshot(date: LocalDate, balance: Long, note: String?) {
        val id = _id.value ?: return
        viewModelScope.launch {
            repo.upsertSnapshot(DepositSnapshot(UUID.randomUUID().toString(), id, date, balance, note))
        }
    }
    fun delete() { val id = _id.value ?: return; viewModelScope.launch { repo.delete(id) } }
}

@HiltViewModel
class DepositEditViewModel @Inject constructor(private val repo: DepositRepository) : ViewModel() {
    private val _s = MutableStateFlow(DepositEditState())
    val state: StateFlow<DepositEditState> = _s
    /** Always re-fetch from DB — see AccountEditViewModel for rationale. */
    fun load(id: String?) {
        if (id == null) { _s.value = DepositEditState(loaded = true); return }
        viewModelScope.launch {
            val d = repo.observeById(id).first()
            _s.value = if (d == null) DepositEditState(loaded = true)
            else DepositEditState(d.id, d.name, d.typeLabel, d.institutionInfo.orEmpty(), true)
        }
    }
    fun set(t: (DepositEditState) -> DepositEditState) { _s.value = t(_s.value) }
    fun submit(onDone: () -> Unit) {
        val s = _s.value
        viewModelScope.launch {
            repo.upsert(
                DepositAsset(
                    id = s.id ?: UUID.randomUUID().toString(),
                    name = s.name, typeLabel = s.type,
                    institutionInfo = s.institution.ifBlank { null },
                    note = null, active = true,
                ),
            )
            onDone()
        }
    }
}

data class DepositEditState(
    val id: String? = null,
    val name: String = "",
    val type: DepositType = DepositType.Deposito,
    val institution: String = "",
    val loaded: Boolean = false,
)

@Composable
fun DepositListScreen(
    onBack: () -> Unit, onItemClick: (String) -> Unit, onAdd: () -> Unit,
    viewModel: DepositListViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val items by viewModel.items.collectAsState()
    val total = items.sumOf { it.latestBalance }
    // Aggregate growth = (current total − baseline total) / baseline total,
    // where baseline is the first snapshot per item summed. Renders a +%
    // chip in the hero, matching the prototype's growth-pill pattern.
    val baseline = items.sumOf { it.firstBalance }
    val depGrowthPct: Float? =
        if (baseline > 0L && baseline != total) ((total - baseline).toFloat() / baseline.toFloat()) * 100f
        else null
    var sortMode by remember {
        mutableStateOf(com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC)
    }
    val sortedItems = remember(items, sortMode) {
        items.sortedWith(
            when (sortMode) {
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC ->
                    compareByDescending<DepositListRow> { it.latestSnapshotDate ?: LocalDate.MIN }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_ASC ->
                    compareBy { it.latestSnapshotDate ?: LocalDate.MAX }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_DESC ->
                    compareByDescending { it.latestBalance }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_ASC ->
                    compareBy { it.latestBalance }
            },
        )
    }
    SimpleSettingsScreen(
        title = stringResource(R.string.deposit_title), onBack = onBack,
        actions = {
            com.gustiadhitya.sakuwise.core.designsystem.components.SwSortMenu(
                options = com.gustiadhitya.sakuwise.core.designsystem.components.assetSortOptions(),
                selected = sortMode,
                onPick = { sortMode = it },
                modifier = Modifier.padding(end = 8.dp),
            )
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(sw.accent).clickable(onClick = onAdd)) {
                Icon(Icons.Outlined.Add, stringResource(R.string.deposit_add_cd),
                    tint = sw.fixedDarkOnMint, modifier = Modifier.size(20.dp))
            }
        },
    ) {
        // Hero per proto screens-assets-detail.jsx:188-201 — mint background
        // (radius 20, padding 18) with the deposit watermark anchored bottom-
        // right at offset(-20, -30) and size 140 so it intentionally bleeds
        // past the card edge under the rounded clip. matchParentSize keeps
        // the watermark layer from inflating the card past the content.
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(sw.accent).padding(18.dp),
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                // Bottom-right watermark, fully inside the card (no overflow,
                // no clipping). Size kept under the card's natural content
                // height so the whole icon stays visible.
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_deposit),
                    contentDescription = null,
                    tint = sw.fixedDarkOnMint.copy(alpha = 0.22f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(72.dp),
                )
            }
            Column {
                Text(stringResource(R.string.deposit_hero_label),
                    color = sw.fixedDarkOnMint.copy(alpha = 0.78f),
                    style = SwType.SectionLabel.copy(fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                RupiahText(value = total, color = sw.fixedDarkOnMint,
                    style = SwType.AmountXL.copy(fontSize = 30.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold))
                if (depGrowthPct != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(sw.fixedDarkOnMint.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            (if (depGrowthPct >= 0f) "+" else "−") +
                                "%.1f".format(kotlin.math.abs(depGrowthPct)) + "%",
                            color = sw.fixedDarkOnMint,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.deposit_hero_sub_format, items.size),
                    color = sw.fixedDarkOnMint.copy(alpha = 0.75f),
                    style = SwType.LabelSmall.copy(fontSize = 12.sp, lineHeight = 14.sp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.deposit_section_label),
            color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp,
                fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
        Spacer(Modifier.height(4.dp))
        if (items.isEmpty()) {
            SwCard {
                Text(stringResource(R.string.deposit_empty),
                    color = sw.inkMuted, style = SwType.Body)
            }
        } else {
            SwCard(padding = PaddingValues(0.dp)) {
                Column {
                    sortedItems.forEachIndexed { i, row ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(row.deposit.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp))
                                    .background(sw.accent.copy(alpha = 0.18f))) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.gustiadhitya.sakuwise.R.drawable.ic_asset_deposit),
                                    contentDescription = null,
                                    tint = sw.accent, modifier = Modifier.size(26.dp),
                                )
                            }
                            Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(row.deposit.name, color = sw.ink,
                                        style = SwType.LabelStrong.copy(fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold))
                                    // Tiny type tag pill per proto.
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(sw.bg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(row.deposit.typeLabel.code(),
                                            color = sw.inkSubtle,
                                            style = SwType.LabelSmall.copy(fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold))
                                    }
                                }
                                Text(stringResource(R.string.deposit_item_sub_format,
                                    row.deposit.typeLabel.code(), row.snapshotCount),
                                    color = sw.inkMuted,
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                            }
                            RupiahText(value = row.latestBalance,
                                style = SwType.Amount.copy(fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"))
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
}

@Composable
fun DepositDetailScreen(
    id: String, onBack: () -> Unit, onEdit: () -> Unit,
    viewModel: DepositDetailViewModel = hiltViewModel(key = "dep-$id"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.bind(id) }
    val d = viewModel.item.collectAsState().value
    val snapshots by viewModel.snapshots.collectAsState()
    var addSnap by remember { mutableStateOf(false) }
    if (d == null) return SimpleSettingsScreen(stringResource(R.string.deposit_detail_default_title), onBack) {
        Text(stringResource(R.string.loading), color = sw.inkMuted, style = SwType.Body)
    }
    val latest = snapshots.lastOrNull()
    var snapSort by remember {
        mutableStateOf(com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC)
    }
    val sortedSnapshots = remember(snapshots, snapSort) {
        snapshots.sortedWith(
            when (snapSort) {
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_DESC ->
                    compareByDescending<DepositSnapshot> { it.date }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.DATE_ASC ->
                    compareBy { it.date }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_DESC ->
                    compareByDescending { it.balance }
                com.gustiadhitya.sakuwise.core.designsystem.components.AssetSort.AMOUNT_ASC ->
                    compareBy { it.balance }
            },
        )
    }
    SimpleSettingsScreen(
        title = d.name, onBack = onBack,
        actions = {
            com.gustiadhitya.sakuwise.core.designsystem.components.SwSortMenu(
                options = com.gustiadhitya.sakuwise.core.designsystem.components.assetSortOptions(),
                selected = snapSort,
                onPick = { snapSort = it },
                modifier = Modifier.padding(end = 8.dp),
            )
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit)) {
                Icon(Icons.Outlined.MoreHoriz,
                    stringResource(R.string.action_edit),
                    tint = sw.ink, modifier = Modifier.size(22.dp))
            }
        },
    ) {
        // Hero — per proto screens-assets-detail.jsx:255-268. Surface bg with
        // border, SALDO TERBARU label + amount + green growth percentage on
        // the same baseline, "Per {date}" muted sub, and the snapshot line
        // chart embedded INSIDE the same card (not as a separate tile).
        val first = snapshots.firstOrNull()
        val growthPct = if (latest != null && first != null && first.balance > 0L)
            ((latest.balance - first.balance).toFloat() / first.balance.toFloat()) * 100f
        else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(sw.surface)
                .border(1.dp, sw.border, RoundedCornerShape(22.dp))
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 14.dp),
        ) {
            Column {
                Text(stringResource(R.string.deposit_balance_latest_label),
                    color = sw.inkSubtle,
                    style = SwType.SectionLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    RupiahText(value = latest?.balance ?: 0L,
                        style = SwType.AmountXL.copy(fontSize = 30.sp,
                            fontWeight = FontWeight.Bold),
                        color = sw.ink)
                    if (snapshots.size >= 2 && growthPct != 0f) {
                        Spacer(Modifier.size(width = 8.dp, height = 1.dp))
                        Text(
                            (if (growthPct >= 0f) "+" else "−") +
                                "%.1f".format(kotlin.math.abs(growthPct)) + "%",
                            color = if (growthPct >= 0f) sw.success else sw.danger,
                            style = SwType.LabelStrong.copy(fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                if (latest != null) {
                    Text(
                        stringResource(R.string.deposit_balance_per_format,
                            latest.date.toAbsoluteId()),
                        color = sw.inkMuted,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (snapshots.size >= 2) {
                    Spacer(Modifier.height(12.dp))
                    InlineSnapshotChart(snapshots = snapshots, lineColor = sw.primary)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        // "DETAIL" section label.
        Text(stringResource(R.string.gold_section_detail), color = sw.inkSubtle,
            style = SwType.SectionLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
        Spacer(Modifier.height(4.dp))
        SwCard(padding = PaddingValues(0.dp)) {
            Column {
                DetailRow(stringResource(R.string.deposit_field_type), d.typeLabel.code())
                DetailRow(stringResource(R.string.deposit_field_institution),
                    d.institutionInfo ?: stringResource(R.string.gold_dash), last = true)
            }
        }
        Spacer(Modifier.height(16.dp))
        // "RIWAYAT SNAPSHOT" label with "+ Snapshot baru" link on the right.
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(stringResource(R.string.deposit_section_snapshot).uppercase(),
                color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { addSnap = true }
                    .padding(horizontal = 4.dp, vertical = 4.dp)) {
                Icon(Icons.Outlined.Add, null, tint = sw.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(width = 2.dp, height = 1.dp))
                Text(stringResource(R.string.deposit_snapshot_add), color = sw.primary,
                    style = SwType.LabelStrong.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (snapshots.isEmpty()) {
            SwCard { Text(stringResource(R.string.deposit_snapshot_empty), color = sw.inkMuted, style = SwType.Body) }
        } else SwCard(padding = PaddingValues(0.dp)) {
            Column {
                sortedSnapshots.forEachIndexed { idx, s ->
                    val prev = sortedSnapshots.getOrNull(idx + 1)
                    val diff = if (prev != null) s.balance - prev.balance else 0L
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        // 36×36 r10 primaryContainer calendar chip per proto.
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(sw.primaryContainer)) {
                            Icon(Icons.Outlined.CalendarMonth, null,
                                tint = sw.onPrimaryContainer, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.date.toAbsoluteId(), color = sw.ink,
                                style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                            if (prev != null) {
                                val sign = if (diff >= 0L) "+" else "−"
                                Text("$sign ${kotlin.math.abs(diff).toRupiahShort()} vs sebelumnya",
                                    color = if (diff >= 0L) sw.success else sw.danger,
                                    style = SwType.LabelSmall.copy(fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold))
                            } else if (s.note != null) {
                                Text(s.note, color = sw.inkSubtle,
                                    style = SwType.LabelSmall.copy(fontSize = 11.sp))
                            }
                        }
                        RupiahText(value = s.balance,
                            style = SwType.Amount.copy(fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"))
                    }
                    if (idx < sortedSnapshots.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        // Proto has no destructive button in body; keep delete as a tiny link.
        Text(stringResource(R.string.deposit_delete), color = sw.danger,
            style = SwType.LabelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.clickable { viewModel.delete(); onBack() }
                .padding(horizontal = 4.dp, vertical = 6.dp))
    }
    if (addSnap) {
        AddSnapshotSheet(
            onSave = { d2, a, n -> viewModel.addSnapshot(d2, a, n); addSnap = false },
            onDismiss = { addSnap = false },
        )
    }
}

/**
 * Bare chart canvas — no card chrome, no header. Used embedded inside the
 * Deposit hero per proto.
 */
@Composable
private fun InlineSnapshotChart(snapshots: List<DepositSnapshot>, lineColor: Color) {
    if (snapshots.size < 2) return
    val sw = SwTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            val maxV = snapshots.maxOf { it.balance }.toFloat()
            val minV = snapshots.minOf { it.balance }.toFloat()
            val range = (maxV - minV).coerceAtLeast(1f)
            val w = size.width; val h = size.height
            val xs = snapshots.indices.map { it * w / (snapshots.size - 1) }
            val ys = snapshots.map { h - ((it.balance - minV) / range) * h * 0.85f - h * 0.05f }
            val area = Path().apply {
                moveTo(xs[0], h)
                for (i in xs.indices) lineTo(xs[i], ys[i])
                lineTo(xs.last(), h); close()
            }
            drawPath(area, brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.22f), Color.Transparent),
            ))
            val line = Path().apply {
                moveTo(xs[0], ys[0])
                for (i in 1 until xs.size) lineTo(xs[i], ys[i])
            }
            drawPath(line, color = lineColor,
                style = Stroke(width = 2.2f * density, cap = StrokeCap.Round))
            for (i in xs.indices) {
                val isLast = i == xs.size - 1
                drawCircle(
                    color = lineColor,
                    radius = if (isLast) 4f * density else 2.5f * density,
                    center = Offset(xs[i], ys[i]),
                )
            }
        }
        // X-axis month labels — same approach as NetWorthTrendCard. Pick
        // first / middle / last (or all if ≤4 points) so labels don't crowd.
        val fmt = java.time.format.DateTimeFormatter.ofPattern("MMM yy", java.util.Locale.getDefault())
        val indices = when {
            snapshots.size <= 4 -> snapshots.indices.toList()
            else -> listOf(0, snapshots.size / 2, snapshots.size - 1)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            indices.forEach { i ->
                Text(snapshots[i].date.format(fmt),
                    color = sw.inkSubtle,
                    style = SwType.LabelSmall.copy(fontSize = 10.sp, fontFeatureSettings = "tnum"))
            }
        }
    }
}

@Composable
private fun SnapshotChart(snapshots: List<DepositSnapshot>, lineColor: Color) {
    val sw = SwTheme.colors
    if (snapshots.size < 2) return
    SwCard {
        Column {
            Text(stringResource(R.string.deposit_chart_label), color = sw.inkSubtle,
                style = SwType.SectionLabel.copy(fontSize = 11.sp))
            Spacer(Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                val maxV = snapshots.maxOf { it.balance }.toFloat()
                val minV = snapshots.minOf { it.balance }.toFloat()
                val range = (maxV - minV).coerceAtLeast(1f)
                val w = size.width; val h = size.height
                val xs = snapshots.indices.map { it * w / (snapshots.size - 1) }
                val ys = snapshots.map { h - ((it.balance - minV) / range) * h * 0.85f - h * 0.05f }
                // Area
                val area = Path().apply {
                    moveTo(xs[0], h)
                    for (i in xs.indices) lineTo(xs[i], ys[i])
                    lineTo(xs.last(), h); close()
                }
                drawPath(area, brush = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.22f), Color.Transparent),
                ))
                // Line
                val line = Path().apply {
                    moveTo(xs[0], ys[0])
                    for (i in 1 until xs.size) lineTo(xs[i], ys[i])
                }
                drawPath(line, color = lineColor,
                    style = Stroke(width = 2.2f * density, cap = StrokeCap.Round))
                // Dots
                for (i in xs.indices) {
                    val isLast = i == xs.size - 1
                    drawCircle(
                        color = lineColor,
                        radius = if (isLast) 4f * density else 2.5f * density,
                        center = Offset(xs[i], ys[i]),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSnapshotSheet(onSave: (LocalDate, Long, String?) -> Unit, onDismiss: () -> Unit) {
    val sw = SwTheme.colors
    var balance by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    SwPickerSheet(title = stringResource(R.string.deposit_snapshot_sheet_title), onDismiss = onDismiss) {
        SwField(value = balance,
            onValueChange = { balance = it.filter { c -> c.isDigit() } },
            label = stringResource(R.string.deposit_snapshot_balance_label), prefix = "Rp", rupiah = true, keyboardType = KeyboardType.Number)
        SwField(value = note, onValueChange = { note = it }, label = stringResource(R.string.deposit_snapshot_note_label))
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
            Text(date.toAbsoluteId(), color = sw.ink, style = SwType.Body.copy(fontSize = 14.sp))
        }
        Spacer(Modifier.height(8.dp))
        SwButton(stringResource(R.string.action_save), onClick = {
            onSave(date, balance.toLongOrNull() ?: 0L, note.ifBlank { null })
        }, enabled = balance.isNotBlank())
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
fun DepositEditScreen(
    id: String?, onClose: () -> Unit,
    viewModel: DepositEditViewModel = hiltViewModel(key = "dep-edit-${id ?: "new"}"),
) {
    val sw = SwTheme.colors
    LaunchedEffect(id) { viewModel.load(id) }
    val s by viewModel.state.collectAsState()
    SimpleSettingsScreen(
        title = if (s.id == null) stringResource(R.string.deposit_edit_new_title)
        else stringResource(R.string.deposit_edit_edit_title),
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
            label = stringResource(R.string.deposit_edit_name_label),
            placeholder = stringResource(R.string.deposit_edit_name_placeholder))
        SwField(s.institution, { v -> viewModel.set { it.copy(institution = v) } },
            label = stringResource(R.string.deposit_edit_institution_label),
            placeholder = stringResource(R.string.deposit_edit_institution_placeholder))
        Text(stringResource(R.string.deposit_edit_type_label), color = sw.inkMuted, style = SwType.Caption.copy(fontSize = 12.sp))
        Spacer(Modifier.height(6.dp))
        Row {
            DepositType.entries.forEach { t ->
                val active = s.type == t
                val label = stringResource(when (t) {
                    DepositType.DPLK -> R.string.deposit_type_dplk
                    DepositType.BPJSTK -> R.string.deposit_type_bpjstk
                    DepositType.Deposito -> R.string.deposit_type_deposito
                    DepositType.Other -> R.string.deposit_type_other
                })
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(end = 8.dp).height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) sw.primary else sw.surface)
                        .clickable { viewModel.set { it.copy(type = t) } }
                        .padding(horizontal = 14.dp)) {
                    Text(label, color = if (active) sw.onPrimary else sw.ink,
                        style = SwType.LabelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

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
