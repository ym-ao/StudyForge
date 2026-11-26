## Development tooling and test cadence

Read `TOOLING.md` for the reproducible Android toolchain requirements and `TESTING_STRATEGY.md` for resource-aware verification. Heavy Gradle/emulator testing is intentionally concentrated at V3.5, V3.9 and V3.10, after substantial core feature groups, or when a bug/regression needs investigation. Lightweight checks remain appropriate during ordinary batches. The current V3.0 acceptance gates remain the immediate priority in `HANDOFF_FOR_NEW_CHAT.md` and `V3_FINAL_ACCEPTANCE_RUNBOOK.md`.

## Version 3 planning baseline — 2026-11-26

Version 3 is planned in three substantial batches: 3.0 scalable data and study foundation (Room Paging 3); 3.1 deterministic numerical question engine; 3.2 stepwise problem solving and advanced question formats. See `VERSION_3_DELIVERY_PLAN.md`, `ROADMAP.md`, and `PROJECT_PLAN.md`. V3 source implementation is authorized in parallel; V2 closeout remains a release/merge gate. The original `StudyForge_Plan.txt` is preserved unchanged.

The user has confirmed on-device that the Cloze crash is resolved, the one-card Study flow works, the rating buttons fit correctly, and Dark Mode works. The compact bottom-navigation source update and remaining Progress/other-screen visual polish still require final phone confirmation.


## Latest V3.0 source continuation — Batch 7 (2026-11-26)

Study no longer observes every rich-content block or multipart question part. The active card loads only its own ordered content blocks; the bounded exam snapshot loads parts only for its selected question IDs. Static Batch 7 readiness is 9/9. Host-side synthetic SQLite benchmarks passed functional checks at 10k and 50k questions, but `EXPLAIN QUERY PLAN` exposed full scans for Library first-page and Due queue ordering, plus a scan for collection paging. No speculative index migration was added. Android/Room compilation, instrumentation, CI, memory/frame and device performance remain unverified. See `V3_BATCH_7_SCALABILITY.md` and `HANDOFF_FOR_NEW_CHAT.md`.

---

## Latest source continuation (2026-11-25)

Library bulk actions now consistently apply to the full selected question set even after filters change; the Select visible control remains scoped to the current results. This is source-implemented with regression-test source and static CI verification. Android JUnit/UI execution is not yet verified. FSRS is excluded from this continuation.

# StudyForge 2.1.24 — V2 preparation continuation

