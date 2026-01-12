package ru.alarmneo.app.ui.components

import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay

@Composable
fun EditAlarmDialog(
    initial: Alarm,
    groupSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var hour by remember { mutableStateOf(initial.hour) }
    var minute by remember { mutableStateOf(initial.minute) }

    var label by remember { mutableStateOf(initial.label) }
    var group by remember { mutableStateOf(initial.groupName) }
    var days by remember { mutableStateOf(initial.days) }

    var groupMenuExpanded by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    if (showCreateGroup) {
        AlertDialog(
            onDismissRequest = { showCreateGroup = false },
            title = { Text("Новая группа") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Название группы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = newGroupName.trim()
                    if (cleaned.isNotEmpty()) group = cleaned
                    newGroupName = ""
                    showCreateGroup = false
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newGroupName = ""
                    showCreateGroup = false
                }) { Text("Отменить") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать") },
        text = {
            Column(Modifier.fillMaxWidth()) {

                Text("Время", style = MaterialTheme.typography.subtitle2)
                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NumberPickerField(
                        value = hour,
                        range = 0..23,
                        onValueChange = { hour = it }
                    )
                    NumberPickerField(
                        value = minute,
                        range = 0..59,
                        onValueChange = { minute = it }
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // GROUP DROPDOWN (+ Create new...)
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Групповой") },
                        trailingIcon = { Text("▾") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Простая кнопка справа для открытия меню (без сложных click-overlay)
                    TextButton(
                        onClick = { groupMenuExpanded = true },
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    ) {
                        Text("Выбрать")
                    }

                    DropdownMenu(
                        expanded = groupMenuExpanded,
                        onDismissRequest = { groupMenuExpanded = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            groupMenuExpanded = false
                            showCreateGroup = true
                        }) { Text("➕ Создать новый…") }

                        val unique = groupSuggestions
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .sortedBy { it.lowercase() }

                        unique.forEach { g ->
                            DropdownMenuItem(onClick = {
                                group = g
                                groupMenuExpanded = false
                            }) { Text(g) }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // DAYS (horizontal scroll, everything visible)
                Text("Days", style = MaterialTheme.typography.subtitle2)
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WeekDay.values().toList()) { day ->
                        val selected = days.contains(day)
                        val bg = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colors.onSurface.copy(alpha = 0.06f)

                        Card(backgroundColor = bg, elevation = 0.dp) {
                            TextButton(
                                onClick = {
                                    days = if (selected) days - day else days + day
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) { Text(day.short) }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (days.isEmpty()) "Каждый день" else "Выбрано: ${days.size}",
                    style = MaterialTheme.typography.caption
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleanedGroup = group.trim().ifEmpty { "По умолчанию" }
                val cleanedLabel = label.trim().ifEmpty { "Будильник" }

                onSave(
                    initial.copy(
                        hour = hour,
                        minute = minute,
                        label = cleanedLabel,
                        groupName = cleanedGroup,
                        days = days
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отменить") }
        }
    )
}

@Composable
private fun NumberPickerField(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                wrapSelectorWheel = true
                setFormatter { v -> v.toString().padStart(2, '0') }
                setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
                this.value = value
            }
        },
        update = { picker ->
            picker.minValue = range.first
            picker.maxValue = range.last
            if (picker.value != value) picker.value = value
        }
    )
}
