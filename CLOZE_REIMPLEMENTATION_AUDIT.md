# Cloze Reimplementation Audit — 2.1.24 V2 hotfix work

## Scope

This is a source-level remove-and-replace exercise for the Cloze implementation. The previous `ClozeRules` regex implementation was replaced in the isolated working copy; no user database, installed APK, signing material, FSRS code, or schema version was changed.

## Before vs. reimplemented

| Area | Previous implementation | Reimplemented behavior |
|---|---|---|
| Parsing | One regular expression extracting hidden text | Deterministic bounded scanner returning cloze index, answer, optional hint, and source range |
| Multiple deletions | Extracted answers and masked all matches | Preserves source order, exposes distinct indices, masks all or a selected cloze number |
| Hints | Regex could skip hint text but did not expose it | Parses `{{cN::answer::hint}}` and exposes the hint separately |
| Malformed syntax | No explicit validation at CSV import | Invalid, empty, zero-index, or unclosed markers are not accepted as valid Cloze content; malformed text is left unchanged by rendering |
| Import answer | CSV stored the supplied back-field verbatim | Cloze answer is derived from the cloze deletions, matching the editor save path; an empty CSV back-field is permitted for a valid Cloze row |
| Editor preview | Displayed raw `{{cN::...}}` notation | Displays the masked question preview |
| Study rendering | Re-ran parsing in the composable and displayed the stored answer | Caches the masked prompt and extracted answer for the active question; extracted cloze text is the revealed answer source |
| Collection persistence | Type-agnostic transaction; database errors could escape the launched coroutine | Stale collection IDs are filtered before link replacement; save failures are reported in a snackbar instead of terminating the UI coroutine |

## Regression coverage added

- `ClozeRulesTest`: multi-deletion mask/extract, per-index mask, hints, repeated indices, multiline answers, malformed markers, and oversized text.
- `QuestionInterchangeTest`: valid/malformed Cloze import, missing back-field handling, answer canonicalization, and CSV round-trip.
- `QuestionVersionRulesTest`: a Cloze question retains its markers/type/answer and collection IDs through a content-version snapshot restore.
- `StudyDatabaseRuntimeTest`: exercises Room persistence, question version creation, and adding a Cloze question to a collection using the same sequence as the app's collection flow.

## Checks performed in this environment

- Standalone Kotlin Cloze parser/rendering harness: **7 checks passed**.
- Standalone Kotlin CSV interchange harness: **4 checks passed**.
- Android Gradle unit tests and Room instrumentation tests: **not run here**; Gradle and the Android SDK are not installed in this environment.
- Physical-device crash reproduction: **not performed**; no Android bug report or stack trace has been received.

## What this audit does not prove

The reported phone crash may still be caused by another runtime path (for example, Android view rendering, a specific imported row, a Room transaction, or a device-specific exception). The source changes and tests narrow the Cloze behavior and add collection-path regression coverage, but only the CI emulator run and a real-device retest can establish whether the crash is resolved. If the app still crashes, collect a bug report/stack trace before making another speculative change.

## Release safety

- App version and versionCode unchanged.
- Room schema unchanged.
- Backup schema unchanged.
- FSRS implementation unchanged.
- Existing question records remain compatible; no migration or destructive data operation was added.
- This package is source code, not a built APK.
