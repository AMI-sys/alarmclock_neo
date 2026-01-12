package ru.alarmneo.app.data

import android.content.Context

enum class ThemeMode { System, Light, Dark }

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME, ThemeMode.System.name) ?: ThemeMode.System.name
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.System)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun getDefaultVibrate(): Boolean {
        return prefs.getBoolean(KEY_DEFAULT_VIBRATE, true)
    }

    fun setDefaultVibrate(value: Boolean) {
        prefs.edit().putBoolean(KEY_DEFAULT_VIBRATE, value).apply()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DEFAULT_VIBRATE = "default_vibrate"
    }
}
