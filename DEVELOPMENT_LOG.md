# StudyForge Development Log

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

## Current source snapshot: 2.1.0

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
