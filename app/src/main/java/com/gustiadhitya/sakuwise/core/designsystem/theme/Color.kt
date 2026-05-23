package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ─── Light Theme ───────────────────────────────────────────────
val Bg                  = Color(0xFFF5F1E8)
val Surface             = Color(0xFFFAF7F0)
val SurfaceElev         = Color(0xFFFFFFFF)
val Ink                 = Color(0xFF1A2520)
val InkMuted            = Color(0xFF5C6963)
val InkSubtle           = Color(0xFF8B948F)
val Border              = Color(0xFFE8E0CC)
val BorderStrong        = Color(0xFFD6CDB4)
val Primary             = Color(0xFF0F4C3A)
val PrimaryHover        = Color(0xFF0A3A2C)
val OnPrimary           = Color(0xFFF5F1E8)
val PrimaryContainer    = Color(0xFFD4E8DC)
val OnPrimaryContainer  = Color(0xFF0A2E22)
val Accent              = Color(0xFF7BC4A4)
val AccentSoft          = Color(0xFFD4E8DC)
val Success             = Color(0xFF2D7A4F)
val SuccessSoft         = Color(0xFFD6EDDC)
val Warning             = Color(0xFFC68A2E)
val WarningSoft         = Color(0xFFF4E4C8)
val Danger              = Color(0xFFB84545)
val DangerSoft          = Color(0xFFF1D6D6)
val Info                = Color(0xFF4A6FA5)
val InfoSoft            = Color(0xFFD6E0EE)

// Track bg for progress bars
val TrackLight          = Color(0xFFEDE5CF)
val TrackDark           = Color(0xFF2A332E)

// Fixed contrast text on mint hero (Wants alloc) — WCAG AA 8.4:1.
// Light mode: deep green on light mint bg. Dark mode: light mint on deep mint bg.
val FixedDarkOnMint     = Color(0xFF0A2820)
val FixedDarkOnMintDark = Color(0xFFC4E8D4)

// ─── Dark Theme ─────────────────────────────────────────────────
val BgDark                  = Color(0xFF0F1411)
val SurfaceDark             = Color(0xFF1A211D)
val SurfaceElevDark         = Color(0xFF232B26)
val InkDark                 = Color(0xFFF0EDE3)
val InkMutedDark            = Color(0xFFA8B0AB)
val InkSubtleDark           = Color(0xFF7A8480)   // A11Y-002: raised from #6B7570
val BorderDark              = Color(0xFF2D3631)
val BorderStrongDark        = Color(0xFF3D4742)
val PrimaryDark             = Color(0xFF7BC4A4)
val PrimaryHoverDark        = Color(0xFF9DD4BA)
val OnPrimaryDark           = Color(0xFF0A1F18)
// Brightened from 0xFF1F3329 — original blended into BgDark on the onboarding
// hero. New value keeps the green hue but raises luminance enough to read.
val PrimaryContainerDark    = Color(0xFF26442F)
val OnPrimaryContainerDark  = Color(0xFFC4E8D4)
// Was 0xFFC4E8D4 (same as OnPrimaryContainerDark) — that made any mint-bg +
// onPrimaryContainer-text combo invisible in dark mode (Deposito hero, etc.).
// Use a deep mint container so backgrounds read as backgrounds and the light
// onPrimaryContainer / fixedDarkOnMint text colors keep proper contrast.
val AccentDark              = Color(0xFF2D5E48)
val AccentSoftDark          = Color(0xFF1F3329)
val SuccessDark             = Color(0xFF6DC48F)
val SuccessSoftDark         = Color(0xFF1E3526)
val WarningDark             = Color(0xFFE0A954)
val WarningSoftDark         = Color(0xFF3B2E18)
val DangerDark              = Color(0xFFD67373)
val DangerSoftDark          = Color(0xFF3D1F1F)
val InfoDark                = Color(0xFF7FA0C7)
val InfoSoftDark            = Color(0xFF1E2A3A)
