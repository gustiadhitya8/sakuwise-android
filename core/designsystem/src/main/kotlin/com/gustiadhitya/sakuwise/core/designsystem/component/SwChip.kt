package com.gustiadhitya.sakuwise.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gustiadhitya.sakuwise.core.designsystem.theme.BodyStyle
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseShapes
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseSpacing
import com.gustiadhitya.sakuwise.core.designsystem.theme.SakuwiseTheme

private val ChipHeight = 36.dp
private val ChipHorizontalPadding = 16.dp

@Composable
fun SwChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary
                         else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .height(ChipHeight)
            .background(containerColor, SakuwiseShapes.full)
            .then(
                if (!selected) Modifier.border(SakuwiseSpacing.borderThin, MaterialTheme.colorScheme.outline, SakuwiseShapes.full)
                else Modifier
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = ChipHorizontalPadding)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BodyStyle.copy(color = contentColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwChipPreviewLight() {
    SakuwiseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwChip(text = "Semua", selected = false, onClick = {})
                SwChip(text = "Needs", selected = true, onClick = {})
                SwChip(text = "Wants", selected = false, onClick = {})
                SwChip(text = "Investment", selected = false, onClick = {})
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SwChipPreviewDark() {
    SakuwiseTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(SakuwiseSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(SakuwiseSpacing.s),
            ) {
                SwChip(text = "Semua", selected = false, onClick = {})
                SwChip(text = "Needs", selected = true, onClick = {})
                SwChip(text = "Wants", selected = false, onClick = {})
            }
        }
    }
}
