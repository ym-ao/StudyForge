# V3.0 Batch 7 — Lazy rich-content hydration and scalability diagnostics

**Date:** 2026-11-26  
**Source baseline:** Batch 6 detailed analytics ZIP  
**Status:** source implementation and host-SQLite synthetic benchmark completed; Android/Room/device acceptance pending.

## Implemented

- Removed the root `StudyForgeApp` collectors that emitted every `QuestionContentBlockEntity` and every `QuestionPartEntity` into Compose state.
- The one-card Study screen now requests rich blocks only for the currently active question ID, orders them by `position`, and shows a loading indicator while they hydrate. A failure does not crash Study; the card continues without optional blocks.
- Exam multipart eligibility now loads parts only for the bounded active exam queue (Study queue contract maximum: 500 IDs), instead of observing the entire `question_parts` table. The Start Exam action waits for hydration and surfaces a load error rather than silently starting with incomplete eligibility.
- Added `getQuestionPartsForQuestions(ids)` as a bounded Room DAO query. Existing per-question block/part read APIs remain available to editor/history workflows.
- Added an Android Room instrumentation test source proving block reads are question-scoped and multipart reads are restricted to the requested exam ID set.
- Added `tools/benchmark_v3_scalability.py`, a deterministic synthetic SQLite workload benchmark for 10k and 50k questions. It checks data completeness, unique IDs, bounded queue size, page/queue/collection/recent-review/rich-content query timings and `EXPLAIN QUERY PLAN` output. It is wired into GitHub Actions.
- Added `tools/verify_v3_batch7_readiness.py` (9/9 source-contract checks) and wired it into CI.

## Host benchmark results

Environment: Python `sqlite3` with synthetic tables and the currently declared Room entity indexes mirrored in the benchmark schema. Seven repetitions per query; median elapsed time. These numbers are host-side diagnostics, not Android Room/device performance results.

| Query | 10k median | 50k median | Rows returned at 50k |
|---|---:|---:|---:|
| Library first page | 0.715 ms | 4.789 ms | 30 |
| Due queue snapshot | 2.063 ms | 7.741 ms | 500 |
| Collection first page | 2.631 ms | 14.161 ms | 30 |
| Recent review history | 0.021 ms | 0.068 ms | 8 |
| Active-card rich content | 0.016 ms | 0.006 ms | 1 |

Functional benchmark checks passed at both sizes. The timings are not release targets and must not be compared directly to phone latency.

## Query-plan findings and remaining optimization

The benchmark exposes real index opportunities rather than hiding them:

- Library first page: SQLite reports `SCAN questions` and a temporary B-tree for `ORDER BY`.
- Due queue snapshot: SQLite reports `SCAN questions` and a temporary B-tree for `ORDER BY`.
- Collection first page: SQLite scans candidate questions, while the cross-reference lookup uses the composite primary-key index.
- Recent reviews use the `reviewedAt` index and question primary-key lookup; SQLite still sorts the final tie-breaker.
- Active-card rich-content lookup uses the `(questionId, position)` index as intended.

No speculative Room index or schema migration was added in this batch. The query-plan evidence should guide a separately reviewed additive migration (likely sort/due indexes) with populated-database migration tests before changing Room schema 15. The collection-first-page query also needs a measured rewrite/plan review. This is an explicit outstanding performance task, not a claim that all hot queries are indexed.

## Verification

- Batch 7 static readiness: 9/9.
- Batch 6: 13/13; Batch 5: 10/10; Batch 4: 11/11; Batch 3: 7/7.
- Paging foundation, Batch 2, backup schema 15, release metadata 2.1.24 and V2 source-integrity checks passed locally.
- Synthetic SQLite functional benchmark: 10k and 50k datasets passed all integrity/bounds checks.
- Room instrumentation test source was added but not executed. No Gradle/Android build, Kotlin/KSP/Compose compile, Android JUnit, emulator, GitHub Actions run, memory/frame measurement or physical-device benchmark was performed in this environment.

## Compatibility and release boundary

- App remains 2.1.24 / versionCode 28.
- Room schema remains 15; JSON backup schema remains 15.
- No migration, FSRS, signing identity, backup contract or original `StudyForge_Plan.txt` change.
- No APK was built or released. V3.0 is not acceptance-complete.

## Next work

Use the captured query plans to design and test a minimal additive Room index migration for Library ordering and Study due ordering, and optimize collection paging. Then run the complete Android CI/emulator suite, profile on a documented physical device, and validate populated migrations, backup/restore and process restoration before V3.0 closeout.
