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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.missingverses.R
import com.missingverses.data.ArabicAlphabet
import com.missingverses.domain.GameState
import com.missingverses.domain.MAX_MISTAKES

private val WrongRed = Color(0xFFD32F2F)

@Composable
fun GameScreen(
    ui: GameUiState,
    onCell: (Int, List<Int>) -> Unit,
    onKey: (Char) -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onNext: () -> Unit,
) {
    val game = ui.game
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (game == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.load_error)) }
            return@Surface
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
            // ---- Top: title, mistakes, hidden verse ----
            HeaderSection(game, ui.levelIndex + 1, onCell)
            // ---- Middle: clues (takes all remaining height) ----
            ClueList(game, onCell, Modifier.weight(1f))
            // ---- Bottom: Arabic keyboard ----
            KeyboardSection(game, onKey, onHint)
        }
        when {
            game.isComplete -> EndDialog(
                title = stringResource(R.string.level_complete),
                verse = game.puzzle.level.verse,
                poet = game.puzzle.level.poet,
                buttonText = if (ui.isLastLevel) null else stringResource(R.string.next_level),
                footer = if (ui.isLastLevel) stringResource(R.string.all_done) else null,
                onClick = onNext,
            )
            game.isFailed -> EndDialog(
                title = stringResource(R.string.level_failed),
                verse = null, poet = null,
                buttonText = stringResource(R.string.retry),
                footer = stringResource(R.string.failed_hint),
                onClick = onRestart,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderSection(game: GameState, levelNumber: Int, onCell: (Int, List<Int>) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            stringResource(R.string.level_label, levelNumber) + "  ·  " + game.puzzle.level.poet +
                (game.puzzle.level.poem?.let { "  ·  $it" } ?: ""),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.alpha(0.8f),
        )
        MistakeMarks(game.mistakes)
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
                    val group = word.map { it.number }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        word.forEach { cell -> GameCell(game, cell.number, group, boxed = false, onCell) }
                    }
                }
            }
        }
    }
}

/** ✕ ✕ ✕ — one mark turns red per mistake; the level fails at [MAX_MISTAKES]. */
@Composable
private fun MistakeMarks(mistakes: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(MAX_MISTAKES) { i ->
            Text(
                "✕", fontSize = 22.sp, fontWeight = FontWeight.Black,
                color = if (i < mistakes) WrongRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            )
        }
    }
}

/** Binds one cipher cell to the game state; every cell with the same number is highlighted together. */
@Composable
private fun GameCell(game: GameState, number: Int, group: List<Int>, boxed: Boolean, onCell: (Int, List<Int>) -> Unit) {
    val sel = game.selection
    LetterCell(
        letter = game.letterAt(number),
        number = number,
        boxed = boxed,
        selected = sel?.number == number,
        related = sel != null && number in sel.group,
        wrong = game.lastWrong?.takeIf { it.first == number }?.second,
        onClick = { onCell(number, group) },
    )
}

/** A single cipher cell: the letter (or a dash while hidden) above its number. */
@Composable
private fun LetterCell(
    letter: Char?, number: Int, boxed: Boolean, selected: Boolean, related: Boolean, wrong: Char?, onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.secondary
    val shape = RoundedCornerShape(6.dp)
    val fill = when {
        selected -> accent.copy(alpha = 0.55f)
        related && letter == null -> accent.copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    Column(
        Modifier
            .width(if (boxed) 36.dp else 28.dp)
            .then(if (boxed) Modifier.border(BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outline), shape) else Modifier)
            .background(fill, shape)
            .clickable(enabled = letter == null, onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = (letter ?: wrong)?.toString() ?: if (boxed) " " else "–",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                letter != null -> MaterialTheme.colorScheme.primary
                wrong != null -> WrongRed
                else -> MaterialTheme.colorScheme.outline
            },
        )
        Text(number.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ClueList(game: GameState, onCell: (Int, List<Int>) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(game.puzzle.level.clues) { index, clue ->
            val numbers = game.puzzle.clueNumbers[index]
            val solved = game.isClueSolved(index)
            val active = game.selection?.group == numbers
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (active) 6.dp else 1.dp,
                border = if (active) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(10.dp)) {
                    Text(
                        "${index + 1}. ${clue.clue}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.alpha(if (solved) 0.55f else 1f),
                    )
                    // Slots can be answered in ANY order: tap a slot, then a key. First slot is on the right (RTL).
                    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        numbers.forEach { number -> GameCell(game, number, numbers, boxed = true, onCell) }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardSection(game: GameState, onKey: (Char) -> Unit, onHint: () -> Unit) {
    // The keyboard is always left-to-right so it matches the physical Arabic keyboard layout.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val widest = ArabicAlphabet.keyboardRows.maxOf { it.size }
            ArabicAlphabet.keyboardRows.forEach { row ->
                Row(Modifier.fillMaxWidth(row.size / widest.toFloat()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { letter ->
                        val disabled = game.isKeyDisabled(letter)
                        Box(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp)
                                .alpha(if (disabled) 0.3f else 1f)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable(enabled = !disabled) { onKey(letter) },
                            contentAlignment = Alignment.Center,
                        ) { Text(letter.toString(), fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            OutlinedButton(onClick = onHint, enabled = !game.isOver) {
                Text(stringResource(R.string.hint) + " (${game.hintsUsed})")
            }
        }
    }
}

@Composable
private fun EndDialog(title: String, verse: String?, poet: String?, buttonText: String?, footer: String?, onClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = {
            Column {
                if (verse != null) Text(verse, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)
                if (poet != null) Text("— $poet", modifier = Modifier.padding(top = 8.dp))
                if (footer != null) Text(footer, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { if (buttonText != null) Button(onClick = onClick) { Text(buttonText) } },
    )
}
