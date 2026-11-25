## CURRENT HANDOFF — FSRS-6 compatibility correction (2026-11-25)

The pinned py-fsrs v6.0.0 compatibility mismatch has been corrected in source. Target reference is commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81` (`fsrs/fsrs.py` and `tests/test_basic.py`), not the current upstream `main` branch.

- `ReviewScheduler.kt` now uses the exact pinned 21 weights and formulas: clamped initial Easy mean-reversion target; same-day stability floor for Good/Easy only; Hard step-zero midpoint or 1.5x single-step delay; completed 24-hour elapsed-day semantics; ties-to-even interval rounding; empty learning/relearning step handling.
- Run `python3 tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py` from the project root. It compiles the exact Kotlin scheduler and compares every transition in the pinned `test_memo_state` fixture, plus focused Hard/elapsed-day policy probes. Current result: PASS, final S=49.447237416 / D=6.827105750.
- `ReviewSchedulerTest.kt` and `Fsrs6ReferenceInvariantTest.kt` were compiled/executed with a lightweight Kotlin/JUnit-compatible local runner: 32 passed, 0 failed. This is not the actual Android Gradle/JUnit task.
- No Gradle wrapper is present in the ZIP and no Gradle executable is available in the current environment. Android build, actual Gradle/JUnit, Room runtime migration, backup/restore, CI, signing and physical-device acceptance remain to be done.
- App identity remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15. No APK release. Original `StudyForge_Plan.txt` remains unchanged.

Continue V1/V2 work; do not start V3 until all remaining acceptance gates are complete. See `BUILD_STATUS.md`, `DEVELOPMENT_LOG.md`, `IMPLEMENTATION_STATUS.md`, `ROADMAP.md` and `SOURCE_PLAN_AUDIT_2.1.24.md` for the current state. Older FSRS mismatch notes later in this file are historical and superseded by this correction.

## 2026-11-25 — saved-session safety follow-up (FSRS excluded)

- Fixed a rename collision in saved study-session editing: editing always resolves the original preset by stable ID; attempting to rename it to another preset's name now shows a visible conflict and disables Save rather than overwriting the other preset. New presets retain the existing behavior where saving with an existing name updates that preset.
- Added a confirmation dialog before deleting a saved session. Confirming deletion clears the active selection when that preset was in use; cancelling leaves it intact.
- Added `StudySessionPresetRules` and four focused JUnit regression cases for rename collisions, unchanged names, create-by-existing-name behavior, and stale edit IDs. Extended `tools/verify_session_preset_edit.py` and the source-integrity gate.
- FSRS source/tests and `StudyForge_Plan.txt` remain untouched. App remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15; no migration, release, or signing changes. Android Gradle/JUnit/Compose runtime still requires CI/device verification.

## Latest continuation — 2026-11-25 bulk-selection consistency (FSRS excluded)

- Fixed a Library multi-select edge case: changing filters after selecting questions could leave hidden IDs selected, while bulk actions only applied to the visible subset. All four bulk action paths now resolve selected IDs against the complete current Library. “Select visible” still selects only the filtered view; IDs for deleted questions are ignored safely.
- Added `QuestionSelectionRules` and three JUnit regression cases for full-library selection, stable Library ordering, stale IDs and empty selection. Added `tools/verify_bulk_selection.py` to GitHub Actions and the current source-integrity gate.
- Static source contract checks pass. Android JUnit/Compose runtime remains unrun. FSRS source/tests untouched; app remains 2.1.24/versionCode 28, Room 15, backup 15. No schema, migration, signing, release or original plan change.

## Latest continuation — 2026-11-25 live saved-filter counts (FSRS excluded)

- Saved Library filter presets now show a live match count on each chip. `QuestionLibraryFilter.countMatches` recalculates against current question rows and current collection links; no count is stored, so the count cannot become stale after edits or membership changes.
- Added JUnit regression source for recalculation and deleted-collection references, plus `tools/verify_live_smart_filters.py` wired into GitHub Actions and `verify_v2_source_integrity.py`. The static contract verifier passes locally; Android JUnit/UI remains unrun.
- This is an improvement to saved filter presets, not a distinct dynamic-collection entity or background refresh service. Applying a preset still applies saved criteria to current Library data.
- FSRS source/tests untouched. App remains 2.1.24/versionCode 28, Room 15, backup 15. No release, schema, migration, signing or original plan changes.

# Continue StudyForge in a new ChatGPT conversation

Upload the latest complete StudyForge source ZIP to the new conversation, then paste the prompt below. This ZIP is the source of truth for the included code, current logs, original project plan, and release handoff.

## Latest continuation — 2026-11-25 bulk collection membership (FSRS excluded)

- Added Library multi-select Add to collection / Remove from collection actions. The UI lets the user choose one existing collection and applies the change to selected visible questions.
- The callback revalidates the collection ID inside a Room transaction, snapshots each changed question with its prior collection membership, replaces membership links atomically, and updates the question timestamp. No-op changes do not create duplicate history.
- Added `tools/verify_bulk_collection.py`; wired it into CI and the V2 source-integrity gate. Static contract validation and six standalone Kotlin assertions passed locally; Android JUnit/runtime/UI behavior remains untested.
- FSRS source/tests remain untouched. App identity remains 2.1.24/versionCode 28, Room 15, backup 15. No APK release, schema change or original plan modification.

## Latest continuation — 2026-11-25 bulk Library tags (FSRS excluded)

- Added multi-select bulk Add tag / Remove tag actions to the Library. The operation applies to currently visible selected questions; adding updates the normalized tag catalogue, while removing leaves the catalogue intact.
- Changed questions save their pre-change content snapshot to question version history inside the same Room transaction. No-op updates do not create redundant versions. Comma-containing tag names are rejected to protect the comma-separated tag representation.
- Added `TagText.add` regression cases and `tools/verify_bulk_tagging.py`; wired the verifier into GitHub Actions and `verify_v2_source_integrity.py`.
- Corrected stale multipart-editor helper copy: partial-credit exam scoring and awarded-point adjustment are already connected in the exam flow.
- Six standalone Kotlin tag-rule assertions passed. Current static/release/backup checks and SQLite migration smoke tests 7→8 through 14→15 passed locally.
- FSRS source/tests were not changed. Android JUnit, Gradle build/lint, Room runtime migrations, Android backup/restore round-trip, GitHub Actions, signing/certificate continuity and physical-device acceptance remain unverified.
- App identity remains 2.1.24/versionCode 28, Room 15, JSON backup 15. No APK release, schema/migration change, signing change or original blueprint edit.

## Latest continuation — 2026-11-25 source-vs-plan audit

- The current source remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15. Migrations 1→2 through 14→15 are registered; imports 1–15 are accepted. No APK release or version bump occurred.
- Corrected a stale current technical-baseline paragraph in `PROJECT_PLAN.md` that still said Room/backup schema 10; the maintained plan now reflects schema 15 and the full migration chain. The original `StudyForge_Plan.txt` remains byte-for-byte unchanged.
- Replayed the pinned py-fsrs v6.0.0 `test_memo_state` sequence using the exact included `ReviewScheduler` object in a standalone Kotlin harness (Room transaction wiring excluded). StudyForge produces final S=53.626902917141386/D=6.34635796621067; upstream test asserts S=49.4472/D=6.8271. The mismatch is reproduced and unresolved. Do not claim FSRS parity or make speculative scheduler changes. Compare intermediate states first; keep local-calendar day handling and the explicit Hard-repeat-current-step policy distinct from upstream behavior.
- `verify_v2_source_integrity.py` now checks that `PROJECT_PLAN.md` technical metadata matches the source. Current static/release/media/backup checks and SQLite migration smoke tests 7→8 through 14→15 passed locally.
- Android Gradle/JUnit, Room runtime migration, real Android backup/restore, GitHub Actions, signing/certificate continuity and physical-device acceptance remain unverified. No Gradle wrapper, Gradle executable or Android SDK is available in this environment.

## Latest continuation — 2026-11-25

- Current baseline is still 2.1.24 / versionCode 28, Room schema 15, JSON backup schema 15. No APK release/version bump occurred.
- The GitHub Actions workflow's stale `verify_release_2_1_23.py` call was corrected to `verify_release_2_1_24.py`; CI now also runs `tools/verify_v2_source_integrity.py`.
- The new static integrity script checks all registered migrations 1→2 through 14→15, JSON schema 15 export/import range 1–15, current artifact/version/signing contract and workflow checks. Static only.
- Added a same-day Hard FSRS-6 regression test. Full upstream py-fsrs golden-vector/state-machine parity remains outstanding.
- Local static checks passed, including SQLite migration smoke checks 7→8 through 14→15 and schema-15 backup check. Android Gradle/JUnit, Room runtime, actual backup round-trip, GitHub Actions execution, signed APK/certificate continuity and device acceptance remain unverified.
- Original `StudyForge_Plan.txt` was not modified.

- Added defensive embedded-image import validation: imported Base64 is decoded safely, malformed/empty data is rejected, and decoded payloads are limited to the editor's 1.5 MB maximum. Five JUnit cases and a CI static contract check were added. A standalone Kotlin smoke harness passed five assertions; Android JUnit and full backup/restore are still unverified.

- Added a 14-day recorded-review activity timeline and current streak to Progress, with local-calendar bucketing and accuracy only for scored reviews. Five JUnit cases were added; a standalone Kotlin harness passed 19 assertions. No time-spent estimates are inferred. Android JUnit remains unrun.

- Corrected FSRS-6 difficulty mean reversion to use the unclamped initial-Easy target, then clamp the resulting difficulty. Updated four-rating reference fixtures. However, an attempted upstream py-fsrs v6.0.0 `test_memo_state` sequence did not match (StudyForge S=53.6269/D=6.3464; upstream expected S=49.4472/D=6.8271); full FSRS parity remains an open blocker.

- Corrected the Home dashboard version label to 2.1.24 and added a static check for it.

The pinned upstream FSRS reference used for investigation is py-fsrs v6.0.0 tag commit `352c7a2`; its `tests/test_basic.py` includes the `test_memo_state` expected result S=49.4472/D=6.8271. StudyForge did not match this sequence in a local scheduler-core harness, so continue diagnosing this before claiming FSRS parity. See `SOURCE_PLAN_AUDIT_2.1.24.md` for the exact URLs and observed StudyForge output.

## Latest continuation — 2026-11-25 consistent-backup pass (FSRS intentionally excluded)

- Changed JSON export so all 12 DB-backed data groups are read inside one `database.withTransaction` snapshot. Serialization and output-stream writing happen after the transaction. This prevents a concurrent edit from producing a backup with cross-table state from different moments.
- Added `tools/verify_backup_snapshot.py`; wired into GitHub Actions and the current source-integrity verifier. It passed locally as a static source contract check only.
- Re-ran current release/source-integrity/backup checks and SQLite migration smoke checks 7→8 through 14→15; all current checks passed locally. Historical scripts that assert schema 10/12/13 or old release versions are archival and are not current gates.
- No FSRS source or tests were changed. The exact pinned upstream `test_memo_state` test uses `Again, Good×5`, elapsed-day increments `[0, 0, 1, 3, 8, 21]`, then one final Good at the same timestamp; it asserts rounded S=49.4472 and D=6.8271. StudyForge's recorded result remains S=53.626902917141386 / D=6.34635796621067. Continue to exclude FSRS until separately requested; never claim parity.
- Android Gradle/Compose/KSP/JUnit, Room runtime migrations, actual Android backup/restore, GitHub Actions execution, signed APK/certificate continuity and physical-device acceptance remain unverified.
- App identity remains 2.1.24 / versionCode 28, Room 15, JSON backup 15. No APK release, schema bump, signing change or original blueprint edit.

## Copy-paste prompt

```text
I'm continuing development of my Android application, StudyForge. The uploaded ZIP is the latest complete source package.

