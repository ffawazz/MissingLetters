package com.missingverses.domain

import com.missingverses.data.ArabicAlphabet
import com.missingverses.data.Level

/** Mirror of tools/validate_levels.py so bad levels are rejected at load time and in unit tests. */
object LevelValidator {
    fun validate(level: Level): List<String> {
        val errors = mutableListOf<String>()
        val verse = ArabicAlphabet.letters(level.verse).toSet()
        val answers = mutableSetOf<Char>()
        val needed = mutableSetOf<Char>()
        needed += verse
        level.clues.forEach { c ->
            if (c.answer.any { ArabicAlphabet.baseLetter(it) != it }) {
                errors += "answer '${c.answer}' must contain only letters (write أ إ آ as ا)"
            }
            answers += ArabicAlphabet.letters(c.answer)
            needed += ArabicAlphabet.letters(c.answer)
        }
        if (level.cipher.keys.any { it.length != 1 }) errors += "cipher keys must be single letters"
        if (level.cipher.values.toSet().size != level.cipher.size) errors += "cipher numbers are not unique"
        val keys = level.cipher.keys.mapNotNull { it.firstOrNull() }.toSet()
        if (keys != needed) errors += "cipher mismatch: missing=${needed - keys} extra=${keys - needed}"
        // Every letter of the verse must be findable inside at least one clue answer.
        if ((verse - answers).isNotEmpty()) errors += "verse letters missing from all clue answers: ${verse - answers}"
        return errors
    }
}
