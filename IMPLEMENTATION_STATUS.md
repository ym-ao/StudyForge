# Current source audit — StudyForge 2.1.11

**App:** 2.1.11 / versionCode 16  
**Room:** schema 9; additive migrations 1→2→3→4→5→6→7→8→9  
**JSON backup:** schema 9; import accepts 1–9  
**Audit basis:** uploaded 2.1.10 source ZIP inspected, then source changes audited.

| V1/V2 area | 2.1.11 source status | Boundary
|---|---|---|
| Saved study sessions | Implemented in source | Editable subject/chapter/topic/tag/difficulty/exam/type/status/performance/mode/limit filters; collection filter not included.
| Structured derivations | Implemented in source | DERIVATION block format and stepwise reveal; equations are LaTeX strings, no symbolic algebra solver.
| Multipart numeric grading | Implemented in source | Accepted text alternatives and absolute/relative tolerance; all-or-nothing per part remains.
| Analytics | Expanded in source | 7-day activity, active review days, exam response accuracy, chapter mistakes; no persisted exam-attempt entity.
| FSRS validation | Partial tests added | Published mathematical invariants tested; full upstream golden vectors still pending.
| Room/backup validation | Partial | SQLite migration smoke scripts and schema-9 JSON static check passed; Room runtime and app-level round-trip pending.
| CI/device acceptance | Not verified | No GitHub Actions run, signed APK or device test available.

## 2.1.11 verification evidence

- Standalone Kotlin harness passed for numeric absolute/relative tolerance, accepted alternatives, derivation parsing/validation and all saved-session criteria.
- `tools/verify_migration_7_8.py`: 7 SQLite checks passed.
- `tools/verify_migration_8_9.py`: existing question-part/session rows preserved and six added columns checked.
- `tools/verify_backup_schema_9.py`: schema-9 example backup parsed and optional new fields checked.
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
| FSRS spaced repetition | Implemented in source; conformance validation pending | `ReviewScheduler.kt` contains FSRS-6 default parameters and state updates. Official reference-vector validation and CI tests remain pending. |
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
