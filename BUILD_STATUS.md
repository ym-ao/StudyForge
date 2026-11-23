# Build status — StudyForge 2.1.11

- **versionName:** `2.1.11`
- **versionCode:** `16`
- **Room database:** `9` (migrations 1→2→3→4→5→6→7→8→9)
- **JSON backup:** schema `9`; imports schemas 1–9
- **Debug artifact:** `StudyForge-2.1.11-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.11-release-apk` (only when the existing signing secrets are configured)

## Local verification performed

- Pure Kotlin harness passed for multipart numeric tolerances, accepted answer alternatives, derivation parsing/validation and saved-session criteria filtering.
- SQLite 7→8 migration verifier: 7 checks passed.
- SQLite 8→9 migration smoke test: existing rows survived; default values and updates for all six new columns passed.
- Schema-9 backup example JSON and static field compatibility check passed.
- Parser-only scan of Kotlin source reported no syntax diagnostics. This is not a Kotlin/Android compile.
- Original `StudyForge_Plan.txt` preserved byte-for-byte.

## Not verified

- Full Gradle Android build, Compose/KSP/Room compilation and Android JUnit execution.
- FSRS upstream golden-vector parity (only equation-level invariants have been added).
- Room runtime migration and app-level JSON export/import round-trip.
- GitHub Actions, signed APK generation, certificate continuity, installation/update or physical-device UI tests.

**Do not treat this source ZIP as a tested APK.** Run the repository workflow and inspect its logs before installing.

---

# Build status — StudyForge 2.1.10

- **versionName:** `2.1.10`
- **versionCode:** `15`
- **Room database:** `8` (migrations 1→2→3→4→5→6→7→8; unchanged)
- **JSON backup:** schema `8`; imports schemas 1–8 (unchanged)
- **Debug artifact:** `StudyForge-2.1.10-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.10-release-apk`

## Work performed

- Started from the exact uploaded `StudyForge-v2.1.8-self-contained-handoff.zip`; read its `HANDOFF_FOR_NEW_CHAT.md` and inspected source, Room model/DAO/migrations, content-block editor, study-card rendering, backup import/export, Gradle config, workflow and project records.
- Confirmed the existing content-block entity already contains `mediaBase64`, `mediaMimeType` and `altText`; schema-8 backup export/import already serializes these fields. Reused them without adding a schema migration.
- Added `OpenDocument` image picker for IMAGE blocks. The selected stream is copied immediately into Base64 content in the question record; image MIME type is checked and source bytes are limited to 1.5 MB. Added offline bitmap rendering in editor and study card.
- Added TABLE block editing and preview using pipe-separated cells/newline-separated rows; preview is bounded to 30 rows and 8 columns.
- Added GRAPH block editing and Canvas preview using numeric x,y pairs; parser accepts comma, semicolon or tab separators and preview is bounded to 200 points.
- Added `ContentBlockParsing` pure helper and five JUnit test cases.
- Bumped app version to 2.1.9 / versionCode 14 and updated GitHub Actions artifact labels. Room schema 8 and JSON backup schema 8 are unchanged.
- Original `StudyForge_Plan.txt` was not edited.

## Verification performed

- Compiled `ContentBlockParsing.kt` with the available Kotlin compiler and ran four standalone smoke checks: pipe-table parsing, comma/semicolon/tab graph parsing, non-finite-number rejection and 200-point bounding. All four passed.
- Checked balanced delimiters in all four edited Kotlin source files.
- Confirmed the existing JSON export serializes `mediaBase64`, `mediaMimeType` and `altText`, and import accepts embedded Base64 media (up to its existing 8 MB encoded cap).
- Confirmed Room version remains 8, backup export schema remains 8, the 1→8 migration chain is unchanged, workflow artifact names match 2.1.9, and the original plan file hash matches the uploaded 2.1.8 ZIP.
- No Gradle executable or Android SDK is available in this environment. The JUnit source is included but has not been executed.

## Not verified

- Full Kotlin/Compose/Room/KSP compilation.
- Android JUnit test execution.
- Actual Android document picker and image decoding on-device.
- Table/graph Compose rendering on-device.
- Room runtime migration, JSON backup export/import round-trip, GitHub Actions, signed release APK, signing-certificate continuity and physical-device installation/update.

**Do not describe the APK as built or the release as device-tested until actual CI/device evidence exists.**


## 2.1.10 work and verification

- Inspected the uploaded 2.1.9 ZIP and handoff. Confirmed existing Room `question_parts` table/DAO and schema-8 backup import/export.
- Connected question parts to timed exam selection and passed the observed part list from the app shell to StudyScreen.
- Added part-by-part answer UI, MCQ option rendering for parts with `optionsJson`, and short-text entry for other parts.
- Added weighted scoring: each valid part contributes its configured positive points when its answer matches after trim/whitespace-collapse/case-fold; otherwise zero. Results show part-level earned/possible points and total weighted percentage.
- Added `MultipartExamRules.kt` and five JUnit test cases.
- No Room or JSON schema change; existing part records and backup representation are reused. Version 2.1.10 / versionCode 15; workflow artifact labels updated.

### Verification performed

- Compiled the pure `MultipartExamRules.kt` source with a lightweight Kotlin stub for the Room entity and ran five standalone assertions successfully.
- Confirmed app version/versionCode, workflow artifact names, Room schema 8, JSON schema 8, and migration chain unchanged.
- Confirmed original `StudyForge_Plan.txt` remains byte-for-byte unchanged relative to the uploaded 2.1.9 archive.
- ZIP integrity and SHA256 manifest checked after packaging.

### Not verified

- Full Android Gradle/Compose/KSP compilation and Android JUnit task.
- Timed exam UI behavior on Android, accessibility and configuration-change behavior.
- GitHub Actions, release signing/certificate continuity, APK installation/update, Room runtime and JSON backup round-trip.

Do not describe the APK as built or device-tested until actual CI/device evidence is available.
