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

// ===== Neo Dark (final) =====
val DarkBg = Color(0xFF0F131A)            // чуть холодный графит
val DarkOnBg = Color(0xFFE8EDF5)          // мягкий белый (не слепит)
val DarkOnBgMuted = Color(0xFFB7C0D3)     // вторичный текст
val DarkOnBgFaint = Color(0xFF8E98AF)     // подсказки/лейблы

val DarkOutline = Color(0xFF7B87A3).copy(alpha = 0.20f)

// Shadows: без “ореолов”
val DarkShadowLight = Color(0xFF1B2331).copy(alpha = 0.92f)
val DarkShadowDark  = Color(0xFF05070B).copy(alpha = 0.88f)

