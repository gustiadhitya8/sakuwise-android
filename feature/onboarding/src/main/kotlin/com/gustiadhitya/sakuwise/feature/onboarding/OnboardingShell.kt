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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.component.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

private val HeroCircleSize: Dp = 200.dp
private val HeroBadgeSize: Dp = 64.dp
private val ProgressDotWidth: Dp = 24.dp
private val ProgressDotHeight: Dp = 8.dp
private val ProgressDotInactiveWidth: Dp = 8.dp

@Composable
fun OnboardingShell(
    stepIndex: Int,
    totalSteps: Int,
    title: String,
    subtitle: String,
    heroContent: @Composable () -> Unit,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = SakuwiseSpacing.xxl)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(SakuwiseSpacing.xxxl))

        StepProgressDots(current = stepIndex, total = totalSteps)

        Spacer(Modifier.height(SakuwiseSpacing.xxl))

        HeroCircle(content = heroContent)

        Spacer(Modifier.height(SakuwiseSpacing.xxl))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(SakuwiseSpacing.s))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SakuwiseTokens.current.inkSubtle,
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(SakuwiseSpacing.xxl))

        content()

        Spacer(Modifier.height(SakuwiseSpacing.xxl))

        SwButton(
            text = actionLabel,
            onClick = onAction,
            enabled = actionEnabled,
            modifier = Modifier.fillMaxWidth(),
        )

        if (secondaryActionLabel != null && onSecondaryAction != null) {
            Spacer(Modifier.height(SakuwiseSpacing.m))
            SwButton(
                text = secondaryActionLabel,
                onClick = onSecondaryAction,
                variant = com.gustiadhitya.sakuwise.core.designsystem.component.SwButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(SakuwiseSpacing.xxxl))
    }
}

@Composable
private fun StepProgressDots(current: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics {},
    ) {
        repeat(total) { index ->
            val isActive = index == current
            val width = if (isActive) ProgressDotWidth else ProgressDotInactiveWidth
            Box(
                modifier = Modifier
                    .width(width)
                    .height(ProgressDotHeight)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
            )
        }
    }
}

@Composable
fun HeroCircle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(HeroCircleSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(HeroBadgeSize)
                .clip(SakuwiseShapes.md)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingShellPreviewLight() {
    SakuwiseTheme {
        OnboardingShell(
            stepIndex = 0,
            totalSteps = 4,
            title = "Halo, kenalan dulu yuk",
            subtitle = "Pilih bahasa yang ingin kamu gunakan.",
            heroContent = {},
            actionLabel = "Lanjut",
            onAction = {},
        ) {}
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingShellPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        OnboardingShell(
            stepIndex = 0,
            totalSteps = 4,
            title = "Halo, kenalan dulu yuk",
            subtitle = "Pilih bahasa yang ingin kamu gunakan.",
            heroContent = {},
            actionLabel = "Lanjut",
            onAction = {},
        ) {}
    }
}
