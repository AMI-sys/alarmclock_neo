package ru.alarmneo.app.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import ru.alarmneo.app.R
import ru.alarmneo.app.ui.sound.AlarmSounds
import android.net.Uri


class AlarmForegroundService : Service() {

    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var audioFocusRequest: AudioFocusRequest? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        handler.post {
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // вернули фокус — продолжаем
                    player?.let { mp ->
                        if (!mp.isPlaying) {
                            runCatching { mp.start() }
                            // если мы уже дошли до полной громкости — не делаем fade заново
                            if (!reachedFullVolume) startFadeIn() else mp.setVolume(1f, 1f)
                        }
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // кратковременная потеря (звонок/ассистент) — пауза
                    player?.runCatching { if (isPlaying) pause() }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // потеря фокуса — останавливаем звук
                    stopRing()
                }
            }
        }
    }

    // громкость (постепенная)
    private val fadeDurationMs = 15_000L
    private val fadeStepMs = 100L
    private var fadeRunnable: Runnable? = null
    private var reachedFullVolume = false
    private val handler by lazy { android.os.Handler(android.os.Looper.getMainLooper()) }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        val vibPattern = intent.getStringExtra(AlarmActions.EXTRA_VIBRATION_PATTERN) ?: "pulse"

        when (action) {

            AlarmActions.ACTION_TRIGGER -> {
                val alarmId = intent.getIntExtra(AlarmActions.EXTRA_ALARM_ID, -1)
                val label = intent.getStringExtra(AlarmActions.EXTRA_LABEL) ?: "Alarm"
                val soundId = intent.getStringExtra(AlarmActions.EXTRA_SOUND_ID) ?: "american"
                val vibrate = intent.getBooleanExtra(AlarmActions.EXTRA_VIBRATE, true)
                val snoozeMin = intent.getIntExtra(AlarmActions.EXTRA_SNOOZE_MIN, 5)

                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                val km = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
                val wasInteractive = pm.isInteractive
                val useFullScreen = !wasInteractive || km.isKeyguardLocked

                val uiIntent = Intent(this, AlarmRingActivity::class.java).apply {
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

                val fullScreenPi = PendingIntent.getActivity(
                    this,
                    alarmId + 1_000_000,
                    uiIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                acquireWakeLock()

                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(
                        alarmId = alarmId,
                        label = label,
                        soundId = soundId,
                        vibrate = vibrate,
                        snoozeMin = snoozeMin,
                        vibPattern = vibPattern,
                        useFullScreen = useFullScreen,
                        fullScreenIntent = fullScreenPi
                    )
                )

                if (useFullScreen) {
                    runCatching {
                        startActivity(uiIntent)
                    }
                }

                startRing(soundId)
                if (vibrate) startVibration(vibPattern)
            }

            AlarmActions.ACTION_DISMISS -> {
                stopEverything()
                stopSelf()
            }

            AlarmActions.ACTION_SNOOZE -> {
                val alarmId = intent.getIntExtra(AlarmActions.EXTRA_ALARM_ID, -1)
                val label = intent.getStringExtra(AlarmActions.EXTRA_LABEL) ?: "Alarm"
                val soundId = intent.getStringExtra(AlarmActions.EXTRA_SOUND_ID) ?: "american"
                val vibrate = intent.getBooleanExtra(AlarmActions.EXTRA_VIBRATE, true)
                val snoozeMin = intent.getIntExtra(AlarmActions.EXTRA_SNOOZE_MIN, 5)

                stopEverything()
                AlarmScheduler.snooze(this, alarmId, label, soundId, vibrate, snoozeMin, vibPattern)
                stopSelf()
            }

            else -> {
                // неизвестное действие — игнорируем
            }
        }

        return START_NOT_STICKY
    }


    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }

    private fun buildNotification(
        alarmId: Int,
        label: String,
        soundId: String,
        vibrate: Boolean,
        vibPattern: String,
        snoozeMin: Int,
        useFullScreen: Boolean,
        fullScreenIntent: PendingIntent
    ): Notification {
        createChannels()

        val channelId = if (useFullScreen) CHANNEL_FS else CHANNEL_HIGH

        val dismissPi = PendingIntent.getService(
            this,
            1,
            Intent(this, AlarmForegroundService::class.java).apply {
                action = AlarmActions.ACTION_DISMISS
                putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozePi = PendingIntent.getService(
            this,
            2,
            Intent(this, AlarmForegroundService::class.java).apply {
                action = AlarmActions.ACTION_SNOOZE
                putExtra(AlarmActions.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmActions.EXTRA_LABEL, label)
                putExtra(AlarmActions.EXTRA_SOUND_ID, soundId)
                putExtra(AlarmActions.EXTRA_VIBRATE, vibrate)
                putExtra(AlarmActions.EXTRA_VIBRATION_PATTERN, vibPattern)
                putExtra(AlarmActions.EXTRA_SNOOZE_MIN, snoozeMin)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Будильник")
            .setContentText(label)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // ✅ критично для heads-up/fullscreen на многих OEM
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, useFullScreen) // ✅ ключ
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .apply {
                if (!useFullScreen) {
                    // экран включён → кнопки удобны
                    addAction(0, "Snooze", snoozePi)
                    addAction(0, "Dismiss", dismissPi)
                } else {
                    // экран выключен/локскрин → кнопки можно не показывать, но важно НЕ глушить priority
                    setDefaults(0)
                    setSound(null)
                }
            }
            .build()
    }


    private fun startRing(soundId: String) {
        stopRing()
        reachedFullVolume = false

        if (soundId.trim().equals(AlarmSounds.NONE_ID, ignoreCase = true)) {
            // звук отключён — ничего не запускаем
            return
        }

        val focusGranted = requestAlarmAudioFocus()
        android.util.Log.d("AlarmFG", "Audio focus granted=$focusGranted")

        val sound = AlarmSounds.byId(soundId) ?: return

        val mp = MediaPlayer()
        player = mp // <-- ВАЖНО: до startFadeIn()

        mp.apply {
            setAudioAttributes(alarmAudioAttributes())
            isLooping = true
            setVolume(0f, 0f)

            if (sound.resId != null) {
                val afd = resources.openRawResourceFd(sound.resId) ?: return
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                prepare()
                start()

                // важно: на некоторых прошивках громкость "слетает" после start()
                setVolume(0f, 0f)

                // небольшая задержка, чтобы гарантированно применилось
                handler.postDelayed({ startFadeIn() }, 150L)
            } else {
                val uri = Uri.parse(sound.id)
                setDataSource(applicationContext, uri)
                setOnPreparedListener { p ->
                    p.start()
                    p.setVolume(0f, 0f)
                    handler.postDelayed({ startFadeIn() }, 150L)
                }
                prepareAsync()
            }

            setOnErrorListener { _, _, _ ->
                handler.post {
                    stopRing()
                    startRing(soundId)
                }
                true
            }
        }
    }


    private fun stopRing() {
        cancelFade()
        reachedFullVolume = false

        player?.runCatching {
            setOnCompletionListener(null)
            setOnErrorListener(null)
            stop()
            release()
        }
        player = null

        abandonAlarmAudioFocus()
    }


    //private fun shouldUseFullScreen(): Boolean {
    //    val km = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
    //    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    //    val screenOff = !pm.isInteractive
    //    return screenOff || km.isKeyguardLocked
    //}


    private fun stopEverything() {
        stopRing()
        stopVibration()
        releaseWakeLock()
        stopForegroundCompat()
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun alarmAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

    private fun requestAlarmAudioFocus(): Boolean {
        val attrs = alarmAudioAttributes()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(focusListener)
                .setAcceptsDelayedFocusGain(false)
                .build()

            audioFocusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAlarmAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { req ->
                audioManager.abandonAudioFocusRequest(req)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }


    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        // 1) кратко будим экран (deprecated, но это стандартный практический путь для будильников)
        @Suppress("DEPRECATION")
        val screenLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "alarmneo:alarm_screen_wakelock"
        )
        screenLock.acquire(30_000L) // 30 сек достаточно, чтобы Activity поднялась

        // 2) и отдельно держим PARTIAL, чтобы CPU не уснул
        if (wakeLock?.isHeld == true) return
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "alarmneo:alarm_wakelock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }


    private fun releaseWakeLock() {
        wakeLock?.runCatching { if (isHeld) release() }
        wakeLock = null
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val high = NotificationChannel(
            CHANNEL_HIGH,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        // Fullscreen-канал: HIGH importance, но без звука/вибра от уведомления.
        // Звук/вибра делает сервис сам.
        val fs = NotificationChannel(
            CHANNEL_FS,
            "Alarm Fullscreen",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm fullscreen notifications"
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        nm.createNotificationChannel(high)
        nm.createNotificationChannel(fs)
    }



    private fun startFadeIn() {
        cancelFade()

        val mp = player ?: return
        mp.setVolume(0f, 0f)

        val steps = (fadeDurationMs / fadeStepMs).toInt().coerceAtLeast(1)
        var i = 0

        fadeRunnable = object : Runnable {
            override fun run() {
                val p = player
                if (p == null) {
                    cancelFade()
                    return
                }

                i++
                val vol = (i.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                p.setVolume(vol, vol)

                if (vol >= 1f) {
                    reachedFullVolume = true
                    player?.setVolume(1f, 1f) // фиксируем
                    cancelFade()
                } else {
                    handler.postDelayed(this, fadeStepMs)
                }
            }
        }

        handler.post(fadeRunnable!!)
    }

    private fun cancelFade() {
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
    }

    private fun vibrationWaveform(pattern: String): LongArray = when (pattern) {
        "short" -> longArrayOf(0, 200, 800) // repeat будет 0, но тут по сути одиночный
        "long" -> longArrayOf(0, 700, 800)
        "heartbeat" -> longArrayOf(0, 120, 120, 260, 900)
        "pulse" -> longArrayOf(0, 300, 300, 300, 700)
        "ramp" -> longArrayOf(0, 120, 250, 180, 220, 260, 180, 320, 160, 500)
        else -> longArrayOf(0, 300, 300, 300, 700)
    }

    private fun getVibrator(): Vibrator {
        vibrator?.let { return it }
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = v
        return v
    }

    private fun startVibration(pattern: String) {
        val v = getVibrator()
        if (!v.hasVibrator()) return

        val timings = vibrationWaveform(pattern)

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, 0)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, 0)
            }
        }
    }



    private fun stopVibration() {
        vibrator?.runCatching { cancel() }
    }


    companion object {
        private const val CHANNEL_HIGH = "alarm_channel_high_v3"
        private const val CHANNEL_FS = "alarm_channel_fullscreen_v3"
        const val NOTIFICATION_ID = 1001
    }
}
