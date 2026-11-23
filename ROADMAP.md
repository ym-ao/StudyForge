# StudyForge Roadmap

This is the live implementation roadmap. The five major versions below are the approved product plan. Smaller patch/minor releases may be used inside a major version.

## Current release snapshot

- **Current source version:** 2.1.11
- **versionCode:** 16
- **Room database version:** 9
- **JSON backup schema:** 9 (import accepts versions 1–9)
- **Build status:** Not verified for this source ZIP; CI must pass after the signing secrets are configured.
- **Device status:** No confirmed install/update test recorded for this source ZIP.

## 2.1.10 progress note (historical)

- Added multipart timed exam selection and per-part answer capture for questions with saved parts.
- Parts with option JSON use selectable choices; other parts use short-text input.
- Added weighted per-part exact-match scoring and per-part result feedback, plus total weighted exam percentage.
- Added five grading test cases; standalone helper smoke checks passed. Full Android JUnit, Gradle, CI and device testing remain pending.
- Room schema 8 and JSON backup schema 8 are unchanged.

## 2.1.9 progress note

- Added image/media picker and offline rendering using existing embedded Base64 media fields; source limit 1.5 MB per image.
- Added TABLE block editing/preview with pipe-separated cells and GRAPH block editing/preview with numeric x,y pairs.
- Added five parser test cases. They are source-only until Android JUnit is run.
- No Room/backup schema change: both remain version 8. Android build, CI, backup round-trip and device verification pending.
- These features complete the requested image/media and table/graph milestones in source only; all other outstanding V1/V2 requirements remain.

## 2.1.8 progress note\n\n- Connected schema-8 ordered content blocks to editor CRUD for text, heading, list and LaTeX; blocks are displayed in the study card. Media, tables and graph editing remain open.\n- Added multipart part authoring for prompt, answer, explanation and weighted points. Exam-time part navigation and partial-credit grading remain open.\n- Added saved study-session preset save/apply/delete UI. The current UI stores mode, topic and question limit; full criteria editing remains open.\n- Added pure validation/normalization helpers and unit test source. Android JUnit, Gradle, CI and device verification remain pending.\n\n## 2.1.7 progress note

- Added Room data structures for ordered rich-content blocks, multipart question parts with point values, and saved study-session presets.
- Added additive migration 7→8 and JSON schema 8 export/import.
- These are persistence foundations only. Rich editor UI, media picker/rendering, multipart authoring and partial-credit grading, and custom-session manager/launcher remain open. Do not mark them complete based on the data tables alone.
- Full Android build, Room runtime migration, backup round-trip, CI and device tests remain pending.

## Major version roadmap

### Version 1.0 — Core app

**Source foundation exists; milestone is not fully acceptance-complete.**

- [x] Local question library and Room persistence.
- [x] Collections with many-to-many question links.
- [x] Basic flashcards and MCQs.
- [x] Basic editor fields, tags, search, and type filter.
- [x] JSON backup/import with schema versioning.
- [x] GitHub Actions workflow source.
- [x] Optional release-signing configuration and conditional signed APK workflow (requires GitHub repository secrets).
- [~] Markdown-like rich text preview supports headings, lists, bold, italic, and inline code; full structured content blocks/media/tables/graphs remain open.
- [~] Native offline LaTeX rendering is integrated for the existing equation field; richer formula placement in all content blocks and device verification remain open.
- [~] Type, status, subject, chapter, topic, tag, collection, difficulty, exam, and performance filters implemented across 2.1.2/2.1.3; named saved filter presets are implemented in 2.1.5; dedicated tag manager is implemented in 2.1.6 source; Room migration and runtime verification remain pending.
- [ ] Successful CI build and installation/update acceptance test for a release candidate.

### Version 2.0 — Learning engine

**Foundation exists; milestone is not fully complete.**

- [~] FSRS-6 scheduler and default parameters implemented in source; CI tests and official reference-vector validation remain pending.
- [x] Again / Hard / Good / Easy ratings and due-date scheduling (scheduler implementation upgraded to FSRS-6 in 2.1.2).
- [x] Due, All, and Practice modes.
- [~] Timed MCQ exam mode; count, duration, and randomization are configurable in 2.1.2, but it remains MCQ-only.
- [x] Mistake categories and review logs.
- [x] Basic progress/weak-topic reporting.
- [~] Persistent desired-retention, max interval, new/review daily limits, review order, and multi-step Learning/Relearning sequences, ordered minute delays, persisted step index and current-step UI added in 2.1.4; CI, FSRS reference-vector and device validation pending.
- [~] Configurable exam count/duration/randomization, mark-for-review, and submit confirmation implemented; MCQ-only, navigation-grid and richer exam support remain open.
- [~] Random, ordered, topic-focused, weakest-first, and basic answer-then-explanation step-by-step modes implemented; structured derivations, numerical and custom session builders remain open.
- [~] Topic, subject and mistake-category analytics expanded from real review logs; chapter/skill analysis remains open.
- [ ] Expanded migration, scheduler, backup round-trip, and exam tests.

