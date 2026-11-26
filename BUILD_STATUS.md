# Build status addendum — CI Kotlin compile-error correction (2026-11-26)

- User-provided GitHub Actions compiler output identified two source errors: a duplicate `@Composable` annotation on `SelectionFormattingField` in `StudyForgeApp.kt`, and `LibraryViewModel.repository` being referenced by member functions without being declared as a property.
- Corrected the duplicate annotation by retaining one `@Composable`. Changed the constructor parameter to `private val repository: QuestionRepository`, preserving the existing factory injection and allowing `countMatches` / `countTagUsage` to access the repository property.
- These changes address the two reported Kotlin compiler diagnostics. They have **not** yet been verified by Gradle compilation, Android lint, JUnit, Room instrumentation, or GitHub Actions in this environment (no Gradle/Android SDK and network download access is blocked). The next CI run is the required confirmation; any additional diagnostics must be handled from its output.
- No FSRS, app identity, version, Room/backup schema, migration, signing, or original `StudyForge_Plan.txt` changes were made.

---

# Build status addendum — V3.0 Batch 9 environment and acceptance preparation (2026-11-26)

- Source: supplied `StudyForge-V3.0-Batch-9-Environment-Prepared.zip`, extracted without changing production source. Environment inspection was performed before installation attempts. Container: Linux x86_64, 5.8 GiB RAM, approximately 30 GiB free disk. Java/Javac 21.0.12 are present; JDK 17, Gradle 8.13, Gradle Wrapper, Android SDK, SDK Platform 36, Build Tools 36.0.0, ADB and Android Emulator are absent. No SDK environment variables are set. No Gradle wrapper JAR or other cached Gradle distribution was found.
- Attempted the repository bootstrap with `bash scripts/bootstrap_android_tools.sh`. The script reached `apt-get update` while trying to provision JDK 17, but Debian repository DNS resolution was unavailable; the attempt was stopped after 30 seconds. Direct HTTPS checks to `services.gradle.org` and `dl.google.com` also failed with `curl: (6) Could not resolve host`. Therefore Gradle, JDK 17 and Android SDK packages could not be installed, and wrapper generation / Android build could not proceed.
- Emulator feasibility check: `/dev/kvm` is absent and no `vmx`/`svm` CPU virtualization flag is exposed. Do not attempt to install or start an emulator in this environment.
- Ran every Python verifier listed in the current `.github/workflows/android-apk.yml` migration/backup regression step: **31 scripts passed, 0 failed**. This includes all migration verifiers 7→8 through 15→16, backup/schema/media/snapshot checks, V2 source integrity, V3 Paging and Batch 2–8 readiness, and V3 final acceptance. `verify_v3_final_acceptance.py` reports **11/11** static checks passed.
- Ran `python3 tools/benchmark_v3_scalability.py`: synthetic host SQLite checks passed at 10,000 and 50,000 questions. All functional/index-plan checks passed. Library, simple Due and collection first-page plans use the intended indexes; recent reviews still report a temporary B-tree for the final ORDER BY term. Median timings are host SQLite only (10k→50k): Library 0.043→0.042 ms; Due snapshot 0.129→0.147 ms; collection page 0.048→0.051 ms; recent reviews 0.019→0.062 ms; active-card content 0.005→0.006 ms. These are not Android/Room/device measurements.
- Android lint, Kotlin/Compose/KSP compilation, Gradle JUnit, Room instrumentation, populated Android Room migration, Android JSON backup/restore, GitHub Actions execution, APK build/signing, and physical-device profiling/acceptance remain **not run**. The working directory has no `.git` metadata and no GitHub connector is available in this execution environment, so no push or workflow dispatch was performed.
- `StudyForge_Plan.txt` SHA-256: `8d5bebe805901cae7d243c41595458ccbe2a288d7ec49b01129f5443e4981abc`; it was not modified. App identity remains 2.1.24/code 28; Room 16; JSON backup 15; FSRS and signing configuration unchanged. No APK/release produced.
- Bootstrap script executable permission was corrected so the documented `./scripts/bootstrap_android_tools.sh` invocation is usable in the prepared source package.

