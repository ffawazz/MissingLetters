package com.missingverses.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.missingverses.R

@Composable
fun MenuScreen(ui: GameUiState, onPlay: () -> Unit, onLevels: () -> Unit, onExit: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.app_name),
                fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary,
            )
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 40.dp))
            val width = Modifier.fillMaxWidth(0.7f)
            Button(onClick = onPlay, enabled = ui.levelCount > 0, modifier = width.heightIn(min = 52.dp)) {
                Text(stringResource(if (ui.completed.isEmpty()) R.string.play else R.string.continue_game), fontSize = 20.sp)
            }
            OutlinedButton(onClick = onLevels, enabled = ui.levelCount > 0, modifier = width.heightIn(min = 52.dp).padding(top = 12.dp)) {
                Text(stringResource(R.string.select_level), fontSize = 18.sp)
            }
            OutlinedButton(onClick = onExit, modifier = width.heightIn(min = 52.dp).padding(top = 12.dp)) {
                Text(stringResource(R.string.exit), fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun LevelSelectScreen(ui: GameUiState, onLevel: (Int) -> Unit, onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.levels_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(76.dp),
                modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items((0 until ui.levelCount).toList()) { index ->
                    val unlocked = ui.isUnlocked(index)
                    val done = index in ui.completed
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 3.dp,
                        border = BorderStroke(if (done) 2.dp else 1.dp, if (done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.heightIn(min = 72.dp).alpha(if (unlocked) 1f else 0.35f).clickable(enabled = unlocked) { onLevel(index) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${index + 1}" + if (done) " ✓" else "", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.main_menu)) }
        }
    }
}
