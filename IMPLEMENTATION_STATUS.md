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

- Current source version metadata says `2.1.0`, `versionCode = 5`.
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
| Version metadata | Implemented in source | `versionName = 2.1.0`, `versionCode = 5`. |
| Release signing config | Implemented in source | Gradle reads the keystore path, alias, and passwords from environment variables; no credentials are hardcoded. |
| GitHub signed-release workflow | Implemented in source | Workflow runs unit tests and debug build; it conditionally builds and uploads a signed release APK when the four Actions secrets are configured. |
| Temporary keystore cleanup | Implemented in source | Workflow removes the decoded keystore from the runner at job end. |
| Keystore generated | Generated separately | The private keystore is delivered outside the source ZIP, with a separate credentials file and Base64 text file. |
| GitHub secrets configured | Not verified | The user must add the four secrets in repository settings. |
| CI build / APK signature | Not verified | Requires a real GitHub Actions run after adding the secrets. |
| Device update test | Not verified | Must be checked on the Android phone. The move from earlier debug APKs to the new release key may require a one-time uninstall/reinstall after JSON backup. |

**2.1.0 feature-scope note:** This package does not implement FSRS, configurable review/exam settings, true offline LaTeX rendering, or the complete filtering/rich editor requirements. They remain planned requirements and are not removed from the roadmap.