### Version 3.0 — Advanced question engine

**Not started in the supplied source.**

- [ ] Dynamic numerical questions and variable-based templates.
- [ ] Bounded variables, constraints, randomization, and reproducible variants.
- [ ] Deterministic answer calculation and automatic answer checking.
- [ ] Unit validation/conversions, answer tolerances, rounding, and significant figures.
- [ ] Step-by-step derivations with substituted values.
- [ ] Rule-based MCQ distractors with validation.

### Version 4.0 — AI and automation

**Not started in the supplied source.**

- [ ] In-app AI generation.
- [ ] Provider-independent integration and multiple provider/model support.
- [ ] Secure backend proxy/secret storage; no private keys in APK/source.
- [ ] JSON-first external publishing API and custom script workflows.
- [ ] Validation pipeline: generate, parse, validate, review, approve, publish.
- [ ] Draft/unverified default status and provenance/source tracking.
- [ ] Optional web dashboard.

### Version 5.0 — Cloud and expansion

**Not started in the supplied source.**

- [ ] Optional accounts; local-only mode remains available.
- [ ] Cloud backup and synchronization.
- [ ] Multi-device support and queued offline changes.
- [ ] Conflict detection/resolution; no silent overwrites.
- [ ] Advanced analytics.
- [ ] Question/collection sharing and multi-user expansion if desired.

## Historical 2.1.0 release state and remaining scope

The 2.1.0 source package added secure optional release signing, CI support for a signed release artifact, and signing/installation documentation. At that time the following V1/V2 work remained; 2.1.2 has since implemented some of it, with the 2.1.3 additions and current status shown above and in `IMPLEMENTATION_STATUS.md`:

1. FSRS implementation and scheduler tests.
2. User-configurable review limits, review order, and exam settings.
3. Real offline LaTeX rendering while preserving source text.
4. Better study filters and collection/topic-focused sessions.
5. Expanded automated tests for database migrations, backup/restore, scheduler edge cases, and exam flow.
6. GitHub Actions verification, followed by installation/update testing on the Android phone.

Avoid introducing Version 3/4/5 features early unless the user changes the approved plan.

## Release rules

1. Keep release history; append to `CHANGELOG.md`.
2. Update `PROJECT_PLAN.md`, this roadmap, `IMPLEMENTATION_STATUS.md`, `DEVELOPMENT_LOG.md`, and `BUILD_STATUS.md` for every release.
3. Increment `versionCode`; keep `versionName` and artifact names consistent.
4. Review Room migrations and JSON compatibility before data-model changes.
5. Add tests for new logic and regression cases.
6. Mark builds verified only after the actual GitHub Actions results pass.
7. Keep complete source and all project records in each handoff ZIP.


## 2.1.1 patch

- [x] Remove the alias GitHub secret dependency; use the documented non-secret alias `studyforge-release`.
- [x] Validate the alias against the decoded keystore with `keytool` before Gradle release packaging.
- [ ] Verify the 2.1.1 workflow in GitHub Actions.
- [ ] Verify installation/update behavior on the Android device.


## StudyForge 2.1.2 — V1/V2 feature release

- [x] Version metadata: 2.1.2 / versionCode 7; release artifacts renamed consistently.
- [x] Keep fixed non-secret signing alias `studyforge-release` and preflight validation from 2.1.1.
- [x] FSRS-6 scheduler source with 21 default parameters, stability/difficulty, retrievability, target-retention interval calculation.
- [x] Room version 2→3 migration adding FSRS memory columns and persisted study settings without destructive table replacement.
- [x] Persistent desired-retention, maximum interval, new/review daily caps, review order, retry delay, and exam settings.
- [x] Due, All, Practice, Random, Ordered, Topic, Weakest, and basic Step-by-step modes.
- [x] Configurable timed MCQ exam length/duration/randomization, mark-for-review, submit confirmation, and result review.
- [x] Expanded library filters: type, status, subject, chapter, topic, tag, collection; search includes answer/explanation.
- [x] Native LaTeX view integration and Markdown-like rich content preview.
- [x] JSON backup schema 3 includes FSRS values and settings; imports schema 1/2/3 and preserves existing FSRS values when importing an older backup.
- [x] Added FSRS unit tests; tests have not been run in this environment.
- [ ] Validate Gradle build, Room migration, all unit tests, signed APK and offline rendering in GitHub Actions/device.
- [ ] Complete remaining rich editor, advanced filters, multi-step scheduling, structured derivations, expanded exam analytics/navigation, and more regression/instrumented tests.