# Build status addendum — V3.0 Batch 7 (2026-11-26)

- Removed root-level full-table rich-content and multipart-part observers. Study hydrates rich blocks for the active question only; Exam hydrates multipart parts for the bounded active queue only, waits before enabling Start Exam, and shows a load error. Added Room runtime test source for the bounded DAO paths.
- Added a host-Python SQLite synthetic benchmark for 10k and 50k question datasets, including Library page, Due queue, collection page, recent review and active-card rich-content queries plus query plans. Functional checks passed at both sizes. Median host timings (10k → 50k): Library 0.715 → 4.789 ms; Due 2.063 → 7.741 ms; collection 2.631 → 14.161 ms; recent reviews 0.021 → 0.068 ms; active content 0.016 → 0.006 ms. These are host-only measurements, not Android device performance.
- Query plans reveal `SCAN questions` + temporary ordering for Library and Due queue; collection paging scans questions while indexed membership lookups work. No speculative schema/index migration was introduced. A measured Room index migration and collection query rewrite remain outstanding.
- Local static checks passed: Batch 7 9/9; Batch 6 13/13; Batch 5 10/10; Batch 4 11/11; Batch 3 7/7; Batch 2 readiness; Paging foundation; schema-15 backup; release 2.1.24; V2 source integrity.
- Android Gradle/Kotlin/Compose/KSP compilation, JUnit, Room instrumentation execution, GitHub Actions, emulator, memory/frame profiling and physical-device testing were not performed.
- App remains 2.1.24/code 28, Room 15, JSON backup 15; FSRS, signing identity and original `StudyForge_Plan.txt` unchanged. No APK/release.

---

# Build status addendum — V3.0 Batch 6 (2026-11-26)

- Added Room-backed detailed Progress aggregates (type, topic, subject, chapter and mistake category), SQLite-grouped 14-day activity, an eight-row recent-review join, exam aggregate statistics (count/average/best/median/trend/bands), ten-row exam history, and scalar Study daily quota counts.
- Removed root-level full question, review-log and exam-attempt observation. Editor fetches by stable question ID; legacy comma-separated tag discovery uses a recursive Room query. Full backup/export remains an explicit complete transactional read.
- Local verification: Batch 6 static gate 13/13; Batch 5 10/10; Batch 4 11/11; Batch 3 7/7; Batch 2 readiness; Paging foundation; schema-15 backup; V2 source integrity. Exact exam aggregate SQL and tag-token SQL smoke-tested against SQLite. Room instrumentation test source added but not run.
- Android Gradle/Kotlin/Compose/KSP compilation, JUnit, Room instrumentation execution, GitHub Actions, APK build, signing and device testing were not performed. 10k/50k query/memory/frame benchmarks remain pending.
- App remains 2.1.24/code 28, Room 15, backup 15; FSRS, signing identity and original `StudyForge_Plan.txt` unchanged. No APK/release.

---

# Build status addendum — V3.0 Batch 5 (2026-11-26)

- Added Room aggregate projections for Home dashboard totals and Progress headline counts/accuracy windows. Added a Room instrumentation test source and `tools/verify_v3_batch5_readiness.py`; wired the verifier into GitHub Actions and the V2 source-integrity gate.
- Local verification: Batch 5 static gate 10/10; Batch 4 11/11; Batch 3 7/7; Batch 2 readiness; Paging foundation; schema-15 backup; V2 source integrity; and exact aggregate SQL smoke-tested against SQLite.
- A broad historical-verifier sweep also ran point-in-time scripts for backup schemas 10/12/13 and releases 2.1.16–2.1.23; those historical metadata assertions fail against the current 2.1.24/schema-15 source by design. Current 2.1.24/schema-15 gates pass.
- Scope boundary: detailed Progress analytics and recent-review prompt hydration still consume full lists; root question observation remains for editor, tag discovery, export/backup and other legacy consumers. This batch does not eliminate every app-wide full-table observation.
- No Android Gradle/KSP compile, JUnit, Room instrumentation execution, GitHub Actions run, APK build, signing operation or device test was performed. 10k/50k performance benchmarks remain pending.
- App remains 2.1.24/code 28, Room 15, backup 15; FSRS, signing identity and original `StudyForge_Plan.txt` unchanged. No APK/release.

