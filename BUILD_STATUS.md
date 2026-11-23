# Build status — StudyForge 2.1.12

- **versionName:** `2.1.12`
- **versionCode:** `17`
- **Room database:** schema `9` (migrations 1→2→3→4→5→6→7→8→9; unchanged)
- **JSON backup:** schema `9`; imports schemas 1–9 (unchanged)
- **Debug artifact:** `StudyForge-2.1.12-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.12-release-apk` (only when existing signing secrets are configured)

## CI-reported failures addressed
1. Removed duplicate `FontWeight` import in `StudyContent.kt` (reported at lines 20 and 27).
2. Moved the editor `error` mutable state declaration before the image-picker callback in `StudyForgeApp.kt`, fixing the callback assignment error reported at line 725.

## Verification performed
- Inspected the exact uploaded 2.1.11 source at both reported locations.
- Static checks confirmed one `FontWeight` import and the `error` state declaration precedes the callback that assigns to it.
- Confirmed app version/versionCode and workflow artifact labels agree.
- Confirmed Room schema 9, JSON backup schema 9 and migration chain are unchanged.
- Confirmed the original `StudyForge_Plan.txt` remains unchanged from the uploaded 2.1.11 archive.
- ZIP integrity and SHA256 manifest checked during packaging.

## Not verified
- Full Android Gradle/Compose/Room/KSP compilation or Android JUnit task.
- GitHub Actions build, signed APK and certificate continuity.
- Physical-device install/update and runtime behavior.

**This is a source-level build-fix release, not a claim that the build now passes. Run GitHub Actions to confirm.**
