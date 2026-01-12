package ru.alarmneo.app.ui.components

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.alarm.NextAlarmInfo
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BlueMuted
import ru.alarmneo.app.ui.theme.BluePrimary
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.Calendar
import java.util.Date
import kotlin.math.max


@Composable
fun NextAlarmCard(
    info: NextAlarmInfo,
    modifier: Modifier = Modifier
) {
    // обновляем "через сколько" автоматически
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(info.triggerAtMillis) {
        while (true) {
            delay(30_000L) // раз в 30 сек достаточно
            now = System.currentTimeMillis()
        }
    }

    val context = LocalContext.current
    val timeText = remember(info.alarm.hour, info.alarm.minute, context) {
        formatTimeSystem(context, info.alarm.hour, info.alarm.minute)
    }

    val delta = max(0L, info.triggerAtMillis - now)

    // Оранжевый акцент: мягкий фон + более заметный контур
    val bg = AccentWarm.copy(alpha = 0.34f)
    val outline = AccentWarm.copy(alpha = 0.62f)

    NeuCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        elevation = 12.dp,
        backgroundColor = bg,
        outlineColor = outline,
        contentPadding = 16.dp
    ) {
        Column {
            Text(
                text = "Ближайший будильник",
                style = MaterialTheme.typography.caption,
                color = BluePrimary
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.h3,
                    color = BluePrimary
                )

                Text(
                    text = "Через ${formatDurationRu(delta)}",
                    style = MaterialTheme.typography.body2,
                    color = BluePrimary
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = info.alarm.label.ifBlank { "Будильник" },
                style = MaterialTheme.typography.body2,
                color = BlueMuted,
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
