#!/usr/bin/env bash
# Reproducible Android toolchain bootstrap for StudyForge.
# Intended for a network-enabled Debian/Ubuntu Linux development runner.
# Does not install or start an emulator; that requires working KVM acceleration.
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-${HOME}/Android/Sdk}"
GRADLE_VERSION="8.13"
GRADLE_HOME="${GRADLE_HOME:-/opt/gradle/gradle-${GRADLE_VERSION}}"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

fail() { echo "ERROR: $*" >&2; exit 1; }
need_cmd() { command -v "$1" >/dev/null 2>&1 || fail "Required command missing: $1"; }

[[ "$(uname -s)" == "Linux" ]] || fail "This bootstrap currently supports Linux only."
[[ -f "$ROOT_DIR/settings.gradle.kts" && -f "$ROOT_DIR/app/build.gradle.kts" ]] || fail "Run this script from the StudyForge project tree."

for c in curl unzip sha256sum; do need_cmd "$c"; done

if ! command -v javac >/dev/null 2>&1 || ! javac -version 2>&1 | grep -q '17\.'; then
  if command -v apt-get >/dev/null 2>&1; then
    [[ "$(id -u)" -eq 0 ]] || fail "JDK 17 is missing; rerun with permissions to install packages (e.g. sudo)."
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jdk ca-certificates curl unzip
  else
    fail "JDK 17 is required. Install it using your OS package manager, then rerun."
  fi
fi

JAVAC_PATH="$(readlink -f "$(command -v javac)")"
JAVA_HOME="$(dirname "$(dirname "$JAVAC_PATH")")"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
javac -version 2>&1 | grep -q '17\.' || fail "Selected javac is not JDK 17: $(javac -version 2>&1)"

# Install a pinned Gradle distribution and verify it against Gradle's published checksum.
if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "$TMP_DIR"' EXIT
  curl --fail --location --retry 3 "$GRADLE_DIST_URL" -o "$TMP_DIR/gradle.zip"
  curl --fail --location --retry 3 "${GRADLE_DIST_URL}.sha256" -o "$TMP_DIR/gradle.zip.sha256"
  EXPECTED="$(tr -d '[:space:]' < "$TMP_DIR/gradle.zip.sha256")"
  ACTUAL="$(sha256sum "$TMP_DIR/gradle.zip" | awk '{print $1}')"
  [[ "$EXPECTED" == "$ACTUAL" ]] || fail "Gradle distribution SHA-256 mismatch."
  mkdir -p "$(dirname "$GRADLE_HOME")"
  unzip -q "$TMP_DIR/gradle.zip" -d "$(dirname "$GRADLE_HOME")"
fi
export GRADLE_HOME
export PATH="$GRADLE_HOME/bin:$PATH"
gradle --version

# Google changes command-line-tools package revisions. Supply the official
# command-line-tools-only ZIP URL explicitly rather than silently guessing a revision.
if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
  : "${ANDROID_CMDLINE_TOOLS_ZIP_URL:?Set ANDROID_CMDLINE_TOOLS_ZIP_URL to the official Google command-line-tools-only Linux ZIP URL.}"
  [[ "$ANDROID_CMDLINE_TOOLS_ZIP_URL" == https://dl.google.com/android/repository/* ]] || fail "SDK tools URL must be an official dl.google.com Android repository URL."
  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "$TMP_DIR"' EXIT
  curl --fail --location --retry 3 "$ANDROID_CMDLINE_TOOLS_ZIP_URL" -o "$TMP_DIR/cmdline-tools.zip"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  unzip -q "$TMP_DIR/cmdline-tools.zip" -d "$TMP_DIR/unpacked"
  [[ -d "$TMP_DIR/unpacked/cmdline-tools" ]] || fail "Unexpected Android command-line tools archive layout."
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$TMP_DIR/unpacked/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
[[ -x "$SDKMANAGER" ]] || fail "sdkmanager not found after installation."

# Platform 36 matches compileSdk. Build Tools 36.0.0 is pinned for repeatability.
set +o pipefail
yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" --licenses >/dev/null
set -o pipefail
"$SDKMANAGER" --sdk_root="$ANDROID_HOME" \
  "platform-tools" \
  "platforms;android-36" \
  "build-tools;36.0.0"

# Generate the wrapper from the pinned Gradle distribution if the source ZIP omitted it.
cd "$ROOT_DIR"
if [[ ! -f gradle/wrapper/gradle-wrapper.properties ]]; then
  gradle wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin
fi
[[ -f gradle/wrapper/gradle-wrapper.jar && -f gradlew ]] || fail "Gradle Wrapper generation did not produce expected files."
chmod +x gradlew
./gradlew --version

# A debug build is the bootstrap smoke test; milestone/full tests follow TESTING_STRATEGY.md.
./gradlew --no-daemon :app:assembleDebug

cat <<REPORT

StudyForge Android toolchain bootstrap completed.
JAVA_HOME=$JAVA_HOME
GRADLE_HOME=$GRADLE_HOME
ANDROID_HOME=$ANDROID_HOME
Verified: JDK 17, Gradle $GRADLE_VERSION wrapper, SDK Platform 36, Build Tools 36.0.0, ADB, debug APK build.
Emulator intentionally not installed/launched by this script. Check /dev/kvm and available resources first.
REPORT
