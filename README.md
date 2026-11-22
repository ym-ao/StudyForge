# StudyForge 1.0.0

StudyForge is an offline-first Android question library and study app. The first release focuses on personal use and local data storage. It is designed so later versions can add adaptive spaced repetition, dynamic numerical questions, AI generation, and optional cloud sync without putting provider secrets in the Android app.

## Included in this first build

- Local Room database; no account or internet connection is required for normal use.
- Question library with search and filters for Flashcards and MCQs.
- Create and edit flashcards and MCQs, including explanations, subject/chapter/topic, tags, status, and a LaTeX source field.
- Collections with many-to-many links: one question can belong to multiple collections.
- Study screen with answer reveal for flashcards and answer checking for MCQs.
- Basic review count and last-reviewed timestamp.
- JSON backup and restore, including question data, collections, collection links, and review counts.
- GitHub Actions workflow that builds a debug APK after a push to `main` or on manual request.

## Known 1.0 limitations

- LaTeX source is saved as text; rendered equation preview is planned for a later 1.x release.
- Review ratings are recorded as review counts only. FSRS-style scheduling, mistake categories, analytics, exam mode, and dynamic numerical templates are planned for Version 2.0 and Version 3.0.
- AI generation, server-side API publishing, account login, and cloud sync are not included. No AI API key should ever be placed in this Android project.
- The Study screen currently cycles through the full question library; collection-specific study filters will be added in a later update.

## Build an APK without a computer

You can upload the source from an Android phone using Termux. The APK is built on GitHub's cloud runner, not on your phone.

### 1. Create a GitHub repository

Create a new empty repository named `StudyForge` on GitHub. Do not add a README or licence there because this ZIP already contains the project files.

### 2. Extract and upload with Termux

Install Termux from a trusted distribution source, then run:

```bash
pkg update && pkg upgrade
pkg install git unzip
termux-setup-storage
```

Place `StudyForge-v1.0.0.zip` in your phone's Download folder. Then run:

```bash
cd ~/storage/downloads
unzip StudyForge-v1.0.0.zip
cd StudyForge

git init
# Set these once on your Termux installation, using your own details:
git config --global user.name "Your Name"
git config --global user.email "YOUR-EMAIL@example.com"
git add .
git commit -m "Initial StudyForge 1.0"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/StudyForge.git
git push -u origin main
```

Replace `YOUR-USERNAME` with your GitHub username. When GitHub asks you to authenticate, use its normal sign-in/token flow; never paste a token into a public script or commit it to this repository.

### 3. Download the APK from GitHub

1. Open your repository on GitHub.
2. Open the **Actions** tab.
3. Select **Build StudyForge APK** and open the latest successful run.
4. In **Artifacts**, download `StudyForge-debug-apk`.
5. Extract the artifact ZIP; it contains `app-debug.apk`.
6. Open the APK on your phone and follow Android's installation prompt.

The first build may take several minutes because GitHub needs to download the Android build dependencies. If the workflow fails, open the failed run and copy the first red error section; that is the useful part to diagnose.

## Project configuration

- Package/application ID: `com.studyforge.app`
- Minimum Android version: Android 8.0 (API 26)
- Compile SDK: 36
- Target SDK: 35
- Language/UI: Kotlin + Jetpack Compose
- Local storage: Room / SQLite
- Build: Android Gradle Plugin 8.13.2, Gradle 8.13, Kotlin 2.2.10

## JSON backup format

`backup-example.json` shows the version 1 structure. The importer checks `app` and `schemaVersion`. Keep the schema version when preparing compatible backup files.

## Privacy and security

The app does not ask for an account and does not include AI provider credentials. The current app does not need network permission for its core functions. Keep backups private if they contain personal notes or study materials. Before adding any AI or cloud integration, use a server-side proxy and store secrets on the server, never in the APK or repository.

## Roadmap

- **1.0 Core app:** local question library, collections, flashcards, MCQs, and JSON backup/restore.
- **1.x improvements:** rendered LaTeX preview, collection-specific study, sorting, and richer editor blocks.
- **2.0 Learning engine:** spaced repetition, exam/practice modes, mistake tracking, and weak-topic analytics.
- **3.0 Advanced question engine:** dynamic numerical templates, deterministic calculation, tolerances, and step-by-step derivations.
- **4.0 AI and automation:** provider-independent AI generation, draft review, external publishing API, and workflow imports.
- **5.0 Cloud and expansion:** optional accounts, sync, conflict resolution, and multi-device support.
