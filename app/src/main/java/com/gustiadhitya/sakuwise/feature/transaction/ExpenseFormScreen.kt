package com.gustiadhitya.sakuwise.feature.transaction

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.ImageCompression
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.common.toRelativeOrAbsolute
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.feature.transaction.ui.AccountPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DatePickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.DebtPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.FieldButton
import com.gustiadhitya.sakuwise.feature.transaction.ui.PlanItemPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ui.TxnFormShell
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel

private enum class ExpensePicker { Account, PlanItem, Date, Debt }

@Composable
fun ExpenseFormScreen(
    onClose: () -> Unit,
    viewModel: TxnFormViewModel = hiltViewModel(),
    txnId: String? = null,
) {
    val sw = SwTheme.colors
    val state by viewModel.state.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val planItems by viewModel.planItemOptions.collectAsState()
    val openDebts by viewModel.openOwedDebts.collectAsState()
    var picker by remember { mutableStateOf<ExpensePicker?>(null) }

    LaunchedEffect(state.saved) { if (state.saved) onClose() }
    // Edit mode — load existing once. Keyed by txnId so re-entering with a
    // different id refills; keyed once per id since loadExisting is itself
    // idempotent. NB: planItemAllocation isn't reconstructed here — the user
    // sees the field but the hero tint defers to plain surface until they
    // re-pick. Worth a follow-up if it becomes a UX gripe.
    LaunchedEffect(txnId) { if (txnId != null) viewModel.loadExisting(txnId) }

    val account = accounts.firstOrNull { it.id == state.accountId }

    // Hero tint follows the picked plan item's allocation bucket (PRD §7.4 +
    // screens-addtxn.jsx:62). Needs=primary, Wants=accent, Invest=info. When
    // no plan item is picked yet, fall back to inkMuted-on-surface so the
    // hero isn't "loud" before the user has selected anything (proto: same).
    val alloc = state.planItemAllocation
    val heroBg = when (alloc) {
        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Needs -> sw.primary
        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Wants -> sw.accent
        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Invest -> sw.info
        null -> sw.primary
    }
    val heroFg = when (alloc) {
        // Mint (accent) needs the fixed-dark token for contrast per proto.
        com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Wants -> sw.fixedDarkOnMint
        else -> sw.onPrimary
    }
    val allocName: String? = alloc?.let { a ->
        when (a) {
            com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Needs -> stringResource(R.string.alloc_needs)
            com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Wants -> stringResource(R.string.alloc_wants)
            com.gustiadhitya.sakuwise.core.domain.model.AllocationId.Invest -> stringResource(R.string.alloc_invest)
        }
    }
    val allocLabel: String? = allocName?.let {
        stringResource(R.string.txn_expense_alloc_subtitle_format, it)
    }

    TxnFormShell(
        title = stringResource(R.string.txn_expense_title),
        heroBg = heroBg,
        heroFg = heroFg,
        heroLabel = stringResource(R.string.txn_expense_amount_label),
        amount = state.amount,
        onAmountChange = viewModel::setAmount,
        heroSubtitle = allocLabel,
        onCancel = onClose,
        saveLabel = stringResource(R.string.action_save),
        saveEnabled = state.amount > 0 && state.accountId != null && state.planItemId != null && !state.saving,
        onSave = viewModel::submitExpense,
        onDelete = if (state.editingId != null) viewModel::delete else null,
    ) {
        val planSubtitle = if (state.planItemName != null && allocName != null) {
            // "{categoryName} · {alloc}" — category not exposed by the VM
            // today, so fall back to "{alloc}" alone. Better-than-nothing.
            allocName
        } else null
        FieldButton(
            label = stringResource(R.string.txn_field_plan_item),
            value = state.planItemName.orEmpty(),
            placeholder = stringResource(R.string.txn_field_plan_item_placeholder),
            required = true,
            subtitle = planSubtitle,
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip(
                    bg = if (alloc == null) sw.surface else heroBg.copy(alpha = 0.18f),
                    fg = if (alloc == null) sw.inkMuted else heroBg,
                ) {
                    Text(
                        state.planItemName?.firstOrNull()?.uppercase() ?: "?",
                        color = androidx.compose.material3.LocalContentColor.current,
                        style = SwType.LabelStrong.copy(fontSize = 13.sp,
                            fontWeight = FontWeight.Bold),
                    )
                }
            },
            onClick = { picker = ExpensePicker.PlanItem },
        )
        val acctSubtitle = account?.let { a ->
            // Saldo subtitle uses the seed/initial balance until the VM
            // exposes a live-computed balance for txn forms. Better-than-
            // nothing and matches the proto's "Saldo: Rp X" affordance.
            stringResource(R.string.txn_field_account_balance_format,
                a.initialBalance.toRupiah())
        }
        FieldButton(
            label = stringResource(R.string.txn_field_account),
            value = account?.name.orEmpty(),
            placeholder = stringResource(R.string.txn_field_account_placeholder),
            required = true,
            subtitle = acctSubtitle,
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip {
                    Icon(Icons.Outlined.AccountBalanceWallet, null,
                        modifier = Modifier.size(16.dp))
                }
            },
            onClick = { picker = ExpensePicker.Account },
        )
        FieldButton(
            label = stringResource(R.string.txn_field_date),
            value = state.date.toAbsoluteId(),
            subtitle = state.date.toRelativeOrAbsolute(),
            leadingContent = {
                com.gustiadhitya.sakuwise.feature.transaction.ui.FieldChip {
                    Icon(Icons.Outlined.CalendarToday, null,
                        modifier = Modifier.size(16.dp))
                }
            },
            onClick = { picker = ExpensePicker.Date },
        )
        SwField(
            value = state.note,
            onValueChange = viewModel::setNote,
            label = stringResource(R.string.txn_field_note),
            placeholder = stringResource(R.string.txn_field_note_expense_placeholder),
        )
        Spacer(Modifier.height(4.dp))
        ExpensePhotoAttach(
            blob = state.photoBlob,
            onAttach = viewModel::setPhotoBlob,
            onClear = { viewModel.setPhotoBlob(null) },
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
                Text(stringResource(R.string.txn_link_debt_label), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                Text(
                    state.debtLabel?.let {
                        stringResource(R.string.txn_link_debt_attached_format, it)
                    } ?: stringResource(R.string.txn_link_debt_hint),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
            Switch(
                checked = state.debtId != null,
                onCheckedChange = { on ->
                    if (on) picker = ExpensePicker.Debt
                    else viewModel.setDebt(null, null)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = sw.onPrimary,
                    checkedTrackColor = sw.primary,
                ),
            )
        }
        if (state.debtId != null) {
            FieldButton(
                label = "Hutang",
                value = state.debtLabel.orEmpty(),
                placeholder = stringResource(R.string.expense_pick_debt_placeholder),
                leadingIcon = Icons.Outlined.Link,
                onClick = { picker = ExpensePicker.Debt },
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    when (picker) {
        ExpensePicker.Account -> AccountPickerSheet(
            accounts = accounts,
            selectedId = state.accountId,
            onPick = { viewModel.setAccount(it.id) },
            onDismiss = { picker = null },
        )
        ExpensePicker.PlanItem -> PlanItemPickerSheet(
            grouped = planItems,
            selectedId = state.planItemId,
            onPick = { viewModel.setPlanItem(it.id, it.name,
                com.gustiadhitya.sakuwise.core.domain.model.AllocationId.fromName(it.allocationName)) },
            onDismiss = { picker = null },
        )
        ExpensePicker.Date -> DatePickerSheet(
            selected = state.date,
            onPick = viewModel::setDate,
            onDismiss = { picker = null },
        )
        ExpensePicker.Debt -> DebtPickerSheet(
            debts = openDebts,
            paidPerDebt = emptyMap(), // outstanding shown as principal until V1.1 aggregates
            selectedId = state.debtId,
            onPick = { d -> viewModel.setDebt(d.id, d.counterparty) },
            onDismiss = { picker = null },
        )
        null -> Unit
    }
}

/**
 * Attach-receipt-photo row on the Expense form (PRD §7.4 + §7.11).
 *
 * Mirrors OcrCaptureScreen's permission + launcher pattern. Camera path uses
 * `TakePicturePreview` (already-granted intent, no CameraX dep). Gallery path
 * uses GetContent with an image-mime filter. Both routes funnel through
 * [ImageCompression.toCompressedJpeg] so the persisted BLOB is ≤ ~200 KB.
 *
 * When a photo is attached we render a 64×64 thumbnail + a ✕ to remove. Tap
 * the camera / gallery buttons again to swap photos.
 */
@Composable
private fun ExpensePhotoAttach(
    blob: ByteArray?,
    onAttach: (ByteArray) -> Unit,
    onClear: () -> Unit,
) {
    val sw = SwTheme.colors
    val ctx = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            ImageCompression.toCompressedJpeg(bitmap)?.let(onAttach)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) cameraLauncher.launch(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val stream = ctx.contentResolver.openInputStream(uri)
            val bm = stream?.let { ImageCompression.decodeBoundedFromStream(it) }
            if (bm != null) ImageCompression.toCompressedJpeg(bm)?.let(onAttach)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) cameraLauncher.launch(null)
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sw.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.txn_expense_photo_label),
                    color = sw.ink,
                    style = SwType.LabelStrong.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    stringResource(R.string.txn_expense_photo_hint),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhotoIconButton(
                    icon = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.txn_expense_photo_camera_cd),
                    onClick = ::launchCamera,
                )
                PhotoIconButton(
                    icon = Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.txn_expense_photo_gallery_cd),
                    onClick = { galleryLauncher.launch("image/*") },
                )
            }
        }
        if (blob != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val thumb = remember(blob) {
                    runCatching {
                        android.graphics.BitmapFactory
                            .decodeByteArray(blob, 0, blob.size)
                            ?.asImageBitmap()
                    }.getOrNull()
                }
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                }
                Spacer(Modifier.size(width = 10.dp, height = 1.dp))
                Text(
                    stringResource(R.string.txn_expense_photo_attached),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 11.sp),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(sw.bg)
                        .clickable(onClick = onClear),
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.txn_expense_photo_remove_cd),
                        tint = sw.inkMuted, modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(sw.primaryContainer)
            .clickable(onClick = onClick),
    ) {
        Icon(
            icon, contentDescription = contentDescription,
            tint = sw.onPrimaryContainer, modifier = Modifier.size(18.dp),
        )
    }
}
