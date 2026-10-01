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

data class GameUiState(
    val game: GameState? = null,
    val levelIndex: Int = 0,
    val levelCount: Int = 0,
    val isLastLevel: Boolean = false,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val progress = ProgressStore(app)
    private val levels: List<Level> = LevelRepository(app).loadAll()

    private val _ui = MutableStateFlow(GameUiState(levelCount = levels.size))
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    init { if (levels.isNotEmpty()) loadLevel(progress.currentLevelIndex) }

    private fun loadLevel(index: Int) {
        val i = index.coerceIn(levels.indices)
        progress.currentLevelIndex = i
        _ui.value = GameUiState(
            game = GameEngine.start(Puzzle(levels[i])),
            levelIndex = i,
            levelCount = levels.size,
            isLastLevel = i == levels.lastIndex,
        )
    }

    private fun update(transform: (GameState) -> GameState) =
        _ui.value.game?.let { g -> _ui.value = _ui.value.copy(game = transform(g)) }

    fun onClueSelected(index: Int) = update { GameEngine.selectClue(it, index) }
    fun onKey(letter: Char) = update { GameEngine.typeLetter(it, letter) }
    fun onBackspace() = update { GameEngine.backspace(it) }
    fun onHint() = update { GameEngine.hint(it) }

    fun onNextLevel() {
        val s = _ui.value
        if (!s.isLastLevel) loadLevel(s.levelIndex + 1)
    }
}
