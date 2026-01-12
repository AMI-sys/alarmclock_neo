package ru.alarmneo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import ru.alarmneo.app.data.ThemeMode

@Composable
fun alarmneoTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    // Переключаем Neu-палитру при смене режима
    LaunchedEffect(dark) {
        Neu.apply(dark)
    }

    // Палитры создаём тут, чтобы они всегда брали актуальные цвета
    val colors = if (dark) {
        darkColors(
            primary = Purple80,
            secondary = PurpleGrey80,
            background = Neu.bg,
            surface = Neu.bg,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Neu.onBg,
            onSurface = Neu.onBg
        )
    } else {
        lightColors(
            primary = Purple40,
            secondary = PurpleGrey40,
            background = Neu.bg,
            surface = Neu.bg,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F)
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable fun textPrimary() = MaterialTheme.colors.onSurface
@Composable fun textSecondary() = MaterialTheme.colors.onSurface.copy(alpha = 0.72f)
@Composable fun textHint() = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)