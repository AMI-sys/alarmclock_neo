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

    var onStateChanged: (() -> Unit)? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var focusRequest: AudioFocusRequest? = null
    private var player: MediaPlayer? = null

    private var currentKey: String? = null

    private enum class State { IDLE, PREPARING, PLAYING }
    private var state: State = State.IDLE

    private var playToken: Int = 0
    private var pendingToken: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null
    private val maxPreviewMs = 8000L

    private val attrs: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun toggle(key: String?) {
        val k = key?.trim().orEmpty()
        if (k.isEmpty() || k == AlarmSounds.NONE_ID) {
            stop()
            return
        }
        if (currentKey == k && (state == State.PREPARING || state == State.PLAYING)) stop() else play(k)
    }

    fun play(key: String?) {
        val k = key?.trim().orEmpty()
        if (k.isEmpty() || k == AlarmSounds.NONE_ID) {
            stop()
            return
        }

        playToken++
        val token = playToken
        pendingToken = token

        stopPlayerOnly()
        if (!requestFocus()) return

        val mp = ensurePlayer()

        currentKey = k
        state = State.PREPARING
        onStateChanged?.invoke()

        val builtInResId = AlarmSounds.byId(k)?.resId

        runCatching {
            if (builtInResId != null) {
                val afd = context.resources.openRawResourceFd(builtInResId)
                    ?: error("openRawResourceFd returned null for $builtInResId")
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                val uri = resolveToUri(k) ?: run {
                    abandonFocus()
                    stopPlayerOnly()
                    return
                }
                mp.setDataSource(context, uri)
            }

            mp.prepareAsync()

            stopRunnable = Runnable {
                if (token == playToken) stop()
            }
            handler.postDelayed(stopRunnable!!, maxPreviewMs)
        }.onFailure {
            if (token == playToken) stop()
        }
    }

    fun isPlaying(key: String?): Boolean {
        val k = key?.trim().orEmpty()
        return currentKey == k && state == State.PLAYING
    }

    fun stop() {
        playToken++
        stopPlayerOnly()
        abandonFocus()
        onStateChanged?.invoke()
    }

    fun release() {
        stop()
        runCatching { player?.release() }
        player = null
    }

    private fun stopPlayerOnly() {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null

        runCatching { player?.reset() }

        currentKey = null
        state = State.IDLE
        onStateChanged?.invoke()
    }

    private fun ensurePlayer(): MediaPlayer {
        val existing = player
        if (existing != null) return existing

        val mp = MediaPlayer().apply {
            setAudioAttributes(attrs)

            setOnPreparedListener {
                // ✅ токен актуальный, не “первый”
                if (pendingToken != playToken) return@setOnPreparedListener
                runCatching {
                    start()
                    state = State.PLAYING
                    onStateChanged?.invoke()
                }.onFailure { stop() }
            }

            setOnCompletionListener { stop() }
            setOnErrorListener { _, _, _ -> stop(); true }
        }

        player = mp
        return mp
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
        if (key.startsWith("content://") || key.startsWith("file://") || key.startsWith("android.resource://")) {
            return Uri.parse(key)
        }

        val normalized = key.trim().lowercase()
            .removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".ogg")
            .replace(' ', '_').replace('-', '_')

        val resId = context.resources.getIdentifier(normalized, "raw", context.packageName)
        return if (resId != 0) Uri.parse("android.resource://${context.packageName}/$resId") else null
    }
}