The pinned py-fsrs v6.0.0 mismatch has been corrected in source. StudyForge now uses the exact 21-parameter vector and pinned v6.0.0 update/learning policies; the standalone Kotlin comparison matches all transitions in `test_memo_state` (final S=49.4472/D=6.8271). The diagnostic is `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. A lightweight Kotlin runner passed 32 scheduler regression tests. Android Gradle/JUnit, CI and device verification remain pending; no APK release was made. See `BUILD_STATUS.md` and `SOURCE_PLAN_AUDIT_2.1.24.md`.

The current source remains 2.1.24 / versionCode 28, Room schema 16 and JSON backup schema 15. The export snapshot consistency check is a static contract check, not proof of Android runtime backup/restore. The CI workflow was corrected to call the 2.1.24 verifier and now includes a source-integrity gate. A same-day Hard FSRS-6 regression test was added. Local static/migration/backup checks passed; Android Gradle/JUnit, Room runtime, real backup/restore, GitHub Actions, signing and physical-device acceptance remain unverified. This is not a new APK release. See `BUILD_STATUS.md`, `ROADMAP.md` and `DEVELOPMENT_LOG.md`.


## Latest non-FSRS continuation — 2026-11-25

- Library bulk actions now include status/difficulty, tag add/remove, and collection membership add/remove, with version snapshots for changed questions.
- Saved Library filters display live match counts recalculated against current questions and collection links. These remain saved criteria presets, not a separate dynamic-collection entity.
- JSON export captures all persisted data groups in one Room transaction before serialization.
- Static source/backup/release checks and local SQLite migration smoke checks are documented in `BUILD_STATUS.md`. Android build/JUnit, Room runtime migrations, real backup/restore, CI execution, signing and physical-device acceptance remain unverified. FSRS was excluded from this continuation.

---

# StudyForge 2.1.24 — continued verification

The current working source remains 2.1.24 / versionCode 28. This audit adds initial-memory-state equation assertions for all four ratings and a source-vs-plan audit. It does not claim full upstream golden-vector parity or an APK release. See `SOURCE_PLAN_AUDIT_2.1.24.md` and `BUILD_STATUS.md` for evidence and remaining acceptance gates.


# StudyForge 2.1.24 — V3.0 source development in progress

StudyForge is an offline-first Android question library and learning app. The current app identity remains 2.1.24 / versionCode 28, Room schema 16 and JSON backup schema 15. V3.0 Batches 1–8 add the Paging foundation, production Library and Study queue migrations, Home/Progress aggregates, lazy rich-content hydration, query-plan-driven indexes and scalability diagnostics. This is unreleased source work: Android compilation, Room runtime, CI, signing and device acceptance are not verified. See `V3_BATCH_2_LIBRARY_MIGRATION_AUDIT.md`, `V3_BATCH_4_STUDY_QUEUE_MIGRATION.md`, `V3_BATCH_5_HOME_PROGRESS_AGGREGATES.md`, `BUILD_STATUS.md` and `HANDOFF_FOR_NEW_CHAT.md` for the current boundary and remaining work.

Version 2.1.24 corrects FSRS-6 long-term review-state calculations and adds reference-equation regression coverage; it retains the 2.1.23 learner-revealed HINT blocks.

## 2.1.24 highlights

- Corrected FSRS-6 long-term review stability to use pre-review difficulty and the published 0.001 stability floor. Added four-rating reference-equation regression fixtures.

- Added structured HINT blocks that authors can edit/reorder and learners can reveal on demand.
- Reuses schema-15 content-block storage and backup; no migration required.

## 2.1.22 highlights

- A new card rated Good advances to the second configured Learning step when one exists.
- A new card rated Hard enters Learning at step 0 instead of graduating immediately.
- Easy and final Good still graduate; Again/restart and existing multi-step progression are retained.
- Room and JSON backup remain schema 15; no migration is needed.

## 2.1.21 highlights

- Compare historical question snapshots against the current question, including before/current values for changed fields.
- Match rich blocks and multipart items by stable IDs; summarize embedded media without exposing Base64 payloads.
- Room and JSON backup remain schema 15; no migration is needed.

## 2.1.20 highlights

- Filter questions by source title/reference and inclusive created/modified date ranges (YYYY-MM-DD); all new criteria persist in saved filter presets and JSON backups.
- Version history now compares core content fields with the current saved question and labels which fields changed; restore continues to preserve scheduling and review history.
- Progress includes scored-review accuracy grouped by question type, excluding unscored logs and separating deleted-question history.
- Room schema 15 adds saved-filter fields through additive migrations 13→14 and 14→15; JSON backup schema 15 still accepts schemas 1–15.
- Local SQLite migration and static source checks are recorded in BUILD_STATUS.md. Full Android build, CI, signing and device acceptance remain pending.

## 2.1.19 highlights

- Questions gained source title and chapter/page/section/URL provenance, preserved through study display, search, CSV, JSON backup and version history.
- CSV/Anki imports gained preview-first confirmation, duplicate detection and row-level validation diagnostics.
- Room schema 13 and JSON backup schema 13; migrations and legacy imports retained.

## 2.1.18 highlights

- Full JSON backup remains the complete portable format; CSV/Anki TSV are intentionally limited to simple question fields.
- Question version restore changes content and collections while preserving the current review history and FSRS scheduling state.
- Manual scheduling can move a question to now, tomorrow or seven days, or reset scheduler memory without deleting lifetime review records.
- Room schema 12 and JSON backup schema 12; imports from schemas 1–12 remain supported.
- Focused SQLite and standalone Kotlin checks passed locally. Full Android build, JUnit, CI, signing and device acceptance remain pending.


## 2.1.16 highlights

- Selection-aware Bold, Italic, Heading, Bullet and Code actions wrap selected text or insert a selected placeholder at the cursor.
- The formatting field is available for question, answer, explanation and multipart prompt/answer/explanation content.
- Progress adds median exam score, score-band distribution and a five-attempt recent-vs-previous trend once at least ten attempts exist.
- Room schema 10 and JSON backup schema 10 are unchanged; no migration is required.
- Standalone Kotlin harnesses passed five formatting assertions and four exam-analytics assertions; SQLite migration 7→8, 8→9, 9→10, schema-10 backup checks and the 2.1.16 source consistency script passed.

## 2.1.15 highlights

- Completed exams are saved with start/completion times, duration, question/answer counts, weighted points, percentage, and question/part response snapshots.
- Exam history remains available if questions are later edited or deleted.
- Progress includes saved exam count, average score, best score and the ten most recent attempts.
- Schema-10 backups include attempt history; schemas 1–9 remain importable. Importing an older backup does not erase existing exam attempts.

## Historical release notes — 2.1.14 baseline

StudyForge is an offline-first Android question library and learning app. Version 2.1.15 adds persisted timed-exam attempts, history and backup support; it retains the 2.1.14 manual examiner-awarded partial-credit workflow. It preserves the Version 2.1.11 feature set: full saved-session criteria, stepwise structured derivations, numeric-tolerance and accepted-alternative multipart grading, richer activity/exam analytics, and FSRS-6 mathematical invariant tests.

**Verification boundary:** Source ZIP only. Local pure-Kotlin harnesses and SQLite/JSON smoke scripts passed as recorded below. Full Android Gradle/KSP/JUnit, Room runtime, GitHub Actions, signed APK, and physical-device testing have not been completed.

## 2.1.13 build-fix release

- Removed duplicate `FontWeight` import in `StudyContent.kt`.
- Moved the question-editor `error` state declaration before the image-picker callback that updates it.
- No Room migration or backup schema change; versionCode advanced to 18.

## Previous features in 2.1.11

- App version 2.1.11 / versionCode 16; Room schema 9; JSON backup schema 9 (imports schemas 1–9).
- Saved study sessions now edit and persist subject, chapter, topic, tag, difficulty, exam/syllabus, question type, publication status, performance group, mode and question limit. The same filters apply when launching a saved Exam session.
- Added DERIVATION content blocks. Author one step per line as `title | LaTeX equation | explanation`; validation checks for non-empty, bounded steps. Editor and study card provide a step-by-step reveal UI.
- Multipart question parts now support accepted text alternatives, absolute numeric tolerance and relative numeric tolerance. Numeric answers are compared deterministically; the allowed error is the greater of absolute tolerance and relative tolerance × absolute expected value.
- Progress analytics now include 7-day review activity, distinct active review days, exam response count/answer accuracy, and chapter-level recorded mistakes.
- Added FSRS-6 reference-invariant tests for the published forgetting-curve property R(S,S)=0.9, the published Good initial stability, and 90% interval behavior. These are mathematical invariant checks, not the complete upstream golden-vector suite.
- Added Room migration 8→9 for three part-grading columns and three session-filter columns. Older backups default new fields safely; JSON schema 9 exports/imports the fields.

## Verification performed

- Standalone Kotlin harness: numeric tolerance, accepted alternatives, derivation parsing/validation and saved-session criteria passed.
- SQLite migration 7→8 script: 7 checks passed.
- SQLite migration 8→9 smoke test: existing rows preserved and all six new columns verified.
- Schema-9 backup JSON example parses and static compatibility check passes.
- Parser-only Kotlin scan found no syntax diagnostics; full Android dependencies were unavailable.
- Original `StudyForge_Plan.txt` remains byte-for-byte unchanged.

## FSRS-6 reference basis

The included invariant tests use the published FSRS-6 forgetting-curve definition and default parameters. See the [FSRS-6 model reference](https://open-spaced-repetition.github.io/ts-fsrs/api/ts-fsrs/models/fsrs-6) and [FSRS-6 mathematical specification](https://github.com/yalexaner/fsrs-kt/blob/master/SPEC.md). This does not substitute for the full upstream golden-vector test suite.

## Not verified

- Full Android Gradle/Compose/KSP compilation and official Android JUnit task.
- FSRS upstream reference golden vectors (the included tests are published-equation invariants only).
- Room runtime migration, actual app backup/restore round-trip, GitHub Actions, signed APK/certificate continuity and device UI behavior.


StudyForge is an offline-first Android question library and learning app. Version 2.1.10 adds multipart timed exam answer capture and weighted per-part scoring; 2.1.9 adds image/media selection with embedded offline rendering plus table and numeric graph content blocks.

**Verification boundary:** This is a source ZIP. Full Android Gradle/KSP/JUnit, Room runtime, backup round-trip, GitHub Actions, signed APK, and physical-device testing have not been completed in this environment.

## What's new in 2.1.10

- App version 2.1.10 / versionCode 15; Room schema 8 and JSON backup schema 8 remain unchanged.
- Timed exams now include questions with authored multipart parts, alongside standard MCQs.
- Multipart answers support option selection when a part has options, or short text entry otherwise.
- Results calculate weighted points per part, show earned/possible points and per-part feedback, and calculate a total weighted percentage.
- Exam navigation/status now recognizes multipart answers.
- Added pure Kotlin multipart grading rules and five regression test cases.

### Grading boundary

Multipart grading is exact-match per part after trimming, collapsing whitespace and ignoring letter case. It awards each part's configured points or zero; it does not yet support numeric tolerances, accepted-answer aliases, partial marks within a free-text part, manual examiner marking, or negative marking. Standard MCQs retain their existing single-answer grading.

### Verification boundary

The pure multipart scoring helper was compiled and exercised in a standalone Kotlin harness. Full Android Gradle/Compose/KSP/JUnit, GitHub Actions, signed APK, Room runtime, backup round-trip and device verification remain pending.

## What's new in 2.1.9

- App version 2.1.9 / versionCode 14; Room schema 8 and JSON backup schema 8 remain unchanged.
- IMAGE content blocks now use Android's document picker. Selected image bytes are copied into the existing mediaBase64 field, with MIME type and alt text, so no external URI permission or network connection is needed to display the image later.
- Image selection validates image MIME type and caps the original file at 1.5 MB. Images render in the editor and study card from embedded bytes.
- TABLE blocks use pipe-separated cells and newline-separated rows (maximum 30 rows and 8 columns in preview).
- GRAPH blocks accept numeric x,y pairs separated by comma, semicolon or tab, one pair per line; preview plots up to 200 points offline.
- Added pure table/graph parsing helpers and JUnit test source.

## Important limitations in 2.1.9

- This release implements milestones 1 and 2 only: image/media blocks, tables and numeric graphs. It does not complete multipart exam grading, full custom-session criteria editing or structured derivations.
- Image payloads are embedded in the existing JSON backup contentBlocks entries. Large images increase backup size; per-image source limit is 1.5 MB.
- Graphs are simple connected numeric x-y plots, not a full graph editor with axis titles, units, multiple series or annotations. Tables are pipe-delimited plain text, not a spreadsheet editor.
- Full Android Gradle/KSP/JUnit, Room runtime, backup round-trip, GitHub Actions, signed APK and physical-device verification remain pending.

## What's new in 2.1.8

- App version 2.1.8 / versionCode 13; Room schema 8; JSON backup schema 8 (no schema bump required).
- Add, edit, reorder and delete ordered TEXT, HEADING, LIST and LATEX blocks in the question editor.
- Render saved text, heading, list and LaTeX blocks on the study card.
- Author multipart question parts with prompts, answers, explanations and positive point values.
- Save, apply and edit named study-session presets with full saved criteria (mode, topic, collection, subject, chapter, tag, difficulty, exam, question type, status, performance and limit). Editing is stable-ID based, duplicate-name collisions are blocked, and deletion requires confirmation.
- Added pure validation/normalization and session-filter helper tests.

## Important remaining limitations

- Image selection/offline rendering and basic table/graph blocks are implemented in 2.1.9; see the latest release section above.
- At 2.1.10, multipart timed exam presentation and weighted all-or-nothing per-part scoring were implemented. Numeric tolerances and answer aliases followed in 2.1.11; manual examiner-awarded within-part partial credit was added in 2.1.14.
- Session presets currently capture mode, topic and question limit; full subject/chapter/tag/difficulty/exam controls are outstanding.
- Structured derivation steps and execution remain outstanding.
- No Android build, JUnit, CI, signed APK, or device verification is claimed.

## What's new in 2.1.6 (historical)

- Dedicated tag manager: create, rename and delete tags, view question usage counts, and apply rename/delete changes to affected questions.
- Room migration 6→7 adds a normalized tag catalogue while retaining existing question tag text for compatibility.
- JSON schema 7 includes tag catalogue; imports schemas 1–7.

### Saved library filter presets (2.1.5)
- Save the current search and library-filter combination under a name.
- Reapply saved presets from the Library screen with one tap.
- Save again using the same name to update the existing preset.
- Manage and delete presets with a confirmation step.
- Presets preserve search, type, status, subject, chapter, topic, tag, collection, difficulty, exam, and performance criteria.
- Collection links are cleared safely if the referenced collection is deleted.

### Data compatibility
- Room schema is now version 8. Migration 5→6 added `saved_filter_presets`; migration 6→7 adds the normalized `tags` catalogue and unique name index.
- JSON backup schema 8 exports/imports saved filter presets and the tag catalogue. Imports accept schemas 1–8.
- Older backup merges do not delete saved presets already on the device.

## Prior capabilities retained in source

### Learning and relearning progression
- The learning and relearning settings now accept multiple comma-separated delays (for example `1, 10` or `10, 30`).
- Pressing **Again** on a new card starts its Learning sequence at step 1.
- Pressing **Again** on a graduated Review card starts its Relearning sequence at step 1 and records a lapse.
- **Good** advances to the next configured step; after the final step, the card graduates to FSRS Review scheduling.
- **Hard** repeats the current learning step. **Again** restarts the applicable sequence. **Easy** graduates immediately.
- The current Learning/Relearning step and delay are shown on the study card.
- Repeated delay values are preserved as separate steps; invalid/out-of-range entries are ignored and an empty sequence safely falls back to 10 minutes.

### Prior 2.1.4 feature retained
- Multi-step Learning/Relearning and the current step position remain in the source. Migration 4→5 added `learningStepIndex`; migration 5→6 adds saved presets without replacing question data.
- Backup schema 8 retains all earlier question, review and settings fields while adding structured content blocks, multipart parts and session presets.

## Current app capabilities retained

The current source includes the Room-backed question library and collections; flashcards, MCQs, True/False, Numerical and Cloze questions; multipart timed exams with weighted scoring and examiner-adjustable per-part awards; persisted exam-attempt snapshots; ordered rich-content blocks (text, heading, list, LaTeX, image, table, graph, derivation and hint); selection-aware text formatting; source metadata; saved library filters and full saved study-session criteria; tag management; CSV/Anki TSV interchange with preview and diagnostics; question version history/diff/restore; FSRS-6 scheduling; manual scheduling; and review/exam progress analytics.

## Known limitations — still unfinished requirements

- The pinned py-fsrs v6.0.0 `test_memo_state` sequence now passes in the standalone source harness, matching every compared transition and final S=49.4472/D=6.8271. Broader upstream-suite parity and Android Gradle/JUnit execution remain pending.
- The structured editor has table/graph validation and bounded previews, but richer block-specific editing, inline/display equation placement/copy, accessibility and responsive device acceptance remain open.
- Saved filter presets, tag management, multi-select bulk status/difficulty, tag add/remove, and collection membership add/remove are source-implemented; dynamic smart-collection rules and broader bulk editing remain partial. Full saved study-session criteria are also source-implemented.
- DERIVATION blocks reveal authored steps one at a time; there is no symbolic algebra solver. Dynamic numerical templates, variable constraints, unit conversion/validation and generated distractors remain Version 3 scope.
- Analytics include exam-attempt history, score summaries, question-type accuracy and a recorded 14-day review timeline/streak. Deeper skill/progress-over-time views and reliable time-spent measurement remain open.
- Room instrumentation source currently tests current-schema persistence/reopen and deletion cleanup; populated historical migrations across schemas 1–15 and actual Android JSON export/import round-trip remain unverified.
- Android Gradle/Compose/KSP/JUnit, GitHub Actions execution, signed APK/certificate continuity and physical-device update/offline acceptance remain pending.

This ZIP is a complete source package, **not a verified APK**. Source implementation and isolated local smoke checks do not prove the complete Android app compiles or runs correctly.

## Project configuration

- Application ID: `com.studyforge.app`
- Version: `2.1.24` (versionCode `28`)
- Minimum Android: Android 8.0 (API 26)
- Compile SDK: 36; Target SDK: 35
- Kotlin 2.2.10; Jetpack Compose; Room 2.8.5
- Java 17; Android Gradle Plugin 8.13.2; Gradle 8.13
- Room database schema: version 15 (additive migrations 1→2 through 14→15)
- JSON backup schema: version 15 (imports schemas 1–15)

## Build an APK from an Android phone

The APK is built in GitHub Actions. The project is intended to be manageable from Android using Termux.

### Apply this ZIP to an existing Git repository

1. Download `StudyForge-2.1.24-v2-audit-continuation.zip` to Downloads.
2. In Termux:

```bash
pkg install unzip git
termux-setup-storage
mkdir -p ~/studyforge-update
unzip -o ~/storage/downloads/StudyForge-2.1.24-v2-audit-continuation.zip -d ~/studyforge-update
cd /path/to/your/existing/StudyForge
cp -r ~/studyforge-update/StudyForge/. .
git add -A
git commit -m "StudyForge 2.1.24 V2 audit continuation"
git push
rm -rf ~/studyforge-update
```

Replace `/path/to/your/existing/StudyForge` with your actual repository location. Do not run `git init` again in an existing repository.

### GitHub Actions signing secrets

The workflow keeps the fixed non-secret alias `studyforge-release` and validates it before the release build. Configure only these three repository secrets, using your existing private keystore and credentials:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_STORE_PASSWORD`
- `SIGNING_KEY_PASSWORD`

