# StudyForge 2.1.2

StudyForge is an offline-first Android question library and learning app. This feature release advances unfinished Version 1 and Version 2 work: FSRS-6 scheduling, persistent study settings, expanded study modes, configurable exams, topic/collection/tag filters, Markdown-like rich-content previews, native offline LaTeX rendering, and backup schema 3.

## What's new in 2.1.2

### Version 1 — Core app improvements
- Native Canvas-based LaTeX rendering for saved equation content, packaged with the app for offline use.
- Markdown-like rich-content preview for headings, bullets, bold, italic, and inline code in question, answer, and explanation content.
- Expanded library filters for type, draft/verified status, subject, chapter, topic, tag, and collection.
- Search also includes answer and explanation text.
- Existing question IDs, collection links, and review history are preserved when editing.

### Version 2 — Learning engine improvements
- FSRS-6 scheduling with the published default 21-parameter set, stability and difficulty state, whole local study-day elapsed intervals, retrievability calculation, retention-target intervals, and Again/Hard/Good/Easy ratings.
- Configurable desired retention, maximum interval, new-card and review-card daily limits, ordering preference, and new/relearning retry delays.
- Study modes: Due, All, Practice (does not write review history or change scheduling), Random, Ordered, Topic-focused, Weakest-first, and a basic Step-by-step reveal flow.
- Daily limits apply to the Due queue; the selected review order affects due reviews.
- Configurable exam question count and duration, optional randomization, mark-for-review flags, submit confirmation, and result review. Exams remain MCQ-only.
- Progress screen includes actual recorded review accuracy, weak topics, mistake categories, and weak-subject counts.
- Study settings and FSRS memory values are included in JSON backup schema 3.

## Known limitations — do not mistake these for completed requirements

- The rich-content feature is a Markdown-like text renderer, not the full structured block editor in the original plan. Image/diagram insertion, tables, graphs, richer inline controls, and a full editor toolbar remain incomplete.
- The LaTeX preview is integrated in source, but offline rendering and device compatibility still need CI and real-device verification.
- The FSRS-6 scheduler is implemented in source using default parameters. It has unit tests, but the tests have not yet been run in GitHub Actions. The implementation is not yet verified against the official FSRS test vectors or on-device review behaviour.
- The current custom retry-delay setting uses the first valid configured number. Full multi-step learning/relearning step progression remains incomplete.
- The step-by-step mode reveals the answer and then explanation; it is not yet a structured derivation-step engine.
- Library filters cover type, status, subject, chapter, topic, tag, and collection. Difficulty, exam-label, performance, and custom saved filters remain incomplete.
- Exam mode remains MCQ-only. It has configurable length/time, randomization, mark-for-review, and submit confirmation, but no multi-part/partial-credit or question navigator grid.
- Dynamic numerical questions remain Version 3; AI generation/publishing remain Version 4; cloud sync/multi-user support remain Version 5.
- No successful 2.1.2 GitHub Actions build or device installation is recorded yet. Do not treat this source ZIP as a verified APK.

## Project configuration

- Application ID: `com.studyforge.app`
- Version: `2.1.2` (versionCode 7)
- Minimum Android: Android 8.0 (API 26)
- Compile SDK: 36; Target SDK: 35
- Kotlin 2.2.10; Jetpack Compose; Room 2.8.5
- Java 17; Android Gradle Plugin 8.13.2; Gradle 8.13
- Room database schema: version 3 (migrations 1→2→3)
- JSON backup schema: version 3 (imports schema versions 1, 2, and 3)

## Backup and data compatibility

Schema 3 includes question FSRS stability/difficulty, collections and cross-links, review history, and study settings. Schema 1 and 2 backups remain importable. When an older backup is imported over an existing 2.1.2 library, existing FSRS values for matching question IDs are retained because older backup schemas do not contain FSRS memory state. Settings absent from an older backup are left unchanged.

The Room 2→3 migration adds the FSRS columns and the settings table without deleting existing questions or review logs. Back up the installed app before installing an update. The migration and restore paths still require CI/instrumented/runtime testing.

## Build an APK from an Android phone

The APK is built in GitHub Actions. The project is intended to be manageable from Android using Termux.

### Apply this ZIP to an existing Git repository

1. Download `StudyForge-v2.1.2-self-contained-handoff.zip` to Downloads.
2. In Termux:

```bash
pkg install unzip git
termux-setup-storage
mkdir -p ~/studyforge-update
unzip -o ~/storage/downloads/StudyForge-v2.1.2-self-contained-handoff.zip -d ~/studyforge-update
cd /path/to/your/existing/StudyForge
cp -r ~/studyforge-update/StudyForge/. .
git add -A
git commit -m "StudyForge 2.1.2 learning features"
git push
rm -rf ~/studyforge-update
```

Replace `/path/to/your/existing/StudyForge` with your actual repository location. Do not run `git init` again in an existing repository.

### GitHub Actions signing secrets

The workflow keeps the fixed non-secret alias `studyforge-release` and validates it before the release build. Configure only these three repository secrets, using your existing private keystore and credentials:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_STORE_PASSWORD`
- `SIGNING_KEY_PASSWORD`

`SIGNING_KEY_ALIAS` is not required. Never put the keystore, Base64 text, passwords, GitHub tokens, or AI keys in the project ZIP or public repository. Do not regenerate the existing signing key for this update.

### Get the APK

1. Open GitHub repository → Actions → latest **Build StudyForge APK** run.
2. Confirm the unit-test step, debug build, signing preflight, signed release build, and artifact uploads succeeded.
3. Download `StudyForge-2.1.2-release-apk` for installation/update. The debug APK is for testing only.
4. Extract the artifact ZIP and install the APK.

Always export a JSON backup before updating. If the installed app and 2.1.2 APK use the same signing certificate, install over the current app; do not uninstall. If Android reports a signature conflict because the installed app was signed with a different debug key, verify the backup first, then perform the one-time uninstall/reinstall and restore. All subsequent updates must use the exact same release keystore and alias.

If Actions fails, share the first red error section, including the complete `What went wrong` and `Caused by` details. A source ZIP alone is not proof that the APK builds.

## Project documents

- `StudyForge_Plan.txt` — original detailed product blueprint; preserve unchanged.
- `PROJECT_PLAN.md` — approved five-version master plan and current release context.
- `IMPLEMENTATION_STATUS.md` — current source-vs-plan status, with verification levels.
- `ROADMAP.md` — live planned/completed work and remaining gaps.
- `CHANGELOG.md` — append-only release history.
- `DEVELOPMENT_LOG.md` — dated technical work and decisions.
- `BUILD_STATUS.md` — actual build/test evidence; 2.1.2 is not yet CI verified.
- `HANDOFF_FOR_NEW_CHAT.md` — full prompt for the next ChatGPT conversation.
- `PROJECT_FILE_GUIDE.md` — source and documentation map.
- `SIGNING_SETUP.md` — release-key configuration and protection.
- `THIRD_PARTY_NOTICES.md` — third-party library and algorithm references.
- `backup-example.json` — example schema 3 JSON backup.

## Privacy and product scope

Current question-library, scheduling, study, and analytics data are stored locally. No account or remote service is required for normal use. Exported backups may contain personal notes; keep them private. AI and cloud features must use a secure server-side proxy/secret store in their future major-version work.

Version 2.1.2 advances V1/V2 features but does not declare either major milestone complete. The complete original feature scope remains authoritative; unfinished work must stay in the roadmap.
