from pathlib import Path

root = Path(__file__).resolve().parents[1]
gradle = (root / "app/build.gradle.kts").read_text()
workflow = (root / ".github/workflows/android-apk.yml").read_text()
scheduler = (root / "app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt").read_text()
tests = (root / "app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt").read_text()
backup = (root / "app/src/main/java/com/studyforge/app/data/BackupManager.kt")
assert 'versionName = "2.1.24"' in gradle and "versionCode = 28" in gradle
assert "StudyForge-V3.1-dev-debug-apk" in workflow
assert "StudyForge-V3.1-dev-signed-apk" in workflow
assert "nextForgetStability(currentD" in scheduler and "nextRecallStability(currentD" in scheduler
assert "coerceAtLeast(0.001)" in scheduler
assert "longTermReviewTransitionsMatchFsrs6ReferenceVectors" in tests
assert "pinnedPyFsrsV600MemoStateSequenceMatchesEveryTransition" in tests
assert "0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966" in scheduler
assert "hardStepMinutes" in scheduler and "completed 24-hour periods" in scheduler
assert "fsrsStability" in tests
print("PASS: 2.1.24 version/workflow metadata and pinned FSRS-6 v6.0.0 compatibility regression coverage")
