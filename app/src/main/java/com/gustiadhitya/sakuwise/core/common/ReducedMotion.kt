package com.gustiadhitya.sakuwise.core.common

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * A11Y-012 — honor "Remove animations" / Animator duration scale = 0 setting.
 * When [isReducedMotion] is true, animations should collapse to fade-only,
 * fills should be instant, and screen slides should become cross-fades.
 */
@Composable
@ReadOnlyComposable
fun isReducedMotion(): Boolean {
    val ctx = LocalContext.current
    return isReducedMotionInternal(ctx)
}

internal fun isReducedMotionInternal(ctx: Context): Boolean = try {
    val scale = Settings.Global.getFloat(
        ctx.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    scale == 0f
} catch (_: Throwable) {
    false
}
