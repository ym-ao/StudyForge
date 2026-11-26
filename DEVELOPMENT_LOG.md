## 2026-11-25 — saved-session safety follow-up (FSRS excluded)

- Fixed a rename collision in saved study-session editing: editing always resolves the original preset by stable ID; attempting to rename it to another preset's name now shows a visible conflict and disables Save rather than overwriting the other preset. New presets retain the existing behavior where saving with an existing name updates that preset.
- Added a confirmation dialog before deleting a saved session. Confirming deletion clears the active selection when that preset was in use; cancelling leaves it intact.
- Added `StudySessionPresetRules` and four focused JUnit regression cases for rename collisions, unchanged names, create-by-existing-name behavior, and stale edit IDs. Extended `tools/verify_session_preset_edit.py` and the source-integrity gate.
- FSRS source/tests and `StudyForge_Plan.txt` remain untouched. App remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15; no migration, release, or signing changes. Android Gradle/JUnit/Compose runtime still requires CI/device verification.

## 2026-11-25 continued V2 preparation — bulk Library tags (no release bump)
- Corrected stale multipart-editor helper text: multipart exam scoring and post-submission point adjustments are already connected; the editor no longer claims otherwise.

- Added bulk Add tag / Remove tag actions to Library multi-select. The operation affects only the currently visible selected questions.
- Tag additions create/update the normalized tag catalogue entry. A comma is rejected because question tags are currently stored as comma-separated text and a comma would split one requested tag into multiple tags.
- Changed questions receive a pre-change content snapshot in question version history, and tag updates run in one Room transaction. Unchanged questions do not receive redundant versions.
- Added `TagText.add` regression cases and `tools/verify_bulk_tagging.py`; wired the verifier into GitHub Actions and the current source-integrity gate.
- Six standalone Kotlin tag-rule assertions and current static/release/backup/SQLite migration checks passed locally. Android JUnit and full app runtime remain unverified.
- FSRS code and tests were not touched. No version, Room/JSON schema, migration, signing or original blueprint change.

# 2026-11-25 — StudyForge 2.1.24 V2 preparation continuation

- Reopened the exact `StudyForge-2.1.24-continued-audit.zip` source package and reviewed its handoff, current audit, implementation matrix, roadmap, build workflow, Gradle config, scheduler, tests and Room migrations before editing.
- Found a real CI mismatch: `.github/workflows/android-apk.yml` still called `verify_release_2_1_23.py` even though the current app and artifacts are 2.1.24. Corrected it to call `verify_release_2_1_24.py`.
- Added `tools/verify_v2_source_integrity.py` and wired it into CI. It statically checks app/versionCode, Room schema and registered migrations 1→2 through 14→15, JSON export/import schema 15/1–15, current release verifier, migration smoke-check workflow steps, artifact names and signing-secret/alias contract. It explicitly identifies itself as static validation, not runtime testing.
- Added a same-day Hard short-term FSRS-6 regression case to `Fsrs6ReferenceInvariantTest.kt`; expected behavior is no reduction below the prior stability for this fixture.
- Local execution passed: `verify_release_2_1_24.py`, `verify_v2_source_integrity.py`, SQLite migration checks 7→8 through 14→15, and `verify_backup_schema_15.py`.
- The Android Gradle/JUnit suite, Room runtime migration, app-level backup/restore, GitHub Actions, signed release APK and physical device were not available/used here and remain unverified.
- Original `StudyForge_Plan.txt` was not edited. Room and backup schema remain 15. App version remains 2.1.24 / versionCode 28; no APK release is claimed.

---

## 2026-11-24 — StudyForge 2.1.21 implementation

- Started from the verified 2.1.20 self-contained ZIP and retained Room schema 15 / JSON backup schema 15; no data-model migration was needed.
- Implemented a recursive, stable-ID-aware comparison of stored question snapshots against current content, including metadata, rich-content blocks, multipart parts and collection membership.
- Added before/current values in the version-history UI, bounded display output, and size-only summaries for embedded image payloads.
- Added focused QuestionVersionRules JVM tests and a release consistency checker; configured a JVM JSON implementation for tests.
- No actor identity was fabricated. Current version history has timestamps and version numbers but no authenticated user identity.
- Full Android build/JUnit, Room runtime, JSON round-trip, CI, signing and device verification remain pending.

---

# StudyForge 2.1.19 — 2026-11-24

- Started from the exact 2.1.18 self-contained ZIP and inspected its handoff, original plan, source tree, Room entities/DAO/migrations, backup/import/export, editor, study screens, tests and workflow.
- Added question provenance fields `sourceTitle` and `sourceDetail` with an additive Room migration 12→13; old questions receive empty values and retain all prior content. Added editor fields, search inclusion and study-card display.
- Extended CSV columns to include provenance. JSON schema 13 exports/imports provenance; older schema imports remain supported. Version snapshots include and restore provenance while continuing to preserve live scheduler/review state.
- Added preview-first CSV/Anki TSV import. The preview classifies new rows, stable-ID updates, duplicates and invalid rows; duplicate IDs/content are skipped, and malformed/unsupported rows receive row-numbered diagnostics. Import is only committed after the user confirms.
- Added tests for duplicate detection, invalid rows, stable-ID updates, Anki duplicates and source-field CSV/search behavior. Added exact SQLite migration 12→13, schema-13 backup and release source checks.
- Preserved the original `StudyForge_Plan.txt`, package/application ID, fixed signing alias and external signing-secret contract. No signing key or credentials were accessed or packaged.
- Local verification actually executed: SQLite migration scripts 7→8, 8→9, 9→10, 10→11, 11→12 and 12→13 all passed; schema-13 backup check and release source check passed; standalone Kotlin interchange harness passed 10 assertions. Parser-only scan found no syntax diagnostics but had 7,736 dependency-related diagnostics and is not a full compile.
- Full Android build/JUnit, Room runtime, backup round-trip, CI, signed APK and device acceptance remain pending.

---

# StudyForge 2.1.18 — 2026-11-24

- Started from the exact 2.1.16 self-contained ZIP, re-read its handoff and original `StudyForge_Plan.txt`, and audited the current Room schema, backup code, editor, study/exam screens, library, and workflow before changing source.
- Added CSV question import/export and Anki-compatible TSV import/export for simple fields; documented that JSON remains the only complete backup format.
- Added True/False, exact-match Numerical and Cloze authoring/study paths, including timed-exam support for True/False and Numerical questions.
- Added multi-select bulk status/difficulty changes, manual due-date controls and scheduler reset that retains lifetime counters/review logs.
- Added collection-aware saved study sessions with collection deletion cleanup and schema-11 backup compatibility.
- Added persistent question-version snapshots, history/restore UI and schema-12 backup support. Version restoration intentionally restores content/collections only and preserves current scheduling/review state.
- Improved multi-line heading/bullet formatting, added underline/highlight rendering and expanded question status selection. Question edits, bulk metadata changes and CSV/JSON imports snapshot previous content before replacing it; restoring a version also snapshots the current state so it can be undone. Added focused tests for interchange, cloze, manual scheduling, collection filters and formatting.
- Added additive Room migrations 10→11 and 11→12; no existing table is rebuilt. Original `StudyForge_Plan.txt` was preserved unchanged.
- Local checks: SQLite migrations 7→8 through 11→12, schema-12 backup checks, release source check, standalone Kotlin helpers and session filter harness passed. A parser-only scan had no syntax diagnostics but is not a full compile.
- Full Android build/JUnit, Room runtime, backup round-trip, CI, signing and device acceptance remain pending; no APK build is claimed.

---

# StudyForge 2.1.9 — 2026-11-23

