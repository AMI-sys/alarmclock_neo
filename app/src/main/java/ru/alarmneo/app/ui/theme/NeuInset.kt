package ru.alarmneo.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neuInset(
    cornerRadius: Dp = 16.dp,
    depth: Dp = 6.dp,
    tint: Color = Color.Transparent,
    topLeftLight: Color = Neu.lightShadow,
    bottomRightDark: Color = Neu.darkShadow,
    // ✅ по умолчанию 0 — никаких странных тонких линий
    edgeAlpha: Float = 0f
): Modifier = this.drawWithCache {
    val r = cornerRadius.toPx()
    val d = depth.toPx().coerceAtLeast(1f)

    val light = topLeftLight.copy(alpha = topLeftLight.alpha * 0.55f)
    val dark = bottomRightDark.copy(alpha = bottomRightDark.alpha * 0.70f)

    val topLeftBrush = Brush.linearGradient(
        colors = listOf(light, Color.Transparent),
        start = Offset(0f, 0f),
        end = Offset(d * 2.2f, d * 2.2f)
    )

    val bottomRightBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, dark),
        start = Offset(size.width - d * 2.2f, size.height - d * 2.2f),
        end = Offset(size.width, size.height)
    )

    onDrawBehind {
        if (tint.alpha > 0f) {
            drawRoundRect(
                color = tint,
                cornerRadius = CornerRadius(r, r)
            )
        }

        drawRoundRect(
            brush = topLeftBrush,
            cornerRadius = CornerRadius(r, r)
        )

        drawRoundRect(
            brush = bottomRightBrush,
            cornerRadius = CornerRadius(r, r)
        )

        if (edgeAlpha > 0f) {
            drawRoundRect(
                color = Neu.outline.copy(alpha = edgeAlpha),
                cornerRadius = CornerRadius(r, r)
            )
        }
    }
}
