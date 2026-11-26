# V3.0 Batch 8 — Query-plan-driven database optimization

**Date:** 2026-11-26  
**Source baseline:** Batch 7 lazy-content/scalability ZIP  
**Status:** source implementation and host SQLite migration/benchmark checks completed; Android/Room/device acceptance pending.

## Why this batch exists

Batch 7's synthetic SQLite `EXPLAIN QUERY PLAN` output showed that the Library first-page and simple Due queue queries scanned `questions` and used temporary sorting. The collection first-page query also scanned candidate questions, although membership lookup used the `question_collection` primary-key index. Batch 8 adds only the two question indexes supported by that evidence.

## Implemented

- Room database version advanced from 15 to 16.
- Added additive `MIGRATION_15_16`, creating:
  - `index_questions_updatedAt_id` on `questions(updatedAt DESC, id ASC)`.
  - `index_questions_dueAt_id` on `questions(dueAt ASC, id ASC)`.
- Declared matching Room entity indexes so fresh databases and migrated databases have the same schema.
- Rewrote the simple Due queue ordering as `dueAt ASC, id ASC`. SQLite sorts NULL before non-NULL in ascending order, preserving the prior explicit null-first semantics while making the index usable.
- Collection first-page continues to filter membership with the existing `(questionId, collectionId)` primary-key index and now scans questions in Library display order through the new sort index. This removes the full table scan and temporary sort in the synthetic plan, but sparse collections can still require scanning many ordered question candidates.
- Updated the 10k/50k host SQLite benchmark to gate the Library, Due and collection plans on index use and absence of a temporary ORDER BY B-tree.
- Added `tools/verify_migration_15_16.py`, which executes the exact migration SQL against populated SQLite data, checks row preservation/index creation/order and verifies query plans.
- Added Room instrumentation test source for current-schema index presence and null-first Due order.
- Added `tools/verify_v3_batch8_readiness.py` and CI wiring.

## Host SQLite results

Environment: Python `sqlite3`, synthetic data, seven repetitions, median query time. These are diagnostics only, not Android Room or physical-device timings.

| Query | 10k median | 50k median | Plan result |
|---|---:|---:|---|
| Library first page | 0.100 ms | 0.061 ms | ordered `index_questions_updatedAt_id` scan |
| Due queue snapshot | 0.291 ms | 0.324 ms | covering `index_questions_dueAt_id` scan |
| Collection first page | 0.069 ms | 0.072 ms | ordered question index + cross-reference primary-key lookup |
| Recent reviews | 0.026 ms | 0.110 ms | review timestamp index; final tie-break sort remains |
| Active-card content | 0.007 ms | 0.007 ms | `(questionId, position)` index |

The timings vary with host/cache and synthetic distribution. The important result is the query-plan regression gate, not the absolute latency.

## Compatibility

- App version remains 2.1.24 / versionCode 28.
- Room schema is now 16; migration 15→16 is additive and does not rewrite question rows.
- JSON backup schema remains 15; import compatibility remains schemas 1–15.
- FSRS, backup payload, signing identity and original `StudyForge_Plan.txt` remain unchanged.
- No APK was built or released.

## Verification boundary

- Exact migration SQL against a populated host SQLite database: implemented as a verifier; run locally for this package.
- Synthetic 10k/50k functional and query-plan checks: run locally for this package.
- Static Batch 8 source checks: run locally for this package.
- Android Gradle/KSP/Compose compilation, JUnit, Room instrumentation execution, actual Room 15→16 migration on Android, GitHub Actions, emulator, physical-device query/memory/frame profiling and backup/restore round-trip: not executed here.

## Remaining V3.0 acceptance

1. Run the CI lint, unit-test and emulator instrumentation workflow.
2. Test a populated real Room schema-15 database upgrading to 16, preserving all question/review/collection rows.
3. Run JSON backup/restore against schema 15 before and after Room migration.
4. Profile 10k/50k libraries on a documented Android device, including cold/warm query latency, scrolling frames and memory.
5. Review the generic saved-session Due queue query separately: its conditional multi-mode `ORDER BY` is more complex than the simple Due snapshot and may not use the due index for every mode/order combination.
6. Consider a dedicated collection-membership-first paging strategy only if device measurements show the ordered-question scan is costly for sparse collections.
