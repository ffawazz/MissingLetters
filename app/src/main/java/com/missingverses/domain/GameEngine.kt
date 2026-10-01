package com.missingverses.domain

import com.missingverses.data.ArabicAlphabet

enum class Feedback { CORRECT, WRONG, LEVEL_COMPLETE }

/**
 * Immutable snapshot of a level in progress.
 *  - [revealed]  cipher numbers whose letter is known (shown in the verse AND in every clue slot using it)
 *  - [drafts]    clue index -> (slot position -> letter) typed by the player but not yet confirmed
 */
data class GameState(
    val puzzle: Puzzle,
    val revealed: Set<Int> = emptySet(),
    val solved: Set<Int> = emptySet(),
    val drafts: Map<Int, Map<Int, Char>> = emptyMap(),
    val activeClue: Int? = 0,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val feedback: Feedback? = null,
) {
    val isComplete: Boolean get() = revealed.containsAll(puzzle.verseNumbers)

    /** The letter currently visible for a verse number, or null while still hidden. */
    fun verseLetter(number: Int): Char? = if (number in revealed) puzzle.letterOf[number] else null

    /** The letter currently visible in slot [pos] of [clue]: a revealed letter wins over a draft. */
    fun slotLetter(clue: Int, pos: Int): Char? =
        verseLetter(puzzle.clueNumbers[clue][pos]) ?: drafts[clue]?.get(pos)
}

/** Pure game rules: no Android, no coroutines, trivially unit-testable. */
object GameEngine {
    fun start(puzzle: Puzzle) = GameState(puzzle, activeClue = firstUnsolved(puzzle, emptySet(), -1))

    fun selectClue(s: GameState, clue: Int): GameState =
        if (clue in s.puzzle.clueNumbers.indices && clue !in s.solved) s.copy(activeClue = clue, feedback = null) else s

    /** Types [key] into the first empty slot of the active clue; auto-checks once the word is full. */
    fun typeLetter(s: GameState, key: Char): GameState {
        val clue = s.activeClue ?: return s
        if (clue in s.solved) return s
        val letter = ArabicAlphabet.baseLetter(key) ?: return s
        val slots = s.puzzle.clueNumbers[clue].size
        val pos = (0 until slots).firstOrNull { s.slotLetter(clue, it) == null } ?: return s
        val next = s.copy(drafts = s.drafts + (clue to (s.drafts[clue].orEmpty() + (pos to letter))), feedback = null)
        return if ((0 until slots).all { next.slotLetter(clue, it) != null }) submit(next, clue) else next
    }

    fun backspace(s: GameState): GameState {
        val clue = s.activeClue ?: return s
        val draft = s.drafts[clue].orEmpty()
        val last = draft.keys.maxOrNull() ?: return s
        return s.copy(drafts = s.drafts + (clue to (draft - last)), feedback = null)
    }

    /** Reveals one number: prefers the active clue, otherwise any still-hidden verse number. */
    fun hint(s: GameState): GameState {
        val fromClue = s.activeClue?.let { c -> s.puzzle.clueNumbers[c].firstOrNull { it !in s.revealed } }
        val target = fromClue ?: s.puzzle.verseNumbers.firstOrNull { it !in s.revealed } ?: return s
        return applyReveal(s.copy(hintsUsed = s.hintsUsed + 1), setOf(target), Feedback.CORRECT)
    }

    private fun submit(s: GameState, clue: Int): GameState {
        val typed = s.puzzle.clueNumbers[clue].indices.map { s.slotLetter(clue, it) }
        val expected = ArabicAlphabet.letters(s.puzzle.level.clues[clue].answer)
        return if (typed == expected) {
            applyReveal(s, s.puzzle.clueNumbers[clue].toSet(), Feedback.CORRECT)
        } else {
            s.copy(drafts = s.drafts - clue, mistakes = s.mistakes + 1, feedback = Feedback.WRONG)
        }
    }

    /**
     * Reveals [numbers] everywhere: updates the verse, auto-solves any other clue whose letters are
     * now all known (the "vice versa" direction), and advances the active clue.
     */
    private fun applyReveal(s: GameState, numbers: Set<Int>, ok: Feedback): GameState {
        val revealed = s.revealed + numbers
        val solved = s.puzzle.clueNumbers.indices.filter { revealed.containsAll(s.puzzle.clueNumbers[it]) }.toSet()
        val active = s.activeClue?.takeIf { it !in solved } ?: firstUnsolved(s.puzzle, solved, s.activeClue ?: -1)
        val next = s.copy(
            revealed = revealed,
            solved = solved,
            drafts = s.drafts.filterKeys { it !in solved },
            activeClue = active,
        )
        return next.copy(feedback = if (next.isComplete) Feedback.LEVEL_COMPLETE else ok)
    }

    private fun firstUnsolved(p: Puzzle, solved: Set<Int>, after: Int): Int? {
        val all = p.clueNumbers.indices
        return all.firstOrNull { it > after && it !in solved } ?: all.firstOrNull { it !in solved }
    }
}
