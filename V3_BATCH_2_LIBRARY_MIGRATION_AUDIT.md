# V3.0 Batch 2 — Production Library Paging migration report

**Date:** 2026-11-26  
**Baseline:** app 2.1.24 / versionCode 28 / Room schema 15 / JSON backup schema 15  
**Status:** production Library Paging path source-integrated with filters, presets, tag CRUD and ID-based bulk actions; Android runtime validation remains open.

## Implemented

- The active root-navigation Library destination is `PagedLibraryScreen`; it receives `LibraryViewModel`, saved presets and collections, not `List<QuestionEntity>` or the complete collection-link list.
- `PagedLibraryScreen` collects `viewModel.rows` using `collectAsLazyPagingItems()` and renders `QuestionLibraryRow` with stable question IDs, bounded Paging load state, append/refresh error handling and retry.
- Search is debounced and criteria are held in `LibraryViewModel`; query changes use `flatMapLatest` to cancel stale streams. Active filters are sent to Room for type, status, difficulty, subject, chapter, topic, tag, exam, source title/detail, created/updated date ranges, collection and performance.
- Current and saved-preset match counts use `QuestionRepository.countLibrary()` / Room `COUNT(*)`; the active Library no longer computes counts by filtering a full question list.
- Selection is an ID set independent of loaded entity objects. It survives paging boundaries while the screen is alive. “Select loaded rows” is explicitly scoped to rows currently held by Paging; selected IDs are hydrated in chunks of at most 900 for transactional status/difficulty/tag/collection bulk operations.
- Edit, delete and schedule actions resolve records by ID on demand. Bulk content/collection changes continue to write version snapshots.
- Room page/count tag predicates were aligned to case-insensitive substring matching to preserve the previous Library filter semantics. Global tag-manager create/rename/delete UI and usage counts are restored using tag-specific Room queries; the unused legacy full-list Library composable was removed. Global tag-manager create/rename/delete and usage counts are restored through tag-specific Room queries.

## Verification

- `python3 tools/verify_v3_batch2_readiness.py` — PASS (static source-shape checks only).
- `python3 tools/verify_v3_paging_foundation.py` — PASS.
- `python3 tools/verify_backup_schema_15.py` — PASS.
- `python3 tools/verify_v2_source_integrity.py` — PASS, including its existing source-contract checks.
- Added a Room instrumentation assertion for partial, case-insensitive tag matching. It has **not** been executed in Android runtime here.
- No Gradle executable, Gradle wrapper, Android SDK or emulator is available in this environment. Kotlin/Compose/KSP compilation, unit tests, Room instrumentation, emulator and device validation are therefore NOT VERIFIED.

## Remaining before Batch 2 is complete

1. Add Compose/instrumentation coverage for rapid criteria changes, selection across page boundaries, selection after deletion, bulk operations, preset/tag count refresh, and paging empty/error/retry states.
2. Validate the actual screen and large datasets in GitHub Actions/emulator; run 10k/50k benchmarks and inspect query plans before adding indexes.

## Boundaries preserved

- App remains 2.1.24 / versionCode 28.
- Room schema remains 15; JSON backup schema remains 15; no migration was added.
- FSRS scheduler, backup serialization, signing identity and original `StudyForge_Plan.txt` were not changed.
- No APK was built or released. V2 CI, migration, backup/restore, signing and device acceptance gates remain separate release gates.
