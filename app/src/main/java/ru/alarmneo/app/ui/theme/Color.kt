package ru.alarmneo.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * App palette (milk + blue + warm accent)
 *
 * Base:
 * - Milk background: #E4DDD3
 * - Primary blue:    #3A4764
 * - Warm accent:     #D9A066
 *
 * Neumorphism shadows tuned for milk background:
 * - Light shadow:    #F5EFE8
 * - Dark shadow:     #CFC7BC
 */

val MilkBg = Color(0xFFE4DDD3)

val BluePrimary = Color(0xFF3A4764)
val BlueMuted = Color(0xFF6B7893)

val AccentWarm = Color(0xFFD9A066)

val ShadowLight = Color(0xFFF5EFE8)
val ShadowDark = Color(0xFFCFC7BC)

// ------------------------------------------------------------------
// Backward-compat names (used by Theme.kt right now).
// We map old "Purple*" identifiers to the new palette so the app
// immediately adopts the new colors without touching other files yet.
// ------------------------------------------------------------------

val Purple40 = BluePrimary
val PurpleGrey40 = AccentWarm
val Pink40 = BlueMuted

// Dark palette placeholders (we'll tune dark theme later if you need it)
val Purple80 = Color(0xFFB9C6E8)      // light tint of BluePrimary
val PurpleGrey80 = Color(0xFFE7C9A6)  // light tint of AccentWarm
val Pink80 = Color(0xFFD4DAEA)        // soft neutral