First, inspect the entire ZIP before changing anything.

1. Read PROJECT_PLAN.md, StudyForge_Plan.txt, IMPLEMENTATION_STATUS.md, CHANGELOG.md, ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, README.md, PROJECT_FILE_GUIDE.md, SIGNING_SETUP.md, THIRD_PARTY_NOTICES.md, and this handoff file.
2. Inspect the actual Kotlin source, Gradle configuration, Room entities/DAO/migrations, unit tests, backup import/export, manifest, and GitHub Actions workflow. Do not trust documentation in place of source inspection.
3. Confirm app version/versionCode, Room version, JSON backup version, migration chain, current feature status, and actual CI/device evidence.
4. The current source is StudyForge 2.1.24 (versionCode 28), Room schema 15, JSON backup schema 15. Migrations 1→2 through 14→15 are registered; JSON backup import accepts schemas 1–15. Version 2.1.7 added persistence/backup data models for ordered rich-content blocks, multipart question parts with point values, and saved study-session presets through additive migration 7→8. Version 2.1.8 connects TEXT/HEADING/LIST/LATEX blocks to editor CRUD and study-card rendering; adds multipart part authoring for prompt/answer/explanation/points (not exam grading); and adds session preset save/apply/delete UI; 2.1.11 expands it to full saved criteria including subject, chapter, topic, tag, difficulty, exam, type, status, performance, mode and limit. Image/media selection with offline rendering and basic table/graph blocks are source-implemented in 2.1.9. StudyForge 2.1.10 adds multipart timed exam presentation, per-part answer capture and weighted all-or-nothing per-part scoring. StudyForge 2.1.11 adds full saved-session criteria, stepwise DERIVATION blocks, numeric tolerances and accepted alternatives for multipart grading, expanded recorded-data analytics, and FSRS-6 equation-invariant tests. Manual within-part partial credit is source-implemented in 2.1.14. StudyForge 2.1.15 adds persisted whole-exam attempt snapshots, attempt-level analytics/history, and schema-10 backup support. StudyForge 2.1.16 adds selection-aware formatting and exam score analytics. StudyForge 2.1.18 adds CSV/Anki TSV interchange, True/False/Numerical/Cloze types, bulk status/difficulty edits, manual scheduling, collection-aware saved sessions, persistent question version history/restore and multi-line formatting with underline/highlight. StudyForge 2.1.19 adds source title/reference details and preview-first CSV/Anki import with duplicate detection and row-level diagnostics. StudyForge 2.1.20 adds source/date-specific saved filters, core-field version change summaries and accuracy by question type. StudyForge 2.1.21 adds full snapshot-to-current content comparison for question version history, including rich blocks, multipart parts and collections. StudyForge 2.1.22 corrects first-review learning progression: Good advances to the next configured step when available, and Hard starts Learning at step 0; Easy and final Good graduate. Room and JSON backup remain schema 15. It retains FSRS-6, persisted settings, study modes, configurable MCQ exams, LaTeX integration, editorial metadata, filters, saved filter presets and tag management. The 2.1.4 feature adds persisted ordered multi-step Learning/Relearning progression and visible step position. The 2.1.5 feature adds reusable library-filter presets. The 2.1.6 feature adds a Library tag manager and normalized tag catalogue. None should be called CI-verified or device-verified unless real logs prove it.
5. Do not silently remove, simplify, replace, or forget any approved requirement in the original five-major-version plan. The original detailed blueprint `StudyForge_Plan.txt` is authoritative. Incomplete items stay as outstanding requirements. Changes to scope require my explicit approval.
6. Version 1/2 are still not fully complete. The 2.1.7 source added structured block, multipart-part and study-session preset data models plus schema-8 backup support. The 2.1.8 source adds user-facing TEXT/HEADING/LIST/LATEX block authoring and study-card rendering, multipart part authoring (not exam grading), and basic study-session preset save/apply/delete UI. The 2.1.9 source adds Android image picking with embedded offline rendering and table/graph content block authoring/preview. Multipart timed exam capture and weighted per-part scoring are source-implemented in 2.1.10; manual within-part partial credit was added in 2.1.14; persisted whole-exam attempt snapshots and attempt analytics/history were added in 2.1.15. Saved filter presets and a dedicated tag manager are present in source. FSRS-6 reference-equation regression fixtures were added in 2.1.24, but full upstream fixture-by-fixture conformance and full CI/device verification are still pending. See the current implementation matrix and roadmap.
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

