package com.missingverses.domain

import com.missingverses.data.ArabicAlphabet

const val MAX_MISTAKES = 3

enum class Feedback { CORRECT, WRONG, LEVEL_COMPLETE, FAILED }

/** The cell the player tapped. [group] is its word/clue (used to jump to the next empty cell after a correct guess). */
data class Selection(val number: Int, val group: List<Int>)

/**
 * Immutable snapshot of a level in progress.
 *  - [revealed]      cipher numbers whose letter is known (shown everywhere that number appears)
 *  - [wrongGuesses]  letters already rejected for a number (re-trying them is free and ignored)
 *  - [lastWrong]     the most recent wrong guess, shown in red until the next action
 */
data class GameState(
    val puzzle: Puzzle,
    val revealed: Set<Int> = emptySet(),
    val selection: Selection? = null,
    val wrongGuesses: Map<Int, Set<Char>> = emptyMap(),
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val lastWrong: Pair<Int, Char>? = null,
    val feedback: Feedback? = null,
) {
    val isComplete: Boolean get() = revealed.containsAll(puzzle.allNumbers)
    val isFailed: Boolean get() = mistakes >= MAX_MISTAKES
    val isOver: Boolean get() = isComplete || isFailed

    /** The letter currently visible for a cipher number, or null while it is still hidden. */
    fun letterAt(number: Int): Char? = if (number in revealed) puzzle.letterOf[number] else null

    fun isClueSolved(clue: Int): Boolean = revealed.containsAll(puzzle.clueNumbers[clue])

    /** Keys are greyed out once their letter is found, or once rejected for the selected cell. */
    fun isKeyDisabled(letter: Char): Boolean {
        if (isOver) return true
        if (puzzle.numberOf[letter]?.let { it in revealed } == true) return true
        val sel = selection ?: return true
        return letter in wrongGuesses[sel.number].orEmpty()
    }
}

/** Pure game rules: no Android, no coroutines, trivially unit-testable. */
object GameEngine {
    fun start(puzzle: Puzzle) = GameState(puzzle)

    /** Select any hidden cell, in the verse or in a clue, in any order. */
    fun select(s: GameState, number: Int, group: List<Int>): GameState =
        if (s.isOver || number in s.revealed) s else s.copy(selection = Selection(number, group), lastWrong = null, feedback = null)

    /** Guesses [key] for the selected cell. A wrong guess costs one of the [MAX_MISTAKES] allowed mistakes. */
    fun guess(s: GameState, key: Char): GameState {
        if (s.isOver) return s
        val sel = s.selection ?: return s
        val letter = ArabicAlphabet.baseLetter(key) ?: return s
        if (s.isKeyDisabled(letter)) return s

        if (s.puzzle.letterOf[sel.number] == letter) return reveal(s, sel.number, Feedback.CORRECT)

        val mistakes = s.mistakes + 1
        return s.copy(
            mistakes = mistakes,
            wrongGuesses = s.wrongGuesses + (sel.number to (s.wrongGuesses[sel.number].orEmpty() + letter)),
            lastWrong = sel.number to letter,
            feedback = if (mistakes >= MAX_MISTAKES) Feedback.FAILED else Feedback.WRONG,
        )
    }

    /** Reveals the selected cell (or the first hidden one if nothing is selected). */
    fun hint(s: GameState): GameState {
        if (s.isOver) return s
        val target = s.selection?.number ?: s.puzzle.allNumbers.firstOrNull { it !in s.revealed } ?: return s
        return reveal(s.copy(hintsUsed = s.hintsUsed + 1), target, Feedback.CORRECT)
    }

    fun restart(s: GameState) = start(s.puzzle)

    private fun reveal(s: GameState, number: Int, ok: Feedback): GameState {
        val revealed = s.revealed + number
        val group = s.selection?.group.orEmpty()
        val from = group.indexOf(number)
        // Next hidden cell after this one inside the same word/clue (wrapping around), else nothing selected.
        val next = if (from < 0) null else (1..group.size).map { group[(from + it) % group.size] }.firstOrNull { it !in revealed }
        val done = revealed.containsAll(s.puzzle.allNumbers)
        return s.copy(
            revealed = revealed,
            selection = next?.let { Selection(it, group) },
            lastWrong = null,
            feedback = if (done) Feedback.LEVEL_COMPLETE else ok,
        )
    }
}
