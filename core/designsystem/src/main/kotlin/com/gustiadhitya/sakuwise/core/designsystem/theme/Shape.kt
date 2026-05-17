package com.gustiadhitya.sakuwise.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object SakuwiseShapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val xl2 = RoundedCornerShape(24.dp)
    val full = RoundedCornerShape(9999.dp)
    val button = RoundedCornerShape(14.dp)  // SW_Button.borderRadius=14 from prototype
    val card = RoundedCornerShape(18.dp)    // SW_Card.borderRadius=18 from prototype
}

val SakuwiseMaterialShapes = Shapes(
    extraSmall = SakuwiseShapes.xs,
    small = SakuwiseShapes.sm,
    medium = SakuwiseShapes.md,
    large = SakuwiseShapes.lg,
    extraLarge = SakuwiseShapes.xl,
)
