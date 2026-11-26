#!/usr/bin/env python3
"""Static source checks for V3.0 Batch 4 StudyScreen queue integration."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DAO = (ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
REPO = (ROOT / "app/src/main/java/com/studyforge/app/data/QuestionRepository.kt").read_text()
SCREEN = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text()
APP = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
RUNTIME = (ROOT / "app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt").read_text()
UNIT = (ROOT / "app/src/test/java/com/studyforge/app/data/StudyQueueQueryTest.kt").read_text()

study_screen = SCREEN.split("fun StudyScreen(", 1)[1].split("@Composable", 1)[0]
checks = {
    "StudyScreen no longer accepts the full question table": "questions: List<QuestionEntity>" not in study_screen,
    "StudyScreen obtains IDs then hydrates only selected cards": "createStudyQueueIds: suspend" in study_screen and "loadStudyQueueCards(ids)" in SCREEN,
    "active app wires repository snapshot plus ordered hydration": "questionRepository.createStudyQueueSnapshot(query, now)" in APP and "questionRepository.loadStudyQueueCards(ids)" in APP,
    "topic choices are database distinct values": "fun observeStudyTopics(): Flow<List<String>>" in DAO and "topics = studyTopics" in APP,
    "Due queue is split to preserve separate daily caps": 'queryFor("DueNew"' in SCREEN and 'queryFor("DueReview"' in SCREEN,
    "Room supports separate New and Review due partitions": "'DueNew'" in DAO and "'DueReview'" in DAO and "reviewOrder: String" in REPO,
    "queue loading and retry states are present": "Loading study queue" in SCREEN and "queueRefreshToken++" in SCREEN,
    "active queue IDs and fingerprint survive saved-state recreation": "activeQueueIds by rememberSaveable" in SCREEN and "activeQueueFingerprint by rememberSaveable" in SCREEN,
    "Room runtime covers Due partition and Weakest ordering": "dueStudyQueueSeparatesNewAndReviewCapsAndPreservesReviewOrder" in RUNTIME,
    "Exam queue filters eligible types and multipart records in Room": "examQueueSelectsOnlyEligibleQuestionTypesAndMultipartQuestions" in RUNTIME and "EXISTS (SELECT 1 FROM question_parts" in DAO,
    "query contract validates internal Due modes, Exam mode and ordering": 'StudyQueueQuery(mode = "DueNew")' in UNIT and 'StudyQueueQuery(mode = "DueReview", reviewOrder = "Weakest")' in UNIT and 'StudyQueueQuery(mode = "Exam", randomize = true)' in UNIT,
}

for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
if not all(checks.values()):
    raise SystemExit(1)
print(f"V3 Batch 4 source readiness: {len(checks)}/{len(checks)} checks passed")
