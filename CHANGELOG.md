## 2.1.24 source continuation — FSRS-6 pinned v6.0.0 compatibility correction

- Aligned the 21 FSRS parameters and memory update formulas with pinned py-fsrs v6.0.0 commit `352c7a2`.
- Corrected Hard learning/relearning intervals, elapsed 24-hour day calculation, ties-to-even interval rounding, and empty learning/relearning step behavior.
- Added pinned per-transition and scheduler policy regressions. Standalone Kotlin/JUnit-compatible runner: 32 passed, 0 failed; diagnostic fixture passes.
- Android Gradle/JUnit/CI/device verification remains pending. No APK release or version/schema change.

## 2.1.24 source continuation — saved-session safety (no APK release)

### Fixed
- Prevented saved-session rename collisions from overwriting a different preset. Editing targets the original stable ID; conflicting names are blocked with an explanation.
- Added delete confirmation for saved sessions and clears the active preset selection only after confirmed deletion.
- Added regression coverage for stable-ID editing, case-insensitive name conflicts, create-by-existing-name updates, and stale edit IDs.

This is source work only; no version bump or APK release. FSRS remains excluded and unchanged.

## 2.1.24 source continuation — Library bulk-selection consistency

- Bulk status, difficulty, tag and collection actions now apply to every selected question in the current Library, even if a later filter change hides some selected rows. “Select visible” remains scoped to the current filtered results.
- Added a pure selection resolver and regression test source; Android runtime tests remain pending.

## [2.1.24] — 2026-11-25

### Added — Library bulk tag editing (source continuation; no APK release)
- Multi-select questions and add or remove one tag across the current visible selection.
- Changed questions save their previous content in version history; tag changes run transactionally.
- Comma-containing tag names are rejected to preserve the comma-separated tag storage format.
- Corrected stale multipart-editor guidance that incorrectly said partial-credit exam scoring was not connected.
- Added standalone tag-rule regression coverage and a CI static contract verifier. Android JUnit/device behavior remains unverified.

### Fixed — FSRS-6 long-term memory-state updates
- New stability now uses the pre-review difficulty; the difficulty transition is calculated separately from the same prior state.
- FSRS stability/retrievability/interval math now respects the upstream 0.001 memory-state floor rather than the previous 0.1 approximation.
- Added four-rating reference-equation fixtures and a minimum-stability regression case.

### Compatibility and verification
- App 2.1.24 / versionCode 28; Room schema 15 and JSON backup schema 15 are unchanged.
- Static release/migration/backup checks and a standalone Kotlin scheduler harness passed locally. Full Android Gradle/JUnit, CI, signing and device verification remain pending.

---


### 2.1.24 continued V2 preparation — 2026-11-25 (no release bump)

- Corrected the GitHub Actions workflow to invoke the current `verify_release_2_1_24.py` instead of the stale 2.1.23 verifier.
- Added a static V2 source-integrity check for version/schema/migration registration, backup compatibility, CI workflow and signing contract; wired it into CI.
- Added a same-day Hard FSRS-6 stability-floor regression test.
- Local release/source-integrity, SQLite migration 7→8 through 14→15, and schema-15 backup checks passed. Android/JUnit, runtime migration/backup, CI execution, signing and device acceptance remain pending.
- No APK release, versionCode change, Room/backup schema change, or signing-key change.

## [2.1.22] — 2026-11-24

### Fixed — first-review Learning progression
- A new card rated Good now advances to the second configured Learning step when one exists, rather than skipping directly to Review.
- A new card rated Hard now enters Learning at step 0 rather than graduating directly to Review.
- Easy still graduates immediately; Good graduates when no next learning step exists. Existing Again, Hard-repeat, step advancement and restart behavior remain in place.
- Preserved StudyForge's product-specific Hard-repeat-current-step policy; this is not a claim of exact upstream Hard-step interval parity.

### Compatibility
- App version 2.1.22 / versionCode 26. Room schema 15 and JSON backup schema 15 remain unchanged; no migration is required.
- Existing app ID, signing alias, signing secret names and original `StudyForge_Plan.txt` are unchanged.

### Verification boundary
- Added focused JUnit source; Android Gradle/JUnit has not been run. Full Android build, Room runtime, backup round-trip, CI, signing and device acceptance remain unverified.

---

## [2.1.21] - 2026-11-24

