# StudyForge V3.0 — Physical-device acceptance checklist

**Prepared:** 2026-11-26  
**Baseline:** app `2.1.24` / versionCode `28`; Room `16`; JSON backup `15`; FSRS unchanged.  
**Purpose:** record the remaining user-device acceptance evidence after the user reported that the GitHub Actions build succeeded. This checklist is not itself evidence that any phone test has passed.

## A. Capture the build being tested

- [ ] Record the GitHub Actions run URL.
- [ ] Record the exact commit SHA.
- [ ] Download the `StudyForge-2.1.24-debug-apk` artifact from that run.
- [ ] Record the APK SHA-256 (for example, `sha256sum app-debug.apk`).
- [ ] Confirm the installed package is `com.studyforge.app`, version `2.1.24`, code `28`.
- [ ] Record phone model and Android version.

Do not install an APK from an older run or an unknown commit. If Android reports a signing conflict or downgrade, stop; do not uninstall the existing app or clear its data to force installation.

## B. Protect existing data first

- [ ] Open the currently installed StudyForge app and confirm the existing library is visible.
- [ ] Export a fresh JSON backup using the app's normal backup flow.
- [ ] Copy the backup off the phone (and preserve any separately stored media files if the export flow requires them).
- [ ] Record approximate question, collection, review-log and exam-attempt counts.
- [ ] Confirm the backup file exists and has a plausible non-zero size.

Never perform migration or restore experiments against the only copy of important study data. Use a duplicate/test dataset where possible.

## C. Install and basic launch

- [ ] Install/update the debug APK over the existing app without uninstalling or clearing app data.
- [ ] Launch the app; confirm no startup crash, blank screen or database-open error.
- [ ] Navigate between Home, Library, Study and Progress.
- [ ] Confirm existing questions, collections and settings remain present.

## D. Library and collection acceptance

- [ ] Search by a known question phrase.
- [ ] Apply and clear subject/type/status/difficulty filters.
- [ ] Change sort order and verify stable ordering when paging.
- [ ] Scroll through multiple pages; confirm no duplicate or missing rows.
- [ ] Open a question, edit it, save, and confirm the change persists after leaving/reopening.
- [ ] Create and delete a test question (only in a safe test dataset).
- [ ] Open a collection, page through its contents, and verify membership changes.
- [ ] Test empty results and recovery from an invalid/cleared filter.

## E. Study, scheduling and content

- [ ] Start a study session with Due/New quotas and verify queue counts/progress.
- [ ] Exercise Again, Hard, Good and Easy on disposable/test cards; verify the next-card behavior and persisted review history.
- [ ] Leave and resume/recreate a session; verify progress is coherent.
- [ ] Test Cloze reveal and answer interaction.
- [ ] Test LaTeX, hints, images and multipart content where available.
- [ ] Confirm no crash when opening, answering or leaving these content types.

Do not use valuable real cards for destructive scheduling experiments; use a disposable test deck/question set.

## F. Backup/restore and schema compatibility

- [ ] Preserve the pre-install schema-15 JSON backup from section B.
- [ ] Confirm the upgraded app opens the existing database and existing records remain intact.
- [ ] Export a second backup from the upgraded app.
- [ ] Restore the pre-upgrade schema-15 backup into a separate clean test installation/profile if available.
- [ ] Compare record counts and spot-check questions, collections, review logs, rich content, multipart content, settings and exam attempts.
- [ ] Confirm media references/files resolve after restore, where applicable.

**Migration evidence limitation:** installing the updated app over an existing schema-15 installation exercises the real on-device 15→16 migration only if that installed database was actually created at schema 15. Record the prior app build/schema provenance. Do not infer migration coverage from an install onto an already-schema-16 database.

## G. Performance and visual acceptance

Where practical, repeat with a representative large test library (10k, then 50k questions):

- [ ] Record approximate dataset size.
- [ ] Measure cold and warm Library first-page load.
- [ ] Measure filter changes and collection paging.
- [ ] Scroll for at least one minute; note visible jank, blank rows or repeated rows.
- [ ] Observe memory pressure, ANRs and crashes.
- [ ] Check narrow-screen navigation labels, rating buttons, bottom content padding and text clipping.

Host SQLite benchmark numbers are not a substitute for these Android measurements.

## H. Results to report

| Evidence | Result / value |
|---|---|
| Actions run URL | Pending |
| Commit SHA | Pending |
| APK SHA-256 | Pending |
| Phone model / Android version | Pending |
| Previous installed app build / DB schema | Pending |
| Dataset size | Pending |
| Backup exported and copied off-device | Pending |
| Install / launch | Pending |
| Library / collections | Pending |
| Study / FSRS / Cloze | Pending |
| Schema 15→16 migration evidence | Pending |
| Backup restore / record comparison | Pending |
| Performance / UI findings | Pending |
| Crashes / ANRs | Pending |

## Acceptance decision

V3.0 is not declared accepted by a successful CI run alone. Close this checklist with evidence for the actual APK, populated migration, backup/restore and physical-device workflows. Record any user-approved deferrals explicitly. Do not begin dependent V3.1 numerical-template work until the V3.0 gates are complete or explicitly deferred.
