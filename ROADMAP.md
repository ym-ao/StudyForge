## Latest FSRS-6 status — 2026-11-25

- [x] Match the pinned py-fsrs v6.0.0 `test_memo_state` fixture at every compared transition.
- [x] Align all 21 pinned defaults, mean-reversion target, same-day Hard behavior, Hard learning/relearning step delays, elapsed-day semantics and interval rounding.
- [x] Add regression fixtures for initial, same-day, long-term, Learning, Relearning, Hard midpoint/1.5x, and empty-step cases.
- [x] Standalone scheduler verification: 32 lightweight Kotlin tests passed; diagnostic policy probes passed.
- [ ] Run actual Android Gradle/JUnit tests and full upstream py-fsrs fixture corpus in CI; perform device acceptance.

The pinned fixture mismatch is no longer an outstanding source-level blocker. Full upstream-suite and Android runtime verification remain open.

---

Earlier FSRS mismatch notes below are historical and superseded by the latest status above.


## 2026-11-25 saved-session safety continuation (FSRS excluded)

- [x] Edit saved study-session criteria while retaining the original stable preset ID.
- [x] Block renaming a preset to another existing preset's name instead of overwriting the other record.
- [x] Confirm deletion and clear active selection only after the user confirms.
- [x] Add focused regression tests and CI source-contract checks.
- [ ] Verify Compose dialogs, rename conflict behavior, persistence and deletion on an Android device.

## 2026-11-25 non-FSRS V2 continuation

- Fixed Library multi-select so all four bulk actions apply to the complete selected set across filter changes. Added regression source and CI static validation.
- Remaining acceptance still includes real Android build/JUnit/Room/backup round-trip/CI/signing/device testing, plus distinct dynamic-collection workflows and editor/accessibility refinements. The pinned FSRS fixture now passes in a standalone Kotlin harness; full upstream-suite and Android runtime verification remain open.

# Roadmap — current status 2.1.24 (V2 preparation continuation)

## Latest source work — 2026-11-25

- Saved Library filter presets now show live match counts calculated from current questions and collection links. Added pure count helper, JUnit source, static verifier and CI wiring. This is not a separate dynamic-collection entity or background refresh service; Android JUnit/UI remains untested.

- Expanded Library bulk operations with transactional multi-select collection add/remove. Each changed question records the previous content and collection membership in version history; added a static verifier and CI gate. Android UI/runtime testing remains pending.

- JSON export now captures all 12 persisted record groups inside one Room transaction before serialization, preventing mixed-time snapshots during concurrent edits. Added a static verifier and CI gate; real Android export/restore remains untested.

- Source-vs-plan audit reproduced the pinned py-fsrs v6.0.0 `test_memo_state` mismatch (StudyForge S=53.6269/D=6.3464; upstream expects S=49.4472/D=6.8271). No speculative scheduler change was made; diagnose intermediate state transitions before claiming parity.
- Corrected `PROJECT_PLAN.md` technical metadata from stale Room/backup schema 10 to current schema 15 and added a CI static assertion for that baseline. Original `StudyForge_Plan.txt` remains unchanged.

- Fixed the stale workflow verifier reference: CI now invokes `verify_release_2_1_24.py`.
- Added a CI source-integrity gate covering version metadata, the complete registered Room migration chain, schema-15 JSON export/import compatibility, current artifact labels and signing configuration.
- Added a same-day Hard FSRS-6 regression test.
- Local static/migration/backup checks passed. This does not mean Android JUnit, Room runtime, GitHub Actions, signing or device acceptance passed.
- No release version bump or database/backup schema change.

## Remaining V2 completion work

1. Expand the now-passing pinned py-fsrs v6.0.0 fixture into broader upstream-suite coverage for edge cases and API/state-machine behavior; the current harness is a focused conformance set, not the entire upstream suite.
2. Add Android Room instrumentation tests that migrate populated databases through the complete supported migration chain and verify data integrity.
3. Add and run real app JSON export/import round-trip tests, including embedded media, review logs, collections, tags, saved presets, multipart content, exam attempts and question versions; include legacy backup fixtures.
4. Complete the original blueprint's outstanding editor/accessibility and richer content-block requirements.
5. Continue expanding bulk editing and smart-collection workflows beyond status/difficulty, tag add/remove and collection membership actions. Saved-filter presets now show live counts, but a distinct dynamic-collection model/management workflow is still not implemented.
6. Complete skill/progress-over-time analytics and reliable time-spent measurement; only attribute actors if a real authenticated identity model is introduced.
7. Run full Android Gradle/Compose/KSP/JUnit and GitHub Actions.
8. Verify the user's existing release certificate continuity and test update/offline/backup behavior on the physical Android device.

