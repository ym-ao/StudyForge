#!/usr/bin/env python3
"""Static compatibility checks for schema-15 saved filter backup fields."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
data = json.loads((root / "backup-example.json").read_text(encoding="utf-8"))
assert data.get("app") == "StudyForge" and data.get("schemaVersion") == 15
preset = data["savedFilterPresets"][0]
assert preset["sourceTitle"] == "Biology textbook" and preset["sourceDetail"] == "Chapter 4 page 82"
assert preset["createdAfter"] == "2026-01-01" and preset["updatedBefore"] == "2026-11-01"
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
assert "schemaVersion in 1..15" in app and '.put("schemaVersion", 15)' in app
for field in ("sourceTitle", "sourceDetail", "createdAfter", "createdBefore", "updatedAfter", "updatedBefore"):
    assert f'.put("{field}", preset.{field})' in app
    assert f'{field} = o.optString("{field}")' in app
assert 'createdAfter = createdAfterFilter' in app and 'updatedBefore = updatedBeforeFilter' in app
assert 'sourceTitle = if (schemaVersion < 14) existing?.sourceTitle.orEmpty() else preset.sourceTitle' in app
assert 'createdAfter = if (schemaVersion < 15) existing?.createdAfter.orEmpty() else preset.createdAfter' in app
print("PASS: schema-15 backup export/import and saved source/date filters")
