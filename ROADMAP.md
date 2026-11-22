# StudyForge Roadmap

This file is the living plan. Every release must update both this roadmap and `CHANGELOG.md`.

## Release status

| Area | Status | Notes |
|---|---|---|
| Offline question library and collections | Complete (1.0) | Room database; multi-collection links |
| Flashcards and MCQs | Complete (1.0) | Create, edit, search, study |
| JSON backup and restore | Complete (1.0), expanded (2.0) | V2 includes schedules and review logs; V1 import remains supported |
| Scheduled review and ratings | Complete (2.0) | Simple SM-2-inspired scheduler, not FSRS |
| Practice and timed MCQ exam | Complete (2.0) | Practice is schedule-neutral; exam up to 10 MCQs / 15 minutes |
| Mistake tracking and progress | Complete (2.0) | Review history, accuracy, weak topics, mistake categories |
| Rendered LaTeX | Planned | Preserve source editing and display rendered equations |
| Dynamic numerical questions | Planned | Deterministic variable templates, constraints, units, tolerance, worked substitutions |
| Advanced rich-content question blocks | Planned | Cloze, step-by-step derivations, images, tables, graphs, multipart questions |
| AI-assisted question generation | Planned | Draft-first, review/approve, provider-independent backend |
| External API / publishing workflows | Planned | Authenticated batch validation and publish/update pipeline |
| Optional cloud sync and accounts | Planned | Backend proxy, conflict handling, multi-device sync |

## Next release: Version 2.1 (recommended)

Focus on reliability and study usability before starting the larger AI/cloud work:

1. Render LaTeX in prompts, answers, and explanations while retaining editable source.
2. Add sorting and study filters for subject, chapter, topic, collection, and question status.
3. Add configurable daily new/review limits, exam duration/question count, and review order.
4. Add collection-specific study sessions and a review-history detail view.
5. Add more tests for backup round-trips, migration compatibility, scheduler edge cases, and timer/answer checking.

## Version 3.0 — Dynamic question engine

- Reusable numerical templates with bounded variables and constraints.
- Deterministic answer calculation and reproducible variants.
- Answer tolerances, units, conversions, rounding, and significant figures.
- Step-by-step worked solutions with generated substitutions.
- Distractor generation from explicit rules and validated calculations.

## Version 4.0 — AI and automation

- In-app AI generation through a secure backend proxy.
- Multiple provider/model configuration without embedding secrets in the APK.
- Draft, validate, edit, approve, publish, and version question workflows.
- JSON-first API and external publishing; CSV for simple content.
- Optional web dashboard and script integration.

## Version 5.0 — Cloud and expansion

- Optional accounts and cloud sync.
- Multi-device conflict detection and resolution.
- Secure media storage and backup management.
- Shareable collections and collaboration only after core sync is reliable.

## Release process

For every version:
1. Update the version name/code and migration if the database schema changes.
2. Add completed work and known limitations to `CHANGELOG.md`.
3. Update completed/in-progress/next items in this file and `DEVELOPMENT_LOG.md`.
4. Add or update automated tests.
5. Run GitHub Actions and verify the APK artifact before marking a release build as verified.
