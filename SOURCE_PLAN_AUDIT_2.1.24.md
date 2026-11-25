## FSRS-6 follow-up correction — 2026-11-25

The earlier sections below describe the pre-correction source snapshot and are historical. The current `ReviewScheduler.kt` has since been corrected against the pinned py-fsrs v6.0.0 commit `352c7a2838071447fb1c55d9fbbbc04fa637ee81`.

- Exact pinned defaults and behavior are now in production source: clamped initial-Easy mean-reversion target; same-day stability floor only for Good/Easy; upstream Hard step-zero midpoint/1.5x delays; completed-24-hour elapsed-day semantics; ties-to-even interval rounding; empty-step behavior.
- `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py` now passes all seven per-transition memory-state/scheduler-state comparisons. Final S=49.447237416 and D=6.827105750.
- A lightweight Kotlin/JUnit-compatible runner compiled and executed 32 tests across `ReviewSchedulerTest.kt` and `Fsrs6ReferenceInvariantTest.kt`: 32 passed, 0 failed. This does not replace the actual Gradle/JUnit run.
- Android Gradle/JUnit, Room runtime migrations, real backup/restore, GitHub Actions, signing and physical-device acceptance remain unverified. No APK/version bump; 2.1.24/28, Room 15, JSON 15. Original blueprint unchanged.

Pinned source: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/fsrs/fsrs.py`; pinned fixture: `https://github.com/open-spaced-repetition/py-fsrs/blob/352c7a2/tests/test_basic.py`.

---

# Source-vs-plan audit — StudyForge 2.1.24

Audit basis: the complete `StudyForge-2.1.24-FSRS6.zip`, including `StudyForge_Plan.txt`, `PROJECT_PLAN.md`, `IMPLEMENTATION_STATUS.md`, `ROADMAP.md`, current Kotlin source, Room database/migrations, backup manager, tests, Gradle configuration and GitHub Actions workflow.

## Release identity and compatibility

- App version: 2.1.24; versionCode: 28.
- Room schema: 15; migration chain 1→2 through 14→15 is present in source.
- JSON backup schema: 15; import compatibility 1–15 is documented and source-backed.
- The supplied ZIP contains no Gradle wrapper. No Gradle executable is available in this environment, so Android compilation/JUnit cannot be asserted.
- No keystore or signing credentials are present in the ZIP. The existing signing setup remains external and must not be regenerated.

## FSRS-6 audit

- `ReviewScheduler.kt` contains the published 21 default weights, the 0.001 stability floor, the trainable-decay forgetting curve, initial state, difficulty update, same-day short-term stability and long-term recall/forget updates.
- The 2.1.24 tests cover four long-term ratings and several equation invariants.
- This audit adds exact initial-state assertions for Again/Hard/Good/Easy using the published default weights and initial-difficulty equation.
- The FSRS-6 reference sources confirm the 21 weights and stability floor. The current tests are equation-derived fixtures, **not** a checked-in upstream golden-vector corpus and do not establish full py-fsrs API/state-machine parity.
- StudyForge's local-calendar study-day handling and its explicit learning-step policy (notably Hard repeating the current step) are product behavior. They must be compared/documented separately rather than silently replaced with upstream scheduler policy.

## Outstanding V1/V2 acceptance gates

1. Obtain/pin a licensed upstream FSRS-6 reference fixture corpus or generate fixtures by executing a pinned upstream implementation; record exact upstream version/commit and provenance.
2. Compare initial states, same-day transitions, long-term transitions, elapsed-day boundaries, interval rounding/clamping and complete rating sequences.
3. Run full Android Gradle/Compose/KSP/JUnit in GitHub Actions.
4. Run Room runtime migration tests and Android JSON backup/restore round-trip tests.
5. Complete the original blueprint's remaining advanced editor/accessibility, bulk/smart-collection, analytics/time-spent, audit-attribution and device-acceptance requirements.
6. Verify existing signing certificate continuity and physical-device update behavior using the user's existing signing setup; no signing key is included or modified here.

## Verification boundary for this audit

Static source checks and standalone Kotlin scheduler-core checks are distinct from Android builds, JUnit execution, CI, signing, and device acceptance. Do not mark V1/V2 complete until the original blueprint and all required acceptance gates are satisfied.

## 2026-11-25 continuation addendum

