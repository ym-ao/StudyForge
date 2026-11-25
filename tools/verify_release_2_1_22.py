#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.22."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
scheduler = (root / "app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt").read_text()
tests = (root / "app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()

assert 'versionName = "2.1.22"' in gradle and "versionCode = 26" in gradle
assert "version = 15" in db
assert '.put("schemaVersion", 15)' in app and "schemaVersion in 1..15" in app
assert 'isNew && grade == 2' in scheduler
assert 'isNew && grade == 3 && learningSteps.size > 1' in scheduler
assert 'newGoodAdvancesToSecondLearningStepWhenConfigured' in tests
assert 'newHardStartsAtFirstLearningStepWhenConfigured' in tests
assert 'StudyForge-2.1.22-debug-apk' in workflow
assert 'StudyForge-2.1.22-release-apk' in workflow
assert 'verify_release_2_1_22.py' in workflow
print("PASS: 2.1.22 version metadata, schema-15 compatibility, first-review learning transitions, tests and workflow labels")
