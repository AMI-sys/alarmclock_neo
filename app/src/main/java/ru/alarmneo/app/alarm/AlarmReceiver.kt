package ru.alarmneo.app.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import ru.alarmneo.app.data.AlarmStore

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val act = intent.action ?: return

        val alarmId = intent.getIntExtra(AlarmActions.EXTRA_ALARM_ID, -1)
        val label = intent.getStringExtra(AlarmActions.EXTRA_LABEL) ?: "Alarm"
        val soundId = intent.getStringExtra(AlarmActions.EXTRA_SOUND_ID) ?: "american"
        val vibrate = intent.getBooleanExtra(AlarmActions.EXTRA_VIBRATE, true)
        val snoozeMin = intent.getIntExtra(AlarmActions.EXTRA_SNOOZE_MIN, 5)
        val vibPattern = intent.getStringExtra(AlarmActions.EXTRA_VIBRATION_PATTERN) ?: "pulse"

        when (act) {
            AlarmActions.ACTION_TRIGGER -> {

                // Snooze-триггер помечаем наличием EXTRA_TRIGGER_AT
                val isSnoozeTrigger = intent.hasExtra(AlarmActions.EXTRA_TRIGGER_AT)

                // 1) Всегда запускаем foreground service (звук/вибра/уведомление)
                val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
                    this.action = AlarmActions.ACTION_TRIGGER
                    putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmActions.EXTRA_LABEL, label)
                    putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
                    putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
                    putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
                    putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
                }
                try {
                    context.startForegroundService(serviceIntent)
                } catch (t: Throwable) {
                    // Фолбэк: если сервис нельзя стартовать из фона на этой прошивке —
                    // поднимаем UI, и уже из Activity можно стартовать сервис безопаснее.
                    val uiIntent = Intent(context, AlarmRingActivity::class.java).apply {
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
                    context.startActivity(uiIntent)
                }


                // 2) Перепланируем "основной" будильник (но НЕ snooze)
                if (!isSnoozeTrigger && alarmId != -1) {
                    val stored = AlarmStore(context).getAlarms().firstOrNull { it.id == alarmId }
                    if (stored != null) {
                        AlarmScheduler.schedule(context, stored)
                    }
                }

                // 3) UI показываем только если экран выключен или локскрин
//                if (shouldLaunchUi(context)) {
//                    val uiIntent = Intent(context, AlarmRingActivity::class.java).apply {
//                        addFlags(
//                            Intent.FLAG_ACTIVITY_NEW_TASK or
//                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
//                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
//                        )
//                        putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
//                        putExtra(AlarmActions.EXTRA_LABEL, label)
//                        putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
//                        putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
//                        putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
//                        putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
//                    }
//                    context.startActivity(uiIntent)
//                }
            }

            AlarmActions.ACTION_DISMISS -> {
                val s = Intent(context, AlarmForegroundService::class.java).apply {
                    this.action = AlarmActions.ACTION_DISMISS
                    putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
                }
                context.startForegroundService(s)
            }

            AlarmActions.ACTION_SNOOZE -> {
                val s = Intent(context, AlarmForegroundService::class.java).apply {
                    this.action = AlarmActions.ACTION_SNOOZE
                    putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmActions.EXTRA_LABEL, label)
                    putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
                    putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
                    putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
                    putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
                }
                context.startForegroundService(s)
            }
        }
    }

    private fun shouldLaunchUi(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val screenOff = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            !pm.isInteractive
        } else {
            @Suppress("DEPRECATION")
            !pm.isScreenOn
        }

        return screenOff || km.isKeyguardLocked
    }
}
