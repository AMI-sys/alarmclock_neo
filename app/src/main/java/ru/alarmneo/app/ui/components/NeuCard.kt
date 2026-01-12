package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    contentPadding: Dp = 16.dp,
    backgroundColor: Color = Neu.bg,

    // обратно совместимо: старые вызовы с outlineColor не сломаются
    outlineColor: Color = Neu.outline,
    outlineWidth: Dp = 0.dp,

    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .neuShadow(
                cornerRadius = cornerRadius,
                elevation = elevation,
                outlineColor = outlineColor,
                outlineWidth = outlineWidth
            )
            .clip(shape)
            .background(backgroundColor)
            .padding(contentPadding)
    ) {
        content()
    }
}
