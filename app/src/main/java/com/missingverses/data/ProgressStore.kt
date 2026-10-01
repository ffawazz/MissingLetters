package com.missingverses.data

import android.content.Context

/** Remembers which level index the player is on. */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    var currentLevelIndex: Int
        get() = prefs.getInt(KEY_LEVEL, 0)
        set(value) = prefs.edit().putInt(KEY_LEVEL, value).apply()

    private companion object { const val KEY_LEVEL = "level_index" }
}
