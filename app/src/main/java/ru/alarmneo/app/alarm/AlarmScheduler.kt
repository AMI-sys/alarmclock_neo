package ru.alarmneo.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, alarm: Alarm) {
        val triggerAt = computeNextTriggerMillis(alarm)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerPi = buildTriggerPendingIntent(
            context = context,
            alarmId = alarm.id,
            label = alarm.label,
            soundId = alarm.sound,
            vibrate = alarm.vibrate,
            snoozeMin = alarm.snoozeMinutes,
            vibPattern = alarm.vibrationPattern
        )

        // showIntent — то, что система покажет пользователю при тапе по "следующему будильнику"
        val showPi = buildShowPendingIntent(
            context = context,
            alarmId = alarm.id,
            label = alarm.label,
            soundId = alarm.sound,
            vibrate = alarm.vibrate,
            snoozeMin = alarm.snoozeMinutes,
            vibPattern = alarm.vibrationPattern
        )

        val info = AlarmManager.AlarmClockInfo(triggerAt, showPi)
        try {
            am.setAlarmClock(info, triggerPi)
        } catch (_: SecurityException) {
            // fallback для прошивок/разрешений
            setExactFallback(am, triggerAt, triggerPi)
        }
    }

    fun cancel(context: Context, alarmId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // В cancel мы не знаем параметры (sound/vibrate/pattern) — поэтому используем дефолты.
        // Важно: requestCode должен совпадать с тем, что использовался в schedule/snooze/show.

        val mainPi = buildTriggerPendingIntent(
            context = context,
            alarmId = alarmId,
            label = "",
            soundId = "",
            vibrate = false,
            snoozeMin = 5
            // vibPattern default = "pulse"
        )
        am.cancel(mainPi)

        val snoozePi = buildSnoozePendingIntent(
            context = context,
            alarmId = alarmId,
            label = "",
            soundId = "",
            vibrate = false,
            snoozeMin = 5,
            triggerAt = 0L
            // vibPattern default = "pulse"
        )
        am.cancel(snoozePi)

        val showPi = buildShowPendingIntent(
            context = context,
            alarmId = alarmId,
            label = "",
            soundId = "",
            vibrate = false,
            snoozeMin = 5
            // vibPattern default = "pulse"
        )
        am.cancel(showPi)
    }

    fun rescheduleAll(context: Context, alarms: List<Alarm>) {
        alarms.forEach { cancel(context, it.id) }
        alarms.forEach { schedule(context, it) }
    }

    fun snooze(
        context: Context,
        alarmId: Int,
        label: String,
        soundId: String,
        vibrate: Boolean,
        snoozeMin: Int,
        vibPattern: String = "pulse"
    ) {
        val triggerAt = System.currentTimeMillis() + snoozeMin * 60_000L

        val snoozePi = buildSnoozePendingIntent(
            context = context,
            alarmId = alarmId,
            label = label,
            soundId = soundId,
            vibrate = vibrate,
            snoozeMin = snoozeMin,
            triggerAt = triggerAt,
            vibPattern = vibPattern
        )

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, snoozePi)
    }

    private fun setExactFallback(am: AlarmManager, triggerAt: Long, pi: PendingIntent) {
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun buildTriggerPendingIntent(
        context: Context,
        alarmId: Int,
        label: String,
        soundId: String,
        vibrate: Boolean,
        snoozeMin: Int,
        vibPattern: String = "pulse"
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmActions.ACTION_TRIGGER
            putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmActions.EXTRA_LABEL, label)
            putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
            putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
            putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
        }

        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildShowPendingIntent(
        context: Context,
        alarmId: Int,
        label: String,
        soundId: String,
        vibrate: Boolean,
        snoozeMin: Int,
        vibPattern: String = "pulse"
    ): PendingIntent {
        val intent = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmActions.EXTRA_LABEL, label)
            putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
            putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
            putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
        }

        // отдельный requestCode, чтобы не конфликтовал с trigger broadcast
        val requestCode = alarmId + 2_000_000

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildSnoozePendingIntent(
        context: Context,
        alarmId: Int,
        label: String,
        soundId: String,
        vibrate: Boolean,
        snoozeMin: Int,
        triggerAt: Long,
        vibPattern: String = "pulse"
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmActions.ACTION_TRIGGER
            putExtra(AlarmActions.EXTRA_TRIGGER_AT, triggerAt)
            putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmActions.EXTRA_LABEL, label)
            putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
            putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
            putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
        }

        return PendingIntent.getBroadcast(
            context,
            alarmId * 10 + 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun computeNextTriggerMillis(alarm: Alarm): Long {
        val now = Calendar.getInstance()

        fun calendarAtToday(): Calendar =
            Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
            }

        val days = alarm.days
        if (days.isEmpty()) {
            val c = calendarAtToday()
            if (c.timeInMillis <= now.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 1)
            return c.timeInMillis
        }

        var best: Calendar? = null
        for (d in days) {
            val c = calendarAtToday()
            val targetDow = toCalendarDow(d)
            val currentDow = c.get(Calendar.DAY_OF_WEEK)

            var addDays = (targetDow - currentDow + 7) % 7
            if (addDays == 0 && c.timeInMillis <= now.timeInMillis) addDays = 7
            c.add(Calendar.DAY_OF_YEAR, addDays)

            if (best == null || c.timeInMillis < best!!.timeInMillis) best = c
        }

        return best?.timeInMillis ?: run {
            val c = calendarAtToday()
            if (c.timeInMillis <= now.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 1)
            c.timeInMillis
        }
    }

    private fun toCalendarDow(d: WeekDay): Int = when (d) {
        WeekDay.Mon -> Calendar.MONDAY
        WeekDay.Tue -> Calendar.TUESDAY
        WeekDay.Wed -> Calendar.WEDNESDAY
        WeekDay.Thu -> Calendar.THURSDAY
        WeekDay.Fri -> Calendar.FRIDAY
        WeekDay.Sat -> Calendar.SATURDAY
        WeekDay.Sun -> Calendar.SUNDAY
    }
}
