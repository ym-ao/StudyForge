# StudyForge V3.0 — Final Android Acceptance Runbook

**Prepared:** 2026-11-26  
**Source baseline:** V3.0 Batch 8 database optimization  
**Purpose:** convert the source-ready V3.0 work into a verified Android acceptance result. This document does not claim that Android tests have already run.

## Release identity and compatibility contract

| Item | Required value |
|---|---|
| Application ID | `com.studyforge.app` |
| Version name / code | `2.1.24` / `28` |
| Room schema | `16` |
| JSON backup schema | `15` (imports 1–15) |
| Room upgrade | Additive `15 → 16`; retain migrations 1→2 through 14→15 |
| FSRS | No policy or parameter change in V3.0 batches |
| Signing | Reuse the existing release keystore and `studyforge-release` alias; never generate a replacement for acceptance |
| Original plan | `StudyForge_Plan.txt` must remain byte-for-byte unchanged |

## 1. Run the repository CI workflow

Push this exact source package to the intended repository branch, or run the existing **Build StudyForge APK** workflow manually with `workflow_dispatch`.

The workflow is expected to perform, in order:

1. Python migration, backup, source-integrity and V3 readiness checks.
2. Synthetic 10k/50k SQLite benchmark and query-plan regression gates.
3. `:app:lintDebug`.
4. `:app:testDebugUnitTest`.
5. API 35 emulator execution of `:app:connectedDebugAndroidTest`.
6. Debug APK build and artifact upload.
7. Signed release APK build only when the existing signing secrets are configured and validated.

Record the GitHub Actions run URL, commit SHA, job result and uploaded artifact names. A successful Python step alone is not a successful Android build.

## 2. Required Android/Room checks

- Confirm Kotlin, KSP, Compose, Room and Paging compile in the CI environment.
- Confirm the complete JUnit suite passes, including FSRS reference-invariant tests and Study queue tests.
- Confirm Room instrumentation tests pass on API 35.
- Exercise a **populated** schema-15 database upgrade to schema 16. Verify question, collection, membership, review-log, rich-content, multipart, settings, preset and exam-attempt records survive. Verify the two new question indexes exist and Due null-first order is preserved.
- Confirm no destructive migration path is used.
- Export a JSON backup before the upgrade and restore it after the upgrade. Also export after the upgrade and restore into a clean current-schema database. Verify backup schema 15 compatibility and record counts/content, not just successful parsing.

## 3. Physical-device acceptance

Use the user's actual Android device and record model, Android version, app build SHA-256 and dataset size. Test both a normal library and synthetic/imported libraries at 10k and 50k questions where practical.

| Area | Acceptance actions |
|---|---|
| Library | Search, all filters, saved filters/counts, sort order, page append, empty/error/retry states, selection across pages, bulk tag/status/difficulty/collection actions |
| Study | Due/New quotas, Oldest/Weakest/Random, topic and saved-session filters, queue progress, Again/Hard/Good/Easy transitions, process recreation and retry |
| Rich content | LaTeX, images, Cloze, hints, multipart/exam content; ensure only active/bounded content is hydrated |
| Home/Progress | Headline counts, accuracy, activity, streak, topic/type/subject/chapter analytics, recent history and exam aggregates |
| Collections/tags | Sparse and dense collection paging, membership changes, deleted/stale links, tag CRUD and counts |
| Backup | Export/import, media validation, restore, data integrity and schema-15 compatibility |
| UI | Narrow-screen navigation labels, rating buttons, scrolling, accessibility labels and no clipping |

Capture cold/warm query latency, peak memory, scroll frame jank and any ANRs/crashes. Host SQLite benchmark timings are not substitutes for device measurements.

## 4. Known query-plan caveats to evaluate

- The recent-review query still uses a temporary B-tree for the final `id DESC` tie-break sort after scanning the review timestamp index. Consider a composite `(reviewedAt DESC, id DESC)` index only if the actual Room query plan and device measurements justify the migration.
- Collection paging uses the ordered question index and indexed membership existence checks. Sparse collections may scan many question candidates; consider a membership-first strategy only if device profiling shows it is needed.
- The generic saved-session queue combines conditional ordering modes. The simple Due snapshot index result does not prove every saved-session/mode combination uses that index efficiently.

Do not add indexes or a new migration speculatively. Any further schema change requires a migration, populated-data preservation test, Room schema validation and backup compatibility check.

## 5. Release decision

V3.0 is accepted only after all CI, Android, populated migration, backup/restore and physical-device checks above have evidence recorded. If any step fails, file the failure with the exact workflow log/test/device details, fix it in source, and rerun the affected gates plus regression suite.

Do not bump version name/code, change FSRS, replace the signing key, or label an APK as release-approved merely because the source checks pass. The current package is a V3.0 development baseline on app identity 2.1.24/code 28.
