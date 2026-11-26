# V3.0 Batch 5 — Home and Progress core aggregates

**Date:** 2026-11-26  
**Status:** Source implementation complete for core dashboard/headline aggregates; Android build/runtime/device validation pending.  
**Baseline preserved:** app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15.

## What changed

- Added `HomeDashboardStats`, populated by a Room scalar aggregate query for question count, collection count, summed question review count, and due count.
- Home now consumes the aggregate projection instead of calculating those four values by iterating the full observed question list.
- Added `ProgressCoreStats`, populated by Room for question count, due/new/learning/mastered counts, total/scored/correct reviews, 30-day scored/correct reviews, last-seven-day review count, and all-time distinct active review days.
- Progress headline cards and learning-status totals now consume `ProgressCoreStats`.
- Added Room instrumentation test source covering aggregate correctness with New, Learning and mastered Review questions plus scored review logs.
- Added `tools/verify_v3_batch5_readiness.py` and wired it into GitHub Actions static verification and the V2 source-integrity verifier.

## Verification performed in this environment

- `python3 tools/verify_v3_batch5_readiness.py`: 10/10 source-shape checks passed.
- Executed the exact Home and Progress aggregate SQL against a representative in-memory SQLite dataset; counts and 30-day/7-day windows returned the expected values.
- Prior Batch 2, 3, 4, Paging foundation, backup schema 15 and V2 source-integrity checks are re-run for this package (results recorded in delivery notes).
- Android Room instrumentation test is added but has **not** been executed here.

## Explicit scope boundary

This batch migrates Home dashboard totals and Progress headline aggregates only. Detailed Progress topic/type/subject/chapter/mistake-category analytics, bounded recent-review prompt hydration, and exam score distribution/median/trend/history remain on full-list inputs. Root `observeQuestions()` remains for legacy editor, tag discovery, export/backup and other flows. Do not describe this batch as eliminating all full-table observations.

## Compatibility and safety

- No Room entities, tables, indexes, migrations or schema version changes.
- No JSON backup schema change.
- No FSRS scheduler or rating behavior change.
- No app version, versionCode, package identity, signing configuration or original `StudyForge_Plan.txt` change.
- No APK was built or released.

Historical point-in-time scripts for backup schemas 10/12/13 and app releases 2.1.16–2.1.23 were also swept; their hard-coded historical metadata assertions fail against the current schema-15 / 2.1.24 source by design. Current schema-15 and 2.1.24 gates pass.

## Not verified

- Android Gradle/Kotlin/Compose/Room KSP compilation.
- JUnit and Room instrumentation execution.
- GitHub Actions emulator run and physical-device testing.
- 10k/50k dataset performance, query plans, memory or frame-time benchmarks.
