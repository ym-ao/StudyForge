## [2.1.8] - 2026-11-23

### Added — structured authoring and study-session workflows
- Connected the existing schema-8 content-block table to the question editor. Users can add, edit, reorder and delete TEXT, HEADING, LIST and LATEX blocks.
- Added study-card rendering for saved text, heading, list and LaTeX blocks.
- Connected multipart question records to the editor for part prompts, answers, explanations and positive point values.
- Added saved study-session preset UI to save the current mode/topic/question limit, apply a saved preset, and delete presets.
- Added pure Kotlin helpers for block/part normalization, multipart validation, and preset filtering, with JUnit test source.

### Scope and limitations
- Image/media picker/rendering, tables, graphs, selection-aware formatting, multipart exam presentation, answer capture and weighted partial-credit grading remain unfinished.
- Session preset UI currently captures mode, selected topic and question limit; full subject/chapter/tag/difficulty/exam editing remains unfinished.
- Structured derivation authoring/execution remains unfinished. No original V1/V2 requirement was removed.

### Data compatibility
- App metadata updated to 2.1.8 / versionCode 13.
- Room remains schema 8 and JSON backup remains schema 8; no migration or backup schema change was required.
- Existing schema-8 backup data and imports for schemas 1–8 remain unchanged.
- Original `StudyForge_Plan.txt`, app ID, fixed signing alias and three signing-secret names are preserved.

### Verification boundary
- Source-level wiring and static consistency checks performed; existing migration 7→8 SQLite smoke script rerun successfully (7 checks).
- Added `QuestionAuthoringRulesTest` and `StudySessionRulesTest` source; Android JUnit has not been run.
- Full Gradle/KSP/Room compile, runtime migration, JSON export/import round trip, GitHub Actions, signed APK, signature continuity and physical-device testing remain pending.

---

# Changelog

## [2.1.7] - 2026-11-23

### Added — structured-content data foundation
- Added ordered question content-block records with text/LaTeX/image/table/graph-ready block kinds, optional embedded Base64 media, MIME type and alt text fields.
- Added ordered multipart question-part records with prompt, answer, explanation, options JSON and positive point values.
- Added saved study-session preset records with mode and subject/chapter/topic/tag/difficulty/exam filters plus question limit.
- Added DAO operations and additive Room migration 7→8; question-owned content blocks and parts cascade only when their parent question is deleted.
- Upgraded JSON backup schema to 8; content blocks, parts and session presets are exported/imported. Schemas 1–7 remain accepted.

### Important limitation
- This release adds persistence and interchange foundations only. The current Compose editor, study flow and exam screen do not yet provide complete UI workflows for authoring/rendering rich blocks, multipart grading, or running saved custom sessions. Those remain outstanding features, not completed functionality.

### Verification
- `tools/verify_migration_7_8.py` extracted the exact migration SQL from source and passed 7 SQLite checks with foreign keys enabled.
- Full Android Gradle/KSP/Room build, Android JUnit, Room runtime migration, backup round-trip, GitHub Actions, signed APK and device update are not verified.
- App metadata updated to 2.1.7 / versionCode 12; original product plan and signing identity preserved.

---


## [2.1.6] - 2026-11-23

- Added a dedicated Library tag manager: create, rename and delete tags, with live question-usage counts.
- Renaming and deleting tags update affected question tag strings inside a database transaction; exact tag matching avoids partial-name edits.
- Added a normalized Room tag catalogue and non-destructive migration 6→7. Existing comma-separated question tags remain supported for backward compatibility.
- Upgraded JSON backup schema to 7; tag catalogue is exported/imported. Schemas 1–6 remain accepted. Older backups rebuild catalogue entries from imported question tags without deleting local catalogue entries.
- Added pure Kotlin tag text helper tests. Android JUnit, full Gradle build, Room runtime migration, CI, signed APK and device checks remain pending.
- App metadata updated to 2.1.6 / versionCode 11; workflow artifact names aligned. Existing signing identity and original five-version product plan are preserved.


