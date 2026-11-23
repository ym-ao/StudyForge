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
