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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gustiadhitya.sakuwise.core.designsystem.brand.DaunMark
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

private val LanguageHeroDaunSize: Dp = 108.dp

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
        title = stringResource(R.string.onb_language_title),
        subtitle = stringResource(R.string.onb_language_subtitle),
        heroContent = {
            OnbHeroSquircle {
                DaunMark(size = LanguageHeroDaunSize)
            }
        },
        actionLabel = stringResource(R.string.onb_language_cta),
        onAction = onNext,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.onb_language_section_header),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SakuwiseTokens.current.inkSubtle,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            LanguageOptionCard(
                code = "id",
                label = stringResource(R.string.onb_lang_id),
                sublabel = stringResource(R.string.onb_lang_id_sublabel),
                isSelected = selectedCode == "id",
                onClick = { onSelectLanguage("id") },
            )
            LanguageOptionCard(
                code = "en",
                label = stringResource(R.string.onb_lang_en),
                sublabel = stringResource(R.string.onb_lang_en_sublabel),
                isSelected = selectedCode == "en",
                onClick = { onSelectLanguage("en") },
            )
        }
    }
}

@Composable
private fun LanguageOptionCard(
    code: String,
    label: String,
    sublabel: String,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                    ),
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SakuwiseTokens.current.inkSubtle,
                    ),
                )
            }
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
