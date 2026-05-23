package com.gustiadhitya.sakuwise.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonSize
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

@Composable
fun OnboardingShell(
    step: Int,
    total: Int,
    title: String,
    subtitle: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    hero: @Composable () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val sw = SwTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(sw.bg)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Progress dots — tightened from 52dp to 24dp top so 4-step content
        // fits on small phones (Galaxy S25 was scrolling on every step).
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 20.dp, end = 20.dp),
        ) {
            repeat(total) { i ->
                val isCurrent = i == step - 1
                val isPast = i < step
                val width by animateDpAsState(
                    targetValue = if (isCurrent) 22.dp else 6.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "progress-dot-$i",
                )
                Box(
                    Modifier
                        .size(width = width, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isPast) sw.primary else sw.border),
                )
            }
        }

        // Hero artwork — tighter vertical padding so it sits closer to copy.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
        ) { hero() }

        // Copy + scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
        ) {
            Text(
                title,
                color = sw.ink,
                style = SwType.H1.copy(fontSize = 24.sp, letterSpacing = (-0.025).em),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                color = sw.inkMuted,
                style = SwType.BodyL.copy(fontSize = 14.sp),
            )
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(8.dp))
        }

        // Sticky actions — bottom inset trimmed; safe-area handles the rest.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        ) {
            SwButton(
                text = primaryLabel,
                onClick = onPrimary,
                size = SwButtonSize.Lg,
                enabled = primaryEnabled,
            )
            if (secondaryLabel != null && onSecondary != null) {
                SwButton(
                    text = secondaryLabel,
                    onClick = onSecondary,
                    variant = SwButtonVariant.Ghost,
                    size = SwButtonSize.Md,
                )
            }
        }
    }
}
