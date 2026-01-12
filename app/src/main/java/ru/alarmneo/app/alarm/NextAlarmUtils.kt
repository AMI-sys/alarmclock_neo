package ru.alarmneo.app.alarm

import ru.alarmneo.app.model.Alarm
import ru.alarmneo.app.model.WeekDay
import java.util.Calendar

data class NextAlarmInfo(
    val alarm: Alarm,
    val triggerAtMillis: Long
)

object NextAlarmUtils {

    fun findNext(alarms: List<Alarm>, nowMillis: Long = System.currentTimeMillis()): NextAlarmInfo? {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }

        var best: NextAlarmInfo? = null
        for (a in alarms) {
            if (!a.enabled) continue
            val t = computeNextTriggerMillis(a, now)
            if (best == null || t < best!!.triggerAtMillis) {
                best = NextAlarmInfo(a, t)
            }
        }
        return best
    }

    private fun computeNextTriggerMillis(alarm: Alarm, now: Calendar): Long {
        fun baseToday(): Calendar =
            Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
            }

        val days = alarm.days // Set<WeekDay>

        // Once
        if (days.isEmpty()) {
            val c = baseToday()
            if (c.timeInMillis <= now.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 1)
            return c.timeInMillis
        }

        // Repeating
        var best: Calendar? = null
        for (d in days) {
            val c = baseToday()
            val targetDow = toCalendarDow(d)
            val currentDow = c.get(Calendar.DAY_OF_WEEK)

            var addDays = (targetDow - currentDow + 7) % 7
            if (addDays == 0 && c.timeInMillis <= now.timeInMillis) addDays = 7
            c.add(Calendar.DAY_OF_YEAR, addDays)

            if (best == null || c.timeInMillis < best!!.timeInMillis) best = c
        }
        return best!!.timeInMillis
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
