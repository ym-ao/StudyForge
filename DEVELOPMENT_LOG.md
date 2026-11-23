# StudyForge Development Log

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
