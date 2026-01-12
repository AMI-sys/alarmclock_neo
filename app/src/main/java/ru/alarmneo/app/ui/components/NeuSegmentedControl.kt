package ru.alarmneo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NeuSegmentedControl(
    leftText: String,
    rightText: String,
    selectedIndex: Int, // 0 = left, 1 = right
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(outerShape)
            .background(Neu.bg)
            .border(1.dp, Neu.outline, outerShape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Segment(
            text = leftText,
            selected = selectedIndex == 0,
            onClick = { onSelect(0) },
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(6.dp))

        Segment(
            text = rightText,
            selected = selectedIndex == 1,
            onClick = { onSelect(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Segment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (selected) {
                    // ✅ активный сегмент = выпуклый
                    Modifier.neuShadow(cornerRadius = 14.dp, elevation = 7.dp)
                } else {
                    // ✅ неактивный = без внешней тени (чтобы не было “двух объектов”)
                    Modifier
                }
            )
            .clip(shape)
            .background(
                if (selected) Neu.bg else Neu.bg
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body2,
            color = if (selected) Neu.onBg.copy(alpha = 0.92f) else Neu.onBg.copy(alpha = 0.55f)
        )
    }
}
