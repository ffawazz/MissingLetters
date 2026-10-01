package com.missingverses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.missingverses.ui.GameScreen
import com.missingverses.ui.GameViewModel
import com.missingverses.ui.LevelSelectScreen
import com.missingverses.ui.MenuScreen
import com.missingverses.ui.MissingVersesTheme
import com.missingverses.ui.Screen

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MissingVersesTheme {
                val ui by vm.ui.collectAsState()
                // System back from a level or the level list returns to the menu; from the menu it exits as usual.
                BackHandler(enabled = ui.screen != Screen.MENU) { vm.openMenu() }
                when (ui.screen) {
                    Screen.MENU -> MenuScreen(ui, onPlay = vm::onPlay, onLevels = vm::openLevels, onExit = { finishAffinity() })
                    Screen.LEVELS -> LevelSelectScreen(ui, onLevel = vm::startLevel, onBack = vm::openMenu)
                    Screen.GAME -> GameScreen(
                        ui = ui,
                        onCell = vm::onCell,
                        onKey = vm::onKey,
                        onHint = vm::onHint,
                        onRestart = vm::onRestart,
                        onNext = vm::onNextLevel,
                        onMenu = vm::openMenu,
                        onExit = { finishAffinity() },
                    )
                }
            }
        }
    }
}