`SIGNING_KEY_ALIAS` is not required. Never put the keystore, Base64 text, passwords, GitHub tokens, or AI keys in the project ZIP or public repository. Do not regenerate the existing signing key for this update.

### Get the APK

1. Open GitHub repository → Actions → latest **Build StudyForge APK** run.
2. Confirm the unit-test step, debug build, signing preflight, signed release build, and artifact uploads succeeded.
3. Download `StudyForge-2.1.24-release-apk` for installation/update. The debug APK is for testing only.
4. Extract the artifact ZIP and install the APK.

Export and verify a JSON backup before updating. Install over the current app only when Android accepts the matching signing certificate. Do not uninstall routinely. If Android reports a signature conflict, verify the backup first and determine which certificate signed the installed app before any uninstall/reinstall.

## Project documents

- `StudyForge_Plan.txt` — original detailed product blueprint; preserved unchanged.
- `PROJECT_PLAN.md` — approved five-version master plan and current release context.
- `IMPLEMENTATION_STATUS.md` — current source-vs-plan audit and verification boundaries.
- `ROADMAP.md` — live planned/completed work and remaining gaps.
- `CHANGELOG.md` — append-only release history.
- `DEVELOPMENT_LOG.md` — dated technical history and local checks.
- `BUILD_STATUS.md` — actual build/test evidence; the 2.1.24 Android build is not yet CI verified.
- `HANDOFF_FOR_NEW_CHAT.md` — prompt for continuing in a fresh conversation.
- `PROJECT_FILE_GUIDE.md` — source and documentation map.
- `SIGNING_SETUP.md` — release-key configuration and protection.
- `THIRD_PARTY_NOTICES.md` — third-party library notices and algorithm references.
- `backup-example.json` — example schema 15 JSON backup including current question and saved-filter fields.

