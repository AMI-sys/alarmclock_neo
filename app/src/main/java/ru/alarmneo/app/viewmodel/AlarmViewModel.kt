package ru.alarmneo.app.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import ru.alarmneo.app.alarm.AlarmScheduler
import ru.alarmneo.app.data.AlarmStore
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.AlarmGroup
import ru.alarmneo.app.model.WeekDay

enum class Mode { Custom, Group }

class AlarmViewModel(app: Application) : AndroidViewModel(app) {

    private val store = AlarmStore(app)

    var mode by mutableStateOf(Mode.Custom)
        private set

    var alarms by mutableStateOf(loadInitial())
        private set

    var groups by mutableStateOf(buildGroups(alarms))
        private set

    var selectedDay by mutableStateOf<WeekDay?>(null)
        private set

    var selectedGroup by mutableStateOf<String?>(null)
        private set

    val visibleAlarms: List<Alarm>
        get() = applyFilters(alarms, selectedDay, selectedGroup)

    fun changeMode(newMode: Mode) {
        mode = newMode
    }

    /**
     * Включение/выключение одного будильника:
     * - enabled=true  -> schedule
     * - enabled=false -> cancel
     */
    fun toggleAlarm(alarmId: Int, enabled: Boolean) {
        val updated = alarms.map { a ->
            if (a.id == alarmId) a.copy(enabled = enabled) else a
        }
        setAndPersist(updated)

        val alarm = updated.firstOrNull { it.id == alarmId } ?: return

        if (enabled) {
            AlarmScheduler.schedule(getApplication(), alarm)
        } else {
            AlarmScheduler.cancel(getApplication(), alarm.id)
        }
    }

    /**
     * Включение/выключение всей группы:
     * - enabled=true  -> schedule для каждого
     * - enabled=false -> cancel для каждого
     */
    fun toggleGroup(groupId: Int, enabled: Boolean) {
        val groupName = groups.firstOrNull { it.id == groupId }?.name ?: return

        val updated = alarms.map { a ->
            if (a.groupName == groupName) a.copy(enabled = enabled) else a
        }
        setAndPersist(updated)

        val affected = updated.filter { it.groupName == groupName }
        if (enabled) {
            affected.forEach { AlarmScheduler.schedule(getApplication(), it) }
        } else {
            affected.forEach { AlarmScheduler.cancel(getApplication(), it.id) }
        }
    }

    fun setDayFilter(day: WeekDay?) {
        selectedDay = day
    }

    fun setGroupFilter(groupName: String?) {
        selectedGroup = groupName
    }

    fun resetFilters() {
        selectedDay = null
        selectedGroup = null
    }

    fun deleteAlarm(alarmId: Int) {
        AlarmScheduler.cancel(getApplication(), alarmId)
        val updated = alarms.filterNot { it.id == alarmId }
        setAndPersist(updated)
    }

    /**
     * Обновление будильника:
     * - если enabled=true -> schedule
     * - если enabled=false -> cancel
     */
    fun updateAlarm(updated: Alarm) {
        val fixed = updated.copy(groupName = normalizeGroupName(updated.groupName))
        val list = alarms.map { a -> if (a.id == fixed.id) fixed else a }
        setAndPersist(list)

        if (fixed.enabled) {
            AlarmScheduler.schedule(getApplication(), fixed)
        } else {
            AlarmScheduler.cancel(getApplication(), fixed.id)
        }
    }

    /**
     * Создание будильника:
     * - если enabled=true -> schedule
     * - если enabled=false -> просто сохраняем
     */
    fun createAlarm(newAlarm: Alarm) {
        val fixed = newAlarm.copy(
            id = nextId(),
            groupName = normalizeGroupName(newAlarm.groupName)
        )
        val list = alarms + fixed
        setAndPersist(list)

        if (fixed.enabled) {
            AlarmScheduler.schedule(getApplication(), fixed)
        }
    }

    private fun setAndPersist(list: List<Alarm>) {
        alarms = list
        store.setAlarms(list)
        recalcGroupsAndFixFilters()
    }

    private fun loadInitial(): List<Alarm> {
        val stored = store.getAlarms()
        if (stored.isNotEmpty()) {
            // На всякий случай при старте пересоздадим расписание только для enabled=true
            stored.filter { it.enabled }.forEach { AlarmScheduler.schedule(getApplication(), it) }
            return stored
        }

        // Первый запуск: НЕ создаём никаких заглушек
        store.setAlarms(emptyList())
        return emptyList()
    }

    private fun recalcGroupsAndFixFilters() {
        groups = buildGroups(alarms)
        val existing = groups.map { it.name }.toSet()
        if (selectedGroup != null && selectedGroup !in existing) {
            selectedGroup = null
        }
    }

    private fun nextId(): Int = (alarms.maxOfOrNull { it.id } ?: 0) + 1

    private fun normalizeGroupName(raw: String): String {
        val cleaned = raw.trim()
        return if (cleaned.isEmpty()) "Default" else cleaned
    }
}

private fun buildGroups(alarms: List<Alarm>): List<AlarmGroup> {
    val grouped = alarms.groupBy { it.groupName }
    return grouped.entries
        .sortedBy { it.key.lowercase() }
        .mapIndexed { index, (name, list) ->
            val enabledCount = list.count { it.enabled }
            AlarmGroup(
                id = index + 1,
                name = name,
                total = list.size,
                enabledCount = enabledCount
            )
        }
}

private fun applyFilters(
    alarms: List<Alarm>,
    day: WeekDay?,
    groupName: String?
): List<Alarm> {
    return alarms.filter { alarm ->
        val groupOk = groupName == null || alarm.groupName == groupName
        val dayOk = if (day == null) true else alarm.days.isEmpty() || alarm.days.contains(day)
        groupOk && dayOk
    }
}
