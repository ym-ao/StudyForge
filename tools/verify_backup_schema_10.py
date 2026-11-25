#!/usr/bin/env python3
"""Static compatibility check for StudyForge schema-10 example backups."""
import json
from pathlib import Path
p = Path(__file__).resolve().parents[1] / "backup-example.json"
data = json.loads(p.read_text(encoding="utf-8"))
assert data.get("app") == "StudyForge"
assert data.get("schemaVersion") == 10
for part in data.get("questionParts", []):
    assert isinstance(part.get("absoluteTolerance", 0.0), (int, float))
    assert isinstance(part.get("relativeTolerance", 0.0), (int, float))
    assert isinstance(part.get("acceptedAnswers", ""), str)
for session in data.get("studySessionPresets", []):
    assert session.get("questionType", "All") in {"All", "Flashcard", "MCQ", "Numerical", "Cloze", "Derivation"}
    assert session.get("performance", "All") in {"All", "Unattempted", "Weak", "Strong"}
print("PASS: schema-10 backup example and optional grading/session fields are valid")
# Source-level compatibility guards: legacy imports are accepted and only present
# schema-10 attempt arrays are merged; no clearing/deletion of existing attempts.
app_source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
assert "schemaVersion in 1..10" in app_source
assert "if (schemaVersion >= 10)" in app_source
assert "if (importedExamAttempts.isNotEmpty()) dao.upsertExamAttempts(importedExamAttempts)" in app_source
assert 'root.put("examAttempts", JSONArray()' in app_source
attempt_source = (Path(__file__).resolve().parents[1] / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text(encoding="utf-8")
assert "version = 10" in attempt_source and "Migration(9, 10)" in attempt_source
assert "FOREIGN KEY" not in attempt_source.split("private val MIGRATION_9_10", 1)[1].split("@Volatile", 1)[0]
print("PASS: schema-10 backup export, legacy import range, non-destructive attempt merge, and Room migration source guards")