---

# Build status addendum — V3 planning baseline (2026-11-26)

- This ZIP adds the V3 delivery plan and reconciles the maintained roadmap, master plan, and handoff with the latest user-reported device results.
- No Android source code or build configuration was changed in this planning update.
- No Gradle build, JUnit, emulator instrumentation, APK build, signing operation, or device test was run for this documentation-only package.
- Source identity remains app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15. FSRS, question records, and signing setup are unchanged.
- User-confirmed on-device results recorded here: Cloze no longer crashes, one-card Study works, Again/rating labels work, and Dark Mode works. Compact bottom-navigation source fix and Progress/other-screen visual polish still need final device confirmation.
- V3 is planned as 3.0 (scalable data/study foundation), 3.1 (deterministic numerical engine), and 3.2 (stepwise problem solving/advanced formats). No V3 implementation has started.

---

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


## 2026-11-25 — new-chat ZIP audit and local verification

- Opened and extracted the supplied `StudyForge-2.1.24-v2-ci-emulator-boot-fix.zip` (132 archive entries). Read the requested project documents, original `StudyForge_Plan.txt`, this handoff, current source-plan audit, build/development logs, signing instructions and third-party notices; inspected the current Kotlin/data/test tree, Gradle configuration, Room migration registration, backup paths, manifest and GitHub Actions workflow.
- Confirmed source metadata: app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15; migrations 1→2 through 14→15 registered; backup imports schemas 1–15. No Gradle wrapper, Gradle executable or Android SDK is present in this execution environment.
- Re-ran 11 current verification scripts: release verifier, V2 source-integrity verifier, backup snapshot/schema/media checks, bulk collection/tag/selection checks, live smart-filter checks, saved-session editing checks, and the pinned FSRS diagnostic. All 11 passed. Also ran all eight current SQLite migration smoke scripts (7→8 through 14→15); all passed.
- The pinned py-fsrs v6.0.0 diagnostic compared every transition in `test_memo_state`; the final state matched S=49.447237416 / D=6.827105750. This is standalone scheduler-core verification, not Android Gradle/JUnit or full upstream-suite parity.
- Source-level CI wiring includes lint, unit tests, and API 35 Room instrumentation. The latest user-reported workflow failure was emulator boot timeout before `connectedDebugAndroidTest`; the workflow now enables KVM and allows 900 seconds. This change still requires a fresh GitHub Actions run.
- Remaining acceptance gates: successful GitHub Actions lint/build/JUnit/emulator run; actual Android JSON export/import round-trip; populated historical Room migration runtime coverage; signed APK certificate continuity using the existing external key; and physical-device update/offline/backup acceptance. Continue auditing approved V1/V2 requirements (advanced editor/accessibility, broader smart collections/bulk workflows, deeper analytics/time-spent reliability, and audit attribution). Do not start V3.
- No production source, app version/versionCode, Room/backup schema, signing material, or original blueprint was changed in this audit. No APK was built or released. The original `StudyForge_Plan.txt` remains unchanged.

---

## 2026-11-26 — Study crash hardening and one-card UX / Library responsiveness

