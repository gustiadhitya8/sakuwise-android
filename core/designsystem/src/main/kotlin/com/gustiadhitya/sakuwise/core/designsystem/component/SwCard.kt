package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseAnimation
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

@Composable
fun SwCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = SakuwiseSpacing.cardPadding,
    noBorder: Boolean = false,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) SakuwiseAnimation.pressScale else 1f,
        animationSpec = tween(SakuwiseAnimation.pressDuration),
        label = "cardScale",
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) SakuwiseAnimation.pressAlpha else 1f,
        animationSpec = tween(SakuwiseAnimation.pressDuration),
        label = "cardAlpha",
    )
    val tokens = SakuwiseTokens.current

    val baseModifier = modifier
        .scale(scale)
        .alpha(pressAlpha)
        .clip(SakuwiseShapes.card)
        .then(
            if (!noBorder) Modifier.border(SakuwiseSpacing.borderThin, tokens.borderStrong.copy(alpha = 0.6f), SakuwiseShapes.card)
            else Modifier
        )

    Surface(
        modifier = if (onClick != null) {
            baseModifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
        } else {
            baseModifier
        },
        color = MaterialTheme.colorScheme.surface,
        shape = SakuwiseShapes.card,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwCardPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                SwCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Static card content", style = MaterialTheme.typography.bodyMedium)
                }
                SwCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                    Text("Clickable card with press feedback", style = MaterialTheme.typography.bodyMedium)
                }
                SwCard(modifier = Modifier.fillMaxWidth(), noBorder = true) {
                    Text("No border card", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwCardPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                SwCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Card dark mode", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
