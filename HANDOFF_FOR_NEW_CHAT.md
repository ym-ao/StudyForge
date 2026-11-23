# Continue StudyForge in a new ChatGPT conversation

Upload the latest complete StudyForge source ZIP to the new conversation, then paste the prompt below. This ZIP is the source of truth for the included code, current logs, original project plan, and release handoff.

## Copy-paste prompt

```text
I'm continuing development of my Android application, StudyForge. The uploaded ZIP is the latest complete source package.

First, inspect the entire ZIP before changing anything.

1. Read PROJECT_PLAN.md, StudyForge_Plan.txt, IMPLEMENTATION_STATUS.md, CHANGELOG.md, ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, README.md, PROJECT_FILE_GUIDE.md, SIGNING_SETUP.md, THIRD_PARTY_NOTICES.md, and this handoff file.
2. Inspect the actual Kotlin source, Gradle configuration, Room entities/DAO/migrations, unit tests, backup import/export, manifest, and GitHub Actions workflow. Do not trust documentation in place of source inspection.
3. Confirm app version/versionCode, Room version, JSON backup version, migration chain, current feature status, and actual CI/device evidence.
4. The current source is StudyForge 2.1.12 (versionCode 17), Room schema 9, JSON backup schema 9. Version 2.1.7 added persistence/backup data models for ordered rich-content blocks, multipart question parts with point values, and saved study-session presets through additive migration 7→8. Version 2.1.8 connects TEXT/HEADING/LIST/LATEX blocks to editor CRUD and study-card rendering; adds multipart part authoring for prompt/answer/explanation/points (not exam grading); and adds basic session preset save/apply/delete UI that currently captures mode, selected topic and question limit. Image/media selection with offline rendering and basic table/graph blocks are source-implemented in 2.1.9. StudyForge 2.1.10 adds multipart timed exam presentation, per-part answer capture and weighted all-or-nothing per-part scoring. StudyForge 2.1.11 adds full saved-session criteria, stepwise DERIVATION blocks, numeric tolerances and accepted alternatives for multipart grading, expanded recorded-data analytics, and FSRS-6 equation-invariant tests. Manual within-part partial credit and whole-exam attempt analytics remain incomplete. It retains FSRS-6, persisted settings, study modes, configurable MCQ exams, LaTeX integration, editorial metadata, filters, saved filter presets and tag management. The 2.1.4 feature adds persisted ordered multi-step Learning/Relearning progression and visible step position. The 2.1.5 feature adds reusable library-filter presets. The 2.1.6 feature adds a Library tag manager and normalized tag catalogue. None should be called CI-verified or device-verified unless real logs prove it.
5. Do not silently remove, simplify, replace, or forget any approved requirement in the original five-major-version plan. The original detailed blueprint `StudyForge_Plan.txt` is authoritative. Incomplete items stay as outstanding requirements. Changes to scope require my explicit approval.
6. Version 1/2 are still not fully complete. The 2.1.7 source added structured block, multipart-part and study-session preset data models plus schema-8 backup support. The 2.1.8 source adds user-facing TEXT/HEADING/LIST/LATEX block authoring and study-card rendering, multipart part authoring (not exam grading), and basic study-session preset save/apply/delete UI. The 2.1.9 source adds Android image picking with embedded offline rendering and table/graph content block authoring/preview. Multipart timed exam capture and weighted per-part scoring are now source-implemented in 2.1.10; advanced within-part partial credit and persisted whole-exam attempt analytics remain incomplete. Saved filter presets and a dedicated tag manager are present in source. Official FSRS reference-vector validation and full CI/device verification are still pending. See the current implementation matrix and roadmap.
7. Before data-model changes, inspect Room migrations, stable question IDs, collection links, review history and backup schema. Prefer reusing the existing schema-8 mediaBase64/mediaMimeType/altText fields when possible; 2.1.9 embeds selected image bytes in those fields and the existing backup path. Never use destructive fallback migrations or silently discard data. Add migrations/tests for required changes.
8. Add tests for new algorithm and database features. For FSRS, compare behavior to upstream reference vectors as well as app-specific cases. Never say tests passed or APK built without actual successful evidence.
9. Keep the Android phone + Termux + GitHub Actions build workflow practical. This project must remain manageable without a desktop computer.
10. Preserve the exact private signing key. The workflow's documented alias is the non-secret constant `studyforge-release`. Required GitHub Actions secrets are SIGNING_KEYSTORE_BASE64, SIGNING_STORE_PASSWORD, and SIGNING_KEY_PASSWORD. Do not regenerate, print, commit, or include the private key or credentials in the ZIP.
11. For every meaningful development session and every release, update relevant logs. Every release must append to CHANGELOG.md and update ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, IMPLEMENTATION_STATUS.md, README.md, PROJECT_FILE_GUIDE.md and this handoff where needed. Keep historical records; do not erase old entries.
12. Keep versionName/versionCode and workflow artifact naming consistent. Increment versionCode on each new APK release.
13. Deliver a complete self-contained source ZIP with source, workflow, original plan, current plan, current implementation audit, logs, build status, signing setup, notices, backup example, and new-chat handoff.
14. Clearly separate source implementation, locally tested, CI-tested, signed APK verified, and tested on my Android device. Do not claim build success until I provide or you obtain actual GitHub Actions evidence.

Start with a source-vs-plan audit and identify any mismatch before continuing the next planned V1/V2 release. Preserve the existing key identity and all user data. If the next step is clear, proceed with the best safe implementation and package the full updated ZIP; do not ask me to repeat information already in the files.
```

