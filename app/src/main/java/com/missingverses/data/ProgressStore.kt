package com.missingverses.data

import android.content.Context

/** Remembers which levels the player has completed. */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    val completed: Set<Int>
        get() = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()

    fun markCompleted(levelIndex: Int) {
        val all = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty() + levelIndex.toString()
        prefs.edit().putStringSet(KEY_COMPLETED, all).apply()
    }

    private companion object { const val KEY_COMPLETED = "completed_levels" }
}
