package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

private data class LanguageOption(
    val code: String,
    val label: String,
    val nativeLabel: String,
)

private val LanguageOptions = listOf(
    LanguageOption(code = "id", label = "Bahasa Indonesia", nativeLabel = ""),
    LanguageOption(code = "en", label = "English", nativeLabel = ""),
)

@Composable
fun Onb_Language(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Onb_LanguageContent(
        selectedCode = uiState.selectedLanguage,
        onSelectLanguage = viewModel::selectLanguage,
        onNext = { viewModel.confirmLanguage(onNext) },
        modifier = modifier,
    )
}

@Composable
internal fun Onb_LanguageContent(
    selectedCode: String,
    onSelectLanguage: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingShell(
        stepIndex = 0,
        totalSteps = 4,
        title = "Halo, kenalan dulu yuk",
        subtitle = "Pilih bahasa yang ingin kamu gunakan.",
        heroContent = {
            Icon(
                imageVector = SakuwiseIcons.Leaf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(SakuwiseSpacing.xxxl),
            )
        },
        actionLabel = "Lanjut",
        onAction = onNext,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            modifier = Modifier.fillMaxWidth(),
        ) {
            LanguageOptions.forEach { option ->
                LanguageOptionCard(
                    label = option.label,
                    isSelected = selectedCode == option.code,
                    onClick = { onSelectLanguage(option.code) },
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline

    val borderWidth = if (isSelected) SakuwiseSpacing.xs / 2 else SakuwiseSpacing.xs / 4

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SakuwiseSpacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Spacer(Modifier.width(SakuwiseSpacing.m))
                Icon(
                    imageVector = SakuwiseIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SakuwiseSpacing.xl),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguagePreviewLight() {
    SakuwiseTheme {
        Onb_LanguageContent(
            selectedCode = "id",
            onSelectLanguage = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguagePreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Onb_LanguageContent(
            selectedCode = "id",
            onSelectLanguage = {},
            onNext = {},
        )
    }
}
