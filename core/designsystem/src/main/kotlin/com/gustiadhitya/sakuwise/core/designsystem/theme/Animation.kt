package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

private val SakuwiseEasing = CubicBezierEasing(0.2f, 0.7f, 0.3f, 1.0f)

object SakuwiseAnimation {
    val quick = tween<Float>(durationMillis = 120, easing = LinearEasing)
    val default = tween<Float>(durationMillis = 200, easing = SakuwiseEasing)
    val medium = tween<Float>(durationMillis = 280, easing = SakuwiseEasing)
    val slow = tween<Float>(durationMillis = 400, easing = SakuwiseEasing)
    val splashFade = tween<Float>(durationMillis = 600, easing = SakuwiseEasing)

    val quickInt = tween<Int>(durationMillis = 120, easing = LinearEasing)
    val defaultInt = tween<Int>(durationMillis = 200, easing = SakuwiseEasing)
    val mediumInt = tween<Int>(durationMillis = 280, easing = SakuwiseEasing)
    val slowInt = tween<Int>(durationMillis = 400, easing = SakuwiseEasing)

    const val pressDuration = 100
    const val pressScale = 0.97f
    const val pressAlpha = 0.85f
}
