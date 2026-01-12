package ru.alarmneo.app.ui.theme

import android.graphics.Color as AColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neuShadow(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    lightShadow: androidx.compose.ui.graphics.Color = Neu.lightShadow,
    darkShadow: androidx.compose.ui.graphics.Color = Neu.darkShadow
): Modifier = this.then(
    Modifier.drawBehind {
        val radius = cornerRadius.toPx()
        val blur = elevation.toPx()
        val offset = (blur * 0.55f)

        // ВАЖНО: рисуем rect слегка “внутрь”, чтобы тень не обрезалась краями canvas
        val inset = blur * 0.6f
        val left = inset
        val top = inset
        val right = size.width - inset
        val bottom = size.height - inset

        if (right <= left || bottom <= top) return@drawBehind

        drawIntoCanvas { canvas ->
            val paint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.FILL
                color = AColor.TRANSPARENT // ✅ рисуем только тень, без “грязной заливки”
            }

            // Тёмная тень (низ-право)
            paint.setShadowLayer(blur, offset, offset, darkShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)

            // Светлая тень (верх-лево)
            paint.setShadowLayer(blur, -offset, -offset, lightShadow.toArgb())
            canvas.nativeCanvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)

            paint.clearShadowLayer()
        }
    }
)
