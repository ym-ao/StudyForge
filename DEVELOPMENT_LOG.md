# StudyForge Development Log

## Current release: 2.0.0

**Date:** 2026-11-23  
**Release status:** Source package prepared; Android cloud build must be confirmed by GitHub Actions.

### Completed in this release

- Added schema version 2 and a Room migration from database version 1.
- Added review scheduling fields to questions.
- Added review-log records with rating, correctness, mistake category, mode, and scheduled due date.
- Implemented a small SM-2-inspired scheduler: Again (10-minute relearning), Hard, Good, and Easy.
- Added Due, All, Practice, and timed MCQ Exam modes.
- Added mistake-classification chips for incorrect practice/review MCQs.
- Added Progress screen with due count, accuracy, recent accuracy, learning-state counts, common mistakes, weak topics, and recent reviews.
- Expanded JSON backup to schema version 2; kept Version 1 backup imports supported.
- Changed database writes to upsert so editing a question/collection does not break linked review history/collection entries.
- Added unit tests for initial intervals, repeated Good rating, and Again relearning.
- Updated GitHub Actions to run unit tests before building the APK.
- Added and began maintaining `CHANGELOG.md`, `ROADMAP.md`, and this file.

### Verification status

- Source archive inspected and edited from the prior 1.0.1 package.
- Static review completed for database migration, scheduler, UI integration, and backup compatibility.
- Android Gradle build was **not run locally** in this environment because a configured Android SDK/Gradle build environment is unavailable here.
- Release is not marked build-verified until the GitHub Actions test and APK steps pass.

### Known limitations

- Review scheduling is SM-2-inspired, not FSRS.
- No rendered LaTeX yet.
- Exam mode is MCQ-only (maximum 10 questions, fixed 15-minute timer).
- Imported V1 backup review counts cannot reconstruct individual historical review records.
- No dynamic numerical engine, AI generation, external API, or cloud sync yet.

## Next

### Version 2.1 — reliability and usability
1. Render LaTeX in questions, answers, and explanations.
2. Add study filters by collection, subject, chapter, topic, and status.
3. Add configurable review limits, exam duration/count, and question ordering.
4. Add detailed review-history views and better session controls.
5. Expand tests for migrations, backup round-trips, and scheduler edge cases.

### Later roadmap
- Version 3.0: dynamic numerical question templates and deterministic answer checking.
- Version 4.0: AI generation and secure API/dashboard publishing.
- Version 5.0: optional cloud accounts, multi-device sync, and conflict handling.

---

## Log maintenance rule

For every future release, append a dated section here and update `CHANGELOG.md` and `ROADMAP.md`. Never claim a build passed until the GitHub Actions run is successful.
