package com.gustiadhitya.sakuwise.feature.asset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountDetailViewModel
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountEditViewModel

private sealed interface AssetRoute {
    data object Hub : AssetRoute
    data object Accounts : AssetRoute
    data class AccountDetail(val accountId: String) : AssetRoute
    data class AccountEdit(val accountId: String?) : AssetRoute
    data object GoldList : AssetRoute
    data class GoldDetail(val id: String) : AssetRoute
    data class GoldEdit(val id: String?) : AssetRoute
    data object LandList : AssetRoute
    data class LandDetail(val id: String) : AssetRoute
    data class LandEdit(val id: String?) : AssetRoute
    data object DepositList : AssetRoute
    data class DepositDetail(val id: String) : AssetRoute
    data class DepositEdit(val id: String?) : AssetRoute
    data object DebtList : AssetRoute
    data class DebtDetail(val id: String) : AssetRoute
    data class DebtEdit(val id: String?) : AssetRoute
}

@Composable
fun AssetTabHost(
    // Bubbled up so the host (MainShell) can open the full-screen edit
    // overlay when a row inside AccountDetail is tapped. Defaulted to a
    // no-op so existing call sites and previews keep working.
    onEditTxn: (com.gustiadhitya.sakuwise.core.domain.model.Transaction) -> Unit = {},
) {
    var route by remember { mutableStateOf<AssetRoute>(AssetRoute.Hub) }

    // Intra-tab back: sub-routes back-step toward Hub, then MainShell takes over.
    androidx.activity.compose.BackHandler(enabled = route !is AssetRoute.Hub) {
        route = when (val r = route) {
            is AssetRoute.AccountDetail -> AssetRoute.Accounts
            is AssetRoute.AccountEdit -> if (r.accountId == null) AssetRoute.Accounts
                else AssetRoute.AccountDetail(r.accountId)
            is AssetRoute.GoldDetail -> AssetRoute.GoldList
            is AssetRoute.GoldEdit -> if (r.id == null) AssetRoute.GoldList else AssetRoute.GoldDetail(r.id)
            is AssetRoute.LandDetail -> AssetRoute.LandList
            is AssetRoute.LandEdit -> if (r.id == null) AssetRoute.LandList else AssetRoute.LandDetail(r.id)
            is AssetRoute.DepositDetail -> AssetRoute.DepositList
            is AssetRoute.DepositEdit -> if (r.id == null) AssetRoute.DepositList else AssetRoute.DepositDetail(r.id)
            is AssetRoute.DebtDetail -> AssetRoute.DebtList
            is AssetRoute.DebtEdit -> if (r.id == null) AssetRoute.DebtList else AssetRoute.DebtDetail(r.id)
            else -> AssetRoute.Hub
        }
    }

    when (val r = route) {
        AssetRoute.Hub -> AssetsHubScreen(
            onNavigateToAccounts = { route = AssetRoute.Accounts },
            onNavigateToGold = { route = AssetRoute.GoldList },
            onNavigateToLand = { route = AssetRoute.LandList },
            onNavigateToDeposit = { route = AssetRoute.DepositList },
            onNavigateToDebt = { route = AssetRoute.DebtList },
        )
        AssetRoute.Accounts -> AccountsListScreen(
            onBack = { route = AssetRoute.Hub },
            onAccountClick = { id -> route = AssetRoute.AccountDetail(id) },
            onAddAccount = { route = AssetRoute.AccountEdit(null) },
        )
        is AssetRoute.AccountDetail -> AccountDetailScreenHost(
            accountId = r.accountId,
            onBack = { route = AssetRoute.Accounts },
            onEdit = { id -> route = AssetRoute.AccountEdit(id) },
            onEditTxn = onEditTxn,
        )
        is AssetRoute.AccountEdit -> AccountEditScreenHost(
            accountId = r.accountId,
            onBack = {
                route = if (r.accountId == null) AssetRoute.Accounts
                else AssetRoute.AccountDetail(r.accountId)
            },
        )

        AssetRoute.GoldList -> com.gustiadhitya.sakuwise.feature.asset.gold.GoldListScreen(
            onBack = { route = AssetRoute.Hub },
            onItemClick = { id -> route = AssetRoute.GoldDetail(id) },
            onAdd = { route = AssetRoute.GoldEdit(null) },
        )
        is AssetRoute.GoldDetail -> com.gustiadhitya.sakuwise.feature.asset.gold.GoldDetailScreen(
            id = r.id,
            onBack = { route = AssetRoute.GoldList },
            onEdit = { route = AssetRoute.GoldEdit(r.id) },
        )
        is AssetRoute.GoldEdit -> com.gustiadhitya.sakuwise.feature.asset.gold.GoldEditScreen(
            id = r.id,
            onClose = {
                route = if (r.id == null) AssetRoute.GoldList
                else AssetRoute.GoldDetail(r.id)
            },
        )

        AssetRoute.LandList -> com.gustiadhitya.sakuwise.feature.asset.land.LandListScreen(
            onBack = { route = AssetRoute.Hub },
            onItemClick = { id -> route = AssetRoute.LandDetail(id) },
            onAdd = { route = AssetRoute.LandEdit(null) },
        )
        is AssetRoute.LandDetail -> com.gustiadhitya.sakuwise.feature.asset.land.LandDetailScreen(
            id = r.id,
            onBack = { route = AssetRoute.LandList },
            onEdit = { route = AssetRoute.LandEdit(r.id) },
        )
        is AssetRoute.LandEdit -> com.gustiadhitya.sakuwise.feature.asset.land.LandEditScreen(
            id = r.id,
            onClose = {
                route = if (r.id == null) AssetRoute.LandList
                else AssetRoute.LandDetail(r.id)
            },
        )

        AssetRoute.DepositList -> com.gustiadhitya.sakuwise.feature.asset.deposit.DepositListScreen(
            onBack = { route = AssetRoute.Hub },
            onItemClick = { id -> route = AssetRoute.DepositDetail(id) },
            onAdd = { route = AssetRoute.DepositEdit(null) },
        )
        is AssetRoute.DepositDetail -> com.gustiadhitya.sakuwise.feature.asset.deposit.DepositDetailScreen(
            id = r.id,
            onBack = { route = AssetRoute.DepositList },
            onEdit = { route = AssetRoute.DepositEdit(r.id) },
        )
        is AssetRoute.DepositEdit -> com.gustiadhitya.sakuwise.feature.asset.deposit.DepositEditScreen(
            id = r.id,
            onClose = {
                route = if (r.id == null) AssetRoute.DepositList
                else AssetRoute.DepositDetail(r.id)
            },
        )

        AssetRoute.DebtList -> com.gustiadhitya.sakuwise.feature.asset.debt.DebtListScreen(
            onBack = { route = AssetRoute.Hub },
            onItemClick = { id -> route = AssetRoute.DebtDetail(id) },
            onAdd = { route = AssetRoute.DebtEdit(null) },
        )
        is AssetRoute.DebtDetail -> com.gustiadhitya.sakuwise.feature.asset.debt.DebtDetailScreen(
            id = r.id,
            onBack = { route = AssetRoute.DebtList },
            onEdit = { route = AssetRoute.DebtEdit(r.id) },
        )
        is AssetRoute.DebtEdit -> com.gustiadhitya.sakuwise.feature.asset.debt.DebtEditScreen(
            id = r.id,
            onClose = {
                route = if (r.id == null) AssetRoute.DebtList
                else AssetRoute.DebtDetail(r.id)
            },
        )
    }
}

@Composable
private fun AccountDetailScreenHost(
    accountId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onEditTxn: (com.gustiadhitya.sakuwise.core.domain.model.Transaction) -> Unit = {},
) {
    val vm: AccountDetailViewModel = hiltViewModel(key = "account-$accountId")
    AccountDetailScreen(
        accountId = accountId,
        onBack = onBack,
        onEdit = onEdit,
        onEditTxn = onEditTxn,
        viewModel = vm,
    )
}

@Composable
private fun AccountEditScreenHost(accountId: String?, onBack: () -> Unit) {
    val vm: AccountEditViewModel = hiltViewModel(key = "account-edit-${accountId ?: "new"}")
    AccountEditScreen(accountId = accountId, onClose = onBack, viewModel = vm)
}
