## 2026-11-25 — FSRS-6 pinned v6.0.0 compatibility correction

- Corrected `ReviewScheduler.kt` to match the exact `py-fsrs` v6.0.0 reference at commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81`: all 21 default weights, clamped initial-Easy mean-reversion target, and the same-day stability floor for Good/Easy only (not Hard).
- Corrected learning/relearning Hard scheduling: step 0 keeps its position and uses the midpoint of the first two configured steps, or 1.5 times the only step; later steps repeat their configured delay.
- Corrected elapsed-day calculation to match Python `timedelta.days` (completed 24-hour periods, not local calendar-day boundaries) and interval rounding to ties-to-even.
- Added exact behavior for empty learning/relearning step lists: no learning steps means new cards graduate directly to Review; no relearning steps means a lapse remains in Review.
- Updated initial-state, same-day, long-term, learning/relearning, empty-step, and pinned `test_memo_state` regression fixtures. The diagnostic compiles the actual scheduler and checks every transition plus focused Hard/elapsed-time policy probes.
- Local results: pinned fixture matches at every transition; final StudyForge state S=49.447237416 / D=6.827105750 (rounded S=49.4472 / D=6.8271). A lightweight Kotlin/JUnit-compatible runner compiled and executed 32 scheduler regression tests: 32 passed, 0 failed. The standalone diagnostic also passed all four focused policy probes.
- Verification boundary: this environment has `kotlinc` and Java but no Gradle executable or Gradle wrapper in the ZIP. Android Gradle/JUnit/Room/emulator execution and GitHub Actions remain unverified. No APK/release was produced; app version 2.1.24/versionCode 28, Room 15 and JSON backup 15 remain unchanged. `StudyForge_Plan.txt` was not modified.

Reference implementation: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/fsrs/fsrs.py`. Reference fixture: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/tests/test_basic.py` (`test_memo_state`).

---

Earlier FSRS mismatch notes below are historical and superseded by the latest status above.


# Build status — StudyForge 2.1.24 (FSRS-6 compatibility correction; no release bump)

- Added stable-ID-safe saved-session rename behavior, visible duplicate-name blocking, and confirmed deletion with active-selection cleanup.
- Added `StudySessionPresetRules`, four JUnit regression cases, and extended static verifier/source-integrity coverage. Local static checks and standalone Kotlin helper assertions are recorded in the handoff for this continuation.
- App identity remains 2.1.24/versionCode 28; Room and JSON backup schema remain 15. The original `StudyForge_Plan.txt` remains unchanged.
- Android Gradle/Compose/KSP/JUnit, Room runtime migration, Android backup/restore, GitHub Actions, signed APK and physical-device acceptance remain unverified. No APK release is claimed.

---

## Latest continuation — 2026-11-25 bulk-selection consistency (FSRS excluded)

- Fixed a Library multi-select edge case: changing filters after selecting questions could leave hidden IDs selected, while bulk actions only applied to the visible subset. All four bulk action paths now resolve selected IDs against the complete current Library. “Select visible” still selects only the filtered view; IDs for deleted questions are ignored safely.
- Added `QuestionSelectionRules` and three JUnit regression cases for full-library selection, stable Library ordering, stale IDs and empty selection. Added `tools/verify_bulk_selection.py` to GitHub Actions and the current source-integrity gate.
- Static source contract checks pass. Android JUnit/Compose runtime remains unrun. FSRS source/tests untouched; app remains 2.1.24/versionCode 28, Room 15, backup 15. No schema, migration, signing, release or original plan change.

# Build status — StudyForge 2.1.24 (2026-11-25 live smart-filter counts; FSRS excluded)

- Saved Library filter presets now display a live match count. Counts are recalculated from current questions and collection links through `QuestionLibraryFilter.countMatches`; no count is persisted, preventing stale values after data changes.
- Added JUnit source for current-data and deleted-collection behavior, plus `tools/verify_live_smart_filters.py` wired into CI and the current source-integrity gate. The static contract verifier passes locally; Android JUnit/UI execution remains unrun.
- This improves saved-filter presets but does not introduce a separate dynamic-collection data model or background refresh service.
- FSRS source/tests remain untouched. App identity remains 2.1.24/versionCode 28, Room schema 15 and JSON backup schema 15. No APK release, schema/migration/signing change or original blueprint edit.
- Android Gradle/Compose/KSP/JUnit, Room runtime migrations, actual Android backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance remain unverified.

---

# Build status — StudyForge 2.1.24 (2026-11-25 bulk collection continuation; FSRS excluded)

- Added Library multi-select bulk add-to-collection and remove-from-collection actions. A selected collection ID is revalidated against the current database inside the transaction. Each changed question receives a pre-change snapshot including its old collection membership; membership replacement and question `updatedAt` updates are committed atomically. No-op membership operations create no history entry.
- Added `tools/verify_bulk_collection.py` and wired it into GitHub Actions and the current source-integrity gate. The static contract verifier passes locally; six standalone Kotlin assertions passed for ordering, duplicate prevention, no-op add/remove, whitespace normalization and blank-ID handling. Android JUnit/UI remains unrun.
- FSRS source/tests were not changed. Android Gradle/Compose/KSP/JUnit, Room runtime migrations, real Android backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance remain unverified.
- App identity remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15. No APK release, schema/migration/signing change or original blueprint edit.

---

# Build status — StudyForge 2.1.24 (2026-11-25 bulk-tag continuation; no release bump)

- Added multi-select Library bulk tag add/remove. Tag additions update the normalized tag catalogue; removals update selected questions only.
- Corrected stale multipart-editor helper text that incorrectly claimed partial-credit scoring was not connected.
- Every changed question gets a pre-change content-version snapshot inside the same Room transaction. No-op changes do not create redundant versions. Comma-containing tag names are rejected to protect the current comma-separated tag representation.
- Added `TagText.add` regression source, `tools/verify_bulk_tagging.py`, and a CI step. Six standalone Kotlin tag-rule assertions passed; the static bulk-tag, release, source-integrity, backup and SQLite migration checks passed locally.
- FSRS source/tests were not changed. Android Gradle/Compose/KSP/JUnit, Room runtime migration, actual backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance remain unverified.
- App identity remains 2.1.24/versionCode 28, Room 15, JSON backup 15. No APK release, schema change, signing change or original blueprint edit.

---

# Build status — StudyForge 2.1.24 (2026-11-25 consistent-backup continuation; no release bump)

- Current identity remains app 2.1.24 / versionCode 28, Room schema 15, JSON backup schema 15.
- JSON export now reads all 12 persisted record groups inside a single Room transaction, then serializes/writes the stable snapshot after the transaction. This avoids cross-table inconsistencies if edits occur while export is running.
- Added `tools/verify_backup_snapshot.py`; it verifies that every exported table source is read inside the transaction. Added it to GitHub Actions and `verify_v2_source_integrity.py`.
- Locally passed: `verify_backup_snapshot.py`, `verify_release_2_1_24.py`, `verify_v2_source_integrity.py`, `verify_backup_schema_15.py`, `verify_backup_media_import.py`, and SQLite migration smoke checks 7→8 through 14→15.
- FSRS source and tests were not changed in this continuation. The upstream mismatch remains explicitly unresolved.
- Not run: Android Gradle/Compose/KSP compilation, Android JUnit, Room runtime migration instrumentation, actual Android JSON backup/restore round-trip, GitHub Actions, signed APK/certificate continuity, physical-device acceptance. No Gradle executable, Gradle wrapper or Android SDK is available in this environment.
- No APK release, version bump, schema change, signing-key access or original blueprint change.

---

# Build status — StudyForge 2.1.24 (V2 preparation continuation; no release bump)

- Current baseline remains app 2.1.24 / versionCode 28, Room schema 15, JSON backup schema 15.
- Corrected `.github/workflows/android-apk.yml` to call `verify_release_2_1_24.py` instead of the stale 2.1.23 verifier. Added `verify_v2_source_integrity.py` to CI.
- Added a same-day Hard FSRS-6 regression test to ensure the short-term memory update does not reduce stability below its prior value.
- Local checks passed: release verifier; new V2 source-integrity gate; SQLite migration smoke checks 7→8 through 14→15; schema-15 backup static check.
- Python compilation of all `tools/*.py` and ZIP integrity are checked during packaging.
- **Not run / not verified:** Android Gradle/Compose/KSP/JUnit; Room runtime migrations; actual Android backup/restore; GitHub Actions execution; signed APK/certificate continuity; physical-device acceptance.
- This is not a new APK release. No versionCode increment, signing-key access, or schema change was made.

---

# Build status — StudyForge 2.1.24 (continued audit; no release bump)

- Current app remains **2.1.24 / versionCode 28**; Room schema 15; JSON backup schema 15. This is a development/audit continuation, not a new APK release.
- Expanded `Fsrs6ReferenceInvariantTest.kt` with initial-state assertions for all four ratings (Again/Hard/Good/Easy), covering stability and difficulty from the published default weights.
- Added `SOURCE_PLAN_AUDIT_2.1.24.md` documenting the actual source-vs-plan state, FSRS conformance boundary, and outstanding V1/V2 gates.
- Passed locally: `verify_release_2_1_24.py`; SQLite/static migration checks 7→8 through 14→15; schema-15 backup static check.
- Standalone `kotlinc` scheduler-core harness passed 8 assertions (four initial-state and four long-term stability fixtures). The harness excludes Android Room transaction wiring and is not Android/JUnit execution.
- **Still not verified:** full upstream golden-vector corpus parity; Android Gradle/Compose/KSP/JUnit; Room runtime migration tests; Android JSON backup/restore round-trip; GitHub Actions; signed APK/certificate continuity; physical-device acceptance.
- No versionCode increment is appropriate because no APK release was produced. Existing private signing key and credentials were not accessed, changed, or included.

---


# Build status — StudyForge 2.1.24 (current)

- **versionName:** `2.1.24`
- **versionCode:** `28`
- **Room database:** schema `15`; migrations 1→2 through 14→15 retained.
- **JSON backup:** schema `15`; imports schemas `1–15` retained.
- **Workflow artifacts:** `StudyForge-2.1.24-debug-apk` and `StudyForge-2.1.24-release-apk`; signed release still requires the existing external signing secrets/key.

## 2.1.24 source changes and verification boundary

- Corrected FSRS-6 long-term stability to use pre-review difficulty before updating difficulty; aligned memory-state stability floor to 0.001.
- Added four-rating reference-equation fixtures (Again/Hard/Good/Easy) plus a minimum-stability case, based on py-fsrs v6.0.0 equations and default 21 weights.
- Passed locally: `tools/verify_release_2_1_24.py`; SQLite/static migration checks 7→8 through 14→15; schema-15 backup static check.
- Standalone `kotlinc` harness compiled the exact `ReviewScheduler` object (Room transaction wiring excluded) and matched all four expected long-term reference-equation values to 1e-9: Again 1.3024312523883599 / 8.347534; Hard 17.775317698817506 / 6.671767; Good 22.928695874322422 / 4.996; Easy 34.21415450301846 / 3.320233 (stability / difficulty).
- Full Android Gradle/Compose/KSP compilation and JUnit, Room runtime migrations, Android backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance remain unverified.
- No Gradle wrapper is present in the supplied source ZIP; no Gradle executable is assumed.

---

# Build status — StudyForge 2.1.22 (current)

- **versionName:** `2.1.22`
- **versionCode:** `26`
- **Room database:** schema `15`; no schema change; migrations 1→2 through 14→15 remain registered.
- **JSON backup:** schema `15`; import accepts schemas `1–15`; no backup-format change.
- **Workflow artifacts:** `StudyForge-2.1.22-debug-apk` and `StudyForge-2.1.22-release-apk`; signed release still requires the existing external signing secrets/key.

## 2.1.22 source changes and local checks

- Corrected first-review Good to advance to the second configured Learning step when present.
- Corrected first-review Hard to enter Learning at step 0 when learning steps are configured; Easy and final Good continue to graduate.
- Preserved StudyForge's configured Hard-repeat-current-step behavior; this is a product scheduling policy and is not claimed as exact parity with the upstream Hard-step interval calculation.
- Added `ReviewSchedulerTest` cases for first-review Good and Hard transitions.
- Passed locally: migration smoke checks 7→8 through 14→15; schema-15 backup export/import static checks; and `tools/verify_release_2_1_22.py` source/version/schema/workflow checks.
- Standalone Kotlin smoke harness compiled the exact scheduler core (with Android Room wiring excluded) and passed 4 assertions: new-card Good, new-card Hard, new-card Easy and one-step Good graduation.

## Verification boundary

- Full Android Gradle/Compose/KSP compilation and JUnit: **not run** in this environment; no Gradle executable or Gradle wrapper is included in this ZIP.
- Room runtime migration and actual Android JSON backup/restore round-trip: **not tested**.
- GitHub Actions: **not run/verified**.
- Signed APK and certificate continuity: **not verified**. Existing private signing key was not accessed, modified, regenerated or included.
- Physical Android device installation/update and UI interaction: **not tested**.
- Earlier current-source verification scripts and exact migration checks are retained; historical scripts that expect schema 10, 12 or 13 are archival checks and are not current-release gates.

## V1/V2 still open

Full fixture-by-fixture upstream FSRS-6 scheduler conformance, broader smart-collection/bulk workflows, advanced editor/accessibility acceptance, deeper skill and progress-over-time analytics, reliable time-spent metrics, complete Android/Room/backup tests, CI, signing and device acceptance remain outstanding. V3 work has not started.

---

# Historical build status — StudyForge 2.1.20

- **versionName:** `2.1.20`
- **versionCode:** `24`
- **Room database:** schema `15`; migrations 1→2 through 14→15 are registered. Migration 13→14 adds source-title/reference fields to saved filters; migration 14→15 adds created/modified date-range fields.
- **JSON backup:** schema `15`; import accepts schemas `1–15`. New saved-filter fields default to empty when importing older backups.
- **Workflow artifacts:** `StudyForge-2.1.20-debug-apk` and `StudyForge-2.1.20-release-apk`; signed release requires the existing external signing secrets/key.

## 2.1.20 local checks

- `tools/verify_migration_13_14.py`: passed; exact SQLite SQL adds source-title/reference preset fields and preserves existing rows.
- `tools/verify_migration_14_15.py`: passed; exact SQLite SQL adds four date-range fields and preserves existing rows.
- `tools/verify_backup_schema_15.py`: static schema/export/import checks.
- `tools/verify_release_2_1_20.py`: static version, schema, workflow, filters, analytics and version-summary consistency checks.
- Standalone Kotlin harness: 6 assertions passed for source/date filtering and question-type accuracy.
- New JUnit source covers independent source/date filters, preset round-trip, and question-type accuracy. Android JUnit execution is pending.
- A parser-only `kotlinc` scan did not produce `expecting`/unexpected-token syntax diagnostics, but reported thousands of unresolved Android/Compose/Room references without the Android classpath; this is not a compile result.

## Verification boundary

- Full Android Gradle/Compose/KSP compilation and JUnit: **not run**; no Gradle executable/wrapper is available in this environment.
- Kotlin parser-only diagnostics: not yet established for this release; do not treat static checks as compilation.
- Room runtime migration and actual Android JSON backup/restore round-trip: **not tested**.
- GitHub Actions: **not run/verified**.
- Signed APK and certificate continuity: **not verified**. Existing private signing key was not accessed, modified, regenerated or included.
- Physical Android device installation/update and UI interaction: **not tested**.

## V1/V2 still open

Official FSRS-6 upstream reference vectors, full smart-collection/bulk workflows, richer version diff/audit attribution, advanced editor/accessibility acceptance, complete Android/Room/backup tests, CI, signing and device acceptance remain outstanding. V3 work has not started.


## 2.1.23 source-package checks

- Source version: 2.1.23 / versionCode 27; Room schema 15; JSON backup schema 15.
- Added HINT authoring and learner reveal UI using existing structured content-block fields; no database/backup schema change.
- Local Python static verifier: passed. Migration/backup scripts for 7→8 through 14→15 and schema 15: all passed in this environment.
- Android Gradle/JUnit, Room runtime migration, actual Android backup/restore, GitHub Actions, release signing and device acceptance: not verified in this package.
- This execution environment does not have the `gradle` executable or a Gradle wrapper in the source package, so Android compilation/JUnit could not be run locally.

## 2026-11-25 V2 continuation — Room runtime test source

- Added Android instrumentation tests for the generated Room schema using a file-backed Android SQLite database: core records are written, the database is closed/reopened, and the records are read back. Coverage includes questions, collections/membership, settings, tags, rich blocks, multipart parts, review logs, study-session presets, exam attempts, question versions and saved filters.
- Added a second runtime test for question-delete cascade behavior across content blocks, multipart parts and review logs.
- Added AndroidX test dependencies and wired `:app:connectedDebugAndroidTest` into GitHub Actions using an Android API 35 emulator.
- The tests and CI wiring are source changes only at this point. They have not run in this environment because no Android SDK/Gradle installation or emulator is available. They test the current schema; historical populated migration tests and a full UI backup/restore round-trip remain open.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



## 2026-11-25 continuation — collection cleanup and Android lint gate

- Added an Android Room instrumentation case for collection deletion. It verifies collection-question links are removed, saved-filter preset collection references become null through the Room `SET_NULL` foreign key, and study-session preset references are explicitly cleared using the same cleanup call as the collection-delete UI.
- Added `:app:lintDebug` to GitHub Actions before unit and emulator tests so Compose/Android lint findings fail the workflow rather than being silently skipped.
- Extended the static source-integrity verifier to require the new runtime test and lint workflow step.
- These additions are source-only until GitHub Actions executes. No Android build, lint, unit-test or emulator result is claimed locally.


## 2026-11-25 continuation — safe embedded-image backup import

- The JSON backup importer previously accepted Base64 strings using a character-only regular expression and a loose 8,000,000-character truncation. That could preserve malformed Base64 or accept decoded image payloads larger than the editor's 1.5 MB limit.
- Added `MediaImportRules.sanitizeBase64Image`, which rejects empty/malformed payloads, checks encoded length before decoding, enforces the same 1,500,000-byte decoded limit as the image picker, and returns canonical Base64 for valid data. The importer now uses this helper.
- Added five JUnit regression cases for valid/canonical payloads, malformed data, empty data, decoded oversize and encoded-length oversize. Android JUnit remains unrun.
- Added a CI static contract check `tools/verify_backup_media_import.py` and wired it into the existing workflow. A standalone `kotlinc` harness compiled the exact helper and passed five smoke assertions; all existing Python release, source-integrity, migration and schema-15 backup checks passed.
- No app version, Room schema, JSON backup schema, migration, signing configuration or original blueprint change. Full Android backup/restore remains unverified.


## 2026-11-25 continuation — review activity timeline

- Added `ReviewTrendRules` to aggregate persisted review logs into a gap-filled 14-day local-calendar timeline with daily review totals, scored accuracy, active-day count and current streak. Unscored review logs count as activity but are excluded from accuracy. If there are no reviews today, a streak may continue from yesterday.
- Added the 14-day activity card to Progress. It uses actual `reviewedAt` timestamps and recorded correctness only; no time-spent estimate is inferred.
- Added five JUnit regression scenarios for gap filling, accuracy, streak behavior and timezone bucketing. A standalone `kotlinc` harness compiled the exact helper against the Room entity shape and passed 19 assertions. Android JUnit remains unrun.
- No schema/version/backup changes.


## 2026-11-25 continuation — FSRS difficulty mean-reversion correction

- Further comparison with the upstream FSRS scheduler formula found that mean reversion must use the *unclamped* initial Easy difficulty as its target, then clamp the final updated difficulty to 1–10. StudyForge had been clamping the target to 1 before mean reversion. Corrected `ReviewScheduler.nextDifficulty` and updated the four long-term rating fixtures; long-term stability values remain unchanged because the stability update uses pre-review difficulty.
- The correction follows the reference scheduler's `_next_difficulty` formula. A standalone Kotlin harness compiled the scheduler core and reproduced the updated four-rating values.
- A separate attempt to reproduce py-fsrs v6.0.0 `test_memo_state` (Again, Good×5, then same-time Good; elapsed-day inputs 0, 0, 1, 3, 8, 21; 1m/10m learning steps) did **not** match the upstream expected final state: StudyForge produced S=53.6269, D=6.3464 versus the upstream test's S=49.4472, D=6.8271. This is an explicit unresolved conformance gap, not a passing golden-vector test. No further scheduler policy change was made without a complete diagnosis.
- No version, Room schema, JSON schema or migration change. Full upstream fixture-by-fixture parity remains blocked.


## 2026-11-25 continuation — dashboard version label

- Corrected the Home dashboard's hard-coded release label from 2.1.18 to the actual source version 2.1.24. Extended the static integrity verifier to prevent the label from becoming stale again.
- No behavior, data schema, versionCode or release signing changes.


# Build status — 2026-11-25 continued source audit (no release bump)

- Source baseline confirmed: app 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15/imports 1–15, additive migrations 1→2 through 14→15 registered.
- Corrected stale maintained-plan metadata in `PROJECT_PLAN.md`; `verify_v2_source_integrity.py` now checks that its current technical baseline agrees with source.
- Current local checks passed with `python3 -S`: release 2.1.24 verifier, V2 source-integrity verifier, media-import contract, schema-15 backup check, and SQLite migration smoke checks 7→8 through 14→15.
- Replayed the exact FSRS `test_memo_state` sequence using a standalone Kotlin harness compiled from the included `ReviewScheduler` object (Room wiring excluded). StudyForge produced S=53.626902917141386/D=6.34635796621067; pinned py-fsrs v6.0.0 test expects S=49.4472/D=6.8271. This is a reproduced failure, not a pass.
- No scheduler fix, app version bump, APK release, Room/backup schema change or signing-key operation was made.
- Not run/verified: Android Gradle/Compose/KSP/JUnit, Room runtime migration, Android JSON backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance. No Gradle wrapper, Gradle executable or Android SDK is available in this environment.


## 2026-11-25 additional local verification — interchange parser

- Strict CSV/TSV quote-state parsing added; exact production parser compiled with kotlinc and passed six smoke assertions.
- Added Android JUnit regression source for malformed quote placement and CRLF. These JUnit tests are not claimed as executed.
- Current static/release/migration/backup verifiers are to be rerun for this continuation. No Android build, lint, JUnit, emulator, signed APK, CI or physical-device result is claimed.


## 2026-11-25 — saved study-session preset editing (FSRS excluded)

- Saved study-session rows now expose Edit. The dialog loads all persisted filter fields and current mode/topic; saving edits by stable preset ID, including when the user renames it. Save current resets the editor for a new preset.
- Static verifier: `tools/verify_session_preset_edit.py`, wired into GitHub Actions and `verify_v2_source_integrity.py`.
- Local static verification only; Android Compose/JUnit execution is not available in this environment. FSRS source/tests untouched; app/Room/backup versions, migrations, signing and original plan unchanged.


# Build status — StudyForge 2.1.24 (2026-11-25 rich-content Room runtime coverage; no release bump)

- Expanded `StudyDatabaseRuntimeTest` with a current-schema Android SQLite persistence case for ordered TEXT, HINT, IMAGE, TABLE and GRAPH content blocks. The IMAGE fixture checks embedded Base64, MIME type and alt text after closing and reopening the Room database.
- Extended `verify_v2_source_integrity.py` to require this instrumentation case and its rich-block/media assertions. The source-integrity verifier passes locally.
- This is current-schema Room persistence coverage only. It does not exercise JSON export/import, historical Room migrations, or Compose rendering/accessibility. The Android instrumentation test has not been executed; Android Gradle/JUnit, CI, signing and physical-device acceptance remain unverified.
- App remains 2.1.24/versionCode 28, Room 15, JSON backup 15. No migration, backup schema, release, signing or FSRS change.

## 2026-11-25 FSRS-6 conformance diagnosis — pinned-reference mismatch identified

- Built and ran `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. It extracts and compiles the exact production `ReviewScheduler` object with minimal Room/entity stubs using `kotlinc`, then replays the pinned `py-fsrs` v6.0.0 `test_memo_state` fixture. In parallel, it runs a local reference port of the exact upstream implementation/defaults from commit `352c7a2`. No network or third-party Python package is required; `kotlinc` and a JRE are required.
- The reference port reproduces the upstream golden result exactly: final S=49.4472374165 and D=6.8271057501 (rounded S=49.4472 / D=6.8271). The exact StudyForge Kotlin source returns S=53.6269029171 and D=6.3463579662.
- Root cause confirmed: the comparison was mixing different default parameter vectors. Pinned py-fsrs v6.0.0 `fsrs/fsrs.py` has defaults `[0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2]`; StudyForge currently uses `[0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542]`. The vectors differ in all 21 positions. The first mismatch is already visible on the initial Again: upstream S/D=0.2172/7.0114; StudyForge=0.212/6.4133.
- Two additional pinned-v6.0.0 behavioral differences were found: (1) upstream v6.0.0 clamps initial Easy difficulty before using it as the mean-reversion target; StudyForge currently passes `clamp=false`; (2) upstream v6.0.0 short-term stability increase is clamped to >=1 for Good/Easy, not Hard, while StudyForge clamps every grade >= Hard. The Hard difference does not cause the `test_memo_state` fixture failure because that fixture contains no Hard ratings.
- This corrects an earlier investigation note that relied on the newer `main/fsrs/scheduler.py` formula and treated its unclamped mean-reversion target as the pinned v6.0.0 contract. The actual pinned commit's source is `fsrs/fsrs.py`, and its behavior is different. Do not use current-main source as a substitute for the pinned reference.
- No production FSRS code was changed. This task was diagnosis only. Before changing production, decide explicitly whether StudyForge's compatibility target is the exact py-fsrs v6.0.0 defaults/behavior or a newer FSRS-6 parameter baseline. If the target remains pinned v6.0.0, the defaults and the two behavioral differences above must be reconciled and regression-tested together.
- Source references: pinned upstream implementation `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/fsrs/fsrs.py`; pinned fixture `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`. Local diagnostic output was reproduced successfully on 2026-11-25. This is a standalone source harness, not Android Gradle/JUnit or emulator verification.

## 2026-11-25 — GitHub Actions Kotlin compile correction

- Investigated the user-reported `:app:compileDebugKotlin` failure in `StudyScreens.kt` (`Box` unresolved at line 187; composable-context follow-on diagnostics at lines 188 and 191).
- The corrected project source used `Box` in the saved-session dialog but lacked `import androidx.compose.foundation.layout.Box`. Added that import.
- This is a targeted compile-source fix; no production logic or version/schema/signing metadata changed. The next GitHub Actions run is required to verify that the cascading composable errors disappear and to identify any further compiler errors.
- Not claimed as fixed by a successful Android build: Gradle/Android compile, lint, JUnit, emulator and device tests remain pending until CI is rerun.


## 2026-11-25 — GitHub Actions unit-test failure follow-up

The first Android CI run after the missing Compose `Box` import fix completed 141 unit tests and reported 5 failures. The five failures were reviewed individually:

- `MultipartExamRulesTest.invalidManualAwardFallsBackToAutomaticMarking`: production defect. Non-finite manual awards (`NaN`, positive/negative infinity) previously returned zero instead of falling back to automatic marking. Fixed in `MultipartExamRules.earnedPoints`; finite manual awards remain clamped to `[0, part.points]`.
- `QuestionInterchangeTest.previewRejectsUnsupportedTypesAndInvalidMcqOptions`: the invalid-MCQ fixture was actually valid (`answer=A`, options `A | B`). Corrected the fixture to options `B | C` and added a valid-MCQ acceptance assertion. Import validation logic was not weakened.
- `QuestionTypeAnalyticsTest.accuracyUsesOnlyScoredReviewsAndGroupsByCurrentQuestionType`: production ordering defect. Analytics now preserves first-seen review-log group order instead of alphabetically reordering ties. Scoring still excludes unscored logs and groups using the current question type.
- `TagTextTest.addTrimsAndDeduplicatesCaseInsensitively`: the first fixture attempted to assert deduplication without including the existing `MDCAT` tag. Corrected the fixture to include that existing tag; implementation remains case-insensitive and preserves existing spelling.
- `TextFormattingRulesTest.headingPrefixesCurrentLineAndPreservesCaret`: the fixture used a literal backslash-plus-`n` instead of an actual newline. Corrected the test input/expected value to use an actual newline; production formatting behavior is unchanged.

Validation in this environment: targeted Kotlin smoke harness passed for the five affected areas; `verify_release_2_1_24.py`, `verify_v2_source_integrity.py`, and the pinned FSRS-6 diagnostic passed. A full Gradle/JUnit run has NOT been executed locally (Gradle is unavailable here). The next GitHub Actions run is required to confirm the complete 141-test suite.

## 2026-11-25 — GitHub Actions emulator boot timeout follow-up

- The user's CI run reached the `Run Room runtime tests on Android emulator` step but failed with `Timeout waiting for emulator to boot`. This is an emulator startup/infrastructure failure: the action did not reach its `connectedDebugAndroidTest` script, so no Room runtime assertion is reported as failed by this log.
- Updated `.github/workflows/android-apk.yml` to enable KVM permissions on the GitHub-hosted Ubuntu runner before launching the AVD, explicitly retain Linux hardware acceleration, and increase `emulator-boot-timeout` from the action's 600-second default to 900 seconds. API 35, x86_64, Pixel 2 profile and the Room test command remain unchanged.
- This is a CI reliability adjustment only. No app source, Room schema/migrations, JSON backup schema, FSRS logic, app version, versionCode, signing material or original plan was changed.
- Required confirmation: rerun GitHub Actions. A successful emulator boot is not yet claimed, and the Room runtime tests remain unexecuted until the connected Android test task completes.
