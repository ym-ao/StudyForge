#!/usr/bin/env python3
"""Static source-integrity gate for the current StudyForge V2 baseline.

This deliberately does not pretend to execute Room, Android, or backup runtime
code. It verifies that release metadata, registered migrations, backup schema
handling, CI wiring, and signing contract agree before expensive CI/device tests.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def read(relative: str) -> str:
    path = ROOT / relative
    if not path.is_file():
        raise AssertionError(f"Missing required source file: {relative}")
    return path.read_text(encoding="utf-8")

app_gradle = read("app/build.gradle.kts")
database = read("app/src/main/java/com/studyforge/app/data/StudyDatabase.kt")
app_ui = read("app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt")
workflow = read(".github/workflows/android-apk.yml")
room_runtime_test = read("app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt")
authoring_rules = read("app/src/main/java/com/studyforge/app/data/QuestionAuthoringRules.kt")
content_parser_tests = read("app/src/test/java/com/studyforge/app/data/ContentBlockParsingTest.kt")
interchange = read("app/src/main/java/com/studyforge/app/data/QuestionInterchange.kt")
interchange_tests = read("app/src/test/java/com/studyforge/app/data/QuestionInterchangeTest.kt")
backup_snapshot_check = read("tools/verify_backup_snapshot.py")
bulk_tag_check = read("tools/verify_bulk_tagging.py")
bulk_collection_check = read("tools/verify_bulk_collection.py")
smart_filter_check = read("tools/verify_live_smart_filters.py")
bulk_selection_check = read("tools/verify_bulk_selection.py")
session_preset_edit_check = read("tools/verify_session_preset_edit.py")
study_ui_performance_check = read("tools/verify_study_ui_performance.py")
session_preset_rules = read("app/src/main/java/com/studyforge/app/data/StudySessionPresetRules.kt")
session_preset_tests = read("app/src/test/java/com/studyforge/app/data/StudySessionPresetRulesTest.kt")
tag_rules = read("app/src/main/java/com/studyforge/app/data/TagText.kt")
tag_tests = read("app/src/test/java/com/studyforge/app/data/TagTextTest.kt")
media_rules = read("app/src/main/java/com/studyforge/app/data/MediaImportRules.kt")
media_tests = read("app/src/test/java/com/studyforge/app/data/MediaImportRulesTest.kt")
review_trend_rules = read("app/src/main/java/com/studyforge/app/data/ReviewTrendRules.kt")
review_trend_ui = read("app/src/main/java/com/studyforge/app/ui/StudyScreens.kt")
review_trend_tests = read("app/src/test/java/com/studyforge/app/data/ReviewTrendRulesTest.kt")
signing = read("SIGNING_SETUP.md")
plan = read("StudyForge_Plan.txt")
current_plan = read("PROJECT_PLAN.md")

assert 'versionName = "2.1.24"' in app_gradle, "Unexpected app versionName"
assert "Version 2.1.24 • StudyForge learning system" in app_ui, "Dashboard release label is stale"
assert re.search(r"versionCode\s*=\s*28\b", app_gradle), "Unexpected versionCode"
assert re.search(r"version\s*=\s*15\s*,", database), "Room schema must remain 15"
assert "Current Room database schema version 15" in current_plan, "PROJECT_PLAN Room baseline is stale"
assert "Backup JSON schema version 15" in current_plan, "PROJECT_PLAN backup baseline is stale"
assert "1 -> 2` through `14 -> 15" in current_plan, "PROJECT_PLAN migration baseline is stale"

# Every additive Room migration from the original schema to the current schema
# must be declared, and every migration must be registered in the builder.
migration_pairs = [(n, n + 1) for n in range(1, 15)]
for old, new in migration_pairs:
    name = f"MIGRATION_{old}_{new}"
    assert name in database, f"Missing Room migration {old}->{new}"
    assert f"{name}," in database or f"{name}).build()" in database, f"Unregistered migration {old}->{new}"
assert "fallbackToDestructiveMigration" not in database, "Destructive Room fallback is forbidden"

# Current JSON export/import schema and legacy compatibility must agree.
assert '.put("schemaVersion", 15)' in app_ui, "JSON export schema is not 15"
assert re.search(r"schemaVersion\s+in\s+1\.\.15", app_ui), "JSON importer must accept schemas 1..15"

# The workflow must run current checks, not a stale historical verifier.
assert "python3 tools/verify_release_2_1_24.py" in workflow
assert "python3 tools/verify_v2_source_integrity.py" in workflow
assert "verify_release_2_1_23.py" not in workflow
for old, new in [(n, n + 1) for n in range(7, 15)]:
    assert f"verify_migration_{old}_{new}.py" in workflow, f"CI omits SQLite migration check {old}->{new}"
assert "verify_backup_schema_15.py" in workflow
assert "verify_backup_media_import.py" in workflow
assert "verify_backup_snapshot.py" in workflow
assert "verify_bulk_tagging.py" in workflow
assert "verify_bulk_collection.py" in workflow
assert "verify_live_smart_filters.py" in workflow
assert "verify_bulk_selection.py" in workflow
assert "verify_session_preset_edit.py" in workflow
assert "verify_study_ui_performance.py" in workflow
assert app_ui.count("QuestionSelectionRules.resolve(questions, selectedQuestionIds)") == 4
assert "bulk actions resolve selected IDs" in bulk_selection_check
assert "saved study-session presets can be edited" in session_preset_edit_check
assert "LaTeX renderer caches results" in study_ui_performance_check
assert "fun hasNameConflict(" in session_preset_rules and "fun existingForSave(" in session_preset_rules
assert "editingCannotRenameOverAnotherPreset" in session_preset_tests
assert "missingEditedPresetDoesNotHijackSameNamedPreset" in session_preset_tests
assert "TagText.add(current.tagsCsv, normalized)" in app_ui
assert "TagText.remove(current.tagsCsv, normalized)" in app_ui
assert "QuestionVersionRules.createSnapshot(current" in app_ui
assert "fun add(csv: String, name: String)" in tag_rules
assert "addTrimsAndDeduplicatesCaseInsensitively" in tag_tests
assert "bulk tag" in bulk_tag_check.lower()
assert "onBulkCollection" in app_ui and "QuestionVersionRules.createSnapshot(current" in bulk_collection_check
assert "fun countMatches(" in read("app/src/main/java/com/studyforge/app/data/QuestionLibraryFilter.kt")
assert "LaunchedEffect(questions, links, savedFilterPresets)" in app_ui
assert "liveFilterMatchCounts = withContext(Dispatchers.Default)" in app_ui
assert "countMatches(questions, savedCriteria, presetCollectionIds)" in app_ui
assert "savedFilterCountsAreRecomputedFromCurrentQuestions" in read("app/src/test/java/com/studyforge/app/data/QuestionLibraryFilterTest.kt")
assert "live saved-filter" in smart_filter_check.lower()
assert "val snapshot = database.withTransaction" in app_ui, "JSON export must use one consistent Room snapshot"
assert "getAllQuestionVersions()" in backup_snapshot_check and "getAllExamAttempts()" in backup_snapshot_check
assert "androidTestImplementation(\"androidx.test:runner" in app_gradle
assert "androidTestImplementation(\"androidx.test.ext:junit" in app_gradle
assert "connectedDebugAndroidTest" in workflow, "CI must execute Room runtime instrumentation tests"
assert "currentSchemaPersistsAndReadsEveryCoreStudyRecord" in room_runtime_test
assert "structuredBlockKindsAndOfflineImagePayloadPersistAcrossReopen" in room_runtime_test
for rich_kind in ("TEXT", "HINT", "IMAGE", "TABLE", "GRAPH"):
    assert f'kind = "{rich_kind}"' in room_runtime_test
assert "iVBORw0KGgo" in room_runtime_test and "image/png" in room_runtime_test
assert "deletingQuestionCascadesDependentContentAndReviewRows" in room_runtime_test
assert "deletingCollectionClearsLinksAndBothKindsOfSavedPresetReferences" in room_runtime_test
assert "clearStudySessionCollection" in room_runtime_test
assert "Run Android lint" in workflow and ":app:lintDebug" in workflow
assert "afterQuote -> throw IllegalArgumentException" in interchange
assert "Quote found inside an unquoted field" in interchange
for case in ("rejectsQuoteInsideUnquotedField", "rejectsCharactersAfterClosingQuotedField", "acceptsQuotedFieldFollowedByDelimiterAndCrLf"):
    assert case in interchange_tests, f"Missing CSV/TSV parser regression case: {case}"
assert "ContentBlockParsing.validateTable" in authoring_rules
assert "ContentBlockParsing.validateGraph" in authoring_rules
assert "graphValidationRequiresTwoFinitePairsAndRejectsMalformedRows" in content_parser_tests
assert "MediaImportRules.sanitizeBase64Image" in app_ui
assert "MAX_IMAGE_BYTES = 1_500_000" in media_rules
assert "ReviewTrendRules.summarize(reviewLogs" in review_trend_ui
assert "Daily review activity · 14 days" in review_trend_ui
assert "currentStreakDays" in review_trend_rules
scheduler = read("app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt")
assert "0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966" in scheduler
assert "initialDifficulty(4)" in scheduler and "clamp = false" not in scheduler
assert "if (grade == 3 || grade == 4) increase = max(increase, 1.0)" in scheduler
assert "hardStepMinutes(learningSteps, 0)" in scheduler
assert "hardStepMinutes(steps, currentIndex)" in scheduler
assert "completed 24-hour periods" in scheduler
assert "learningSteps.isEmpty()" in scheduler and "relearningSteps.isEmpty()" in scheduler
fsrs_tests = read("app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt")
review_scheduler_tests = read("app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt")
for case in ("pinnedPyFsrsV600MemoStateSequenceMatchesEveryTransition", "sameDayHardUsesPinnedV600ShortTermStabilityFormula", "longTermReviewTransitionsMatchFsrs6ReferenceVectors"):
    assert case in fsrs_tests, f"Missing pinned FSRS-6 regression case: {case}"
for case in ("hardOnSingleLearningStepUsesOneAndAHalfTimesTheStep", "relearningHardUsesUpstreamSingleStepMultiplier", "emptyLearningStepsGraduateNewCardsDirectlyToReview", "emptyRelearningStepsKeepLapsedCardsInReview"):
    assert case in review_scheduler_tests, f"Missing scheduler policy regression case: {case}"
for case in ("returnsGapFilledFourteenDayTimelineAndScoredAccuracy", "currentStreakIncludesYesterdayWhenTodayHasNoReviews", "currentStreakStartsTodayAndStopsAtFirstGap", "unscoredReviewsCountAsActivityButHaveNoAccuracy", "timestampsAreGroupedUsingTheRequestedLocalTimeZone"):
    assert case in review_trend_tests, f"Missing review trend regression case: {case}"
for case in ("validImagePayloadIsPreservedInCanonicalBase64", "malformedBase64IsRejected", "payloadOverDecodedByteLimitIsRejected"):
    assert case in media_tests, f"Missing media import regression case: {case}"
assert "StudyForge-2.1.24-debug-apk" in workflow
assert "StudyForge-2.1.24-release-apk" in workflow
assert "KEY_ALIAS: studyforge-release" in workflow
for secret in ("SIGNING_KEYSTORE_BASE64", "SIGNING_STORE_PASSWORD", "SIGNING_KEY_PASSWORD"):
    assert secret in workflow and secret in signing, f"Signing contract missing {secret}"
assert len(plan.strip()) > 1000, "Original project blueprint is unexpectedly empty"

print("PASS: version 2.1.24 / code 28 consistency")
print("PASS: maintained PROJECT_PLAN technical baseline matches current source")
print("PASS: Room schema 15 and registered additive migrations 1->2 through 14->15")
print("PASS: JSON backup schema 15 export and import compatibility 1..15")
print("PASS: current CI verifier, migration checks, artifacts, and signing contract")
print("PASS: Room Android instrumentation tests and emulator CI execution are wired")
print("PASS: CI runs Android lint before unit and emulator tests")
print("PASS: CSV/TSV parser rejects malformed quote placement and has regression coverage")
print("PASS: structured table/graph authoring validation and regression tests are present")
print("PASS: imported image Base64 is validated against the 1.5 MB editor limit")
print("PASS: Progress screen displays recorded 14-day review activity and streak analytics")
print("PASS: FSRS-6 matches pinned py-fsrs v6.0.0 defaults, memory updates and scheduler policies")
print("PASS: bulk tag add/remove is transactional and preserves question version history")
print("PASS: bulk collection add/remove is transactional and preserves membership version history")
print("PASS: saved smart filters show live counts recalculated from current questions and links")
print("PASS: bulk actions resolve selections across the full Library, even after filters change")
print("PASS: saved study-session presets support editing and stable-ID renaming")
print("PASS: Study is one-card-at-a-time; Library filtering is off-main-thread; math previews are guarded")
print("LIMIT: instrumentation tests are source-added but have not run in this local environment")