### Added — full question version comparison
- Replaced the core-field-only change summary with a structured comparison of the saved snapshot against the current question content.
- The comparison covers scalar question metadata, prompt/answer/explanation, source references, tags, options, LaTeX, ordered rich-content blocks, multipart fields and collection membership. Array items with stable IDs are matched by ID rather than display position.
- Version history displays the changed field path and truncated before/current values. Embedded media payloads are represented by size-only summaries so image data is not printed into the history UI.
- Review logs, scheduling state and FSRS memory remain outside content snapshots and are not modified by comparison or restore.

### Data compatibility
- App version 2.1.21 / versionCode 25. Room remains schema 15 and JSON backup remains schema 15; no database migration or backup-format change was required.
- Existing version snapshots remain readable. The diff uses fields already stored in snapshots and treats absent legacy fields as absent rather than overwriting them.
- Added a JVM JSON test dependency for version-diff tests. The existing app ID, signing alias, signing secret names and original `StudyForge_Plan.txt` are unchanged.

### Verification boundary
- Static release consistency and migration/backup source checks are run locally; full Android Gradle/JUnit, Room runtime, backup round-trip, GitHub Actions, signed APK and device verification remain pending unless separately evidenced in `BUILD_STATUS.md`.
- Actor identity attribution is not claimed: this local-only history records version number and timestamp, not which person made an edit. Adding multi-user/actor attribution would require an explicit identity model and is not inferred.

---

## [2.1.19] - 2026-11-24

### Added — provenance and safer imports
- Added per-question source title and source detail fields for textbooks, notes, documents, chapters, pages, sections and URLs. Source details are editable, searchable, shown with study-question metadata, included in CSV interchange, JSON backups and question-version snapshots.
- Added additive Room migration 12→13. Existing question rows retain all content and receive empty source fields; no table rebuild or destructive fallback is used.
- Added CSV and Anki TSV import preview before writing. The preview reports new rows, stable-ID updates, duplicate rows and invalid rows, shows sample questions and requires explicit confirmation before importing.
- Added duplicate detection for repeated stable IDs, repeated prompt/answer pairs in the same file, and exact normalized prompt/answer duplicates already in the library. Duplicate rows are skipped rather than silently overwriting another question.
- Added row-number diagnostics for missing required fields, oversized text fields, unsupported question types/status/difficulty and invalid MCQ/True-False options/answers. Existing schedules and review counters remain preserved for ID-matched updates; blank source fields in older CSV files do not erase saved provenance.

### Data compatibility
- App version 2.1.19 / versionCode 23.
- Room schema 13; migration 12→13 adds `sourceTitle` and `sourceDetail` with empty defaults. Migrations 1→2 through 12→13 remain registered.
- JSON backup schema 13 exports/imports provenance. Older backup schemas 1–12 remain supported; when an older backup is merged into an existing question, current provenance is preserved instead of being cleared. Question-version restore includes source metadata while preserving current scheduling/review state.
- CSV gains optional `sourceTitle` and `sourceDetail` columns; older CSV/Anki formats remain accepted. JSON remains the complete backup format.
- Original `StudyForge_Plan.txt`, app ID, fixed signing alias and signing-secret names are preserved.

### Verification boundary
- Executed locally: SQLite migration scripts 7→8 through 12→13 passed; schema-13 backup compatibility and release source checks passed; standalone Kotlin import harness passed 10 assertions. A parser-only Kotlin scan found no syntax diagnostics but is not a full Android compile.
- Full Android Gradle/Compose/KSP/JUnit, Room runtime migration, Android backup/restore round-trip, GitHub Actions, signing continuity and physical-device acceptance remain unverified.

---

## [2.1.18] - 2026-11-24

### Added — remaining V1/V2 workflows
- Added simple-question CSV import/export with quoted commas, quotes, multiline fields, common header aliases and ID-based merges. Added Anki-compatible three-field TSV import/export (Front, Back, Tags).
- Added True/False, exact-match Numerical and Anki-style Cloze question authoring and study workflows. True/False and Numerical questions can also be used in timed exams; Numerical grading is exact-match in this release.
- Added multi-select library actions for bulk status and difficulty changes; expanded status choices to Draft, Unverified, Verified, Incorrect and Archived.
- Added manual due-date actions (due now, tomorrow, in seven days) and scheduler reset. Reset preserves lifetime review counters and the review log.
- Added collection-aware saved study-session filters; deleting a collection clears its reference from saved sessions.
- Added persistent question content version snapshots, version history UI and restore. Restoring content preserves current review history and FSRS scheduling state.
- Extended formatting shortcuts with underline and highlight markup; heading/bullet prefixes apply to all selected lines. The Markdown-like renderer displays the new styles.

