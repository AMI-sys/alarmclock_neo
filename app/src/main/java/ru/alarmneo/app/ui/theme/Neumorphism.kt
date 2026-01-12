package ru.alarmneo.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Neumorphism configuration tuned for milk background.
 *
 * Base background: #E4DDD3
 * Primary text/icon: #3A4764
 * Outline: muted blue (for strokes/dividers)
 *
 * Shadows:
 *  - Light: #F5EFE8
 *  - Dark:  #CFC7BC
 */
@Immutable
object Neu {

    // Base surface / background
    val bg: Color = MilkBg

    // Main content color on bg (text, icons)
    val onBg: Color = BluePrimary

    // Borders / dividers / subtle strokes
    val outline: Color = BlueMuted.copy(alpha = 0.55f)

    // Neumorphic shadows
    val lightShadow: Color = ShadowLight
    val darkShadow: Color = ShadowDark

    // Default elevation values (if you reference them anywhere)
    val elevationSmall = 4f
    val elevationMedium = 8f
    val elevationLarge = 12f
}