Continue V1/V2 gaps before starting Version 3: official upstream FSRS-6 golden-vector validation; full Gradle/Compose/KSP/JUnit; Room runtime migration tests; actual Android JSON backup/restore round-trip; authenticated actor attribution (full content diff was added in 2.1.21; current local history stores only version number and timestamp); advanced editor/accessibility and block editing; broader bulk/smart-collection workflows; deeper skill analytics and reliable time-spent metrics; GitHub Actions, signing continuity and device acceptance. Source/date-specific filters and question-type accuracy are implemented in 2.1.20; full content version diffs are implemented in 2.1.21; first-review Good/Hard learning progression is corrected in 2.1.22. Do not call V1/V2 complete until all original blueprint requirements and testing gates pass. Numerical templates, variable constraints, unit conversion/validation and generated distractors remain V3; do not start V3 before the V1/V2 gate passes.

## Historical 2.1.19 release state

- App 2.1.19 / versionCode 23; Room schema 13; JSON backup schema 13.
- Adds source title/reference detail to question authoring, search, study display, CSV, JSON backup and version snapshots.
- Adds CSV/Anki TSV import preview, stable-ID update classification, duplicate skipping, row-level validation diagnostics and explicit confirmation.
- Added additive Room migration 12→13; all prior migrations retained. Legacy backup imports 1–12 remain supported.
- Full Android build/JUnit, Room runtime, actual Android backup/restore, CI, signed APK and device verification remain pending.

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
- Full custom-session criteria editing, structured derivation authoring/execution and numeric tolerance/accepted-answer grading were source-implemented by 2.1.11; manual within-part partial credit was added in 2.1.14. Persisted whole-exam attempt analytics, official FSRS reference vectors and full CI/device acceptance remain open.
- Full Android build, GitHub Actions, signed APK, backup round-trip and device testing remain unverified. Preserve original `StudyForge_Plan.txt`, signing identity and all approved roadmap requirements.

