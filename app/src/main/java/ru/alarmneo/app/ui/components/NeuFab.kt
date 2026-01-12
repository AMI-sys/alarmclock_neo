package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BluePrimary
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    content: @Composable () -> Unit
) {
    val accent = if (MaterialTheme.colors.isLight) BluePrimary else AccentWarm
    val bg = accent.copy(alpha = if (MaterialTheme.colors.isLight) 0.18f else 0.22f)
    val contentColor = accent.copy(alpha = 0.95f)

    Box(
        modifier = modifier
            .size(size)
            .neuShadow(cornerRadius = 999.dp, elevation = 12.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
