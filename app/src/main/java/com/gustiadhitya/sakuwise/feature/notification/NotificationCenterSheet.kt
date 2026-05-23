package com.gustiadhitya.sakuwise.feature.notification

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButton
import com.gustiadhitya.sakuwise.core.designsystem.components.SwButtonVariant
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwTheme
import com.gustiadhitya.sakuwise.core.designsystem.theme.SwType

/**
 * Lightweight notification "inbox" sheet. Items are derived from live app
 * state (no DB table) — V1 only surfaces the backup-overdue signal. The
 * sheet marks notifications as read on dismissal so the dashboard bell's
 * red dot clears without requiring the user to tap a specific item.
 */
data class NotificationItem(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconFg: Color,
    val onTap: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    items: List<NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    val sw = SwTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Mark seen as soon as the sheet is composed — opening counts as "seen".
    LaunchedEffect(Unit) { onMarkAllRead() }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.notifications_title),
                    color = sw.ink,
                    style = SwType.H2.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onDismiss),
                ) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.action_close),
                        tint = sw.inkMuted, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (items.isEmpty()) {
                EmptyNotifBody()
            } else {
                items.forEachIndexed { i, item ->
                    NotificationRow(item)
                    if (i < items.lastIndex) Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                SwButton(
                    text = stringResource(R.string.notifications_clear),
                    onClick = onDismiss,
                    variant = SwButtonVariant.Outline,
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    val sw = SwTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sw.bg)
            .clickable(onClick = item.onTap)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.iconBg),
        ) {
            Icon(item.icon, null, tint = item.iconFg, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(width = 12.dp, height = 1.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, color = sw.ink,
                style = SwType.LabelStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text(item.body, color = sw.inkMuted,
                style = SwType.LabelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp))
        }
        Icon(Icons.Outlined.ChevronRight, null,
            tint = sw.inkSubtle, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyNotifBody() {
    val sw = SwTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(sw.primaryContainer),
        ) {
            Icon(Icons.Outlined.CheckCircle, null,
                tint = sw.onPrimaryContainer, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.notifications_empty_title),
            color = sw.ink,
            style = SwType.LabelStrong.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.notifications_empty_body),
            color = sw.inkMuted,
            style = SwType.Body.copy(fontSize = 13.sp),
        )
    }
}

/**
 * Convenience: build the active notification list from preferences/state.
 * Currently only the backup-overdue signal — extend as more push-able
 * events land (reminder fires, recurring income generated, etc.).
 */
@Composable
fun rememberDefaultNotifications(
    backupOverdueDays: Int,
    onOpenBackup: () -> Unit,
): List<NotificationItem> {
    val sw = SwTheme.colors
    val items = mutableListOf<NotificationItem>()
    if (backupOverdueDays > 30) {
        val title = if (backupOverdueDays == Int.MAX_VALUE)
            stringResource(R.string.notif_backup_never_title)
        else
            stringResource(R.string.notif_backup_overdue_title)
        val body = if (backupOverdueDays == Int.MAX_VALUE)
            stringResource(R.string.notif_backup_never_body)
        else
            stringResource(R.string.notif_backup_overdue_body_format, backupOverdueDays)
        items += NotificationItem(
            title = title,
            body = body,
            icon = Icons.Outlined.Shield,
            iconBg = sw.warningSoft,
            iconFg = sw.warning,
            onTap = onOpenBackup,
        )
    }
    return items
}