## Next planned V1/V2 work after 2.1.10 (historical)

1. Full custom study-session criteria editing and launch filtering.
2. Structured derivation authoring and step-by-step execution.
3. Advanced multipart grading (numeric tolerances, accepted alternatives, manual marking and optional within-part partial credit).
4. Broader exam analytics, official FSRS reference-vector validation, Room/backup tests, CI and device acceptance.


## Current 2.1.13 release state

- 2.1.13 corrects the sole failing test found in the uploaded 2.1.12 CI report: `TagTextTest.renameAvoidsDuplicateNames`. The test fixture now includes `MDCAT`, which its assertion expects to preserve. Production tag logic is unchanged.
- Uploaded report summary: 55 tests, 54 passed, 1 failed before this correction. The corrected test has not yet been rerun in Gradle.

- App version 2.1.13 / versionCode 18; Room schema 9; JSON backup schema 9 (imports 1–9).
- 2.1.13 fixes the two compile errors reported in the user's CI log: duplicate `FontWeight` import removed from `StudyContent.kt`; question-editor `error` state moved before the image-picker callback in `StudyForgeApp.kt`.
- No Room migration, backup schema or feature scope change. Workflow artifact labels use 2.1.13.
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

## Next work after 2.1.13 (historical)

1. Manual examiner marking and optional within-part partial credit (implemented in 2.1.14).
2. Persisted whole-exam attempt/result records and analytics.
3. Full upstream FSRS-6 golden vectors for initial state, review transitions and short-term updates.
4. Room runtime migration tests and app-level backup/restore round-trip tests.
5. GitHub Actions build, signed release validation and Android device acceptance.


