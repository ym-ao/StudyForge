# Build and test status

## Current source — StudyForge 2.1.0

- **versionName:** `2.1.0`
- **versionCode:** `5`
- **Room database version:** `2`
- **JSON backup schema:** `2` (import accepts versions 1 and 2)
- **Workflow:** `.github/workflows/android-apk.yml`
- **Debug artifact:** `StudyForge-2.1.0-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.0-release-apk` (only when GitHub Actions signing secrets are configured)

## Source-level checks completed

- App version metadata is 2.1.0 / versionCode 5.
- Existing Room database version 2 and migration 1-to-2 are unchanged.
- Existing JSON backup schema version 2 and version 1/2 import compatibility are unchanged.
- The Room `Migration` import remains `androidx.room.migration.Migration`.
- Gradle release signing reads environment variables and does not contain literal passwords or a keystore path committed as a secret.
- GitHub Actions still runs unit tests and a debug APK build, and conditionally assembles/uploads a release APK when `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, and `SIGNING_KEY_PASSWORD` are configured.
- The workflow includes a cleanup step for the temporary decoded keystore.
- `.gitignore` excludes keystore and signing credential files.

## Verification status

**Not yet verified.** This environment did not have a configured Android SDK or Gradle executable, so no Android Gradle tests or APK assembly were run here. No successful GitHub Actions run for the 2.1.0 source package has been supplied.

After adding the four repository secrets, run the workflow and confirm:

1. `Run unit tests` succeeds.
2. `Build debug APK` succeeds.
3. `Prepare release signing key` succeeds.
4. `Build signed release APK` succeeds.
5. `Upload signed release APK` succeeds and produces `StudyForge-2.1.0-release-apk`.

Then test the release APK on the Android phone. Verify future releases are signed by the same certificate. Device install/update is a separate acceptance step.

## Critical install compatibility note

The previously distributed workflow artifacts were debug APKs. The new 2.1.0 release APK uses a separate release key. Android generally refuses an in-place update if an installed package is signed by a different certificate. Before the first transition, export and verify a JSON backup. If installation reports a signature conflict, uninstall the old debug-signed app, install the 2.1.0 release APK, then restore the backup. After this first transition, keep and reuse this exact keystore for all future updates.

## Known feature gaps still tracked

This package is not the full Version 2 completion milestone. FSRS, configurable review limits/settings, configurable exam settings, rendered offline LaTeX, fuller filters/study modes, and additional regression coverage remain open in `ROADMAP.md`.