- Uploaded `StudyForge-v2.1.8-self-contained-handoff.zip` was extracted and its handoff instructions followed. Audited the existing content-block entity, Room schema 8, backup import/export, editor, study-card renderer and workflow before modifying code.
- Confirmed schema-8 `QuestionContentBlockEntity` already has `mediaBase64`, `mediaMimeType` and `altText`, and JSON export/import already preserves those fields. Reused existing storage; no migration or backup schema change.
- Added Android OpenDocument image selection to IMAGE content blocks. The app reads the selected image immediately, validates image MIME type, caps the original bytes at 1.5 MB and stores Base64 bytes plus MIME type and alt text.
- Added offline image bitmap rendering to the editor preview and study card.
- Added TABLE block editing and preview (pipe-delimited cells, newline rows; bounded preview 30×8).
- Added GRAPH block editing and preview (numeric x,y pairs; comma/semicolon/tab separators; connected point plot capped at 200 points).
- Added pure `ContentBlockParsing` helper and five JUnit test cases. Test execution is pending because Gradle/Android SDK are unavailable.
- Updated version to 2.1.9 / versionCode 14 and workflow artifact labels. Room remains 8; JSON backup remains 8.
- Original `StudyForge_Plan.txt` was not modified. Full Android build, CI, backup round-trip and device verification are not claimed.

---

# StudyForge 2.1.8 — 2026-11-23

- Uploaded source ZIP `StudyForge-v2.1.7-self-contained-handoff.zip` was inspected before editing. Read the handoff, plan, implementation matrix, roadmap, changelog, development/build logs, README, signing setup, third-party notices, Room entities/DAO/migrations, Compose editor/study screens, backup code and workflow.
- Source-vs-plan audit confirmed that 2.1.7 added schema-8 data/backup foundations only. The existing `question_content_blocks`, `question_parts`, and `study_session_presets` tables were not yet connected to complete UI workflows.
- Added DAO Flow observers for content blocks and question parts without changing Room schema.
- Added editor UI for ordered TEXT, HEADING, LIST and LATEX content blocks, including create/edit/reorder/delete. Added study-card rendering for those block types.
- Added multipart part authoring for prompt, answer, explanation and positive point values. This is authoring only; multipart exam navigation and weighted partial-credit grading remain outstanding.
- Added saved study-session preset save/apply/delete UI. Current UI captures mode, selected topic and question limit; full criteria controls remain outstanding.
- Added `QuestionAuthoringRules` for validation and normalization, plus `StudySessionRules` for deterministic preset filtering. Added unit test source for both helpers.
- No Room migration or JSON backup schema change was required. App version advanced to 2.1.8 / versionCode 13; workflow artifact labels updated. Existing signing alias/secrets contract preserved.
- Original `StudyForge_Plan.txt` preserved byte-for-byte.
- Verification performed in this environment: ZIP source extraction; static source wiring/metadata consistency checks; balanced delimiter checks on edited Kotlin files; project plan hash comparison; package manifest/checksum checks to be recorded in `BUILD_STATUS.md`.
- Not available: Gradle executable, Kotlin compiler, Android SDK. Therefore no full Kotlin/Compose compile, Android JUnit, Room runtime, backup round-trip, GitHub Actions, signed APK or device test is claimed.

# StudyForge 2.1.7 — 2026-11-23

- Started from the exact 2.1.6 self-contained ZIP and audited its current database, backup code and status documents.
- Added `QuestionContentBlockEntity` for ordered portable rich-content records, `QuestionPartEntity` for multipart structure and weighted point values, and `StudySessionPresetEntity` for reusable study selection criteria.
- Added DAO APIs and additive Room migration 7→8. Child content/parts cascade with their question; no existing question/review tables are rebuilt.
- Added JSON schema-8 export/import for all three data models, retaining schema 1–7 import compatibility and bounding imported text/media payload sizes.
- Bumped app version to 2.1.7 / versionCode 12 and Room schema to 8. Updated the current handoff, roadmap, status, README, file guide, changelog and build record.
- `tools/verify_migration_7_8.py` extracted the exact migration SQL from source and passed 7 SQLite checks with foreign keys enabled. Full Android build and runtime verification unavailable: no Gradle wrapper or installed Gradle executable is present in the working environment.
- Explicitly recorded that rich editor UI, multipart exam grading and custom-session UI are still not implemented; this release is a data/backup foundation only. Original `StudyForge_Plan.txt` preserved unchanged.

---

## StudyForge 2.1.6 — 2026-11-23

- Audited the 2.1.5 tag storage and backup paths. Existing question tags are comma-separated strings; kept that representation for compatibility while adding a normalized tag catalogue for management.
- Added `TagEntity`, a unique case-sensitive name index (IDs are normalized lowercase), DAO methods and Room migration 6→7.
- Added tag catalogue synchronization from current question tags. Added Library tag manager actions to create, rename and delete. Rename/delete traverse the current question set and write changed tag strings inside `database.withTransaction`; comparisons are exact and case-insensitive.
- Added `TagText` pure Kotlin helpers for parsing, deduplication, rename, removal and discovery, with five JUnit test cases.
- JSON backup schema 7 exports/imports the tag catalogue. Import still accepts schemas 1–6; older imports leave existing tag catalogue entries intact while imported question tags are discovered through the question Flow.
- Bumped app version to 2.1.6 / versionCode 11, Room schema 7, and aligned debug/release workflow artifact names. Preserved signing alias and secrets contract; no private key included.
- Verification performed: standalone Kotlin harness passed 5/5 tag-helper checks; exact migration 6→7 SQL passed a SQLite smoke test preserving representative legacy rows and enforcing the tag-name unique index; schema-7 backup example JSON parsed successfully. Full Android build, Android JUnit, Room runtime migration, backup round-trip, GitHub Actions, signed APK and device test are pending.

# StudyForge Development Log

## StudyForge 2.1.5 — 2026-11-23

- Inspected the complete 2.1.4 self-contained ZIP before editing, including the active handoff, source, Room schema/migrations, JSON import/export, workflow, test sources, and current roadmap/status. Preserved the original `StudyForge_Plan.txt` unchanged.
- Added a persisted `SavedFilterPresetEntity` containing every current library-filter field and created DAO flow/read/upsert/delete operations.
- Added Library UI to save the current criteria, apply saved presets, update by case-insensitive existing name, and manage/delete with confirmation.
- Added Room migration 5→6 to create a preset table and collection index. The optional collection foreign key uses `ON DELETE SET NULL` so deleting a collection does not delete the preset.
- Upgraded JSON backups to schema 6. Export includes presets; import accepts schema 1–6 and only imports presets from schema 6. Older schema imports leave existing local presets untouched. Imported collection references are validated against the merged collection set.
- Added `SavedFilterPresetTest` source for full criteria round-trip, defaults, trimmed names, and stable identity/timestamps when updating.
- Bumped app to 2.1.5 / versionCode 10 and aligned GitHub Actions artifact labels. Kept the fixed `studyforge-release` alias and the same three signing secrets; no private key is included.
- Local check results: the actual data-layer sources compiled against lightweight Android/Room/Flow stubs; a mapper harness passed 5 checks; SQLite migration 5→6 smoke test passed 5 checks; backup example JSON parsed with all preset fields. The UI source parser scan found no Kotlin syntax diagnostics, but used no Android/Compose classpath.
- Verification boundary: these are focused local checks only. Full Android Gradle/KSP/JUnit/Room runtime, GitHub Actions, signed APK and device update remain pending.


## StudyForge 2.1.4 — 2026-11-23

- Inspected the full 2.1.3 source ZIP, current Room/backup schema, scheduler, UI settings, build workflow, roadmap, and new-chat handoff before editing.
- Found that 2.1.3 stored multi-step delay strings but only scheduled the first valid delay. Implemented sequential Learning/Relearning states and a persistent step index.
- Defined behavior: Again starts/restarts the applicable sequence; Hard repeats the current step; Good advances; Easy or the final Good graduates; a failure on a Review card increments lapse count once and enters Relearning.
- Added the current step cue in the study view and labels Learning/Relearning entries distinctly in review logs.
- Added Room migration 4→5 and backup schema 5. Older schema imports retain an existing step index for matching questions.
- Updated app version, artifact names, README, plan, roadmap, status matrix, file guide, changelog, build status, and handoff. Original `StudyForge_Plan.txt` remains unchanged.
- Compiled the real scheduler source with lightweight test stubs and ran a standalone Kotlin smoke harness: 22 assertions passed.
- Full Android Gradle/JUnit build and CI are unavailable in this environment; runtime Room tests, backup round-trip, signing, and device update remain pending.

