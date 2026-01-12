package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .then(if (selected) Modifier.neuShadow(cornerRadius = 999.dp, elevation = 6.dp) else Modifier)
            .clip(shape)
            .background(Neu.bg)
            .border(1.dp, Neu.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            color = if (selected) Neu.onBg.copy(alpha = 0.90f) else Neu.onBg.copy(alpha = 0.60f)
        )
    }
}
