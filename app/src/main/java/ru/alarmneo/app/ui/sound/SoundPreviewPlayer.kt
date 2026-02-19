package ru.alarmneo.app.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper

class SoundPreviewPlayer(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var focusRequest: AudioFocusRequest? = null
    private var player: MediaPlayer? = null

    private var currentKey: String? = null

    private enum class State { IDLE, PREPARING, PLAYING }
    private var state: State = State.IDLE

    // Токен, чтобы “устаревшие” onPrepared не могли стартануть после stop()
    private var playToken: Int = 0

    // авто-стоп
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null
    private val maxPreviewMs = 8000L

    private val attrs: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun toggle(key: String?) {
        val k = key?.trim().orEmpty()
        if (k.isEmpty() || k == AlarmSounds.NONE_ID) {
            stop()
            return
        }

        // Если тот же трек уже играет ИЛИ готовится — stop
        if (currentKey == k && (state == State.PREPARING || state == State.PLAYING)) {
            stop()
        } else {
            play(k)
        }
    }

    fun play(key: String?) {
        val k = key?.trim().orEmpty()
        if (k.isEmpty() || k == AlarmSounds.NONE_ID) {
            stop()
            return
        }

        // Инвалидируем все старые колбэки
        playToken++
        val token = playToken

        stopPlayerOnly()
        if (!requestFocus()) return

        val uri = resolveToUri(k) ?: run {
            abandonFocus()
            return
        }

        val mp = (player ?: MediaPlayer().also { player = it }).apply {
            reset()
            setAudioAttributes(attrs)

            setOnPreparedListener {
                if (token != playToken) return@setOnPreparedListener
                state = State.PLAYING
                it.start()
            }
            setOnCompletionListener {
                if (token == playToken) stop()
            }
            setOnErrorListener { _, _, _ ->
                if (token == playToken) stop()
                true
            }
        }

        currentKey = k
        state = State.PREPARING

        runCatching {
            mp.setDataSource(context, uri)
            mp.prepareAsync()

            stopRunnable = Runnable {
                if (token == playToken) stop()
            }
            handler.postDelayed(stopRunnable!!, maxPreviewMs)
        }.onFailure {
            if (token == playToken) stop()
        }
    }

    fun stop() {
        // Инвалидируем колбэки
        playToken++
        stopPlayerOnly()
        abandonFocus()
    }

    fun release() = stop()

    private fun stopPlayerOnly() {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null

        runCatching { player?.stop() }
        currentKey = null
        state = State.IDLE
    }

    private fun requestFocus(): Boolean {
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { focus ->
                if (focus <= 0) stop()
            }
            .build()

        focusRequest = req
        return audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun resolveToUri(key: String): Uri? {
        return when {
            key.startsWith("content://") || key.startsWith("file://") || key.startsWith("android.resource://") ->
                Uri.parse(key)
            else -> {
                val resId = context.resources.getIdentifier(key, "raw", context.packageName)
                if (resId != 0) Uri.parse("android.resource://${context.packageName}/$resId") else null
            }
        }
    }
}