## [2.1.5] - 2026-11-23

### Added — saved library filter presets
- Save the current library search and filter criteria under a named preset.
- Apply saved presets from the Library screen without manually rebuilding the filter combination.
- Saving with an existing case-insensitive name updates the same preset ID; creation and update timestamps are retained/updated appropriately.
- Manage saved presets and require confirmation before deleting one.
- Persist every supported filter field: query, type, status, subject, chapter, topic, tag, collection, difficulty, exam and performance.

### Data and compatibility
- Room schema 5→6 adds `saved_filter_presets` and an index for its optional collection reference. Deleting a collection sets the preset's collection filter to null instead of deleting the preset.
- JSON backup schema 5→6 exports/imports saved filter presets. Import supports schemas 1–6; older backups leave locally saved presets unchanged.
- App metadata updated to 2.1.5 / versionCode 10; debug/release artifact names updated.
- Original question IDs, review history, FSRS state, study settings, collection links and release-signing identity remain compatibility requirements.

### Verification boundary
- Focused source, SQLite migration, backup JSON and archive checks are recorded in `BUILD_STATUS.md`.
- Android Gradle build, Android JUnit execution, Room runtime validation, GitHub Actions, signed APK and physical-device update remain unverified.

### Still open
- Dedicated tag manager, full structured rich editor/media/tables/graphs, structured derivations, custom study sessions, multipart/partial-credit exams, deeper analytics and FSRS reference-vector validation remain on the approved roadmap.


## [2.1.4] - 2026-11-23

### Added — Version 2 learning progression
- Implemented ordered multi-step Learning and Relearning sequences using the existing configurable minute-delay settings.
- New-card Again starts Learning at step 1. Good advances through configured steps; Hard repeats the current step; Again restarts the sequence; Easy or final Good graduates to FSRS review.
- Again on a graduated Review card starts Relearning and increments lapse count once. Relearning uses its own configured ordered delay sequence.
- Repeated configured delay values are preserved as separate steps; invalid values are ignored and an empty sequence safely falls back to 10 minutes.
- Added visible current-step information on the study card and distinct Learning/Relearning review-log mode labels.
- Added scheduler regression test source for progression, graduation, restart, Hard-repeat, relearning, and repeated delay values.

### Data and compatibility
- Room schema 4→5 adds `learningStepIndex` with a safe default of `-1`; migrations 1→2→3→4→5 remain registered.
- JSON backup schema 4→5 exports the current step position and imports schema versions 1–5. Older schema merges preserve the existing step index for matching question IDs.
- App metadata updated to 2.1.4 / versionCode 9; debug and release artifact names updated.
- Existing application ID, question IDs, collection links, review history, FSRS state, and fixed release-signing alias are retained.

### Verification
- Standalone Kotlin compilation and smoke harness passed 22 scheduler assertions covering learning/relearning progress, restart, Hard repeat, graduation, lapse accounting, and delay parsing.
- SQLite smoke migration and archive/schema integrity checks are recorded in `BUILD_STATUS.md`.
- Android Gradle build, Android JUnit suite, Room runtime validation, JSON backup round-trip, GitHub Actions, signed APK, and device installation remain unverified.

### Still open
- Full structured editor/media/tables/graphs, saved filter presets, tag manager, structured derivations, custom sessions, multipart/partial-credit exams, and expanded analytics remain incomplete and retained in the roadmap.


## [2.1.3] - 2026-11-23

### Added
- Editorial question difficulty (Unrated/Easy/Medium/Hard) and exam/syllabus metadata.
- Formatting shortcut toolbars for question, answer, and explanation fields.
- Library filters for difficulty, exam/syllabus, and recorded performance (Unattempted/Weak/Strong).
- Timed MCQ exam question navigation strip with jump-to-question and answer/mark status.
- Exam result summary grouped by editorial difficulty.
- Focused `QuestionLibraryFilterTest` coverage for search, metadata, performance, and collection filtering.

