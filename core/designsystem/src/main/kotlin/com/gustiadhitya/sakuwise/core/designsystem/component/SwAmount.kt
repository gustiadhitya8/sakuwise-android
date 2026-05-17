package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.gustiadhitya.sakuwise.core.designsystem.theme.H1Style
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

enum class SwAmountDisplay { Full, Short }

@Composable
fun SwAmount(
    amount: Long,
    modifier: Modifier = Modifier,
    display: SwAmountDisplay = SwAmountDisplay.Full,
    style: TextStyle = H1Style,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    RupiahText(
        amount = amount,
        modifier = modifier,
        style = style.copy(color = color),
        short = display == SwAmountDisplay.Short,
    )
}

@Preview(showBackground = true)
@Composable
private fun SwAmountPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwAmount(amount = 4_700_000L)
                SwAmount(
                    amount = 15_500_000L,
                    style = MaterialTheme.typography.titleMedium,
                )
                SwAmount(
                    amount = 5_200_000L,
                    display = SwAmountDisplay.Short,
                    style = MaterialTheme.typography.bodyMedium,
                )
                SwAmount(
                    amount = -10_800_000L,
                    display = SwAmountDisplay.Short,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwAmountPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                verticalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwAmount(amount = 4_700_000L)
                SwAmount(
                    amount = -10_800_000L,
                    display = SwAmountDisplay.Short,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
