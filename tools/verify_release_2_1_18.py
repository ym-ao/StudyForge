#!/usr/bin/env python3
"""Static source consistency checks for StudyForge 2.1.18."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
db = (root / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
app = (root / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
screens = (root / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
assert 'versionName = "2.1.18"' in gradle and "versionCode = 22" in gradle
assert "version = 12" in db and "Migration(11, 12)" in db
assert "schemaVersion in 1..12" in app and '.put("schemaVersion", 12)' in app
assert "QuestionVersionRules.createSnapshot" in app and "QuestionVersionRules.restore" in app
assert "QuestionVersionEntity::class" in db and "question_versions" in db
assert "getQuestionVersionByNumber" in db and "dao.upsertQuestionVersion(safeVersion)" in app
assert "Unverified" in app and "Archived" in app
renderer = (root / "app/src/main/java/com/studyforge/app/ui/StudyContent.kt").read_text()
assert "groups[4]" in renderer and "groups[5]" in renderer
formatting = (root / "app/src/main/java/com/studyforge/app/data/TextFormattingRules.kt").read_text()
assert '"UNDERLINE"' in formatting and '"HIGHLIGHT"' in formatting
assert "onRestoreVersion" in app and "History (" in app
assert "QuestionInterchange" in app and "onImportAnki" in app and "onExportCsv" in app
assert "onBulkUpdate" in app and "onManualSchedule" in app
assert '"True/False"' in app and '"Numerical"' in screens and '"Cloze"' in screens
assert "collectionId" in db and "collectionQuestionIds" in screens
assert "snapshot.length <= 20_000_000" in app
assert "StudyForge-2.1.18-debug-apk" in workflow and "StudyForge-2.1.18-release-apk" in workflow
for test in ["QuestionInterchangeTest.kt", "ClozeRulesTest.kt", "ManualScheduleRulesTest.kt", "TextFormattingRulesTest.kt"]:
    assert any(root.rglob(test)), test
print("PASS: 2.1.18 version, Room/backup schema, version history, interchange, formats, bulk actions, manual scheduling and session collections")
