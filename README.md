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
* The player selects a clue and types with the in-app Arabic keyboard (a system keyboard would break RTL/letter folding).
* When a clue's slots are full it is checked: correct → all its numbers are revealed in the verse **and** in every
  other clue using them (clues fully revealed this way are auto-solved); wrong → slots cleared, mistake counted.
* Hint reveals one number. The level completes when every verse number is revealed, then the full original verse is shown.
* Letter folding: أ إ آ ٱ→ا, ى ئ→ي, ؤ→و, ة→ه; tashkeel/punctuation ignored. Answers must use the 28 base letters.

## Adding levels

1. Create `app/src/main/assets/levels/level_003.json` with `id`, `poet`, optional `poem`, `verse`, and `clues`
   (`clue` text + `answer`). Omit `cipher`.
2. `python3 tools/validate_levels.py --fill` generates the cipher and validates everything.
   The key rule: every distinct verse letter must appear in at least one clue answer.
3. Rebuild. Levels are sorted by `id`. `./gradlew test` also validates all level files.
