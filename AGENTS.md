# Repository Guidelines — RegainPlus

## Product scope

RegainPlus is a personal, offline-first app for focus/blocking, study, body tracking, wardrobe, skills, and mind tools.

- **Privacy-first:** on-device data by default, no accounts/cloud/services required for core features.
- **Personal use:** not a commercial product; do not add pro/paywall/account flows unless explicitly requested.
- **Safety/consent:** do not build or enable features intended to monitor or control other people’s devices without consent.

## Engineering principles

- **Original work only:** do not copy code from other apps/projects. Reimplement ideas originally.
- **Preserve user data:** be careful with Room schema changes, migrations, backups, and export/import behavior.
- **Reliability over cleverness:** blocking, alarms, notifications, accessibility, overlays, and background work must behave predictably and degrade gracefully on OEM/device differences.
- **Performance-aware:** avoid unnecessary wakeups/polling/services when features are disabled; keep startup and UI responsive.
- **Additive changes:** prefer safe, incremental improvements with verification.

## Architecture & code organization

- **Android app module:** `:app` (Kotlin, Jetpack Compose, Hilt, Room, WorkManager/DataStore/Glance).
- **Key packages:** `ui/`, `ui/viewmodel/`, `data/`, `di/`, `service/`, `receiver/`, `worker/`, `util/`, `widget/`.
- **State/integration:** some runtime coordination uses holders/services/receivers for system-level behavior; keep responsibilities clear and avoid duplicate sources of truth.
- See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and [`docs/PLANNING.md`](docs/PLANNING.md) for details.

## Development workflow

- **Toolchain:** JDK 17+ (21 recommended), Android SDK (platform/build-tools 34), Gradle 8.9.
- **Build/test:**
  - `gradle :app:assembleDebug`
  - `gradle :app:testDebugUnitTest`
- **Verify changes:** run relevant tests/builds and, for runtime/system features, test on a real device when possible.
- **Docs:** update `README.md`, `CHANGELOG.md`, and docs when behavior, setup, or architecture changes.

## Collaboration

- Contributions are welcome; a human must review and own the final diff.
- Keep PRs focused, explain intent, and include verification notes.
- Follow [`CONTRIBUTING.md`](CONTRIBUTING.md), [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md), and [`SECURITY.md`](SECURITY.md).