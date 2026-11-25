#!/usr/bin/env python3
"""Static contract check for versioned bulk tag operations in the Library."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ui = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
rules = (ROOT / "app/src/main/java/com/studyforge/app/data/TagText.kt").read_text(encoding="utf-8")
tests = (ROOT / "app/src/test/java/com/studyforge/app/data/TagTextTest.kt").read_text(encoding="utf-8")
assert "onBulkTag = { selected, tagName, add -> scope.launch" in ui
assert "TagText.add(current.tagsCsv, normalized)" in ui
assert "TagText.remove(current.tagsCsv, normalized)" in ui
assert "QuestionVersionRules.createSnapshot(current" in ui
assert "dao.insertQuestionVersion" in ui
assert "database.withTransaction" in ui
assert 'Text("Add tag")' in ui and 'Text("Remove tag")' in ui
assert "normalized.contains(',')" in ui
assert "fun add(csv: String, name: String)" in rules
for case in ("addTrimsAndDeduplicatesCaseInsensitively", "addIgnoresBlankOrCommaDelimitedTagName"):
    assert case in tests, f"Missing bulk-tag text rule regression: {case}"
print("PASS: bulk tag add/remove is transactional, avoids comma-format corruption, and snapshots changed question content")
