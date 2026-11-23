# Continue StudyForge in a new ChatGPT conversation

Upload the latest complete StudyForge source ZIP to the new conversation, then paste the prompt below. This ZIP is the source of truth for the included code, current logs, original project plan, and release handoff.

## Copy-paste prompt

```text
I'm continuing development of my Android application, StudyForge. The uploaded ZIP is the latest complete source package.

First, inspect the entire ZIP before changing anything.

1. Read PROJECT_PLAN.md, StudyForge_Plan.txt, IMPLEMENTATION_STATUS.md, CHANGELOG.md, ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, README.md, PROJECT_FILE_GUIDE.md, SIGNING_SETUP.md, THIRD_PARTY_NOTICES.md, and this handoff file.
2. Inspect the actual Kotlin source, Gradle configuration, Room entities/DAO/migrations, unit tests, backup import/export, manifest, and GitHub Actions workflow. Do not trust documentation in place of source inspection.
3. Confirm app version/versionCode, Room version, JSON backup version, migration chain, current feature status, and actual CI/device evidence.
4. The current source is StudyForge 2.1.2 (versionCode 7), Room schema 3, JSON backup schema 3. It adds source implementations for FSRS-6, persisted review/exam settings, expanded library filters, more study modes, configurable MCQ exams, native LaTeX rendering, and Markdown-like rich-content preview. None of these should be called CI-verified or device-verified unless the real logs prove it.
5. Do not silently remove, simplify, replace, or forget any approved requirement in the original five-major-version plan. The original detailed blueprint `StudyForge_Plan.txt` is authoritative. Incomplete items stay as outstanding requirements. Changes to scope require my explicit approval.
6. Version 1/2 are still not fully complete. The rich editor still lacks the full structured block/media/table/graph model; filters still lack all difficulty/exam/performance presets; scheduler multi-step progression and advanced settings need work; step-by-step derivations/custom sessions and advanced exam navigation/types remain incomplete. See the current implementation matrix and roadmap.
7. Before data-model changes, inspect Room migrations, stable question IDs, collection links, review history and backup schema. Never use destructive fallback migrations or silently discard data. Add migrations/tests for required changes.
8. Add tests for new algorithm and database features. For FSRS, compare behavior to upstream reference vectors as well as app-specific cases. Never say tests passed or APK built without actual successful evidence.
9. Keep the Android phone + Termux + GitHub Actions build workflow practical. This project must remain manageable without a desktop computer.
10. Preserve the exact private signing key. The workflow's documented alias is the non-secret constant `studyforge-release`. Required GitHub Actions secrets are SIGNING_KEYSTORE_BASE64, SIGNING_STORE_PASSWORD, and SIGNING_KEY_PASSWORD. Do not regenerate, print, commit, or include the private key or credentials in the ZIP.
11. For every meaningful development session and every release, update relevant logs. Every release must append to CHANGELOG.md and update ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, IMPLEMENTATION_STATUS.md, README.md, PROJECT_FILE_GUIDE.md and this handoff where needed. Keep historical records; do not erase old entries.
12. Keep versionName/versionCode and workflow artifact naming consistent. Increment versionCode on each new APK release.
13. Deliver a complete self-contained source ZIP with source, workflow, original plan, current plan, current implementation audit, logs, build status, signing setup, notices, backup example, and new-chat handoff.
14. Clearly separate source implementation, locally tested, CI-tested, signed APK verified, and tested on my Android device. Do not claim build success until I provide or you obtain actual GitHub Actions evidence.

Start with a source-vs-plan audit and identify any mismatch before continuing the next planned V1/V2 release. Preserve the existing key identity and all user data. If the next step is clear, proceed with the best safe implementation and package the full updated ZIP; do not ask me to repeat information already in the files.
```

## Current 2.1.2 source snapshot

- App: 2.1.2 / versionCode 7.
- Room: schema version 3; migrations 1→2 and 2→3. Version 2→3 adds FSRS stability/difficulty columns and the `study_settings` table.
- Backup: schema version 3; imports schemas 1, 2, and 3. Schema 3 includes FSRS memory state and settings. Older backup imports are supported; matching existing FSRS values are retained for schema 1/2 imports.
- Scheduler: source implementation of FSRS-6 with the published default 21 parameters and whole local study-day elapsed intervals (calendar-day difference in the device time zone). A standalone Kotlin smoke test passed, but it has not yet been validated with upstream reference vectors or in CI.
- Settings: desired retention, maximum interval, daily new/review limits, review order, retry delays, exam question count/duration/randomization.
- Study modes: Due, All, Practice, Random, Ordered, Topic, Weakest, basic Step-by-step, Exam.
- Library: type/status/subject/chapter/topic/tag/collection filters; search includes answer/explanation.
- LaTeX: native RaTeX view integrated for the equation field; offline/device rendering not verified.
- Rich content: Markdown-like preview only. Full structured editor with media/tables/graphs remains outstanding.
- CI and device verification: not yet recorded for 2.1.2. Run the workflow and inspect the actual logs.
- Signing: keep the existing `.jks` private and use the same alias/key for all updates. Three GitHub secrets are documented in `SIGNING_SETUP.md`.

## Next unfinished approved work

Continue V1/V2 gaps before starting Version 3: complete rich blocks/media, advanced filters and tag management, multi-step scheduler flow and full custom settings, structured derivations/custom sessions, richer exams, database/backup/FSRS reference tests, and full CI/device acceptance. The V3/V4/V5 roadmap remains unchanged.

## Package rules

- `StudyForge_Plan.txt` is the preserved original blueprint and must not be edited to reduce scope.
- `PROJECT_PLAN.md` is the current master plan.
- `IMPLEMENTATION_STATUS.md` records source-vs-plan and verification status.
- `CHANGELOG.md` is append-only.
- `BUILD_STATUS.md` records only actual build/test evidence.
- Export and verify a backup before installing an APK.
- Never uninstall the app as a routine update step; if there is a signature mismatch, investigate whether the installed version is debug-signed before performing the one-time transition.
- Never include passwords, signing key material, GitHub tokens, or AI secrets in the ZIP.