### Data compatibility
- App version 2.1.18 / versionCode 22.
- Room schema 12: migration 10→11 adds saved-session collection filtering; migration 11→12 adds indexed question-version history. Existing migrations 1→2 through 11→12 remain registered and additive.
- JSON backup schema 12 exports/imports question versions and collection-aware study sessions; backup imports 1–12 remain supported. CSV/Anki are intentionally lightweight and do not replace full JSON backup.
- Original `StudyForge_Plan.txt`, app ID, fixed signing alias and secret names are preserved.

### Verification boundary
- Focused local SQLite migration checks 7→8 through 11→12, schema-12 backup checks, release source checks, and standalone Kotlin harness checks for formatting, cloze parsing, CSV/Anki interchange, manual scheduling and collection-aware session filtering passed.
- Kotlin parser scan reported no syntax diagnostics, but it is not a Gradle/Android compilation.
- Full Android Gradle/Compose/KSP/JUnit, Room runtime migration, Android JSON backup/restore round-trip, GitHub Actions, signed APK/certificate continuity and physical-device acceptance remain unverified.

---

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


## [2.1.14] — 2026-11-24

- Added manual examiner-awarded partial credit for multipart exam answers, bounded by each part's configured points.
- Retained automatic exact/alternative/numeric-tolerance marking as the default score.
- Delayed exam review-history updates until final grading is confirmed, so adjusted scores determine the recorded outcome.
- Added regression tests for manual award overrides, bounds, and invalid values.
- App version 2.1.14 / versionCode 19. Room schema 9 and JSON backup schema 9 remain unchanged.
- Whole-exam attempt history and persistent per-attempt analytics remain outstanding.


## 2.1.15 — Persisted exam attempts and history

- App version 2.1.15 / versionCode 20.
- Room schema 10 adds `exam_attempts` with additive migration 9→10. The attempt record stores timestamps, duration, answered/marked counts, weighted points, percentage and a self-contained JSON snapshot of each question/part response and grading result.
- Attempt history intentionally has no foreign key to the question library, so deleting or editing a question does not erase completed exam records.
- Progress now shows saved exam count, average and best score, and the ten most recent attempts.
- JSON backup schema 10 exports/imports attempt history. Import still accepts schemas 1–9; old backups do not delete existing attempt history.
- Added pure exam-score aggregation tests, SQLite migration 9→10 regression script, and schema-10 backup fixture validation. GitHub Actions now runs the migration/backup scripts before Gradle unit tests.
- Verification boundary: focused Python SQLite/JSON checks passed locally. Android Gradle/KSP/JUnit, Room runtime, full Android backup/restore round-trip, CI, signed APK/certificate continuity and device testing remain unverified.


## [2.1.16] — Selection-aware editor formatting and exam analytics

- App version 2.1.16 / versionCode 21. Room schema 10 and JSON backup schema 10 remain unchanged.
- Replaced append-only formatting shortcuts with selection-aware Bold, Italic, Heading, Bullet and Code actions. Selected text is wrapped/prefixed; an empty selection inserts a placeholder and keeps it selected for immediate replacement.
- Applied the formatting field to the question prompt, answer, explanation, plain-text/heading/list content blocks, and multipart prompt/answer/explanation fields.
- Added median exam score, four score-distribution bands, and a recent-vs-previous five-attempt trend to Progress. Non-finite imported score values are safely normalized for analytics. Trend appears only when ten or more saved attempts are available.
- Added pure Kotlin formatting regression tests and expanded FSRS-6 same-day short-term equation checks.
- Local formatting helper harness: 5 assertions passed. Full Android Gradle/JUnit, Room runtime, backup round-trip, CI, signed APK and device verification remain pending.


## [2.1.20] — 2026-11-24

### Added
- Added independent source-title and source-reference filters in the question library.
- Added inclusive created-date and modified-date ranges using `YYYY-MM-DD`; invalid dates are rejected by the filter matcher rather than silently interpreted.
- Saved filter presets now retain all source/date criteria, and JSON backups export/import the expanded preset fields.
- Question version history now shows a core-field comparison summary against the current saved question. It is a summary, not a character-level diff.
- Progress now reports scored-review accuracy by question type, excluding unscored review rows and retaining a separate bucket for deleted-question history.

### Compatibility
- Room schema 15; additive migrations 13→14 (source criteria) and 14→15 (created/modified date criteria). Earlier migrations remain registered.
- JSON backup schema 15; importer continues accepting schemas 1–15. Older backups default new saved-filter fields to empty.
- App version 2.1.20 / versionCode 24; workflow artifact labels updated.