## Privacy and approved product scope

Question-library, scheduling, study, and analytics data remain local. No account or remote service is required for normal use. Exported backups may contain personal notes; keep them private. Future AI and cloud features must use a secure server-side proxy/secret store.

Version 2.1.24 advances part of V1/V2; it does not declare Version 1 or Version 2 complete. The original five-version scope in `StudyForge_Plan.txt` remains authoritative. Unfinished requirements must remain in the roadmap and must not be silently removed.


## 2.1.14 — manual multipart partial credit

On the exam-results screen, each multipart part now shows its automatic score and an editable examiner-awarded score. The award is bounded between zero and the part's configured maximum. The adjusted total determines the review outcome when the user saves grading and finishes. No database or backup schema change was required.

**Verification status:** source implementation and test source added. Android Gradle tests, CI, signed APK and device installation are not verified in this package.


## 2.1.23 — learner-revealed hint blocks

- Added `HINT` as a supported structured content-block kind. Authors can add, edit, reorder and delete hint blocks in the question editor; existing content-block storage, question snapshots and JSON backup paths are reused.
- During study, hint content is hidden behind a **Reveal hint** action and is displayed only after the learner chooses to reveal it. Hint reveal state is scoped to the current question in the active screen; it is not persisted as a review event.
- No Room migration or JSON backup schema change: both remain version 15. Existing backups and unknown/older block kinds retain their prior behavior.
- Added static release consistency checks and updated GitHub Actions artifact labels to 2.1.23.
- Verification in this package: Python source-consistency and migration/backup smoke scripts are run locally and recorded in `BUILD_STATUS.md`. Android Gradle/JUnit, Room runtime, CI, signing and device acceptance are not claimed unless separately evidenced.

