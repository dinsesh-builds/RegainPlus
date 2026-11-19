# Contributing to RegainPlus

Thanks for your interest in improving RegainPlus. This is a personal, offline-first project shared for learning, personal use, and collaboration.

## Ground rules

- **Keep it personal and privacy-first:** no accounts/cloud by default, no tracking/analytics additions without clear discussion, and avoid features that enable monitoring others without consent.
- **Original work only:** do not copy code from other apps/projects. If you reference ideas, reimplement them originally.
- **Safety first:** ask before risky/destructive commands or changes. Preserve user data and local evidence where possible.
- **Human-owned changes:** contributions are welcome, but a human should review, understand, and own the final diff.

## Development setup

### Prerequisites
- JDK 17+ (JDK 21 recommended)
- Android SDK (Platform 34, Build-Tools 34.0.0)
- Gradle 8.9
- Android device/emulator for runtime verification

### Build & test

```bash
# From repo root (Windows PowerShell example)
$env:JAVA_HOME = "C:\path\to\jdk-21"
$env:ANDROID_HOME = "C:\path\to\android\sdk"
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

Install debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` is machine-specific and should point to your local Android SDK path.

## Project conventions

- **Language/UI:** Kotlin + Jetpack Compose (Material 3), Hilt, Room, WorkManager, DataStore, Glance.
- **Architecture:** keep changes consistent with existing layers (`ui/`, `ui/viewmodel/`, `data/`, `service/`, `worker/`, `util/`). Prefer dependency injection for app/database dependencies; avoid introducing duplicate singletons.
- **Performance:** avoid unnecessary background work/wakeups when features are disabled; keep startup work light; be mindful of polling and battery impact.
- **UX:** prioritize clarity, accessibility, and device/OEM compatibility (especially permissions, alarms, accessibility/overlay behavior).

## Pull requests

- Keep PRs focused and explain **what** changed and **why**.
- Include verification notes (build/tests run, device tested if relevant).
- Update docs (`README.md`, `CHANGELOG.md`, `docs/ARCHITECTURE.md`) when behavior/structure changes.
- Do not commit secrets, machine-specific paths, or generated build outputs.

## Reporting issues

When filing an issue, include:
- Device model, Android version, and OEM skin (if applicable)
- App version/tag and steps to reproduce
- Expected vs actual behavior
- Relevant logs/screenshots (redact sensitive data)

See `.github/ISSUE_TEMPLATE/` for templates.