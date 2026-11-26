#!/usr/bin/env python3
"""Static source checks for V3.0 Batch 5 dashboard and Progress aggregates."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DAO = (ROOT / "app/src/main/java/com/studyforge/app/data/StudyDatabase.kt").read_text()
APP = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt").read_text()
SCREENS = (ROOT / "app/src/main/java/com/studyforge/app/ui/StudyScreens.kt").read_text()
RUNTIME = (ROOT / "app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt").read_text()

checks = {
    "Home dashboard has a compact Room aggregate projection": "data class HomeDashboardStats" in DAO and "observeHomeDashboardStats" in DAO,
    "Home totals and due count are computed in SQLite": "COUNT(*) FROM questions WHERE dueAt IS NULL OR dueAt <= :now" in DAO and "COALESCE(SUM(reviewCount), 0)" in DAO,
    "Home route consumes aggregate values rather than deriving its metrics from question rows": "questionCount = homeStats.questionCount" in APP and "reviewCount = homeStats.reviewCount" in APP and "dueCount = homeStats.dueCount" in APP,
    "Progress headline stats have a compact aggregate projection": "data class ProgressCoreStats" in DAO and "observeProgressCoreStats" in DAO,
    "Progress counts and scored accuracy are database aggregates": "AS scoredReviews" in DAO and "AS correctReviews" in DAO and "AS masteredCount" in DAO,
    "Progress recent-window and active-day metrics are database aggregates": "AS recentScoredReviews" in DAO and "AS lastSevenDays" in DAO and "AS activeReviewDays" in DAO,
    "Progress screen receives the Room aggregate projection": "coreStats: ProgressCoreStats" in SCREENS and "coreStats = progressStats" in APP,
    "Progress UI reads aggregate counts instead of recounting full question and log lists": "coreStats.totalReviews.toString()" in SCREENS and "coreStats.questionCount" in SCREENS,
    "Room runtime test verifies dashboard and Progress aggregates": "dashboardAndProgressAggregatesAreComputedByRoom" in RUNTIME,
    "Batch 5 baseline preserved schema 15 without an app-version bump": "Room schema 15" in (ROOT / "V3_BATCH_5_HOME_PROGRESS_AGGREGATES.md").read_text() and 'versionName = "2.1.24"' in (ROOT / "app/build.gradle.kts").read_text(),
}

for label, passed in checks.items():
    print(f"{'PASS' if passed else 'FAIL'}: {label}")
if not all(checks.values()):
    raise SystemExit(1)
print(f"V3 Batch 5 source readiness: {len(checks)}/{len(checks)} checks passed")