### V2 runtime verification additions (2026-11-25)

Android instrumentation tests now exercise current Room database creation, persistence across database reopen, and dependent-row cascade deletion. GitHub Actions is configured to execute them on an Android API 35 emulator. The tests are not considered passed until a successful CI run is available. Historical migration and complete app backup/restore runtime tests are still required.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



### V2 verification additions

GitHub Actions now runs Android lint, unit tests, and Room instrumentation tests on an API 35 emulator. The Room tests cover current-schema persistence/reopen behavior, dependent-row cascades, and collection deletion cleanup. These are configured checks, not proof of a passing run; check the Actions log for actual results.


### Latest V2 source hardening (2026-11-25)

Imported embedded images are now validated as Base64 and limited to the same 1.5 MB decoded size as images added through the editor. A standalone Kotlin smoke harness and static Python checks passed; full Android JUnit and app-level JSON backup/restore still require CI/device execution. No release version or data schema was changed.

The Progress screen also includes a 14-day review activity timeline and current streak based on recorded review timestamps. Time spent is not estimated from review timestamps.


## Latest test-source continuation — 2026-11-25 (FSRS excluded)

Room instrumentation source now includes a current-schema close/reopen test for ordered TEXT/HINT/IMAGE/TABLE/GRAPH content blocks and embedded offline image metadata. The source-integrity gate checks that coverage. The instrumentation has not yet run; this does not establish Android runtime or backup/restore success. Version/schema identity remains 2.1.24 / 28 / Room 15 / JSON 15.

