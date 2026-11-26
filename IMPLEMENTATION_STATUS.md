## Latest V3.0 implementation — Batch 7 (2026-11-26)

- Source-implemented: per-active-question rich block hydration; per-active-exam-snapshot multipart part hydration; bounded Room query and Room instrumentation test source; synthetic 10k/50k SQLite benchmark; EXPLAIN plan reporting; CI/static gate 9/9.
- Host synthetic benchmark functional checks pass. Query plans show Library and Due queue scans plus collection-page scan; index/query optimization is not yet implemented.
- Not verified: Android build/KSP/Compose, JUnit, Room instrumentation execution, CI run, memory/frame profiling, physical-device performance or V2 closeout.
- No APK or release; app 2.1.24/code 28, Room 15, JSON backup 15. FSRS, signing identity and original plan unchanged.

---

## Latest FSRS-6 implementation — 2026-11-25

- Pinned py-fsrs v6.0.0 compatibility correction is source-implemented in `ReviewScheduler.kt`.
- Exact pinned `test_memo_state` replay passes all seven compared memory-state/scheduler-state transitions; final S=49.447237416 and D=6.827105750.
- Additional corrections cover Hard step-zero delays, 24-hour elapsed-day semantics, ties-to-even interval rounding, and empty learning/relearning step configurations.
- A lightweight local Kotlin test runner compiled/executed the two scheduler test classes: 32 passed, 0 failed. This is not an Android Gradle/JUnit execution.
- Android Gradle/CI, Room runtime migration, real backup/restore, signed APK and physical-device acceptance remain pending. No release bump or APK.

---

Earlier FSRS mismatch notes below are historical and superseded by the latest status above.


## Latest continuation — 2026-11-25 bulk-selection consistency (FSRS excluded)

- Fixed a Library multi-select edge case: changing filters after selecting questions could leave hidden IDs selected, while bulk actions only applied to the visible subset. All four bulk action paths now resolve selected IDs against the complete current Library. “Select visible” still selects only the filtered view; IDs for deleted questions are ignored safely.
- Added `QuestionSelectionRules` and three JUnit regression cases for full-library selection, stable Library ordering, stale IDs and empty selection. Added `tools/verify_bulk_selection.py` to GitHub Actions and the current source-integrity gate.
- Static source contract checks pass. Android JUnit/Compose runtime remains unrun. FSRS source/tests untouched; app remains 2.1.24/versionCode 28, Room 15, backup 15. No schema, migration, signing, release or original plan change.

# Current continuation — 2026-11-25

| Area | Status | Evidence / boundary |
|---|---|---|
| JSON export consistency | Improved in source | All 12 persisted record groups are captured in one Room transaction; static contract verifier passes. Android runtime export remains untested. |
| Bulk Library tags | Implemented in source | Multi-select add/remove, normalized tag catalogue update, transactional pre-change version snapshots, and comma validation; standalone Kotlin rules/static contract pass. Android JUnit/UI remains untested. |
| Bulk collection membership | Implemented in source | Multi-select add/remove for one chosen collection, transactional membership updates, pre-change version snapshots and target-ID validation; static contract passes. Android UI/Room runtime remains untested. |
| Saved-filter live counts | Implemented in source | Each saved filter chip recalculates matching questions from current question and collection-link data; JUnit source and static contract added. This is not a separate dynamic-collection entity/background service. Android UI/JUnit remains untested. |
| Multipart editor guidance | Corrected | Removed stale text claiming partial-credit exam scoring was disconnected; exam flow already supports automatic marking and awarded-point adjustment. |
| CSV/Anki parser | Hardened in source | Strict quote placement and CRLF/multiline handling have standalone Kotlin smoke evidence; Android JUnit remains unrun. |
| FSRS-6 pinned v6.0.0 fixture parity | Passed in standalone source harness | Exact pinned `test_memo_state` per-transition state values match; 32 scheduler tests passed in a lightweight Kotlin runner. Android Gradle/JUnit execution remains pending. |
| Room historical runtime migrations | Outstanding | SQLite smoke checks 7→8 through 14→15 pass; Android Room runtime chain not executed. |
| Android backup/restore round trip | Outstanding | Static schema/media/snapshot contracts pass; actual Android export/import remains untested. |
| Android build, lint and JUnit | Outstanding | CI is configured, but no local Gradle/SDK and no Actions run evidence. |
| Signing and device acceptance | Outstanding | Existing signing identity preserved; signed APK and physical device not verified. |

## 2026-11-25 V2 preparation continuation

- CI workflow now runs the current 2.1.24 source verifier (the previous workflow incorrectly called the 2.1.23 verifier) and the new `tools/verify_v2_source_integrity.py` gate.
- The source-integrity gate checks version metadata, Room schema 15 and registered migrations 1→2 through 14→15, JSON schema-15 export/import compatibility, CI migration checks, artifact labels and signing contract. It is static validation only.
- Added same-day Hard stability-floor regression test source.
- Local source/migration/backup checks passed; Android JUnit and runtime tests were not executed.
- Remaining V2 gaps remain: full upstream FSRS-6 suite beyond the pinned fixture, Room runtime migration tests, app-level backup round-trip, editor/accessibility completion, broader bulk operations and smart collections, deeper analytics/time-spent, CI execution, signed release continuity and physical-device acceptance. Bulk tag add/remove is now source-implemented, but does not complete the broader bulk-edit requirement.

## 2.1.24 addition — FSRS-6 reference-equation correction

- Long-term stability now uses the card difficulty from before the current review; the difficulty transition is computed separately from that same prior state, matching the upstream FSRS-6 update order.
- Stability floors now use 0.001 rather than the previous 0.1 approximation in the FSRS memory-state math and interval/retrievability paths.
- Added four-rating long-term regression fixtures (Again/Hard/Good/Easy) and a minimum-stability case. Fixtures follow the py-fsrs v6.0.0 equations with default 21 weights and fuzzing disabled.
- These are focused reference-equation fixtures, not a claim that the complete upstream scheduler API or StudyForge learning-step policy is identical.
- Reference basis: pinned py-fsrs v6.0.0 source equations and the Open Spaced Repetition FSRS algorithm reference; expected values were calculated from those equations for deterministic fixtures.
- No Room or JSON backup schema change; schema 15 and imports 1–15 remain.

## 2.1.23 addition — learner-revealed hint blocks

- `HINT` is now an authorable structured block kind. Editor supports add/edit/reorder/delete; study view keeps its text concealed until the learner taps Reveal hint.
- Existing block IDs/text flow through current snapshot, backup and restore paths; no schema bump.
- Source implementation only; Compose runtime and accessibility behavior require device verification.

# Current source audit — StudyForge 2.1.24

**App:** 2.1.24 / versionCode 28  
**Room:** schema 15; additive migrations 1→2 through 14→15  
**JSON backup:** schema 15; import accepts 1–15  
**Audit basis:** uploaded 2.1.21 ZIP inspected before editing; actual Kotlin source, Gradle configuration, Room entities/DAO/migrations, tests, backup paths, manifest, workflow and project records reviewed. Original `StudyForge_Plan.txt` retained byte-for-byte.

## 2.1.19 additions (historical release record)

