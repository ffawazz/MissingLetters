package com.missingverses.data

/** The 28 base (isolated-form) Arabic letters used by the keyboard and by the cipher. */
object ArabicAlphabet {
    /** Keyboard layout, 4 rows x 7 keys, in abjadi-hija'i order (rendered right-to-left). */
    val keyboardRows: List<List<Char>> = listOf(
        "ابتثجحخ", "دذرزسشص", "ضطظعغفق", "كلمنهوي"
    ).map { it.toList() }

    private val keyboardSet: Set<Char> = keyboardRows.flatten().toSet()

    /**
     * Folds any presentation/hamza/ending variant to its base letter, or returns null for
     * anything that is not a letter (spaces, punctuation, tashkeel, tatweel).
     *  أ إ آ ٱ -> ا   |   ى ئ -> ي   |   ؤ -> و   |   ة -> ه
     */
    fun baseLetter(c: Char): Char? {
        val folded = when (c) {
            'أ', 'إ', 'آ', 'ٱ' -> 'ا'
            'ى', 'ئ' -> 'ي'
            'ؤ' -> 'و'
            'ة' -> 'ه'
            else -> c
        }
        return folded.takeIf { it in keyboardSet }
    }

    fun letters(text: String): List<Char> = text.mapNotNull(::baseLetter)
}
