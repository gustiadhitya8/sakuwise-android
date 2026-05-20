package com.gustiadhitya.sakuwise.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

enum class AddTxnKind { Expense, Income, Transfer, Ocr }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTxnPickerSheet(
    onDismiss: () -> Unit,
    onPick: (AddTxnKind) -> Unit,
) {
    val sw = SwTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = sw.surface,
        contentColor = sw.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(sw.borderStrong),
            )
        },
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp)) {
            Text(
                stringResource(R.string.addtxn_question),
                color = sw.ink,
                style = SwType.H2.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.height(16.dp))
            PickerRow(
                icon = Icons.Outlined.TrendingDown,
                title = stringResource(R.string.addtxn_expense),
                subtitle = stringResource(R.string.addtxn_expense_sub),
                bg = sw.dangerSoft,
                fg = sw.danger,
                onClick = { onPick(AddTxnKind.Expense) },
            )
            Spacer(Modifier.height(10.dp))
            PickerRow(
                icon = Icons.Outlined.TrendingUp,
                title = stringResource(R.string.addtxn_income),
                subtitle = stringResource(R.string.addtxn_income_sub),
                bg = sw.successSoft,
                fg = sw.success,
                onClick = { onPick(AddTxnKind.Income) },
            )
            Spacer(Modifier.height(10.dp))
            PickerRow(
                icon = Icons.Outlined.SwapHoriz,
                title = stringResource(R.string.addtxn_transfer),
                subtitle = stringResource(R.string.addtxn_transfer_sub),
                bg = sw.infoSoft,
                fg = sw.info,
                onClick = { onPick(AddTxnKind.Transfer) },
            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(sw.border))
            Spacer(Modifier.height(16.dp))
            PickerRow(
                icon = Icons.Outlined.CameraAlt,
                title = stringResource(R.string.addtxn_ocr),
                subtitle = stringResource(R.string.addtxn_ocr_sub),
                bg = sw.primaryContainer,
                fg = sw.onPrimaryContainer,
                onClick = { onPick(AddTxnKind.Ocr) },
            )
        }
    }
}

@Composable
private fun PickerRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bg: Color,
    fg: Color,
    onClick: () -> Unit,
) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg),
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                subtitle, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 12.sp),
            )
        }
    }
}
