package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class SakuwiseExtendedTokens(
    val inkSubtle: Color,
    val borderStrong: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val accent: Color,
    val accentSoft: Color,
)

val LightSakuwiseTokens = SakuwiseExtendedTokens(
    inkSubtle = InkSubtle,
    borderStrong = BorderStrongLight,
    success = SuccessLight,
    successSoft = SuccessSoftLight,
    warning = WarningLight,
    warningSoft = WarningSoftLight,
    danger = DangerLight,
    dangerSoft = DangerSoftLight,
    info = InfoLight,
    infoSoft = InfoSoftLight,
    accent = MintSage,
    accentSoft = MintSageSoft,
)

val DarkSakuwiseTokens = SakuwiseExtendedTokens(
    inkSubtle = InkSubtleDark,
    borderStrong = BorderStrongDark,
    success = SuccessDark,
    successSoft = SuccessSoftDark,
    warning = WarningDark,
    warningSoft = WarningSoftDark,
    danger = DangerDark,
    dangerSoft = DangerSoftDark,
    info = InfoDark,
    infoSoft = InfoSoftDark,
    accent = AccentDark,
    accentSoft = AccentSoftDark,
)

val LocalSakuwiseTokens = compositionLocalOf { LightSakuwiseTokens }

object SakuwiseTokens {
    val current: SakuwiseExtendedTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSakuwiseTokens.current
}
