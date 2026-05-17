package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.ButtonTextLgStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.ButtonTextMdStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.ButtonTextSmStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseAnimation
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

enum class SwButtonVariant { Primary, Secondary, Outline, Ghost, Danger }
enum class SwButtonSize { Sm, Md, Lg }

private val SwButtonSize.height: Dp
    get() = when (this) {
        SwButtonSize.Sm -> 40.dp
        SwButtonSize.Md -> 48.dp
        SwButtonSize.Lg -> 56.dp
    }

private val SwButtonSize.horizontalPadding: Dp
    get() = when (this) {
        SwButtonSize.Sm -> SakuwiseSpacing.l
        SwButtonSize.Md -> SakuwiseSpacing.xl
        SwButtonSize.Lg -> SakuwiseSpacing.xxl
    }

private val SwButtonSize.iconSize: Dp
    get() = when (this) {
        SwButtonSize.Sm -> 16.dp
        SwButtonSize.Md -> 18.dp
        SwButtonSize.Lg -> 20.dp
    }

@Composable
fun SwButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SwButtonVariant = SwButtonVariant.Primary,
    size: SwButtonSize = SwButtonSize.Md,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SakuwiseAnimation.pressScale else 1f,
        animationSpec = tween(SakuwiseAnimation.pressDuration),
        label = "btnScale",
    )
    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) SakuwiseAnimation.pressAlpha else 1f,
        animationSpec = tween(SakuwiseAnimation.pressDuration),
        label = "btnAlpha",
    )

    val tokens = SakuwiseTokens.current
    val containerColor: Color
    val contentColor: Color
    val borderStroke: BorderStroke?

    when (variant) {
        SwButtonVariant.Primary -> {
            containerColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            borderStroke = null
        }
        SwButtonVariant.Secondary -> {
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            borderStroke = null
        }
        SwButtonVariant.Outline -> {
            containerColor = Color.Transparent
            contentColor = MaterialTheme.colorScheme.onBackground
            borderStroke = BorderStroke(SakuwiseSpacing.borderThin, tokens.borderStrong)
        }
        SwButtonVariant.Ghost -> {
            containerColor = Color.Transparent
            contentColor = MaterialTheme.colorScheme.primary
            borderStroke = null
        }
        SwButtonVariant.Danger -> {
            containerColor = tokens.danger
            contentColor = Color.White
            borderStroke = null
        }
    }

    val textStyle = when (size) {
        SwButtonSize.Sm -> ButtonTextSmStyle
        SwButtonSize.Md -> ButtonTextMdStyle
        SwButtonSize.Lg -> ButtonTextLgStyle
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(size.height)
            .scale(scale)
            .alpha(pressAlpha),
        enabled = enabled && !loading,
        shape = SakuwiseShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f),
        ),
        border = borderStroke,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.iconSize),
                    )
                    Spacer(Modifier.width(SakuwiseSpacing.s))
                }
                Text(text = text, style = textStyle)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwButtonPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwButtonVariant.entries.forEach { variant ->
                    SwButton(text = variant.name, onClick = {}, variant = variant)
                }
                SwButton(text = "Large", onClick = {}, size = SwButtonSize.Lg)
                SwButton(text = "Small", onClick = {}, size = SwButtonSize.Sm)
                SwButton(text = "Disabled", onClick = {}, enabled = false)
                SwButton(text = "Loading", onClick = {}, loading = true)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwButtonPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwButtonVariant.entries.forEach { variant ->
                    SwButton(text = variant.name, onClick = {}, variant = variant)
                }
            }
        }
    }
}
