#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.20."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
filters = (root / "app/src/main/java/com/studyforge/app/data/QuestionLibraryFilter.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
assert 'versionName = "2.1.20"' in gradle and "versionCode = 24" in gradle
assert "version = 15" in db and "Migration(13, 14)" in db and "MIGRATION_13_14" in db and "Migration(14, 15)" in db and "MIGRATION_14_15" in db
assert "sourceTitle" in filters and "sourceDetail" in filters
assert "schemaVersion in 1..15" in app and '.put("schemaVersion", 15)' in app
assert 'sourceTitle = sourceTitleFilter' in app and 'sourceDetail = sourceDetailFilter' in app
assert 'createdAfter = createdAfterFilter' in app and 'updatedBefore = updatedBeforeFilter' in app
assert "QuestionTypeAnalytics.summarize" in (root / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text()
assert '"Changed: ${changed.joinToString' in app
assert "StudyForge-2.1.20-debug-apk" in workflow and "StudyForge-2.1.20-release-apk" in workflow
assert "verify_migration_14_15.py" in workflow and "verify_backup_schema_15.py" in workflow
for test in ["QuestionLibraryFilterTest.kt", "SavedFilterPresetTest.kt", "QuestionTypeAnalyticsTest.kt"]:
    assert any(root.rglob(test)), test
print("PASS: 2.1.20 version, schema 15, source/date filters, question-type analytics, version comparison and workflow labels")
