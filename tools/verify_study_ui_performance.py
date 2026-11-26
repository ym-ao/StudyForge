#!/usr/bin/env python3
"""Static regression gate for the one-card Study flow and lightweight Library."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(relative: str) -> str:
    path = ROOT / relative
    if not path.is_file():
        raise AssertionError(f"Missing required source file: {relative}")
    return path.read_text(encoding="utf-8")

study = read("app/src/main/java/com/studyforge/app/ui/StudyScreens.kt")
library = read("app/src/main/java/com/studyforge/app/ui/PagedLibraryScreen.kt")
root_app = read("app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt")
content = read("app/src/main/java/com/studyforge/app/ui/StudyContent.kt")

# The production Study screen uses bounded database snapshots, not full-list derivation.
assert "createStudyQueueIds: suspend" in study
assert "loadStudyQueueCards: suspend" in study
assert "activeQueueIds by rememberSaveable" in study
assert "activeQueueFingerprint by rememberSaveable" in study
assert 'queryFor("DueNew"' in study and 'queryFor("DueReview"' in study
assert "val queueFingerprint = listOf(" in study
assert "loadContentBlocks: suspend (String) -> List<QuestionContentBlockEntity>" in study
assert "activeQuestionBlocks = runCatching { loadContentBlocks(q.id).sortedBy { it.position } }" in study
assert "contentBlocks.groupBy { it.questionId }" not in study

# The standard Study screen presents one active card with visible queue progress.
assert 'val q = activeQuestions[safeIndex]' in study
assert 'Text("Card $progressPosition of $progressTotal"' in study
assert 'var dueCompletedCount by rememberSaveable' in study
assert 'dueCompletedCount++' in study and 'index = 0' in study
assert "LinearProgressIndicator(" in study
assert 'Text("Show answer")' in study
assert 'if (mode != "Due")' in study, "Due reviews should advance through ratings, not manual skipping"
assert 'onReview(q, rating, wasCorrect' in study
# Rating controls must keep labels on one line and use compact horizontal padding
# so the five-character "Again" label fits on narrow phone widths.
assert "contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)" in study
assert "Text(rating, maxLines = 1, softWrap = false)" in study

# The active production Library uses Room Paging and lightweight projection rows.
assert "collectAsLazyPagingItems()" in library
assert "viewModel.rows" in library
assert "delay(180)" in library and "viewModel.updateQuery(search.trim())" in library
assert "viewModel.countMatches(criteria)" in library
assert "viewModel.countMatches(preset.toCriteria())" in library
assert "QuestionLibraryRow" in library and "rows.itemKey { it.id }" in library
assert "LaunchedEffect(questions, criteria, collectionQuestionIds)" not in library
assert "QuestionLibraryFilter.matches" not in library
assert "1 -> PagedLibraryScreen(" in root_app
assert "questions: List<QuestionEntity>" not in library
assert "row.prompt.take(360)" in library

# Math rendering is cached, guarded against parser exceptions, and bounded in size.
assert "private fun FrameLayout.renderFormulaSafely(source: String)" in content
assert "if (tag == source && childCount > 0) return" in content
assert "runCatching" in content and "RaTeXView(context)" in content
assert "source.length <= 1500" in content
assert "Formula preview unavailable" in content

print("PASS: Study uses bounded Room queue snapshots with saved-state ID restoration, daily Due quotas and active-question-only rich-content hydration")
print("PASS: Study renders one active card with queue progress and rating-driven Due navigation")
print("PASS: Again/Hard/Good/Easy labels remain single-line with compact button padding")
print("PASS: active Library uses debounced Room Paging, database-backed counts, stable projection rows and no in-memory full-list filtering")
print("PASS: LaTeX renderer caches results and falls back safely for malformed/oversized input")
print("LIMIT: static source checks do not replace Android Compose runtime and physical-device tests")
