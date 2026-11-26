# V3.0 Batch 4 — production Study queue migration

**Date:** 2026-11-26  
**Status:** Source implementation complete; Android build/runtime/device validation pending.  
**Baseline preserved:** app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15.

## What changed

- The production `StudyScreen` no longer accepts `List<QuestionEntity>` as its source of study cards. It requests a bounded ID snapshot from `QuestionRepository`, then hydrates only those IDs in snapshot order.
- `StudyForgeApp` wires the production screen to `createStudyQueueSnapshot(StudyQueueQuery, now)` and `loadStudyQueueCards(ids)`.
- Topic chips are populated by a Room `SELECT DISTINCT trim(topic)` flow rather than deriving topics from the full question list.
- Due mode is split into separate database snapshots for New and Review cards so the configured daily new-card and review-card quotas remain independent. The SQL path supports Oldest, Weakest and Random review ordering; due cards are filtered by due time in SQLite.
- Saved-session subject, chapter, topic, tag, difficulty, exam, question type, status, collection and performance criteria are applied in Room before the bounded limit.
- Ordered, Weakest, Random and Topic modes are selected in the database. Random order is materialized in the ID snapshot.
- Exam mode now asks Room only for eligible MCQ, True/False, Numerical or multipart questions. The configured randomize setting is passed into the query; non-random exam order uses creation time.
- Active queue IDs and a criteria fingerprint use Compose saved state, allowing the same bounded queue order and current index to be restored after activity/process recreation when Android restores saved instance state. Cards are rehydrated by ID.
- Loading, query-error and retry UI states are included. A completed Due card is removed from the in-memory active ID snapshot immediately; updated review-log quotas cause a fresh Due snapshot when the database emits the new log.
- Added Room instrumentation source for DueNew/DueReview quota partitioning, Weakest ordering and Exam eligibility; extended query validation tests and added a Batch 4 source-readiness verifier wired into GitHub Actions and the V2 source-integrity gate.

## Compatibility and safety

- No Room entity, table, index, migration or schema-version changes.
- No JSON backup schema change.
- No FSRS scheduler/rating behavior change is intended; existing `onReview` and `recordGradedReview` pathways remain the review boundary.
- No app version, versionCode, package identity, signing configuration or original `StudyForge_Plan.txt` change.
- The Study session snapshot is bounded to at most 500 IDs (or the saved preset's lower limit). This is intentional; the screen no longer materializes the full question table to create a session.

## Verification performed in this environment

- `python3 tools/verify_v3_batch4_readiness.py`: 11/11 source-shape checks passed.
- `python3 tools/verify_v3_batch3_readiness.py`: 7/7 checks passed.
- `python3 tools/verify_v3_batch2_readiness.py`: passed.
- `python3 tools/verify_v3_paging_foundation.py`: passed, including existing SQLite page/count SQL checks.
- `python3 tools/verify_backup_schema_15.py`: passed.
- `python3 tools/verify_v2_source_integrity.py`: passed, including the new Batch 4 CI wiring; Android runtime remains explicitly unverified.
- `python3 tools/verify_study_ui_performance.py`: passed after updating its Study checks to the new bounded-snapshot architecture.
- Independently executed the new Study snapshot SQL with Python SQLite against representative New/Review/Due/Exam records. DueNew, DueReview Oldest/Weakest, Exam eligibility and randomized Exam selection returned the expected records.
- ZIP integrity and SHA-256 manifest are generated for the delivery archive.

## Not verified / remaining

- Android Gradle, Kotlin, Compose, Room KSP compilation.
- JUnit and Room instrumentation execution on Android.
- GitHub Actions emulator run and physical-device Study UI testing.
- Process-death restoration under real Android lifecycle conditions; saved-state wiring is source-implemented but not runtime-proven.
- Full-library Home/Progress/other consumer observation remains elsewhere in `StudyForgeApp`; this batch removes the full question list from `StudyScreen`, not from every app destination.
- Review logs and rich content/parts are still observed as full lists; aggregate and lazy rich-content migrations remain future work.
- Large-library 10k/50k performance and memory benchmarks.

Do not treat this source batch as a release or as full V3.0 completion.
