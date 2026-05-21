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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.common.toAbsoluteId
import com.gustiadhitya.sakuwise.core.domain.model.TxnType
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.components.SwSectionLabel
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountDetailViewModel
import com.gustiadhitya.sakuwise.feature.transaction.ui.SwPickerSheet

@Composable
fun AccountDetailScreen(
    accountId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    // Tapping a row in "Account Transactions" routes to the matching edit form.
    // Default is a no-op so previews/tests don't crash; host wires it.
    onEditTxn: (com.gustiadhitya.sakuwise.core.domain.model.Transaction) -> Unit = {},
    viewModel: AccountDetailViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    LaunchedEffect(accountId) { viewModel.bind(accountId) }
    val state by viewModel.state.collectAsState()
    val msg by viewModel.reconcileMessage.collectAsState()
    var showReconcile by remember { mutableStateOf(false) }
    var editSnapshot by remember { mutableStateOf<com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot?>(null) }

    LaunchedEffect(msg) { if (msg != null) { kotlinx.coroutines.delay(2200); viewModel.clearMessage() } }

    Column(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = SwSpace.pageH, top = 6.dp, bottom = 12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
            ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.cd_back), tint = sw.ink) }
            Text(
                state.account?.name ?: stringResource(R.string.account_detail_default_title),
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SwSpace.pageH),
        ) {
            // Hero — green card with SALDO · {type} label, big amount, and a
            // sub line that includes the last reconcile date + the diff (so
            // the user sees how stale/accurate the balance is) per proto.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(sw.primaryHero)
                    .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 18.dp),
            ) {
                Column {
                    Text(
                        stringResource(R.string.account_detail_balance_prefix,
                            state.account?.type?.name?.uppercase() ?: ""),
                        color = sw.onPrimaryHero.copy(alpha = 0.78f),
                        style = SwType.SectionLabel.copy(fontSize = 11.sp),
                    )
                    Spacer(Modifier.height(6.dp))
                    RupiahText(value = state.balance, color = sw.onPrimaryHero, style = SwType.AmountXL)
                    Spacer(Modifier.height(6.dp))
                    val lastSnap = state.snapshots.firstOrNull()
                    if (lastSnap != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.account_detail_last_recon_format,
                                    lastSnap.date.toAbsoluteId()),
                                color = sw.onPrimaryHero.copy(alpha = 0.78f),
                                style = SwType.LabelSmall.copy(fontSize = 12.sp),
                            )
                            // Show last selisih inline so the user sees the
                            // health of the latest reconcile at a glance.
                            if (lastSnap.diff != 0L) {
                                Text(stringResource(R.string.account_detail_diff_label),
                                    color = sw.onPrimaryHero.copy(alpha = 0.78f),
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                                RupiahText(
                                    value = lastSnap.diff,
                                    short = true,
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold),
                                    color = if (lastSnap.diff > 0) sw.accent
                                    else sw.onPrimaryHero,
                                )
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.account_detail_no_recon),
                            color = sw.onPrimaryHero.copy(alpha = 0.78f),
                            style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SwButton(
                    text = stringResource(R.string.account_detail_btn_reconcile),
                    onClick = { showReconcile = true },
                    modifier = Modifier.weight(1f),
                    leading = { Icon(Icons.Outlined.Check, null, tint = sw.onPrimary, modifier = Modifier.size(18.dp)) },
                )
                SwButton(
                    text = stringResource(R.string.account_detail_btn_edit),
                    onClick = { onEdit(accountId) },
                    variant = SwButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    leading = { Icon(Icons.Outlined.Edit, null, tint = sw.onPrimaryContainer, modifier = Modifier.size(18.dp)) },
                )
            }
            Spacer(Modifier.height(20.dp))

            // Snapshots
            SwSectionLabel(stringResource(R.string.account_detail_section_snapshots))
            if (state.snapshots.isEmpty()) {
                SwCard {
                    Text(
                        stringResource(R.string.account_detail_snapshots_empty),
                        color = sw.inkMuted, style = SwType.Body,
                    )
                }
            } else {
                SwCard(padding = PaddingValues(0.dp)) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Text(stringResource(R.string.account_detail_col_date), color = sw.inkSubtle,
                                style = SwType.SectionLabel.copy(fontSize = 10.sp),
                                modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.account_detail_col_observed), color = sw.inkSubtle,
                                style = SwType.SectionLabel.copy(fontSize = 10.sp),
                                modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.account_detail_col_diff), color = sw.inkSubtle,
                                style = SwType.SectionLabel.copy(fontSize = 10.sp))
                        }
                        state.snapshots.forEach { snap ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editSnapshot = snap }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text(snap.date.toAbsoluteId(), color = sw.ink,
                                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                                    modifier = Modifier.weight(1f))
                                RupiahText(value = snap.observedBalance, short = true,
                                    style = SwType.Amount.copy(fontSize = 12.sp),
                                    color = sw.ink)
                                Spacer(Modifier.weight(0.2f))
                                RupiahText(
                                    value = snap.diff, short = true,
                                    style = SwType.Amount.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    color = when {
                                        snap.diff > 0 -> sw.success
                                        snap.diff < 0 -> sw.danger
                                        else -> sw.inkSubtle
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SwSectionLabel(stringResource(R.string.account_detail_section_txns))
            if (state.transactions.isEmpty()) {
                SwCard { Text(stringResource(R.string.account_detail_txns_empty), color = sw.inkMuted, style = SwType.Body) }
            } else {
                SwCard(padding = PaddingValues(0.dp)) {
                    Column {
                        state.transactions.forEach { t ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { m ->
                                        if (t.type == TxnType.Expense || t.type == TxnType.Income || t.type == TxnType.Transfer)
                                            m.clickable { onEditTxn(t) } else m
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                val typeFallback = stringResource(when (t.type) {
                                    TxnType.Income -> R.string.txntype_income
                                    TxnType.Expense -> R.string.txntype_expense
                                    TxnType.Transfer -> R.string.txntype_transfer
                                    TxnType.DebtInflow -> R.string.txntype_debt_inflow
                                    TxnType.DebtOutflow -> R.string.txntype_debt_outflow
                                    TxnType.Reconciliation -> R.string.txntype_reconciliation
                                })
                                Column(Modifier.weight(1f)) {
                                    Text(t.note ?: typeFallback, color = sw.ink,
                                        style = SwType.LabelStrong.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold))
                                    Text(t.date.toAbsoluteId(), color = sw.inkSubtle,
                                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                                }
                                RupiahText(value = t.amount, short = true,
                                    style = SwType.Amount.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showReconcile) {
        ReconcileSheet(
            currentBalance = state.balance,
            onSubmit = { obs, note ->
                viewModel.submitReconciliation(obs, note)
                showReconcile = false
            },
            onDismiss = { showReconcile = false },
        )
    }
    editSnapshot?.let { snap ->
        SnapshotActionSheet(
            snapshot = snap,
            onSaveNote = { newNote ->
                viewModel.updateSnapshotNote(snap, newNote)
                editSnapshot = null
            },
            onDelete = {
                viewModel.deleteSnapshot(snap.id)
                editSnapshot = null
            },
            onDismiss = { editSnapshot = null },
        )
    }

    if (msg != null) {
        // Simple toast-style overlay
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(sw.ink)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(msg!!, color = sw.bg, style = SwType.LabelStrong.copy(fontSize = 13.sp))
            }
        }
    }
}

/**
 * 3-stage reconciliation flow per prototype:
 *  Stage 1 — Input: user enters observed balance + optional note
 *  Stage 2 — Confirm: shows delta and what adjustment will happen
 *  Stage 3 — Done: success summary with check, auto-dismisses on tap
 */
private enum class ReconcileStage { Input, Confirm, Done }

@Composable
private fun ReconcileSheet(
    currentBalance: Long,
    onSubmit: (Long, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var stage by remember { mutableStateOf(ReconcileStage.Input) }
    var observed by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val observedLong = observed.toLongOrNull() ?: 0L
    val delta = observedLong - currentBalance

    SwPickerSheet(
        title = when (stage) {
            ReconcileStage.Input -> stringResource(R.string.reconcile_title)
            ReconcileStage.Confirm -> stringResource(R.string.reconcile_confirm_title)
            ReconcileStage.Done -> stringResource(R.string.reconcile_done_title)
        },
        onDismiss = onDismiss,
    ) {
        // Proto shows each reconcile stage as a separate full screen with
        // a clear title — no inline progress dots. The title above already
        // signals the stage, so the dots add visual noise.
        when (stage) {
            ReconcileStage.Input -> {
                Text(
                    stringResource(R.string.reconcile_intro),
                    color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
                )
                Spacer(Modifier.height(12.dp))
                // Per proto reconcile-input ref (screen 48): subtle card with
                // surface bg + border. Section label UPPERCASE, big ink amount,
                // muted helper sub. Was previously a filled primaryContainer
                // which looked like a CTA instead of a read-only summary.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(sw.surface)
                        .border(1.dp, sw.border, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            stringResource(R.string.reconcile_app_balance).uppercase(),
                            color = sw.inkSubtle,
                            style = SwType.SectionLabel.copy(fontSize = 11.sp),
                        )
                        Spacer(Modifier.height(6.dp))
                        RupiahText(value = currentBalance,
                            style = SwType.AmountL.copy(fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"),
                            color = sw.ink)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.reconcile_app_balance_sub),
                            color = sw.inkMuted,
                            style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwField(
                    value = observed,
                    onValueChange = { observed = it.filter { ch -> ch.isDigit() } },
                    label = stringResource(R.string.reconcile_observed_label),
                    prefix = "Rp", rupiah = true,
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(4.dp))
                SwField(
                    value = note,
                    onValueChange = { note = it },
                    label = stringResource(R.string.reconcile_note_label),
                    placeholder = stringResource(R.string.reconcile_note_placeholder),
                )
                Spacer(Modifier.height(16.dp))
                // Per proto screen 48: when the observed matches the app balance
                // exactly (delta == 0), the CTA flips to "Sudah sesuai · Selesai"
                // and skips the Confirm stage — there's nothing to reconcile.
                val matched = observed.isNotEmpty() && delta == 0L
                if (matched) {
                    SwButton(
                        text = stringResource(R.string.reconcile_action_matched),
                        onClick = {
                            onSubmit(observedLong, note.ifBlank { null })
                            stage = ReconcileStage.Done
                        },
                    )
                } else {
                    SwButton(
                        text = stringResource(R.string.action_continue),
                        onClick = { stage = ReconcileStage.Confirm },
                        enabled = observed.isNotEmpty(),
                    )
                }
            }

            ReconcileStage.Confirm -> {
                Text(
                    stringResource(R.string.reconcile_confirm_intro),
                    color = sw.inkMuted, style = SwType.Body.copy(fontSize = 13.sp),
                )
                Spacer(Modifier.height(12.dp))
                // Comparison rows
                ReconcileRow(
                    label = stringResource(R.string.reconcile_app_balance),
                    value = currentBalance,
                    color = sw.ink,
                )
                Spacer(Modifier.height(8.dp))
                ReconcileRow(
                    label = stringResource(R.string.reconcile_observed_label),
                    value = observedLong,
                    color = sw.ink,
                )
                Spacer(Modifier.height(8.dp))
                // Delta box — colored by sign
                val deltaBg = when {
                    delta > 0L -> sw.successSoft
                    delta < 0L -> sw.dangerSoft
                    else -> sw.surface
                }
                val deltaFg = when {
                    delta > 0L -> sw.success
                    delta < 0L -> sw.danger
                    else -> sw.inkMuted
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(deltaBg)
                        .padding(16.dp),
                ) {
                    Column {
                        Text(
                            stringResource(
                                if (delta >= 0L) R.string.reconcile_delta_positive
                                else R.string.reconcile_delta_negative,
                            ),
                            color = deltaFg,
                            style = SwType.LabelSmall.copy(fontSize = 12.sp),
                        )
                        Spacer(Modifier.height(4.dp))
                        RupiahText(
                            value = kotlin.math.abs(delta),
                            style = SwType.AmountL.copy(fontSize = 20.sp),
                            color = deltaFg,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(
                                if (delta >= 0L) R.string.reconcile_will_add
                                else R.string.reconcile_will_subtract,
                            ),
                            color = deltaFg,
                            style = SwType.LabelSmall.copy(fontSize = 11.sp),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwButton(
                        text = stringResource(R.string.action_back),
                        onClick = { stage = ReconcileStage.Input },
                        variant = SwButtonVariant.Outline,
                        modifier = Modifier.weight(1f),
                    )
                    SwButton(
                        text = stringResource(R.string.reconcile_submit),
                        onClick = {
                            onSubmit(observedLong, note.ifBlank { null })
                            stage = ReconcileStage.Done
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            ReconcileStage.Done -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(sw.successSoft)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(sw.success),
                        ) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = sw.onPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.reconcile_done_title),
                            color = sw.success,
                            style = SwType.H3.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.reconcile_done_body),
                            color = sw.success,
                            style = SwType.Body.copy(fontSize = 13.sp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SwButton(
                    text = stringResource(R.string.action_done),
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun ReconcileRow(label: String, value: Long, color: androidx.compose.ui.graphics.Color) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, color = sw.inkMuted, style = SwType.LabelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.weight(1f))
        RupiahText(value = value, style = SwType.LabelStrong.copy(fontSize = 14.sp,
            fontWeight = FontWeight.Bold), color = color)
    }
}

/**
 * Tap-to-edit/delete sheet for an account snapshot row. The snapshot's observed
 * balance is immutable post-creation (it'd retroactively invalidate the
 * reconciliation diff); the user can edit the note or delete the whole row.
 */
@Composable
private fun SnapshotActionSheet(
    snapshot: com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot,
    onSaveNote: (String?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sw = SwTheme.colors
    var note by remember { mutableStateOf(snapshot.note ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    SwPickerSheet(
        title = stringResource(R.string.snapshot_sheet_title),
        onDismiss = onDismiss,
    ) {
        // Read-only summary
        Box(modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sw.surface)
            .border(1.dp, sw.border, RoundedCornerShape(14.dp))
            .padding(14.dp)) {
            Column {
                Text(snapshot.date.toAbsoluteId(), color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.account_detail_col_observed),
                            color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 10.sp))
                        RupiahText(value = snapshot.observedBalance,
                            style = SwType.LabelStrong.copy(fontSize = 15.sp,
                                fontWeight = FontWeight.Bold),
                            color = sw.ink)
                    }
                    Column {
                        Text(stringResource(R.string.account_detail_col_diff),
                            color = sw.inkSubtle,
                            style = SwType.LabelSmall.copy(fontSize = 10.sp))
                        RupiahText(value = snapshot.diff, style = SwType.LabelStrong.copy(
                            fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        ), color = when {
                            snapshot.diff > 0 -> sw.success
                            snapshot.diff < 0 -> sw.danger
                            else -> sw.inkSubtle
                        })
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SwField(
            value = note, onValueChange = { note = it },
            label = stringResource(R.string.reconcile_note_label),
            placeholder = stringResource(R.string.reconcile_note_placeholder),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SwButton(
                text = stringResource(R.string.action_delete),
                onClick = { confirmDelete = true },
                variant = SwButtonVariant.Outline,
                modifier = Modifier.weight(1f),
            )
            SwButton(
                text = stringResource(R.string.action_save),
                onClick = { onSaveNote(note.ifBlank { null }) },
                modifier = Modifier.weight(1f),
            )
        }
        if (confirmDelete) {
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(sw.dangerSoft)
                .padding(14.dp)) {
                Column {
                    Text(stringResource(R.string.snapshot_delete_confirm),
                        color = sw.danger, style = SwType.Body.copy(fontSize = 13.sp))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SwButton(text = stringResource(R.string.action_cancel),
                            onClick = { confirmDelete = false },
                            variant = SwButtonVariant.Outline,
                            modifier = Modifier.weight(1f))
                        SwButton(text = stringResource(R.string.action_delete),
                            onClick = onDelete,
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