### Changed
- Room database version 3→4 using additive `difficulty` and `exam` columns with safe defaults.
- JSON backup schema 3→4; exports new metadata and continues to accept backup schemas 1, 2, and 3.
- Older backup merges preserve existing FSRS memory state and 2.1.3 editorial metadata when those fields are absent from the source backup.
- App metadata updated to 2.1.3 / versionCode 8; CI artifact names updated.

### Compatibility
- Existing question IDs, collection links, review logs, and scheduler values are preserved by the additive Room migration.
- Existing 2.1.2 backups and older schema-1/2 backups remain importable.
- Existing release signing alias and key configuration are retained.

### Verification
- Source consistency and targeted static checks only. Android Gradle build, JUnit execution, Room runtime migration, backup round-trip, signed APK, and device installation remain unverified.

### Still open
- Full structured/media editor, saved filter presets, tag manager, multi-step FSRS learning/relearning, structured derivations, custom study sessions, and multipart/partial-credit exams remain incomplete and retained in the roadmap.



## [2.1.2] - 2026-11-23

### Added — Version 1 core improvements
- Added native Canvas-based offline LaTeX rendering for the existing equation field and Markdown-like rich-content previews for headings, lists, bold, italic, and inline code.
- Expanded library filters for question type, status, subject, chapter, topic, tag, and collection; search now includes answer and explanation text.

### Added — Version 2 learning engine
- Replaced the previous SM-2-inspired scheduler with an FSRS-6 source implementation using the published 21-parameter default set, memory stability/difficulty, whole local study-day elapsed intervals, retrievability, and retention-target intervals.
- Added a Room-backed study settings screen for desired retention, maximum interval, daily new/review caps, review order, retry delay, exam length/duration, and randomization.
- Added Random, Ordered, Topic-focused, Weakest-first, and basic Step-by-step study modes; retained Due, All, and Practice.
- Expanded timed MCQ exams with configurable count/duration, optional randomization, mark-for-review, submit confirmation, and result review.
- Expanded progress analysis with actual recorded weak-subject counts.

### Data and compatibility
- Increased Room schema to version 3 with a non-destructive 2→3 migration for FSRS memory values and persistent study settings.
- Increased JSON backup schema to 3; exports FSRS state and settings and continues importing schemas 1 and 2.
- Added accurate third-party notices distinguishing RaTeX MIT-licensed renderer code from KaTeX fonts licensed under SIL Open Font License 1.1.
- Preserved question IDs, review logs, collections, historical release records, and the original five-version plan.
- Kept the 2.1.1 fixed signing alias and preflight validation; the existing private signing key is unchanged and excluded from this source ZIP.

### Tests and verification
- Replaced SM-2-oriented scheduler tests with FSRS-focused unit test source.
- Android Gradle build, CI, migration runtime, official FSRS reference-vector validation, signed APK verification, and device installation are not yet verified. See `BUILD_STATUS.md`.

### Remaining scope
- Rich editor media/blocks/tables/graphs, advanced filters, full multi-step scheduler progression, structured derivation blocks, advanced exam navigation, and expanded instrumented tests remain incomplete. These are not removed from the approved plan.



## [2.1.1] - 2026-11-23

### Fixed
- Removed the non-secret signing alias from GitHub Actions secret configuration; the workflow now supplies the documented alias `studyforge-release` directly.
- Added a `keytool` preflight check to validate that the decoded keystore opens with the configured store password and contains the expected alias before Gradle release packaging.
- Added clearer signing diagnostics so an alias/keystore mismatch fails in the preparation step rather than surfacing later as an opaque `packageRelease` error.

### Changed
- Updated app metadata to 2.1.1 (versionCode 6) and aligned APK artifact names.
- Reduced required GitHub Actions signing secrets from four to three: keystore Base64, store password, and key password.

### Verification
- Workflow and Gradle configuration inspected in source. Android Gradle build, GitHub Actions execution, APK signature, and device installation remain unverified.

### Compatibility
- No application database, Room migration, JSON backup schema, question data, review history, or signing key was intentionally changed.
- The original five-major-version requirements remain intact; incomplete FSRS, LaTeX rendering, editor/filter, and study-engine features remain on the roadmap.