## Historical 2.1.2 source snapshot (retained)

- App: 2.1.2 / versionCode 7 (historical).
- Room: schema version 3; migrations 1→2 and 2→3 (historical). Version 2→3 adds FSRS stability/difficulty columns and the `study_settings` table.
- Backup: schema version 3; imports schemas 1, 2, and 3 (historical). Schema 3 includes FSRS memory state and settings. Older backup imports are supported; matching existing FSRS values are retained for schema 1/2 imports.
- Scheduler: source implementation of FSRS-6 with the published default 21 parameters and whole local study-day elapsed intervals (calendar-day difference in the device time zone). A standalone Kotlin smoke test passed, but it has not yet been validated with upstream reference vectors or in CI.
- Settings: desired retention, maximum interval, daily new/review limits, review order, retry delays, exam question count/duration/randomization.
- Study modes: Due, All, Practice, Random, Ordered, Topic, Weakest, basic Step-by-step, Exam.
- Library: type/status/subject/chapter/topic/tag/collection filters; search includes answer/explanation.
- LaTeX: native RaTeX view integrated for the equation field; offline/device rendering not verified.
- Rich content: Markdown-like preview only. Full structured editor with media/tables/graphs remains outstanding.
- CI and device verification for 2.1.2 were not recorded in that release snapshot.
- Signing: keep the existing `.jks` private and use the same alias/key for all updates. Three GitHub secrets are documented in `SIGNING_SETUP.md`.

## Next unfinished approved work

Continue V1/V2 gaps before starting Version 3: image/media/table/graph blocks, selection-aware editing, structured derivations, full custom-session criteria editing, multipart exam presentation and weighted partial-credit grading, deeper analytics, FSRS reference-vector validation, database/backup tests, and full CI/device acceptance. Tag management, saved filter presets, basic content-block authoring, multipart part authoring and basic saved sessions are source-implemented but need runtime/device verification. The V3/V4/V5 roadmap remains unchanged.

## Package rules

- `StudyForge_Plan.txt` is the preserved original blueprint and must not be edited to reduce scope.
- `PROJECT_PLAN.md` is the current master plan.
- `IMPLEMENTATION_STATUS.md` records source-vs-plan and verification status.
- `CHANGELOG.md` is append-only.
- `BUILD_STATUS.md` records only actual build/test evidence.
- Export and verify a backup before installing an APK.
- Never uninstall the app as a routine update step; if there is a signature mismatch, investigate whether the installed version is debug-signed before performing the one-time transition.
- Never include passwords, signing key material, GitHub tokens, or AI secrets in the ZIP.


