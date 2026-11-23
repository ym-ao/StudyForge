# StudyForge — Master Project Plan

**Current source version:** 2.1.0  
**Android versionCode:** 5  
**Plan status:** Living plan; original blueprint preserved in `StudyForge_Plan.txt`  
**Platform:** Android (Kotlin + Jetpack Compose)  
**Working style:** Develop in staged releases, with each ZIP containing the source and updated project records.

> **Important status correction:** This project has source code for versions 1.0.0 through 2.1.0. The original planning file said that no implementation had started; that statement described the planning baseline and is now historical. The 2.1.0 package adds release signing infrastructure but is not yet verified as a successful APK build, and some items promised by the major-version plan are still incomplete. See `IMPLEMENTATION_STATUS.md` and `BUILD_STATUS.md` before deciding what to implement next.

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

The first useful APK should be genuinely usable for study, not an empty prototype. The architecture should prepare for later releases without prematurely introducing AI/cloud dependencies. The current source has much of the local foundation, but rendered LaTeX and a fully structured rich editor are not yet implemented.

### Version 2.0 — Learning engine

- Spaced repetition using **FSRS**.
- Exam mode and timed tests.
- Multiple study modes.
- Mistake tracking.
- Weak-topic analytics.
- Custom review settings.

The current 2.0.0/2.0.1 implementation is a learning-engine foundation, but its scheduler is SM-2-inspired rather than FSRS. It also has a limited fixed-format exam and basic analytics. Version 2 should remain open until the planned FSRS and configurable learning features are delivered and tested.

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

The current source version is `2.1.0` with `versionCode = 5`. This 2.1.0 package adds conditional release-signing support and a signed APK workflow. It is not yet the full 2.1.0 learning-engine completion milestone; the planned FSRS, settings, LaTeX rendering, richer filters, and additional tests remain open. The version is a source candidate until CI and device checks pass. Do not reuse a version code. Increment it for every APK release. Do not mark Version 2 or the 2.1.0 completion scope as finished while important Version 1 and Version 2 requirements remain incomplete. The current 2.1.0 source is a signing-enabled development/release candidate; continue with the outstanding approved items before calling the milestone complete.

## Next recommended work

Before starting Version 3.0, prioritize the gaps listed in `IMPLEMENTATION_STATUS.md`. The current 2.1.0 package establishes a stable release-signing path. The rest of the already-approved 2.1.0 completion scope remains required and outstanding:

1. Implement and validate the planned FSRS scheduler (or explicitly document a user-approved change to the plan before replacing it).
2. Add configurable new/review limits, review order, and exam question-count/duration settings.
3. Improve the study modes and topic/collection filters.
4. Implement real LaTeX rendering in prompts, answers, and explanations while retaining editable source.
5. Expand migration, backup/restore, scheduler, and exam tests.
6. Verify the full GitHub Actions test + APK build and then test installation/update on the Android device.

Avoid scope creep: keep dynamic numerical templates for Version 3, AI/API workflows for Version 4, and cloud/multi-user expansion for Version 5 unless the user explicitly changes the roadmap.

## Technical baseline from current source

- Kotlin + Jetpack Compose.
- Room / SQLite local database.
- App ID: `com.studyforge.app`.
- Minimum SDK 26; compile SDK 36; target SDK 35.
- Java 17 target.
- Android Gradle Plugin 8.13.2; Gradle 8.13 in GitHub Actions.
- Current Room database schema version 2; migration `1 -> 2` is in `StudyDatabase.kt`.
- Backup JSON schema version 2; import accepts schema versions 1 and 2.
- Current scheduler code is `ReviewScheduler.kt` and is SM-2-inspired, not FSRS.
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
