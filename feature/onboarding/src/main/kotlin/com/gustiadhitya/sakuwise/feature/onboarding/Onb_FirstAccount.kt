package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.common.format.RupiahFormatter
import com.gustiadhitya.sakuwise.core.designsystem.brand.DaunMark
import com.gustiadhitya.sakuwise.core.designsystem.component.SwField
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.AmountStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens
import com.gustiadhitya.sakuwise.core.model.AccountType

private val AccountCardWidth: Dp = 220.dp
private val AccountCardHeight: Dp = 130.dp
private val AccountCardWatermarkSize: Dp = 140.dp

@Composable
fun Onb_FirstAccount(
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Onb_FirstAccountContent(
        accountName = uiState.accountName,
        accountType = uiState.accountType,
        initialBalance = uiState.initialBalance,
        onAccountNameChange = viewModel::setAccountName,
        onAccountTypeChange = viewModel::setAccountType,
        onBalanceChange = { raw ->
            val digits = raw.filter { it.isDigit() }
            viewModel.setInitialBalance(digits.toLongOrNull() ?: 0L)
        },
        onDone = { viewModel.finishOnboarding(onDone) },
        onSkip = { viewModel.finishOnboarding(onSkip) },
        modifier = modifier,
    )
}

@Composable
internal fun Onb_FirstAccountContent(
    accountName: String,
    accountType: AccountType,
    initialBalance: Long,
    onAccountNameChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onBalanceChange: (String) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingShell(
        stepIndex = 3,
        totalSteps = 4,
        title = stringResource(R.string.onb_first_account_title),
        subtitle = stringResource(R.string.onb_first_account_subtitle),
        heroContent = { AccountCardHero(accountName, accountType, initialBalance) },
        actionLabel = stringResource(R.string.onb_first_account_cta),
        onAction = onDone,
        secondaryActionLabel = stringResource(R.string.onb_first_account_skip),
        onSecondaryAction = onSkip,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SwField(
                value = accountName,
                onValueChange = onAccountNameChange,
                label = stringResource(R.string.onb_first_account_name_label),
                placeholder = stringResource(R.string.onb_first_account_name_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            Text(
                text = stringResource(R.string.onb_first_account_type_label),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Spacer(Modifier.height(SakuwiseSpacing.s))
            AccountTypePicker(
                selected = accountType,
                onSelect = onAccountTypeChange,
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            SwField(
                value = if (initialBalance == 0L) "" else initialBalance.toString(),
                onValueChange = onBalanceChange,
                label = stringResource(R.string.onb_first_account_balance_label),
                placeholder = "0",
                prefix = "Rp",
                hint = stringResource(R.string.onb_first_account_balance_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AccountCardHero(
    accountName: String,
    accountType: AccountType,
    initialBalance: Long,
) {
    val typeText = when (accountType) {
        AccountType.CASH -> stringResource(R.string.onb_account_type_cash)
        AccountType.BANK -> stringResource(R.string.onb_account_type_bank)
        AccountType.EWALLET -> stringResource(R.string.onb_account_type_ewallet)
    }
    val cashLabel = stringResource(R.string.onb_account_type_cash)

    Box(
        modifier = Modifier
            .size(width = AccountCardWidth, height = AccountCardHeight)
            .clip(SakuwiseShapes.card)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        DaunMark(
            size = AccountCardWatermarkSize,
            primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            onPrimaryColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = SakuwiseSpacing.xxl, y = SakuwiseSpacing.xxl),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SakuwiseSpacing.l),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.onb_account_card_label, typeText.uppercase()),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
            Column {
                Text(
                    text = accountName.ifBlank { cashLabel },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Text(
                    text = RupiahFormatter.format(initialBalance),
                    style = AmountStyle.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontFeatureSettings = "tnum",
                    ),
                )
            }
        }
    }
}

@Composable
private fun AccountTypePicker(
    selected: AccountType,
    onSelect: (AccountType) -> Unit,
) {
    val options = listOf(
        AccountType.CASH to stringResource(R.string.onb_account_type_cash),
        AccountType.BANK to stringResource(R.string.onb_account_type_bank),
        AccountType.EWALLET to stringResource(R.string.onb_account_type_ewallet),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEach { (type, label) ->
            AccountTypeCard(
                label = label,
                icon = when (type) {
                    AccountType.CASH -> SakuwiseIcons.Cash
                    AccountType.BANK -> SakuwiseIcons.Bank
                    AccountType.EWALLET -> SakuwiseIcons.Wallet
                },
                isSelected = selected == type,
                onClick = { onSelect(type) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AccountTypeCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline
    val borderWidth = if (isSelected) SakuwiseSpacing.xs / 2 else SakuwiseSpacing.xs / 4

    Card(
        onClick = onClick,
        modifier = modifier.semantics {
            role = Role.RadioButton
            selected = isSelected
        },
        shape = SakuwiseShapes.lg,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(SakuwiseSpacing.xxxxxxl + SakuwiseSpacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SakuwiseSpacing.xl),
            )
            Spacer(Modifier.height(SakuwiseSpacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FirstAccountPreviewLight() {
    SakuwiseTheme {
        Onb_FirstAccountContent(
            accountName = "Tunai",
            accountType = AccountType.CASH,
            initialBalance = 0L,
            onAccountNameChange = {},
            onAccountTypeChange = {},
            onBalanceChange = {},
            onDone = {},
            onSkip = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FirstAccountPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Onb_FirstAccountContent(
            accountName = "Tunai",
            accountType = AccountType.CASH,
            initialBalance = 1500000L,
            onAccountNameChange = {},
            onAccountTypeChange = {},
            onBalanceChange = {},
            onDone = {},
            onSkip = {},
        )
    }
}
