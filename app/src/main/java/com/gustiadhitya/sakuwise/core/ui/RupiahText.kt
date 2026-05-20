package com.gustiadhitya.sakuwise.core.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.gustiadhitya.sakuwise.core.common.toRupiah
import com.gustiadhitya.sakuwise.core.common.toRupiahShort
import com.gustiadhitya.sakuwise.core.common.toRupiahSpoken
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/**
 * RupiahText — the only sanctioned way to render Rupiah in the app.
 * Always emits tnum (tabular figures) so columns of numbers line up.
 * Sets a spoken-out contentDescription for TalkBack (A11Y-013).
 */
@Composable
fun RupiahText(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = SwType.Amount,
    color: Color = Color.Unspecified,
    sign: RupiahSign = RupiahSign.None,
    short: Boolean = false,
    prefixOpacity: Float = 0.78f,
) {
    val resolved = color.takeOrElse(LocalContentColor.current)
    val display = if (short) value.toRupiahShort() else value.toRupiah()
    val spoken = (if (sign == RupiahSign.Negative) "minus " else "") + value.toRupiahSpoken()

    // Split into "Rp" prefix vs numeric body so the prefix can dim.
    val rpIdx = display.indexOf("Rp")
    val numeric = display.removePrefix("Rp ").trimStart()
    val annotated = buildAnnotatedString {
        if (sign != RupiahSign.None) {
            withStyle(SpanStyle(color = resolved.copy(alpha = 0.7f))) {
                append(if (sign == RupiahSign.Positive) "+ " else "− ")
            }
        }
        if (rpIdx >= 0) {
            withStyle(
                SpanStyle(
                    color = resolved.copy(alpha = prefixOpacity),
                    fontWeight = FontWeight.Medium,
                    fontSize = style.fontSize * 0.62f,
                )
            ) { append("Rp ") }
        }
        append(numeric)
    }

    Text(
        text = annotated,
        modifier = modifier.semantics { contentDescription = spoken },
        style = style.copy(fontFeatureSettings = "tnum"),
        color = resolved,
    )
}

enum class RupiahSign { None, Positive, Negative }

private fun Color.takeOrElse(other: Color): Color =
    if (this == Color.Unspecified) other else this
