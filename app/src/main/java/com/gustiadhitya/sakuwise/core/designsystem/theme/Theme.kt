package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun lightSchemeFrom(sw: SwColors) = lightColorScheme(
    primary = sw.primary,
    onPrimary = sw.onPrimary,
    primaryContainer = sw.primaryContainer,
    onPrimaryContainer = sw.onPrimaryContainer,
    secondary = sw.accent,
    onSecondary = sw.onPrimaryContainer,
    secondaryContainer = sw.accentSoft,
    onSecondaryContainer = sw.onPrimaryContainer,
    tertiary = sw.info,
    tertiaryContainer = sw.infoSoft,
    background = sw.bg,
    onBackground = sw.ink,
    surface = sw.surface,
    onSurface = sw.ink,
    surfaceVariant = sw.border,
    onSurfaceVariant = sw.inkMuted,
    surfaceContainerHigh = sw.surfaceElev,
    outline = sw.inkSubtle,
    outlineVariant = sw.borderStrong,
    error = sw.danger,
    onError = sw.onPrimary,
    errorContainer = sw.dangerSoft,
)

private fun darkSchemeFrom(sw: SwColors) = darkColorScheme(
    primary = sw.primary,
    onPrimary = sw.onPrimary,
    primaryContainer = sw.primaryContainer,
    onPrimaryContainer = sw.onPrimaryContainer,
    secondary = sw.accent,
    onSecondary = sw.onPrimaryContainer,
    secondaryContainer = sw.accentSoft,
    onSecondaryContainer = sw.onPrimaryContainer,
    tertiary = sw.info,
    tertiaryContainer = sw.infoSoft,
    background = sw.bg,
    onBackground = sw.ink,
    surface = sw.surface,
    onSurface = sw.ink,
    surfaceVariant = sw.border,
    onSurfaceVariant = sw.inkMuted,
    surfaceContainerHigh = sw.surfaceElev,
    outline = sw.inkSubtle,
    outlineVariant = sw.borderStrong,
    error = sw.danger,
    onError = sw.onPrimary,
    errorContainer = sw.dangerSoft,
)

@Composable
fun SakuwiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val sw = if (darkTheme) DarkSwColors else LightSwColors
    val scheme = if (darkTheme) darkSchemeFrom(sw) else lightSchemeFrom(sw)
    CompositionLocalProvider(LocalSwColors provides sw) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SwTypography,
            content = content,
        )
    }
}
