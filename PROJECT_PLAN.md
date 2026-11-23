# StudyForge — Master Project Plan

**Current source version:** 2.1.13  
**Android versionCode:** 17  
**Plan status:** Living plan; original blueprint preserved in `StudyForge_Plan.txt`  
**Platform:** Android (Kotlin + Jetpack Compose)  
**Working style:** Develop in staged releases, with each ZIP containing the source and updated project records.

> **Important status correction:** Earlier audit notes describe the 2.1.0 baseline and are retained as history. The current source package is 2.1.13 (versionCode 18, Room schema 9, JSON backup schema 9). The 2.1.11 source adds full saved-session criteria, stepwise DERIVATION blocks, multipart accepted-answer/numeric tolerance grading, broader recorded-data analytics, and FSRS-6 equation-invariant tests. Manual within-part partial credit, persisted whole-exam attempts, complete upstream FSRS vectors, Room runtime/backup round-trip, CI and device verification remain open. The original planning file said that no implementation had started; that statement described the planning baseline and is now historical. The 2.1.2–2.1.10 sources advance unfinished Version 1 and Version 2 features. The 2.1.4 source adds persisted multi-step learning/relearning progression; 2.1.5 adds reusable library-filter presets; 2.1.6 adds tag management. These source features are not yet verified as a successful APK build or device installation. See `IMPLEMENTATION_STATUS.md` and `BUILD_STATUS.md` for source status versus evidence.

## Product vision

StudyForge is a customizable, offline-first Android learning and question-management app inspired by flashcard tools, but intended to support richer question types, mathematical content, dynamic numerical questions, AI-assisted generation, API-based publishing, spaced repetition, learning analytics, backup, and optional synchronization.

It should support MDCAT, board examinations, science, mathematics, and other learning needs. Personal/local use must remain possible without an account. The design should remain extensible without making the first useful releases dependent on cloud services.

## Non-negotiable product principles

1. **Offline first:** question creation, local library management, and study should work without an internet connection.
2. **Data ownership:** users must be able to export, back up, and restore their content.
3. **Stable identity and history:** question edits and migrations must not silently discard question IDs, collection links, scheduling data, or review history.
4. **Extensible question model:** new formats and content blocks should not force a full database redesign.
5. **Deterministic correctness:** numerical answer checking and validation should use tested code/formulas, not unverified AI claims.
6. **Safe AI integration:** AI-generated content starts as draft/unverified; private provider keys must never be embedded in the APK or committed to GitHub.
7. **Honest verification:** distinguish code written, tests run, CI build passed, and app tested on the user's device.

## Five major development versions

The following is the user's approved major-release structure. Smaller patch/minor releases are allowed inside each major version. A major version must not be described as complete merely because a basic implementation exists; use the acceptance criteria and implementation matrix.

### Version 1.0 — Core app

First useful offline-first release:

- Question library and collections.
- Rich question editor.
- LaTeX equation rendering.
- Flashcards and MCQs.
- Tags, search, and filters.
- Local database.
- JSON import/export.
- Basic review history.
- GitHub Actions APK building.

The first useful APK should be genuinely usable for study, not an empty prototype. The architecture should prepare for later releases without prematurely introducing AI/cloud dependencies. The 2.1.2 source added native offline LaTeX renderer integration and Markdown-like content previews. The 2.1.3 source adds exam/difficulty metadata, formatting shortcuts across question/answer/explanation fields, performance/exam/difficulty filters, and exam question navigation. The 2.1.5 source adds named saved library-filter presets with persistence and backup support. The 2.1.6 source adds a dedicated tag manager and normalized tag catalogue while retaining the legacy question tag strings. The full structured block editor (including media, tables, and graphs) remains incomplete.

### Version 2.0 — Learning engine

- Spaced repetition using **FSRS**.
- Exam mode and timed tests.
- Multiple study modes.
- Mistake tracking.
- Weak-topic analytics.
- Custom review settings.