## StudyForge 2.1.3 feature release — 2026-11-23

- Inspected the complete 2.1.2 ZIP, original `StudyForge_Plan.txt`, implementation matrix, roadmap, handoff, source, database schema, backup importer/exporter, and GitHub workflow.
- Added question editorial difficulty and exam/syllabus metadata; distinguished the categorical field from FSRS numeric memory difficulty.
- Added additive Room migration 3→4 and JSON backup schema 4. Older schema 1–3 merges retain existing FSRS state and new metadata for matching IDs when the old backup does not contain those fields.
- Added Markdown formatting starter toolbars in prompt, answer, and explanation fields. These are append-at-end shortcuts, not a full selection-aware block editor.
- Extracted library predicates to `QuestionLibraryFilter` and added difficulty, exam, and performance filtering with unit tests.
- Added timed MCQ exam navigation chips and a difficulty-based score summary.
- Kept the existing FSRS scheduler, 2.1.2 capabilities, fixed signing alias, private key identity, and original five-version requirements.
- Updated app version to 2.1.3 / versionCode 8 and CI artifact names.
- Verification boundary: no Android SDK/Gradle build or connected GitHub Actions run was available in this environment. Unit tests, Room runtime migration, backup round-trip, signed APK, and on-device UI behavior remain unverified.


## StudyForge 2.1.2 — V1/V2 feature implementation — 2026-11-23

- Used the full uploaded project ZIP and `HANDOFF_FOR_NEW_CHAT.md` as the working source of truth; preserved the original five-version plan.
- Implemented FSRS-6 scheduling source with the published 21 default parameters, stability/difficulty storage, whole local study-day elapsed intervals, retrievability and retention-target interval calculation. Legacy interval/ease data is retained and safely used to seed an unconverted card's first FSRS review.
- Added Room database version 3 and migration 2→3 for FSRS columns and `study_settings`; no destructive migration was added.
- Added a settings screen and persisted user-selected desired retention, max interval, daily new/review caps, order, retry delay, exam count/duration, and randomization.
- Expanded study modes to Due, All, Practice, Random, Ordered, Topic, Weakest, and a basic answer-then-explanation Step-by-step flow. Practice does not update scheduler/review history.
- Added configurable timed MCQ exam behavior, mark-for-review, submit confirmation and result review.
- Added type/status/subject/chapter/topic/tag/collection filters and broader search.
- Integrated native offline LaTeX rendering for the saved equation field and Markdown-like content previews; full structured rich-content editor remains unfinished.
- Updated JSON backup to schema 3 with FSRS state and settings; schema 1/2 remain accepted. Importing an older schema preserves already-stored FSRS memory state for matching current question IDs and leaves current settings unchanged if absent in that backup.
- Updated scheduler test source for FSRS initial states, lapse/relearning delays, retention/max-interval behaviour, retrievability, the 90% stability point, and whole local study-day handling. A standalone Kotlin smoke test of the scheduler passed; a SQLite smoke test executed the 2→3 migration SQL against a populated test table and confirmed existing row preservation/settings-table creation; and the schema-3 example JSON parsed successfully. The JUnit suite, Room runtime migration, and Android build were not run because this environment lacks Gradle and Android SDK. GitHub Actions build, Room runtime migration, official FSRS vector comparison, signed APK and device testing remain pending.
- Added separate RaTeX MIT-code and KaTeX-font SIL Open Font License 1.1 notices, with the font attribution and full OFL text.
- Kept the same release-signing alias workflow (`studyforge-release`) and do not regenerate or bundle the private keystore.
- Updated `CHANGELOG.md`, `ROADMAP.md`, `BUILD_STATUS.md`, `IMPLEMENTATION_STATUS.md`, `README.md`, `PROJECT_PLAN.md`, `PROJECT_FILE_GUIDE.md`, and this handoff file. Previous entries were retained.


## 2.1.1 signing workflow patch — 2026-11-23

- Inspected the supplied 2.1.0 ZIP and followed its `HANDOFF_FOR_NEW_CHAT.md` instructions.
- Investigated the reported GitHub Actions failure: `:app:packageRelease` could not find the alias value passed from `SIGNING_KEY_ALIAS`; the log rendered the requested alias as replacement characters.
- Removed reliance on the `SIGNING_KEY_ALIAS` GitHub secret because the documented alias is a non-secret constant: `studyforge-release`.
- Added a `keytool` preflight validation before Gradle release packaging, with a targeted diagnostic on mismatch.
- Updated app version to 2.1.1 / versionCode 6 and aligned artifact names.
- Kept the Room database at version 2, JSON backup schema at version 2, and preserved the original product plan and all previous release records.
- Verification boundary: source/workflow edits only. No Android Gradle executable/SDK or actual GitHub Actions run was available here; CI, APK signature, and phone installation remain unverified.


This log is append-only in spirit: new dated entries are added at the top; historical release notes are retained.

## 2.1.0 signing and release workflow — 2026-11-23

- Audited the complete 2.0.1 handoff ZIP before editing. Preserved the original `StudyForge_Plan.txt` and the existing source, database version 2, and JSON backup schema version 2.
- Bumped Android metadata to version 2.1.0 / versionCode 5.
- Added a release signing configuration sourced only from CI environment variables.
- Updated GitHub Actions to run unit tests, build the debug APK, and conditionally build/upload a release APK if signing secrets are configured. The temporary keystore is removed from the runner at job end.
- Added `SIGNING_SETUP.md` and updated README, file guide, plan, roadmap, implementation status, changelog, build status, and this handoff.
- Created a private signing keystore separately from the source package. The `.jks`, Base64 encoding, and private credentials file are not included in the source ZIP and must never be committed to GitHub.
- No Room schema migration was made; existing question IDs, collections, review logs, database version 2, and JSON schema version 2 are unchanged by this source update.
- FSRS, configurable review/exam settings, offline LaTeX rendering, and the full filter/rich-editor requirements remain open. They were not silently removed or described as completed.
- Verification boundary: no Android SDK/Gradle build was available in this environment. No CI, APK signature, or physical-device update test has yet been confirmed.

## Historical source snapshot — 2.1.0 (retained)

**Date:** 2026-11-23  
**App versionCode:** 5  
**Room database version:** 2  
**JSON backup schema:** 2  
**Release status:** Source package and project handoff docs prepared. Successful CI build not yet evidenced in the supplied material.

### Project documentation and handoff package — 2026-11-23

- Added the original user-supplied `StudyForge_Plan.txt` to the source project so the product blueprint travels with the code.
- Added `PROJECT_PLAN.md` with the approved five-major-version release structure and current project decisions.
- Added `IMPLEMENTATION_STATUS.md` to distinguish source implementation from incomplete requirements and verified build/device behavior.
- Reworked `ROADMAP.md` so it follows the approved Version 1–5 structure and records the incomplete Version 1/2 requirements.
- Added `HANDOFF_FOR_NEW_CHAT.md` with a copy-paste prompt for continuing development in a fresh ChatGPT conversation.
- Updated README and build-status records to identify the documentation bundle and the verification boundary.
- No Kotlin/Gradle application implementation was changed in this documentation-only package update. App version remains 2.0.1 / versionCode 4.

### Static audit findings

- `StudyDatabase.kt` contains the corrected import `androidx.room.migration.Migration` and the Room 1-to-2 migration.
- `ReviewScheduler.kt` is explicitly SM-2-inspired; it is not FSRS. The five-version plan calls for FSRS, so Version 2.0 remains incomplete.
- LaTeX is stored and previewed as source text; a MathJax/KaTeX equation renderer was not found.
- The question editor is a basic form, not yet the full structured content-block editor.
- Library search is present; current filter UI is limited to question type.
- Exam mode is MCQ-only, up to 10 questions, with a fixed 15-minute duration.
- Basic review logs, mistake categories, and progress summaries exist.
- Dynamic numerical templates, AI generation/publishing, and cloud sync are not present in this source package.

