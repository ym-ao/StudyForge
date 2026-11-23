# StudyForge Roadmap

This is the live implementation roadmap. The five major versions below are the approved product plan. Smaller patch/minor releases may be used inside a major version.

## Current release snapshot

- **Current source version:** 2.1.0
- **versionCode:** 5
- **Room database version:** 2
- **JSON backup schema:** 2 (import accepts versions 1 and 2)
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
- [ ] Full structured rich-content editor (content blocks, formatting, images/tables/graphs as scoped).
- [ ] Proper rendered LaTeX in prompts, answers, hints, and explanations.
- [ ] Full subject/chapter/topic/tag/collection/status filters.
- [ ] Successful CI build and installation/update acceptance test for a release candidate.

### Version 2.0 — Learning engine

**Foundation exists; milestone is not fully complete.**

- [ ] Replace/upgrade the current SM-2-inspired algorithm with the planned FSRS scheduler, after evaluating a compatible implementation and adding tests.
- [x] Again / Hard / Good / Easy ratings and due-date scheduling (current scheduler is SM-2-inspired, not FSRS).
- [x] Due, All, and Practice modes.
- [x] Timed MCQ exam mode (currently fixed at 10 questions maximum and 15 minutes).
- [x] Mistake categories and review logs.
- [x] Basic progress/weak-topic reporting.
- [ ] Configurable new/review limits and review settings.
- [ ] Configurable exam count/duration and more complete exam navigation/settings.
- [ ] More study modes (step-by-step, topic/weak-area/numerical/custom sessions and selectable order).
- [ ] Stronger topic/subject/chapter/skill analytics.
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

## Current 2.1.0 release state and remaining scope

The 2.1.0 source package adds secure optional release signing, CI support for a signed release artifact, and signing/installation documentation. The remaining approved Version 1/2 work is still required and remains in scope; it is not complete in this source ZIP:

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
