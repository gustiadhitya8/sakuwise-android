package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Light palette ──────────────────────────────────────────────────────────
val ForestGreen = Color(0xFF0F4C3A)
val ForestGreenHover = Color(0xFF0A3A2C)
val Cream = Color(0xFFF5F1E8)
val CreamSurface = Color(0xFFFAF7F0)
val CreamSurfaceElev = Color(0xFFFFFFFF)
val InkDark = Color(0xFF1A2520)
val InkMuted = Color(0xFF5C6963)
val InkSubtle = Color(0xFF8B948F)
val BorderLight = Color(0xFFE8E0CC)
val BorderStrongLight = Color(0xFFD6CDB4)
val PrimaryContainerLight = Color(0xFFD4E8DC)
val OnPrimaryContainerLight = Color(0xFF0A2E22)
val MintSage = Color(0xFF7BC4A4)
val MintSageSoft = Color(0xFFD4E8DC)
val SuccessLight = Color(0xFF2D7A4F)
val SuccessSoftLight = Color(0xFFD6EDDC)
val WarningLight = Color(0xFFC68A2E)
val WarningSoftLight = Color(0xFFF4E4C8)
val DangerLight = Color(0xFFB84545)
val DangerSoftLight = Color(0xFFF1D6D6)
val InfoLight = Color(0xFF4A6FA5)
val InfoSoftLight = Color(0xFFD6E0EE)

// ── Dark palette ───────────────────────────────────────────────────────────
val DarkBg = Color(0xFF0F1411)
val DarkSurface = Color(0xFF1A211D)
val DarkSurfaceElev = Color(0xFF232B26)
val InkLight = Color(0xFFF0EDE3)
val InkMutedDark = Color(0xFFA8B0AB)
val InkSubtleDark = Color(0xFF6B7570)
val BorderDark = Color(0xFF2D3631)
val BorderStrongDark = Color(0xFF3D4742)
val DarkPrimaryHover = Color(0xFF9DD4BA)
val OnPrimaryDark = Color(0xFF0A1F18)
val PrimaryContainerDark = Color(0xFF1F3329)
val OnPrimaryContainerDark = Color(0xFFC4E8D4)
val AccentDark = Color(0xFFC4E8D4)
val AccentSoftDark = Color(0xFF1F3329)
val SuccessDark = Color(0xFF6DC48F)
val SuccessSoftDark = Color(0xFF1E3526)
val WarningDark = Color(0xFFE0A954)
val WarningSoftDark = Color(0xFF3B2E18)
val DangerDark = Color(0xFFD67373)
val DangerSoftDark = Color(0xFF3D1F1F)
val InfoDark = Color(0xFF7FA0C7)
val InfoSoftDark = Color(0xFF1E2A3A)

// ── Color schemes ──────────────────────────────────────────────────────────

val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Cream,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = MintSage,
    onSecondary = InkDark,
    secondaryContainer = MintSageSoft,
    onSecondaryContainer = Color(0xFF0A2E22),
    tertiary = InfoLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = InfoSoftLight,
    onTertiaryContainer = Color(0xFF1A2A40),
    error = DangerLight,
    onError = Color(0xFFFFFFFF),
    errorContainer = DangerSoftLight,
    onErrorContainer = Color(0xFF3D1414),
    background = Cream,
    onBackground = InkDark,
    surface = CreamSurface,
    onSurface = InkDark,
    surfaceVariant = BorderLight,
    onSurfaceVariant = InkMuted,
    outline = InkSubtle,
    outlineVariant = BorderStrongLight,
    surfaceContainerHigh = CreamSurfaceElev,
    surfaceContainerLow = CreamSurface,
    surfaceContainer = Cream,
)

val DarkColorScheme = darkColorScheme(
    primary = MintSage,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = AccentDark,
    onSecondary = Color(0xFF0A1F18),
    secondaryContainer = AccentSoftDark,
    onSecondaryContainer = Color(0xFFC4E8D4),
    tertiary = InfoDark,
    onTertiary = Color(0xFF0A1520),
    tertiaryContainer = InfoSoftDark,
    onTertiaryContainer = Color(0xFFB0C8E8),
    error = DangerDark,
    onError = Color(0xFF1A0808),
    errorContainer = DangerSoftDark,
    onErrorContainer = Color(0xFFF0C0C0),
    background = DarkBg,
    onBackground = InkLight,
    surface = DarkSurface,
    onSurface = InkLight,
    surfaceVariant = BorderDark,
    onSurfaceVariant = InkMutedDark,
    outline = InkSubtleDark,
    outlineVariant = BorderStrongDark,
    surfaceContainerHigh = DarkSurfaceElev,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkBg,
)