### Verification boundary

- Static source inspection was completed for the supplied ZIP.
- Four scheduler unit test methods are present in `app/src/test`.
- This environment did not have a configured Android SDK or Gradle executable, so no Android Gradle test/build was run during this package update.
- The latest supplied failure log was from the previous 2.0.0 workflow run and reported unresolved `Migration`. The 2.0.1 source has the corrected import, but no succeeding 2.0.1 GitHub Actions result was supplied.
- Do not mark 2.0.1 CI-verified until the GitHub Actions unit-test and APK-build jobs complete successfully. Device installation/update also remains unverified.

## 2.0.1 patch — 2026-11-23 (source history)

- Corrected `StudyDatabase.kt` to import `androidx.room.migration.Migration`.
- Set app metadata to version 2.0.1 (versionCode 4).
- Updated GitHub Actions artifact naming and README instructions.
- The exact prior unresolved-import source error is corrected in the provided file; full CI compilation remains pending evidence.

## 2.0.0 — 2026-11-23 (source history)

- Added due/all/practice/exam modes, Again/Hard/Good/Easy review ratings, review logs, mistake categories, basic analytics, and JSON schema v2 scheduling/history support.
- Added Room migration from database version 1 to 2.
- Added scheduler unit tests and wired unit tests into GitHub Actions before APK assembly.
- Scheduler is SM-2-inspired, not FSRS; exam is limited; LaTeX rendering remains outstanding.

## 1.0.1 — 2026-11-22 (source history)

- Corrected Compose BOM dependency declaration by placing `platform(...)` inside the Gradle `dependencies` block.
- Set app metadata to version 1.0.1 (versionCode 2).

## 1.0.0 — 2026-11-21 (source history)

- Initial local question-library foundation with flashcards, MCQs, collections, JSON backup/restore, basic study UI, and GitHub Actions APK workflow.

## Ongoing development rules

- Keep the five-major-version plan as the roadmap authority unless the user explicitly changes it.
- Check source code against the plan; never infer completion from a changelog entry alone.
- Preserve database data and backward-compatible imports where feasible.
- Append new release entries and keep previous history.
- Record build/test/device verification honestly.


## 2026-11-23 — StudyForge 2.1.10

- Audited uploaded 2.1.9 handoff and source before editing. Confirmed app 2.1.9/code 14, Room 8, JSON schema 8 and existing persisted multipart `question_parts`.
- Implemented multipart exam eligibility and part-by-part answer entry. Parts with options render selectable options; other parts accept short text.
- Implemented deterministic weighted score aggregation and results review. Matching is trimmed, whitespace-collapsed and case-insensitive exact text. Each part earns its configured points or zero.
- Preserved existing standard MCQ exam flow and review recording behavior.
- Added five focused multipart scoring JUnit test cases.
- No Room/backup schema change; no destructive migration.
- Updated version to 2.1.10 / versionCode 15 and CI artifact labels.
- Full Android Gradle/Compose/KSP build and official JUnit task are not available here; CI, signing, backup round-trip and device checks remain pending.


## 2026-11-23 — StudyForge 2.1.11

- Started from the uploaded 2.1.10 source ZIP and followed its handoff. Inspected Room models/migrations, backup import/export, session screen, multipart exam rules, derivation/content-block UI, FSRS scheduler, workflow and project records.
- Extended saved study-session criteria with question type, status and performance; exposed existing subject/chapter/topic/tag/difficulty/exam fields in the save dialog; included Exam as a supported mode and passed filtered questions into the exam screen.
- Added DERIVATION content block authoring. Each line stores title, LaTeX equation and explanation separated by pipes. Added validation and a stepwise reveal component used by the editor and study card.
- Added accepted text alternatives and absolute/relative tolerance to multipart grading. Added Room migration 8→9 and schema-9 backup serialization/import.
- Expanded recorded-data analytics: 7-day review count, distinct active review days, exam response count/accuracy and chapter mistake aggregation.
- Added FSRS-6 invariant tests for retrievability at stability, published Good initial stability and 90% interval behavior. These do not claim full official vector parity.
- Added migration/backup smoke scripts. Original `StudyForge_Plan.txt` preserved unchanged.

### Verification performed

- Kotlin helper harness passed: tolerance, accepted alternatives, derivation parser/validation and full saved-session criteria filtering.
- SQLite migration 7→8: 7 checks passed.
- SQLite migration 8→9: row preservation/defaults/new field updates passed.
- Schema-9 JSON example parses and static optional-field check passes.
- Kotlin parser-only scan found no syntax diagnostics; this does not establish full compilation.

### Outstanding verification and scope

- Full Gradle/Android/Compose/KSP/JUnit build, Room runtime migration, backup round-trip, CI, signed APK and device testing are pending.
- FSRS upstream golden vectors remain pending.
- Manual examiner marking and within-part partial credit remain pending.
- Exam analytics currently aggregate per-question review logs rather than persisted whole-exam attempts.


## 2026-11-23 — StudyForge 2.1.12 CI compile-error fixes

- Started from the uploaded `StudyForge-v2.1.11-self-contained-handoff.zip` and followed its `HANDOFF_FOR_NEW_CHAT.md`.
- Reproduced the exact source locations from the user's GitHub Actions log.
- `StudyContent.kt`: removed the second duplicate `androidx.compose.ui.text.font.FontWeight` import.
- `StudyForgeApp.kt`: moved `var error by remember { mutableStateOf<String?>(null) }` above the image-picker callback, so `.onFailure` assigns to the declared state.
- Advanced app version to 2.1.12 / versionCode 17 and updated workflow artifact names. Room schema 9 and JSON backup schema 9 remain unchanged.
- Original `StudyForge_Plan.txt` was not edited. Existing signing workflow and alias remain unchanged.
- Static assertions confirmed exactly one `FontWeight` import and that the error state declaration precedes the image picker callback.
- Full Gradle/Android build and JUnit execution could not be run here; GitHub Actions and device verification remain pending.


## 2026-11-23 — StudyForge 2.1.13 unit-test fixture correction

- Inspected the user's `StudyForge-Unit-Test-Reports.zip` rather than guessing from the Gradle summary.
- Report totals: 55 tests across 10 test classes; 54 passed and 1 failed.
- Sole failure: `TagTextTest.renameAvoidsDuplicateNames`, line 16. Expected `Physics, MDCAT`, actual `Physics`; test input was `Physics, Chemistry`, so `MDCAT` was not present to preserve.
- Corrected the test input to `Physics, Chemistry, MDCAT`. Kept production `TagText.rename` unchanged because its behavior was consistent with the supplied input.
- Bumped app to 2.1.13 / versionCode 18; Room and JSON schemas remain 9. Updated workflow artifact names and release docs.
- Full Gradle/JUnit rerun, CI, signed APK and device verification remain pending.


## 2026-11-24 — StudyForge 2.1.14 manual multipart marking

- Audited the uploaded 2.1.13 ZIP against the handoff, original plan, current implementation matrix, Room source, backup import/export and CI workflow.
- Corrected current-version metadata in PROJECT_PLAN.md (2.1.13/versionCode 18 had been contradicted by a stale versionCode 17 header); preserved the historical 2.1.10 snapshot as history.
- Implemented manual examiner-awarded points per multipart part on the results screen. Awards are finite and clamped to [0, part maximum]. Automatic grading remains the default.
- Exam review logs are now recorded only after the final grading action, using the adjusted total.
- Added unit test source for manual award override, clamping and invalid award fallback.
- Advanced app to 2.1.14/versionCode 19; Room and JSON backup schemas remain 9. Updated workflow artifact labels.
- A standalone Kotlin harness compiled the pure multipart grading helper against a minimal entity stub and passed its manual-award, bounds and aggregation assertions. The Gradle/JUnit suite could not be run because Gradle is not installed and the source ZIP has no wrapper. No GitHub Actions run, signed APK validation or Android-device test was performed in this environment.


