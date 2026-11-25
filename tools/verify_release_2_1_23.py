#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.23."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
study = (root / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
assert 'versionName = "2.1.23"' in gradle and "versionCode = 27" in gradle
assert "version = 15" in db
assert '.put("schemaVersion", 15)' in app and "schemaVersion in 1..15" in app
assert '"HINT"' in app and '"HINT"' in study
assert 'Text("Move up")' in app and 'Text("Move down")' in app
assert "Reveal hint" in study and "revealedHintIds" in study
assert 'StudyForge-2.1.23-debug-apk' in workflow
assert 'StudyForge-2.1.23-release-apk' in workflow
assert 'verify_release_2_1_23.py' in workflow
print("PASS: 2.1.23 metadata, schema-15 compatibility, editable/revealable HINT block, clearer reorder labels, and workflow labels")
