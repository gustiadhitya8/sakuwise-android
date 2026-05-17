package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyLStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.CaptionStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTokens

private val FieldHeight = 52.dp
private val FieldHorizontalPadding = 14.dp

@Composable
fun SwField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    prefix: String? = null,
    suffix: String? = null,
    hint: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val tokens = SakuwiseTokens.current
    val borderColor = when {
        isError -> tokens.danger
        else -> MaterialTheme.colorScheme.outline
    }
    val hintColor = if (isError) tokens.danger else MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(bottom = SakuwiseSpacing.xs),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = BodyLStyle.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FieldHeight)
                        .background(
                            color = if (enabled) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = SakuwiseShapes.md,
                        )
                        .border(SakuwiseSpacing.borderThin, borderColor, SakuwiseShapes.md)
                        .padding(horizontal = FieldHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (prefix != null) {
                        Text(
                            text = prefix,
                            style = BodyLStyle.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        Spacer(Modifier.width(SakuwiseSpacing.s))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = BodyLStyle.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                ),
                            )
                        }
                        innerTextField()
                    }
                    if (suffix != null) {
                        Spacer(Modifier.width(SakuwiseSpacing.s))
                        Text(
                            text = suffix,
                            style = CaptionStyle.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            },
        )

        if (hint != null) {
            Text(
                text = hint,
                style = CaptionStyle.copy(color = hintColor),
                modifier = Modifier.padding(top = SakuwiseSpacing.xs, start = SakuwiseSpacing.xs),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwFieldPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                var name by remember { mutableStateOf("Tunai") }
                SwField(value = name, onValueChange = { name = it }, label = "Nama akun")
                var amount by remember { mutableStateOf("") }
                SwField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Saldo awal",
                    prefix = "Rp",
                    placeholder = "0",
                    hint = "Boleh 0 — kamu bisa update nanti.",
                )
                SwField(
                    value = "",
                    onValueChange = {},
                    label = "Email *",
                    isError = true,
                    hint = "Email tidak valid",
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwFieldPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.m),
            ) {
                var name by remember { mutableStateOf("Tunai") }
                SwField(value = name, onValueChange = { name = it }, label = "Nama akun")
                var amount by remember { mutableStateOf("") }
                SwField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Saldo awal",
                    prefix = "Rp",
                    placeholder = "0",
                )
            }
        }
    }
}
