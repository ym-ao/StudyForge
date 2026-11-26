# StudyForge development tooling

**Purpose:** make Android builds reproducible across ChatGPT work sessions, local machines, and CI.

## Current project requirements

Read the Gradle configuration before changing tool versions. At this source baseline:

- Android Gradle Plugin: `8.13.2`
- Kotlin Android / Compose plugins: `2.2.10`
- KSP: `2.2.10-2.0.2`
- `compileSdk`: `36`
- Java source/target and Kotlin JVM target: `17`
- `minSdk`: `26`
- Android instrumentation runner: `androidx.test.runner.AndroidJUnitRunner`

Use JDK 17 unless the project's Gradle/AGP compatibility documentation demonstrates a necessary change. Install Android SDK Platform 36 and the Android SDK Build-Tools version required by the Android Gradle Plugin. Install platform-tools (ADB) for device/emulator workflows.

**Important current limitation:** the supplied V3.0 Batch 9 source package does not include a Gradle wrapper, and the handoff records that no Gradle executable, Android SDK tools, or ADB were available in the previous working environment. Re-check the live environment before assuming that is still true. Do not claim a build or test passed until its actual command output is available.

## Preferred toolchain

1. JDK 17.
2. Gradle Wrapper, checked into the repository after it has been generated and verified against the project's Android Gradle Plugin.
3. Android SDK command-line tools.
4. Android SDK Platform 36 and compatible Build-Tools.
5. Android platform-tools (`adb`).
6. Android Emulator and a compatible system image, only if virtualization/hardware acceleration is available and resource limits make it practical.

Prefer the repository's Gradle Wrapper (`./gradlew`) over a machine-global Gradle installation. Pin wrapper distribution/version and verify it against AGP 8.13.2 before committing it. Do not casually upgrade AGP, Kotlin, KSP, SDK, or dependency versions as part of environment setup.

## Required bootstrap action for a new work environment

A new development session must attempt the actual toolchain bootstrap, not merely report installation instructions. Inspect the live environment first, then install missing permitted components from the preferred toolchain above. Verify JDK 17, the pinned Gradle Wrapper, Android SDK command-line tools, SDK Platform 36, compatible Build-Tools, and ADB using their actual version outputs. Test with the repository Gradle build. Attempt emulator/system-image setup only after checking virtualization/hardware acceleration and resource availability. If a wrapper is absent, generate and verify a compatible pinned wrapper before relying on it. Record exact versions, commands, results, and blockers in `BUILD_STATUS.md`; keep a reproducible setup script current. Never claim a tool or test works without evidence, and never repeatedly download components already installed and verified.

## Environment setup principles

- Inspect OS, architecture, free disk space, RAM, Java, Gradle, SDK, ADB, and virtualization support before installing anything.
- Install only missing components; avoid repeated large downloads.
- Accept Android SDK licenses only through the environment's normal documented setup process.
- Keep SDKs, Gradle caches, emulator images, local properties, signing files, and credentials out of Git.
- Never commit keystores, passwords, access tokens, or machine-specific absolute paths.
- Use environment variables or local untracked configuration for machine-specific SDK locations.
- Make setup reproducible and document exact versions and commands once verified.
- If the environment is ephemeral, expect SDK/cache downloads to need repeating. Preserve a setup script or concise bootstrap instructions, but do not claim persistence across sessions.
- Emulator setup is optional and conditional. A working SDK does not prove emulator acceleration is available.
- Keep GitHub Actions as an independent build/test environment even after local tooling is installed.

## Routine commands

Use the wrapper once it exists and is verified:

```bash
./gradlew --version
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

`connectedDebugAndroidTest` requires a running compatible emulator or connected Android device. Do not run it as if it were a host-only test.

For a lightweight local pass, run the smallest relevant static verifier or targeted unit-test task first. Use the full command set at the defined checkpoints in `TESTING_STRATEGY.md`.

## Evidence and reporting

For every verification run, record:

- exact source commit/archive or working-tree state;
- exact command and tool versions;
- pass/fail result and relevant failure summary;
- whether the test ran on host JVM, Android emulator, CI, or physical device;
- any skipped/unavailable gates and why.

Distinguish static source checks, host SQLite checks, Kotlin-only harnesses, Android Gradle/JUnit, Room instrumentation, CI, emulator, and physical-device acceptance. One category does not substitute for another.

Do not call StudyForge V3.0 accepted or release-ready while any required gate in `V3_FINAL_ACCEPTANCE_RUNBOOK.md` remains unverified or explicitly deferred by the user.

## Reproducible bootstrap script

For a network-enabled Debian/Ubuntu Linux runner with package-install permissions:

1. Obtain the official Android command-line-tools-only Linux ZIP URL from the Android Developers download page.
2. Run:

```bash
ANDROID_CMDLINE_TOOLS_ZIP_URL='https://dl.google.com/android/repository/<official-linux-command-line-tools-archive>.zip' \
  ./scripts/bootstrap_android_tools.sh
```

The script installs JDK 17 through APT if missing, downloads Gradle 8.13 and verifies its published SHA-256 checksum, installs Android SDK command-line tools plus Platform 36 / Build Tools 36.0.0 / ADB, generates the Gradle Wrapper if absent, and runs `:app:assembleDebug` as a bootstrap smoke test. It does not install or start an emulator. Review `BUILD_STATUS.md` for the last environment's actual blockers and results; never assume that a previous session's installation persisted.
