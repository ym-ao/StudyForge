# StudyForge 2.1.0

StudyForge is an offline-first Android question library and study app. Version 2.0 adds a learning engine: scheduled reviews, review history, mistake tracking, progress analytics, and a timed MCQ exam mode. Your question library remains local and works without an account.

## What's included in 2.1.0

### Library and authoring
- Local Room database; no account or internet connection is required for normal use.
- Question library with search and filters for Flashcards and MCQs.
- Create/edit questions with explanations, subject, chapter, topic, tags, status, MCQ options, and LaTeX source text.
- Collections with many-to-many links: one question can belong to multiple collections.
- JSON backup and restore, now including review history and scheduling data.

### Learning engine
- **Due mode:** works through new and due questions first.
- **All mode:** review the complete library.
- **Practice mode:** practice without changing the review schedule.
- **Again / Hard / Good / Easy ratings:** a transparent SM-2-inspired interval scheduler assigns the next due date. Again schedules a 10-minute relearning step; the other ratings set longer intervals.
- **Review history:** each scheduled review records its time, rating, mode, correctness (when known), and mistake category.
- **Mistake tracking:** classify incorrect MCQs as concept gap, wrong formula, calculation/algebra, unit conversion, sign/direction, misread question, forgotten fact, or guess/uncertainty.
- **Exam mode:** up to 10 randomly selected MCQs in a 15-minute session, with a results summary and review-history updates.
- **Progress screen:** due count, review count, overall and recent accuracy, learning status, common mistake categories, and topics with incorrect answers.

### Data safety and compatibility
- Database migration from Version 1 to Version 2 adds scheduler fields and review logs without deleting existing questions or collections.
- Version 2 imports both Version 1 and Version 2 JSON backups.
- Version 2 exports questions, collections, links, scheduling state, and review logs.
- Editing a question preserves its review schedule and history.

## Known limitations in 2.1.0

- The scheduler is a simple, transparent SM-2-inspired implementation, **not FSRS**.
- LaTeX is saved as source text; it is not yet rendered as formatted mathematics.
- Exam mode currently tests MCQs only. The test is limited to 10 questions and has a fixed 15-minute time limit.
- Progress analytics are based on stored review history; imported Version 1 backups have review counts but no historic individual review records.
- Dynamic numerical question templates, deterministic unit-aware calculation, AI generation, external publishing API, and cloud sync are not included yet.

## Build an APK without a computer

The APK is built on GitHub's cloud runner. You can manage the source from an Android phone using Termux.

### First upload to GitHub

1. Create an empty GitHub repository named `StudyForge` (do not add a README or licence).
2. Extract this ZIP. In Termux, install Git and unzip if needed:

```bash
pkg update
pkg install git unzip
termux-setup-storage
cd ~/storage/downloads
unzip -o StudyForge-v2.1.0-self-contained-handoff.zip
cd StudyForge
```

3. Initialise and push the repository (set your own Git identity once):

```bash
git init
git config --global user.name "Your Name"
git config --global user.email "YOUR-EMAIL@example.com"
git add .
git commit -m "StudyForge 2.1.0 patch release"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/StudyForge.git
git push -u origin main
```

Replace the username and email placeholders with your own details. Authenticate through GitHub's normal sign-in/token flow. Never place an access token in the source code or a public file.

### Updating an existing StudyForge Git repository

First download this ZIP into your phone's Downloads folder. In Termux, extract its `StudyForge` folder to a temporary location, copy the updated project files over your existing Git checkout, then commit and push:

```bash
pkg install unzip git
termux-setup-storage
mkdir -p ~/studyforge-update
unzip -o ~/storage/downloads/StudyForge-v2.1.0-self-contained-handoff.zip -d ~/studyforge-update
cd /path/to/your/existing/StudyForge
cp -r ~/studyforge-update/StudyForge/. .
git add -A
git commit -m "Update StudyForge to 2.1.0"
git push
rm -rf ~/studyforge-update
```

Replace `/path/to/your/existing/StudyForge` with the real location of your cloned or initialised repository. Do not run `git init` again inside an already existing repository.

### Download the APK

