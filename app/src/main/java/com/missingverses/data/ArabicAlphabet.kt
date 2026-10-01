package com.missingverses.data

/**
 * Arabic letters used by the keyboard and the cipher.
 *
 * Letters that look alike but are different letters stay DIFFERENT here:
 * ي ى ء ه ة ئ و ؤ each have their own key and their own cipher number.
 * The only folding is the hamza-on-alef family (أ إ آ ٱ -> ا); change [FOLDED] to alter that.
 */
object ArabicAlphabet {
    /** Standard Arabic keyboard layout, listed left-to-right exactly like a normal keyboard. */
    val keyboardRows: List<List<Char>> = listOf(
        "ضصثقفغعهخحجد",
        "شسيبلاتنمكط",
        "ذئءؤرىةوزظ",
    ).map { it.toList() }

    val letters: Set<Char> = keyboardRows.flatten().toSet()

    private val FOLDED = mapOf('أ' to 'ا', 'إ' to 'ا', 'آ' to 'ا', 'ٱ' to 'ا')

    /** The cipher letter for [c], or null for anything that is not a letter (space, punctuation, tashkeel, tatweel). */
    fun baseLetter(c: Char): Char? = (FOLDED[c] ?: c).takeIf { it in letters }

    fun letters(text: String): List<Char> = text.mapNotNull(::baseLetter)
}
