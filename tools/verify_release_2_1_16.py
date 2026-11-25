#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.16."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text(encoding="utf-8")
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text(encoding="utf-8")
screens = (root / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text(encoding="utf-8")
format_rules = (root / "app/src/main/java/com/studyforge/app/data/TextFormattingRules.kt").read_text(encoding="utf-8")
workflow = (root / ".github/workflows/android-apk.yml").read_text(encoding="utf-8")
backup = (root / "backup-example.json").read_text(encoding="utf-8")

assert 'versionName = "2.1.16"' in gradle
assert "versionCode = 21" in gradle
assert "version = 10" in db
assert "Migration(9, 10)" in db
assert "schemaVersion in 1..10" in app
assert '.put("schemaVersion", 10)' in app
assert 'root.put("examAttempts", JSONArray()' in app
assert 'TextFormattingRules.apply' in app
assert 'SelectionFormattingField' in app and 'setOf("TEXT", "HEADING", "LIST")' in app
for field in ['"Question / Front"', '"Explanation (optional)"', '"Part prompt"', '"Part answer"', '"Part explanation"']:
    assert field in app, field
for operation in ['"BOLD"', '"ITALIC"', '"HEADING"', '"BULLET"', '"CODE"']:
    assert operation in format_rules, operation
assert "medianScore" in screens and "recentTrend" in screens and "scoreBandCounts" in screens
assert "StudyForge-2.1.16-debug-apk" in workflow
assert "StudyForge-2.1.16-release-apk" in workflow
assert '"schemaVersion": 10' in backup
assert (root / "app/src/test/java/com/studyforge/app/data/TextFormattingRulesTest.kt").exists()
print("PASS: 2.1.16 version, Room/backup compatibility, editor formatting, analytics, tests and workflow labels")
