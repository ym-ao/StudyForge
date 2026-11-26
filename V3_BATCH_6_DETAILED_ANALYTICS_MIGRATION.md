# V3.0 Batch 6 — Detailed Analytics & Full-List Elimination

**Date:** 2026-11-26  
**Source baseline:** Batch 5 ZIP  
**Status:** Source implementation and static/SQLite checks completed; Android compilation and runtime validation pending.

## Implemented

- Moved Progress question-type accuracy, weak-topic counts, weak-subject counts, weak-chapter counts and mistake-category counts into compact Room projections.
- Added an eight-row recent-review projection that joins the current question prompt in SQLite; the Progress screen no longer receives all review logs or all questions.
- Moved the 14-day review activity aggregation into SQLite and retained calendar gap-filling, accuracy labels and current streak calculation in the screen.
- Moved exam attempt count, average, best, median, five-vs-previous-five trend and score-band counts into one Room aggregate projection.
- Limited displayed exam history to the latest ten attempts. Full history remains available to explicit backup export.
- Replaced the Study screen's full review-log input with two scalar daily quota counts (new cards and reviews).
- Removed root-level full `observeQuestions()`, `observeReviewLogs()` and `observeExamAttempts()` collection. The editor now resolves its selected question by stable ID; legacy comma-separated tag discovery uses a recursive Room query.
- Added Room instrumentation test source for analytics projections, bounded recent history, exam aggregates, daily quotas and legacy CSV tag discovery.
- Added `verify_v3_batch6_readiness.py` to CI and updated the source-integrity check for the database-backed daily trend.

## Preserved

- Room schema 15 and JSON backup schema 15; no migration was added.
- App version 2.1.24 / versionCode 28, package identity and signing configuration.
- FSRS scheduler and review-log records. Study daily quota semantics remain New vs non-New review counts.
- Complete backup export/import: backup still explicitly reads all persisted records on user action inside the existing transaction.
- Original `StudyForge_Plan.txt` unchanged.

## Verification evidence

- Batch 6 static readiness: 13/13.
- Existing Batch 5 (10/10), Batch 4 (11/11), Batch 3 (7/7), Batch 2, Paging foundation and schema-15 backup gates pass.
- V2 source-integrity checks pass after updating the trend assertion to the Room projection contract.
- Exact exam aggregate SQL smoke-tested in SQLite with ten known scores; average 55, best 100, median 55, recent trend +50 and bands 3/2/2/3.
- Recursive tag-token SQL smoke-tested with mixed case and whitespace.
- ZIP integrity verified after packaging.

## Not verified here

No Gradle executable/wrapper or Android SDK is available in this local environment. Kotlin/Compose/KSP compilation, JUnit, Room instrumentation, emulator CI, physical-device behavior and 10k/50k performance measurements remain pending. Static checks and SQLite smoke tests are not substitutes for those gates.

## Remaining V3.0 work

- Review and remove remaining full-list consumers for rich content blocks, question parts, collection links and backup/export paths where it is safe and useful; explicit backup export must remain a complete snapshot.
- Inspect query plans and add indexes only when supported by measured evidence.
- Build 10k functional and 50k stress datasets; record query latency, memory, and frame performance.
- Run the complete Android/CI/device acceptance suite and confirm the UI behavior on the user's phone.
