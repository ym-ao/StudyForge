#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.21."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
versions = (root / "app/src/main/java/com/studyforge/app/data/QuestionVersionRules.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
assert 'versionName = "2.1.21"' in gradle and "versionCode = 25" in gradle
assert "version = 15" in db
assert '.put("schemaVersion", 15)' in app and "schemaVersion in 1..15" in app
assert "fun compare(previousSnapshotJson: String, currentSnapshotJson: String)" in versions
assert "mediaBase64" in versions and "embedded media" in versions
assert "QuestionVersionRules.compare(version.snapshotJson, currentSnapshot)" in app
assert 'StudyForge-2.1.21-debug-apk' in workflow and 'StudyForge-2.1.21-release-apk' in workflow
assert "verify_release_2_1_21.py" in workflow
assert (root / "app/src/test/java/com/studyforge/app/data/QuestionVersionRulesTest.kt").exists()
assert "testImplementation(\"org.json:json:20240303\")" in gradle
print("PASS: 2.1.21 version metadata, unchanged Room/backup schema 15, full version diff, tests and workflow labels")
