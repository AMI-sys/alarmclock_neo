package ru.alarmneo.app.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Date
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import ru.alarmneo.app.ui.theme.BluePrimary
import ru.alarmneo.app.ui.theme.AccentWarm
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import ru.alarmneo.app.ui.components.*
import ru.alarmneo.app.ui.sound.AlarmSounds
import ru.alarmneo.app.ui.sound.SoundPreviewPlayer
import ru.alarmneo.app.ui.vibration.VibrationPatterns
import ru.alarmneo.app.ui.theme.Neu
import kotlin.math.max
import kotlin.math.min
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt



@Composable
fun AlarmEditScreen(
    initial: Alarm,
    groupSuggestions: List<String>,
    onCancel: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val previewPlayer = remember { SoundPreviewPlayer(context) }
    DisposableEffect(Unit) {
        onDispose { previewPlayer.release() }
    }

    var previewTick by remember { mutableIntStateOf(0) }

    DisposableEffect(previewPlayer) {
        previewPlayer.onStateChanged = { previewTick++ }
        onDispose { previewPlayer.onStateChanged = null }
    }

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
            else -> AlarmSounds.all.find { it.id == soundId } ?: AlarmSounds.Sound(
                soundId,
                "Пользовательский файл",
                null
            )
        }
    }


    val scrollState = rememberScrollState()

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var wasEdited by remember { mutableStateOf(false) }

    var showSoundPicker by remember { mutableStateOf(false) }
    val customSoundLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val name =
                    context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst()) cursor.getString(nameIndex) else "Пользовательский файл"
                    } ?: "Пользовательский файл"

                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                soundId = it.toString()
                previewPlayer.play(soundId)
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
            })
    }

    BackHandler {
        if (wasEdited) showUnsavedDialog = true else onCancel()
    }


    Scaffold(topBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            NeuTopBar(
                title = "Будильник", showSettings = false, onNavigation = {
                    if (wasEdited) showUnsavedDialog = true else onCancel()
                })
        }
    }, bottomBar = {
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
                }) {
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

            val isLightTheme = MaterialTheme.colors.isLight
            val timeCardTint = if (isLightTheme) {
                lerp(Neu.bg, BluePrimary, 0.08f) // light — едва заметный холодный
            } else {
                lerp(Neu.bg, AccentWarm, 0.16f)  // dark — заметный тёплый блок
            }

            NeuCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevation = 14.dp,
                backgroundColor = timeCardTint,
                outlineWidth = 0.dp,
                contentPadding = 16.dp
            ) {
                Column(Modifier.padding(16.dp)) {

                    // “ванночка” под пикеры: визуально отделяет, но не ломает стиль
                    val wellBg = if (isLightTheme) {
                        MaterialTheme.colors.surface.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colors.surface.copy(alpha = 0.28f)
                    }
                    val wellOutline = if (isLightTheme) {
                        BluePrimary.copy(alpha = 0.20f)
                    } else {
                        AccentWarm.copy(alpha = 0.40f)
                    }

                    NeuCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        elevation = 8.dp,
                        backgroundColor = wellBg,
                        outlineColor = wellOutline,
                        contentPadding = 12.dp
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(170.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (is24h) {
                                    WheelPicker(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(110.dp),
                                        value = hour24,
                                        range = 0..23,
                                        onValueChange = { hour24 = it; wasEdited = true })
                                } else {
                                    WheelPicker(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(110.dp),
                                        value = hour12,
                                        range = 1..12,
                                        onValueChange = { hour12 = it; wasEdited = true })
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(
                                        if (isLightTheme) BluePrimary.copy(alpha = 0.35f)
                                        else AccentWarm.copy(alpha = 0.55f)
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                WheelPicker(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(110.dp),
                                    value = minute,
                                    range = 0..59,
                                    onValueChange = { minute = it; wasEdited = true })
                            }
                        }

                    }

                    if (!is24h) {
                        Spacer(Modifier.height(12.dp))
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
                    Text(
                        "Повтор",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeekDay.values().forEach { day ->
                            NeuChip(
                                text = day.short, selected = days.contains(day), onClick = {
                                    days = if (days.contains(day)) days - day else days + day
                                    wasEdited = true
                                })
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                SettingRowInput(
                    label = "Метка", value = label, onValueChange = {
                        label = it
                        wasEdited = true
                    })
            }

            Spacer(Modifier.height(16.dp))

            NeuCard(modifier = Modifier.fillMaxWidth()) {
                SettingRowInput(
                    label = "Группа", value = group, onValueChange = {
                        group = it
                        wasEdited = true
                    })

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
                    Text(
                        "Повтор сигнала: $snoozeMinutes мин",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
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
                    Text(
                        "Вибрация",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    NewToggle(checked = vibrate, onCheckedChange = {
                        vibrate = it
                        wasEdited = true
                    })
                }
            }

            if (vibrate) {
                NeuCard(modifier = Modifier.fillMaxWidth()) {
                    SettingRowClickable(
                        title = "Паттерн вибрации",
                        value = VibrationPatterns.titleFor(vibrationPattern),
                        onClick = { showVibrationPicker = true })
                }
            }

            if (showSoundPicker) {
                var pendingId by remember(soundId) { mutableStateOf(soundId) }
                val previewTickLocal = previewTick

                AlertDialog(
                    onDismissRequest = {
                        previewPlayer.stop()
                        showSoundPicker = false
                    },
                    title = { Text("Выберите мелодию") },
                    text = {
                        Column {
                            LazyColumn {
                                item {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                pendingId = AlarmSounds.NONE_ID
                                                previewPlayer.stop()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(Modifier.width(48.dp)) // чтобы выровнять с IconButton слева

                                        Text("Без звука", Modifier.weight(1f))

                                        RadioButton(
                                            selected = pendingId == AlarmSounds.NONE_ID,
                                            onClick = {
                                                pendingId = AlarmSounds.NONE_ID
                                                previewPlayer.stop()
                                            }
                                        )
                                    }
                                }

                                items(AlarmSounds.all) { sound ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { pendingId = sound.id }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { previewPlayer.toggle(sound.id) }
                                        ) {
                                            Icon(
                                                imageVector = if (previewPlayer.isPlaying(sound.id)) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Прослушать"
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Text(sound.title, Modifier.weight(1f))

                                        RadioButton(
                                            selected = pendingId == sound.id,
                                            onClick = { pendingId = sound.id }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            NewButton(onClick = {
                                // открываем SAF‑пикер
                                customSoundLauncher.launch(arrayOf("audio/*"))
                            }) {
                                Text("Выбрать свой файл")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            // Назначаем только здесь
                            soundId = pendingId
                            wasEdited = true
                            previewPlayer.stop()
                            showSoundPicker = false
                        }) {
                            Text("Выбрать")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            previewPlayer.stop()
                            showSoundPicker = false
                        }) {
                            Text("Отмена")
                        }
                    }
                )
            }


            if (showVibrationPicker) {
                AlertDialog(
                    onDismissRequest = { showVibrationPicker = false },
                    title = { Text("Паттерн вибрации") },
                    text = {
                        Column {
                            VibrationPatterns.all.forEach { pattern ->
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        vibrationPattern = pattern.id
                                        showVibrationPicker = false
                                        wasEdited = true
                                    }
                                    .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
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
private fun SettingRowClickable(
    title: String, value: String, onClick: () -> Unit
) {
    val primary = MaterialTheme.colors.onSurface
    val secondary = MaterialTheme.colors.onSurface.copy(alpha = 0.72f)

    Row(Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title, color = primary, style = MaterialTheme.typography.body1
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 180.dp)
        ) {
            Text(
                text = value,
                color = secondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                style = MaterialTheme.typography.body2
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = secondary
            )
        }
    }
}


@Composable
private fun SettingRowInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    val primary = MaterialTheme.colors.onSurface
    val secondary = MaterialTheme.colors.onSurface.copy(alpha = 0.72f)
    val hint = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = hint) },
            placeholder = {
                if (placeholder.isNotBlank()) Text(placeholder, color = hint)
            },
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.body1.copy(color = primary),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = primary,
                cursorColor = MaterialTheme.colors.primary,
                focusedBorderColor = Neu.outline.copy(alpha = 0.55f),
                unfocusedBorderColor = Neu.outline.copy(alpha = 0.30f),
                focusedLabelColor = secondary,
                unfocusedLabelColor = hint,
                placeholderColor = hint,
                backgroundColor = Neu.bg
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun WheelPicker(
    value: Int,
    range: IntRange,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val isLight = MaterialTheme.colors.isLight
    val accent = if (isLight) BluePrimary else AccentWarm

    val normalColor =
        if (isLight) MaterialTheme.colors.onSurface.copy(alpha = 0.70f)
        else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f)

    val selectedColor =
        if (isLight) accent.copy(alpha = 0.95f)
        else androidx.compose.ui.graphics.Color.White

    val items = remember(range) { range.toList() }
    val visibleRows = 5
    val paddingRows = visibleRows / 2

    // стартовый индекс (первый элемент списка), чтобы value был в центре
    val startFirstIndex = remember(range.first, range.last, value) {
        val target = (value - range.first).coerceIn(0, items.lastIndex)
        (target - paddingRows).coerceAtLeast(0)
    }

    // listState пересоздаём только при смене диапазона
    androidx.compose.runtime.key(range.first, range.last) {
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = startFirstIndex)
        val fling = rememberSnapFlingBehavior(lazyListState = listState)

        var userInteracted by remember { mutableStateOf(false) }
        var lastEmittedValue by remember { mutableStateOf(value) }
        var isLaidOut by remember { mutableStateOf(false) }

        // ✅ блокируем передачу флинга/скролла родителю (чтобы не улетал весь экран)
        val blockParentScroll = remember {
            object : NestedScrollConnection {

                // ВАЖНО: ничего не съедаем ДО скролла — иначе LazyColumn не двигается
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset.Zero

                // Съедаем ОСТАТОК (то, что пошло бы в родителя)
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    // available.y — это остаток, который пытается пойти вверх по дереву
                    return Offset(0f, available.y)
                }

                // Аналогично для fling: пусть колесо обработает fling, а остаток не отдаём родителю
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    return Velocity(0f, available.y)
                }
            }
        }


        // ✅ когда список реально разложился
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.isNotEmpty() }
                .collect { ok -> if (ok) isLaidOut = true }
        }

        // фиксируем факт реального взаимодействия (только когда уже есть layout)
        LaunchedEffect(listState) {
            snapshotFlow { listState.isScrollInProgress }
                .collect { inProgress ->
                    if (isLaidOut && inProgress) userInteracted = true
                }
        }

        BoxWithConstraints(
            modifier = modifier.nestedScroll(blockParentScroll),
            contentAlignment = Alignment.Center
        ) {
            val rowH = maxHeight / visibleRows

            // центральное окно
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowH)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = if (isLight) 0.12f else 0.18f))
            )

            // ближайший к центру индекс (только когда реально есть элементы)
            val centerIndex: Int? by remember(listState, items) {
                derivedStateOf {
                    val layout = listState.layoutInfo
                    val visible = layout.visibleItemsInfo
                    if (visible.isEmpty()) return@derivedStateOf null

                    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
                    val closest = visible.minByOrNull { info ->
                        kotlin.math.abs((info.offset + info.size / 2) - viewportCenter)
                    }
                    closest?.index?.coerceIn(0, items.lastIndex)
                }
            }

            // ✅ синхронизируем внешний value -> позиция только ДО первого взаимодействия
            LaunchedEffect(value, isLaidOut) {
                if (!isLaidOut) return@LaunchedEffect
                if (userInteracted) return@LaunchedEffect

                val target = (value - range.first).coerceIn(0, items.lastIndex)
                val first = (target - paddingRows).coerceAtLeast(0)

                listState.scrollToItem(first)   // без анимации
                lastEmittedValue = value
            }

            // ✅ когда скролл остановился — эмитим, но только если:
            // 1) layout готов
            // 2) пользователь реально трогал колесо
            // 3) centerIndex известен
            LaunchedEffect(listState.isScrollInProgress, centerIndex, isLaidOut, userInteracted) {
                if (!isLaidOut || !userInteracted) return@LaunchedEffect
                if (listState.isScrollInProgress) return@LaunchedEffect

                val idx = centerIndex ?: return@LaunchedEffect
                val newValue = items[idx]

                if (newValue != value && newValue != lastEmittedValue) {
                    lastEmittedValue = newValue
                    onValueChange(newValue)
                }
            }

            LazyColumn(
                state = listState,
                flingBehavior = fling,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = rowH * paddingRows),
            ) {
                items(items.size) { idx ->
                    val v = items[idx]
                    val isSelected = (centerIndex != null && idx == centerIndex)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowH),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = v.toString().padStart(2, '0'),
                            color = if (isSelected) selectedColor else normalColor,
                            fontSize = if (isSelected) 26.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
