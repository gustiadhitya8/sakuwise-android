package com.gustiadhitya.sakuwise.app

import android.app.Activity
import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.core.designsystem.components.SwTab
import com.gustiadhitya.sakuwise.core.designsystem.components.SwTabBar
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.feature.asset.AssetTabHost
import com.gustiadhitya.sakuwise.feature.dashboard.DashboardScreen
import com.gustiadhitya.sakuwise.feature.lock.AppLockController
import com.gustiadhitya.sakuwise.feature.lock.LockScreen
import com.gustiadhitya.sakuwise.feature.onboarding.OnboardingFlow
import com.gustiadhitya.sakuwise.feature.plan.PlanScreen
import com.gustiadhitya.sakuwise.feature.settings.SettingsTabHost
import com.gustiadhitya.sakuwise.feature.transaction.AddTxnKind
import com.gustiadhitya.sakuwise.feature.transaction.AddTxnPickerSheet
import com.gustiadhitya.sakuwise.feature.transaction.ExpenseFormScreen
import com.gustiadhitya.sakuwise.feature.transaction.IncomeFormScreen
import com.gustiadhitya.sakuwise.feature.transaction.TransferFormScreen
import com.gustiadhitya.sakuwise.feature.transaction.ocr.OcrCaptureScreen
import com.gustiadhitya.sakuwise.feature.transaction.viewmodel.TxnFormViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppLockEntryPoint {
    fun appLockController(): AppLockController
}

@Composable
private fun rememberAppLockController(): AppLockController {
    val app = LocalContext.current.applicationContext as Application
    return remember(app) {
        EntryPointAccessors.fromApplication(app, AppLockEntryPoint::class.java)
            .appLockController()
    }
}

@Composable
fun SakuwiseApp(mainViewModel: MainViewModel = hiltViewModel()) {
    val sw = SwTheme.colors
    val prefs by mainViewModel.prefs.collectAsState()
    val lockController = rememberAppLockController()
    val showLock by lockController.shouldShowLock.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        if (!prefs.onboardingCompleted) {
            OnboardingFlow()
        } else {
            MainShell()
        }
        // Lock overlay on top of everything except onboarding
        if (showLock && prefs.onboardingCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sw.bg)
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                LockScreen(onUnlock = { lockController.unlock() })
            }
        }
    }
}

private enum class FullScreenOverlay { Expense, Income, Transfer, Ocr }

@Composable
private fun MainShell() {
    val sw = SwTheme.colors
    val ctx = LocalContext.current
    var active by remember { mutableStateOf(SwTab.Home) }
    var pickerOpen by remember { mutableStateOf(false) }
    var overlay by remember { mutableStateOf<FullScreenOverlay?>(null) }
    var exitConfirm by remember { mutableStateOf(false) }
    // Shared TxnFormViewModel so OCR overlay can prefill before Expense overlay opens.
    val txnFormVm: TxnFormViewModel = hiltViewModel()

    // System back behaviour:
    //  1. overlay / picker / exit-confirm visible → close that first.
    //  2. tab content (AssetTabHost / SettingsTabHost) handles its own sub-route back via
    //     a nested BackHandler — when on Hub it propagates here.
    //  3. non-Home tab → switch to Home.
    //  4. Home tab → show exit-confirm sheet (don't silently close the app).
    BackHandler(enabled = overlay != null) { overlay = null }
    BackHandler(enabled = overlay == null && pickerOpen) { pickerOpen = false }
    BackHandler(enabled = overlay == null && !pickerOpen && exitConfirm) { exitConfirm = false }
    BackHandler(enabled = overlay == null && !pickerOpen && !exitConfirm && active != SwTab.Home) {
        active = SwTab.Home
    }
    BackHandler(enabled = overlay == null && !pickerOpen && !exitConfirm && active == SwTab.Home) {
        exitConfirm = true
    }

    Box(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Apply status-bar inset ONCE here so every tab content (and nested
            // SimpleSettingsScreen sub-screens) gets the safe-area gap. Avoids
            // per-screen double-padding under enableEdgeToEdge().
            Box(
                modifier = Modifier
                    .weight(1f)
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                when (active) {
                    SwTab.Home -> DashboardScreen(
                        onNavigateToPlan = { active = SwTab.Plan },
                        onNavigateToAssets = { active = SwTab.Assets },
                        onNavigateToMe = { active = SwTab.Me },
                    )
                    SwTab.Plan -> PlanScreen()
                    SwTab.Assets -> AssetTabHost()
                    SwTab.Me -> SettingsTabHost()
                }
            }
            SwTabBar(
                active = active,
                onSelect = { active = it },
                onAdd = { pickerOpen = true },
            )
        }

        if (overlay != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(sw.bg)
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                when (overlay) {
                    FullScreenOverlay.Expense -> ExpenseFormScreen(
                        viewModel = txnFormVm,
                        onClose = { overlay = null },
                    )
                    FullScreenOverlay.Income -> IncomeFormScreen(onClose = { overlay = null })
                    FullScreenOverlay.Transfer -> TransferFormScreen(onClose = { overlay = null })
                    FullScreenOverlay.Ocr -> OcrCaptureScreen(
                        onClose = { overlay = null },
                        onComplete = { draft ->
                            txnFormVm.prefillFromOcrDraft(
                                amount = draft.totalAmount,
                                date = draft.date,
                                merchant = draft.merchant,
                            )
                            overlay = FullScreenOverlay.Expense
                        },
                    )
                    null -> Unit
                }
            }
        }

        if (pickerOpen) {
            AddTxnPickerSheet(
                onDismiss = { pickerOpen = false },
                onPick = { kind ->
                    pickerOpen = false
                    overlay = when (kind) {
                        AddTxnKind.Expense -> FullScreenOverlay.Expense
                        AddTxnKind.Income -> FullScreenOverlay.Income
                        AddTxnKind.Transfer -> FullScreenOverlay.Transfer
                        AddTxnKind.Ocr -> FullScreenOverlay.Ocr
                    }
                },
            )
        }

        if (exitConfirm) {
            ExitConfirmSheet(
                onConfirm = {
                    exitConfirm = false
                    (ctx as? Activity)?.finish()
                },
                onDismiss = { exitConfirm = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitConfirmSheet(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val sw = SwTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sw.surface,
        contentColor = sw.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sw.borderStrong),
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        ) {
            Text(
                stringResource(R.string.exit_confirm_title),
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.exit_confirm_body),
                color = sw.inkMuted,
                style = SwType.Body.copy(fontSize = 13.sp),
            )
            Spacer(Modifier.height(20.dp))
            SwButton(
                text = stringResource(R.string.exit_confirm_yes),
                onClick = onConfirm,
                variant = SwButtonVariant.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            SwButton(
                text = stringResource(R.string.exit_confirm_no),
                onClick = onDismiss,
                variant = SwButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