## 2026-11-24 — StudyForge 2.1.15 implementation

- Audited the uploaded 2.1.14 source ZIP and handoff before editing. Existing Room schema 9 and JSON backup schema 9 were the source baseline.
- Added `ExamAttemptEntity` and DAO observation/upsert methods; Room migration 9→10 creates an indexed attempt-history table. It deliberately has no question foreign key so completed attempt snapshots survive question deletion.
- Timed exam submission now persists an attempt snapshot with stable question IDs, question metadata, user responses, correct-answer snapshots, per-part automatic/manual awarded points, possible points, marked/answered state, duration and weighted total. Existing review-log creation still occurs after the final grading action.
- Progress UI now reports saved attempt count, average/best percentage, and recent attempt history.
- JSON backup advances to schema 10, includes `examAttempts`, accepts legacy schemas 1–9, and upserts attempts only when present. It never deletes pre-existing attempts when an older backup is imported.
- Added `ExamAttemptRulesTest`, SQLite migration 9→10 and schema-10 backup checks; wired the Python checks into GitHub Actions.
- Local checks: migration 7→8 (7 checks), migration 8→9, migration 9→10, and schema-10 backup fixture all passed. Full Android build/test and device checks were not available/performed.


## 2026-11-24 — StudyForge 2.1.16 editor and analytics improvements

- Started from the supplied 2.1.15 source package and preserved its original five-version blueprint, app ID, signing workflow, Room schema 10, JSON schema 10, migrations 1→2 through 9→10, and backup compatibility.
- Implemented selection-aware Markdown formatting for Bold, Italic, Heading, Bullet and Code. It wraps selected text or inserts a placeholder with the inner text selected when the cursor has no selection. Wired the component into question prompt, answer, explanation, and multipart prompt/answer/explanation and plain-text/heading/list content-block authoring.
- Added pure Kotlin formatting regression test source for selected text, empty selection, heading insertion, bullet removal and reversed selection. A standalone kotlinc harness executed five matching assertions successfully.
- Expanded exam analytics with median score, score-band distribution, and latest-five vs previous-five trend; trend is suppressed until ten attempts exist. Non-finite imported percentages are normalized to zero before aggregation. Added JUnit test source for these calculations.
- Expanded FSRS-6 same-day checks for the short-term update equation for Good, Easy and Again ratings. These are equation-based checks, not upstream end-to-end golden vectors.
- Advanced app metadata to 2.1.16 / versionCode 21 and updated workflow artifact labels. No Room or JSON schema change was needed.
- Full Android Gradle/Compose/KSP compilation and JUnit, Room runtime migration, Android backup/restore round-trip, GitHub Actions, signed release/certificate continuity and device installation remain unverified. The existing private signing key was not accessed or changed.


## 2026-11-24 — StudyForge 2.1.20 implementation

- Started from the 2.1.19 self-contained source ZIP. Preserved original `StudyForge_Plan.txt`, existing app identity and signing contract.
- Added independent source-title/source-reference and created/modified date-range filters. Date boundaries are inclusive and use local `yyyy-MM-dd` calendar dates. Saved presets and JSON backups retain all six new criteria fields.
- Added additive Room migration 13→14 for source filters and 14→15 for four date boundaries; existing presets receive empty defaults.
- Added core-field change summaries in question version history. This is not a line-level diff or full audit attribution.
- Added question-type accuracy analytics based on scored review logs; unscored logs are excluded and deleted-question logs remain visible in their own category.
- Added filter, preset, question-type analytics tests plus SQLite migration and static backup/release verification scripts.
- Advanced app to 2.1.20/versionCode 24, Room schema 15 and JSON schema 15; updated workflow artifact names and validation steps.
- Android Gradle compilation/JUnit, Room runtime, full backup round-trip, CI, signing/certificate continuity and device acceptance have not been performed.


## 2026-11-24 — StudyForge 2.1.22 first-review Learning correction

- Started from the uploaded `StudyForge-v2.1.21-self-contained-handoff.zip`; read the complete handoff and inspected the current project plan, original blueprint, implementation matrix, roadmap, build records, Kotlin source, Room entities/DAO/migrations, tests, backup import/export, manifest and GitHub Actions workflow.
- Confirmed app 2.1.21/versionCode 25, Room schema 15, JSON backup schema 15, registered migrations 1→2 through 14→15, and backup imports 1–15. Confirmed the ZIP contains no Gradle wrapper and this environment has no Gradle executable.
- Source audit found a first-review state-transition gap: Good on a new card bypassed the configured next Learning step, and Hard on a new card graduated directly to Review. Corrected Good to advance to step 1 when a second step exists and Hard to enter Learning at step 0. Easy and final Good still graduate.
- Preserved the explicit StudyForge policy that Hard repeats the current step; this release does not claim full upstream scheduler-policy parity. Upstream state-transition documentation was consulted for New/Learning Good and Hard behavior.
- Added JUnit test source for the new Good and Hard transitions. A standalone `kotlinc` harness compiled the scheduler core with Android Room wiring excluded and passed four assertions (new Good, new Hard, new Easy, one-step Good graduation). Android JUnit has not been run.
- Advanced app metadata to 2.1.22/versionCode 26 and updated workflow artifact labels. Room and JSON backup remain schema 15; no migration or backup-format change.
- Corrected stale current-version metadata in `PROJECT_PLAN.md` and the audit basis in `IMPLEMENTATION_STATUS.md`; updated release records and handoff. Original `StudyForge_Plan.txt` was not modified.
- Local Python checks passed for migrations 7→8 through 14→15, schema-15 backup export/import static checks and the 2.1.22 source consistency check. No Gradle build, Android JUnit, Room runtime, app backup/restore round-trip, GitHub Actions, signing verification or device test was performed. Existing private signing key and credentials were not accessed or changed.


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

- Local checks executed: release consistency verifier passed; migration smoke/static checks 7→8, 8→9, 9→10, 10→11, 11→12, 12→13, 13→14 and 14→15 passed; schema-15 backup static check passed. Standalone Kotlin harness compiled the exact scheduler core (excluding Room transaction wiring) and matched all four rating outputs to 1e-9.
- No Gradle executable or Gradle wrapper is available in this environment; Android JUnit/build, Room runtime, backup round-trip, CI, signed APK and device checks were not run.


## 2.1.24 continued verification — initial-state coverage and plan audit

- Re-opened the complete 2.1.24 source package and handoff; confirmed version 2.1.24/versionCode 28, Room schema 15, backup schema 15, and the outstanding V1/V2 acceptance gates.
- Added four initial-state FSRS-6 equation assertions covering stability and difficulty for Again, Hard, Good and Easy using the published default 21 weights. Existing four-rating long-term equation fixtures remain.
- Added `SOURCE_PLAN_AUDIT_2.1.24.md`; explicitly distinguishes equation-derived fixtures from a true upstream-generated golden-vector corpus. Full upstream parity remains outstanding.
- Passed `verify_release_2_1_24.py`, migration/static checks 7→8 through 14→15, and schema-15 backup static checks. Standalone Kotlin scheduler-core harness passed four initial-state plus four long-term assertions.
- No Android Gradle/JUnit, Room runtime, app backup/restore, GitHub Actions, signing, or device verification was performed. No APK was built; versionCode remains 28. Existing signing identity was not accessed or changed.

## 2026-11-25 — V2 Room runtime verification added

Added `StudyDatabaseRuntimeTest` under `app/src/androidTest`. It opens the actual generated Room database on Android SQLite, persists representative records from the current schema, closes/reopens the database and checks persistence. A second test checks foreign-key cascade behavior when a question is deleted. AndroidX instrumentation dependencies were added, and GitHub Actions now invokes `:app:connectedDebugAndroidTest` on an API 35 emulator before building the APK.

