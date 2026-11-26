#!/usr/bin/env python3
"""Source-shape checks for the V3.0 Batch 3 bounded Study queue boundary."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DAO = (ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
REPO = (ROOT / "app/src/main/java/com/studyforge/app/data/QuestionRepository.kt").read_text()
RUNTIME = (ROOT / "app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt").read_text()
UNIT = (ROOT / "app/src/test/java/com/studyforge/app/data/StudyQueueQueryTest.kt").read_text()

checks = {
    "database-side bounded ID snapshot": "suspend fun snapshotStudyQueueIds(" in DAO and "LIMIT :limit" in DAO,
    "saved-session filters": all(x in DAO for x in ("presetTopic", "collectionId", "performance", "questionType", "status")),
    "mode-specific deterministic ordering": all(x in DAO for x in ("'Due'", "'Ordered'", "'Weakest'", "'Random'", "q.id ASC")),
    "validated bounded query contract": "data class StudyQueueQuery" in REPO and "query.validate()" in REPO and "1..MAX_STUDY_SESSION_SIZE" in REPO,
    "on-demand hydration retains snapshot order": "return ids.mapNotNull(byId::get)" in REPO,
    "Room runtime coverage": "studyQueueSnapshotAppliesPresetFiltersAndModeOrderingInDatabase" in RUNTIME,
    "query validation unit coverage": all(x in UNIT for x in ("acceptsEveryExistingStudyMode", "rejectsUnknownModesAndUnboundedLimits", "defaultQueueIsBounded")),
}

for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
if not all(checks.values()):
    raise SystemExit(1)
print(f"V3 Batch 3 source readiness: {len(checks)}/{len(checks)} checks passed")
