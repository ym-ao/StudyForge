## 2.1.9 additions and historical 2.1.8 user-facing workflow additions

- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — editor UI for TEXT/HEADING/LIST/LATEX content blocks and multipart question-part authoring; persists block/part changes in a database transaction.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — renders saved content blocks in the study card and provides saved study-session preset save/apply/delete UI.
- `app/src/main/java/com/studyforge/app/data/QuestionAuthoringRules.kt` — pure block/part normalization and part validation helpers.
- `app/src/main/java/com/studyforge/app/data/StudySessionRules.kt` — deterministic filtering for saved session criteria.
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
- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — Room entities, DAO, database, and migrations 1-to-2 through 7-to-8.
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
- `backup-example.json` — example version 8 backup structure, including tags, saved library-filter presets, structured content blocks, multipart question parts and session presets.

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


## 2.1.11 additions

- `app/src/main/java/com/studyforge/app/data/DerivationRules.kt`: DERIVATION block parser and validation.
- `app/src/test/java/com/studyforge/app/data/DerivationRulesTest.kt`: derivation parsing/validation tests.
- `app/src/test/java/com/studyforge/app/data/Fsrs6ReferenceInvariantTest.kt`: published FSRS-6 equation invariants (not full upstream vectors).
- `tools/verify_migration_8_9.py`: additive SQLite migration smoke test.
- `tools/verify_backup_schema_9.py`: schema-9 backup static validation.
