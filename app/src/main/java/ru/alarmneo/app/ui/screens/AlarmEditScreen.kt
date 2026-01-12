package ru.alarmneo.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.NumberPicker
import android.widget.Toast
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import ru.alarmneo.app.ui.components.*
import ru.alarmneo.app.ui.sound.AlarmSounds
import ru.alarmneo.app.ui.vibration.VibrationPatterns
import ru.alarmneo.app.ui.theme.Neu
import kotlin.math.max
import kotlin.math.min

@Composable
fun AlarmEditScreen(
    initial: Alarm,
    groupSuggestions: List<String>,
    onCancel: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val is24h = remember { DateFormat.is24HourFormat(context) }

    var minute by rememberSaveable { mutableStateOf(initial.minute) }

// UI state for hour:
    var hour12 by rememberSaveable { mutableStateOf(to12Hour(initial.hour)) } // 1..12
    var isPm by rememberSaveable { mutableStateOf(initial.hour >= 12) }       // AM/PM

    var hour24 by rememberSaveable { mutableStateOf(initial.hour) }           // 0..23

    var label by rememberSaveable { mutableStateOf(initial.label) }
    var group by rememberSaveable { mutableStateOf(initial.groupName) }
    var days by rememberSaveable { mutableStateOf(initial.days.toSet()) }
    var soundId by rememberSaveable { mutableStateOf(initial.sound) }
    var snoozeMinutes by rememberSaveable { mutableStateOf(initial.snoozeMinutes) }
    var vibrate by rememberSaveable { mutableStateOf(initial.vibrate) }
    var vibrationPattern by rememberSaveable {
        mutableStateOf(initial.vibrationPattern ?: "pulse")
    }
    var showVibrationPicker by remember { mutableStateOf(false) }

    val selectedSound = remember(soundId) {
        when (soundId) {
            AlarmSounds.NONE_ID -> AlarmSounds.Sound(AlarmSounds.NONE_ID, "Без звука", null)
            else -> AlarmSounds.all.find { it.id == soundId }
                ?: AlarmSounds.Sound(soundId, "Пользовательский файл", null)
        }
    }


    val scrollState = rememberScrollState()

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var wasEdited by remember { mutableStateOf(false) }

    var showSoundPicker by remember { mutableStateOf(false) }
    val customSoundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) cursor.getString(nameIndex) else "Пользовательский файл"
            } ?: "Пользовательский файл"

            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            soundId = it.toString()
            AlarmSounds.registerCustomSound(soundId, name) // см. ниже
            wasEdited = true
        }
    }

    val isSoundOff = soundId == AlarmSounds.NONE_ID
    val mode = when {
        isSoundOff && vibrate -> "vibrate_only"
        !isSoundOff && vibrate -> "sound_and_vibrate"
        else -> "sound_only"
    }

    val saveHour = if (is24h) hour24 else to24Hour(hour12, isPm)

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Несохраненные изменения") },
            text = { Text("У вас есть несохраненные изменения. Выйти без сохранения?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onCancel()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }

    BackHandler {
        if (wasEdited) showUnsavedDialog = true else onCancel()
    }


    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                NeuTopBar(
                    title = "Будильник",
                    showSettings = false,
                    onNavigation = {
                        if (wasEdited) showUnsavedDialog = true else onCancel()
                    }
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                onDelete?.let {
                    NewButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        elevation = 8.dp,
                        backgroundColor = Neu.bg,
                        outlineColor = MaterialTheme.colors.error.copy(alpha = 0.55f),
                        onClick = onDelete
                    ) {
                        Text(
                            text = "Удалить",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.error.copy(alpha = 0.92f)
                        )
                    }
                }

                // Primary Save
                NewButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    elevation = 12.dp,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.18f),
                    outlineColor = MaterialTheme.colors.primary.copy(alpha = 0.55f),
                    enabled = wasEdited, // 🔥 становится активной только при изменениях

                    onClick = {
                        onSave(
                            initial.copy(
                                hour = saveHour,
                                minute = minute,
                                label = label.ifBlank { "Alarm" },
                                groupName = group.ifBlank { "Default" },
                                days = days,
                                sound = soundId,
                                snoozeMinutes = snoozeMinutes,
                                vibrate = vibrate,
                                vibrationPattern = vibrationPattern
                            )
                        )
                        wasEdited = false
                        Toast.makeText(context, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary.copy(alpha = 0.95f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Сохранить",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.primary.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }


    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {

                    // Крупный "превью" времени (чтобы всегда было понятно что получится)
                    val previewHour24 = if (is24h) hour24 else to24Hour(hour12, isPm)
                    Text(
                        text = formatTimeForPreview(context, previewHour24, minute),
                        style = MaterialTheme.typography.h4
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (is24h) {
                            NumberPickerField(hour24, 0..23) {
                                hour24 = it
                                wasEdited = true
                            }
                        } else {
                            NumberPickerField(hour12, 1..12) {
                                hour12 = it
                                wasEdited = true
                            }
                        }

                        NumberPickerField(minute, 0..59) {
                            minute = it
                            wasEdited = true
                        }
                    }

                    if (!is24h) {
                        Spacer(Modifier.height(12.dp))

                        // AM / PM — аккуратно, в стиле твоего SegmentedControl
                        NeuSegmentedControl(
                            leftText = "AM",
                            rightText = "PM",
                            selectedIndex = if (isPm) 1 else 0,
                            onSelect = { idx ->
                                isPm = (idx == 1)
                                wasEdited = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Повтор", style = MaterialTheme.typography.subtitle1)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeekDay.values().forEach { day ->
                            NeuChip(
                                text = day.short,
                                selected = days.contains(day),
                                onClick = {
                                    days = if (days.contains(day)) days - day else days + day
                                    wasEdited = true
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                SettingRowInput("Метка", label) {
                    label = it
                    wasEdited = true
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                SettingRowInput("Группа", group) {
                    group = it
                    wasEdited = true
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                SettingRowClickable("Мелодия", selectedSound.title) {
                    showSoundPicker = true
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Повтор сигнала: $snoozeMinutes мин", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NewButton(onClick = {
                            snoozeMinutes = max(1, snoozeMinutes - 1)
                            wasEdited = true
                        }) { Text("−") }

                        NewButton(onClick = {
                            snoozeMinutes = min(60, snoozeMinutes + 1)
                            wasEdited = true
                        }) { Text("+") }
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Вибрация", style = MaterialTheme.typography.body1)
                    NewToggle(checked = vibrate, onCheckedChange = {
                        vibrate = it
                        wasEdited = true
                    })
                }
            }

            if (vibrate) {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    SettingRowClickable(
                        title = "Vibration type",
                        value = VibrationPatterns.titleFor(vibrationPattern),
                        onClick = { showVibrationPicker = true }
                    )
                }
            }

            if (showSoundPicker) {
                AlertDialog(
                    onDismissRequest = { showSoundPicker = false },
                    title = { Text("Выберите мелодию") },
                    text = {
                        Column {
                            LazyColumn {
                                item {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                soundId = AlarmSounds.NONE_ID
                                                showSoundPicker = false
                                                wasEdited = true
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Без звука", Modifier.weight(1f))
                                        if (soundId == AlarmSounds.NONE_ID) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                }

                                items(AlarmSounds.all) { sound ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                soundId = sound.id
                                                showSoundPicker = false
                                                wasEdited = true
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sound.title, Modifier.weight(1f))
                                        if (sound.id == soundId) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                }

                                item {
                                    if (soundId != AlarmSounds.NONE_ID && AlarmSounds.all.none { it.id == soundId }) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showSoundPicker = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Пользовательский файл", Modifier.weight(1f))
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                }


                            }
                            Spacer(Modifier.height(8.dp))
                            NewButton(onClick = {
                                showSoundPicker = false
                                customSoundLauncher.launch(arrayOf("audio/*"))
                            }) {
                                Text("Выбрать свой файл")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }

            if (showVibrationPicker) {
                AlertDialog(
                    onDismissRequest = { showVibrationPicker = false },
                    title = { Text("Vibration type") },
                    text = {
                        Column {
                            VibrationPatterns.all.forEach { pattern ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            vibrationPattern = pattern.id
                                            showVibrationPicker = false
                                            wasEdited = true
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pattern.title, Modifier.weight(1f))
                                    if (pattern.id == vibrationPattern) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                )
            }

        }
    }
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
                setFormatter { it.toString().padStart(2, '0') }
                setOnValueChangedListener { _, _, new -> onValueChange(new) }
                this.value = value
            }
        },
        update = { picker ->
            if (picker.value != value) picker.value = value
        }
    )
}


@Composable
private fun SettingRowClickable(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 180.dp) // можно подправить
        ) {
            Text(
                text = value,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }

    }
}

@Composable
private fun SettingRowInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

private fun to12Hour(h24: Int): Int {
    val h = h24 % 12
    return if (h == 0) 12 else h
}

private fun to24Hour(h12: Int, pm: Boolean): Int {
    val normalized = h12 % 12 // 12 -> 0
    return if (pm) normalized + 12 else normalized
}

private fun formatTimeForPreview(context: android.content.Context, h24: Int, m: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, h24)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val fmt = DateFormat.getTimeFormat(context) // уважает 12/24 и локаль
    return fmt.format(Date(cal.timeInMillis))
}
