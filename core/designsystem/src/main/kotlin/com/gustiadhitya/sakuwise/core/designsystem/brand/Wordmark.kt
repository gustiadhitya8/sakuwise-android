package com.gustiadhitya.sakuwise.core.designsystem.brand

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.FigtreeFontFamily
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

// "Saku" ink + "wise" primary. Figtree Bold, -0.025em tracking. Ported from brand/logos.jsx.
@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
) {
    val inkColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = inkColor)) { append("Saku") }
            withStyle(SpanStyle(color = primaryColor)) { append("wise") }
        },
        fontFamily = FigtreeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        letterSpacing = (-0.025f * fontSize.value).sp,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun WordmarkPreviewLight() {
    SakuwiseTheme { Wordmark() }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WordmarkPreviewDark() {
    SakuwiseTheme(darkTheme = true) { Wordmark() }
}
