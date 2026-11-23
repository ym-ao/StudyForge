# StudyForge Roadmap

This is the live implementation roadmap. The five major versions below are the approved product plan. Smaller patch/minor releases may be used inside a major version.

## Current release snapshot

- **Current source version:** 2.1.2
- **versionCode:** 7
- **Room database version:** 3
- **JSON backup schema:** 3 (import accepts versions 1, 2, and 3)
- **Build status:** Not verified for this source ZIP; CI must pass after the signing secrets are configured.
- **Device status:** No confirmed install/update test recorded for this source ZIP.

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
- [~] Type, status, subject, chapter, topic, tag, and collection filters implemented; difficulty/exam/performance and saved filters remain open.
- [ ] Successful CI build and installation/update acceptance test for a release candidate.

### Version 2.0 — Learning engine

**Foundation exists; milestone is not fully complete.**

- [~] FSRS-6 scheduler and default parameters implemented in source; CI tests and official reference-vector validation remain pending.
- [x] Again / Hard / Good / Easy ratings and due-date scheduling (scheduler implementation upgraded to FSRS-6 in 2.1.2).
- [x] Due, All, and Practice modes.
- [~] Timed MCQ exam mode; count, duration, and randomization are configurable in 2.1.2, but it remains MCQ-only.
- [x] Mistake categories and review logs.
- [x] Basic progress/weak-topic reporting.
- [~] Persistent desired-retention, max interval, new/review daily limits, review order, and retry-delay settings implemented in source; test and device verification pending.
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

The 2.1.0 source package added secure optional release signing, CI support for a signed release artifact, and signing/installation documentation. At that time the following V1/V2 work remained; 2.1.2 has since implemented some of it, with the current status shown above and in `IMPLEMENTATION_STATUS.md`:

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