## Current 2.1.14 release state

- App version 2.1.14 / versionCode 19; Room schema 9; JSON backup schema 9 (imports 1–9).
- Manual examiner-awarded multipart partial credit is source-implemented. Each part's award is clamped to zero through its configured maximum; automatic grading remains the initial score.
- Exam review logs are recorded after the user saves final grading, so the adjusted total determines the review outcome.
- Test source covers manual overrides, clamping and invalid values; Android Gradle execution is pending.
- Persisted whole-exam attempt analytics, complete upstream FSRS vectors, runtime migration/backup round-trip tests, CI, signed APK verification and device acceptance remain outstanding.
- The original signing key and credentials were not accessed or changed.


## Next work after 2.1.14

1. Persist whole-exam attempts and result snapshots, then expose attempt-level analytics without replacing question-level review history.
2. Add full upstream FSRS-6 golden-vector parity tests for initial state, review transitions and short-term updates.
3. Add Room runtime migration tests and app-level backup/restore round-trip tests.
4. Obtain GitHub Actions test/build evidence, verify the existing signing certificate on the release APK, and complete Android device update/offline/backup acceptance.
5. Preserve all remaining Version 1/2 requirements and the original five-major-version plan.


## Current 2.1.15 release state

- App 2.1.15 / versionCode 20; Room schema 10; JSON backup schema 10.
- Additive migration 9→10 creates indexed `exam_attempts`; attempt snapshots are independent of question rows and therefore survive question deletion.
- Exam submission stores question and multipart-part response snapshots, manual/automatic points, timing, counts and weighted score. Progress shows average/best score and recent attempts.
- JSON import still accepts schemas 1–9; importing an older backup does not delete attempt history.
- Focused SQLite migrations 7→8, 8→9, 9→10 and schema-10 JSON fixture checks passed locally. Full Android Gradle/JUnit, Room runtime, real Android backup/restore, CI, signing and device checks remain pending.


