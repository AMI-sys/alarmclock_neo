package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NewButton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 10.dp,
    backgroundColor: Color = Neu.bg,
    outlineColor: Color = Neu.outline,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val alpha = if (enabled) 1f else 0.45f

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.65f)
            .then(if (enabled) Modifier.neuShadow(cornerRadius = cornerRadius, elevation = elevation) else Modifier)
            .clip(shape)
            .background(backgroundColor.copy(alpha = backgroundColor.alpha * alpha))
            .border(1.dp, outlineColor.copy(alpha = outlineColor.alpha * alpha), shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding)
    ) {
        content()
    }
}
