package ru.alarmneo.app.ui.vibration

data class VibrationPattern(
    val id: String,
    val title: String
)

object VibrationPatterns {
    val all = listOf(
        VibrationPattern("pulse", "Pulse"),
        VibrationPattern("short", "Short"),
        VibrationPattern("long", "Long"),
        VibrationPattern("heartbeat", "Heartbeat"),
        VibrationPattern("ramp", "Ramp")
    )

    fun titleFor(id: String): String =
        all.firstOrNull { it.id == id }?.title ?: "Pulse"
}