1. Open your GitHub repository and select **Actions**.
2. Open the latest **Build StudyForge APK** run.
3. Confirm the unit-test step and APK build both succeeded.
4. When the four signing secrets are configured, download `StudyForge-2.1.0-release-apk` for normal installation and future updates. Otherwise, `StudyForge-2.1.0-debug-apk` is available for testing only.
5. Extract the artifact ZIP and install the APK inside it.

Always export a JSON backup before updating. For ordinary future updates signed with the same release key, install over the existing StudyForge app; do not uninstall it. For the first switch from the old debug-signed 2.0.1 app to the new release-signed APK, follow the one-time transition below. The database remains at version 2 in this source update.

If the workflow fails, copy the first red error section from the Actions log rather than only the final `BUILD FAILED` line.

## Signed release APKs and future updates

The workflow can create a release-signed APK using a private key supplied through GitHub Actions secrets. Follow `SIGNING_SETUP.md` and use the separately supplied private keystore/credential files. Never add the keystore or passwords to this repository.

**Important:** The earlier workflow artifacts were debug-signed. The first APK signed with the new release key will not update an installed copy that has a different signing certificate. Export and verify a JSON backup first. Then uninstall the old debug copy, install the signed release APK, and import the backup. This is a one-time transition; future releases must keep using the exact same key.

The workflow artifacts are `StudyForge-2.1.0-debug-apk` and, when signing secrets are present, `StudyForge-2.1.0-release-apk`. A source ZIP alone does not prove that either artifact builds; check the actual GitHub Actions run.

## Project configuration

- Application ID: `com.studyforge.app`
- Version: `2.1.0` (versionCode 5)
- Minimum Android: Android 8.0 (API 26)
- Compile SDK: 36; Target SDK: 35
- Kotlin + Jetpack Compose
- Room / SQLite local database
- Java 17; Android Gradle Plugin 8.13.2; Gradle 8.13

## Backup format

The exported JSON uses `schemaVersion: 2`. Version 2 supports the Version 1 backup format and fills new scheduler fields with safe defaults when importing old backups. See `backup-example.json` for a sample Version 2 structure.

## Privacy

Core library, study, scheduling, and analytics features are local. The current app does not need an account or AI provider key. Keep exported backups private if they contain your personal notes. AI or cloud features must use a server-side secret store/proxy in a later release; never put provider API keys in the Android app or Git repository.

## Project logs

- `CHANGELOG.md` — release-by-release record of completed changes.
- `ROADMAP.md` — what is completed, in progress, and planned next.
- `DEVELOPMENT_LOG.md` — concise development status and verification notes.


## Project documentation and new-chat handoff

This source ZIP is designed to be uploaded into a fresh ChatGPT conversation so development can continue without depending on an earlier chat. Read these files before changing the project:

- `PROJECT_PLAN.md` — current plan and approved five-major-version structure.
- `StudyForge_Plan.txt` — original detailed product blueprint supplied by the user.
- `IMPLEMENTATION_STATUS.md` — code-audited feature matrix showing implemented, partial, and not-started work.
- `CHANGELOG.md` — release history.
- `ROADMAP.md` — live roadmap and next release priorities.
- `DEVELOPMENT_LOG.md` — dated implementation and decision history.
- `BUILD_STATUS.md` — actual build/test evidence and known failures.
- `HANDOFF_FOR_NEW_CHAT.md` — copy-paste prompt for continuing in a new chat.
- `SIGNING_SETUP.md` — how to configure private GitHub Actions secrets and transition from debug to release signing.

**Important:** the included app source is 2.1.0 (versionCode 5). No successful GitHub Actions run for this package has been supplied or verified yet. The source includes the previously corrected Room `Migration` import and a conditional signed-release pipeline. Configure signing secrets and inspect the Actions run before treating an APK as build-verified.

### Remaining approved work

The current 2.1.0 package adds release-signing infrastructure. It does not yet complete the planned FSRS scheduler, configurable review settings, rendered LaTeX, full filters/rich editor, or all regression tests. These original requirements remain open in `ROADMAP.md` and must not be removed from the plan. Version 3.0 remains reserved for the dynamic numerical question engine.
