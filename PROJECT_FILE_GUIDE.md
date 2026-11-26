## V3.0 Batch 7 — lazy content and performance diagnostics

- `V3_BATCH_7_SCALABILITY.md` — implementation, host benchmark results, query-plan findings and remaining performance work.
- `tools/benchmark_v3_scalability.py` — deterministic host SQLite benchmark for 10k/50k synthetic libraries; not a substitute for Android profiling.
- `tools/verify_v3_batch7_readiness.py` — static source contract gate for lazy rich-content hydration and benchmark CI wiring.
- `StudyDatabaseRuntimeTest.richContentHydrationIsScopedToActiveQuestionAndExamSnapshot` — Room instrumentation test source; execution pending.

## 2026-11-26 V3.0 Batch 5 — Home and Progress core aggregates

- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — `HomeDashboardStats` and `ProgressCoreStats` compact Room aggregate projections.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — collects Home/Progress aggregate flows and supplies the values to destinations.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — Progress headline cards consume Room aggregate counts.
- `app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt` — aggregate projection instrumentation test source.
- `tools/verify_v3_batch5_readiness.py` — 10-check static readiness gate, wired into `.github/workflows/android-apk.yml` and `verify_v2_source_integrity.py`.
- `V3_BATCH_5_HOME_PROGRESS_AGGREGATES.md`, `VERSION_3_DELIVERY_PLAN.md`, `IMPLEMENTATION_STATUS.md`, `BUILD_STATUS.md`, `DEVELOPMENT_LOG.md`, and `HANDOFF_FOR_NEW_CHAT.md` — scope and verification boundary.

## 2026-11-25 FSRS-6 compatibility correction

- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt` — aligned with pinned py-fsrs v6.0.0 commit `352c7a2` including defaults, same-day updates, Hard learning/relearning delays and elapsed-day semantics.
- `app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt` and `Fsrs6ReferenceInvariantTest.kt` — updated/expanded scheduler regressions; 32 tests compiled/executed with a lightweight local JUnit-compatible runner (not Gradle/JUnit).
- `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py` — executable exact-source Kotlin comparison against the pinned fixture plus focused policy probes.
- `BUILD_STATUS.md` and `HANDOFF_FOR_NEW_CHAT.md` — current verification boundary and remaining Android/CI/device acceptance gates.

## 2026-11-25 continued source-vs-plan audit

- `SOURCE_PLAN_AUDIT_2.1.24.md` — records the current metadata audit, corrected plan baseline and reproduced FSRS v6.0.0 sequence mismatch.
- `PROJECT_PLAN.md` — maintained technical baseline now matches Room schema 15, JSON schema 15 and registered migrations through 14→15.
- `tools/verify_v2_source_integrity.py` — additionally prevents the maintained plan metadata from drifting from source.

## 2026-11-25 V2 preparation additions

- `tools/verify_v2_source_integrity.py` — static cross-check of current release metadata, Room migration registration, JSON backup schema compatibility, CI steps and signing configuration. It does not execute Android/Room/backup runtime behavior.
- `.github/workflows/android-apk.yml` — now calls `verify_release_2_1_24.py` (instead of the stale 2.1.23 verifier) and the source-integrity gate.
- `Fsrs6ReferenceInvariantTest.kt` — includes a same-day Hard stability-floor regression case.

## Continued 2.1.24 verification

- `SOURCE_PLAN_AUDIT_2.1.24.md` — source-vs-plan audit and outstanding V1/V2 acceptance gates.
- `app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt` — includes initial-state equation assertions for all four ratings in addition to existing long-term and invariant coverage.
- `BUILD_STATUS.md` — records the exact local checks and explicitly unverified Android/CI/signing/device gates.


## 2.1.24 additions

- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt`: corrects FSRS-6 long-term update ordering and the 0.001 stability floor.
- `app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt`: adds four-rating long-term equation fixtures and a stability-floor regression.
- `tools/verify_release_2_1_24.py`: checks release metadata, workflow artifact labels and FSRS regression coverage.
- Room and JSON backup remain schema 15; no migration is introduced.

## 2.1.22 additions

- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt`: first-review Good advances to the next configured learning step; first-review Hard enters Learning at step 0.
- `app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt`: regression coverage for the new-card Good/Hard transitions.
- `tools/verify_release_2_1_22.py`: static source/version/schema/workflow consistency check.
- Room and JSON backup remain schema 15; no migration is introduced.

## 2.1.21 additions

- `app/src/main/java/com/studyforge/app/data/QuestionVersionRules.kt`: stable-ID-aware recursive snapshot comparison; embedded media is summarized rather than printed.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt`: version-history UI shows before/current snippets for changed fields.
- `app/src/test/java/com/studyforge/app/data/QuestionVersionRulesTest.kt`: focused diff tests.
- `tools/verify_release_2_1_21.py`: static release consistency check.
- Room and JSON backup remain schema 15; no migration is introduced.

## 2.1.20 additions

- `QuestionLibraryFilter.kt` — independent source title/reference and created/modified date-range matching.
- `StudyDatabase.kt` — Room schema 15; migrations 13→14 and 14→15 extend saved filter criteria without destructive changes.
- `QuestionTypeAnalytics.kt` — accuracy aggregation from scored review logs by question type.
- `StudyForgeApp.kt` — source/date filter UI, saved-preset application and schema-15 JSON backup fields; question version history displays a core-field change summary.
- `StudyScreens.kt` — progress UI includes accuracy by question type.
- `QuestionLibraryFilterTest.kt`, `SavedFilterPresetTest.kt`, `QuestionTypeAnalyticsTest.kt` — focused regression test source.
- `tools/verify_migration_13_14.py`, `verify_migration_14_15.py`, `verify_backup_schema_15.py`, `verify_release_2_1_20.py` — local migration/backup/release checks.

## 2.1.19 additions

- `QuestionEntity.sourceTitle` and `sourceDetail` store question provenance; migration 12→13 adds both columns non-destructively.
- `QuestionInterchange.previewCsv` and `previewAnkiTsv` classify new/update/duplicate/invalid rows before import.
- `StudyForgeApp.kt` presents the import preview and confirmation, edits provenance, and includes provenance in JSON schema 13 backup and CSV interchange.
- `QuestionVersionRules.kt` includes provenance in content snapshots and restores.
- `tools/verify_migration_12_13.py`, `verify_backup_schema_13.py`, and `verify_release_2_1_19.py` validate the new migration, backup compatibility and source consistency.

## 2.1.9 additions and historical 2.1.8 user-facing workflow additions

- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — editor UI for TEXT/HEADING/LIST/LATEX content blocks and multipart question-part authoring; persists block/part changes in a database transaction.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — renders saved content blocks in the study card and provides saved study-session preset save/apply/edit/delete UI with rename collision checks and delete confirmation.
- `app/src/main/java/com/studyforge/app/data/QuestionAuthoringRules.kt` — pure block/part normalization and part validation helpers.
- `app/src/main/java/com/studyforge/app/data/StudySessionRules.kt` — deterministic filtering for saved session criteria.
- `app/src/main/java/com/studyforge/app/data/StudySessionPresetRules.kt` — stable-ID save targeting and case-insensitive rename-conflict checks for saved sessions.
- `app/src/test/java/com/studyforge/app/data/QuestionAuthoringRulesTest.kt` and `StudySessionRulesTest.kt` — regression test source (Android JUnit execution pending).

# Project file guide

## 2.1.7 data-model additions

- `QuestionContentBlockEntity` — ordered rich-content block storage, including optional embedded media fields.
- `QuestionPartEntity` — ordered multipart question storage with point values.
- `StudySessionPresetEntity` — saved study selection criteria.
- These data structures are not yet connected to complete editor, exam grading or session-launcher UI.

- `tools/verify_migration_7_8.py` — extracts and smoke-tests the exact 7→8 SQLite migration SQL.

## Android source

