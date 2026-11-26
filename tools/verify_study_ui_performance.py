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
library = read("app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt")
content = read("app/src/main/java/com/studyforge/app/ui/StudyContent.kt")

# Queue calculations are cached across answer/reveal/selection recompositions.
assert "remember(questions, now / 60_000L)" in study
assert "remember(questions, mode, selectedTopic, dueQuestions, randomQuestions)" in study
assert "remember(modeQuestions, activeSessionPreset, collectionQuestionIds)" in study
assert "contentBlocks.groupBy { it.questionId }" in study
assert "contentBlocksByQuestion[q.id].orEmpty().sortedBy { it.position }" in study

# The standard Study screen presents one active card with visible queue progress.
assert 'val q = activeQuestions[safeIndex]' in study
assert 'Text("Card $progressPosition of $progressTotal"' in study
assert 'var dueCompletedCount by rememberSaveable' in study
assert 'dueCompletedCount++; index = 0' in study
assert "LinearProgressIndicator(" in study
assert 'Text("Show answer")' in study
assert 'if (mode != "Due")' in study, "Due reviews should advance through ratings, not manual skipping"
assert 'onReview(q, rating, wasCorrect' in study
# Rating controls must keep labels on one line and use compact horizontal padding
# so the five-character "Again" label fits on narrow phone widths.
assert "contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)" in study
assert "Text(rating, maxLines = 1, softWrap = false)" in study

# Library filtering is stable across UI-only state changes and does not instantiate math views per row.
assert "LaunchedEffect(questions, criteria, collectionQuestionIds)" in library
assert "withContext(Dispatchers.Default)" in library
assert "var filtering by remember { mutableStateOf(true) }" in library
assert "delay(180)" in library and "appliedSearch = search" in library
library_start = library.index("private fun LibraryScreen(")
library_end = library.find("\n@Composable", library_start + 1)
library_body = library[library_start:library_end if library_end >= 0 else len(library)]
assert "LatexFormulaView(q.latex" not in library_body
assert "Math formula available · open question to view" in library_body
assert "q.prompt.take(360)" in library_body

# Math rendering is cached, guarded against parser exceptions, and bounded in size.
assert "private fun FrameLayout.renderFormulaSafely(source: String)" in content
assert "if (tag == source && childCount > 0) return" in content
assert "runCatching" in content and "RaTeXView(context)" in content
assert "source.length <= 1500" in content
assert "Formula preview unavailable" in content

print("PASS: Study queue derivations are memoized")
print("PASS: Study renders one active card with queue progress and rating-driven Due navigation")
print("PASS: Again/Hard/Good/Easy labels remain single-line with compact button padding")
print("PASS: Library search is debounced, filtering/counts run off the UI thread, and card previews avoid eager math rendering")
print("PASS: LaTeX renderer caches results and falls back safely for malformed/oversized input")
print("LIMIT: static source checks do not replace Android Compose runtime and physical-device tests")
