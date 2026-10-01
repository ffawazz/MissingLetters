package com.missingverses

import com.missingverses.data.Level
import com.missingverses.domain.Feedback
import com.missingverses.domain.GameEngine
import com.missingverses.domain.LevelValidator
import com.missingverses.domain.Puzzle
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GameEngineTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val levelFiles = File("src/main/assets/levels").listFiles { f -> f.name.startsWith("level_") }!!.sortedBy { it.name }

    private fun levels() = levelFiles.map { json.decodeFromString<Level>(it.readText()) }

    @Test fun allShippedLevelsAreValid() {
        assertTrue(levelFiles.isNotEmpty())
        levels().forEach { assertEquals("level ${it.id}", emptyList<String>(), LevelValidator.validate(it)) }
    }

    @Test fun correctAnswerRevealsLettersInVerse() {
        val puzzle = Puzzle(levels().first())
        var s = GameEngine.start(puzzle)
        "قمر".forEach { s = GameEngine.typeLetter(s, it) }
        assertEquals(Feedback.CORRECT, s.feedback)
        assertTrue(0 in s.solved)
        assertEquals('ق', s.verseLetter(puzzle.numberOf.getValue('ق')))
    }

    @Test fun wrongAnswerIsRejectedAndCleared() {
        var s = GameEngine.start(Puzzle(levels().first()))
        "قمل".forEach { s = GameEngine.typeLetter(s, it) }
        assertEquals(Feedback.WRONG, s.feedback)
        assertEquals(1, s.mistakes)
        assertTrue(s.revealed.isEmpty())
        assertNull(s.drafts[0])
    }

    @Test fun solvingAllCluesCompletesLevel() {
        levels().forEach { level ->
            var s = GameEngine.start(Puzzle(level))
            level.clues.forEachIndexed { i, c ->
                s = GameEngine.selectClue(s, i)
                if (i !in s.solved) c.answer.forEach { s = GameEngine.typeLetter(s, it) }
            }
            assertTrue("level ${level.id}", s.isComplete)
            assertEquals(Feedback.LEVEL_COMPLETE, s.feedback)
        }
    }
}
