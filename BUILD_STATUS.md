# Build status — StudyForge 2.1.13

- **versionName:** `2.1.13`
- **versionCode:** `18`
- **Room database:** schema `9` (unchanged)
- **JSON backup:** schema `9`; imports schemas 1–9 (unchanged)
- **Debug artifact:** `StudyForge-2.1.13-debug-apk`
- **Signed release artifact:** `StudyForge-2.1.13-release-apk` (requires the existing signing secrets)

## CI test failure diagnosed
The uploaded GitHub Actions test report contains 55 tests across 10 test classes: 54 passed and 1 failed. The sole failure is `TagTextTest.renameAvoidsDuplicateNames` at `TagTextTest.kt:16`. The test expected `"Physics, MDCAT"` while its input was only `"Physics, Chemistry"`; the implementation correctly preserved only tags present in the input, yielding `"Physics"`.

## Fix in 2.1.13
- Corrected the test fixture to `"Physics, Chemistry, MDCAT"`, making the fixture match its intended assertion: renaming `Chemistry` to an already-existing `physics` tag deduplicates the collision while preserving the unrelated `MDCAT` tag.
- Production `TagText.rename` logic was not changed because the report demonstrates a test-data/expectation mismatch, not a production defect.
- App version advanced to 2.1.13 / versionCode 18; Room and JSON schemas remain 9.

## Verification boundary
- The uploaded XML/HTML test report was inspected directly. It confirms 54/55 tests passed and identifies the single mismatch above.
- The corrected test was source-inspected and the ZIP/checksum manifest verified.
- The Android Gradle/JUnit suite has **not** been rerun after the correction. GitHub Actions, signed APK and physical-device update remain pending.

This is a targeted test-fixture correction, not a claim that CI now passes. Preserve the existing signing key and original `StudyForge_Plan.txt`.