## StudyForge 2.1.3 — authoring, filters, and exam navigation

- [x] Version metadata: 2.1.3 / versionCode 8.
- [x] Editorial difficulty (Unrated/Easy/Medium/Hard) and exam/syllabus metadata on questions.
- [x] Formatting shortcut toolbars on prompt, answer, and explanation fields.
- [x] Library filters for difficulty, exam/syllabus, and recorded performance (Unattempted/Weak/Strong).
- [x] Pure filter predicate with focused JUnit regression tests.
- [x] Timed MCQ exam jump navigation with answered/marked/unanswered status.
- [x] Difficulty-based exam result summary.
- [x] Room migration 3→4; JSON backup schema 4 with import of schemas 1–4 and preservation of metadata when older backups are merged into existing questions.
- [ ] GitHub Actions unit tests and Android build.
- [ ] Runtime migration from populated Room-v3 database; JSON export/import round trip.
- [ ] Signed release APK and offline/device acceptance test.

## Remaining V1/V2 work after 2.1.3

1. Full structured editor with image/diagram attachments, tables, graphs, and selection-aware formatting.
2. Saved filter presets and a dedicated tag manager.
3. Complete multi-step FSRS learning/relearning progression and remaining scheduler controls.
4. Structured derivation steps and custom study-session builder.
5. Multi-part exams, richer question navigation, partial credit, and broader analytics.
6. FSRS reference-vector tests, migration runtime tests, backup round-trip tests, and exam-flow instrumented tests.
7. GitHub Actions and real-device verification.


## StudyForge 2.1.4 — multi-step learning/relearning

- [x] Version metadata: 2.1.4 / versionCode 9.
- [x] FSRS learning steps accept ordered comma-separated minute delays, including repeated values.
- [x] Again on a new card enters Learning at the first step; Again on a graduated card enters Relearning and records one lapse.
- [x] Good advances to the next configured step; Hard repeats the current step; Again restarts; Easy or final Good graduates to FSRS review.
- [x] Persist `learningStepIndex` in Room updates and visible study-card progress.
- [x] Room migration 4→5 adds `learningStepIndex` with default -1; prior migrations remain registered.
- [x] JSON backup schema 5 exports the learning step and imports schemas 1–5, preserving existing step position when merging older backups into matching IDs.
- [x] Added scheduler regression tests and an isolated Kotlin smoke harness (22 assertions passed).
- [ ] Run the real Android JUnit suite and Gradle builds in GitHub Actions.
- [ ] Validate all Room migrations with Room runtime and populated databases; verify JSON round-trip.
- [ ] Validate FSRS against official reference vectors and test the release on the Android device.

## Remaining V1/V2 work after 2.1.4

1. Full structured editor, media/diagram blocks, tables, graphs, and selection-aware editing.
2. Saved filters/presets and a dedicated tag manager.
3. Structured derivations and custom study-session builder.
4. Multipart exam questions, partial credit, additional question types and deeper analytics.
5. FSRS official-vector validation, Room runtime migrations, backup round-trip, exam integration tests, CI and physical-device acceptance.


## StudyForge 2.1.5 — saved library filter presets

- [x] Version metadata: 2.1.5 / versionCode 10; workflow artifact names aligned.
- [x] Named presets store the current query and all library filter criteria.
- [x] Apply presets from the library; saving with a matching name updates that preset; manage/delete includes confirmation.
- [x] Room schema 5→6 adds `saved_filter_presets` with a collection foreign key using `ON DELETE SET NULL` and an index.
- [x] JSON backup schema 6 exports/imports presets; imports schemas 1–6. Older backup schemas do not delete existing local presets.
- [x] Added `SavedFilterPresetTest` source for criteria round-trip, default fields and stable update identity.
- [ ] Run the real Android JUnit suite and Gradle debug/release builds in GitHub Actions.
- [ ] Validate Room 5→6 and full migration chain using Room runtime against a populated database; run a full JSON export/import round trip.
- [ ] Verify signing certificate, install/update and preset save/apply/delete behavior on the Android device.

## StudyForge 2.1.6 — tag manager

- [x] Dedicated Library tag manager UI for create, rename and delete.
- [x] Usage count for each tag based on current question tag strings.
- [x] Exact, case-insensitive rename/delete updates to affected questions in a database transaction.
- [x] Normalized Room tag catalogue with non-destructive migration 6→7.
- [x] JSON schema 7 exports/imports the tag catalogue; schemas 1–6 remain accepted.
- [x] Added pure Kotlin tag helper test source; Android JUnit execution remains pending.
- [ ] Full Room migration/runtime and backup round-trip tests.
- [ ] Full Gradle/CI, signed APK and device acceptance.

