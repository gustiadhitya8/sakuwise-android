package com.gustiadhitya.sakuwise.feature.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.icon.SakuwiseIcons
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

private val PrivacyShieldSize: Dp = 100.dp
private val PrivacyCheckBadgeSize: Dp = 40.dp

private data class PrivacyFeature(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

@Composable
fun Onb_Privacy(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Onb_PrivacyContent(
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
internal fun Onb_PrivacyContent(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val features = listOf(
        PrivacyFeature(
            icon = SakuwiseIcons.Shield,
            title = stringResource(R.string.onb_privacy_feat1_title),
            description = stringResource(R.string.onb_privacy_feat1_desc),
        ),
        PrivacyFeature(
            icon = SakuwiseIcons.EyeOff,
            title = stringResource(R.string.onb_privacy_feat2_title),
            description = stringResource(R.string.onb_privacy_feat2_desc),
        ),
        PrivacyFeature(
            icon = SakuwiseIcons.Check,
            title = stringResource(R.string.onb_privacy_feat3_title),
            description = stringResource(R.string.onb_privacy_feat3_desc),
        ),
    )

    OnboardingShell(
        stepIndex = 2,
        totalSteps = 4,
        title = stringResource(R.string.onb_privacy_title),
        subtitle = stringResource(R.string.onb_privacy_subtitle),
        heroContent = { PrivacyHero() },
        actionLabel = stringResource(R.string.onb_privacy_cta),
        onAction = onDone,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            modifier = Modifier.fillMaxWidth(),
        ) {
            features.forEach { feature ->
                PrivacyFeatureBlock(feature = feature)
            }
        }
    }
}

@Composable
private fun PrivacyHero() {
    OnbHeroSquircle {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SakuwiseIcons.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(PrivacyShieldSize),
            )
            Box(
                modifier = Modifier
                    .size(PrivacyCheckBadgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = -SakuwiseSpacing.m, y = -SakuwiseSpacing.m)
                    .clip(SakuwiseShapes.full)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SakuwiseIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(SakuwiseSpacing.xl),
                )
            }
        }
    }
}

@Composable
private fun PrivacyFeatureBlock(feature: PrivacyFeature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SakuwiseShapes.lg)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(SakuwiseSpacing.l),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(SakuwiseSpacing.xl),
        )
        Spacer(Modifier.width(SakuwiseSpacing.m))
        Column {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyPreviewLight() {
    SakuwiseTheme {
        Onb_PrivacyContent(onDone = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrivacyPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Onb_PrivacyContent(onDone = {})
    }
}
