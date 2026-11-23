#!/usr/bin/env python3
"""Static compatibility check for StudyForge schema-9 example backups."""
import json
from pathlib import Path
p = Path(__file__).resolve().parents[1] / "backup-example.json"
data = json.loads(p.read_text(encoding="utf-8"))
assert data.get("app") == "StudyForge"
assert data.get("schemaVersion") == 9
for part in data.get("questionParts", []):
    assert isinstance(part.get("absoluteTolerance", 0.0), (int, float))
    assert isinstance(part.get("relativeTolerance", 0.0), (int, float))
    assert isinstance(part.get("acceptedAnswers", ""), str)
for session in data.get("studySessionPresets", []):
    assert session.get("questionType", "All") in {"All", "Flashcard", "MCQ", "Numerical", "Cloze", "Derivation"}
    assert session.get("performance", "All") in {"All", "Unattempted", "Weak", "Strong"}
print("PASS: schema-9 backup example and optional grading/session fields are valid")
