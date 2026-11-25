#!/usr/bin/env python3
"""Static compatibility checks for schema-13 source/provenance backup fields."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
data = json.loads((root / "backup-example.json").read_text(encoding="utf-8"))
assert data.get("app") == "StudyForge" and data.get("schemaVersion") == 13
assert data["questions"][0]["sourceTitle"] == "Example textbook"
assert data["questions"][0]["sourceDetail"] == "Chapter 1, page 2"
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
assert "schemaVersion in 1..13" in app and '.put("schemaVersion", 13)' in app
assert '"sourceTitle", q.sourceTitle' in app and '"sourceDetail", q.sourceDetail' in app
assert 'sourceTitle = if (schemaVersion >= 13)' in app and 'sourceDetail = if (schemaVersion >= 13)' in app
assert 'sourceTitle = if (schemaVersion < 13) existing.sourceTitle else imported.sourceTitle' in app
assert 'sourceDetail = if (schemaVersion < 13) existing.sourceDetail else imported.sourceDetail' in app
rules = (root / "app/src/main/java/com/studyforge/app/data/QuestionVersionRules.kt").read_text(encoding="utf-8")
assert '.put("sourceTitle", question.sourceTitle)' in rules and 'sourceDetail = root.optString("sourceDetail", current.sourceDetail)' in rules
print("PASS: schema-13 example, legacy-compatible import, source export/import and version snapshots")
