package ru.alarmneo.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Базовая Typography для Material M2.
// Пока без кастомных шрифтов — это правильно на данном этапе.
val Typography = Typography(
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 0.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    body1 = TextStyle(
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    ),
    button = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