V3 remains not started until all V1/V2 requirements and acceptance gates are satisfied.

---

# Roadmap — current status 2.1.24

## Current release status

- **2.1.24 source implementation:** corrected FSRS-6 long-term review-state math to use pre-review difficulty and the published 0.001 stability floor; added multi-rating reference-equation regression vectors.
- **2.1.23 source implementation:** added authorable learner-revealed HINT content blocks using existing schema-15 storage.
- **2.1.22 source implementation:** first-review Good advances into the next configured learning step; first-review Hard starts Learning at step 0 when steps are configured. Easy and final Good graduate as before. StudyForge retains its specified Hard-repeat-current-step policy.
- **2.1.21 source implementation:** full snapshot-to-current content diff in question version history, including rich blocks, multipart data and collections; schema remains 15.
- **2.1.20 source implementation:** source/date-specific saved filters, core-field version comparison summaries and question-type accuracy analytics, building on 2.1.19 provenance and preview-first CSV/Anki imports.
- **Database compatibility:** Room schema 15; additive migrations 1→2 through 14→15 are registered. JSON backup schema 15; import accepts schemas 1–15.
- **Local verification:** see current `BUILD_STATUS.md`; do not infer Android build or runtime success from source checks.
- **Not verified:** full Gradle/Compose/KSP/JUnit build, Room runtime migrations, actual Android backup/restore, GitHub Actions, signed APK/certificate continuity and physical-device acceptance.

## Remaining V1/V2 work before final acceptance

1. Expand the now-passing pinned py-fsrs v6.0.0 `test_memo_state` fixture and focused scheduler regressions to cover the broader upstream test suite and edge-case API/state-machine behavior.
2. Add Room instrumentation migration tests across every supported schema, including schema 10→11→12→13→14→15.
3. Run actual Android JSON export/import round trips including question versions, media, collections, review logs, attempts and legacy backups.
4. Improve CSV/Anki field mapping and optional duplicate merge workflows; preview, row diagnostics and conservative duplicate skipping are now implemented. CSV/Anki do not carry all rich content.
5. Finish full question-format coverage and robust numerical tolerance/units; current single-question Numerical is exact-match and does not implement V3's dynamic templates/units engine.
6. Source/provenance title and reference detail fields are implemented; add authenticated actor attribution if a user identity model is introduced, accessible editing, full inline/display equation placement/copying, and block-specific graph/table editing refinements.
7. Expand bulk editing beyond status/difficulty and tag add/remove; add richer smart-collection workflows.
8. Extend question-type accuracy with advanced weak-skill analytics, time-spent measurement where reliable, progress-over-time views and review consistency summaries.
9. Run full Android build/JUnit, GitHub Actions, signing continuity and device acceptance.

## 2.1.19 progress note

- Added provenance title/reference fields to the question model, editor, search, study display, CSV, JSON schema-13 backup and version snapshots.
- Added import preview/confirmation for CSV and Anki TSV, including stable-ID update classification, duplicate detection and row-level diagnostics.
- Room migration 12→13 adds source fields with empty defaults; older backups remain importable.
- Android Gradle/JUnit, Room runtime, backup round-trip, CI, signing and device acceptance remain pending.

## 2.1.18 progress note

The source now covers more of the original V1/V2 blueprint, but source implementation is not acceptance. Keep V3/V4/V5 roadmap unchanged and do not mark V1/V2 complete until every original acceptance criterion and the testing gates pass.

---

## 2.1.16 progress note

- Selection-aware Bold/Italic/Heading/Bullet/Code actions are connected to prompt, answer, explanation, plain-text/heading/list blocks and multipart authoring fields.
- Progress adds median exam score, score-band distribution, and a latest-five vs previous-five score trend.
- Added test source for editor selection behavior, analytics calculations, and FSRS-6 same-day short-term equation behavior. A standalone formatting harness passed five assertions; Android JUnit remains unrun.
- No Room/backup schema change: Room 10 and JSON backup 10 remain current.
- Full Android build, JUnit, Room runtime, backup round-trip, CI, signed APK and device checks remain pending.

## Historical 2.1.14 release snapshot

