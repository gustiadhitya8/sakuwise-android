package com.gustiadhitya.sakuwise.core.designsystem.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// ── A11Y-012: Reduce Motion ────────────────────────────────────────────────

val LocalReduceMotion = compositionLocalOf { false }

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

// ── SakuwiseTheme ──────────────────────────────────────────────────────────

@Composable
fun SakuwiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedTokens = if (darkTheme) DarkSakuwiseTokens else LightSakuwiseTokens
    val reduceMotion = rememberReduceMotion()

    CompositionLocalProvider(
        LocalSakuwiseTokens provides extendedTokens,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SakuwiseTypography,
            shapes = SakuwiseMaterialShapes,
            content = content,
        )
    }
}

object SakuwiseThemeValues {
    val tokens: SakuwiseExtendedTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSakuwiseTokens.current

    val reduceMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReduceMotion.current
}
