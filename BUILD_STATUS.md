# Build status — StudyForge 2.1.2

## Current source snapshot

- **versionName:** `2.1.2`
- **versionCode:** `7`
- **Room database version:** `3`
- **JSON backup schema:** `3` (imports 1, 2, 3)
- **Gradle:** `8.13`; Android Gradle Plugin `8.13.2`; Java `17`
- **Debug artifact:** `StudyForge-2.1.2-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.2-release-apk`
- **Signing alias:** fixed non-secret `studyforge-release`; keystore validated by `keytool` before Gradle release packaging.
- **Required signing secrets:** `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_PASSWORD`.

## Verification status

**APK/CI not verified.** This environment does not have a configured Android SDK or Gradle executable, so no Android Gradle build, Room runtime test, or JUnit suite could be run here. Local checks completed: standalone Kotlin scheduler smoke checks passed; the extracted SQLite 2→3 migration SQL preserved an existing question row and created the expected settings columns; the schema-3 example JSON parsed and contained FSRS/settings fields. A parser-only scan of the Kotlin sources reported no Kotlin syntax-parser diagnostics, but standalone compilation of the Android app cannot complete without Android/Compose/Room dependencies and an Android SDK. These checks do not replace Room/Gradle/CI or device testing. No successful GitHub Actions run or device test for 2.1.2 has been supplied.

The unit test source covers FSRS initial stability/difficulty, new-card and relearning delays, retention-sensitive intervals, max-interval clamping, step parsing, retrievability, the 90% stability point, and whole local study-day handling. The JUnit tests are not marked passed until GitHub Actions runs them successfully.

## Required GitHub Actions verification

1. `Run unit tests` passes.
2. `Build debug APK` succeeds and uploads `StudyForge-2.1.2-debug-apk`.
3. Signing preflight finds alias `studyforge-release` in the same existing keystore.
4. `Build signed release APK` succeeds.
5. `Upload signed release APK` uploads `StudyForge-2.1.2-release-apk`.
6. Check the APK signing certificate remains identical to the previous release-key-signed app.
7. Device acceptance: upgrade from actual installed app, Room 2→3 migration with populated data, settings persistence, FSRS due behaviour, no-network LaTeX display, and schema 1/2/3 backup restore.

## Data safety / install cautions

Room migration 2→3 adds FSRS columns and the settings table; there is no destructive fallback. The migration must be runtime-tested before broad installation. JSON schema 3 preserves legacy import. Export and verify a backup from the currently installed app before updating.

If the installed app uses the exact same release signing certificate, install the 2.1.2 APK over it. If Android shows a signature conflict, first verify the JSON backup; then determine whether the installed app was signed with a different debug key. Only then use the one-time uninstall/reinstall transition and restore the backup. Do not uninstall as a routine update step.

## 2.1.2 source-level change record

- FSRS-6 scheduler with 21 default parameters; no FSRS reference-vector/CI verification yet.
- Persistent settings and Room migration 2→3.
- Expanded due queue, study modes, configurable MCQ exam, library filters and analytics.
- Offline native LaTeX view integration and Markdown-like rich text preview.
- JSON schema 3 exports FSRS memory state and settings; imports old schemas 1 and 2.
- 2.1.1 signing alias fix and same key identity retained.

---

# Historical build status — earlier packages (retained)

# Build and test status

## Historical source snapshot — StudyForge 2.1.1 (retained)

- **versionName:** `2.1.1`
- **versionCode:** `6`
- **Room database version:** `2`
- **JSON backup schema:** `2` (import accepts versions 1 and 2)
- **Workflow:** `.github/workflows/android-apk.yml`
- **Debug artifact:** `StudyForge-2.1.1-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.1-release-apk` (only when GitHub Actions signing secrets are configured)

## Source-level checks completed

- App version metadata is 2.1.1 / versionCode 6.
- Existing Room database version 2 and migration 1-to-2 are unchanged.
- Existing JSON backup schema version 2 and version 1/2 import compatibility are unchanged.
- The Room `Migration` import remains `androidx.room.migration.Migration`.
- Gradle release signing reads environment variables and does not contain literal passwords or a keystore path committed as a secret.
- GitHub Actions still runs unit tests and a debug APK build, and conditionally assembles/uploads a release APK when `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, and `SIGNING_KEY_PASSWORD` are configured.
- The workflow includes a cleanup step for the temporary decoded keystore.
- `.gitignore` excludes keystore and signing credential files.

## Verification status

**Not yet verified.** This environment did not have a configured Android SDK or Gradle executable, so no Android Gradle tests or APK assembly were run here. No successful GitHub Actions run for the 2.1.1 source package has been supplied.

After adding the three repository secrets, run the workflow and confirm:

1. `Run unit tests` succeeds.
2. `Build debug APK` succeeds.
3. `Prepare release signing key` succeeds.
4. `Build signed release APK` succeeds.
5. `Upload signed release APK` succeeds and produces `StudyForge-2.1.1-release-apk`.

Then test the release APK on the Android phone. Verify future releases are signed by the same certificate. Device install/update is a separate acceptance step.

## Critical install compatibility note

The previously distributed workflow artifacts were debug APKs. The new 2.1.0 release APK uses a separate release key. Android generally refuses an in-place update if an installed package is signed by a different certificate. Before the first transition, export and verify a JSON backup. If installation reports a signature conflict, uninstall the old debug-signed app, install the 2.1.0 release APK, then restore the backup. After this first transition, keep and reuse this exact keystore for all future updates.

## Known feature gaps still tracked

This package is not the full Version 2 completion milestone. FSRS, configurable review limits/settings, configurable exam settings, rendered offline LaTeX, fuller filters/study modes, and additional regression coverage remain open in `ROADMAP.md`.


## Historical 2.1.1 signing failure investigation

The supplied 2.1.0 GitHub Actions log failed in `:app:packageRelease` because the alias passed to Gradle was rendered as replacement characters and was not found in the keystore. The 2.1.1 workflow no longer reads the alias from a GitHub secret. It uses the documented fixed alias `studyforge-release` and validates it with `keytool` before Gradle packaging. This is a source-level mitigation; CI success remains unverified until the user runs the workflow.
