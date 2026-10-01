#!/usr/bin/env python3
"""Validate (and optionally auto-number) level JSON files.

Usage:
  python3 tools/validate_levels.py [levels_dir]          # validate only
  python3 tools/validate_levels.py [levels_dir] --fill   # generate missing "cipher" maps, then validate

Authoring a new level only needs: id, poet, verse, clues[{clue, answer}].
Run with --fill to generate a random-but-stable letter->number cipher.
"""
import json
import pathlib
import random
import sys

# Look-alikes (ي ى ء ه ة ئ و ؤ) are DIFFERENT letters. Only the hamza-on-alef family folds to ا.
KEYBOARD = set("ضصثقفغعهخحجدشسيبلاتنمكطذئءؤرىةوزظ")
FOLD = {"أ": "ا", "إ": "ا", "آ": "ا", "ٱ": "ا"}


def base(ch):
    ch = FOLD.get(ch, ch)
    return ch if ch in KEYBOARD else None


def letters(text):
    return [b for b in map(base, text) if b]


def needed_letters(level):
    s = set(letters(level["verse"]))
    for c in level["clues"]:
        s |= set(letters(c["answer"]))
    return s


def fill(level):
    chars = sorted(needed_letters(level))
    nums = list(range(1, len(chars) + 1))
    random.Random(level["id"]).shuffle(nums)
    level["cipher"] = dict(zip(chars, nums))


def validate(level):
    errs = []
    cipher = level.get("cipher") or {}
    verse = set(letters(level["verse"]))
    answers = set()
    for c in level["clues"]:
        a = c["answer"]
        if any(base(ch) != ch for ch in a):
            errs.append(f"answer '{a}' must contain only letters (write أ إ آ as ا)")
        answers |= set(letters(a))
    if len(set(cipher.values())) != len(cipher):
        errs.append("cipher numbers are not unique")
    if set(cipher) != verse | answers:
        errs.append(f"cipher letters mismatch; missing={sorted((verse|answers)-set(cipher))} extra={sorted(set(cipher)-(verse|answers))}")
    if verse - answers:
        errs.append(f"verse letters missing from ALL clue answers: {sorted(verse - answers)}")
    return errs


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    do_fill = "--fill" in sys.argv
    root = pathlib.Path(args[0] if args else "app/src/main/assets/levels")
    bad = 0
    ids = set()
    for f in sorted(root.glob("level_*.json")):
        level = json.loads(f.read_text(encoding="utf-8"))
        if level["id"] in ids:
            print(f"{f.name}: duplicate id {level['id']}")
            bad += 1
        ids.add(level["id"])
        if do_fill and not level.get("cipher"):
            fill(level)
            f.write_text(json.dumps(level, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        errs = validate(level)
        print(f"{f.name}: {'OK' if not errs else 'FAIL'}")
        for e in errs:
            print("   -", e)
        bad += bool(errs)
    print(f"{len(ids)} level(s) checked, {bad} problem file(s)")
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()
