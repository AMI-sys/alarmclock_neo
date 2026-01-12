package ru.alarmneo.app.model

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val groupName: String,
    val enabled: Boolean,
    val days: Set<WeekDay> = emptySet(),


    val sound: String = "american",
    val snoozeMinutes: Int = 10,
    val vibrate: Boolean = true,
    val vibrationPattern: String = "pulse"
)
