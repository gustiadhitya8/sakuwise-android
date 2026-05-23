package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Sakuwise typography — Figtree (fallback to system sans-serif until the font
 * binaries land in res/font/). Sizes use sp per A11Y guidance; tabular nums
 * are enforced explicitly on every Amount style (and via [RupiahText] in :core:ui).
 */
val SwFontFamily: FontFamily = FontFamily.Default

private val Tnum = "tnum"

private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun ts(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Float = 0f,
    tnum: Boolean = false,
): TextStyle = TextStyle(
    fontFamily = SwFontFamily,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.em,
    fontFeatureSettings = if (tnum) Tnum else null,
    lineHeightStyle = tightLineHeight,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

object SwType {
    val DisplayL    = ts(40, 48, FontWeight.Bold,   -0.02f)
    val DisplayM    = ts(32, 40, FontWeight.Bold,   -0.02f)
    val H1          = ts(26, 34, FontWeight.Bold,   -0.01f)
    val H2          = ts(20, 28, FontWeight.SemiBold, -0.005f)
    val H3          = ts(17, 24, FontWeight.SemiBold)
    val BodyL       = ts(16, 24, FontWeight.Normal)
    val Body        = ts(14, 20, FontWeight.Normal)
    val Caption     = ts(12, 16, FontWeight.Medium, 0.01f)
    val AmountXL    = ts(36, 40, FontWeight.Bold,    -0.02f, tnum = true)
    val AmountL     = ts(22, 28, FontWeight.SemiBold,-0.01f, tnum = true)
    val Amount      = ts(16, 22, FontWeight.SemiBold,        tnum = true)
    val SectionLabel = ts(11, 14, FontWeight.Bold, 0.08f)
    val LabelStrong = ts(13, 18, FontWeight.SemiBold)
    val LabelSmall  = ts(11, 14, FontWeight.Medium)
}

internal val SwTypography = Typography(
    displayLarge   = SwType.DisplayL,
    displayMedium  = SwType.DisplayM,
    headlineLarge  = SwType.H1,
    headlineMedium = SwType.H2,
    titleLarge     = SwType.H2,
    titleMedium    = SwType.H3,
    titleSmall     = SwType.LabelStrong,
    bodyLarge      = SwType.BodyL,
    bodyMedium     = SwType.Body,
    bodySmall      = SwType.Caption,
    labelLarge     = SwType.LabelStrong,
    labelMedium    = SwType.LabelSmall,
    labelSmall     = SwType.Caption,
)
