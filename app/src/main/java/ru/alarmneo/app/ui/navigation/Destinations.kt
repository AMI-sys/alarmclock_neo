package ru.alarmneo.app.ui.navigation

sealed class Dest(val route: String, val title: String) {
    data object Alarm : Dest("alarm", "Будильник")
    data object WorldTime : Dest("world_time", "Мировое время")
    data object Timer : Dest("timer", "Таймер")
    data object Stopwatch : Dest("stopwatch", "Секундомер")
}