### Verification boundary
- Exact SQLite migration scripts and static source/backup checks were run locally; results are recorded in `BUILD_STATUS.md`.
- Android Gradle/Compose/KSP/JUnit, Room runtime migration, app-level backup/restore, GitHub Actions, signing continuity and physical-device acceptance remain unverified.


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

## 2.1.24 — V2 verification continuation (2026-11-25)

- Added Android instrumentation tests for current Room schema persistence across close/reopen and question-dependent cascade deletion.
- Added AndroidX instrumentation test dependencies and an API 35 emulator step to GitHub Actions (`:app:connectedDebugAndroidTest`).
- Extended the static V2 integrity gate to detect missing instrumentation source/dependencies/CI wiring.
- These tests are added but not yet executed; no Android build, emulator, CI, historical migration-runtime, backup/restore, signing or device result is claimed.
- Structured TABLE and GRAPH blocks now receive pre-save validation (row/column bounds, minimum graph points, exact numeric x,y pairs, and finite values), with regression test source added.


## Unreleased V2 verification improvements (2026-11-25)

- Added Room runtime instrumentation coverage for collection deletion cleanup and saved-filter/study-session preset references.
- Added Android lint to the GitHub Actions verification pipeline and static integrity checks.
- No app version, Room schema, backup schema, or signing identity changed.

## 2026-11-25 continuation — consistent JSON backup snapshot

- Changed JSON export to capture all 12 database-backed record groups (questions, collections, links, review logs, settings, saved filters, tags, content blocks, multipart parts, study-session presets, exam attempts and question versions) inside one Room transaction. JSON formatting and file writing happen after the transaction using the detached snapshot.
- This prevents concurrent edits during export from producing a backup assembled from different database moments, such as a question from before an edit and its rich blocks or collection links from after it.
- Added `tools/verify_backup_snapshot.py` and wired it into GitHub Actions and the current source-integrity gate. The static contract check passed locally. It does not replace Android runtime or real export/import testing.
- No FSRS changes, version/versionCode bump, Room/backup schema change, migration, signing change or original blueprint edit.



## 2026-11-25 — Library bulk collection membership

- Added multi-select add-to-collection and remove-from-collection actions.
- Validates target collections inside a Room transaction, snapshots previous content and collection membership for changed questions, and updates membership atomically.
- Added a static source-contract verifier and CI gate. Android runtime/UI tests remain pending.
- No FSRS, version, Room schema, JSON schema, migration, signing or original-plan changes.

## 2026-11-25 — live saved-filter counts (FSRS excluded)

- Saved Library filter presets now show a live matching-question count beside each chip. Counts are recalculated from the current question list and current collection links, so question edits, tag/status/difficulty changes and collection membership changes are reflected without persisting stale count values.
- Added a pure `QuestionLibraryFilter.countMatches` helper and JUnit source for current-data recalculation and deleted-collection references. Added `tools/verify_live_smart_filters.py` to GitHub Actions and the current source-integrity gate; its static contract check passes locally.
- This is a live-count improvement to existing saved-filter presets, not a separate Room-backed dynamic-collection type or a background auto-refresh service. Applying a preset still applies its saved criteria to the current Library data.
- No FSRS source/tests, app version/versionCode, Room schema, JSON backup schema, migration, signing identity or original blueprint were changed. Android JUnit/UI runtime remains untested.



## 2026-11-25 — saved study-session preset editing (FSRS excluded)

- Added an Edit action to each saved study-session preset. The editor is prefilled with its saved name, question limit, collection, subject, chapter, tag, difficulty, exam, question type, status and performance filters; its mode and topic are restored before editing.
- Updates target the original preset by stable ID, so changing its name updates that preset instead of creating a duplicate. Creating a new preset remains a separate action, and delete remains available.
- Added `tools/verify_session_preset_edit.py` and wired it into CI and the source-integrity gate. This is a static source-contract check; Android Compose/JUnit runtime has not been run locally.
- No FSRS files, app version, Room schema, backup schema, migrations, signing identity or original `StudyForge_Plan.txt` were changed.

## 2.1.24 — CI compile fix (2026-11-25)

- Added the missing Jetpack Compose `Box` layout import to `StudyScreens.kt`, resolving the reported unresolved-reference source defect. Android CI rerun is pending; no release version bump.


## 2.1.24 — CI unit-test follow-up (2026-11-25)

- Fixed multipart grading so invalid manual awards fall back to automatic answer marking.
- Preserved first-seen review order in question-type analytics.
- Corrected three unit-test fixtures that had asserted invalid scenarios incorrectly; added explicit acceptance coverage for a valid MCQ import.
- Full Android unit-test rerun is pending GitHub Actions.
