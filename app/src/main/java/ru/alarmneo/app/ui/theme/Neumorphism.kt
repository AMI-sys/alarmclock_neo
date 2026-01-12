package ru.alarmneo.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Immutable
data class NeuPalette(
    val bg: Color,
    val onBg: Color,
    val outline: Color,
    val lightShadow: Color,
    val darkShadow: Color
)

private val LightNeu = NeuPalette(
    bg = MilkBg,
    onBg = BluePrimary,
    outline = BlueMuted.copy(alpha = 0.55f),
    lightShadow = ShadowLight,
    darkShadow = ShadowDark
)

private val DarkNeu = NeuPalette(
    bg = DarkBg,
    onBg = DarkOnBg,
    outline = DarkOutline,
    lightShadow = DarkShadowLight,
    darkShadow = DarkShadowDark
)

object Neu {
    private var palette by mutableStateOf(LightNeu)

    val bg: Color get() = palette.bg
    val onBg: Color get() = palette.onBg
    val outline: Color get() = palette.outline
    val lightShadow: Color get() = palette.lightShadow
    val darkShadow: Color get() = palette.darkShadow

    fun apply(dark: Boolean) {
        palette = if (dark) DarkNeu else LightNeu
    }

    // если где-то используешь:
    val elevationSmall = 4f
    val elevationMedium = 8f
    val elevationLarge = 12f
}
