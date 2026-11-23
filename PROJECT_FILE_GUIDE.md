# Project file guide

## Android source

- `app/src/main/java/com/studyforge/app/MainActivity.kt` — Android activity entry point.
- `app/src/main/java/com/studyforge/app/ui/StudyForgeApp.kt` — Compose app shell, navigation, library/editor/collections, backup and restore, home UI.
- `app/src/main/java/com/studyforge/app/ui/StudyScreens.kt` — study modes, configurable exam UI, and progress UI.
- `app/src/main/java/com/studyforge/app/ui/SettingsScreen.kt` — persistent FSRS review and exam settings UI.
- `app/src/main/java/com/studyforge/app/ui/StudyContent.kt` — native LaTeX view and Markdown-like rich-content renderer.
- `app/src/main/java/com/studyforge/app/data/StudyDatabase.kt` — Room entities, DAO, database, and migrations 1-to-2 and 2-to-3.
- `app/src/main/java/com/studyforge/app/data/ReviewScheduler.kt` — FSRS-6 scheduler implementation and review recording.
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
- `backup-example.json` — example version 3 backup structure.

- `SIGNING_SETUP.md` — secure release-signing setup instructions.


## Signing baseline retained from 2.1.1

`SIGNING_SETUP.md` documents the three required GitHub Actions secrets. The non-secret alias `studyforge-release` is fixed in `.github/workflows/android-apk.yml` and validated before Gradle release packaging.


- `THIRD_PARTY_NOTICES.md` — RaTeX MIT attribution and FSRS algorithm reference.
