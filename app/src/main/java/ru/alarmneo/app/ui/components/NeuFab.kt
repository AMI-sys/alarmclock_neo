package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .neuShadow(cornerRadius = 999.dp, elevation = 10.dp)
            .clip(CircleShape)
            .background(Neu.bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