- **Source version at that snapshot:** 2.1.14
- **versionCode:** 19
- **Room database version:** 9
- **JSON backup schema:** 9 (import accepts versions 1–9)
- **Build status:** Not verified for this source ZIP; CI must pass after the signing secrets are configured.
- **Device status:** No confirmed install/update test recorded for this source ZIP.

## 2.1.13 build-fix note (historical)

- Removed the duplicate Compose `FontWeight` import in `StudyContent.kt`.
- Moved the question-editor error state before the image-picker callback in `StudyForgeApp.kt`.
- App version 2.1.13 / versionCode 18; Room and JSON schema remain 9.
- Source-only correction; CI and device verification remain pending.

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

## 2.1.8 progress note

- Connected schema-8 ordered content blocks to editor CRUD for text, heading, list and LaTeX; blocks are displayed in the study card. Media, tables and graph editing remain open.\n- Added multipart part authoring for prompt, answer, explanation and weighted points. Exam-time part navigation and partial-credit grading remain open.\n- Added saved study-session preset save/apply/delete UI. The current UI stores mode, topic and question limit; full criteria editing remains open.\n- Added pure validation/normalization helpers and unit test source. Android JUnit, Gradle, CI and device verification remain pending.\n\n## 2.1.7 progress note

- Added Room data structures for ordered rich-content blocks, multipart question parts with point values, and saved study-session presets.
- Added additive migration 7→8 and JSON schema 8 export/import.
- These are persistence foundations only. Rich editor UI, media picker/rendering, multipart authoring and partial-credit grading, and custom-session manager/launcher remain open. Do not mark them complete based on the data tables alone.
- Full Android build, Room runtime migration, backup round-trip, CI and device tests remain pending.

## 2.1.19 progress note

- Added editable/searchable question source metadata (title and chapter/page/section/URL detail), preserved in CSV, JSON schema-13 backups and question-version snapshots.
- Added CSV/Anki TSV preview and explicit confirmation, with stable-ID update classification, duplicate skipping and row-level validation diagnostics.
- Room migration 12→13 adds two non-null source columns with empty defaults; all earlier migrations remain additive.
- Full Android build, JUnit, Room runtime, backup round-trip, CI, signing and device acceptance remain pending.

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
- [~] Question source/provenance fields (source title and reference details) are editable, searchable and exported; date/source-specific filters and provenance audit attribution remain open.
- [~] CSV/Anki import preview, duplicate detection and row-level diagnostics are source-implemented; merge wizard/field mapping refinements remain open.
- [~] Markdown-like rich text preview and selection-aware Bold/Italic/Heading/Bullet/Code formatting are source-implemented; structured blocks/media/tables/graphs exist, while richer block-specific authoring and runtime/device verification remain open.
- [~] Native offline LaTeX rendering is integrated for the existing equation field; richer formula placement in all content blocks and device verification remain open.
- [~] Type, status, subject, chapter, topic, tag, collection, difficulty, exam, and performance filters implemented across 2.1.2/2.1.3; named saved filter presets are implemented in 2.1.5; dedicated tag manager is implemented in 2.1.6 source; Room migration and runtime verification remain pending.
- [ ] Successful CI build and installation/update acceptance test for a release candidate.

### Version 2.0 — Learning engine

**Foundation exists; milestone is not fully complete.**

- [~] FSRS-6 scheduler and default parameters implemented in source; CI tests and official reference-vector validation remain pending.
- [x] Again / Hard / Good / Easy ratings and due-date scheduling (scheduler implementation upgraded to FSRS-6 in 2.1.2).
- [x] Due, All, and Practice modes.
- [~] Timed exams support standard MCQs and multipart questions, configurable count/duration/randomization, answer navigation, manual partial credit, persisted attempts and attempt history; richer question formats and full runtime acceptance remain open.
- [x] Mistake categories and review logs.
- [x] Basic progress/weak-topic reporting.
- [~] Persistent desired-retention, max interval, new/review daily limits, review order, and multi-step Learning/Relearning sequences, ordered minute delays, persisted step index and current-step UI added in 2.1.4; CI, FSRS reference-vector and device validation pending.
- [~] Configurable exam count/duration/randomization, mark-for-review, submit confirmation, multipart grading and persisted attempt history implemented; advanced exam types and full runtime acceptance remain open.
- [~] Random, ordered, topic-focused, weakest-first, and basic answer-then-explanation step-by-step modes implemented; structured derivations, numerical and custom session builders remain open.
- [~] Topic, subject and mistake-category analytics expanded from real review logs; question-type/skill analytics, progress-over-time and reliable time-spent metrics remain open.
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

