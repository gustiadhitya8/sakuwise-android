package com.gustiadhitya.sakuwise.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/**
 * Reusable list-sort affordance: a 40dp tinted icon button that opens a
 * DropdownMenu with one entry per [options]. The selected option is
 * surfaced with a check mark; tapping a different option calls
 * [onPick] with the new selection.
 *
 * Pair with [rememberSortState] in the parent composable so the
 * selection survives recomposition.
 */
@Composable
fun <T> SwSortMenu(
    options: List<SortOption<T>>,
    selected: T,
    onPick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sw = SwTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(sw.surface)
                .clickable { expanded = true },
        ) {
            Icon(
                Icons.Outlined.SwapVert,
                contentDescription = null,
                tint = sw.ink,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { opt ->
                val isSelected = opt.value == selected
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                opt.label,
                                color = sw.ink,
                                style = SwType.LabelStrong.copy(
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                ),
                            )
                            if (isSelected) {
                                Spacer(Modifier.size(8.dp))
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = sw.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        if (!isSelected) onPick(opt.value)
                    },
                )
            }
        }
    }
}

data class SortOption<T>(val value: T, val label: String)

/** Default sort dimensions for an asset / transaction list: by date or
 *  by amount, ascending or descending. Use [assetSortOptions] to surface
 *  the matching labels from values/strings.xml. */
enum class AssetSort { DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC }

@Composable
fun assetSortOptions(): List<SortOption<AssetSort>> = listOf(
    SortOption(
        AssetSort.DATE_DESC,
        androidx.compose.ui.res.stringResource(com.gustiadhitya.sakuwise.R.string.sort_date_newest),
    ),
    SortOption(
        AssetSort.DATE_ASC,
        androidx.compose.ui.res.stringResource(com.gustiadhitya.sakuwise.R.string.sort_date_oldest),
    ),
    SortOption(
        AssetSort.AMOUNT_DESC,
        androidx.compose.ui.res.stringResource(com.gustiadhitya.sakuwise.R.string.sort_amount_high),
    ),
    SortOption(
        AssetSort.AMOUNT_ASC,
        androidx.compose.ui.res.stringResource(com.gustiadhitya.sakuwise.R.string.sort_amount_low),
    ),
)
