# Build status

## StudyForge 2.0.0 (versionCode 3)

- Added review scheduling, review history, mistake tracking, Practice mode, timed MCQ Exam mode, and a Progress screen.
- Added a Room database migration from version 1 to version 2.
- Version 2 JSON export includes scheduler state and review logs; import remains compatible with Version 1 backups.
- GitHub Actions now runs unit tests before building the debug APK.
- `CHANGELOG.md`, `ROADMAP.md`, and `DEVELOPMENT_LOG.md` are maintained with the release.

**Verification:** source changes and JSON sample were inspected. This environment does not have a configured Android Gradle build/SDK, so the APK has not been built here. Mark 2.0.0 as build-verified only after both GitHub Actions test and APK steps pass.
