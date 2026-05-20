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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwCard
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwSpace
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType
import com.gustiadhitya.sakuwise.core.domain.model.AccountType
import com.gustiadhitya.sakuwise.feature.transaction.ui.displayName
import com.gustiadhitya.sakuwise.core.ui.RupiahText
import com.gustiadhitya.sakuwise.feature.asset.viewmodel.AccountsListViewModel

private fun AccountType.icon(): ImageVector = when (this) {
    AccountType.Cash -> Icons.Outlined.Payments
    AccountType.Bank -> Icons.Outlined.AccountBalance
    AccountType.EWallet, AccountType.Other -> Icons.Outlined.AccountBalanceWallet
}

@Composable
fun AccountsListScreen(
    onBack: () -> Unit,
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit = {},
    viewModel: AccountsListViewModel = hiltViewModel(),
) {
    val sw = SwTheme.colors
    val accounts by viewModel.accounts.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(sw.bg)) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
            Text(stringResource(R.string.accounts_title), color = sw.ink,
                style = SwType.H1.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(sw.primary)
                    .clickable(onClick = onAddAccount),
            ) { Icon(Icons.Outlined.Add, stringResource(R.string.accounts_add_cd), tint = sw.onPrimary, modifier = Modifier.size(20.dp)) }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SwSpace.pageH),
        ) {
            if (accounts.isEmpty()) {
                SwCard {
                    Text(stringResource(R.string.accounts_empty),
                        color = sw.inkMuted, style = SwType.Body)
                }
            } else {
                SwCard(padding = PaddingValues(0.dp)) {
                    Column {
                        accounts.forEachIndexed { i, ab ->
                            val divider = i < accounts.size - 1
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAccountClick(ab.account.id) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(sw.primaryContainer),
                                ) {
                                    Icon(ab.account.type.icon(), null,
                                        tint = sw.onPrimaryContainer, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.size(width = 12.dp, height = 1.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(ab.account.name, color = sw.ink,
                                        style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
                                    Text(ab.account.type.displayName(), color = sw.inkMuted,
                                        style = SwType.LabelSmall.copy(fontSize = 11.sp))
                                }
                                RupiahText(value = ab.balance, short = true,
                                    style = SwType.Amount.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                                Spacer(Modifier.size(width = 6.dp, height = 1.dp))
                                Icon(Icons.Outlined.ChevronRight, null,
                                    tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
                            }
                            if (divider) {
                                Box(Modifier.fillMaxWidth().height(1.dp)
                                    .padding(start = 72.dp)
                                    .background(sw.border))
                            }
                        }
                    }
                }
            }
        }
    }
}