- Hardened `LatexFormulaView`: math parsing is cached between unrelated Compose recompositions, input above 1,500 characters uses a bounded plain-text fallback, and parser construction/assignment failures display a readable fallback instead of propagating into the Study/Library UI. The exact physical-device crash cause is not proven without its Android stack trace; malformed/unsupported imported formulas are a plausible trigger.
- Study queue derivations (today's logs, due cards, sorted/mode-specific queue, active saved-session filter) are now memoized so answer reveal, option selection and other local state changes do not repeatedly filter/sort the full question list. Content blocks are grouped by question ID once rather than scanning the complete block list for each recomposition.
- The Study flow continues to display exactly one active question. Added explicit card position/progress, renamed the reveal action to “Show answer”, and made Due mode move through the queue via Again/Hard/Good/Easy ratings rather than manual Previous/Next skipping. Practice and other exploratory modes retain manual navigation.
- Library search is debounced by 180 ms, and filtering plus saved-filter count calculations run on Dispatchers.Default rather than the UI thread. Question previews are capped at 360 characters and math is represented by a light “Math formula available” hint; the full formula is rendered only after opening the question or in the single active Study card. This reduces repeated native math-view creation while scrolling the Library.
- Added `tools/verify_study_ui_performance.py` and wired it into CI plus `verify_v2_source_integrity.py`.
- No FSRS source/test changes, Room migration, backup-schema change, version bump, signing change, APK build or release. Identity remains 2.1.24/versionCode 28, Room 15, JSON backup 15; original `StudyForge_Plan.txt` remains unchanged.
- Verification boundary: static source checks can run locally. Android Gradle/Compose compilation, Android unit tests, runtime crash reproduction and physical-device acceptance still require CI/device execution. A crash log is still needed to identify the precise original exception.

## 2026-11-26 — Cloze reimplementation source check (unreleased)

The Cloze source implementation has been replaced with the bounded scanner described in `CLOZE_REIMPLEMENTATION_AUDIT.md`. Standalone Kotlin harnesses passed: 7 parser/rendering checks and 5 CSV interchange checks. Current source-contract verification scripts passed, including the new Cloze verifier and the bulk-collection verifier. A Room Android instrumentation regression now exercises a Cloze question through question-version creation and collection assignment, but it has not been executed locally. Gradle and Android SDK are not installed in this environment, so no Android APK was built and no device crash was reproduced. Treat this as source-ready for CI, not as a verified release or confirmed phone-crash fix.


## 2026-11-26 — rating button layout patch

- Source fix: compact equal-width Again/Hard/Good/Easy buttons and single-line labels for narrow devices.
- Static verification: `verify_study_ui_performance.py` now asserts compact 4 dp horizontal content padding and `maxLines = 1` / `softWrap = false`.
- No Android APK was built in this environment; run GitHub Actions and visually confirm on the phone before treating the UI fix as device-verified.

## 2026-11-26 — bottom navigation label clipping fix

- Fixed narrow-screen bottom navigation labels: `Collections` displays as `Folders`, and `Progress` displays as `Stats`; full names remain available to accessibility services.
- Forced all seven display labels onto one line with compact 10sp typography. Touch targets and tab routing are unchanged.
- Added and CI-wired `tools/verify_navigation_layout.py`; aggregate V2 source-integrity verification now requires this regression gate.
- Current CI-wired Python source/migration checks pass locally. Android lint, Gradle/JUnit, emulator instrumentation, APK build and phone confirmation remain pending.
- No version, FSRS, Room/backup schema, signing, or original plan changes.


# Build status addendum — V3.0 batch 1 (2026-11-26)

- Added Paging 3 runtime/Compose and Room Paging dependencies.
- Added lightweight Library paging projection, filtered `PagingSource`/count DAO queries, repository, ViewModel, Compose paged-row component, bounded Study queue snapshot API, and Room instrumentation regressions.
- No Room/backup schema change; no app version bump; FSRS and signing configuration untouched.
- Static source review and archive-level checks are available, but Android Gradle/KSP compilation and emulator tests are **not verified**: this environment has no Gradle executable or Android SDK. Run GitHub Actions before treating this batch as buildable.
- Production Library/Study screens still use legacy full-list state; this is an architectural foundation only, not the completed pagination migration.

- The workflow now runs `verify_v3_paging_foundation.py`; development APK artifact labels are marked `V3.0-dev` to avoid presenting this as a 2.1.24 release.
- Local result: all 22 verification scripts invoked by the workflow pass, and the pinned FSRS diagnostic still passes. Ten historical version/schema fixture scripts are intentionally not part of current CI and fail when run against the current schema-15/version-2.1.24 source because they assert old snapshot values.


# Build status addendum — V3.0 Batch 2 Library integration (2026-11-26)

- Source implementation: active Library route now uses Paging 3 `LazyPagingItems` and lightweight Room projection rows. Database-backed filter counts, saved-preset counts, criteria-driven search/filtering, ID-based selection and chunked bulk status/difficulty/tag/collection actions are wired.
- Local static checks PASS: `verify_v3_batch2_readiness.py`, `verify_v3_paging_foundation.py`, `verify_backup_schema_15.py`, `verify_v2_source_integrity.py`. The new Room tag-substring instrumentation assertion is source-added only.
- Not verified: Gradle/Kotlin/Compose/KSP compilation, Android JUnit, Room instrumentation, emulator CI, signed APK, or physical-device behavior. This environment has no Gradle executable/wrapper or Android SDK.
- Global tag-manager CRUD has been restored in the paged destination. App-wide `observeQuestions()` remains for Home/Study/Progress; only the active Library path is paged.
- No APK, release or version bump. App remains 2.1.24/code 28; Room/backup schema 15; FSRS, signing identity and original plan unchanged.


# Build status addendum — V3.0 Batch 3 (2026-11-26)

- Added the bounded database-side Study queue query contract and wired its source-readiness verifier into CI. The query applies Study mode, saved-preset filters, collection membership and performance criteria before returning bounded IDs; hydration preserves snapshot order.
- Added `StudyQueueQueryTest` and Room instrumentation coverage for query validation, filters and mode ordering. These tests are source additions only; they have not been run by Gradle or on an Android emulator.
- Local source verifiers: Batch 3 readiness passed 7/7; Batch 2 readiness, Paging foundation, V2 source integrity, release 2.1.24, and all current non-historical checks passed. The all-files sweep also ran retired historical backup-schema 10/12/13 and release 2.1.16/2.1.18–2.1.23 scripts; those 10 failures are expected because those scripts assert obsolete fixture/version values, not current regressions.
- This environment still has no Gradle executable/wrapper or Android SDK/ADB, so Kotlin/KSP compilation, JUnit, Room instrumentation, APK build and device acceptance were not run.
- Active StudyScreen still consumes the full question list; queue API/UI integration, process recreation, exact daily quota/order parity, database aggregate migration, and 10k/50k benchmarks remain open.
- App remains 2.1.24/versionCode 28, Room schema 15, JSON backup schema 15. FSRS, signing setup and original `StudyForge_Plan.txt` are unchanged. No APK was produced.

## 2026-11-26 — V3.0 Batch 4 production Study queue migration

- Replaced `StudyScreen`'s full `List<QuestionEntity>` input with bounded Room ID snapshots and on-demand card hydration. Production app wiring now uses `QuestionRepository`; topic choices come from a distinct-topic Room flow.
- Preserved separate daily New/Review caps with independent `DueNew` and `DueReview` queries, including Oldest/Weakest/Random review ordering. Saved-session criteria are applied in SQLite. Exam mode selects eligible MCQ/True-False/Numerical/multipart records in Room and respects the exam randomize setting.
- Active queue IDs/fingerprint use Compose saved state to rehydrate the same snapshot after Android restores activity state. Added loading/error/retry states and queue tests.
- Static verifiers passed: Batch 4 (11/11), Batch 3 (7/7), Batch 2 readiness, Paging foundation, schema-15 backup, and V2 source integrity. The Study snapshot SQL also executed successfully in a standalone Python SQLite smoke test.
- Verification boundary: no Android Gradle/Kotlin/Compose/KSP build, JUnit, Room instrumentation, emulator, CI or physical-device test was run. Saved-state process restoration is source-wired, not runtime-proven. Home/Progress still use the global full question stream; review logs and rich content are still full-list inputs. 10k/50k benchmarks remain outstanding.
- No app version/versionCode, Room schema, backup schema, FSRS policy, signing identity or original `StudyForge_Plan.txt` change. No APK built or released.


## 2026-11-26 — V3.0 Batch 8 source update

- Room schema advanced additively from 15 to 16 with only two query-plan-driven indexes: Library sort `(updatedAt DESC, id ASC)` and Due ordering `(dueAt ASC, id ASC)`. Backup JSON schema remains 15.
- Due queue SQL now uses `dueAt ASC, id ASC`; SQLite's ascending NULL ordering preserves the previous explicit null-first order.
- Updated synthetic 10k/50k host SQLite benchmark to assert the Library, Due and collection-first-page query plans use the ordered question index without a temporary ORDER BY B-tree.
- Added populated-SQLite migration verifier and Room instrumentation test source for index presence/order; wired Batch 8 gates into CI.
- Android Gradle/KSP/JUnit, actual Room migration instrumentation, GitHub Actions and physical-device profiling remain unverified. No APK produced. App remains 2.1.24/versionCode 28; FSRS, signing and original `StudyForge_Plan.txt` unchanged.

# Build status addendum — V3.0 Batch 9 final acceptance preparation (2026-11-26)

- Added `V3_FINAL_ACCEPTANCE_RUNBOOK.md` covering GitHub Actions, Android lint/JUnit, API 35 Room instrumentation, populated schema-15→16 migration preservation, JSON backup/restore, and physical-device acceptance.
- Added `tools/verify_v3_final_acceptance.py`; 11/11 static preparation checks pass and the verifier is wired into CI.
- Re-ran all CI-wired Python migration, backup, V2/V3 source gates and the 10k/50k host SQLite benchmark. Migration 15→16 preserved populated rows and query-plan checks passed. Recent-review query still has a temporary B-tree for its final ID tie-break and is documented for measurement-led follow-up.
- Local environment has Java/Javac and kotlinc but no Gradle executable/wrapper, Android SDK tools or ADB. Android lint/compile, JUnit, Room instrumentation, emulator, APK, signing and physical-device profiling remain unverified. Do not describe V3.0 as accepted.
- App remains 2.1.24/code 28, Room 16, backup 15; FSRS/signing/original `StudyForge_Plan.txt` unchanged. No APK produced.

---

# Toolchain bootstrap attempt — 2026-11-26 (ChatGPT execution environment)

## Environment inspected

- OS/architecture: Debian GNU/Linux 13 (trixie), x86_64.
- Available resources at inspection: approximately 30 GiB free disk, 5.8 GiB RAM, 3 visible vCPUs.
- Java/Javac: OpenJDK 21.0.12.1 is present; the project specifies Java 17. JDK 17 is not installed.
- Gradle/Gradle Wrapper: no `gradle` executable and no wrapper files in the supplied source package.
- Android SDK/ADB: no `sdkmanager`, `avdmanager`, `adb`, Android platform or build-tools found in standard locations.
- Emulator acceleration: `/dev/kvm` is absent; no `/dev/dri` device is available. Emulator acceleration is therefore not available in this container as inspected.
- Network: DNS resolution failed for `services.gradle.org` (`curl: (6) Could not resolve host`). This prevents downloading Gradle, JDK packages, or Google Android command-line tools in this environment. APT package metadata also exposes no install candidates in the current container.

## Actions completed here

- Extracted and inspected the supplied V3.0 Batch 9 project archive and its tooling/handoff instructions.
- Ran current source/static readiness gates: V3 final acceptance preparation (11/11), Batch 8 (9/9), Batch 7 (9/9), Batch 6 (13/13), Batch 5 (10/10), Batch 4 (11/11), Batch 3 (7/7), Batch 2, Paging foundation, backup schema 15, and V2 source integrity. All reported PASS.
- `python3 -m compileall -q tools` passed.
- ZIP integrity check passed.
- Added `scripts/bootstrap_android_tools.sh`, a reproducible Debian/Ubuntu bootstrap for JDK 17, checksum-verified Gradle 8.13, Android command-line tools, SDK Platform 36, Build Tools 36.0.0, ADB, Gradle Wrapper generation and a debug-build smoke test. The Google command-line-tools ZIP URL is supplied explicitly to avoid guessing a changing package revision.

## Not completed / verification boundary

- Actual installation is blocked by unavailable network/DNS and missing package sources; no Android toolchain component was newly installed.
- No Gradle build, Kotlin/Compose/KSP compile, Android JUnit, Room instrumentation, emulator test, APK build, signing, or physical-device test was run here.
- No emulator attempt was made because KVM is unavailable.
- This container's environment may be ephemeral; installation and build verification must be rerun in a network-enabled development runner using `scripts/bootstrap_android_tools.sh`.

Do not report the Android toolchain as installed or V3.0 as accepted. GitHub Actions remains the independent Android build/test route.
