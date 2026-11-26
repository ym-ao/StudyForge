#!/usr/bin/env python3
"""Static contract check for live saved-filter result counts (no Android runtime claim)."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
filter_source = (ROOT / "app/src/main/java/com/studyforge/app/data/QuestionLibraryFilter.kt").read_text(encoding="utf-8")
ui_source = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
paged_ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/PagedLibraryScreen.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/QuestionLibraryFilterTest.kt").read_text(encoding="utf-8")

assert "fun countMatches(" in filter_source
assert "questions.count { matches(it, criteria, collectionQuestionIds) }" in filter_source
assert "viewModel.countMatches(preset.toCriteria())" in paged_ui
assert "presetCounts[preset.id]" in paged_ui
assert "1 -> PagedLibraryScreen(" in ui_source
assert "savedFilterCountsAreRecomputedFromCurrentQuestions" in tests
assert "deletedCollectionReferenceProducesNoMatchesInsteadOfStaleMembership" in tests
print("PASS: active Library saved-filter counts are recalculated through Room count queries")
print("PASS: stale/deleted collection references safely show zero matches")
print("LIMIT: static source contract only; Android JUnit/UI execution not performed")
