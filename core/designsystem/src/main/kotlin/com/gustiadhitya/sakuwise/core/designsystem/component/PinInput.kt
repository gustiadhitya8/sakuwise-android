package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.H2Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

private val PinCellHeight = 56.dp
private val PinDotSize = 10.dp

@Composable
fun PinInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    cellCount: Int = 6,
    obscure: Boolean = true,
    enabled: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Invisible text field to capture keyboard input
        BasicTextField(
            value = value,
            onValueChange = { new ->
                if (new.length <= cellCount && new.all { it.isDigit() }) {
                    onValueChange(new)
                }
            },
            modifier = Modifier
                .size(1.dp)
                .alpha(0.005f)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "PIN input, ${value.length} dari $cellCount digit diisi"
                },
            horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
        ) {
            repeat(cellCount) { index ->
                val char = value.getOrNull(index)
                val isCellFocused = isFocused && index == value.length

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(PinCellHeight)
                        .background(MaterialTheme.colorScheme.surface, SakuwiseShapes.md)
                        .border(
                            width = if (isCellFocused) SakuwiseSpacing.borderFocus
                                    else SakuwiseSpacing.borderThin,
                            color = if (isCellFocused) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                            shape = SakuwiseShapes.md,
                        )
                        .clickable(enabled = enabled) { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (char != null) {
                        if (obscure) {
                            val dotColor = MaterialTheme.colorScheme.onSurface
                            Canvas(modifier = Modifier.size(PinDotSize)) {
                                drawCircle(color = dotColor)
                            }
                        } else {
                            Text(
                                text = char.toString(),
                                style = H2Style.copy(color = MaterialTheme.colorScheme.onSurface),
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (enabled) focusRequester.requestFocus()
    }
}

@Preview(showBackground = true)
@Composable
private fun PinInputPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SakuwiseSpacing.l)) {
                PinInput(value = "123", onValueChange = {})
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinInputPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SakuwiseSpacing.l)) {
                PinInput(value = "12", onValueChange = {})
            }
        }
    }
}