## [Project documentation bundle] - 2026-11-23

### Documentation only
- Added the original product blueprint, current master plan, implementation-status audit, source file guide, and new-chat handoff prompt to the source package.
- Updated the roadmap and build/development records to distinguish implemented source from incomplete planned features and unverified builds.
- No application code or app version metadata changed in this documentation-only package update.


All notable StudyForge changes are recorded here. Newest release first.

## [2.1.0] - 2026-11-23

### Added
- Added optional Gradle release signing driven only by CI environment variables. No passwords or keystore are embedded in source.
- Updated GitHub Actions to build and upload a signed release APK when repository signing secrets are configured, while retaining the debug APK artifact.
- Added signing setup and first-install transition instructions, including the debug-to-release signature mismatch warning.
- Added ignore rules for keystores and signing credential files.

### Changed
- Updated app version metadata to 2.1.0 (versionCode 5).
- Updated workflow artifact names for 2.1.0.

### Verification
- Source/configuration changes were inspected in this package. Android Gradle tests, APK assembly, APK signature verification, and device installation are not yet verified; check the GitHub Actions run after configuring the repository secrets.

### Still outstanding
- FSRS scheduler, configurable review/exam settings, fully rendered offline LaTeX, expanded filters/study modes, and additional regression coverage remain approved requirements in the roadmap. This release does not mark those requirements complete.


## [2.0.1] - 2026-11-23

### Fixed
- Corrected the Room `Migration` import to `androidx.room.migration.Migration`, fixing Kotlin compilation in the database migration.
- Updated the debug APK artifact name and app metadata to 2.0.1 (versionCode 4).

### Verification
- Confirmed the erroneous import is removed and the migration class uses the Room migration package.
- GitHub Actions build is still required to verify Kotlin compilation and APK assembly.


## [2.0.0] - 2026-11-23

### Added
- Due, All, Practice, and timed Exam study modes.
- Again / Hard / Good / Easy review ratings with a 10-minute relearning step and progressively longer review intervals.
- Review history with rating, timestamp, correctness, study mode, mistake category, and scheduled due date.
- Mistake categories for common MCQ errors.
- Progress dashboard with due count, accuracy, recent accuracy, learning-state counts, weak topics, and frequent mistake categories.
- Timed exam mode: up to 10 randomly selected MCQs in 15 minutes, with score and answer explanations.
- Version 1-to-2 Room database migration.
- Version 2 JSON export/import with review history and scheduling state; Version 1 backup import remains supported.
- Unit tests for core review-scheduling intervals.
- GitHub Actions runs unit tests before assembling the debug APK.

### Changed
- Updated app version to 2.0.0 (versionCode 3).
- Question and collection edits now use upsert semantics to avoid deleting linked review history or collection links.
- Editing question content preserves its review schedule and performance counters.
- Home screen displays the due count and learning-engine release.

### Notes
- Scheduler is SM-2-inspired and intentionally not described as FSRS.
- LaTeX remains source-only; rendered math is still planned.
- Local-first behaviour remains; no login, AI provider key, or cloud account is required.

## [1.0.1] - 2026-11-22

### Fixed
- Corrected Compose BOM dependency declaration by placing `platform(...)` inside the Gradle `dependencies` block.
- Updated app version metadata to 1.0.1 (versionCode 2).

## [1.0.0] - 2026-11-21

### Added
- Offline question library with flashcards and MCQs.
- Collections with many-to-many question links.
- Question create/edit, search, basic study screen, review counters, and JSON backup/restore.
- GitHub Actions debug APK workflow.

## [2.1.9] - 2026-11-23

### Added — image/media, table and graph content blocks
- Added Android document-picker image selection for IMAGE blocks. Image bytes are embedded in the existing `mediaBase64` field with MIME type and alt text; image display is offline and does not rely on a persisted external URI.
- Added a 1.5 MB per-image input limit and image MIME validation.
- Added TABLE block editor/preview using pipe-separated cells and newline-separated rows (preview bounded to 30 rows and 8 columns).
- Added GRAPH block editor/preview using numeric x,y pairs separated by comma, semicolon or tab; preview bounded to 200 points.
- Added pure parsing helpers and JUnit test source for table/graph formats.

