package com.missingverses.data

import android.content.Context
import android.util.Log
import com.missingverses.domain.LevelValidator
import kotlinx.serialization.json.Json

/**
 * Loads every assets/levels/level_*.json, sorted by id. Adding hundreds of levels only means
 * dropping more files into that folder (or swapping this class for a Room/remote source).
 */
class LevelRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadAll(): List<Level> =
        context.assets.list(LEVELS_DIR).orEmpty()
            .filter { it.startsWith("level_") && it.endsWith(".json") }
            .mapNotNull { name ->
                runCatching {
                    val text = context.assets.open("$LEVELS_DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }
                    json.decodeFromString<Level>(text)
                }.onFailure { Log.e(TAG, "Cannot parse $name", it) }.getOrNull()
            }
            .filter { level ->
                LevelValidator.validate(level).also { errs ->
                    if (errs.isNotEmpty()) Log.e(TAG, "Level ${level.id} invalid: $errs")
                }.isEmpty()
            }
            .sortedBy { it.id }

    private companion object {
        const val LEVELS_DIR = "levels"
        const val TAG = "LevelRepository"
    }
}