No runtime result is claimed yet: the emulator job must run in GitHub Actions. These tests do not yet construct every historical schema version or prove migrations 1→15 with populated records; nor do they execute the app's complete JSON export/import flow.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



## 2026-11-25 continuation — collection cleanup and Android lint gate

- Added an Android Room instrumentation case for collection deletion. It verifies collection-question links are removed, saved-filter preset collection references become null through the Room `SET_NULL` foreign key, and study-session preset references are explicitly cleared using the same cleanup call as the collection-delete UI.
- Added `:app:lintDebug` to GitHub Actions before unit and emulator tests so Compose/Android lint findings fail the workflow rather than being silently skipped.
- Extended the static source-integrity verifier to require the new runtime test and lint workflow step.
- These additions are source-only until GitHub Actions executes. No Android build, lint, unit-test or emulator result is claimed locally.


## 2026-11-25 continuation — safe embedded-image backup import

- The JSON backup importer previously accepted Base64 strings using a character-only regular expression and a loose 8,000,000-character truncation. That could preserve malformed Base64 or accept decoded image payloads larger than the editor's 1.5 MB limit.
- Added `MediaImportRules.sanitizeBase64Image`, which rejects empty/malformed payloads, checks encoded length before decoding, enforces the same 1,500,000-byte decoded limit as the image picker, and returns canonical Base64 for valid data. The importer now uses this helper.
- Added five JUnit regression cases for valid/canonical payloads, malformed data, empty data, decoded oversize and encoded-length oversize. Android JUnit remains unrun.
- Added a CI static contract check `tools/verify_backup_media_import.py` and wired it into the existing workflow. A standalone `kotlinc` harness compiled the exact helper and passed five smoke assertions; all existing Python release, source-integrity, migration and schema-15 backup checks passed.
- No app version, Room schema, JSON backup schema, migration, signing configuration or original blueprint change. Full Android backup/restore remains unverified.


## 2026-11-25 continuation — review activity timeline

- Added `ReviewTrendRules` to aggregate persisted review logs into a gap-filled 14-day local-calendar timeline with daily review totals, scored accuracy, active-day count and current streak. Unscored review logs count as activity but are excluded from accuracy. If there are no reviews today, a streak may continue from yesterday.
- Added the 14-day activity card to Progress. It uses actual `reviewedAt` timestamps and recorded correctness only; no time-spent estimate is inferred.
- Added five JUnit regression scenarios for gap filling, accuracy, streak behavior and timezone bucketing. A standalone `kotlinc` harness compiled the exact helper against the Room entity shape and passed 19 assertions. Android JUnit remains unrun.
- No schema/version/backup changes.


## 2026-11-25 continuation — FSRS difficulty mean-reversion correction

- Further comparison with the upstream FSRS scheduler formula found that mean reversion must use the *unclamped* initial Easy difficulty as its target, then clamp the final updated difficulty to 1–10. StudyForge had been clamping the target to 1 before mean reversion. Corrected `ReviewScheduler.nextDifficulty` and updated the four long-term rating fixtures; long-term stability values remain unchanged because the stability update uses pre-review difficulty.
- The correction follows the reference scheduler's `_next_difficulty` formula. A standalone Kotlin harness compiled the scheduler core and reproduced the updated four-rating values.
- A separate attempt to reproduce py-fsrs v6.0.0 `test_memo_state` (Again, Good×5, then same-time Good; elapsed-day inputs 0, 0, 1, 3, 8, 21; 1m/10m learning steps) did **not** match the upstream expected final state: StudyForge produced S=53.6269, D=6.3464 versus the upstream test's S=49.4472, D=6.8271. This is an explicit unresolved conformance gap, not a passing golden-vector test. No further scheduler policy change was made without a complete diagnosis.
- No version, Room schema, JSON schema or migration change. Full upstream fixture-by-fixture parity remains blocked.


## 2026-11-25 continuation — dashboard version label

- Corrected the Home dashboard's hard-coded release label from 2.1.18 to the actual source version 2.1.24. Extended the static integrity verifier to prevent the label from becoming stale again.
- No behavior, data schema, versionCode or release signing changes.


## 2026-11-25 — StudyForge 2.1.24 source-vs-plan audit and FSRS reproduction (no release)

- Reopened the uploaded ZIP and checked all requested project documents, current source modules, Gradle/manifest/workflow, Room schema/migrations, backup paths and test sources. Confirmed 2.1.24/versionCode 28, Room 15, JSON backup 15/imports 1–15, migrations 1→2 through 14→15.
- Found a stale current technical baseline in `PROJECT_PLAN.md` (schema 10 / migration through 9→10) and corrected it to schema 15 / migrations through 14→15. Extended the CI source-integrity script to enforce this. The original detailed blueprint remains unchanged.
- Built a standalone Kotlin harness around the exact `ReviewScheduler` object and replayed the pinned py-fsrs v6.0.0 `test_memo_state` sequence. The final StudyForge state reproduces the known mismatch: S=53.626902917141386/D=6.34635796621067 versus upstream S=49.4472/D=6.8271.
- Deliberately made no scheduler math change: intermediate state parity must be diagnosed first, with StudyForge's local-calendar-day and Hard-repeat-step product policies separated from upstream behavior.
- Passed local static release/source-integrity/media-import/schema-15 backup checks and SQLite migration smoke checks 7→8 through 14→15. No Android/Gradle/JUnit, Room runtime, actual backup/restore, GitHub Actions, signing or device verification occurred.
- No APK release or versionCode/schema change.


## 2026-11-25 continuation — strict CSV/Anki quoted-field parsing

- Hardened the shared CSV/TSV parser: a quote may only open a field at its beginning; after a closing quote only a delimiter, record separator or end-of-input is accepted. Malformed quote placement now produces a row-level import preview issue instead of being silently reinterpreted. RFC-style doubled quotes, quoted delimiters, quoted newlines and CRLF remain supported.
- Added JUnit regression source for quotes inside unquoted fields, trailing characters after a closing quote, and valid quoted fields with CRLF. Existing CSV round-trip and Anki quoted-tab/newline cases remain in the suite.
- Compiled the exact production parser with a lightweight entity stub using kotlinc and passed six standalone smoke assertions, including export/import round-trip and malformed-input rejection. Android JUnit remains unexecuted because Gradle/Android SDK are unavailable here.
- No scheduler math, Room schema, JSON backup schema, migration, app version, versionCode, release signing or original blueprint changes.

## 2026-11-25 continuation — consistent JSON backup snapshot

- Changed JSON export to capture all 12 database-backed record groups (questions, collections, links, review logs, settings, saved filters, tags, content blocks, multipart parts, study-session presets, exam attempts and question versions) inside one Room transaction. JSON formatting and file writing happen after the transaction using the detached snapshot.
- This prevents concurrent edits during export from producing a backup assembled from different database moments, such as a question from before an edit and its rich blocks or collection links from after it.
- Added `tools/verify_backup_snapshot.py` and wired it into GitHub Actions and the current source-integrity gate. The static contract check passed locally. It does not replace Android runtime or real export/import testing.
- No FSRS changes, version/versionCode bump, Room/backup schema change, migration, signing change or original blueprint edit.



## 2026-11-25 continuation — bulk collection membership (FSRS excluded)

- Added multi-select Library actions to add selected questions to, or remove them from, one chosen collection.
- The operation revalidates the collection in the DB transaction, saves a pre-change question-version snapshot with the old collection IDs, updates link rows, and touches the question timestamp. Repeating an already-satisfied operation does not create a version.
- Added `tools/verify_bulk_collection.py` and CI/source-integrity wiring. Static contract check and six standalone Kotlin assertions passed locally (stable ordering, duplicate prevention, no-op operations, whitespace and blank IDs); full Compose/Room runtime verification is still pending.
- No FSRS source/tests were changed; no schema/version/signing/original-plan changes.

## 2026-11-25 — live saved-filter counts (FSRS excluded)

