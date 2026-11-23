# StudyForge 2.1.0 — Release signing setup

This project can produce a signed release APK when the GitHub repository has the four Actions secrets below. The private keystore and its passwords are deliberately excluded from this source ZIP and must never be committed to Git.

## Files supplied separately

- `StudyForge-release-key.jks` — private Android signing keystore.
- `StudyForge-keystore-base64.txt` — Base64 encoding of the keystore, for the GitHub secret.
- `StudyForge-Signing-Credentials-PRIVATE.txt` — alias and passwords. Keep this private.

## Add GitHub Actions secrets

Open the StudyForge repository on GitHub, then go to **Settings → Secrets and variables → Actions → New repository secret**. Create these four repository secrets:

| Secret name | Value |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | Entire one-line contents of `StudyForge-keystore-base64.txt` |
| `SIGNING_STORE_PASSWORD` | Store password from the private credentials file |
| `SIGNING_KEY_ALIAS` | `studyforge-release` |
| `SIGNING_KEY_PASSWORD` | Key password from the private credentials file |

Do not put any of these values in Kotlin, Gradle files, README files, Git commits, or public issues.

## Build behavior

Every workflow run still runs unit tests and creates a debug APK. When the keystore secret exists, the workflow also builds and uploads `StudyForge-2.1.0-release-apk`. When the keystore secret is empty, the signed release build is skipped and the debug artifact is still produced. If the keystore secret is present but any of the other three signing secrets is missing, the workflow fails clearly rather than producing an unsigned release artifact. The workflow removes the temporary keystore file at the end of the job.

Gradle reads the signing data from environment variables and only configures the release signing block when all required values are present.

## Important one-time installation transition

The previous 2.0.1 workflow produced **debug APKs**, not APKs signed with this new release key. Android normally refuses to update an installed app when the signing certificate changes. Before installing the first 2.1.0 signed release APK:

1. Open the currently installed StudyForge app.
2. Export a JSON backup and verify that the file exists in your chosen location.
3. Install the first signed release APK. If Android reports a signature conflict, uninstall the old debug-signed app, then install the signed release APK.
4. Open StudyForge and import your JSON backup.

Uninstalling the old app can erase its local database, so do not uninstall until the JSON backup has been exported. After this one-time switch, every future release must use this same keystore, alias, and credentials to update in place.

## Protect the key

- Keep at least two backups of the `.jks` file in separate private/offline locations.
- Keep the passwords stored separately from the keystore where practical.
- Never regenerate or replace this key for an ordinary app update.
- Never include the keystore, Base64 text, or credentials file in the source ZIP or GitHub repository.
- If the keystore is lost, future sideloaded APKs signed with a different key will not update the installed app in place. A JSON backup allows data recovery after reinstall, but does not preserve the installed signature identity.

## Verify a release

After GitHub Actions succeeds, download the `StudyForge-2.1.0-release-apk` artifact. Check that the workflow shows successful unit tests, successful `assembleRelease`, and a successfully uploaded release artifact. Device installation and update must still be tested separately.
