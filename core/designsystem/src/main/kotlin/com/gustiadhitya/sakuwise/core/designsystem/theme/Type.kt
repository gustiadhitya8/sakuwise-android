package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gustiadhitya.sakuwise.core.designsystem.R

val FigtreeFontFamily = FontFamily(
    Font(R.font.figtree, FontWeight.Light),
    Font(R.font.figtree, FontWeight.Normal),
    Font(R.font.figtree, FontWeight.Medium),
    Font(R.font.figtree, FontWeight.SemiBold),
    Font(R.font.figtree, FontWeight.Bold),
    Font(R.font.figtree, FontWeight.ExtraBold),
)

// ── Sakuwise custom type styles (beyond M3 typography slots) ──────────────

val DisplayLStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.8).sp,
)

val DisplayMStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.64).sp,
)

val H1Style = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.26).sp,
)

val H2Style = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.1).sp,
)

val H3Style = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
)

val BodyLStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
)

val BodyStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
)

val CaptionStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.12.sp,
)

// Button text styles — SemiBold weight, tight tracking per SW_Button prototype
val ButtonTextSmStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    letterSpacing = (-0.065).sp,
)
val ButtonTextMdStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    letterSpacing = (-0.075).sp,
)
val ButtonTextLgStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    letterSpacing = (-0.08).sp,
)

// Amount styles — tabular nums enforced via fontFeatureSettings at call site
val AmountXLStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.72).sp,
)

val AmountLStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.22).sp,
)

val AmountStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
)

// ── Material 3 Typography wiring ──────────────────────────────────────────

val SakuwiseTypography = Typography(
    displayLarge = DisplayLStyle,
    displayMedium = DisplayMStyle,
    displaySmall = H1Style,
    headlineLarge = H1Style,
    headlineMedium = H2Style,
    headlineSmall = H3Style,
    titleLarge = H2Style,
    titleMedium = H3Style,
    titleSmall = BodyLStyle.copy(fontWeight = FontWeight.Medium),
    bodyLarge = BodyLStyle,
    bodyMedium = BodyStyle,
    bodySmall = CaptionStyle,
    labelLarge = BodyStyle.copy(fontWeight = FontWeight.Medium),
    labelMedium = CaptionStyle,
    labelSmall = CaptionStyle.copy(fontSize = 11.sp, letterSpacing = 0.88.sp),
)