## Remaining V1/V2 work after 2.1.6

1. Full structured rich editor with selection-aware editing, media/image/diagram blocks, tables and graphs.
2. Structured derivations and custom study-session builder.
3. Multipart questions, partial credit, broader question types and deeper analytics.
4. Official FSRS reference vectors, full Room/backup tests, exam integration tests, CI and device acceptance.


## StudyForge 2.1.8 — structured authoring progress

- [x] App version 2.1.8 / versionCode 13; Room schema 8 and JSON backup schema 8 unchanged.
- [x] Create/edit/reorder/delete ordered TEXT, HEADING, LIST and LATEX content blocks in the question editor.
- [x] Render saved text/heading/list/LaTeX blocks in the study card.
- [x] Create/edit/delete multipart prompt, answer, explanation and positive point values.
- [~] Multipart authoring is present, but multipart exam flow and partial-credit scoring are not connected.
- [x] Save, apply and delete named study-session presets from the Study screen.
- [~] Session presets currently capture mode, selected topic and question limit; full subject/chapter/tag/difficulty/exam preset controls remain open.
- [x] Added pure Kotlin validation and session-filter helper test sources.
- [ ] Run Android JUnit, Gradle debug/release builds, Room runtime and JSON round-trip checks.
- [ ] Verify editor, study rendering, multipart authoring and saved sessions on the Android device.

### Remaining V1/V2 work

1. Media/image picker and offline rendering; table and graph block authoring/rendering.
2. Full selection-aware formatting and richer editor UX.
3. Multipart exam navigation, answer capture and weighted partial-credit grading.
4. Full custom session filter editing and richer launch settings.
5. Structured derivation authoring/execution.
6. Official FSRS reference vectors, Room/backup round-trip and exam-flow tests.
7. CI, signing, offline and device acceptance.


## StudyForge 2.1.10 — multipart timed exams

- [x] Version metadata 2.1.10 / versionCode 15; workflow artifact labels aligned.
- [x] Include authored multipart questions in timed exam selection.
- [x] Capture answers per part, using saved options where available or short text otherwise.
- [x] Weighted per-part scoring and per-part results feedback.
- [x] Total weighted score across an exam.
- [x] Add pure scoring helper and five regression test cases.
- [x] Reuse Room schema 8 and JSON schema 8; no migration required.
- [ ] Full Android Gradle/Compose/KSP and JUnit run.
- [ ] CI build, signing verification and physical-device exam testing.

### Next V1/V2 work

1. Full custom study-session criteria editor and reliable launch filtering (subject, chapter, topic, tag, difficulty, exam, mode and limit).
2. Structured derivation authoring and step-by-step execution.
3. Improved multipart grading: accepted-answer alternatives, numeric tolerances, manual marking and optional within-part partial credit.
4. Exam analytics and migration/backup round-trip tests; official FSRS reference-vector validation.
5. Full CI and physical-device acceptance.


## 2.1.11 progress note

- Saved study-session editor now exposes subject, chapter, topic, tag, difficulty, exam/syllabus, question type, status, performance and question limit. Saved Exam sessions apply their preset filters to the exam queue.
- Added DERIVATION block authoring in the existing ordered content-block table. Format: one line per step, `title | LaTeX equation | explanation`; validation enforces 1–40 steps and bounded fields. Editor/study view reveal one step at a time.
- Multipart parts now support accepted text alternatives and absolute/relative numeric tolerance. Grading remains all-or-nothing per part; manual marks and partial credit within a part remain open.
- Progress screen adds 7-day review activity, distinct review days, exam response/answer accuracy and chapter-level mistake counts, derived only from recorded review logs.
- FSRS-6 invariant tests cover R(S,S)=0.9, published Good initial stability and 90% interval behavior. This is not a full upstream golden-vector parity suite.
- Room migration 8→9 adds three grading columns and three saved-session criteria columns. JSON backup schema 9 exports/imports them; schemas 1–8 default missing fields safely.
- Local Kotlin harness, SQLite 7→8 and 8→9 smoke checks, backup JSON check passed. Full Android build/JUnit, Room runtime, backup round-trip, CI, signed APK and device checks remain pending.

## Remaining V1/V2 acceptance work after 2.1.11

- Manual examiner marking and optional within-part partial credit.
- Full exam attempt/session-level analytics with persisted exam attempt records (current exam analytics summarize per-question exam review logs).
- Full upstream FSRS-6 golden-vector parity tests, including rating transitions and short-term updates.
- Room migration tests through Android Room runtime and complete app backup/restore round-trip tests.
- GitHub Actions build, signed release verification and physical-device acceptance.
