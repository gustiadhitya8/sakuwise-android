package com.gustiadhitya.sakuwise.feature.settings.sub

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.gustiadhitya.sakuwise.core.crypto.PinStore
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.components.SwField
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import dagger.hilt.android.lifecycle.HiltViewModel
import com.gustiadhitya.sakuwise.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ResetAppViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val db: SakuwiseDatabase,
    private val prefsRepo: UserPreferencesRepository,
    private val pinStore: PinStore,
    private val transactionRepo: TransactionRepository,
) : ViewModel() {
    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done

    private val _txnsDeleted = MutableStateFlow(false)
    val txnsDeleted: StateFlow<Boolean> = _txnsDeleted

    fun reset() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { WorkManager.getInstance(appContext).cancelAllWork() }
                runCatching { db.clearAllTables() }
                runCatching { pinStore.clear() }
            }
            prefsRepo.resetAll()
            _done.value = true
        }
    }

    fun deleteAllTransactions() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { transactionRepo.deleteAll() }
            _txnsDeleted.value = true
            _txnsDeleted.value = false
        }
    }
}

private const val ACK_PHRASE = "HAPUS SEMUA"

@Composable
fun ResetAppScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ResetAppViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val done by viewModel.done.collectAsState()
    var phrase by remember { mutableStateOf("") }
    var showDeleteTxnsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Only navigate away when done transitions to true — guard against spurious
    // recompositions on fresh/empty data that could trigger onDone() prematurely.
    LaunchedEffect(done) { if (done) onDone() }

    if (showDeleteTxnsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTxnsDialog = false },
            icon = { Icon(Icons.Outlined.DeleteForever, null, tint = sw.danger) },
            title = { Text("Hapus Data Transaksi?", color = sw.ink, style = SwType.H3) },
            text = {
                Text(
                    "Semua transaksi akan dihapus permanen. Plan, akun, dan data aset tidak terpengaruh.",
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
            },
            confirmButton = {
                Column {
                    SwButton(
                        text = "Batal",
                        onClick = { showDeleteTxnsDialog = false },
                        variant = SwButtonVariant.Primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    SwButton(
                        text = "Hapus Transaksi",
                        onClick = { showDeleteTxnsDialog = false; viewModel.deleteAllTransactions() },
                        variant = SwButtonVariant.GhostDanger,
                    )
                }
            },
            dismissButton = null,
            containerColor = sw.surface,
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Outlined.DeleteForever, null, tint = sw.danger) },
            title = {
                Text(
                    stringResource(R.string.reset_confirm_dialog_title),
                    color = sw.ink,
                    style = SwType.H3,
                )
            },
            text = {
                Text(
                    stringResource(R.string.reset_confirm_dialog_body),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
            },
            // Both buttons are the same size. "Batal" is highlighted (Primary) so users
            // read the warning before accidentally tapping the destructive action.
            confirmButton = {
                Column {
                    SwButton(
                        text = stringResource(R.string.reset_confirm_dialog_no),
                        onClick = { showResetConfirmDialog = false },
                        variant = SwButtonVariant.Primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    SwButton(
                        text = stringResource(R.string.reset_confirm_dialog_yes),
                        onClick = { showResetConfirmDialog = false; viewModel.reset() },
                        variant = SwButtonVariant.GhostDanger,
                    )
                }
            },
            dismissButton = null,
            containerColor = sw.surface,
        )
    }

    SimpleSettingsScreen(title = stringResource(R.string.reset_title), onBack = onBack) {
        // ── Hapus Data Transaksi ───────────────────────────────────────
        SwCard(padding = PaddingValues(16.dp)) {
            Column {
                Text(
                    "Hapus Data Transaksi",
                    color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Hapus seluruh riwayat transaksi tanpa menghapus Plan, akun, atau data aset. Berguna untuk impor ulang dari awal.",
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
                Spacer(Modifier.height(12.dp))
                SwButton(
                    text = "Hapus Semua Transaksi",
                    onClick = { showDeleteTxnsDialog = true },
                    variant = SwButtonVariant.Danger,
                    leading = { Icon(Icons.Outlined.DeleteForever, null, tint = sw.onPrimary, modifier = androidx.compose.ui.Modifier.padding(end = 4.dp)) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(sw.dangerSoft)
                .padding(20.dp),
        ) {
            Column {
                Icon(Icons.Outlined.WarningAmber, null, tint = sw.danger,
                    modifier = Modifier.padding(bottom = 6.dp))
                Text(stringResource(R.string.reset_hero_title),
                    color = sw.danger,
                    style = SwType.H2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.reset_hero_body),
                    color = sw.ink,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        SwCard(padding = PaddingValues(16.dp)) {
            Column {
                Text(stringResource(R.string.reset_confirm_title), color = sw.ink,
                    style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.reset_confirm_intro_format, ACK_PHRASE),
                    color = sw.inkMuted,
                    style = SwType.LabelSmall.copy(fontSize = 12.sp),
                )
                Spacer(Modifier.height(10.dp))
                SwField(
                    value = phrase,
                    onValueChange = { phrase = it },
                    label = stringResource(R.string.reset_confirm_label),
                    placeholder = ACK_PHRASE,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        SwButton(
            text = stringResource(R.string.reset_action),
            onClick = { showResetConfirmDialog = true },
            variant = SwButtonVariant.Danger,
            enabled = phrase.trim() == ACK_PHRASE,
        )
        Spacer(Modifier.height(8.dp))
        SwButton(text = stringResource(R.string.reset_cancel), onClick = onBack, variant = SwButtonVariant.Ghost)
        // LocalContext referenced so a future toast/snackbar wiring can hook in
        @Suppress("UnusedExpression") LocalContext.current
    }
}
