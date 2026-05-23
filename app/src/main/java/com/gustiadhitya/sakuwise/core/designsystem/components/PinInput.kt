package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme

/**
 * PinInput — 6-cell numeric PIN entry. The cells are pure presentation;
 * a hidden, full-bleed TextField captures real numeric input from the system
 * keyboard so the user can type with the IME (or biometric keypad).
 */
@Composable
fun PinInput(
    value: String,
    onChange: (String) -> Unit,
    onComplete: (() -> Unit)? = null,
    length: Int = 6,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    val focus = remember { FocusRequester() }
    val interaction = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(length) { i ->
                val filled = i < value.length
                val active = i == value.length
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(sw.surface)
                        .border(
                            1.5.dp,
                            // Use borderStrong for inactive cells so they read
                            // clearly against the dark-mode surface (A11Y-008).
                            if (active) sw.primary else sw.borderStrong,
                            RoundedCornerShape(12.dp),
                        ),
                ) {
                    if (filled) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(sw.ink),
                        )
                    }
                }
            }
        }

        // Invisible TextField covering the row — receives keystrokes from the IME.
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(length)
                onChange(digits)
                if (digits.length == length) onComplete?.invoke()
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            interactionSource = interaction,
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focus)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                ) { focus.requestFocus() }
                .alpha(0.01f),
        )
    }
}