| Area | Status | Boundary |
|---|---|---|
| Question provenance | Implemented in source | `sourceTitle` and `sourceDetail` editable, searchable, shown in study UI, and carried by CSV/JSON/version snapshots; source-specific and date filters were added in 2.1.20. |
| CSV/Anki preview | Implemented in source | Preview classifies new rows, stable-ID updates, duplicates and invalid rows; user confirms before import. |
| Duplicate detection | Implemented in source | Duplicate stable IDs and exact normalized prompt/answer pairs are skipped; this is conservative skip behavior, not a merge wizard. |
| Row diagnostics | Implemented in source | Missing fields, length, type/status/difficulty and MCQ/True-False option errors are reported by row number. |
| Data compatibility | Source + SQLite/static checks | Room 13 migration 12→13 adds provenance columns; JSON schema 13 retains imports 1–12. Runtime Room and Android backup round-trip remain pending. |

## 2.1.22 addition — first-review learning-step progression

- Corrected the first-review branch so a new card rated Good advances to configured learning step 1 when a second step exists.
- A new card rated Hard now enters Learning at step 0 rather than graduating directly to Review.
- Easy still graduates immediately; Good still graduates when no next learning step exists. Existing Again, repeat, restart and graduation behavior remains intact.
- This follows the configured-step transition model documented by the upstream `ts-fsrs` scheduler; StudyForge retains its explicit product rule that Hard repeats the current step rather than adopting the upstream Hard-interval calculation.
- Added focused JUnit source for new-card Good and Hard transitions. Android JUnit execution remains pending.
- No Room migration or JSON backup change; schemas remain 15.

## 2.1.21 addition — full content version diff

- Stored question snapshots are compared against the current question using stable IDs for block/part arrays.
- Differences include metadata, prompt/answer/explanation, source details, tags/options/LaTeX, rich blocks, multipart data and collection membership.
- Version history shows changed paths and before/current snippets, truncates long values, and never prints embedded Base64 image contents.
- No Room/backup schema change; existing snapshots are read in place.
- Actor attribution remains unavailable: version history stores timestamp/version, not an authenticated editor identity.

## Remaining V1/V2 feature work

- Broader FSRS-6 upstream golden/reference-vector coverage beyond the now-passing pinned `test_memo_state` fixture (equation invariants and one pinned fixture do not establish the entire upstream suite).
- Full visual version diff is implemented in 2.1.21; authenticated actor attribution remains unsupported in the local-only model.
- Rich editor refinements: hint authoring, inline/display LaTeX placement/copy, accessibility and responsive acceptance.
- Broader bulk editing and smart-collection workflows.
- Deeper skill analytics, progress-over-time and reliable time-spent measurement (2.1.20 adds question-type accuracy).
- Full Gradle/Compose/KSP/JUnit, Room runtime migration, Android backup/restore round-trip, CI, signing and device acceptance.

V3 numerical templates, variable constraints, unit conversion/validation and generated distractors remain outside the V1/V2 completion gate.

---

# Historical source audit — StudyForge 2.1.18

**App:** 2.1.18 / versionCode 22  
**Room:** schema 12; additive migrations 1→2→3→4→5→6→7→8→9→10→11→12  
**JSON backup:** schema 12; import accepts 1–12  
**Audit basis:** exact 2.1.16 ZIP source plus 2.1.18 changes inspected; original `StudyForge_Plan.txt` retained byte-for-byte.

## 2.1.18 additions

| Area | Status | Boundary |
|---|---|---|
| CSV interchange | Implemented in source | Simple fields only; no rich blocks, media, collections or review history. |
| Anki TSV interchange | Implemented in source | Front/Back/Tags; basic three-field text format, not full Anki package/media scheduling. |
| Question formats | Expanded in source | True/False, exact-match Numerical and Cloze added; dynamic numerical templates, unit validation and tolerance engine remain V3. |
| Bulk library editing | Implemented in source | Multi-select status and difficulty changes; richer bulk metadata/tag/delete operations remain open. |
| Manual scheduling | Implemented in source | Due now/tomorrow/seven days and scheduler reset; reset preserves review logs and lifetime counters. |
| Collection-aware sessions | Implemented in source | Saved session collection filter; collection deletion clears stale references. |
| Question version history | Implemented in source | Prior content snapshots, history list and restore; restoring content preserves current scheduler/review data. |
| Rich text formatting | Implemented in source | Bold, italic, underline, highlight, code, headings and bullets; multi-line heading/bullet; Android runtime/editor verification pending. |
| Room/backup | Source + SQLite/static checks | Room 12, migrations 10→11 and 11→12, JSON schema 12; Room runtime and Android round-trip pending. |

## Remaining V1/V2 feature work

- Full FSRS-6 upstream golden/reference-vector validation beyond the pinned `test_memo_state` fixture.
- CSV/Anki import preview, row-level diagnostics, richer field mapping and duplicate detection/merge policy.
- Source/provenance fields (textbook/document/page/generator), created/modified-date filters and source filters.
- Version-to-version change diff and audit attribution; current history lists snapshots and restores content.
- More complete question format support and numerical tolerance/unit handling (dynamic engine belongs to V3).
- Advanced editor/accessibility, full inline/display equation placement/copying and block-specific rich editing refinements.
- Broader bulk edit and smart collection workflows.
- Accuracy-by-question-type, deeper skill-level analytics, progress-over-time and reliable time-spent metrics.
- Full Gradle/Compose/KSP/JUnit, Room runtime, Android backup round-trip, CI, signing and device acceptance.

## Status meanings

- **Implemented in source:** corresponding code exists; not proof that it compiles or works correctly on-device.
- **Partial:** a useful subset exists, but it does not satisfy the complete original requirement.
- **Not implemented:** no corresponding implementation was added in this source release.
- **CI/device verified:** only use after actual successful build/test and device evidence.


## 2.1.15 current feature status

| Area | Status | Boundary |
|---|---|---|
| Persisted exam attempts | Implemented in source | Completed attempts store weighted score, timing, counts and self-contained question/part snapshots; not Room-runtime tested. |
| Attempt analytics/history | Implemented in source | Progress shows average/best/median score, score bands, a recent trend, count and ten recent attempts. |
| Review-history preservation | Designed into migration/import | Migration is additive; legacy backup import does not delete existing attempt/review rows. SQLite migration smoke test passed; app-level merge/restore not runtime-tested. |
| Backup compatibility | Implemented in source | Schema 10 exports/imports attempt records; schemas 1–9 remain accepted. Fixture/static checks passed; actual Android round-trip pending. |
| Database migration | Source + SQLite smoke test | 9→10 creates an indexed attempt table with no question FK; Room runtime migration pending. |
| CI/device acceptance | Not verified | Workflow updated, but no Actions run, signed APK, or device evidence exists yet. |

| V1/V2 area | 2.1.11 source status | Boundary
|---|---|---|
| Saved study sessions | Implemented in source | Editable subject/chapter/topic/tag/difficulty/exam/type/status/performance/mode/limit filters; collection filter not included.
| Structured derivations | Implemented in source | DERIVATION block format and stepwise reveal; equations are LaTeX strings, no symbolic algebra solver.
| Multipart numeric grading | Implemented in source | Accepted text alternatives and absolute/relative tolerance; all-or-nothing per part remains.
| Analytics | Expanded in source | 7-day activity, active review days, exam response accuracy, chapter mistakes; no persisted exam-attempt entity.
| FSRS validation | Partial tests added | Published mathematical invariants tested; full upstream golden vectors still pending.
| Room/backup validation | Partial | SQLite migration smoke scripts and schema-9 JSON static check passed; Room runtime and app-level round-trip pending.
| CI/device acceptance | Not verified | No GitHub Actions run, signed APK or device test available.

