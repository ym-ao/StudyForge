# Implementation status — StudyForge 2.1.2 source audit

**Current source version:** 2.1.2 / versionCode 7  
**Room database:** version 3 (migrations 1→2→3)  
**JSON backup:** schema 3 (imports schema 1, 2, and 3)  
**Audit date:** 2026-11-23  
**Verification boundary:** source inspection and static checks only. Android Gradle build, CI, on-device, migration runtime, and renderer tests are not yet verified.

## Status meanings

- **Implemented in source:** corresponding source exists; not proof that it compiles or works on-device.
- **Partial:** a useful subset exists, but it does not satisfy the complete original requirement.
- **Not implemented:** no corresponding feature was implemented in this release.
- **CI/device verified:** only use after actual successful build/test and device evidence.

## Version 1.0 — Core app

| Requirement | 2.1.2 source status | Notes |
|---|---|---|
| Question library and collections | Implemented in source | Room entities, DAO, collection cross-reference and UI remain present. |
| Rich question editor | Partial | Prompt, answer, explanation, metadata, options, tags, status, collections, LaTeX field, and Markdown-like preview are present. Full structured blocks, in-editor formatting toolbar, images/diagrams, tables, graphs, and more advanced content widgets remain incomplete. |
| LaTeX rendering | Implemented in source; device verification pending | `RaTeXView` native renderer integrated for the question equation field. Need verify actual APK packaging and offline rendering on Android. |
| Flashcards and MCQs | Implemented in source | Both types can be created and studied; MCQs support answer checking. |
| Tags and search | Partial / improved | Tags are stored and searchable; tag filter added. Dedicated tag manager and normalized tag model remain open. Search includes question, answer, explanation, subject, chapter, topic, and tags. |
| Library filters | Partial / improved | Type, status, subject, chapter, topic, tag, and collection filters added. Difficulty, exam, performance, saved filters/presets remain open. |
| Offline local database | Implemented in source; device test pending | Room local database; no remote service required for core use. |
| JSON import/export | Implemented in source; round-trip test pending | Schema 3 exports FSRS memory state/settings and accepts schema 1, 2, and 3. Older backup imports retain current FSRS values for existing matching question IDs; absent legacy settings are left unchanged. |
| Review history | Implemented in source | Existing review log data retained; practice mode does not record a review. |
| GitHub Actions APK workflow | Implemented in source; CI not verified | Java 17/Gradle 8.13 workflow runs unit tests, builds debug, validates signing key, and conditionally builds signed release. |

## Version 2.0 — Learning engine

| Requirement | 2.1.2 source status | Notes |
|---|---|---|
| FSRS spaced repetition | Implemented in source; test validation pending | `ReviewScheduler.kt` implements FSRS-6 default parameters, stability/difficulty state, whole local study-day elapsed intervals, retrievability, same-day update, recall/forget updates, retention-target interval calculation, and rating mapping. Old SM-2 interval/ease values are used as a safe initial seed for legacy cards rather than discarded. A standalone Kotlin smoke test passed for key scheduling behaviours, but this is not a JUnit/CI result. Not yet validated against official FSRS test vectors or on-device traces. |
| Due queue and ratings | Implemented in source | Again/Hard/Good/Easy, per-day caps for new and review cards, configurable ordering, and custom initial retry delay. Current retry setting uses the first valid numeric value; full multi-step sequence progression remains incomplete. |
| Study modes | Partial / improved | Due, All, Practice (no scheduler/review write), Random, Ordered, Topic-focused, Weakest-first, and basic Step-by-step answer/explanation reveal. Structured derivation steps, numerical mode, and custom session builder remain open. |
| Timed exams | Partial / improved | Configurable MCQ question count, duration, optional random order, mark-for-review, submit confirmation, score/results. Still MCQ-only; richer navigation grid, multiparts, partial credit and deeper exam configuration remain open. |
| Mistake tracking | Implemented in source | Existing mistake categories and review logs retained; exam wrong answers logged as a generic exam mistake. |
| Weak-topic analytics | Partial / improved | Accuracy, recent accuracy, topics, subjects, mistake categories from recorded activity; chapter/skill/time-series and broader analytics remain open. |
| Custom review settings | Implemented in source; persistence test pending | Desired retention, max interval, new/review daily caps, order, retry delays, exam count/duration/randomization stored in Room and exported to backup. |

## Version 3.0 — Advanced question engine

Not implemented in 2.1.2; remains reserved for deterministic dynamic numerical questions, variables/constraints, unit validation/tolerances, step-by-step numerical derivations, and validated distractors.

## Version 4.0 — AI and automation

Not implemented in 2.1.2; remains reserved for AI providers, secure server-side secret proxy, external publishing, validation pipeline, and optional dashboard.

## Version 5.0 — Cloud and expansion

Not implemented in 2.1.2; remains reserved for accounts, sync, multi-device, conflict resolution, sharing, and multi-user features.

## Data migration and compatibility audit

- Room current version is 3. Migration 1→2 is retained; migration 2→3 adds `fsrsStability`, `fsrsDifficulty`, and `study_settings`. No destructive fallback or table replacement was introduced.
- New FSRS question columns declare matching SQL defaults and migration defaults (`0.0`).
- JSON schema 3 adds FSRS state and settings. Import range remains 1..3. Schema 1/2 remain supported.
- The existing `StudyForge_Plan.txt` and five-major-version scope are retained unchanged.
- No source code change should be treated as proof of safe migration. Test upgrade from a populated Room-v2 database and backup round-trip before device rollout.

## Signing and CI

- The fixed non-secret alias remains `studyforge-release`.
- The workflow validates the existing private keystore before Gradle packaging.
- Required GitHub secrets remain `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, and `SIGNING_KEY_PASSWORD`.
- Never replace or regenerate the private key for this release; it is not included in this ZIP.
- CI, signed APK certificate, migration runtime, and device install/update are **not yet verified**.

## Remaining accepted V1/V2 work

1. Full structured rich editor, media, image/diagram blocks, tables, graphs, and editor toolbar.
2. Difficulty/exam/performance filters and saved filter presets; proper tag manager.
3. Multi-step learning and relearning progression and complete custom scheduler controls.
4. Structured step-by-step derivation blocks and custom study session builder.
5. Broader exam question types, navigation overview, partial credit/multipart results.
6. Migration, backup round-trip, FSRS reference-vector, scheduler edge-case, and exam instrumented tests.
7. GitHub Actions verification and real-device offline/update acceptance tests.

These are **unfinished requirements**, not removed scope. Version 3/4/5 remain as in `PROJECT_PLAN.md` and `StudyForge_Plan.txt`.

---

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