- Saved Library filter presets now show a live matching-question count beside each chip. Counts are recalculated from the current question list and current collection links, so question edits, tag/status/difficulty changes and collection membership changes are reflected without persisting stale count values.
- Added a pure `QuestionLibraryFilter.countMatches` helper and JUnit source for current-data recalculation and deleted-collection references. Added `tools/verify_live_smart_filters.py` to GitHub Actions and the current source-integrity gate; its static contract check passes locally.
- This is a live-count improvement to existing saved-filter presets, not a separate Room-backed dynamic-collection type or a background auto-refresh service. Applying a preset still applies its saved criteria to the current Library data.
- No FSRS source/tests, app version/versionCode, Room schema, JSON backup schema, migration, signing identity or original blueprint were changed. Android JUnit/UI runtime remains untested.



## 2026-11-25 — Library multi-select consistency (FSRS excluded)

- Fixed bulk operations to resolve the selected ID set against all current Library questions, not only the currently filtered subset. This prevents the selected count from overstating the number of records modified after filters change. The “Select visible” control continues to select only visible results.
- Added `QuestionSelectionRules` with regression test source for full-library resolution, Library ordering, stale IDs and empty selections. Added a static CI verifier and wired it into the source-integrity gate.
- Static checks pass; Android JUnit/Compose runtime not run. FSRS source/tests unchanged. No app/Room/backup version or schema change.


## 2026-11-25 — saved study-session preset editing (FSRS excluded)

- Saved study-session rows now expose Edit. The dialog loads all persisted filter fields and current mode/topic; saving edits by stable preset ID, including when the user renames it. Save current resets the editor for a new preset.
- Static verifier: `tools/verify_session_preset_edit.py`, wired into GitHub Actions and `verify_v2_source_integrity.py`.
- Local static verification only; Android Compose/JUnit execution is not available in this environment. FSRS source/tests untouched; app/Room/backup versions, migrations, signing and original plan unchanged.


## 2026-11-25 — rich-content Room persistence test coverage (FSRS excluded)

Expanded `StudyDatabaseRuntimeTest` with a close/reopen persistence case covering ordered TEXT, HINT, IMAGE, TABLE and GRAPH blocks. The image fixture verifies embedded Base64, MIME type and alt text survive Room persistence. Updated `verify_v2_source_integrity.py` to require this case. The verifier passed locally. No Android instrumentation execution is claimed, and this is not a backup round-trip or historical migration test. App 2.1.24/versionCode 28, Room 15 and JSON backup 15 remain unchanged; FSRS and signing identity are untouched.

## 2026-11-25 FSRS-6 conformance diagnosis — pinned-reference mismatch identified

- Built and ran `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. It extracts and compiles the exact production `ReviewScheduler` object with minimal Room/entity stubs using `kotlinc`, then replays the pinned `py-fsrs` v6.0.0 `test_memo_state` fixture. In parallel, it runs a local reference port of the exact upstream implementation/defaults from commit `352c7a2`. No network or third-party Python package is required; `kotlinc` and a JRE are required.
- The reference port reproduces the upstream golden result exactly: final S=49.4472374165 and D=6.8271057501 (rounded S=49.4472 / D=6.8271). The exact StudyForge Kotlin source returns S=53.6269029171 and D=6.3463579662.
- Root cause confirmed: the comparison was mixing different default parameter vectors. Pinned py-fsrs v6.0.0 `fsrs/fsrs.py` has defaults `[0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2]`; StudyForge currently uses `[0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542]`. The vectors differ in all 21 positions. The first mismatch is already visible on the initial Again: upstream S/D=0.2172/7.0114; StudyForge=0.212/6.4133.
- Two additional pinned-v6.0.0 behavioral differences were found: (1) upstream v6.0.0 clamps initial Easy difficulty before using it as the mean-reversion target; StudyForge currently passes `clamp=false`; (2) upstream v6.0.0 short-term stability increase is clamped to >=1 for Good/Easy, not Hard, while StudyForge clamps every grade >= Hard. The Hard difference does not cause the `test_memo_state` fixture failure because that fixture contains no Hard ratings.
- This corrects an earlier investigation note that relied on the newer `main/fsrs/scheduler.py` formula and treated its unclamped mean-reversion target as the pinned v6.0.0 contract. The actual pinned commit's source is `fsrs/fsrs.py`, and its behavior is different. Do not use current-main source as a substitute for the pinned reference.
- No production FSRS code was changed. This task was diagnosis only. Before changing production, decide explicitly whether StudyForge's compatibility target is the exact py-fsrs v6.0.0 defaults/behavior or a newer FSRS-6 parameter baseline. If the target remains pinned v6.0.0, the defaults and the two behavioral differences above must be reconciled and regression-tested together.
- Source references: pinned upstream implementation `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/fsrs/fsrs.py`; pinned fixture `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`. Local diagnostic output was reproduced successfully on 2026-11-25. This is a standalone source harness, not Android Gradle/JUnit or emulator verification.

## 2026-11-25 — FSRS-6 pinned v6.0.0 compatibility correction

- Corrected `ReviewScheduler.kt` to match the exact `py-fsrs` v6.0.0 reference at commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81`: all 21 default weights, clamped initial-Easy mean-reversion target, and the same-day stability floor for Good/Easy only (not Hard).
- Corrected learning/relearning Hard scheduling: step 0 keeps its position and uses the midpoint of the first two configured steps, or 1.5 times the only step; later steps repeat their configured delay.
- Corrected elapsed-day calculation to match Python `timedelta.days` (completed 24-hour periods, not local calendar-day boundaries) and interval rounding to ties-to-even.
- Added exact behavior for empty learning/relearning step lists: no learning steps means new cards graduate directly to Review; no relearning steps means a lapse remains in Review.
- Updated initial-state, same-day, long-term, learning/relearning, empty-step, and pinned `test_memo_state` regression fixtures. The diagnostic compiles the actual scheduler and checks every transition plus focused Hard/elapsed-time policy probes.
- Local results: pinned fixture matches at every transition; final StudyForge state S=49.447237416 / D=6.827105750 (rounded S=49.4472 / D=6.8271). A lightweight Kotlin/JUnit-compatible runner compiled and executed 32 scheduler regression tests: 32 passed, 0 failed. The standalone diagnostic also passed all four focused policy probes.
- Verification boundary: this environment has `kotlinc` and Java but no Gradle executable or Gradle wrapper in the ZIP. Android Gradle/JUnit/Room/emulator execution and GitHub Actions remain unverified. No APK/release was produced; app version 2.1.24/versionCode 28, Room 15 and JSON backup 15 remain unchanged. `StudyForge_Plan.txt` was not modified.

Reference implementation: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/fsrs/fsrs.py`. Reference fixture: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/tests/test_basic.py` (`test_memo_state`).

---


## 2026-11-25 — FSRS-6 pinned v6.0.0 compatibility correction

