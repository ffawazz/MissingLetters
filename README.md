# أبيات مفقودة (Missing Verses)

Arabic cryptogram/crossword word game (Figgerits-style) for Android — Kotlin, Jetpack Compose, MVVM.
Hidden phrases are verses from classical and modern Arabic poetry; the whole UI is RTL Modern Standard Arabic.

## Architecture

```
app/src/main
├── assets/levels/level_001.json …        one JSON file per level (add hundreds freely)
├── java/com/missingverses
│   ├── data/    ArabicAlphabet (28 base letters + normalisation), Level (JSON model),
│   │            LevelRepository (loads+validates assets), ProgressStore (current level)
│   ├── domain/  Puzzle (number<->letter maps, verse/clue cells), GameEngine (pure rules),
│   │            LevelValidator
│   ├── ui/      GameViewModel (StateFlow), GameScreen (Compose), Theme (forces RTL)
│   └── MainActivity
tools/validate_levels.py                  CLI validator + cipher generator
```

MVVM: `GameScreen` renders an immutable `GameState`; user actions go to `GameViewModel`, which delegates to the
pure `GameEngine` and republishes the new state.

## Rules implemented

* Every letter of the verse and of every answer has a cipher number (`cipher` in the JSON).
* Tap ANY hidden cell — in a clue or directly in the verse, in any order — then tap a key. The guess is checked
  immediately; a correct letter is revealed in every cell sharing that number, and the selection jumps to the next hidden cell of the same word/clue.
* Wrong guesses cost a mistake (✕ ✕ ✕ at the top). Re-trying a letter already rejected for that cell is free.
  After 3 mistakes the level fails and can be retried.
* The level completes when every number is revealed, then the full original verse is shown. Hint reveals the selected cell.
* Look-alike letters are different letters with their own keys and numbers: ي ى ء ه ة ئ و ؤ.
  Only أ إ آ fold to ا (see `ArabicAlphabet.FOLDED`). Tashkeel/punctuation are ignored.
* The keyboard follows the standard Arabic layout, left-to-right like a physical keyboard.

## Adding levels

1. Create `app/src/main/assets/levels/level_003.json` with `id`, `poet`, optional `poem`, `verse`, and `clues`
   (`clue` text + `answer`). Omit `cipher`.
2. `python3 tools/validate_levels.py --fill` generates the cipher and validates everything.
   Answers must contain only letters (write أ إ آ as ا).
3. Rebuild. Levels are sorted by `id`. `./gradlew test` also validates all level files.
