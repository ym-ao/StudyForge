#!/usr/bin/env python3
"""Static contract check for applying bulk operations to the full selected Library set."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
rules = (ROOT / "app/src/main/java/com/studyforge/app/data/QuestionSelectionRules.kt").read_text(encoding="utf-8")
ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/QuestionSelectionRulesTest.kt").read_text(encoding="utf-8")
assert "fun resolve(questions: List<QuestionEntity>, selectedIds: Set<String>)" in rules
assert "questions.filter { it.id in selectedIds }" in rules
assert ui.count("QuestionSelectionRules.resolve(questions, selectedQuestionIds)") == 4
assert "filtered.filter { it.id in selectedQuestionIds }" not in ui
for case in ("resolvesSelectedQuestionsAcrossTheCompleteLibraryInLibraryOrder", "ignoresSelectedIdsThatNoLongerExist", "emptySelectionResolvesToNoQuestions"):
    assert case in tests
print("PASS: all four bulk actions resolve selected IDs against the complete Library")
print("PASS: regression source covers order, stale IDs and empty selections")
print("LIMIT: static source contract only; Android JUnit/UI execution not performed")
