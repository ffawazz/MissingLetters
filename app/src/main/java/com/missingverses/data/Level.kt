package com.missingverses.data

import kotlinx.serialization.Serializable

/** One level exactly as stored in assets/levels/level_NNN.json. */
@Serializable
data class Level(
    val id: Int,
    val poet: String,
    val poem: String? = null,
    val difficulty: Int = 1,
    /** The famous verse (may contain hamza, tashkeel and punctuation; only shown in full on completion). */
    val verse: String,
    /** Base letter -> number. Every letter of the verse and of all answers must be present. */
    val cipher: Map<String, Int>,
    val clues: List<Clue>,
)

@Serializable
data class Clue(val clue: String, val answer: String)