- Corrected `ReviewScheduler.kt` to match the exact `py-fsrs` v6.0.0 reference at commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81`: all 21 default weights, clamped initial-Easy mean-reversion target, and the same-day stability floor for Good/Easy only (not Hard).
- Corrected learning/relearning Hard scheduling: step 0 keeps its position and uses the midpoint of the first two configured steps, or 1.5 times the only step; later steps repeat their configured delay.
- Corrected elapsed-day calculation to match Python `timedelta.days` (completed 24-hour periods, not local calendar-day boundaries) and interval rounding to ties-to-even.
- Added exact behavior for empty learning/relearning step lists: no learning steps means new cards graduate directly to Review; no relearning steps means a lapse remains in Review.
- Updated initial-state, same-day, long-term, learning/relearning, empty-step, and pinned `test_memo_state` regression fixtures. The diagnostic compiles the actual scheduler and checks every transition plus focused Hard/elapsed-time policy probes.
- Local results: pinned fixture matches at every transition; final StudyForge state S=49.447237416 / D=6.827105750 (rounded S=49.4472 / D=6.8271). A lightweight Kotlin/JUnit-compatible runner compiled and executed 32 scheduler regression tests: 32 passed, 0 failed. The standalone diagnostic also passed all four focused policy probes.
- Verification boundary: this environment has `kotlinc` and Java but no Gradle executable or Gradle wrapper in the ZIP. Android Gradle/JUnit/Room/emulator execution and GitHub Actions remain unverified. No APK/release was produced; app version 2.1.24/versionCode 28, Room 15 and JSON backup 15 remain unchanged. `StudyForge_Plan.txt` was not modified.

Reference implementation: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/fsrs/fsrs.py`. Reference fixture: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/tests/test_basic.py` (`test_memo_state`).

---


## 2026-11-25 CI build failure correction — missing Compose Box import

- User-provided GitHub Actions log showed `:app:compileDebugKotlin` failing at `StudyScreens.kt:187` with `Unresolved reference 'Box'`, followed by two composable-context errors at lines 188 and 191.
- Inspected the exact source in the FSRS-6 corrected project ZIP. `StudyScreen` already uses `Box { ... }` inside its `@Composable` dialog content, but the file's imports omitted `androidx.compose.foundation.layout.Box`.
- Added the missing `Box` import only. The two composable-context diagnostics are expected to be cascading errors from the unresolved `Box` call; they must be confirmed gone by the next GitHub Actions compile.
- No runtime behavior, scheduler logic, app version/versionCode, Room schema, JSON backup schema, migrations, signing configuration or original plan changed.
- Verification boundary: source/import inspection completed locally. A full Android Gradle compile/lint/JUnit run has not been executed here; rerun GitHub Actions to confirm compilation and reveal any subsequent independent errors.


## 2026-11-25 — Triage of five GitHub unit-test failures

Reviewed the CI report (`141 tests completed, 5 failed`) and traced each failure to source or fixture behavior. Fixed two production issues: invalid/non-finite manual multipart awards now fall back to automatic marking, and question-type analytics preserve first-seen review group order. Corrected three test-fixture defects without weakening product validation: invalid MCQ now has an answer absent from its options (plus a valid-MCQ acceptance assertion); tag deduplication fixture now contains the duplicate tag; heading test now uses an actual newline rather than an escaped literal sequence.

Targeted Kotlin smoke checks passed across multipart grading, CSV/MCQ preview, question-type analytics, tag operations, and heading formatting. The pinned FSRS-6 golden diagnostic and V2 source/release verifiers also passed. Full Android Gradle/JUnit verification remains pending in GitHub Actions.

## 2026-11-25 — GitHub Actions emulator boot timeout follow-up

- The user's CI run reached the `Run Room runtime tests on Android emulator` step but failed with `Timeout waiting for emulator to boot`. This is an emulator startup/infrastructure failure: the action did not reach its `connectedDebugAndroidTest` script, so no Room runtime assertion is reported as failed by this log.
- Updated `.github/workflows/android-apk.yml` to enable KVM permissions on the GitHub-hosted Ubuntu runner before launching the AVD, explicitly retain Linux hardware acceleration, and increase `emulator-boot-timeout` from the action's 600-second default to 900 seconds. API 35, x86_64, Pixel 2 profile and the Room test command remain unchanged.
- This is a CI reliability adjustment only. No app source, Room schema/migrations, JSON backup schema, FSRS logic, app version, versionCode, signing material or original plan was changed.
- Required confirmation: rerun GitHub Actions. A successful emulator boot is not yet claimed, and the Room runtime tests remain unexecuted until the connected Android test task completes.


## 2026-11-25 — new-chat ZIP audit and local verification

- Opened and extracted the supplied `StudyForge-2.1.24-v2-ci-emulator-boot-fix.zip` (132 archive entries). Read the requested project documents, original `StudyForge_Plan.txt`, this handoff, current source-plan audit, build/development logs, signing instructions and third-party notices; inspected the current Kotlin/data/test tree, Gradle configuration, Room migration registration, backup paths, manifest and GitHub Actions workflow.
- Confirmed source metadata: app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15; migrations 1→2 through 14→15 registered; backup imports schemas 1–15. No Gradle wrapper, Gradle executable or Android SDK is present in this execution environment.
- Re-ran 11 current verification scripts: release verifier, V2 source-integrity verifier, backup snapshot/schema/media checks, bulk collection/tag/selection checks, live smart-filter checks, saved-session editing checks, and the pinned FSRS diagnostic. All 11 passed. Also ran all eight current SQLite migration smoke scripts (7→8 through 14→15); all passed.
- The pinned py-fsrs v6.0.0 diagnostic compared every transition in `test_memo_state`; the final state matched S=49.447237416 / D=6.827105750. This is standalone scheduler-core verification, not Android Gradle/JUnit or full upstream-suite parity.
- Source-level CI wiring includes lint, unit tests, and API 35 Room instrumentation. The latest user-reported workflow failure was emulator boot timeout before `connectedDebugAndroidTest`; the workflow now enables KVM and allows 900 seconds. This change still requires a fresh GitHub Actions run.
- Remaining acceptance gates: successful GitHub Actions lint/build/JUnit/emulator run; actual Android JSON export/import round-trip; populated historical Room migration runtime coverage; signed APK certificate continuity using the existing external key; and physical-device update/offline/backup acceptance. Continue auditing approved V1/V2 requirements (advanced editor/accessibility, broader smart collections/bulk workflows, deeper analytics/time-spent reliability, and audit attribution). Do not start V3.
- No production source, app version/versionCode, Room/backup schema, signing material, or original blueprint was changed in this audit. No APK was built or released. The original `StudyForge_Plan.txt` remains unchanged.


## 2026-11-26 — V3 pagination scope and Cloze crash report

- Added formal V3 Room Paging 3/database pagination scope to `ROADMAP.md`, including paged Library queries, DB-backed search/filter/sort, incremental Study loading, lazy math rendering, large-library performance tests, and data/FSRS preservation.
- Logged a critical V2 device report: Study crashes after reaching an imported Cloze card; Study now crashes on every entry; assigning a Cloze question to a collection also crashes. Source inspection does not establish root cause. Existing Cloze tests are limited to pure masking/extraction.
- Next: obtain Logcat stack trace, isolate minimal Cloze record, and test import → Study reveal → collection membership/version snapshot on device. No code fix is claimed.
- Original `StudyForge_Plan.txt`, app version, Room schema, backup schema and signing configuration are unchanged.

## 2026-11-26 — Cloze remove-and-replace audit

Replaced the minimal regex-based Cloze helper with a bounded scanner that parses indexed deletions and optional hints, supports whole-note and per-index masking, and leaves malformed notation unchanged. Unified Cloze answer normalization between CSV import and the question editor, added import validation, masked the editor preview, and cached Cloze rendering data in Study. Hardened collection save paths by filtering stale collection IDs and surfacing persistence failures rather than allowing unhandled coroutine exceptions to close the app. Added unit and Room runtime regression coverage. See `CLOZE_REIMPLEMENTATION_AUDIT.md` for before/after comparison and verification limitations. Standalone Kotlin Cloze and CSV harnesses passed (7 + 4 checks); Android Gradle/Room tests and physical-device verification remain pending.


## 2026-11-26 — Study rating button narrow-screen layout fix

- Fixed the rating row where the five-character “Again” label wrapped onto two lines on the user's phone. All four rating buttons retain equal weight, but now use compact horizontal content padding (4 dp) and explicitly single-line labels (`maxLines = 1`, `softWrap = false`).
- Added static regression assertions to `tools/verify_study_ui_performance.py` so future changes cannot silently remove the compact padding or single-line label behavior.
- No FSRS logic, rating values, question data, Room schema, backup schema, app version/versionCode, signing configuration, or original `StudyForge_Plan.txt` was changed.
- Verification: release 2.1.24, Cloze reimplementation, Study/Library performance, and V2 source-integrity scripts pass. Android Gradle/Compose compilation and a physical-device visual retest still require GitHub Actions and the user's phone; no APK was built here.
