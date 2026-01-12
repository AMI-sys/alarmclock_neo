package ru.alarmneo.app.model

data class AlarmGroup(
    val id: Int,
    val name: String,
    val total: Int,
    val enabledCount: Int
) {
    val state: GroupState
        get() = when {
            total == 0 -> GroupState.Empty
            enabledCount == 0 -> GroupState.AllOff
            enabledCount == total -> GroupState.AllOn
            else -> GroupState.Mixed
        }
}

enum class GroupState {
    Empty, AllOff, AllOn, Mixed
}
