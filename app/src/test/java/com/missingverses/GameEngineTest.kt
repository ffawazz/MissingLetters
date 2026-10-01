package com.missingverses

import com.missingverses.data.ArabicAlphabet
import com.missingverses.data.Level
import com.missingverses.domain.Feedback
import com.missingverses.domain.GameEngine
import com.missingverses.domain.GameState
import com.missingverses.domain.LevelValidator
import com.missingverses.domain.MAX_MISTAKES
import com.missingverses.domain.Puzzle
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GameEngineTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val levelFiles = File("src/main/assets/levels").listFiles { f -> f.name.startsWith("level_") }!!.sortedBy { it.name }

    private fun levels() = levelFiles.map { json.decodeFromString<Level>(it.readText()) }
    private fun first() = Puzzle(levels().first())

    private fun pick(s: GameState, letter: Char) = GameEngine.select(s, s.puzzle.numberOf.getValue(letter), listOf(s.puzzle.numberOf.getValue(letter)))

    @Test fun lookAlikeLettersStayDistinct() {
        "يىءهةئوؤ".forEach { assertEquals(it, ArabicAlphabet.baseLetter(it)) }
        assertEquals('ا', ArabicAlphabet.baseLetter('أ'))
        assertEquals(33, ArabicAlphabet.letters.size)
    }

    @Test fun allShippedLevelsAreValid() {
        assertTrue(levelFiles.isNotEmpty())
        levels().forEach { assertEquals("level ${it.id}", emptyList<String>(), LevelValidator.validate(it)) }
    }

    @Test fun launchesWithTwentySequentialLevels() {
        assertEquals((1..20).toList(), levels().map { it.id })
    }

    @Test fun correctGuessRevealsLetterEverywhere() {
        var s = pick(GameEngine.start(first()), 'ق')
        s = GameEngine.guess(s, 'ق')
        assertEquals(Feedback.CORRECT, s.feedback)
        assertEquals('ق', s.letterAt(s.puzzle.numberOf.getValue('ق')))
        assertEquals(0, s.mistakes)
    }

    @Test fun anyCellCanBeAnsweredInAnyOrder() {
        // Answer the LAST letter of the first clue first, straight away.
        val p = first()
        val last = p.clueNumbers[0].last()
        var s = GameEngine.select(GameEngine.start(p), last, p.clueNumbers[0])
        s = GameEngine.guess(s, p.letterOf.getValue(last))
        assertTrue(last in s.revealed)
    }

    @Test fun wrongGuessCountsAndRepeatIsFree() {
        var s = pick(GameEngine.start(first()), 'ق')
        s = GameEngine.guess(s, 'م')
        assertEquals(1, s.mistakes)
        assertEquals(Feedback.WRONG, s.feedback)
        s = GameEngine.guess(s, 'م') // already rejected for this cell: ignored
        assertEquals(1, s.mistakes)
    }

    @Test fun threeMistakesFailsLevelAndLocksInput() {
        var s = pick(GameEngine.start(first()), 'ق')
        "مرد".forEach { s = GameEngine.guess(s, it) }
        assertEquals(MAX_MISTAKES, s.mistakes)
        assertTrue(s.isFailed)
        assertEquals(Feedback.FAILED, s.feedback)
        val after = GameEngine.guess(s, 'ق')
        assertFalse(s.puzzle.numberOf.getValue('ق') in after.revealed)
        assertEquals(0, GameEngine.restart(s).mistakes)
    }

    @Test fun revealingEveryNumberCompletesLevel() {
        levels().forEach { level ->
            val p = Puzzle(level)
            var s = GameEngine.start(p)
            p.allNumbers.forEach { n ->
                s = GameEngine.select(s, n, listOf(n))
                s = GameEngine.guess(s, p.letterOf.getValue(n))
            }
            assertTrue("level ${level.id}", s.isComplete)
            assertEquals(Feedback.LEVEL_COMPLETE, s.feedback)
            assertEquals(0, s.mistakes)
        }
    }
}
