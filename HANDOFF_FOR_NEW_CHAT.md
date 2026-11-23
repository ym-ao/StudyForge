# Continue StudyForge in a new ChatGPT conversation

Upload the latest complete StudyForge source ZIP to the new conversation, then paste the prompt below. This ZIP is intended to be self-contained: it includes source code, the original detailed plan, the current master plan, logs, build workflow, implementation audit, and this handoff guide.

## Copy-paste prompt

```text
I'm continuing development of my Android application, StudyForge. The uploaded ZIP is the current source-of-truth package.

First, carefully inspect the entire ZIP before changing anything.

1. Read PROJECT_PLAN.md, StudyForge_Plan.txt, IMPLEMENTATION_STATUS.md, CHANGELOG.md, ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, README.md, and this handoff file.
2. Inspect the actual Kotlin source, database entities/DAO/migrations, Gradle configuration, unit tests, JSON backup/import code, Android manifest, and GitHub Actions workflow. Do not rely on the documentation alone.
3. Confirm the current app version, versionCode, Room database version, JSON backup schema, completed source features, known limitations, and latest verified build/test status.
4. Compare source code against the approved five-major-version plan. Distinguish clearly between planned, implemented in source, tested, CI-build-verified, and tested on the Android device.
5. Do not silently remove, simplify, or change the approved product requirements. If the source and documentation disagree, report the discrepancy and preserve data compatibility while resolving it.
6. Continue with the next planned release. The current package is StudyForge 2.1.0 (versionCode 5). The implementation audit says Version 1 and Version 2 are not fully complete: LaTeX is source-only, the scheduler is SM-2-inspired rather than FSRS, review/exam settings are limited, and the full filters/rich editor requirements remain incomplete. The 2.1.0 package adds secure optional release signing and a conditional release APK workflow; those learning-engine requirements remain open and must not be removed. Continue them before marking the milestone complete or moving to 3.0.
7. Before editing code, inspect database migrations, existing question IDs, collection links, review history, and JSON import/export compatibility. Never use destructive database fallbacks or silently discard user data. Add migrations and tests when schema changes are necessary.
8. Add or update tests for new features and regression risks. Never claim tests passed or APK built unless the command/CI result actually confirms it.
9. Keep the Android phone + Termux + GitHub Actions workflow practical. I do not rely on a PC for the normal build process. Keep the ZIP GitHub-ready.
10. For every release, update versionName/versionCode and workflow artifact naming consistently. Append to CHANGELOG.md; update ROADMAP.md, DEVELOPMENT_LOG.md, BUILD_STATUS.md, IMPLEMENTATION_STATUS.md, README.md, and this handoff information where relevant. Keep historical logs; do not erase old entries.
11. Deliver a complete new source ZIP containing all source code, GitHub Actions workflow, original plan, current master plan, implementation matrix, release logs, build status, signing setup guide, and new-chat prompt. Include clear Termux instructions for applying it to an existing Git repository and explain backup/install precautions.
12. Preserve the exact release signing key. Never regenerate it for a routine update and never include it or its credentials in the source ZIP. The user stores it separately and configures GitHub Actions secrets as documented in `SIGNING_SETUP.md`.
13. If the installed app is still signed with an earlier debug key, explain the one-time backup/uninstall/reinstall transition needed before installing the release-key-signed APK. For all later updates, use the same keystore and alias.

Start by summarizing your audit of the uploaded project and the next release scope. Then proceed with development and produce the complete updated ZIP. Clearly identify anything that cannot be verified in your environment and what I must check in GitHub Actions or on my phone.
```

## Rules for using the package

- Always upload the newest complete source ZIP, not only a log file.
- Keep `StudyForge_Plan.txt` as the preserved original detailed blueprint.
- Treat `PROJECT_PLAN.md` as the current release-level plan, and `IMPLEMENTATION_STATUS.md` as the source-vs-plan audit.
- Treat the latest successful GitHub Actions run as the evidence for cloud build/test status. A source edit is not proof of a successful build.
- Before installing an update, export a backup from the currently installed app. Install the new APK over the existing app; do not uninstall first as a routine update method.
- Never put GitHub tokens, signing passwords, or AI provider API keys in the ZIP or public repository.


## Current 2.1.0 signing state

- App source is 2.1.0 / versionCode 5.
- Room database remains version 2; JSON backup schema remains version 2. No DB migration was introduced in this update.
- Release signing is configured through environment variables only. GitHub Actions needs four repository secrets: `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, and `SIGNING_KEY_PASSWORD`.
- `SIGNING_SETUP.md` describes the separate private key materials and the initial debug-to-release transition. The key and passwords are not part of this ZIP.
- No 2.1.0 CI build or device update is yet verified. Check the latest Actions run; do not claim build success without it.
- The planned FSRS/settings/LaTeX/filter requirements remain outstanding and are not deleted from the approved plan.