The 2.1.2 source adds an FSRS-6 scheduler, persisted review/exam settings, additional study modes, configurable MCQ exams, and expanded analytics. The 2.1.3 source adds timed-exam question navigation and difficulty-based result breakdown. The 2.1.4 source adds ordered multi-step learning/relearning progression with saved step position; 2.1.5 adds saved library-filter presets; 2.1.6 adds tag management and a normalized catalogue. Version 2 remains open until CI/regression and real-device acceptance checks pass, and custom sessions, richer exam types/partial credit, and advanced analytics are complete.

### Version 3.0 — Advanced question engine

- Dynamic numerical questions.
- Automatic answer checking.
- Variable-based templates.
- Unit validation and tolerances.
- Step-by-step derivations.
- Automatic MCQ distractors with validation.

Use deterministic calculation, range/constraint validation, explicit rounding/significant-figure rules, reproducible variants, and clear answer tolerances. Keep the question template and individual generated variant traceable.

### Version 4.0 — AI and automation

- In-app AI generation.
- Multiple AI providers.
- External API publishing.
- JSON and script workflows.
- Question validation pipeline.
- Draft, approve, and publish workflow.
- Optional web dashboard.

Use provider-independent interfaces and a secure server-side proxy for secrets. AI output must be reviewed and editable before publishing; it must not silently become verified content.

### Version 5.0 — Cloud and expansion

- Optional accounts.
- Cloud synchronization.
- Multi-device support.
- Conflict resolution.
- Advanced analytics.
- Question sharing.
- Multi-user expansion.

Local-only use must continue to work without an account. Synchronization must detect conflicts and avoid silent overwrites.

## Smaller release numbering

Use semantic versioning within the major release, for example:

- `1.0.0` — initial release.
- `1.0.1` — patch/bug fixes.
- `1.1.0` — compatible feature additions.
- `1.2.0` — further improvements.
- `2.0.0` — major learning-engine milestone.

The current source version is `2.1.10` with `versionCode = 15`. Version 2.1.7 added schema-8 persistence and JSON backup foundations for structured content blocks, multipart question parts, and saved study-session presets. Version 2.1.8 adds user-facing text/heading/list/LaTeX block authoring and study-card rendering, multipart part authoring (not partial-credit grading), and basic saved-session save/apply/delete UI. Version 2.1.9 adds image selection with embedded offline rendering, plus table and numeric graph content blocks using the existing schema-8 media fields and backup serialization. Multipart timed exam presentation and weighted all-or-nothing per-part scoring are now implemented in source; advanced within-part partial credit, full session criteria editing, structured derivations, and richer table/graph authoring remain outstanding. It retains the 2.1.6 tag manager, 2.1.5 saved filters, 2.1.4 ordered multi-step Learning/Relearning progression, 2.1.3 question metadata/filters/exam navigation, FSRS-6 scheduler, persisted study settings/modes, LaTeX renderer integration, and fixed signing workflow. Room is schema 8 and JSON backup schema is 8. The release remains a source candidate until CI, migration, backup, and device checks pass. Do not reuse a version code. Increment it for every APK release. Do not mark Version 1 or Version 2 finished while important approved requirements remain incomplete.

## Next recommended work

The 2.1.2–2.1.10 sources advance several previously missing V1/V2 features, but they do not complete the entire approved scope. Before starting Version 3.0, continue with the remaining V1/V2 requirements:

1. Extend the new structured editor with image/media selection and offline rendering, table and graph blocks, and selection-aware formatting.
2. Complete structured derivation authoring/execution, full custom-session criteria editing, multipart exam navigation/answer capture, and weighted partial-credit marking (basic block authoring, session presets, saved filters and tag management are now present in source).
3. Validate the multi-step Learning/Relearning behavior against FSRS expectations, add review-recovery controls, and complete the remaining custom scheduler settings.
4. Expand step-by-step work into structured derivation steps; implement any remaining custom/numerical/topic learning session variants.
5. Expand the current MCQ question navigator into richer exam types, multi-part questions, partial credit, and deeper results.
6. Add database migration, JSON round-trip, FSRS reference-vector, scheduling edge-case, and exam-flow tests.
7. Verify GitHub Actions build, signed APK signature, installation/update, offline LaTeX, DB migration, and backup restore on the Android device.

