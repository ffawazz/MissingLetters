package com.missingverses.domain

import com.missingverses.data.ArabicAlphabet
import com.missingverses.data.Level

/** A single hidden letter of the verse, identified by its cipher number. */
data class VerseCell(val number: Int, val letter: Char)

/** Level data pre-processed for fast lookup: number <-> letter mapping, verse cells, clue cells. */
class Puzzle(val level: Level) {
    val numberOf: Map<Char, Int> = level.cipher.entries.associate { it.key.first() to it.value }
    val letterOf: Map<Int, Char> = numberOf.entries.associate { it.value to it.key }

    /** Verse split into words; each word is a list of cells (punctuation/diacritics dropped). */
    val verseWords: List<List<VerseCell>> = level.verse.split(Regex("\\s+"))
        .map { word -> ArabicAlphabet.letters(word).map { VerseCell(numberOf.getValue(it), it) } }
        .filter { it.isNotEmpty() }

    val verseNumbers: Set<Int> = verseWords.flatten().map { it.number }.toSet()

    /** For each clue, the cipher numbers of its answer letters (one per slot). */
    val clueNumbers: List<List<Int>> = level.clues.map { c ->
        ArabicAlphabet.letters(c.answer).map { numberOf.getValue(it) }
    }
}