## Current 2.1.16 release state

- App 2.1.16 / versionCode 21; Room schema 10; JSON backup schema 10.
- Selection-aware Bold/Italic/Heading/Bullet/Code formatting is connected to question prompt, answer, explanation, plain-text/heading/list content blocks and multipart prompt/answer/explanation fields.
- Progress includes median exam score, score distribution, and newest-five vs previous-five trend when at least ten attempts exist.
- Added formatting, analytics and same-day FSRS-6 equation regression test source. Standalone formatting harness passed five assertions.
- No Room migration or backup schema bump; all existing migration and legacy import paths are retained.
- Full Android build/JUnit, Room runtime, app backup/restore round-trip, GitHub Actions, signing identity and device checks remain pending.

## Next work after 2.1.16

1. Complete official upstream FSRS-6 golden-vector parity testing.
2. Add/run Android Room migration instrumentation tests and full app backup/restore round-trip tests.
3. Run full Gradle/JUnit and GitHub Actions, then verify release certificate continuity and test update/offline behavior on the phone.
4. Continue auditing the original blueprint; do not begin Version 3 until V1/V2 acceptance is genuinely complete.


## Current 2.1.20 release state

- App 2.1.20 / versionCode 24; Room schema 15; JSON backup schema 15.
- Adds independent source-title/reference filters and inclusive created/modified date ranges; saved filter presets and JSON backups preserve all criteria.
- Adds core-field change summaries to question version history (not a line-level diff) and scored-review accuracy grouped by question type.
- Additive migrations 13→14 and 14→15; all prior migrations remain registered. JSON import accepts schemas 1–15.
- New SQLite migration and static source/backup checks are included. Full Android Gradle/JUnit, Room runtime, backup round-trip, CI, signing and device acceptance remain unverified.
- Remaining next work: full FSRS-6 upstream vectors, full visual diff/audit attribution, smart collections/bulk workflows, editor/accessibility, and verification gates. Do not begin V3 before V1/V2 acceptance.


## Current 2.1.22 release state

- App 2.1.22 / versionCode 26; Room schema 15; JSON backup schema 15.
- First-review Good advances to configured Learning step 1 when a second step exists; otherwise it graduates to Review.
- First-review Hard enters Learning at step 0. Easy graduates immediately. StudyForge retains its explicit Hard-repeat-current-step policy for learning cards.
- Added regression test source for new-card Good and Hard transitions.
- No Room migration or JSON backup schema change.
- Full Android Gradle/JUnit, Room runtime migrations, backup round-trip, CI, signed APK/certificate continuity and device acceptance remain unverified.
- Official upstream FSRS-6 golden-vector parity remains open; this release does not certify complete FSRS conformance.


## Current 2.1.21 release state

- App 2.1.21 / versionCode 25; Room schema 15; JSON backup schema 15.
- Adds full structured comparison of saved question snapshots to current question content, including metadata, source fields, blocks, parts and collection membership. Stable IDs are used for child arrays.
- Version history shows before/current snippets with output limits; embedded media payloads are summarized without displaying Base64 data.
- No Room migration or JSON schema change.
- Full Android Gradle/JUnit, runtime migrations, backup round-trip, CI, signing and device acceptance remain unverified.
- Version history does not have authenticated actor identity; it records version numbers and timestamps only.


## 2.1.23 — learner-revealed hint blocks

- Added `HINT` as a supported structured content-block kind. Authors can add, edit, reorder and delete hint blocks in the question editor; existing content-block storage, question snapshots and JSON backup paths are reused.
- During study, hint content is hidden behind a **Reveal hint** action and is displayed only after the learner chooses to reveal it. Hint reveal state is scoped to the current question in the active screen; it is not persisted as a review event.
- No Room migration or JSON backup schema change: both remain version 15. Existing backups and unknown/older block kinds retain their prior behavior.
- 2.1.24 corrects FSRS-6 long-term state math (pre-review difficulty and 0.001 stability floor) and adds four-rating reference-equation regression fixtures. GitHub Actions artifact labels are 2.1.24.
- Verification in this package: Python source-consistency and migration/backup smoke scripts are run locally and recorded in `BUILD_STATUS.md`. Android Gradle/JUnit, Room runtime, CI, signing and device acceptance are not claimed unless separately evidenced.