## Historical 2.1.5 release state

- App version 2.1.5 / versionCode 10.
- Room database version 6 (migrations 1→2→3→4→5→6); 4→5 adds `learningStepIndex`; 5→6 creates `saved_filter_presets` and its collection index.
- JSON backup schema 6; imports versions 1, 2, 3, 4, 5, and 6. Older backup merges preserve current FSRS state and newer metadata when absent from the old schema.
- New source features from 2.1.3: editorial difficulty and exam/syllabus fields, formatting shortcuts, library filters, timed-exam question navigation and difficulty result summaries. 2.1.4 adds multi-step Learning/Relearning, saved step position, and a current-step study cue. 2.1.5 adds saved library-filter presets.
- Added `QuestionLibraryFilterTest` and `SavedFilterPresetTest` sources; do not claim Android JUnit tests have passed until GitHub Actions actually runs them.
- Remaining V1/V2 requirements include full structured/media editor, dedicated tag manager, structured derivations, custom study-session builder, multipart/partial-credit exams, deeper analytics, FSRS reference-vector validation, and full runtime/CI/device checks. Do not remove these.
- Current build, tests, migration runtime, backup round trip, signed APK, and device behavior are not verified.
- Preserve the existing 2.1.1 signing workflow behavior and exact release key identity. The workflow expects three secrets: `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, and `SIGNING_KEY_PASSWORD`; alias `studyforge-release` is fixed in workflow.


## 2.1.4 scheduler behavior and verification boundary (historical feature retained)

- Learning and relearning delay settings are ordered comma-separated minute values. Repeated values are valid separate steps.
- Again on a new question enters Learning at step 0; Again on a graduated Review question enters Relearning at step 0 and increments lapse count once.
- Good advances to the next step, Hard repeats the current step, Again restarts the sequence, and Easy or final Good graduates to Review.
- `learningStepIndex` is stored on the question, persisted in review updates, and included in schema-5 backups. Older backup schemas keep the existing step index when merging into a matching existing question ID.
- A standalone Kotlin smoke harness compiled the scheduler source and passed 22 assertions. SQLite 4→5 migration SQL smoke check passed on a populated table. These do not replace Android JUnit, Room migration runtime, CI, signature or device verification.


## Historical 2.1.5 saved-filter preset behavior and verification boundary

- Every current LibraryFilterCriteria property is stored as a dedicated Room column in `SavedFilterPresetEntity`; no criteria are omitted from a preset.
- The Library screen saves the current query and filters, reapplies presets from chips, and updates an existing preset when the supplied name matches case-insensitively.
- Manage/delete requires a separate confirmation dialog.
- Presets optionally reference a collection through a Room foreign key with `ON DELETE SET NULL`; the preset survives collection deletion.
- Schema-6 backups include `savedFilterPresets`. Import accepts schemas 1–6; old schema imports do not delete existing local presets. Presets with collection IDs missing from the merged database are imported with `collectionId = null`.
- Added `SavedFilterPresetTest` source; Android JUnit has not been run. Room runtime, backup round-trip, CI, signing, and device checks remain pending.

## Historical 2.1.5 release state

- App 2.1.5 / versionCode 10; Room 6; JSON backup 6.
- New Room table is created by migration 5→6; full migration chain remains registered.
- Historical workflow artifact names were `StudyForge-2.1.5-debug-apk` and `StudyForge-2.1.5-release-apk`.
- At the 2.1.5 release point, remaining V1/V2 work included the structured rich editor, tag manager/CRUD, structured derivations, custom sessions, multipart/partial-credit exams, broader analytics, official FSRS reference vectors, and CI/device acceptance. The tag manager is now source-implemented in 2.1.6.


## Historical 2.1.6 release state

- App 2.1.6 / versionCode 11; Room 7; JSON backup 7.
- Room migrations 1→2→3→4→5→6→7; migration 6→7 creates `tags` and a unique name index without rebuilding question/review/settings/preset tables.
- `TagEntity` is a normalized tag catalogue; legacy question `tagsCsv` remains in place. A question Flow observer discovers tags and upserts catalogue entries.
- Library tag manager supports create, rename, delete and usage counts. Rename/delete update exact case-insensitive matches in all affected question strings inside a Room transaction.
- `TagText.kt` contains pure helpers for parsing, deduplication, rename and removal; `TagTextTest.kt` adds five JUnit test cases. Android JUnit has not been executed.
- JSON schema 7 exports/imports the tag catalogue. Imports schemas 1–6 remain supported; old backup tags are recovered from question tag strings.
- Current source is not Gradle/CI/device verified. Room runtime migration, backup round-trip, signed APK and physical device checks are pending.
- Workflow artifacts are `StudyForge-2.1.6-debug-apk` and `StudyForge-2.1.6-release-apk`. Preserve the existing private signing key and fixed alias `studyforge-release`.


## Historical 2.1.8 release state

- App version 2.1.8 / versionCode 13; Room schema 8; JSON backup schema 8.
- No schema change was required. Existing DAO tables are now connected to UI for ordered TEXT/HEADING/LIST/LATEX blocks, multipart part authoring, and basic saved study-session presets.
- Study card renders saved text/heading/list/LaTeX blocks. Multipart exam flow/partial-credit grading is not implemented. Session UI currently saves mode, topic and question limit only.
- Added `QuestionAuthoringRules` and `StudySessionRules` plus JUnit test source; Android JUnit is not run.
- No Gradle/Kotlin compiler/Android SDK was available. Full compile, CI, signed APK, Room runtime, backup round-trip and device checks remain pending.
- Original `StudyForge_Plan.txt` remains byte-for-byte unchanged. Preserve all V1/V2/V3/V4/V5 requirements.

## Historical 2.1.7 release state

- App version 2.1.7 / versionCode 12; Room schema 8; JSON backup schema 8 (imports schemas 1–8).
- Migration 7→8 creates `question_content_blocks`, `question_parts`, and `study_session_presets`. Existing question/review/collection/settings/tag/filter data is not rebuilt.
- JSON schema 8 adds `contentBlocks`, `questionParts`, and `studySessionPresets`.
- These are persistence/interchange foundations only. Complete structured editor UI, media selection/rendering, multipart authoring and partial-credit exam grading, and custom-session manager/launcher are still outstanding.
- `tools/verify_migration_7_8.py` extracted the exact migration SQL and passed 7 SQLite checks. Full Android build, Room runtime migration, backup round-trip, GitHub Actions, signed APK and device verification are pending.
- Original `StudyForge_Plan.txt` remains byte-for-byte unchanged. Preserve all V1/V2/V3/V4/V5 requirements.


## Current 2.1.9 release state

- App version 2.1.9 / versionCode 14; Room schema 8; JSON backup schema 8.
- Built from the uploaded 2.1.8 ZIP after following its handoff and inspecting source.
- IMAGE blocks now use Android OpenDocument picker. Selected image bytes are copied into the existing `mediaBase64` field (maximum 1.5 MB raw bytes), with MIME type and alt text. Embedded bytes render offline in editor and study card. Existing schema-8 JSON export/import already includes these fields; no migration/schema change was needed.
- TABLE blocks use pipe-separated cells and newline-separated rows; preview capped at 30 rows × 8 columns. GRAPH blocks use numeric x,y pairs separated by comma, semicolon or tab; simple connected Canvas preview capped at 200 points.
- Added `ContentBlockParsing.kt` and five JUnit test sources. Tests have not been executed.
- Full Android build, Gradle/JUnit, backup round-trip, CI, signed APK and device testing remain pending.
- Remaining V1/V2 work includes multipart exam navigation and weighted partial-credit grading, full custom-session criteria editing, structured derivations, selection-aware formatting, deeper analytics, official FSRS reference-vector validation and full CI/device acceptance. Preserve all original plan requirements.


## Current 2.1.10 release state

- App version 2.1.10 / versionCode 15; Room schema 8; JSON backup schema 8.
- Timed exams include questions with authored multipart parts, in addition to standard MCQs. Parts with saved options render selectable choices; other parts accept short-text answers.
- `MultipartExamRules.kt` grades each part all-or-nothing for its configured positive points. Answer comparison trims, collapses whitespace and ignores case. Exam results show per-part earned/possible points and total weighted percentage.
- Standard MCQ exam behavior remains available. Existing `question_parts` table and schema-8 backup representation are reused; no migration or backup schema bump.
- Added five JUnit test cases; Android Gradle/JUnit has not been run. A standalone Kotlin harness compiled the pure grading helper with a stub entity and passed five assertions.
- Full custom-session criteria editing, structured derivation authoring/execution, advanced numeric tolerance/accepted-answer grading, within-part partial credit, official FSRS reference vectors and full CI/device acceptance remain open.
- Full Android build, GitHub Actions, signed APK, backup round-trip and device testing remain unverified. Preserve original `StudyForge_Plan.txt`, signing identity and all approved roadmap requirements.

## Next planned V1/V2 work after 2.1.10 (historical)

1. Full custom study-session criteria editing and launch filtering.
2. Structured derivation authoring and step-by-step execution.
3. Advanced multipart grading (numeric tolerances, accepted alternatives, manual marking and optional within-part partial credit).
4. Broader exam analytics, official FSRS reference-vector validation, Room/backup tests, CI and device acceptance.


## Current 2.1.12 release state

- App version 2.1.12 / versionCode 17; Room schema 9; JSON backup schema 9 (imports 1–9).
- 2.1.12 fixes the two compile errors reported in the user's CI log: duplicate `FontWeight` import removed from `StudyContent.kt`; question-editor `error` state moved before the image-picker callback in `StudyForgeApp.kt`.
- No Room migration, backup schema or feature scope change. Workflow artifact labels use 2.1.12.
- These fixes are source-inspected; a complete Android build and JUnit run are still pending. Do not claim CI success until a successful Actions run is available.

## Previous 2.1.11 release state

- App version 2.1.11 / versionCode 16; Room schema 9; JSON backup schema 9 (imports 1–9).
- Saved study-session criteria now include subject, chapter, topic, tag, difficulty, exam, question type, status, performance, mode and question limit. Exam-mode sessions use filtered question queues. Collection filtering is not included.
- DERIVATION content block format is one line per step: `title | LaTeX equation | explanation`. Validation requires 1–40 valid steps; editor and study card reveal one step at a time. No symbolic algebra solver is included.
- Multipart parts add accepted text alternatives, absolute tolerance and relative tolerance. Grading awards all configured points or zero for each part; manual marking and within-part partial credit are not implemented.
- Progress screen adds 7-day review activity, distinct active review days, exam response count/accuracy and chapter-level mistake counts. Exam analytics aggregate per-question review logs, not persisted exam-attempt records.
- Migration 8→9 adds three grading columns and three session-criteria columns. JSON schema 9 includes the fields; imports 1–8 default them.
- Local Kotlin harness passed for grading, derivation and session filters. SQLite migration 7→8 (7 checks) and 8→9 smoke tests passed. Schema-9 backup JSON check passed. Parser-only scan had no syntax diagnostics but is not a full compile.
- Full Android build/JUnit, Room runtime migration, app backup round-trip, GitHub Actions, signed APK and device verification remain pending.
- FSRS test source checks published mathematical invariants only; full upstream golden-vector parity remains pending.
- Original `StudyForge_Plan.txt` remains unchanged. Preserve all V1/V2/V3/V4/V5 requirements and existing signing identity.

## Next work after 2.1.12

1. Manual examiner marking and optional within-part partial credit.
2. Persisted whole-exam attempt/result records and analytics.
3. Full upstream FSRS-6 golden vectors for initial state, review transitions and short-term updates.
4. Room runtime migration tests and app-level backup/restore round-trip tests.
5. GitHub Actions build, signed release validation and Android device acceptance.
