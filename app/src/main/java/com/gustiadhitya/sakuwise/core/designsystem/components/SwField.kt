package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/**
 * SwField — Sakuwise input. 52 dp tall, 1.5 dp border (red on error), 16 sp text.
 * For readOnly + onClick → behaves like a picker (FieldButton in proto).
 */
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
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    password: Boolean = false,
) {
    val sw = SwTheme.colors
    val isError = !error.isNullOrEmpty()
    val borderColor = if (isError) sw.danger else sw.border
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                label,
                color = sw.inkMuted,
                style = SwType.Caption.copy(fontSize = 12.sp),
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(shape)
                .background(sw.surface)
                .border(1.5.dp, borderColor, shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp),
        ) {
            if (prefix != null) {
                Text(
                    prefix,
                    color = sw.inkMuted,
                    style = SwType.LabelStrong.copy(fontSize = 15.sp),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            val textStyle = LocalTextStyle.current.merge(
                SwType.BodyL.copy(
                    color = sw.ink,
                    fontFeatureSettings = if (keyboardType == KeyboardType.Number || prefix == "Rp") "tnum" else null,
                ),
            )
            if (readOnly && onClick != null) {
                Text(
                    text = value.ifEmpty { placeholder.orEmpty() },
                    color = if (value.isEmpty()) sw.inkSubtle else sw.ink,
                    style = textStyle,
                    modifier = Modifier.weight(1f),
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(sw.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (password) KeyboardType.Password else keyboardType,
                    ),
                    visualTransformation = if (password)
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
                    else androidx.compose.ui.text.input.VisualTransformation.None,
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, color = sw.inkSubtle, style = textStyle)
                        }
                        inner()
                    },
                )
            }
            if (suffix != null) {
                Text(
                    suffix,
                    color = sw.inkMuted,
                    style = SwType.Body.copy(fontSize = 14.sp),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        val hintText = error ?: hint
        if (!hintText.isNullOrEmpty()) {
            Text(
                hintText,
                color = if (isError) sw.danger else sw.inkSubtle,
                style = SwType.LabelSmall.copy(fontSize = 11.sp),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}
