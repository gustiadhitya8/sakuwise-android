package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Sakuwise semantic color set. Material 3 maps the obvious tokens; the rest
 * (success / warning / info / soft variants / track / fixed-dark) live here.
 */
@Immutable
data class SwColors(
    val bg: Color,
    val surface: Color,
    val surfaceElev: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    val border: Color,
    val borderStrong: Color,
    val primary: Color,
    val primaryHover: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val track: Color,
    val fixedDarkOnMint: Color,
    val isDark: Boolean,
)

val LightSwColors = SwColors(
    bg = Bg, surface = Surface, surfaceElev = SurfaceElev,
    ink = Ink, inkMuted = InkMuted, inkSubtle = InkSubtle,
    border = Border, borderStrong = BorderStrong,
    primary = Primary, primaryHover = PrimaryHover, onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer, onPrimaryContainer = OnPrimaryContainer,
    accent = Accent, accentSoft = AccentSoft,
    success = Success, successSoft = SuccessSoft,
    warning = Warning, warningSoft = WarningSoft,
    danger = Danger, dangerSoft = DangerSoft,
    info = Info, infoSoft = InfoSoft,
    track = TrackLight,
    fixedDarkOnMint = FixedDarkOnMint,
    isDark = false,
)

val DarkSwColors = SwColors(
    bg = BgDark, surface = SurfaceDark, surfaceElev = SurfaceElevDark,
    ink = InkDark, inkMuted = InkMutedDark, inkSubtle = InkSubtleDark,
    border = BorderDark, borderStrong = BorderStrongDark,
    primary = PrimaryDark, primaryHover = PrimaryHoverDark, onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark, onPrimaryContainer = OnPrimaryContainerDark,
    accent = AccentDark, accentSoft = AccentSoftDark,
    success = SuccessDark, successSoft = SuccessSoftDark,
    warning = WarningDark, warningSoft = WarningSoftDark,
    danger = DangerDark, dangerSoft = DangerSoftDark,
    info = InfoDark, infoSoft = InfoSoftDark,
    track = TrackDark,
    // Inverted in dark mode — mint bg is now dark (AccentDark = #2D5E48),
    // so the "fixed on mint" fg becomes light.
    fixedDarkOnMint = FixedDarkOnMintDark,
    isDark = true,
)

val LocalSwColors = staticCompositionLocalOf { LightSwColors }

object SwTheme {
    val colors: SwColors
        @Composable @ReadOnlyComposable
        get() = LocalSwColors.current
}
