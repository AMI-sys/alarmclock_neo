package ru.alarmneo.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BluePrimary
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.ui.theme.neuInset
import ru.alarmneo.app.ui.theme.neuShadow

@Composable
fun NewToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val width = 58.dp
    val height = 32.dp
    val pad = 4.dp
    val knob = 24.dp
    val radius = 999.dp
    val shape = RoundedCornerShape(radius)

    val knobOffset by animateDpAsState(
        targetValue = if (checked) width - knob - pad * 2 else 0.dp,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.86f),
        label = "toggleOffset"
    )

    val onTint = if (MaterialTheme.colors.isLight) {
        // Смешиваем молочный фон с синим → получается “поверхностный” tint
        androidx.compose.ui.graphics.lerp(Neu.bg, BluePrimary, 0.22f).copy(alpha = 0.55f)
    } else {
        // В тёмной теме оставляем тёплый, но тоже поверхностный
        androidx.compose.ui.graphics.lerp(Neu.bg, AccentWarm, 0.22f).copy(alpha = 0.50f)
    }

    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .background(Neu.bg)
            .then(
                if (checked) Modifier.neuInset(cornerRadius = radius, depth = 7.dp, tint = onTint)
                else Modifier.neuShadow(cornerRadius = radius, elevation = 6.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(pad)
    ) {
        // knob всегда raised — чисто и дорого
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(knob)
                .clip(CircleShape)
                .background(Neu.bg)
                .neuShadow(cornerRadius = radius, elevation = 5.dp)
        )
    }
}