## Historical 2.1.23 release state

- App version 2.1.23 / versionCode 27; Room schema 15; JSON backup schema 15 (imports 1–15).
- Adds authorable `HINT` structured content blocks. The editor supports add/edit/reorder/delete; the study screen hides hint text until the learner selects Reveal hint. Reveal state is temporary and scoped to the current question screen.
- Reuses existing content-block entity, question-version snapshot, and JSON backup paths; no Room migration or backup schema bump.
- Reorder controls now use explicit “Move up” and “Move down” labels instead of arrow-only labels.
- Local Python static verifier and migration/backup smoke scripts 7→8 through 14→15 plus schema-15 check passed.
- Android Gradle/JUnit, Room runtime, actual Android backup/restore, GitHub Actions, signed APK and device acceptance remain unverified.


## 2.1.24 — FSRS-6 reference-equation correction

- Corrected long-term review transitions to calculate new stability from the pre-review difficulty, then calculate the updated difficulty separately from that same prior state, matching the py-fsrs FSRS-6 update order.
- Corrected the FSRS memory-state lower bound from 0.1 to 0.001 in stability/retrievability/interval calculations, consistent with the upstream FSRS-6 reference implementation.
- Added four-rating long-term reference-equation regression fixtures for Again, Hard, Good and Easy (S=10, D=5, elapsed=5 study days, desired retention 0.90, default 21 weights), plus a lower-bound case. Fixtures follow py-fsrs v6.0.0 equations; this does not certify full scheduler/API parity or StudyForge's distinct learning-step policy.
- App version 2.1.24 / versionCode 28. Room schema 15 and JSON backup schema 15 remain unchanged; migrations 1→2 through 14→15 and backup imports 1–15 are retained.
- Existing HINT blocks and all previously implemented features remain in source. Original `StudyForge_Plan.txt` and signing identity are preserved.
- Android Gradle/JUnit, Room runtime, app backup/restore, GitHub Actions, signed APK and physical-device verification are not claimed without external evidence.


## Current 2.1.24 release state

- FSRS-6 long-term update order corrected to use pre-review difficulty for stability, then independently update difficulty.
- FSRS memory-state floor aligned to 0.001.
- Four-rating reference-equation fixtures cover Again/Hard/Good/Easy for a 5-day review with S=10, D=5, retention 0.90; one lower-bound test added.
- These fixtures follow py-fsrs v6.0.0 equations; full upstream test-suite vector parity remains pending, and StudyForge learning-step transitions intentionally retain product-specific behavior.
- App 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15. No migration or backup format change.
- Android Gradle/JUnit, Room runtime, backup round-trip, GitHub Actions, signed APK and device acceptance remain unverified.

- 2.1.24 local evidence: `verify_release_2_1_24.py`, migration checks 7→8 through 14→15 and schema-15 backup static check passed. A standalone `kotlinc` harness compiled the scheduler core (Room wiring excluded) and matched all four reference-equation values to 1e-9. This is not a full Android compile or JUnit execution.


## Continued 2.1.24 verification note

The current working package remains 2.1.24/versionCode 28 (no new APK release). The FSRS regression test now asserts initial stability and difficulty for all four ratings as well as existing long-term equations. `SOURCE_PLAN_AUDIT_2.1.24.md` records the source-vs-plan audit. Do not describe these equation-derived fixtures as a full upstream golden-vector corpus: obtain/pin upstream-generated vectors with exact provenance and compare complete state sequences before claiming parity. Local Python migration/backup/static checks and an 8-assertion standalone Kotlin scheduler-core harness passed; Android Gradle/JUnit, Room runtime, Android backup/restore, CI, signing, and device acceptance remain unverified. Continue V1/V2; do not start V3 or bump versionCode absent an actual APK release.

## 2026-11-25 V2 continuation — Room instrumentation source added

