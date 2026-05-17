package com.gustiadhitya.sakuwise.core.designsystem.brand

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.FigtreeFontFamily
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

// DaunMark + Wordmark lockup. Proportional to markSize parameter; no hardcoded layout dims.
@Composable
fun Lockup(
    modifier: Modifier = Modifier,
    markSize: Dp = 48.dp,
    vertical: Boolean = false,
    showTagline: Boolean = false,
) {
    val wordmarkFontSize = (markSize.value * 0.72f).sp
    val taglineFontSize = (markSize.value * 0.30f).sp
    val gapLarge = (markSize.value * 0.40f).dp
    val gapSmall = (markSize.value * 0.16f).dp
    val horizontalGap = (markSize.value * 0.35f).dp

    if (vertical) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DaunMark(size = (markSize.value * 1.6f).dp)
            Spacer(Modifier.height(gapLarge))
            Wordmark(fontSize = wordmarkFontSize)
            if (showTagline) {
                Spacer(Modifier.height(gapSmall))
                Text(
                    text = "Rencanakan. Catat. Tenang.",
                    fontFamily = FigtreeFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = taglineFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DaunMark(size = markSize)
            Spacer(Modifier.width(horizontalGap))
            Column {
                Wordmark(fontSize = wordmarkFontSize)
                if (showTagline) {
                    Spacer(Modifier.height(gapSmall))
                    Text(
                        text = "Rencanakan. Catat. Tenang.",
                        fontFamily = FigtreeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = taglineFontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockupHorizontalPreview() {
    SakuwiseTheme { Lockup(markSize = 48.dp) }
}

@Preview(showBackground = true)
@Composable
private fun LockupVerticalTaglinePreview() {
    SakuwiseTheme { Lockup(markSize = 56.dp, vertical = true, showTagline = true) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LockupDarkPreview() {
    SakuwiseTheme(darkTheme = true) { Lockup(markSize = 48.dp) }
}
