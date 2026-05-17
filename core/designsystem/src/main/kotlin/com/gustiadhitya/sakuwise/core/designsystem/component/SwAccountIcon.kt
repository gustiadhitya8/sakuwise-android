package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

@Composable
fun SwAccountIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = SakuwiseSpacing.xxxxxl,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(SakuwiseShapes.md)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(SakuwiseSpacing.xxl),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwAccountIconPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "Mandiri")
                SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "BCA")
                SwAccountIcon(icon = Icons.Outlined.CreditCard, contentDescription = "GoPay")
                SwAccountIcon(icon = Icons.Outlined.Savings, contentDescription = "Tunai")
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwAccountIconPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwAccountIcon(icon = Icons.Outlined.AccountBalance, contentDescription = "Mandiri")
                SwAccountIcon(icon = Icons.Outlined.CreditCard, contentDescription = "GoPay")
            }
        }
    }
}