- Added `app/src/androidTest/java/com/studyforge/app/data/StudyDatabaseRuntimeTest.kt`. It opens a file-backed Room database on Android, writes and reads representative current-schema rows (question, collection/link, settings, tag, rich block, multipart part, review log, session preset, exam attempt, version snapshot and saved filter), closes/reopens the DB and checks persistence. A second test checks question-delete cascades to blocks, parts and review logs.
- Added AndroidX instrumentation dependencies and a GitHub Actions API 35 emulator step invoking `:app:connectedDebugAndroidTest`.
- `tools/verify_v2_source_integrity.py` checks that the runtime test source, dependencies and CI invocation remain wired.
- These instrumentation tests have NOT yet been executed in this local environment. A successful GitHub Actions run is still required. This is current-schema runtime coverage only, not full populated migration testing across versions 1–15. Full app JSON export/import round-trip, signing certificate continuity and physical-device acceptance also remain open.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



## 2026-11-25 continuation — collection cleanup and Android lint gate

- Added an Android Room instrumentation case for collection deletion. It verifies collection-question links are removed, saved-filter preset collection references become null through the Room `SET_NULL` foreign key, and study-session preset references are explicitly cleared using the same cleanup call as the collection-delete UI.
- Added `:app:lintDebug` to GitHub Actions before unit and emulator tests so Compose/Android lint findings fail the workflow rather than being silently skipped.
- Extended the static source-integrity verifier to require the new runtime test and lint workflow step.
- These additions are source-only until GitHub Actions executes. No Android build, lint, unit-test or emulator result is claimed locally.


## 2026-11-25 continuation — strict CSV/Anki quoted-field parsing

- Hardened the shared CSV/TSV parser: a quote may only open a field at its beginning; after a closing quote only a delimiter, record separator or end-of-input is accepted. Malformed quote placement now produces a row-level import preview issue instead of being silently reinterpreted. RFC-style doubled quotes, quoted delimiters, quoted newlines and CRLF remain supported.
- Added JUnit regression source for quotes inside unquoted fields, trailing characters after a closing quote, and valid quoted fields with CRLF. Existing CSV round-trip and Anki quoted-tab/newline cases remain in the suite.
- Compiled the exact production parser with a lightweight entity stub using kotlinc and passed six standalone smoke assertions, including export/import round-trip and malformed-input rejection. Android JUnit remains unexecuted because Gradle/Android SDK are unavailable here.
- No scheduler math, Room schema, JSON backup schema, migration, app version, versionCode, release signing or original blueprint changes.


## 2026-11-25 — saved study-session preset editing (FSRS excluded)

- Saved study-session rows now expose Edit. The dialog loads all persisted filter fields and current mode/topic; saving edits by stable preset ID, including when the user renames it. Save current resets the editor for a new preset.
- Static verifier: `tools/verify_session_preset_edit.py`, wired into GitHub Actions and `verify_v2_source_integrity.py`.
- Local static verification only; Android Compose/JUnit execution is not available in this environment. FSRS source/tests untouched; app/Room/backup versions, migrations, signing and original plan unchanged.


## Latest continuation — 2026-11-25 rich-content Room persistence test source (FSRS excluded)

- Expanded `StudyDatabaseRuntimeTest` with a current-schema Android Room close/reopen test for ordered TEXT, HINT, IMAGE, TABLE and GRAPH blocks. The IMAGE row checks embedded Base64, MIME type and alt text after reopening.
- Extended `tools/verify_v2_source_integrity.py` to require this test and its fixture content. Local source-integrity verification passes.
- The instrumentation test has NOT been executed. This is not historical migration coverage or a JSON backup/restore round-trip. Android Gradle/JUnit, emulator CI, signing and device acceptance remain open.
- App remains 2.1.24/versionCode 28, Room 15, JSON backup 15. No migration, release, signing, original-plan or FSRS change.


## 2026-11-25 — CI compile failure fix

The user reported GitHub Actions failing at `:app:compileDebugKotlin`: `StudyScreens.kt:187` could not resolve `Box`, with composable-context errors immediately after. The source was inspected and lacked `import androidx.compose.foundation.layout.Box` despite using `Box { ... }` inside the saved-session dialog. That import has now been added. The follow-on composable errors are expected to be cascading but must be confirmed by rerunning CI. No Android Gradle build has yet verified the fix. Next action: push the new ZIP contents and rerun GitHub Actions; inspect the next first compiler error if any. No app/version/schema/signing/original-plan changes.