## 2.1.12 compile-fix evidence

- Duplicate `FontWeight` import removed; exactly one import remains.
- Editor error state declaration now precedes the image-picker callback assignment.
- Full Android compilation and JUnit remain pending.

## 2.1.11 verification evidence

- Standalone Kotlin harness passed for numeric absolute/relative tolerance, accepted alternatives, derivation parsing/validation and all saved-session criteria.
- `tools/verify_migration_7_8.py`: 7 SQLite checks passed.
- `tools/verify_migration_8_9.py`: existing question-part/session rows preserved and six added columns checked.
- `tools/verify_backup_schema_10.py`: schema-10 example backup parsed; attempt export/import compatibility guards checked.
- Kotlin parser-only scan found no syntax diagnostics, but unresolved Android/Compose/Room symbols prevent treating it as a compile.
- Original `StudyForge_Plan.txt` was not modified.

# Implementation status — StudyForge 2.1.10 source audit

**Current source version:** 2.1.10 / versionCode 15  
**Room database:** version 8 (migrations 1→2→3→4→5→6→7→8)  
**JSON backup:** schema 8 (imports schemas 1–8)  
**Audit date:** 2026-11-23  
**Verification boundary:** source inspection plus focused local source/SQLite/JSON checks described in `BUILD_STATUS.md`. Full Android Gradle build, JUnit execution, GitHub Actions, Room runtime validation, signed APK, and device installation/update remain unverified.

## Status meanings

- **Implemented in source:** corresponding code exists; not proof that it compiles or works correctly on-device.
- **Partial:** a useful subset exists, but it does not satisfy the complete original requirement.
- **Not implemented:** no corresponding implementation was added in this source release.
- **CI/device verified:** only use after actual successful build/test and device evidence.

## Version 1.0 — Core app

| Requirement | 2.1.8 source status | Notes |
|---|---|---|
| Question library and collections | Implemented in source | Room entities, DAO, collection cross-reference, library and collection UI remain present. |
| Rich question editor | Partial / improved | Prompt/answer/explanation, metadata, formatting starter shortcuts and LaTeX field exist. Ordered TEXT/HEADING/LIST/LATEX blocks can be authored, reordered, deleted and rendered in study; image/media, tables, graphs and selection-aware editing remain incomplete. |
| LaTeX rendering | Implemented in source; device verification pending | RaTeX renderer is integrated for the equation field. APK packaging and offline Android rendering still need verification. |
| Flashcards and MCQs | Implemented in source | Both types can be created and studied; MCQs support answer checking. |
| Tags and search | Partial / improved | Tags are stored/searchable/filterable; a tag catalogue and create/rename/delete manager are implemented in 2.1.6 source. Room runtime validation is pending. |
| Library filters | Implemented in source; runtime testing pending | Type, status, subject, chapter, topic, tag, collection, difficulty, exam and performance filters exist. Named presets can be saved, applied, updated by name, managed and deleted. |
| Offline local database | Implemented in source; device test pending | Room local database; no remote service is required for core use. |
| JSON import/export | Implemented in source; round-trip test pending | Schema 8 exports questions, collections, review logs, FSRS values, settings, learning-step state, saved filter presets, tag catalogue, content blocks, multipart parts and session presets. Import accepts schemas 1–8. |
| Review history | Implemented in source | Review logs persist ratings, correctness, mistake category, mode and scheduled due timestamp. |
| GitHub Actions APK workflow | Implemented in source; CI not verified | Workflow runs unit tests, builds debug, validates the existing signing key, and conditionally builds signed release. |

## Version 2.0 — Learning engine

| Requirement | 2.1.8 source status | Notes |
|---|---|---|
| FSRS spaced repetition | Pinned v6.0.0 fixture passes in standalone harness; broader conformance pending | `ReviewScheduler.kt` matches the pinned 21 weights and policies; 32 local Kotlin scheduler tests pass. Full upstream suite and Android Gradle/JUnit remain pending. |
| Due queue and ratings | Implemented in source | Again/Hard/Good/Easy, daily caps, order preferences, multi-step Learning/Relearning, and persisted step index. |
| Study modes | Partial / improved | Due, All, Practice, Random, Ordered, Topic-focused, Weakest-first and basic answer/explanation Step-by-step. Structured derivations and numerical mode remain open; basic saved sessions can be saved/applied/deleted, but full session criteria editing remains open. |
| Timed exams | Partial / improved | Configurable MCQ count/duration/random order, mark-for-review, submit confirmation, question navigation and score/difficulty breakdown. Multipart timed exam presentation, per-part answer capture and weighted all-or-nothing scoring are implemented in 2.1.10; advanced within-part partial credit remains open. |
| Mistake tracking | Implemented in source | Mistake categories and review logs retained; exam wrong answers are recorded as an exam mistake. |
| Weak-topic analytics | Partial / improved | Accuracy, recent accuracy, topics, subjects and mistake categories from recorded activity; deeper chapter/skill/time-series analytics remain open. |
| Custom review settings | Implemented in source; persistence tests pending | Desired retention, max interval, daily limits, order, multi-step learning/relearning delays and exam settings are stored locally and exported. |

## 2.1.6 additions (historical)

- Dedicated Library tag manager supports create, rename, delete, and question-usage counts.
- Normalized tag catalogue table added through Room migration 6→7; legacy `tagsCsv` on questions remains supported.
- Rename/delete update affected question tag strings transactionally with exact tag matching.
- JSON backup schema 7 includes tag catalogue; older schemas 1–6 remain importable and old question tags are rediscovered.
- Five pure Kotlin tag-helper JUnit tests added; not executed by Android JUnit in this environment.
- Full Gradle/KSP/Room build, migration runtime, backup round-trip, CI and device checks remain pending.

## 2.1.5 additions

| Feature | Source status | Notes |
|---|---|---|
| Saved filter preset data model | Implemented in source | `SavedFilterPresetEntity` stores each criterion explicitly, with stable ID, name and timestamps. |
| Save and update | Implemented in source | Save current search/filter state under a name; saving with a case-insensitive matching name updates the existing record ID. |
| Apply saved preset | Implemented in source | Library chips restore all search/filter values, including optional collection ID. |
| Manage/delete | Implemented in source | Manager lists saved presets; deletion requires confirmation. |
| Collection deletion behavior | Implemented in migration/schema | Foreign key uses `ON DELETE SET NULL`; the preset survives and no longer filters by the deleted collection. |
| Room migration | Implemented in source; SQLite smoke check passed, Room runtime pending | Migration 5→6 creates the preset table; migration 6→7 creates the tag catalogue and unique name index; prior migrations remain registered. |
| JSON backup | Implemented in source; JSON shape check passed, full round-trip pending | Schema 7 exports/imports saved presets and tag catalogue; schemas 1–6 remain supported. Full Android export/import round-trip remains pending. |
| Regression tests | Added in source; Android JUnit not run | Tests cover all-criteria mapping, defaults, name trimming, stable ID and timestamps on update. |


