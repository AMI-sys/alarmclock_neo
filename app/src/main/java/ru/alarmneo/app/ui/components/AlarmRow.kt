package ru.alarmneo.app.ui.components

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.ui.theme.BlueMuted
import ru.alarmneo.app.ui.theme.BluePrimary
import java.util.Calendar
import java.util.Date

@Composable
fun AlarmRow(
    alarm: Alarm,
    onToggle: (Int, Boolean) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val timeText = remember(alarm.hour, alarm.minute, context) {
        formatTimeSystem(context, alarm.hour, alarm.minute)
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить будильник?") },
            text = { Text("$timeText • ${alarm.label}") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(alarm.id)
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отменить") }
            }
        )
    }

    val contentAlpha = if (alarm.enabled) 1f else 0.45f

    NeuCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onEdit(alarm.id) },
        cornerRadius = 18.dp,
        elevation = 10.dp,
        contentPadding = 16.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // LEFT — content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                // Time (главный акцент)
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.h3,
                    color = BluePrimary.copy(alpha = contentAlpha)
                )

                // Label (вторично)
                if (alarm.label.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.body2,
                        color = BlueMuted.copy(alpha = contentAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Repeat line (терциарно)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatDays(alarm.days),
                    style = MaterialTheme.typography.caption,
                    color = BlueMuted.copy(alpha = 0.85f * contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(10.dp))

                // Group chip
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GroupChip(text = alarm.groupName, enabledAlpha = contentAlpha)
                }
            }

            // RIGHT — controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                NewToggle(
                    checked = alarm.enabled,
                    onCheckedChange = { onToggle(alarm.id, it) }
                )

                Spacer(Modifier.height(8.dp))

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(
                            "⋯",
                            style = MaterialTheme.typography.h6,
                            color = BlueMuted.copy(alpha = 0.85f * contentAlpha)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            onEdit(alarm.id)
                        }) { Text("Редактировать") }

                        DropdownMenuItem(onClick = {
                            menuExpanded = false
                            confirmDelete = true
                        }) {
                            Text("Удалить", color = MaterialTheme.colors.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupChip(
    text: String,
    enabledAlpha: Float
) {
    // тёплый акцент, но очень мягко (чтобы не спорил с основным UI)
    val bg = AccentWarm.copy(alpha = 0.28f * enabledAlpha)
    val fg = BluePrimary.copy(alpha = 0.95f * enabledAlpha)

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text.ifBlank { "По умолчанию" },
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatTimeSystem(context: Context, h24: Int, m: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, h24)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val fmt = DateFormat.getTimeFormat(context) // уважает 12/24, AM/PM и локаль
    return fmt.format(Date(cal.timeInMillis))
}


private fun formatDays(days: Set<WeekDay>): String {
    if (days.isEmpty()) return "Единожды"

    val ordered = listOf(
        WeekDay.Mon, WeekDay.Tue, WeekDay.Wed, WeekDay.Thu,
        WeekDay.Fri, WeekDay.Sat, WeekDay.Sun
    ).filter { it in days }

    return ordered.joinToString(" ") { d ->
        when (d) {
            WeekDay.Mon -> "Mon"
            WeekDay.Tue -> "Tue"
            WeekDay.Wed -> "Wed"
            WeekDay.Thu -> "Thu"
            WeekDay.Fri -> "Fri"
            WeekDay.Sat -> "Sat"
            WeekDay.Sun -> "Sun"
        }
    }
}