Avoid scope creep: keep dynamic numerical templates for Version 3, AI/API workflows for Version 4, and cloud/multi-user expansion for Version 5 unless the user explicitly changes the roadmap.

## Technical baseline from current source

- Kotlin + Jetpack Compose.
- Room / SQLite local database.
- App ID: `com.studyforge.app`.
- Minimum SDK 26; compile SDK 36; target SDK 35.
- Java 17 target.
- Android Gradle Plugin 8.13.2; Gradle 8.13 in GitHub Actions.
- Current Room database schema version 8; migrations `1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8` are in `StudyDatabase.kt`.
- Backup JSON schema version 8; import accepts schema versions 1 through 8.
- Current scheduler code is `ReviewScheduler.kt` and implements FSRS-6 with the published default parameter set; CI/reference-vector validation remains pending.
- GitHub Actions workflow: `.github/workflows/android-apk.yml`.

These values must be checked against the source before changing them. Database and backup schema changes require deliberate migration/backward-compatibility work.

## Data safety and release process

For each release:

1. Read the current source, plan, all logs, and implementation-status matrix.
2. Keep the source tree intact; make only the required code/config changes.
3. Review database migrations and import/export compatibility before changing models.
4. Add tests for new logic and regression cases.
5. Update `versionName`, `versionCode`, and workflow artifact naming consistently.
6. Update `CHANGELOG.md`, `ROADMAP.md`, `DEVELOPMENT_LOG.md`, `BUILD_STATUS.md`, and `IMPLEMENTATION_STATUS.md`.
7. Keep historical entries; append rather than erasing release history.
8. Build/test using GitHub Actions. Do not claim CI passed without the actual successful run result.
9. Provide a complete GitHub-ready ZIP containing source, workflow, project docs, and handoff prompt.
10. Give Android/Termux-friendly update instructions. Recommend a backup before installing an update and never advise uninstalling the old app as a routine update step.

## Source of truth

- `StudyForge_Plan.txt` is the original detailed product blueprint supplied by the user. Preserve it as a historical source document.
- `PROJECT_PLAN.md` is the current release-level plan and sequencing guide.
- `IMPLEMENTATION_STATUS.md` describes what the included source actually implements versus what remains planned.
- `CHANGELOG.md` is the immutable release history (append new entries; do not rewrite historical releases).
- `ROADMAP.md` is the live status and next-work list.
- `DEVELOPMENT_LOG.md` records dated technical progress and decisions.
- `BUILD_STATUS.md` records actual build/test evidence and known failures.
- `HANDOFF_FOR_NEW_CHAT.md` contains the prompt to continue in a fresh chat.


## 2.1.1 patch scope

The 2.1.1 patch hardens the release-signing workflow: the non-secret alias is fixed to `studyforge-release` and checked with `keytool` before Gradle packages the APK. The original five-major-version product requirements are unchanged.


## 2.1.3 feature scope

Version 2.1.3 advances unfinished Version 1 and Version 2 features. The exact source-level scope and its limitations are recorded in `IMPLEMENTATION_STATUS.md`. This release does not remove any original requirement, and it does not move Version 3/4/5 features into the current milestone.


## 2.1.3 release addition

- Added question editorial difficulty and exam/syllabus fields.
- Added editor formatting shortcut toolbars for prompt, answer, and explanation fields.
- Added library difficulty, exam, and performance filters.
- Added timed-exam question jump navigation and difficulty-based result summaries.
- Room migration 3→4 adds the new question metadata fields; JSON schema 4 exports them while remaining backward compatible with schemas 1–3.
- This release does not change or reduce the approved five-major-version scope.