- Manual examiner marking and optional within-part partial credit (implemented in source in 2.1.14).
- Full exam attempt/session-level analytics with persisted exam attempt records (current exam analytics summarize per-question exam review logs).
- Full upstream FSRS-6 golden-vector parity tests, including rating transitions and short-term updates.
- Room migration tests through Android Room runtime and complete app backup/restore round-trip tests.
- GitHub Actions build, signed release verification and physical-device acceptance.


## 2.1.14 — manual multipart partial-credit marking

- Added examiner-awarded points for each multipart answer on the exam-results screen.
- Automatic accepted-answer/numeric-tolerance grading remains the starting score; an examiner can adjust a part's award from zero to its configured maximum.
- The final adjusted total is used when recording the question's exam review outcome. Review history is recorded only after the user selects “Save grading and finish”.
- Added unit-test cases for manual overrides, bounds clamping, and invalid award fallback.
- No Room or JSON schema change. Whole-exam attempt history is still not persisted as a separate record.
- Android Gradle tests, CI, signed APK verification, and device acceptance are pending; source implementation only.


## 2.1.22 progress — V1/V2 continuation

- [x] Corrected first-review Good/Hard handling for configured multi-step Learning progression.
- [x] Added focused test source for the two first-review transitions.
- [ ] Full FSRS-6 upstream algorithm golden-vector parity remains open; this release fixes a learning-step transition and does not certify full FSRS conformance.
- [ ] Android Gradle/JUnit, Room runtime migrations, JSON round-trip, CI, signing and device acceptance remain pending.

V3 remains blocked until V1/V2 acceptance and verification gates pass.


## 2.1.20 progress — V1/V2 continuation

- [x] Source-title and source-reference library filters.
- [x] Inclusive created/modified date-range filters; saved presets and JSON backup support.
- [x] Core-field version comparison summary in version history.
- [x] Accuracy breakdown by question type from scored review records.
- [ ] Full visual diff, per-field audit attribution and version-history usability/accessibility review.
- [ ] Broader smart collections and bulk-edit actions.
- [ ] Official upstream FSRS-6 golden-vector parity.
- [ ] Android Gradle/Compose/KSP/JUnit, Room runtime migrations and full backup/restore round-trip.
- [ ] GitHub Actions build, signed APK/certificate continuity and physical-device acceptance.

V3 remains blocked until V1/V2 acceptance and verification gates pass.


## 2.1.21 progress — V1/V2 continuation

- [x] Full content comparison between historical snapshots and current questions, including stable-ID matching for blocks and parts.
- [x] Before/current values displayed with bounded output and embedded-media payload redaction.
- [ ] Authenticated editor/actor attribution; current offline history has no user identity model.
- [ ] Official FSRS-6 upstream conformance vectors, full Android build/JUnit, Room runtime migration, JSON round-trip, CI, signing and device acceptance.


## 2.1.23 — learner-revealed hint blocks

- Added `HINT` as a supported structured content-block kind. Authors can add, edit, reorder and delete hint blocks in the question editor; existing content-block storage, question snapshots and JSON backup paths are reused.
- During study, hint content is hidden behind a **Reveal hint** action and is displayed only after the learner chooses to reveal it. Hint reveal state is scoped to the current question in the active screen; it is not persisted as a review event.
- No Room migration or JSON backup schema change: both remain version 15. Existing backups and unknown/older block kinds retain their prior behavior.
- Added static release consistency checks and updated GitHub Actions artifact labels to 2.1.23.
- Verification in this package: Python source-consistency and migration/backup smoke scripts are run locally and recorded in `BUILD_STATUS.md`. Android Gradle/JUnit, Room runtime, CI, signing and device acceptance are not claimed unless separately evidenced.


## 2.1.24 continued verification — V1/V2 gates

- [x] Add initial-memory-state equation assertions for all four FSRS ratings using published default weights.
- [x] Record source-vs-plan audit and evidence boundaries.
- [x] Pin the py-fsrs v6.0.0 `test_memo_state` fixture with exact upstream version/commit and provenance; compare all state transitions.
- [ ] Expand to the full upstream-generated FSRS-6 golden-vector corpus, including additional short-term, long-term, state-transition and interval edge cases.
- [ ] Full Android Gradle/Compose/KSP/JUnit; Room runtime migration tests; actual Android backup/restore round-trip.
- [ ] GitHub Actions, signing certificate continuity and physical-device acceptance.
- [ ] Remaining approved original-blueprint V1/V2 workflows and accessibility/analytics requirements.

