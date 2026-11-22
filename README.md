# StudyForge 2.0.1

StudyForge is an offline-first Android question library and study app. Version 2.0 adds a learning engine: scheduled reviews, review history, mistake tracking, progress analytics, and a timed MCQ exam mode. Your question library remains local and works without an account.

## What's included in 2.0.1

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

## Known limitations in 2.0.1

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
unzip -o StudyForge-v2.0.1-source.zip
cd StudyForge
```

3. Initialise and push the repository (set your own Git identity once):

```bash
git init
git config --global user.name "Your Name"
git config --global user.email "YOUR-EMAIL@example.com"
git add .
git commit -m "StudyForge 2.0.1 patch release"
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
unzip -o ~/storage/downloads/StudyForge-v2.0.1-source.zip -d ~/studyforge-update
cd /path/to/your/existing/StudyForge
cp -r ~/studyforge-update/StudyForge/. .
git add -A
git commit -m "Fix Room migration import"
git push
rm -rf ~/studyforge-update
```

Replace `/path/to/your/existing/StudyForge` with the real location of your cloned or initialised repository. Do not run `git init` again inside an already existing repository.

### Download the APK

1. Open your GitHub repository and select **Actions**.
2. Open the latest **Build StudyForge APK** run.
3. Confirm the unit-test step and APK build both succeeded.
4. Download the artifact named `StudyForge-2.0.1-debug-apk`.
5. Extract the artifact ZIP and install `app-debug.apk`.

Install the new APK over the existing StudyForge app. **Do not uninstall the old app first**; uninstalling can delete its local database. Export a JSON backup before updating as an extra precaution. The Version 1-to-2 database migration is designed to preserve existing questions and collections.

If the workflow fails, copy the first red error section from the Actions log rather than only the final `BUILD FAILED` line.

## Project configuration

- Application ID: `com.studyforge.app`
- Version: `2.0.1` (versionCode 4)
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
