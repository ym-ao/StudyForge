# StudyForge 2.1.12

StudyForge is an offline-first Android question library and learning app. Version 2.1.12 is a build-fix release based on CI compiler errors; it preserves the Version 2.1.11 feature set: full saved-session criteria, stepwise structured derivations, numeric-tolerance and accepted-alternative multipart grading, richer activity/exam analytics, and FSRS-6 mathematical invariant tests.

**Verification boundary:** Source ZIP only. Local pure-Kotlin harnesses and SQLite/JSON smoke scripts passed as recorded below. Full Android Gradle/KSP/JUnit, Room runtime, GitHub Actions, signed APK, and physical-device testing have not been completed.

## 2.1.12 build-fix release

- Removed duplicate `FontWeight` import in `StudyContent.kt`.
- Moved the question-editor `error` state declaration before the image-picker callback that updates it.
- No Room migration or backup schema change; versionCode advanced to 17.

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
- Save, apply and delete named study-session presets. Current UI captures study mode, selected topic and question limit.
- Added pure validation/normalization and session-filter helper tests.

## Important remaining limitations

- Image selection/offline rendering and basic table/graph blocks are implemented in 2.1.9; see the latest release section above.
- Multipart timed exam presentation and weighted all-or-nothing per-part scoring are implemented in 2.1.10. Numeric tolerances, answer aliases, manual marking and within-part partial credit remain open.
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

The source still includes the Room-backed question library and collections, flashcards, MCQs and multipart timed exams with weighted per-part results, Markdown-like rich previews, the question equation renderer integration, type/status/subject/chapter/topic/tag/collection/difficulty/exam/performance filters, FSRS-6 scheduling, persisted study/exam settings, Due/All/Practice/Random/Ordered/Topic/Weakest/Step-by-step modes, timed MCQ exams with navigation, review history, and activity-derived progress analytics.

## Known limitations — still unfinished requirements

- The structured editor supports ordered text, heading, list, LaTeX, image, table and numeric graph blocks; advanced diagram editing and selection-aware formatting remain incomplete.
- A basic saved-filter preset manager and a dedicated tag manager are implemented in source; Room runtime migration and backup round-trip tests remain pending.
- FSRS still needs upstream reference-vector validation and broader app/Room integration tests; this release implements multi-step progression but does not certify algorithm conformance.
- The step-by-step study mode remains an answer/explanation reveal, not a structured derivation-step engine.
- Custom study-session builder is partially implemented (mode/topic/limit presets); full criteria editing, numerical study mode, expanded chapter/skill analytics, broader question types, advanced multipart grading (numeric tolerances, aliases, manual marking and within-part partial credit) remains open.
- Migration runtime and backup round-trip tests, GitHub Actions verification, signed APK verification, and real-device update testing remain pending.

This ZIP is a complete source package, **not a verified APK**. Source implementation and isolated local smoke checks do not prove the complete Android app compiles or runs correctly.

## Project configuration

- Application ID: `com.studyforge.app`
- Version: `2.1.10` (versionCode `15`)
- Minimum Android: Android 8.0 (API 26)
- Compile SDK: 36; Target SDK: 35
- Kotlin 2.2.10; Jetpack Compose; Room 2.8.5
- Java 17; Android Gradle Plugin 8.13.2; Gradle 8.13
- Room database schema: version 8 (migrations 1→2→3→4→5→6→7→8)
- JSON backup schema: version 8 (imports schemas 1–8)

## Build an APK from an Android phone

The APK is built in GitHub Actions. The project is intended to be manageable from Android using Termux.

### Apply this ZIP to an existing Git repository

1. Download `StudyForge-v2.1.10-self-contained-handoff.zip` to Downloads.
2. In Termux:

```bash
pkg install unzip git
termux-setup-storage
mkdir -p ~/studyforge-update
unzip -o ~/storage/downloads/StudyForge-v2.1.10-self-contained-handoff.zip -d ~/studyforge-update
cd /path/to/your/existing/StudyForge
cp -r ~/studyforge-update/StudyForge/. .
git add -A
git commit -m "StudyForge 2.1.10 multipart exams"
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
3. Download `StudyForge-2.1.11-release-apk` for installation/update. The debug APK is for testing only.
4. Extract the artifact ZIP and install the APK.

Export and verify a JSON backup before updating. Install over the current app only when Android accepts the matching signing certificate. Do not uninstall routinely. If Android reports a signature conflict, verify the backup first and determine which certificate signed the installed app before any uninstall/reinstall.

## Project documents

- `StudyForge_Plan.txt` — original detailed product blueprint; preserved unchanged.
- `PROJECT_PLAN.md` — approved five-version master plan and current release context.
- `IMPLEMENTATION_STATUS.md` — current source-vs-plan audit and verification boundaries.
- `ROADMAP.md` — live planned/completed work and remaining gaps.
- `CHANGELOG.md` — append-only release history.
- `DEVELOPMENT_LOG.md` — dated technical history and local checks.
- `BUILD_STATUS.md` — actual build/test evidence; the 2.1.10 Android build is not yet CI verified.
- `HANDOFF_FOR_NEW_CHAT.md` — prompt for continuing in a fresh conversation.
- `PROJECT_FILE_GUIDE.md` — source and documentation map.
- `SIGNING_SETUP.md` — release-key configuration and protection.
- `THIRD_PARTY_NOTICES.md` — third-party library notices and algorithm references.
- `backup-example.json` — example schema 8 JSON backup including saved filter presets and tag catalogue.

## Privacy and approved product scope

Question-library, scheduling, study, and analytics data remain local. No account or remote service is required for normal use. Exported backups may contain personal notes; keep them private. Future AI and cloud features must use a secure server-side proxy/secret store.

Version 2.1.10 advances part of V1/V2; it does not declare Version 1 or Version 2 complete. The original five-version scope in `StudyForge_Plan.txt` remains authoritative. Unfinished requirements must remain in the roadmap and must not be silently removed.