- Fixed the workflow's stale 2.1.23 verifier reference and added a CI static source-integrity gate.
- The gate confirms current app metadata, Room schema 15 and migration registrations 1→2 through 14→15, JSON schema 15 export/import compatibility, current workflow checks/artifact labels and documented signing secrets/alias.
- Added a same-day Hard regression case.
- Local source/migration/backup checks passed. This does not establish Android/JUnit, Room runtime, actual backup/restore, GitHub Actions execution, signed APK or device success.

## 2026-11-25 additional V2 runtime-test source

Added Android Room instrumentation tests for current-schema persistence across a database close/reopen and for dependent-row cascade deletion. Added AndroidX test dependencies and an API 35 emulator CI step. The static integrity verifier checks the test source and workflow wiring. No emulator run has been performed locally; no successful CI execution is claimed. The test source does not yet cover populated historical migrations across the full 1→15 chain or app-level JSON backup/restore.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



## 2026-11-25 continuation — CI lint and collection lifecycle coverage

The follow-up pass adds one more Room instrumentation scenario for collection deletion and the cleanup of both saved-filter and saved-session collection references. It also adds Android `lintDebug` to the CI sequence before unit tests and emulator tests. The static verifier now requires these checks to remain wired. The pure Kotlin table/graph validator was compiled and exercised locally with seven smoke assertions. Android instrumentation, Gradle lint, and the full JUnit suite remain unexecuted in this environment.


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

### Upstream FSRS-6 references consulted

- py-fsrs v6.0.0 release/tag: `https://github.com/open-spaced-repetition/py-fsrs/releases/tag/v6.0.0` (release commit `352c7a2`).
- Pinned v6.0.0 basic scheduler tests: `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`.
- Upstream scheduler `_next_difficulty` reference formula (current upstream source consulted for the unclamped initial-Easy mean-reversion target): `https://github.com/open-spaced-repetition/py-fsrs/blob/main/fsrs/scheduler.py`.

The `test_memo_state` expected result is explicitly recorded above as an unresolved comparison. Do not treat the StudyForge output as matching this upstream fixture.


## 2026-11-25 continued source-vs-plan audit — FSRS mismatch reproduced

- Re-inspected the uploaded source tree, current plan, original `StudyForge_Plan.txt`, implementation matrix, roadmap, logs, Room entities/DAO/migrations, backup import/export, app manifest, Gradle files, unit/instrumentation test sources and GitHub Actions workflow. The original blueprint remains byte-for-byte unchanged (SHA-256 `8d5bebe805901cae7d243c41595458ccbe2a288d7ec49b01129f5443e4981abc`).
- Confirmed source metadata: app 2.1.24 / versionCode 28; Room schema 15 with migrations 1→2 through 14→15 registered; JSON backup schema 15 with imports 1–15. No destructive Room fallback is present.
- Found and corrected a documentation mismatch in `PROJECT_PLAN.md`: its maintained technical-baseline section still stated Room/backup schema 10 and migrations only through 9→10. It now reflects schema 15 and the full registered chain. `StudyForge_Plan.txt` was not edited. The V2 source-integrity script now asserts that the maintained plan's metadata matches source.
- Recompiled the exact pure `ReviewScheduler` object with lightweight entity/settings stubs (Room transaction wiring excluded) and replayed the pinned py-fsrs v6.0.0 `test_memo_state` sequence: Again, Good×5 at elapsed-day offsets 0, 0, 1, 3, 8, 21, then another Good at the same timestamp, with 1-minute/10-minute learning steps. StudyForge produced final S=53.626902917141386 and D=6.34635796621067. The pinned upstream test asserts rounded S=49.4472 and D=6.8271. The discrepancy is therefore reproduced from the included source; it is not resolved.
- No scheduler math was changed: a speculative correction could damage persisted scheduling states. The next safe FSRS task is to compare every intermediate state and exact v6.0.0 source branch/transition, while separately preserving StudyForge's explicit Hard-repeat-current-step and local-calendar-day product policies. Do not claim FSRS parity.
- Current source checks executed with `python3 -S` passed: `verify_release_2_1_24.py`, `verify_v2_source_integrity.py`, `verify_backup_media_import.py`, `verify_backup_schema_15.py`, and SQLite smoke checks 7→8 through 14→15. Historical verifiers for schemas 10/12/13 are archival and are not current release gates.
- Android Gradle/Compose/KSP/JUnit, Room runtime migrations, app-level Android backup/restore, GitHub Actions execution, signed APK/certificate continuity and physical-device acceptance remain unverified. The ZIP has no Gradle wrapper; this environment has no Gradle executable or Android SDK.
- No app version/versionCode bump, APK release, Room/backup schema change or signing-key operation occurred.


