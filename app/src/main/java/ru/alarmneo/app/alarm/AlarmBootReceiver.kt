package ru.alarmneo.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.alarmneo.app.data.AlarmStore

class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val alarms = AlarmStore(context).getAlarms()
                AlarmScheduler.rescheduleAll(context, alarms.filter { it.enabled })
            }
        }
    }
}
