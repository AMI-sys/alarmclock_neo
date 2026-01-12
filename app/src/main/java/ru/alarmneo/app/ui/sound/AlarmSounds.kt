package ru.alarmneo.app.ui.sound

import ru.alarmneo.app.R

object AlarmSounds {

    const val NONE_ID = "none"
    data class Sound(val id: String, val title: String, val resId: Int? = null)

    private val builtIn = listOf(
        Sound("china", "China", R.raw.china),
        Sound("american", "American", R.raw.american),
        Sound("iphone_apex", "iPhone Apex", R.raw.iphone_apex)
    )

    private val customSounds = mutableMapOf<String, String>()

    val all: List<Sound>
        get() = builtIn + customSounds.map { Sound(it.key, it.value, null) }

    private fun normalizeId(id: String): String {
        val trimmed = id.trim()
        return when (trimmed.lowercase()) {
            "default" -> "american"
            NONE_ID -> NONE_ID
            else -> trimmed
        }
    }

    fun byId(id: String): Sound? {
        val norm = normalizeId(id)
        if (norm == NONE_ID) return null
        return builtIn.find { it.id == norm }
            ?: customSounds[norm]?.let { Sound(norm, it, null) }
    }


    fun registerCustomSound(id: String, title: String) {
        customSounds[id] = title
    }
}


