package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BluePrimary
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuInset
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    val accent = if (MaterialTheme.colors.isLight) BluePrimary else AccentWarm
    val tint = accent.copy(alpha = if (MaterialTheme.colors.isLight) 0.10f else 0.12f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Neu.bg)
            .then(
                if (selected) Modifier.neuInset(cornerRadius = 999.dp, depth = 6.dp, tint = tint)
                else Modifier.neuShadow(cornerRadius = 999.dp, elevation = 3.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            color = if (selected) Neu.onBg.copy(alpha = 0.92f) else Neu.onBg.copy(alpha = 0.70f)
        )
    }
}