## 2.1.7 additions — source implementation only

| Capability | Status | Notes |
|---|---|---|
| Ordered rich-content block storage | Data model implemented; UI not implemented | `QuestionContentBlockEntity` supports kind, text, order, optional embedded Base64 media, MIME type and alt text. Current editor/study UI does not yet create or render these rows. |
| Multipart question storage | Data model implemented; authoring/grading not implemented | `QuestionPartEntity` stores ordered parts, prompt/answer/explanation/options and points. Existing exam flow does not grade parts or partial credit. |
| Saved custom-session storage | Data model implemented; UI/execution not implemented | `StudySessionPresetEntity` stores mode and filters plus question limit. No preset manager or session launcher is wired into the UI yet. |
| Room migration 7→8 | Implemented in source; SQLite smoke test passed | Creates three additive tables and indices. Room runtime/KSP migration validation remains pending. |
| JSON backup schema 8 | Implemented in source; full round trip pending | Exports/imports the three new models. Schemas 1–7 remain accepted. |

## 2.1.8 additions — user-facing workflow progress

| Capability | Status | Notes |
|---|---|---|
| Ordered text/heading/list/LaTeX blocks | Implemented in source | Editor supports create/edit/reorder/delete; study card renders these kinds. Image/media, tables, graphs and selection-aware formatting remain open. |
| Multipart part authoring | Partial | Prompt, answer, explanation and positive points can be authored. Exam presentation, answer capture and weighted per-part grading were added in 2.1.10; advanced within-part partial credit remains open. |
| Saved study-session presets | Partial / improved | Save/apply/delete works in source. Current UI captures mode, selected topic and question limit; full filter criteria controls remain open. |
| Validation and filtering helpers | Implemented in source; tests added | Pure Kotlin helpers normalize child ordering/ownership, validate part content/points and filter saved sessions. Android JUnit has not been run. |

## Remaining accepted V1/V2 work

1. Add image/media selection and offline rendering, plus table and graph block authoring/rendering and selection-aware formatting.
2. Add structured derivation authoring and a true step-by-step derivation study UI.
3. Connect multipart questions to exam presentation, answer capture, weighted partial-credit grading, and results review.
4. Expand saved session editing to subject/chapter/tag/difficulty/exam criteria and richer launch configuration.
5. Complete deeper analytics and review-recovery controls.
6. Validate FSRS against upstream reference vectors; add Room runtime migration, JSON backup round-trip, scheduler and exam-flow tests.
7. Verify GitHub Actions, signed APK identity, offline behavior, installation/update and backup restore on the Android device.

These are unfinished requirements, not removed scope. Version 3/4/5 remain as approved in `PROJECT_PLAN.md` and the original `StudyForge_Plan.txt`.

## Data safety and signing

