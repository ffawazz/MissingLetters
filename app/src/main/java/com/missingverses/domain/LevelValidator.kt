package com.missingverses.domain

import com.missingverses.data.ArabicAlphabet
import com.missingverses.data.Level

/** Mirror of tools/validate_levels.py so bad levels are rejected at load time and in unit tests. */
object LevelValidator {
    fun validate(level: Level): List<String> {
        val errors = mutableListOf<String>()
        val needed = mutableSetOf<Char>()
        needed += ArabicAlphabet.letters(level.verse)
        level.clues.forEach { c ->
            if (c.answer.any { ArabicAlphabet.baseLetter(it) != it }) {
                errors += "answer '${c.answer}' must contain only letters (write أ إ آ as ا)"
            }
            needed += ArabicAlphabet.letters(c.answer)
        }
        if (level.cipher.keys.any { it.length != 1 }) errors += "cipher keys must be single letters"
        if (level.cipher.values.toSet().size != level.cipher.size) errors += "cipher numbers are not unique"
        val keys = level.cipher.keys.mapNotNull { it.firstOrNull() }.toSet()
        if (keys != needed) errors += "cipher mismatch: missing=${needed - keys} extra=${keys - needed}"
        return errors
    }
}