V3 remains blocked. These verification additions do not constitute a new app release or completion of V1/V2.

## V2 verification progress — 2026-11-25

- [x] Add Android Room current-schema persistence/cascade instrumentation test source.
- [x] Add Android emulator execution to CI workflow.
- [ ] Obtain successful emulator/Gradle CI evidence.
- [ ] Add populated historical Room migration runtime coverage across the full supported chain.
- [ ] Execute a real app-level JSON backup/restore round-trip on Android, including media and all schema-15 records.
- [ ] Complete signing-certificate continuity and physical-device update/offline acceptance.

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


## 2026-11-25 rich-content persistence test coverage (FSRS excluded)

- [x] Add current-schema Room instrumentation source for ordered TEXT/HINT/IMAGE/TABLE/GRAPH block persistence across database reopen, including embedded image metadata.
- [x] Require the test and its rich-block fixtures in the source-integrity gate.
- [ ] Execute the instrumentation test on the configured API 35 emulator in GitHub Actions and inspect the result.
- [ ] Separately complete actual JSON backup/restore round-trip and historical populated migration testing; this test does not cover either.

## 2026-11-25 FSRS-6 conformance diagnosis — pinned-reference mismatch identified

- Built and ran `tools/fsrs6_diagnostics/compare_py_fsrs_6_0_0.py`. It extracts and compiles the exact production `ReviewScheduler` object with minimal Room/entity stubs using `kotlinc`, then replays the pinned `py-fsrs` v6.0.0 `test_memo_state` fixture. In parallel, it runs a local reference port of the exact upstream implementation/defaults from commit `352c7a2`. No network or third-party Python package is required; `kotlinc` and a JRE are required.
- The reference port reproduces the upstream golden result exactly: final S=49.4472374165 and D=6.8271057501 (rounded S=49.4472 / D=6.8271). The exact StudyForge Kotlin source returns S=53.6269029171 and D=6.3463579662.
- Root cause confirmed: the comparison was mixing different default parameter vectors. Pinned py-fsrs v6.0.0 `fsrs/fsrs.py` has defaults `[0.2172, 1.1771, 3.2602, 16.1507, 7.0114, 0.57, 2.0966, 0.0069, 1.5261, 0.112, 1.0178, 1.849, 0.1133, 0.3127, 2.2934, 0.2191, 3.0004, 0.7536, 0.3332, 0.1437, 0.2]`; StudyForge currently uses `[0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542]`. The vectors differ in all 21 positions. The first mismatch is already visible on the initial Again: upstream S/D=0.2172/7.0114; StudyForge=0.212/6.4133.
- Two additional pinned-v6.0.0 behavioral differences were found: (1) upstream v6.0.0 clamps initial Easy difficulty before using it as the mean-reversion target; StudyForge currently passes `clamp=false`; (2) upstream v6.0.0 short-term stability increase is clamped to >=1 for Good/Easy, not Hard, while StudyForge clamps every grade >= Hard. The Hard difference does not cause the `test_memo_state` fixture failure because that fixture contains no Hard ratings.
- This corrects an earlier investigation note that relied on the newer `main/fsrs/scheduler.py` formula and treated its unclamped mean-reversion target as the pinned v6.0.0 contract. The actual pinned commit's source is `fsrs/fsrs.py`, and its behavior is different. Do not use current-main source as a substitute for the pinned reference.
- No production FSRS code was changed. This task was diagnosis only. Before changing production, decide explicitly whether StudyForge's compatibility target is the exact py-fsrs v6.0.0 defaults/behavior or a newer FSRS-6 parameter baseline. If the target remains pinned v6.0.0, the defaults and the two behavioral differences above must be reconciled and regression-tested together.
- Source references: pinned upstream implementation `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/fsrs/fsrs.py`; pinned fixture `https://raw.githubusercontent.com/open-spaced-repetition/py-fsrs/352c7a2/tests/test_basic.py`. Local diagnostic output was reproduced successfully on 2026-11-25. This is a standalone source harness, not Android Gradle/JUnit or emulator verification.