- The existing question IDs, collection links, review history, FSRS memory state, settings, and signing identity remain compatibility requirements.
- Migrations 5→6 and 6→7 are additive: they create the saved preset table and tag catalogue; existing question, collection, review and settings rows are not rebuilt.
- Saved filter presets reference collections with `ON DELETE SET NULL`; they are not cascade-deleted with a collection.
- Schema 1–6 backup imports do not delete current presets or tags. Schema 6+ imports merge saved presets by ID; schema 7 imports merge tag catalogue records.
- The original `StudyForge_Plan.txt` is preserved byte-for-byte. The five major versions and all remaining requirements are retained.
- Fixed signing alias remains `studyforge-release`; required Actions secrets remain `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, and `SIGNING_KEY_PASSWORD`. No key material is included.

# Historical audit — 2.1.0 baseline (retained)

# Implementation status — source audit

**Audited source package:** StudyForge 2.1.0  
**App versionCode:** 5  
**Audit date:** 2026-11-23  
**Scope:** Static comparison of the 2.1.0 source package against its 2.0.1 source baseline, including Kotlin source, Gradle files, tests, workflow, and project documentation. No Android Gradle build or device test was run during this audit.

## Status meanings

- **Implemented in source:** corresponding code exists in this ZIP. This does not by itself prove the feature builds or works correctly on-device.
- **Partial:** a limited version exists, but it does not meet the full approved requirement.
- **Not implemented:** no corresponding implementation was found in the inspected source.
- **Build/device verified:** only use this when a successful build/test or device result is actually available.

## Version 1.0 — Core app

| Planned capability | Source status | Audit notes |
|---|---|---|
| Question library | Implemented in source | Room `QuestionEntity`, DAO queries, local library UI. |
| Collections | Implemented in source | Separate collection entity and many-to-many `question_collection` cross-reference. |
| Rich question editor | Partial | Form fields include prompt, answer, explanation, subject/chapter/topic, type, options, LaTeX source, tags, status, and collection selection. It is not yet a full structured-block editor with headings, rich formatting, media, tables, and graphs. |
| LaTeX equation rendering | Not implemented | LaTeX is stored as a string and displayed as raw source/preview text; no MathJax/KaTeX renderer was found. |
| Flashcards | Implemented in source | Basic flashcard review screen exists. |
| MCQs | Implemented in source | Options and correct answer are stored; review/exam screens support MCQs. |
| Tags | Partial | Tags are stored and participate in search, but a dedicated tag-management/filter experience is not present. |
| Search | Implemented in source | Search covers prompt, subject, chapter, topic, and tags in DAO query. |
| Filters | Partial | Library UI has a question-type filter; full filters for subject, chapter, topic, tag, collection, difficulty, exam, status, and performance are not present. |
| Local database/offline foundation | Implemented in source | Room/SQLite database; no account or remote backend is required for current core features. Actual offline device behavior still needs testing. |
| JSON import/export | Implemented in source | Current code writes backup schema version 2 and accepts schema versions 1 and 2. Backup/restore round-trip still needs automated/runtime verification. |
| Basic review history | Partial / expanded in 2.0 | Review counters and individual `review_logs` exist in current database. |
| GitHub Actions APK workflow | Implemented in source | Workflow runs unit tests, assembles debug APK, and uploads an artifact. A successful 2.0.1 run has not been supplied/verified. |

**Version 1 conclusion:** A usable local foundation exists in source, but the Version 1.0 plan is not fully satisfied because proper LaTeX rendering and the full rich editor/filter requirements are missing. The source has not yet met the user's stated “first useful APK” acceptance checks through a verified CI build and device test.

## Version 2.0 — Learning engine

| Planned capability | Source status | Audit notes |
|---|---|---|
| FSRS spaced repetition | Not implemented | `ReviewScheduler.kt` contains an SM-2-inspired interval scheduler. Do not call it FSRS. |
| Review ratings / due queue | Partial / implemented foundation | Again/Hard/Good/Easy ratings and due timestamps exist, but the scheduling algorithm is not the planned FSRS. |
| Multiple study modes | Partial | Due, All, Practice, and Exam modes exist. The complete planned set (step-by-step, ordered/random/topic/weak-area/numerical/custom sessions) is not implemented. |
| Exam mode and timed tests | Partial | MCQ-only, up to 10 questions, fixed 15-minute time limit; not configurable. |
| Mistake tracking | Implemented in source | Categories include concept gap, wrong formula, calculation/algebra, unit conversion, sign/direction, misread question, forgotten fact, and guess/uncertainty. |
| Weak-topic analytics | Partial | Progress screen calculates basic accuracy, recent accuracy, topic-level incorrect counts, and mistake-category counts from recorded logs. It is not the full subject/chapter/skill analytics planned. |
| Custom review settings | Not implemented | New/review limits, configurable intervals, ordering preferences, and other settings are not exposed as a settings feature. |
| Review history | Implemented in source | `ReviewLogEntity` records timestamp, rating, correctness, category, mode, and scheduled due date. UI detail/analytics are basic. |

**Version 2 conclusion:** The current 2.0.0/2.0.1 source is a learning-engine foundation, not completion of the full Version 2.0 plan. FSRS and configurable review settings remain open.

## Version 3.0 — Advanced question engine

| Planned capability | Source status | Audit notes |
|---|---|---|
| Dynamic numerical templates | Not implemented | No variable/template engine found. |
| Automatic deterministic answer checking | Not implemented | No numerical expression evaluator/checker found. |
| Variable ranges and constraints | Not implemented | No template constraint model found. |
| Units, conversions, tolerances | Not implemented | No unit-aware validation system found. |
| Step-by-step derivations | Not implemented | No structured derivation-step engine found. |
| Automatic MCQ distractors | Not implemented | No rule-based distractor generation found. |

## Version 4.0 — AI and automation

| Planned capability | Source status | Audit notes |
|---|---|---|
| In-app AI generation | Not implemented | No AI provider integration found. |
| Multiple AI providers | Not implemented | No provider abstraction found. |
| External API publishing | Not implemented | No publishing API/backend found. |
| JSON and script workflows | Partial foundation | JSON backup exists, but this is not the full question interchange/publishing API or script workflow. |
| Validation pipeline | Not implemented | No draft/validate/approve/publish pipeline found. |
| Optional web dashboard | Not implemented | No dashboard source included. |
| Secret handling | Design requirement only | No AI keys are currently needed. When added, use a server-side proxy/secret store; never embed provider secrets in APK/source. |

## Version 5.0 — Cloud and expansion

| Planned capability | Source status | Audit notes |
|---|---|---|
| Optional accounts | Not implemented | Current app is local-only. |
| Cloud sync | Not implemented | No backend or sync worker found. |
| Multi-device support | Not implemented | Manual JSON backup is the current portability mechanism. |
| Conflict resolution | Not implemented | No sync conflict model found. |
| Advanced analytics | Partial foundation | Current progress screen is basic local analytics, not the planned advanced analytics. |
| Question sharing / multi-user expansion | Not implemented | No sharing or multi-user features found. |

## Build and test evidence

- Historical 2.1.0 source metadata was `versionName = 2.1.0`, `versionCode = 5` (corrected against the 2.1.0 changelog and build configuration).
- Room database version is `2`; migration `1 -> 2` is unchanged.
- JSON backup schema remains `2`; the existing import path accepts schema versions 1 and 2.
- `StudyDatabase.kt` retains the corrected `androidx.room.migration.Migration` import.
- Four unit tests from the 2.0.1 baseline remain in the source for scheduler intervals/relearning behavior.
- GitHub Actions now runs unit tests and builds a debug APK, then conditionally decodes the supplied release keystore and builds/uploads a signed release APK. The workflow fails if only some signing secrets are provided.
- The private keystore is kept separately; it is not part of this source ZIP.
- No successful GitHub Actions run for the 2.1.0 package has been supplied. Do not describe it as build-verified.
- No installation or update test on the Android device is documented.

## Remaining 2.1.0 completion work

The release-signing infrastructure has been added, but the originally planned learning-engine completion tasks remain: implement and test FSRS, configurable review/exam settings, true offline LaTeX rendering, fuller filters/study modes, and expanded Room/backup/exam regression coverage. These remain approved requirements; do not remove them from the roadmap.


## StudyForge 2.1.0 — release infrastructure audit

| Capability | Status | Notes |
|---|---|---|
| Version metadata | Implemented in source | `versionName = 2.1.0`, `versionCode = 5` (corrected historical record). |
| Release signing config | Implemented in source | Gradle reads the keystore path, alias, and passwords from environment variables; no credentials are hardcoded. |
| GitHub signed-release workflow | Implemented in source | Workflow runs unit tests and debug build; it conditionally builds and uploads a signed release APK when the four Actions secrets are configured. |
| Temporary keystore cleanup | Implemented in source | Workflow removes the decoded keystore from the runner at job end. |
| Keystore generated | Generated separately | The private keystore is delivered outside the source ZIP, with a separate credentials file and Base64 text file. |
| GitHub secrets configured | Not verified | The user must add the four secrets in repository settings. |
| CI build / APK signature | Not verified | Requires a real GitHub Actions run after adding the secrets. |
| Device update test | Not verified | Must be checked on the Android phone. The move from earlier debug APKs to the new release key may require a one-time uninstall/reinstall after JSON backup. |

**2.1.0 feature-scope note:** This package does not implement FSRS, configurable review/exam settings, true offline LaTeX rendering, or the complete filtering/rich editor requirements. They remain planned requirements and are not removed from the roadmap.


## StudyForge 2.1.1 signing workflow audit

| Capability | Status | Evidence |
|---|---|---|
| Fixed signing alias | Implemented in source | Workflow sets `SIGNING_KEY_ALIAS=studyforge-release`; no alias secret is read. |
| Keystore alias preflight | Implemented in workflow source | `keytool -list` validates the alias before `assembleRelease`. |
| Clear signing failure | Implemented in workflow source | Validation fails before Gradle packaging and emits a targeted message. |
| GitHub CI result | Not verified | Requires a real Actions run with the three signing secrets. |
| Device update | Not verified | Requires installing the successful release APK on the phone. |


## 2.1.8 additions — user-facing workflows implemented in source

| Capability | Status | Notes |
|---|---|---|
| Ordered text/heading/list/LaTeX blocks | Implemented in source | Editor can add, edit, reorder and delete blocks. Blocks are stored in existing schema-8 table and shown in the study card. Image/media selection, tables and graph editing remain outstanding. |
| Multipart part authoring | Partial | Editor can add, edit and delete part prompts, answers, explanations and positive point values. Part-aware exam presentation, answer capture and weighted partial-credit grading are not yet connected. |
| Saved study-session presets | Partial / improved | Study screen can save, launch and delete named presets. Current UI saves mode, selected topic and question limit; the underlying model supports additional criteria, but editor controls for subject/chapter/tag/difficulty/exam are still outstanding. |
| Authoring validation | Implemented in source | Pure helpers normalize block/part order and ownership, bound text/media fields, and reject blank part prompts/answers or invalid points. |
| Regression tests | Added in source | Added tests for part validation, block/part normalization and session preset filtering. Android JUnit execution remains pending. |

No Room migration or JSON schema change was needed: 2.1.8 connects the existing schema-8 structures to Compose UI and the existing backup paths.

## Remaining accepted V1/V2 work after 2.1.8

1. Image/media picker and offline image rendering, plus structured table and graph blocks.
2. Selection-aware rich formatting and a full editor experience across all question fields.
3. Connect multipart parts to exam presentation, answer capture, weighted partial-credit grading, and results review.
4. Expand session preset editing to all stored criteria and add richer launch configuration/custom session types.
5. Structured derivation authoring and step-by-step derivation execution.
6. Official FSRS reference-vector validation, Room runtime migration tests, backup round-trip tests, and broader scheduler/exam tests.
7. GitHub Actions verification, signed APK validation, and real-device offline/update acceptance.

These are outstanding, not removed requirements. Version 3/4/5 remain unchanged.


## 2.1.9 additions — requested image/media and table/graph milestones

| Requirement | 2.1.9 source status | Notes |
|---|---|---|
| Select image from Android document picker | Implemented in source | Uses OpenDocument with `image/*`; bytes are copied immediately into the existing content-block Base64 field. Device picker behavior pending. |
| Offline image rendering | Implemented in source | Decodes embedded bytes to bitmap in editor and study card; no remote URL or retained URI permission is needed. Per-image source limit 1.5 MB. Device verification pending. |
| Image alt text and MIME type | Implemented in source | Stored in existing `altText` and `mediaMimeType` fields. |
| Table content block | Implemented in source | Pipe-separated text editor and bounded preview; not a spreadsheet/grid editor. |
| Numeric graph content block | Implemented in source | Comma/semicolon/tab-separated x,y data and simple connected Canvas plot; no multiple series/axis labels/annotations. |
| Table/graph parser tests | Test source added | Five JUnit cases; not executed in Android Gradle test task. |
| Backup of image bytes | Existing schema-8 path reused | Existing content-block JSON export/import already includes mediaBase64, mediaMimeType and altText. Backup round-trip not executed. |

No Room migration or JSON schema bump was needed. The existing media columns and backup fields were already present in schema 8. These milestones are source-implemented only; Android compilation, CI and device verification remain pending.

## Remaining V1/V2 work after 2.1.9

Advanced multipart grading, full custom-session criteria editing, structured derivation authoring/execution, selection-aware rich formatting, broader analytics, official FSRS reference-vector validation, and full CI/device acceptance remain open. Do not remove these requirements.


## 2.1.10 — multipart exam progress

| Requirement | Status | Notes |
|---|---|---|
| Multipart exam selection | Implemented in source | Timed exam includes questions with saved parts as well as standard MCQs. |
| Multipart answer capture | Implemented in source | Parts with at least two saved options show selectable options; other parts use short-text entry. |
| Weighted per-part scoring | Implemented in source | Each part earns its configured points or zero on normalized exact match. No numeric tolerance or within-part partial credit yet. |
| Per-part results | Implemented in source | Shows response, expected answer, explanation, and earned/possible points. |
| Total weighted exam result | Implemented in source | Sums earned and possible points across selected questions. |
| JUnit regression tests | Added in source; not run by Android Gradle | Five cases; pure helper also exercised in a standalone Kotlin harness. |
| Full custom-session criteria editing | Not implemented | Remains outstanding. |
| Structured derivation authoring/execution | Not implemented | Remains outstanding. |
| Full Android/CI/device verification | Pending | Do not mark complete without actual evidence. |

### Grading limitations

Text answers are graded using case-insensitive exact matching after trimming and collapsing whitespace. Each part is all-or-nothing for its configured points. Numeric tolerances, aliases, manual marking, within-part partial credit and negative marking remain outstanding.


## 2.1.14 source delta

| Capability | Status | Evidence / limitation |
|---|---|---|
| Manual multipart partial credit | Implemented in source | Results screen accepts an examiner award per part; values are bounded by the part maximum. |
| Final review outcome after manual grading | Implemented in source | Review logs are written when grading is saved, using the adjusted score. |
| Regression coverage | Test source added | Tests cover overrides, clamping and invalid values; not executed by Android Gradle. |
| Persisted whole-exam attempt analytics | Not implemented | Still outstanding. |
| Upstream FSRS reference vectors | Not implemented | Existing equation-invariant tests are not a substitute. |
| Room runtime migration / backup round-trip | Pending | No runtime evidence supplied. |
| CI / signed APK / device acceptance | Pending | No current run or device evidence. |


## 2.1.14 current audit addendum

- App version 2.1.14 / versionCode 19; Room schema 9; JSON backup schema 9.
- Manual multipart partial credit is implemented in source through bounded examiner-awarded points on the results screen.
- Review logs are written after final grading is saved, so the adjusted score controls the recorded outcome.
- Added test source for manual score overrides, clamping and invalid award handling; tests have not been run through Android Gradle.
- Whole-exam attempts are not yet stored as independent persisted records. Existing exam analytics still derive from question-level review logs.
- Full upstream FSRS vectors, Room runtime migrations, app-level backup round-trip, CI, signing and device acceptance remain pending.


## 2.1.16 current source addendum

| Capability | Status | Boundary |
|---|---|---|
| Selection-aware formatting | Implemented in source | Bold, Italic, Heading, Bullet and Code operate on the active selection or insert a selected placeholder. Connected to question prompt, answer, explanation and multipart prompt/answer/explanation. Pure Kotlin harness passed five focused assertions; Android UI behavior unverified. |
| Exam analytics | Implemented in source | Progress shows median, four score bands and newest-five vs preceding-five trend when ten attempts exist. JUnit source added; Android JUnit not run. |
| FSRS-6 same-day checks | Expanded test source | Adds Good/Easy/Again short-term equation assertions. This is not the official upstream golden-vector suite. |
| Room / backup compatibility | Unchanged | Room schema 10, JSON schema 10, migrations 1→2 through 9→10 and import range 1–10 retained. |
| CI / signed APK / device | Not verified | No new Actions run, signed APK evidence, certificate comparison or device test. |

### Remaining V1/V2 work after 2.1.16

1. Expand upstream FSRS-6 golden-vector coverage beyond the pinned `test_memo_state` sequence for more edge cases and API/state-machine parity.
2. Room runtime migration tests across the complete supported migration chain.
3. Real Android JSON export/import round-trip tests, including media bytes, multipart data, session presets, review history and exam attempts.
4. Full Gradle/Compose/KSP/JUnit build and GitHub Actions verification.
5. Existing signing certificate continuity and physical-device install/update/offline acceptance.
6. Audit remaining original-blueprint V1/V2 acceptance criteria; do not mark the milestones complete until all are tested.


## 2.1.20 source addendum

| Capability | Status | Boundary |
|---|---|---|
| Source-title/reference filters | Implemented in source | Independent fields; persisted in saved presets and JSON backups. |
| Created/modified date ranges | Implemented in source | Inclusive local calendar dates in YYYY-MM-DD; invalid dates do not match. |
| Version comparison | Partial | Shows which core fields differ from the current saved question; not a character-level diff or full audit attribution. |
| Accuracy by question type | Implemented in source | Uses scored review logs only; groups deleted-question history separately. |
| Room/backup compatibility | Source implemented | Room schema 15, migrations 13→14 and 14→15; JSON schema 15 accepts 1–15. Runtime/round-trip not tested. |
| Android/CI/device acceptance | Pending | Full Gradle/JUnit, Room runtime, backup round-trip, CI, signing and device tests remain unverified. |


## Historical 2.1.20 status

See the 2.1.20 source addendum below for the current source implementation and verification boundary. The 2.1.19 table above is retained as historical release evidence.


## 2.1.23 — learner-revealed hint blocks

- Added `HINT` as a supported structured content-block kind. Authors can add, edit, reorder and delete hint blocks in the question editor; existing content-block storage, question snapshots and JSON backup paths are reused.
- During study, hint content is hidden behind a **Reveal hint** action and is displayed only after the learner chooses to reveal it. Hint reveal state is scoped to the current question in the active screen; it is not persisted as a review event.
- No Room migration or JSON backup schema change: both remain version 15. Existing backups and unknown/older block kinds retain their prior behavior.
- Added static release consistency checks and updated GitHub Actions artifact labels to 2.1.23.
- Verification in this package: Python source-consistency and migration/backup smoke scripts are run locally and recorded in `BUILD_STATUS.md`. Android Gradle/JUnit, Room runtime, CI, signing and device acceptance are not claimed unless separately evidenced.

## 2026-11-25 continuation — runtime Room test coverage

The source now includes Android instrumentation coverage for current Room schema creation, representative entity persistence across a close/reopen cycle, and question-dependent row cascades. GitHub Actions is configured to run these tests on an API 35 emulator. This is not yet a passed test result. Full historical migration runtime tests, app-level JSON backup/restore, CI run evidence, signing continuity and device acceptance remain outstanding.

## 2026-11-25 additional structured-editor validation

- Table blocks are now validated before save: at least one row, at most 30 rows, and at most 8 columns per row.
- Graph blocks are now validated before save: at least two data rows, at most 200 rows, and every nonblank row must contain exactly two finite numeric values. This prevents malformed rows from being silently omitted from the saved graph preview.
- Added unit-test source for valid and invalid table/graph content and editor save validation. These JUnit tests have not run locally because Gradle is unavailable.



## 2026-11-25 continuation — collection cleanup and Android lint gate

- Added an Android Room instrumentation case for collection deletion. It verifies collection-question links are removed, saved-filter preset collection references become null through the Room `SET_NULL` foreign key, and study-session preset references are explicitly cleared using the same cleanup call as the collection-delete UI.
- Added `:app:lintDebug` to GitHub Actions before unit and emulator tests so Compose/Android lint findings fail the workflow rather than being silently skipped.
- Extended the static source-integrity verifier to require the new runtime test and lint workflow step.
- These additions are source-only until GitHub Actions executes. No Android build, lint, unit-test or emulator result is claimed locally.


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


## 2026-11-25 continued source audit — FSRS mismatch reproduced

- Confirmed current source metadata and migration/backup compatibility directly from Gradle, Room and JSON import/export code: app 2.1.24/versionCode 28, Room 15, JSON 15/imports 1–15, migrations 1→2 through 14→15 registered.
- Replayed the exact pinned py-fsrs v6.0.0 `test_memo_state` rating/date sequence against the exact `ReviewScheduler` object in a standalone Kotlin harness. StudyForge final state is S=53.626902917141386/D=6.34635796621067; upstream test expects rounded S=49.4472/D=6.8271. This remains a real, reproduced FSRS conformance failure.
- No scheduler code was changed because the cause has not been isolated. StudyForge's local-calendar day handling and explicit Hard-repeat-current-step behavior must be compared separately from upstream UTC timing and Hard-step scheduling.
- Corrected the stale Room/backup schema-10 technical baseline in `PROJECT_PLAN.md`; extended `verify_v2_source_integrity.py` to catch future drift. Original `StudyForge_Plan.txt` remains byte-for-byte unchanged.
- Static source, release, backup and SQLite migration checks passed locally. Android/JUnit, Room runtime, app backup/restore, CI, signing and device acceptance remain unverified.


## 2026-11-25 — saved study-session preset editing (FSRS excluded)

- Saved study-session rows now expose Edit. The dialog loads all persisted filter fields and current mode/topic; saving edits by stable preset ID, including when the user renames it. Save current resets the editor for a new preset.
- Static verifier: `tools/verify_session_preset_edit.py`, wired into GitHub Actions and `verify_v2_source_integrity.py`.
- Local static verification only; Android Compose/JUnit execution is not available in this environment. FSRS source/tests untouched; app/Room/backup versions, migrations, signing and original plan unchanged.


## 2026-11-25 rich-content runtime test-source extension (FSRS excluded)

- Added a current-schema Room instrumentation test that closes/reopens the database and verifies ordered TEXT/HINT/IMAGE/TABLE/GRAPH blocks, including the offline image Base64 payload, MIME type and alt text.
- Extended the source-integrity verifier to require the test and fixture coverage. Static verification passes locally.
- Boundary: test source is not execution evidence; Android instrumentation, full backup/restore, historical migration runtime, Compose rendering/accessibility and device behavior remain unverified. No app/schema/backup version change.

## 2026-11-25 FSRS-6 conformance diagnosis — pinned-reference mismatch identified

- Built and ran `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. It extracts and compiles the exact production `ReviewScheduler` object with minimal Room/entity stubs using `kotlinc`, then replays the pinned `py-fsrs` v6.0.0 `test_memo_state` fixture. In parallel, it runs a local reference port of the exact upstream implementation/defaults from commit `352c7a2`. No network or third-party Python package is required; `kotlinc` and a JRE are required.
- The reference port reproduces the upstream golden result exactly: final S=49.4472374165 and D=6.8271057501 (rounded S=49.4472 / D=6.8271). The exact StudyForge Kotlin source returns S=53.6269029171 and D=6.3463579662.
- Root cause confirmed: the comparison was mixing different default parameter vectors. Pinned py-fsrs v6.0.0 `fsrs/fsrs.py` has defaults `[0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2]`; StudyForge currently uses `[0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542]`. The vectors differ in all 21 positions. The first mismatch is already visible on the initial Again: upstream S/D=0.2172/7.0114; StudyForge=0.212/6.4133.
- Two additional pinned-v6.0.0 behavioral differences were found: (1) upstream v6.0.0 clamps initial Easy difficulty before using it as the mean-reversion target; StudyForge currently passes `clamp=false`; (2) upstream v6.0.0 short-term stability increase is clamped to >=1 for Good/Easy, not Hard, while StudyForge clamps every grade >= Hard. The Hard difference does not cause the `test_memo_state` fixture failure because that fixture contains no Hard ratings.
- This corrects an earlier investigation note that relied on the newer `main/fsrs/scheduler.py` formula and treated its unclamped mean-reversion target as the pinned v6.0.0 contract. The actual pinned commit's source is `fsrs/fsrs.py`, and its behavior is different. Do not use current-main source as a substitute for the pinned reference.
- No production FSRS code was changed. This task was diagnosis only. Before changing production, decide explicitly whether StudyForge's compatibility target is the exact py-fsrs v6.0.0 defaults/behavior or a newer FSRS-6 parameter baseline. If the target remains pinned v6.0.0, the defaults and the two behavioral differences above must be reconciled and regression-tested together.
- Source references: pinned upstream implementation `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/fsrs/fsrs.py`; pinned fixture `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`. Local diagnostic output was reproduced successfully on 2026-11-25. This is a standalone source harness, not Android Gradle/JUnit or emulator verification.


## 2026-11-25 — new-chat ZIP audit and local verification

- Opened and extracted the supplied `StudyForge-2.1.24-v2-ci-emulator-boot-fix.zip` (132 archive entries). Read the requested project documents, original `StudyForge_Plan.txt`, this handoff, current source-plan audit, build/development logs, signing instructions and third-party notices; inspected the current Kotlin/data/test tree, Gradle configuration, Room migration registration, backup paths, manifest and GitHub Actions workflow.
- Confirmed source metadata: app 2.1.24 / versionCode 28; Room schema 15; JSON backup schema 15; migrations 1→2 through 14→15 registered; backup imports schemas 1–15. No Gradle wrapper, Gradle executable or Android SDK is present in this execution environment.
- Re-ran 11 current verification scripts: release verifier, V2 source-integrity verifier, backup snapshot/schema/media checks, bulk collection/tag/selection checks, live smart-filter checks, saved-session editing checks, and the pinned FSRS diagnostic. All 11 passed. Also ran all eight current SQLite migration smoke scripts (7→8 through 14→15); all passed.
- The pinned py-fsrs v6.0.0 diagnostic compared every transition in `test_memo_state`; the final state matched S=49.447237416 / D=6.827105750. This is standalone scheduler-core verification, not Android Gradle/JUnit or full upstream-suite parity.
- Source-level CI wiring includes lint, unit tests, and API 35 Room instrumentation. The latest user-reported workflow failure was emulator boot timeout before `connectedDebugAndroidTest`; the workflow now enables KVM and allows 900 seconds. This change still requires a fresh GitHub Actions run.
- Remaining acceptance gates: successful GitHub Actions lint/build/JUnit/emulator run; actual Android JSON export/import round-trip; populated historical Room migration runtime coverage; signed APK certificate continuity using the existing external key; and physical-device update/offline/backup acceptance. Continue auditing approved V1/V2 requirements (advanced editor/accessibility, broader smart collections/bulk workflows, deeper analytics/time-spent reliability, and audit attribution). Do not start V3.
- No production source, app version/versionCode, Room/backup schema, signing material, or original blueprint was changed in this audit. No APK was built or released. The original `StudyForge_Plan.txt` remains unchanged.


# V3.0 implementation status — 2026-11-26

## Batch 1: Room Paging data foundation (source added; build pending)

- Added `androidx.paging:paging-runtime-ktx:3.3.6`, `paging-compose:3.3.6`, and Room Paging 2.8.5 integration.
- Added `QuestionLibraryRow`, a lightweight projection excluding answer/explanation/LaTeX/content-block/media payloads.
- Added parameterized Room `PagingSource` and matching count query for text and current Library criteria, with deterministic `updatedAt DESC, id ASC` order.
- Added `QuestionRepository`, `LibraryPageQuery`, `LibraryViewModel`, and `PagedQuestionRows` with stable keys and load/error/retry states.
- Added bounded Study queue ID snapshot API and ordered card hydration; FSRS scheduling logic is unchanged.
- Added Android Room instrumentation regressions for paging sort/filter/count and stable queue order after a due-date mutation.
- Room schema remains 15; backup schema remains 15; app remains 2.1.24/versionCode 28. No APK release.

## Batch 1 checkpoint limitations (superseded where noted by Batch 2)

- The active Library UI was still on `Flow<List<QuestionEntity>>` at the end of Batch 1; this was migrated in Batch 2 below.
- Study UI has not yet adopted the snapshot queue API.
- Other database aggregate queries, index/query-plan analysis, 10k/50k benchmarks, and process-recreation queue persistence remain.
- Android Gradle/KSP compile, Room runtime instrumentation, emulator CI and device testing have not run in this environment.


## V3.0 Batch 2 — Production Library Paging integration (2026-11-26)

- Active Library route now uses `PagedLibraryScreen` + `LibraryViewModel` + `collectAsLazyPagingItems()`; the screen no longer receives a full `List<QuestionEntity>`.
- Active Library filtering and saved-preset counts are database-backed. Stable ID selection spans pages; bulk status/difficulty/tag/collection operations hydrate only selected IDs in safe chunks and retain transaction/version-snapshot behavior.
- Edit/delete/scheduling resolve current records by ID on demand. Lightweight paged rows do not include answer/explanation/LaTeX/media payloads.
- Static readiness, paging foundation, schema-15 backup and V2 source-integrity checks pass locally. Added Room instrumentation coverage for case-insensitive substring filter matching, tag lookup/counts and stable page order; not executed.
- Remaining: Compose/instrumentation tests for paging and cross-page selection, 10k/50k performance/query-plan validation, and migration of Study to bounded queue snapshots.
- Root `StudyForgeApp` still observes all questions for Home/Study/Progress and related flows; the Library screen no longer consumes that list, but the app-wide full-list observation is not yet eliminated.
- No Gradle/Android SDK: Android compilation, KSP, Room instrumentation, emulator, CI and phone validation are NOT VERIFIED. App 2.1.24/code 28, Room 15, backup 15, FSRS and signing remain unchanged; no APK.


## V3.0 Batch 3 — bounded Study queue contract (2026-11-26)

**Source status:** Implemented database/repository contract; not yet wired into the active Study UI.

- Added immutable `StudyQueueQuery` with validated supported modes and 1–500 session bounds.
- Added Room-side ID selection with preset criteria, collection membership, performance filters, mode ordering, and stable ID tie-break.
- Preserved on-demand hydration order and the legacy queue API.
- Added `StudyQueueQueryTest`, Room runtime coverage, and `tools/verify_v3_batch3_readiness.py`.
- No schema, backup, FSRS, version, or signing changes.

**Still open:** StudyScreen integration; exact Due daily quota and review-order parity; process recreation/resume policy; Home/Progress/Collections database aggregates; 10k/50k measured performance; Android build and emulator/device runtime validation.

## V3.0 Batch 4 — production Study queue migration (source implemented)

`StudyScreen` now consumes bounded Room-selected queue IDs and on-demand hydrated cards instead of the full question list. Due New/Review quotas are queried separately; saved-session filters and Exam eligibility are database-side; topic options are distinct-topic Room data. Active queue IDs/fingerprint use Compose saved state for rehydration when Android restores screen state. Static/source and standalone SQLite checks pass. Android compilation, Room instrumentation, CI, lifecycle/device validation and large-library benchmarks remain pending. See `V3_BATCH_4_STUDY_QUEUE_MIGRATION.md`.


## V3.0 Batch 5 — Home and Progress core aggregates (2026-11-26)

**Source status:** Implemented; Android runtime validation pending.

- Home dashboard question/collection/review/due totals are supplied by one compact Room aggregate projection rather than computed from `questions` in Compose.
- Progress headline counts and accuracy windows are supplied by a Room aggregate projection: question state counts, total/scored/correct reviews, 30-day scored accuracy, seven-day activity, and all-time distinct active review days.
- Added `dashboardAndProgressAggregatesAreComputedByRoom` instrumentation test source and `tools/verify_v3_batch5_readiness.py`; wired the verifier into CI and source integrity. Local static readiness: 10/10.
- Scope is intentionally partial: detailed Progress groupings (topic/type/subject/chapter/mistake categories), recent review prompt hydration, and exam analytics/history still consume full lists. Root question observation remains for editor, tag discovery, backup/export, and legacy consumers. These are explicit follow-on items, not claimed complete.
- Room schema 15, JSON backup schema 15, app 2.1.24/versionCode 28, FSRS, package/signing identity and original `StudyForge_Plan.txt` remain unchanged. No APK produced.
- Android Gradle/KSP compilation, JUnit, Room instrumentation, CI emulator, process/device behavior and 10k/50k performance remain unverified.