### Data compatibility
- App version 2.1.9 / versionCode 14. Room schema remains 8 and JSON backup schema remains 8; existing content-block media fields and backup serialization are reused. No database migration was required.
- Existing backup schema 1–8 import compatibility retained. Original `StudyForge_Plan.txt` and signing identity contract preserved.

### Verification boundary
- Source inspection and static checks performed. Pure parser test source added but Android JUnit has not been executed. Full Android Gradle/KSP build, Room runtime, backup round-trip, CI, signed APK and device verification remain pending.
- This release completes the requested image/media and table/graph authoring milestones in source; it does not complete all V1/V2 requirements.

---



## 2.1.10 — Multipart exams and weighted part scoring

- Added multipart question support to timed exam selection.
- Added per-part answer capture with MCQ option selection or short-text entry.
- Added deterministic weighted scoring using each part's configured points.
- Added per-part result feedback, expected-answer review and total weighted percentage.
- Added multipart grading helper and five regression test cases.
- No Room migration or JSON schema change; reuses schema-8 `question_parts` and existing backup data.
- Full Android build, JUnit, CI, signed APK and device verification remain pending.


## [2.1.11] — 2026-11-23

### Added
- Full saved study-session criteria editing for subject, chapter, topic, tag, difficulty, exam/syllabus, question type, status, performance, mode and question limit. Saved Exam sessions now apply the preset filter.
- DERIVATION content blocks with one-step-at-a-time reveal and LaTeX equation rendering.
- Multipart answer alternatives plus absolute and relative numeric tolerance.
- Expanded progress analytics for 7-day activity, distinct review days, exam response accuracy and chapter-level mistakes.
- FSRS-6 mathematical invariant tests based on the published forgetting-curve definition and default initial stability.
- SQLite migration and schema-9 backup verification scripts.

### Changed
- Room schema 8→9 adds absoluteTolerance, relativeTolerance and acceptedAnswers to question_parts, plus questionType, status and performance to study_session_presets.
- JSON backup schema 8→9; imports schemas 1–8 with defaults for the new fields.
- versionCode 15→16; GitHub Actions artifact names updated to 2.1.11.

### Limitations
- Multipart grading remains all-or-nothing per part; manual marking and within-part partial credit are not implemented.
- FSRS tests are invariant checks, not the full upstream reference-vector suite.
- No full Android build, GitHub Actions, signed APK, backup round-trip or device test has been verified.


## [2.1.12] — 2026-11-23

### Fixed
- Removed the duplicate `FontWeight` import that caused an ambiguous-import Kotlin compiler error in `StudyContent.kt`.
- Moved the question-editor `error` state declaration above the image-picker callback that assigns to it, fixing the unresolved/invalid assignment at the callback.

### Changed
- App version advanced to 2.1.12 / versionCode 17.
- GitHub Actions artifact names updated to 2.1.12.
- Room schema remains 9; JSON backup schema remains 9. No database or backup format change.

### Verification boundary
- The two reported compile errors were corrected by source inspection. Full Android compilation, JUnit, GitHub Actions, signed APK and device verification remain pending.


## [2.1.13] — 2026-11-23

### Fixed
- Corrected `TagTextTest.renameAvoidsDuplicateNames` input fixture to include the unrelated `MDCAT` tag that the assertion expects to remain.
- Kept production tag-renaming logic unchanged; the failing report showed the test expected a tag absent from its input.
- Advanced app version to 2.1.13 / versionCode 18 and aligned CI artifact names.
- Room schema 9 and JSON backup schema 9 remain unchanged.

### Verification
- Inspected uploaded GitHub Actions XML/HTML reports: 55 tests, 54 passed, 1 failed.
- Corrected fixture source-inspected; full Gradle/JUnit rerun remains pending.