## 2.1.4 feature scope

Version 2.1.4 advances the Version 2 learning engine. The scheduler now processes ordered comma-separated Learning/Relearning delays, persists the current step, and exposes step progress in the study UI. The exact grade transitions, migration and backup behavior, tests, and verification boundary are recorded in `IMPLEMENTATION_STATUS.md`, `ROADMAP.md`, `CHANGELOG.md`, and `BUILD_STATUS.md`. This is a source-progress release, not a claim that Version 2 is complete or that an APK has been verified.


## 2.1.5 saved filter preset feature

Version 2.1.5 adds reusable named library-filter presets without changing the original five-major-version scope. The preset record stores each filter field explicitly, including an optional collection reference. Room migration 5→6 creates the preset table and index; deleting a referenced collection sets the preset collection filter to `null` rather than deleting the preset. JSON schema 6 exports presets and imports them by stable ID. Older schema imports leave existing device presets untouched. The feature remains source-implemented until Android JUnit, Room runtime, GitHub Actions, and physical-device checks pass.


## 2.1.6 tag manager feature

Version 2.1.6 adds a dedicated Library tag manager to create, rename and delete tags and show usage counts. The existing question `tagsCsv` field remains for compatibility; a normalized `tags` catalogue is added with Room migration 6→7. Rename/delete operations update exact matching tag names in question records transactionally. JSON backup schema 7 includes the catalogue and accepts older schemas 1–6. This is source implementation only until Android JUnit, Room runtime migration, backup round-trip, CI, signed APK and device checks pass.


## 2.1.7 structured-data foundation

The 2.1.7 source adds ordered `question_content_blocks`, `question_parts`, and `study_session_presets` Room tables via migration 7→8. JSON backup schema 8 exports/imports these records and retains import support for schemas 1–7. These tables are not yet connected to complete Compose editor, multipart grading, or custom-session UI workflows. They are foundations only; those accepted product requirements remain open until usable UI and end-to-end tests are delivered.


## 2.1.8 release progress

- Connected existing schema-8 content block records to the editor and study card for text, headings, lists and LaTeX. Blocks can be created, edited, reordered and deleted.
- Added multipart question-part authoring for prompt, answer, explanation and positive point values. Partial-credit exam grading is not implemented yet.
- Added saved study-session preset management: save current mode/topic/question limit, apply a saved preset, and delete a preset.
- Added pure validation and filtering helpers with test source.
- No database/backup schema bump was needed. Full Android build, JUnit, Room runtime, backup round-trip, CI, signing and device checks remain pending.


## 2.1.9 release progress

- Image/media picker and offline rendering are implemented in source using existing schema-8 Base64 media fields, MIME type and alt text.
- TABLE blocks support pipe-delimited cells and newline-separated rows; GRAPH blocks support numeric x,y point series and a simple offline plot.
- No Room or backup schema change was required. Five parser JUnit test cases were added but not executed.
- Full Android build, CI, signed APK, backup round-trip and physical-device checks remain pending.

## 2.1.10 delivery update

Multipart questions now participate in timed exams. The exam captures a response for each saved part and calculates a weighted score from the part point values. Current text grading is normalized exact-match and each part is all-or-nothing. Advanced within-part partial credit, numeric tolerances, accepted-answer alternatives, manual marking, full custom-session criteria editing and structured derivations remain outstanding. This delivery does not change the approved original five-version scope.


## 2.1.10 release progress

Multipart question parts now participate in timed exams. Each part accepts an option selection or short-text answer and earns its configured points on a normalized exact match. The results screen shows per-part feedback and total weighted percentage. No database or backup schema change was needed. Advanced within-part partial credit, numeric tolerance, accepted-answer aliases, manual marking, full custom-session criteria editing and structured derivations remain outstanding. Android Gradle/JUnit, CI, signing, backup round-trip and device acceptance are still pending.
