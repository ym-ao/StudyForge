# Build status

## Version 1.0.1 (build-script hotfix)

- Fixed the Compose BOM declaration: `platform(...)` is now called inside the Gradle `dependencies` block as `implementation(platform(...))`.
- Incremented Android `versionCode` to 2 and `versionName` to 1.0.1.
- The Gradle 8.13 lifecycle notice is a warning, not the cause of the previous failure.

The project still requires a successful GitHub Actions build to confirm dependency resolution and APK compilation.