## 2026-11-25 continuation — strict CSV/Anki quoted-field parsing

- Hardened the shared CSV/TSV parser: a quote may only open a field at its beginning; after a closing quote only a delimiter, record separator or end-of-input is accepted. Malformed quote placement now produces a row-level import preview issue instead of being silently reinterpreted. RFC-style doubled quotes, quoted delimiters, quoted newlines and CRLF remain supported.
- Added JUnit regression source for quotes inside unquoted fields, trailing characters after a closing quote, and valid quoted fields with CRLF. Existing CSV round-trip and Anki quoted-tab/newline cases remain in the suite.
- Compiled the exact production parser with a lightweight entity stub using kotlinc and passed six standalone smoke assertions, including export/import round-trip and malformed-input rejection. Android JUnit remains unexecuted because Gradle/Android SDK are unavailable here.
- No scheduler math, Room schema, JSON backup schema, migration, app version, versionCode, release signing or original blueprint changes.


## 2026-11-25 rich-content persistence test-source addendum

The current-schema Room instrumentation suite now includes a close/reopen test for ordered TEXT/HINT/IMAGE/TABLE/GRAPH content blocks and embedded image metadata. The source-integrity verifier checks this test's presence. This improves source-level acceptance coverage for the original rich-content requirements but does not prove Android runtime behavior until emulator CI runs. It does not close the separate full backup/restore, historical migration, rendering/accessibility, signing or device gates.

## 2026-11-25 FSRS-6 conformance diagnosis — pinned-reference mismatch identified

- Built and ran `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. It extracts and compiles the exact production `ReviewScheduler` object with minimal Room/entity stubs using `kotlinc`, then replays the pinned `py-fsrs` v6.0.0 `test_memo_state` fixture. In parallel, it runs a local reference port of the exact upstream implementation/defaults from commit `352c7a2`. No network or third-party Python package is required; `kotlinc` and a JRE are required.
- The reference port reproduces the upstream golden result exactly: final S=49.4472374165 and D=6.8271057501 (rounded S=49.4472 / D=6.8271). The exact StudyForge Kotlin source returns S=53.6269029171 and D=6.3463579662.
- Root cause confirmed: the comparison was mixing different default parameter vectors. Pinned py-fsrs v6.0.0 `fsrs/fsrs.py` has defaults `[0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2]`; StudyForge currently uses `[0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542]`. The vectors differ in all 21 positions. The first mismatch is already visible on the initial Again: upstream S/D=0.2172/7.0114; StudyForge=0.212/6.4133.
- Two additional pinned-v6.0.0 behavioral differences were found: (1) upstream v6.0.0 clamps initial Easy difficulty before using it as the mean-reversion target; StudyForge currently passes `clamp=false`; (2) upstream v6.0.0 short-term stability increase is clamped to >=1 for Good/Easy, not Hard, while StudyForge clamps every grade >= Hard. The Hard difference does not cause the `test_memo_state` fixture failure because that fixture contains no Hard ratings.
- This corrects an earlier investigation note that relied on the newer `main/fsrs/scheduler.py` formula and treated its unclamped mean-reversion target as the pinned v6.0.0 contract. The actual pinned commit's source is `fsrs/fsrs.py`, and its behavior is different. Do not use current-main source as a substitute for the pinned reference.
- No production FSRS code was changed. This task was diagnosis only. Before changing production, decide explicitly whether StudyForge's compatibility target is the exact py-fsrs v6.0.0 defaults/behavior or a newer FSRS-6 parameter baseline. If the target remains pinned v6.0.0, the defaults and the two behavioral differences above must be reconciled and regression-tested together.
- Source references: pinned upstream implementation `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/fsrs/fsrs.py`; pinned fixture `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`. Local diagnostic output was reproduced successfully on 2026-11-25. This is a standalone source harness, not Android Gradle/JUnit or emulator verification.
