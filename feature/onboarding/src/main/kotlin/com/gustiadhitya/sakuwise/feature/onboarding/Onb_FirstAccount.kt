package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.designsystem.component.SwField
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens
import com.gustiadhitya.sakuwise.core.model.AccountType

private data class AccountTypeOption(
    val type: AccountType,
    val label: String,
)

private val AccountTypeOptions = listOf(
    AccountTypeOption(AccountType.CASH, "Tunai"),
    AccountTypeOption(AccountType.BANK, "Bank"),
    AccountTypeOption(AccountType.EWALLET, "Dompet"),
)

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
        title = "Akun pertamamu",
        subtitle = "Kita siapkan akun Tunai dulu. Bisa di-rename, ubah saldonya, atau langsung lanjut.",
        heroContent = {
            Icon(
                imageVector = SakuwiseIcons.Cash,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(SakuwiseSpacing.xxxl),
            )
        },
        actionLabel = "Selesai · Masuk Beranda",
        onAction = onDone,
        secondaryActionLabel = "Tambah akun lain nanti",
        onSecondaryAction = onSkip,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SwField(
                value = accountName,
                onValueChange = onAccountNameChange,
                label = "Nama akun",
                placeholder = "Tunai",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(SakuwiseSpacing.l))

            Text(
                text = "Tipe",
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
                label = "Saldo awal",
                placeholder = "0",
                prefix = "Rp",
                hint = "Boleh 0 — kamu bisa update nanti.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AccountTypePicker(
    selected: AccountType,
    onSelect: (AccountType) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
        modifier = Modifier.fillMaxWidth(),
    ) {
        AccountTypeOptions.forEach { option ->
            AccountTypeCard(
                label = option.label,
                icon = when (option.type) {
                    AccountType.CASH -> SakuwiseIcons.Cash
                    AccountType.BANK -> SakuwiseIcons.Bank
                    AccountType.EWALLET -> SakuwiseIcons.Wallet
                },
                isSelected = selected == option.type,
                onClick = { onSelect(option.type) },
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
