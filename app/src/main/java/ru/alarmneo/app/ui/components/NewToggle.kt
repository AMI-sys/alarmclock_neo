package ru.alarmneo.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuShadow

/**
 * Neu toggle v2:
 * - без текста (как в референсах)
 * - трек почти плоский + тонкая рамка
 * - ползунок выпуклый (neuShadow)
 * - ON даёт лёгкий акцент на треке
 */
@Composable
fun NewToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)

    // положение ползунка
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 2.dp,
        label = "knobOffset"
    )

    val accent = MaterialTheme.colors.primary.copy(alpha = 0.18f)
    val trackFill = if (checked) accent else Color.Black.copy(alpha = 0.02f)

    Box(
        modifier = modifier
            .size(width = 62.dp, height = 32.dp)
            .clip(shape)
            // трек НЕ делаем выпуклым, иначе будет “двойной объект”
            .background(Neu.bg)
            .border(1.dp, Neu.outline, shape)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        // лёгкая заливка трека (состояние ON читается без текста)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(trackFill)
        )

        // Ползунок
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(28.dp)
                .align(Alignment.CenterStart)
                .neuShadow(cornerRadius = 999.dp, elevation = 7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Neu.bg)
        )
    }
}
