package ru.alarmneo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import ru.alarmneo.app.data.SettingsStore
import ru.alarmneo.app.data.ThemeMode
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import ru.alarmneo.app.ui.components.*
import ru.alarmneo.app.ui.theme.Neu
import ru.alarmneo.app.viewmodel.AlarmViewModel
import ru.alarmneo.app.viewmodel.Mode
import ru.alarmneo.app.alarm.NextAlarmUtils

private const val NEW_ALARM_ID = -1

@Composable
fun MainScreen(
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    vm: AlarmViewModel = viewModel()
) {
    var editingId by remember { mutableStateOf<Int?>(null) }
    var showSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val settingsStore = remember { SettingsStore(context) }

    var defaultVibrate by rememberSaveable {
        mutableStateOf(settingsStore.getDefaultVibrate())
    }

    val groupNames = vm.groups.map { it.name }

    if (showSettings) {
        SettingsScreen(
            onBack = { showSettings = false },
            vibrateByDefault = defaultVibrate,
            onVibrateByDefaultChanged = {
                defaultVibrate = it
                settingsStore.setDefaultVibrate(it)
            },
            themeMode = themeMode,
            onThemeModeChanged = onThemeModeChanged
        )
        return
    }

    if (editingId != null) {
        val initial = if (editingId == NEW_ALARM_ID) {
            defaultNewAlarm(groupNames, defaultVibrate)
        } else {
            vm.alarms.firstOrNull { it.id == editingId } ?: run {
                editingId = null
                return
            }
        }

        AlarmEditScreen(
            initial = initial,
            groupSuggestions = groupNames,
            onCancel = { editingId = null },
            onSave = { updated ->
                if (editingId == NEW_ALARM_ID) vm.createAlarm(updated) else vm.updateAlarm(updated)
                editingId = null
            }
        )
        return
    }

    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (vm.mode == Mode.Custom) {
                NeuFab(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp),
                    onClick = { editingId = NEW_ALARM_ID }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add alarm",
                        tint = Neu.onBg.copy(alpha = 0.9f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            NeuTopBar(
                title = "Будильник Neo",
                onNavigation = { showSettings = true },
                showSettings = true
            )

            NeuSegmentedControl(
                leftText = "Одиночный",
                rightText = "Групповой",
                selectedIndex = if (vm.mode == Mode.Group) 1 else 0,
                onSelect = { index ->
                    vm.changeMode(if (index == 1) Mode.Group else Mode.Custom)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )

            when (vm.mode) {
                Mode.Custom -> {
                    FiltersBar(
                        selectedDay = vm.selectedDay,
                        selectedGroup = vm.selectedGroup,
                        groups = groupNames,
                        onDayChanged = vm::setDayFilter,
                        onGroupChanged = vm::setGroupFilter,
                        onReset = vm::resetFilters
                    )

                    val nextInfo by remember {
                        derivedStateOf { NextAlarmUtils.findNext(vm.alarms) }
                    }


                    nextInfo?.let { info ->
                        NextAlarmCard(
                            info = info,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    val sorted = vm.visibleAlarms
                        .sortedWith(compareBy<Alarm>({ it.hour }, { it.minute }))
                    val selectedDay = vm.selectedDay
                    val everyday = sorted.filter { it.days.isEmpty() }

                    val dayList = if (selectedDay != null) {
                        sorted.filter { it.days.contains(selectedDay) }
                    } else emptyList()

                    val nothingToShow =
                        if (selectedDay == null) sorted.isEmpty()
                        else (everyday.isEmpty() && dayList.isEmpty())

                    if (nothingToShow) {
                        EmptyState(onClear = vm::resetFilters)
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            if (everyday.isNotEmpty()) {
                                item { SectionHeader(title = "Каждый день", count = everyday.size) }
                                items(everyday, key = { "every_${it.id}" }) { alarm ->
                                    AlarmRow(
                                        alarm = alarm,
                                        onToggle = vm::toggleAlarm,
                                        onEdit = { id -> editingId = id },
                                        onDelete = vm::deleteAlarm
                                    )
                                }
                            }

                            if (selectedDay != null) {
                                if (dayList.isNotEmpty()) {
                                    item { SectionHeader(title = selectedDay.short, count = dayList.size) }
                                    items(dayList, key = { "${selectedDay.name}_${it.id}" }) { alarm ->
                                        AlarmRow(
                                            alarm = alarm,
                                            onToggle = vm::toggleAlarm,
                                            onEdit = { id -> editingId = id },
                                            onDelete = vm::deleteAlarm
                                        )
                                    }
                                }
                            } else {
                                WeekDay.values().forEach { day ->
                                    val list = sorted.filter { it.days.contains(day) }
                                    if (list.isNotEmpty()) {
                                        item { SectionHeader(title = day.short, count = list.size) }
                                        items(list, key = { "${day.name}_${it.id}" }) { alarm ->
                                            AlarmRow(
                                                alarm = alarm,
                                                onToggle = vm::toggleAlarm,
                                                onEdit = { id -> editingId = id },
                                                onDelete = vm::deleteAlarm
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(96.dp)) }
                        }
                    }
                }

                Mode.Group -> {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        items(vm.groups, key = { it.id }) { group ->
                            GroupRow(group = group, onToggleGroup = vm::toggleGroup)
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

private fun defaultNewAlarm(groupSuggestions: List<String>, defaultVibrate: Boolean): Alarm {
    val group = groupSuggestions.firstOrNull() ?: "Default"
    return Alarm(
        id = NEW_ALARM_ID,
        hour = 7,
        minute = 0,
        label = "",
        groupName = group,
        enabled = true,
        days = emptySet(),
        sound = "american",
        snoozeMinutes = 10,
        vibrate = defaultVibrate
    )
}
