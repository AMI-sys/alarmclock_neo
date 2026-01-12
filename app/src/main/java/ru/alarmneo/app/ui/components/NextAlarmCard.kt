package ru.alarmneo.app.ui.components

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.alarmneo.app.alarm.NextAlarmInfo
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BluePrimary
import ru.alarmneo.app.ui.theme.Neu
import java.util.Calendar
import java.util.Date
import kotlin.math.max

@Composable
fun NextAlarmCard(
    info: NextAlarmInfo,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(info.triggerAtMillis) {
        while (isActive) {
            delay(30_000L)
            now = System.currentTimeMillis()
        }
    }

    val context = LocalContext.current
    val timeText = remember(info.alarm.hour, info.alarm.minute, context) {
        formatTimeSystem(context, info.alarm.hour, info.alarm.minute)
    }

    val delta = max(0L, info.triggerAtMillis - now)

    val accent = if (MaterialTheme.colors.isLight) BluePrimary else AccentWarm

    NeuCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        elevation = 12.dp,
        backgroundColor = Neu.bg,
        outlineWidth = 0.dp,
        contentPadding = 16.dp
    ) {
        Column {

            // тонкий акцент сверху — премиально и не кричит
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(99.dp))
                    .background(accent.copy(alpha = if (MaterialTheme.colors.isLight) 0.50f else 0.62f))
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Ближайший будильник",
                style = MaterialTheme.typography.caption,
                color = Neu.onBg.copy(alpha = 0.72f)
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.h3,
                    color = Neu.onBg.copy(alpha = 0.92f)
                )

                Text(
                    text = "Через ${formatDurationRu(delta)}",
                    style = MaterialTheme.typography.body2,
                    color = Neu.onBg.copy(alpha = 0.72f)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = info.alarm.label.ifBlank { "Будильник" },
                style = MaterialTheme.typography.body2,
                color = Neu.onBg.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTimeSystem(context: Context, h24: Int, m: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, h24)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val fmt = DateFormat.getTimeFormat(context)
    return fmt.format(Date(cal.timeInMillis))
}

private fun formatDurationRu(ms: Long): String {
    val totalMin = (ms / 60_000L).toInt()
    val h = totalMin / 60
    val m = totalMin % 60

    return when {
        h <= 0 -> "${m} мин"
        m == 0 -> "${h} ч"
        else -> "${h} ч ${m} мин"
    }
}
