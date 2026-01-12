package ru.alarmneo.app.alarm

object AlarmActions {
    const val ACTION_TRIGGER = "ru.alarmneo.app.alarm.ACTION_TRIGGER"
    const val ACTION_DISMISS = "ru.alarmneo.app.alarm.ACTION_DISMISS"
    const val ACTION_SNOOZE = "ru.alarmneo.app.alarm.ACTION_SNOOZE"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_LABEL = "extra_label"
    const val EXTRA_SOUND_ID = "extra_sound_id"
    const val EXTRA_VIBRATE = "extra_vibrate"
    const val EXTRA_SNOOZE_MIN = "extra_snooze_min"
    const val EXTRA_TRIGGER_AT = "extra_trigger_at" // для snooze one-shot

    const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"

}
