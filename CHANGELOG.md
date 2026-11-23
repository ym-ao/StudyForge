# Changelog

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
