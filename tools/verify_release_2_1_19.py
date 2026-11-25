#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.19."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
interchange = (root / "app/src/main/java/com/studyforge/app/data/QuestionInterchange.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
assert 'versionName = "2.1.19"' in gradle and "versionCode = 23" in gradle
assert "version = 13" in db and "Migration(12, 13)" in db and "MIGRATION_12_13" in db
assert "sourceTitle" in db and "sourceDetail" in db
assert "schemaVersion in 1..13" in app and '.put("schemaVersion", 13)' in app
assert "readQuestionTablePreview" in app and "Import valid rows" in app
assert "previewCsv" in interchange and "previewAnkiTsv" in interchange
assert "duplicateCount" in interchange and "ImportIssue" in interchange
assert "sourceTitle" in app and "sourceDetail" in app
assert "StudyForge-2.1.19-debug-apk" in workflow and "StudyForge-2.1.19-release-apk" in workflow
assert "verify_migration_12_13.py" in workflow and "verify_backup_schema_13.py" in workflow
for test in ["QuestionInterchangeTest.kt", "QuestionLibraryFilterTest.kt"]:
    assert any(root.rglob(test)), test
print("PASS: 2.1.19 version, Room/backup schema, provenance fields, import preview/diagnostics and workflow labels")
