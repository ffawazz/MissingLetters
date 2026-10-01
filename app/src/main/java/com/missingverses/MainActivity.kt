package com.missingverses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.missingverses.ui.GameScreen
import com.missingverses.ui.GameViewModel
import com.missingverses.ui.MissingVersesTheme

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MissingVersesTheme {
                val ui by vm.ui.collectAsState()
                GameScreen(
                    ui = ui,
                    onCell = vm::onCell,
                    onKey = vm::onKey,
                    onHint = vm::onHint,
                    onRestart = vm::onRestart,
                    onNext = vm::onNextLevel,
                )
            }
        }
    }
}
