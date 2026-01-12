package ru.alarmneo.app.ui.theme

import android.graphics.Color as AColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neuShadow(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    lightShadow: Color = Neu.lightShadow,
    darkShadow: Color = Neu.darkShadow,

    // ✅ обратно совместимо со старым кодом
    outlineColor: Color = Neu.outline,
    outlineWidth: Dp = 0.dp
): Modifier = this.drawWithCache {
    val r = cornerRadius.toPx()
    val blur = elevation.toPx().coerceAtLeast(1f)

    // Смещение (как у неоморфизма)
    val offset = blur * 0.55f

    // Чтобы тень не обрезалась по краям
    val inset = blur * 0.60f

    // ✅ Paint кэшируется, НЕ создаётся каждый кадр (плавность)
    val paint = AndroidPaint().apply {
        isAntiAlias = true
        style = AndroidPaint.Style.FILL
        color = AColor.TRANSPARENT
    }

    val strokePx = outlineWidth.toPx().coerceAtLeast(0f)

    onDrawBehind {
        val left = inset
        val top = inset
        val right = size.width - inset
        val bottom = size.height - inset
        if (right <= left || bottom <= top) return@onDrawBehind

        drawIntoCanvas { canvas ->
            // Тёмная тень (низ-право)
            paint.setShadowLayer(blur, offset, offset, darkShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(left, top, right, bottom, r, r, paint)

            // Светлая тень (верх-лево)
            paint.setShadowLayer(blur, -offset, -offset, lightShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(left, top, right, bottom, r, r, paint)

            paint.clearShadowLayer()
        }

        // ✅ тонкая обводка только если реально нужна
        if (strokePx > 0f) {
            drawRoundRect(
                color = outlineColor,
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = strokePx)
            )
        }
    }
}