## 2026-11-25 FSRS-6 pinned v6.0.0 compatibility correction

- Corrected the scheduler against the actual pinned upstream source at commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81`, not current upstream `main`.
- Replaced the 21 defaults with the exact pinned vector; restored the clamped initial-Easy mean-reversion target and the Good/Easy-only same-day stability floor.
- Corrected Hard learning/relearning delays, completed-24-hour elapsed-day semantics, ties-to-even interval rounding, and empty learning/relearning-step behavior.
- Added pinned fixture and focused regression coverage. The standalone diagnostic compiles the exact Kotlin scheduler and reports a per-transition pass; final S=49.447237416 / D=6.827105750. A lightweight Kotlin/JUnit-compatible runner compiled and executed 32 scheduler tests: 32 passed, 0 failed.
- The 32-test runner is not Android Gradle/JUnit. The ZIP has no Gradle wrapper and this environment has no Gradle executable. Android build, actual Gradle/JUnit, Room runtime, backup/restore, CI, signing and device tests remain outstanding.
- No APK or version bump; version 2.1.24/versionCode 28, Room 15 and JSON backup 15 are unchanged. Original `StudyForge_Plan.txt` unchanged.


## V3.0 Batch 3 — Study queue query foundation

The source now includes a validated `StudyQueueQuery` and a Room-side bounded ID snapshot query for Study modes and saved-session criteria. It supports collection membership and performance filtering, uses mode-specific ordering with a stable ID tie-breaker, and hydrates only selected cards in snapshot order. Run `python3 tools/verify_v3_batch3_readiness.py` for source-shape checks.

This is not yet the active Study-screen migration: `StudyScreen` still receives the legacy full question list. Process-recreation persistence, daily quota/order parity, database-backed aggregates, Android runtime validation and large-library benchmarks remain pending.

## V3.0 Batch 4 — production Study queue migration

The active Study screen now requests bounded database ID snapshots and hydrates only those cards, rather than receiving the full question table. Due New/Review quotas are independently selected in Room, saved-session criteria and Exam eligibility are SQL-filtered, and topic options are database-backed. Queue IDs and their criteria fingerprint are saved through Compose saved state for restoration when Android restores the screen. Source checks and standalone SQLite query smoke checks pass; Android build, Room instrumentation, CI, physical-device testing and 10k/50k benchmarks remain pending. See `V3_BATCH_4_STUDY_QUEUE_MIGRATION.md`.


## Latest V3.0 source continuation — Batch 8 (2026-11-26)

Room schema is now 16 while JSON backup schema remains 15. Additive migration 15→16 adds only the `questions(updatedAt DESC, id ASC)` and `questions(dueAt ASC, id ASC)` indexes, based on Batch 7 query-plan evidence. The Due snapshot uses equivalent null-first ascending order to match the index. The host synthetic benchmark now gates Library, Due and collection first-page plans against the intended index and absence of temporary ORDER BY sorting. These are host SQLite results, not Android/Room/device acceptance. See `V3_BATCH_8_DATABASE_OPTIMIZATION.md` and `HANDOFF_FOR_NEW_CHAT.md`.
