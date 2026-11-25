#!/usr/bin/env python3
"""Static compatibility checks for schema-12 backup and question versions."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
data = json.loads((root / "backup-example.json").read_text(encoding="utf-8"))
assert data.get("app") == "StudyForge" and data.get("schemaVersion") == 12
assert isinstance(data.get("questionVersions"), list)
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
assert "schemaVersion in 1..12" in app
assert '.put("schemaVersion", 12)' in app
assert 'root.put("questionVersions", JSONArray()' in app
assert 'if (schemaVersion >= 12)' in app
assert 'dao.upsertQuestionVersion(safeVersion)' in app
rules = (root / "app/src/main/java/com/studyforge/app/data/QuestionVersionRules.kt").read_text(encoding="utf-8")
assert "fsrsStability" not in rules and "reviewCount" not in rules
print("PASS: schema-12 backup carries version snapshots; restores preserve scheduler and review fields")
