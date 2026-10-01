package com.missingverses.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.missingverses.R
import com.missingverses.data.ArabicAlphabet
import com.missingverses.domain.GameState

@Composable
fun GameScreen(
    ui: GameUiState,
    onClue: (Int) -> Unit,
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onHint: () -> Unit,
    onNext: () -> Unit,
) {
    val game = ui.game
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (game == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.load_error)) }
            return@Surface
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            // ---- Top: title + hidden verse ----
            HeaderSection(game, ui.levelIndex + 1)
            // ---- Middle: clues (takes all remaining height) ----
            ClueList(game, onClue, Modifier.weight(1f))
            // ---- Bottom: Arabic keyboard ----
            KeyboardSection(game.mistakes, onKey, onBackspace, onHint)
        }
        if (game.isComplete) {
            LevelCompleteDialog(game, ui.isLastLevel, onNext)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderSection(game: GameState, levelNumber: Int) {
    val activeNumbers = game.activeClue?.let { game.puzzle.clueNumbers[it].toSet() }.orEmpty()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(stringResource(R.string.level_label, levelNumber), style = MaterialTheme.typography.labelLarge)
        Text(
            "— ${game.puzzle.level.poet}" + (game.puzzle.level.poem?.let { " · $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.alpha(0.7f),
        )
        // Verse can be long: keep it scrollable inside a bounded area so the keyboard never gets pushed off.
        Box(
            Modifier.fillMaxWidth().heightIn(max = 190.dp).verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            // In an RTL layout FlowRow starts at the right, so words read in the correct Arabic order.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                game.puzzle.verseWords.forEach { word ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        word.forEach { cell ->
                            LetterCell(
                                letter = game.verseLetter(cell.number),
                                number = cell.number,
                                highlighted = cell.number in activeNumbers && game.verseLetter(cell.number) == null,
                                boxed = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A single cipher cell: the letter (or a dash while hidden) above its number. */
@Composable
private fun LetterCell(letter: Char?, number: Int, highlighted: Boolean, boxed: Boolean) {
    val accent = MaterialTheme.colorScheme.secondary
    val shape = RoundedCornerShape(6.dp)
    Column(
        Modifier
            .width(if (boxed) 36.dp else 28.dp)
            .then(if (boxed) Modifier.border(BorderStroke(1.dp, if (highlighted) accent else MaterialTheme.colorScheme.outline), shape) else Modifier)
            .background(if (highlighted) accent.copy(alpha = 0.18f) else Color.Transparent, shape)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = letter?.toString() ?: if (boxed) " " else "–",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (letter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
        Text(number.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ClueList(game: GameState, onClue: (Int) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(game.puzzle.level.clues) { index, clue ->
            val solved = index in game.solved
            val active = index == game.activeClue
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (active) 6.dp else 1.dp,
                border = if (active) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
                modifier = Modifier.fillMaxWidth().clickable(enabled = !solved) { onClue(index) },
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        "${index + 1}. ${clue.clue}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.alpha(if (solved) 0.55f else 1f),
                    )
                    // Answer slots: first slot is on the right in RTL, matching the word's reading order.
                    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        game.puzzle.clueNumbers[index].forEachIndexed { pos, number ->
                            LetterCell(
                                letter = game.slotLetter(index, pos),
                                number = number,
                                highlighted = active,
                                boxed = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardSection(mistakes: Int, onKey: (Char) -> Unit, onBackspace: () -> Unit, onHint: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ArabicAlphabet.keyboardRows.forEach { row ->
            // Row children are laid out right-to-left, so ا ب ت ... start at the right edge.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { letter ->
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onKey(letter) },
                        contentAlignment = Alignment.Center,
                    ) { Text(letter.toString(), fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onHint, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.hint)) }
            OutlinedButton(onClick = onBackspace, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.backspace)) }
            Text(stringResource(R.string.mistakes_label, mistakes), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LevelCompleteDialog(game: GameState, isLast: Boolean, onNext: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.level_complete)) },
        text = {
            Column {
                Text(game.puzzle.level.verse, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                Text("— ${game.puzzle.level.poet}", modifier = Modifier.padding(top = 8.dp))
                if (isLast) Text(stringResource(R.string.all_done), modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            if (!isLast) Button(onClick = onNext) { Text(stringResource(R.string.next_level)) }
        },
    )
}
