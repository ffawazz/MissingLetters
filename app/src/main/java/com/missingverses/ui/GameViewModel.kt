package com.missingverses.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.missingverses.data.Level
import com.missingverses.data.LevelRepository
import com.missingverses.data.ProgressStore
import com.missingverses.domain.GameEngine
import com.missingverses.domain.GameState
import com.missingverses.domain.Puzzle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Screen { MENU, LEVELS, GAME }

data class GameUiState(
    val screen: Screen = Screen.MENU,
    val game: GameState? = null,
    val levelIndex: Int = 0,
    val levelCount: Int = 0,
    val completed: Set<Int> = emptySet(),
) {
    val isLastLevel: Boolean get() = levelIndex == levelCount - 1
    /** A level is playable once the one before it has been completed. */
    fun isUnlocked(index: Int) = index == 0 || (index - 1) in completed
}

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val progress = ProgressStore(app)
    private val levels: List<Level> = LevelRepository(app).loadAll()

    private val _ui = MutableStateFlow(GameUiState(levelCount = levels.size, completed = progress.completed))
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    // ---- navigation ----
    fun openMenu() { _ui.value = _ui.value.copy(screen = Screen.MENU) }
    fun openLevels() { _ui.value = _ui.value.copy(screen = Screen.LEVELS) }

    /** "Play / Continue": first level not yet completed, or the level list once everything is done. */
    fun onPlay() {
        val next = levels.indices.firstOrNull { it !in _ui.value.completed }
        if (next != null) startLevel(next) else openLevels()
    }

    fun startLevel(index: Int) {
        if (index !in levels.indices || !_ui.value.isUnlocked(index)) return
        _ui.value = _ui.value.copy(
            screen = Screen.GAME,
            game = GameEngine.start(Puzzle(levels[index])),
            levelIndex = index,
        )
    }

    fun onNextLevel() { if (!_ui.value.isLastLevel) startLevel(_ui.value.levelIndex + 1) }

    // ---- gameplay ----
    private fun update(transform: (GameState) -> GameState) {
        val old = _ui.value.game ?: return
        val new = transform(old)
        var state = _ui.value.copy(game = new)
        if (new.isComplete && !old.isComplete) {
            progress.markCompleted(state.levelIndex)
            state = state.copy(completed = progress.completed)
        }
        _ui.value = state
    }

    fun onCell(number: Int, group: List<Int>) = update { GameEngine.select(it, number, group) }
    fun onKey(letter: Char) = update { GameEngine.guess(it, letter) }
    fun onHint() = update { GameEngine.hint(it) }
    fun onRestart() = update { GameEngine.restart(it) }
}
