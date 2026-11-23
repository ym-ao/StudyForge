# StudyForge — Release signing setup (2.1.2)

This project creates a release-signed APK when the GitHub repository has the three Actions secrets below. The alias is a non-secret constant (`studyforge-release`) validated by the workflow before Gradle runs.

## Private files supplied separately

- `StudyForge-release-key.jks` — private Android signing keystore.
- `StudyForge-keystore-base64.txt` — Base64 encoding of the keystore, for the GitHub secret.
- `StudyForge-Signing-Credentials-PRIVATE.txt` — store and key passwords. Keep this private.

Never commit these files or their contents to GitHub.

## Configure GitHub Actions secrets

Open the StudyForge repository, then go to **Settings → Secrets and variables → Actions**. Create or update these three repository secrets:

| Secret name | Value |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | Entire contents of `StudyForge-keystore-base64.txt` |
| `SIGNING_STORE_PASSWORD` | Store password from the private credentials file |
| `SIGNING_KEY_PASSWORD` | Key password from the private credentials file |

`SIGNING_KEY_ALIAS` is no longer needed. The workflow uses the documented alias `studyforge-release` directly, so remove the old alias secret if you wish. It is harmless if it remains configured because the workflow does not read it.

## Preflight validation

Before Gradle assembles the release APK, GitHub Actions decodes the keystore and uses `keytool` to verify that:
- The keystore can be opened with `SIGNING_STORE_PASSWORD`.
- It contains the expected alias `studyforge-release`.

If validation fails, the workflow stops before `:app:packageRelease` and reports a clearer signing setup error. It may list aliases present in the keystore, but never prints passwords.

## Build behavior

Every workflow run runs unit tests and creates a debug APK. If `SIGNING_KEYSTORE_BASE64` is absent, the signed release build is skipped and the debug artifact remains available. If the keystore is supplied but a password is missing or validation fails, the workflow stops with an explicit error. The temporary decoded keystore is removed from the runner at the end of the job.

## One-time transition from debug signing

Earlier StudyForge workflow artifacts were debug-signed. Android normally refuses an in-place update if the signing certificate changes. Before installing the first release-key-signed APK:

1. Open the currently installed app.
2. Export a JSON backup and verify that it exists.
3. Install the release APK. If Android reports a signature conflict, uninstall the old debug-signed app only after verifying the backup.
4. Install the release APK and restore the backup.

After this one-time transition, use this exact same keystore, passwords, and alias for every future update. Do not regenerate the key.

## Protect the key

- Keep at least two secure backups of the `.jks` file.
- Keep passwords private and separate where practical.
- Never include the keystore, Base64 text, or credentials in the source ZIP or repository.
- Losing the key prevents ordinary in-place updates signed with the same certificate; a JSON backup can help recover data after reinstall, but does not preserve the app's signing identity.
