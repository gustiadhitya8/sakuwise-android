package com.gustiadhitya.sakuwise.core.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Returns true when POST_NOTIFICATIONS is granted, or N/A (pre-Android 13).
 */
fun hasNotificationPermission(ctx: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        ctx,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Remembers a permission launcher; invoking the returned lambda will either
 * fire onGranted immediately (already granted / pre-API 33) or prompt the
 * user — onGranted is invoked with the final granted state on result.
 */
@Composable
fun rememberNotificationPermissionRequester(
    onResult: (granted: Boolean) -> Unit,
): () -> Unit {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )
    return remember(launcher) {
        {
            if (hasNotificationPermission(ctx)) {
                onResult(true)
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