- `app/src/main/java/com/studyforge/app/MainActivity.kt` — Android activity entry point.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — Compose app shell, navigation, library/editor/collections, backup and restore, home UI.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — study modes, configurable exam UI, and progress UI.
- `app/src/main/java/com/studyforge/app/ui/SettingsScreen.kt` — persistent FSRS review and exam settings UI.
- `app/src/main/java/com/studyforge/app/ui/StudyContent.kt` — native LaTeX view and Markdown-like rich-content renderer.
- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — Room entities, DAO, database, and migrations 1-to-2 through 14-to-15.
- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt` — FSRS-6 scheduler implementation and review recording.
- `app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt` — scheduler unit tests.

## Build and app configuration

- `app/build.gradle.kts` — Android app config and dependencies.
- `build.gradle.kts` — plugin versions.
- `settings.gradle.kts` — plugin/dependency repositories and included module.
- `gradle.properties` — Gradle properties.
- `.github/workflows/android-apk.yml` — GitHub Actions unit-test and APK build workflow.
- `app/src/main/AndroidManifest.xml` — app manifest.

## Planning and continuity records

- `PROJECT_PLAN.md` — current high-level product/release plan.
- `StudyForge_Plan.txt` — preserved original detailed blueprint.
- `IMPLEMENTATION_STATUS.md` — source-vs-plan feature audit.
- `ROADMAP.md` — upcoming work and milestone status.
- `CHANGELOG.md` — release history.
- `DEVELOPMENT_LOG.md` — dated technical history.
- `BUILD_STATUS.md` — build/test verification status.
- `HANDOFF_FOR_NEW_CHAT.md` — prompt for a fresh conversation.
- `PROJECT_FILE_GUIDE.md` — this source inventory.
- `backup-example.json` — current schema-15 backup structure, including tags, saved library-filter presets, structured content blocks, multipart question parts, session presets and exam attempts.

- `SIGNING_SETUP.md` — secure release-signing setup instructions.


## Signing baseline retained from 2.1.1

`SIGNING_SETUP.md` documents the three required GitHub Actions secrets. The non-secret alias `studyforge-release` is fixed in `.github/workflows/android-apk.yml` and validated before Gradle release packaging.


- `THIRD_PARTY_NOTICES.md` — RaTeX MIT attribution and FSRS algorithm reference.


## 2.1.3 additions

- `app/src/main/java/com/studyforge/app/data/QuestionLibraryFilter.kt` — reusable pure predicate for search and library filter behavior.
- `app/src/test/java/com/studyforge/app/data/QuestionLibraryFilterTest.kt` — focused filter regression tests.
- `StudyDatabase.kt` — additive 3→4 migration adds editorial difficulty and exam labels.


## 2.1.4 additions

- `ReviewScheduler.kt` — ordered multi-step Learning/Relearning sequence and step-position updates.
- `ReviewSchedulerTest.kt` — source tests for sequence advancement, repetition, restart, graduation, and lapse behavior.
- `StudyDatabase.kt` — Room v5, additive migration 4→5 for `learningStepIndex`.
- `backup-example.json` — schema 5 example including `learningStepIndex`.


## 2.1.7 additions

- Structured content block, multipart part, and saved study-session preset entities are defined in `StudyDatabase.kt`.
- Room migration 7→8 and JSON schema 8 backup code are included.
- Complete user-facing editor, grading and session workflows remain unfinished.

## 2.1.6 additions (historical)

- `TagText.kt` — pure Kotlin tag parsing, deduplication, rename and removal helpers.
- `TagTextTest.kt` — tests for exact tag operations and normalization.
- `StudyDatabase.kt` — normalized tag catalogue and migration 6→7.
- `StudyForgeApp.kt` — tag manager UI and schema-7 backup import/export.
- `backup-example.json` — schema 7 example including the tag catalogue.

## 2.1.5 additions

- `StudyDatabase.kt` — `SavedFilterPresetEntity`, saved-preset DAO operations, and additive Room migration 5→6.
- `StudyForgeApp.kt` — save/apply/manage library filter presets and schema-6 JSON import/export.
- `SavedFilterPresetTest.kt` — tests that the complete filter criteria survive conversion to/from a persisted preset.
- `backup-example.json` — schema 7 example containing saved presets and a tag catalogue.


## 2.1.9 additions

- `app/src/main/java/com/studyforge/app/data/ContentBlockParsing.kt` — bounded pure parsers for pipe-separated table rows and numeric x,y graph points.
- `app/src/test/java/com/studyforge/app/data/ContentBlockParsingTest.kt` — five JUnit test cases for table/graph parsing limits and invalid data.
- `app/src/main/java/com/studyforge/app/ui/StudyContent.kt` — offline embedded-image renderer, table preview and graph Canvas preview.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — Android document picker and structured block authoring controls.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — study-card rendering for IMAGE, TABLE and GRAPH blocks.


## 2.1.10 additions

- `app/src/main/java/com/studyforge/app/data/MultipartExamRules.kt` — deterministic weighted per-part scoring and normalized exact-answer matching.
- `app/src/test/java/com/studyforge/app/data/MultipartExamRulesTest.kt` — five regression test cases for weighted points, normalized text matching, unanswered parts, wrong answers and invalid point values.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — exam mode now presents multipart questions, captures each part answer, supports question navigation, and shows weighted per-part and total results.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — passes persisted question parts into StudyScreen.


## 2.1.12 build fixes

- Removed duplicate Compose `FontWeight` import.
- Declared editor error state before image-picker callback use.
- No data schema changes.

## 2.1.11 additions

- `app/src/main/java/com/studyforge/app/data/DerivationRules.kt`: DERIVATION block parser and validation.
- `app/src/test/java/com/studyforge/app/data/DerivationRulesTest.kt`: derivation parsing/validation tests.
- `app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt`: published FSRS-6 equation invariants (not full upstream vectors).
- `tools/verify_migration_8_9.py`: additive SQLite migration smoke test.
- `tools/verify_backup_schema_10.py`: historical schema-12 backup static validation (supersedes the historical schema-9 check).


## 2.1.14 update

`app/src/main/java/com/studyforge/app/data/MultipartExamRules.kt` now supports bounded manual per-part awards; `ui/StudyScreens.kt` exposes examiner scoring on exam results and records review outcomes only after final grading. `MultipartExamRulesTest.kt` includes manual-award test cases. No schema/backup change.


## 2.1.15 additions

- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — `ExamAttemptEntity`, attempt DAO methods, and additive Room migration 9→10. The attempt table intentionally has no FK to questions so history survives question deletion.
- `app/src/main/java/com/studyforge/app/data/ExamAttemptRules.kt` — bounded weighted-score percentage and average/best score helpers.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — saves timed exam snapshots and displays exam history/aggregate scores in Progress.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — observes attempt history and exports/imports it in JSON backup schema 12.
- `app/src/test/java/com/studyforge/app/data/ExamAttemptRulesTest.kt` — percentage and aggregate-score test source.
- `tools/verify_migration_9_10.py` — SQLite regression check for the additive attempt migration and history preservation.
- `tools/verify_backup_schema_10.py` — schema-12 backup fixture check.
- `backup-example.json` — schema 10 example including an empty `examAttempts` array.


## 2.1.16 additions

- `app/src/main/java/com/studyforge/app/data/TextFormattingRules.kt` — pure selection-aware Markdown formatting and selection-range calculation.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — reusable formatting field connected to question and multipart authoring fields; app/backup descriptions updated.
- `app/src/main/java/com/studyforge/app/data/ExamAttemptRules.kt` — median, recent trend and score-band analytics.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — median, score distribution and recent exam trend display.
- `app/src/test/java/com/studyforge/app/data/TextFormattingRulesTest.kt`, `ExamAttemptRulesTest.kt`, and `Fsrs6ReferenceInvariantTest.kt` — expanded regression test source; Android JUnit execution pending.
- `app/build.gradle.kts` and `.github/workflows/android-apk.yml` — version 2.1.16 / versionCode 21 and matching artifact labels.
- `tools/verify_release_2_1_16.py` — source-level release consistency checks, run by GitHub Actions before the Android unit tests.
- Room schema 10, JSON backup schema 12 and all registered migrations are unchanged.


## 2.1.18 additions

- `QuestionInterchange.kt` — CSV and Anki TSV export/import for basic question fields.
- `ClozeRules.kt` — Anki-style cloze masking and answer extraction.
- `ManualScheduleRules.kt` — manual due-date and scheduler-reset transformations.
- `QuestionVersionRules.kt` — content-only version snapshot and restore; preserves scheduler/review fields.
- `StudyDatabase.kt` — Room schema 12, collection-aware saved sessions and question version-history table; additive migrations 10→11 and 11→12.
- `StudyForgeApp.kt` — CSV/Anki import/export, bulk status/difficulty editing, manual schedule menu, version history/restore and schema-12 backup integration.
- `StudyScreens.kt` — collection-aware saved sessions, True/False/Numerical/Cloze study paths and timed Numerical/True-False questions.
- `StudyContent.kt` — Markdown-like renderer for bold, italic, underline, highlight, inline code, headings and lists.
- `QuestionInterchangeTest.kt`, `ClozeRulesTest.kt`, `ManualScheduleRulesTest.kt` — new unit-test source; full Android JUnit not yet executed.
- `tools/verify_migration_10_11.py`, `tools/verify_migration_11_12.py`, `tools/verify_backup_schema_12.py`, `tools/verify_release_2_1_18.py` — focused local/CI source checks.
- `backup-example.json` — schema 12 with `questionVersions` and collection-aware study-session fields.


## 2.1.20 additions
- `app/src/main/java/com/studyforge/app/data/QuestionTypeAnalytics.kt`: scored-review accuracy aggregation by question type.
- `tools/verify_migration_13_14.py` and `tools/verify_migration_14_15.py`: additive SQLite migration checks.
- `tools/verify_backup_schema_15.py` and `tools/verify_release_2_1_20.py`: backup and release source consistency checks.


## 2.1.23 — learner-revealed hint blocks

- Added `HINT` as a supported structured content-block kind. Authors can add, edit, reorder and delete hint blocks in the question editor; existing content-block storage, question snapshots and JSON backup paths are reused.
- During study, hint content is hidden behind a **Reveal hint** action and is displayed only after the learner chooses to reveal it. Hint reveal state is scoped to the current question in the active screen; it is not persisted as a review event.
- No Room migration or JSON backup schema change: both remain version 15. Existing backups and unknown/older block kinds retain their prior behavior.
- Added static release consistency checks and updated GitHub Actions artifact labels to 2.1.23.
- Verification in this package: Python source-consistency and migration/backup smoke scripts are run locally and recorded in `BUILD_STATUS.md`. Android Gradle/JUnit, Room runtime, CI, signing and device acceptance are not claimed unless separately evidenced.


## 2.1.24 — FSRS-6 reference-equation correction

- Corrected long-term review transitions to calculate new stability from the pre-review difficulty, then calculate the updated difficulty separately from that same prior state, matching the py-fsrs FSRS-6 update order.
- Corrected the FSRS memory-state lower bound from 0.1 to 0.001 in stability/retrievability/interval calculations, consistent with the upstream FSRS-6 reference implementation.
- Added four-rating long-term reference-equation regression fixtures for Again, Hard, Good and Easy (S=10, D=5, elapsed=5 study days, desired retention 0.90, default 21 weights), plus a lower-bound case. Fixtures follow py-fsrs v6.0.0 equations; this does not certify full scheduler/API parity or StudyForge's distinct learning-step policy.
- App version 2.1.24 / versionCode 28. Room schema 15 and JSON backup schema 15 remain unchanged; migrations 1→2 through 14→15 and backup imports 1–15 are retained.
- Existing HINT blocks and all previously implemented features remain in source. Original `StudyForge_Plan.txt` and signing identity are preserved.
- Android Gradle/JUnit, Room runtime, app backup/restore, GitHub Actions, signed APK and physical-device verification are not claimed without external evidence.

### Android runtime tests

`app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt` contains on-device/emulator Room tests for current-schema persistence and foreign-key cascade behavior. `.github/workflows/android-apk.yml` runs `:app:connectedDebugAndroidTest` on an API 35 emulator. These tests are distinct from the Python static SQLite migration checks and do not yet cover populated historical migrations or the complete Android backup/restore UI flow.



### Android runtime verification

`app/src/androidTest/.../StudyDatabaseRuntimeTest.kt` contains Android SQLite/Room runtime checks. `.github/workflows/android-apk.yml` runs `:app:lintDebug`, `:app:testDebugUnitTest`, and `:app:connectedDebugAndroidTest`. A source file or workflow step is not a passed test; successful CI logs are required.


## 2026-11-25 continuation files

- `app/src/main/java/com/studyforge/app/data/MediaImportRules.kt` validates imported image Base64 and enforces the 1.5 MB decoded image limit already used by the editor.
- `app/src/test/java/com/studyforge/app/data/MediaImportRulesTest.kt` contains five JUnit regression cases (source added; Android JUnit not yet run).
- `tools/verify_backup_media_import.py` checks that backup import uses the validator and that its boundary tests remain present; CI runs this static contract check.

- `app/src/main/java/com/studyforge/app/data/ReviewTrendRules.kt` builds the gap-filled 14-day review timeline and streak from persisted review logs. `ReviewTrendRulesTest.kt` covers dates, accuracy, streaks and timezone bucketing (JUnit source added; not run locally).
- `tools/verify_backup_snapshot.py`: static contract check that all 12 JSON-export data sources are captured in one Room transaction.
- `tools/verify_bulk_tagging.py`: static contract check for transactional bulk tag add/remove and pre-change question-version snapshots.
- `tools/verify_bulk_collection.py`: static contract check for transactional bulk collection membership add/remove and pre-change question-version snapshots.

- `app/src/main/java/com/studyforge/app/data/QuestionLibraryFilter.kt`: shared Library criteria matching and live saved-filter match counts.
- `app/src/test/java/com/studyforge/app/data/QuestionLibraryFilterTest.kt`: saved-filter live-count regression cases (JUnit source; not yet executed in Android).
- `tools/verify_live_smart_filters.py`: static source-contract check for current-data saved-filter counts; CI runs this check.



## 2026-11-25 Library selection consistency

- `app/src/main/java/com/studyforge/app/data/QuestionSelectionRules.kt` — resolves selected question IDs against the complete Library while preserving Library order and ignoring deleted IDs.
- `app/src/test/java/com/studyforge/app/data/QuestionSelectionRulesTest.kt` — regression test source for selection resolution.
- `tools/verify_bulk_selection.py` — static check that all four bulk action paths use the full selected set.


## 2026-11-25 rich-content persistence test addition

- `app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt` — current-schema Room reopen test for ordered TEXT/HINT/IMAGE/TABLE/GRAPH blocks and embedded image metadata.
- `tools/verify_v2_source_integrity.py` — now asserts that the rich-content runtime test and its key fixtures remain present.
- `BUILD_STATUS.md` — records that this is test source only until Android emulator CI executes it.

## 2026-11-26 V3.0 Batch 2 production Library Paging

- `app/src/main/java/com/studyforge/app/ui/PagedLibraryScreen.kt` — active Library destination; collects Paging 3 `LazyPagingItems<QuestionLibraryRow>`, renders bounded lightweight rows, manages database-backed filters/presets/tag CRUD, and retains ID-based selection across loaded pages.
- `app/src/main/java/com/studyforge/app/viewmodel/LibraryViewModel.kt` — owns Library criteria, cancels stale paging flows with `flatMapLatest`, and exposes Room-backed filter/tag counts.
- `app/src/main/java/com/studyforge/app/data/QuestionRepository.kt` — exposes paged Library data, matching count queries, tag usage counts, and bounded Study queue snapshot APIs.
- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — lightweight `QuestionLibraryRow`, Room PagingSource/count SQL and tag-specific lookup/count queries.
- `app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt` — Room runtime test source for paging order/filter/count/invalidation, tag substring matching, and stable Study queue snapshots; not yet executed in Android runtime.
- `tools/verify_v3_batch2_readiness.py` — static production-Library integration gate; CI now runs it after the paging foundation verifier.
- `tools/verify_study_ui_performance.py`, `tools/verify_live_smart_filters.py`, `tools/verify_bulk_tagging.py`, and `tools/verify_bulk_collection.py` — updated static contracts for the active paged Library implementation.
- `V3_BATCH_2_LIBRARY_MIGRATION_AUDIT.md`, `VERSION_3_DELIVERY_PLAN.md`, `BUILD_STATUS.md`, `IMPLEMENTATION_STATUS.md`, `DEVELOPMENT_LOG.md`, and `HANDOFF_FOR_NEW_CHAT.md` — record the source changes, verification boundary and remaining V3 work.
