# Project file guide

## Android source

- `app/src/main/java/com/studyforge/app/MainActivity.kt` — Android activity entry point.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — Compose app shell, navigation, library/editor/collections, backup and restore, home UI.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — study modes, exam UI, and progress UI.
- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — Room entities, DAO, database, and migration 1-to-2.
- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt` — current SM-2-inspired scheduler and review recording.
- `app/src/test/java/com/studyforge/app/data/ReviewSchedulerTest.kt` — scheduler unit tests.

## Build and app configuration

- `app/build.gradle.kts` — Android app config and dependencies.
- `build.gradle.kts` — plugin versions.
- `settings.gradle.kts` — plugin/dependency repositories and included module.
- `gradle.properties` — Gradle properties.
- `.github/workflows/android-apk.yml` — GitHub Actions unit-test and APK build workflow.
- `app/src/main/AndroidManifest.xml` — app manifest.

## Planning and continuity records

- `PROJECT_PLAN.md` — current high-level product/release plan.
- `StudyForge_Plan.txt` — preserved original detailed blueprint.
- `IMPLEMENTATION_STATUS.md` — source-vs-plan feature audit.
- `ROADMAP.md` — upcoming work and milestone status.
- `CHANGELOG.md` — release history.
- `DEVELOPMENT_LOG.md` — dated technical history.
- `BUILD_STATUS.md` — build/test verification status.
- `HANDOFF_FOR_NEW_CHAT.md` — prompt for a fresh conversation.
- `PROJECT_FILE_GUIDE.md` — this source inventory.
- `backup-example.json` — example version 2 backup structure.

- `SIGNING_SETUP.md` — secure release-signing setup instructions.
