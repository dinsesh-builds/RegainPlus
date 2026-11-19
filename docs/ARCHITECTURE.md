# Architecture Overview

This document describes the current structure of RegainPlus and the main design decisions to help with maintenance, planning, and collaboration.

## High-level goals

- **Offline-first & on-device:** core data and behavior run locally (Room/SharedPreferences/DataStore).
- **Personal-use focus/blocking:** Android system features (Accessibility, Usage Stats, overlays, alarms, notifications) are used where required.
- **Maintainable single-module app:** one Android app module (`:app`) with clear package boundaries.

## Module & package structure

- `app/src/main/java/com/regainplus/`
  - `ui/` – Jetpack Compose screens, navigation, theme, reusable components
  - `ui/viewmodel/` – screen state and actions (Hilt-injected ViewModels)
  - `data/entity/`, `data/dao/`, `data/db/` – Room entities, DAOs, database
  - `di/` – Hilt modules (database/providers)
  - `service/` – foreground/background services, accessibility/notification listeners, overlay/blocking/alarm/timer helpers
  - `receiver/` – broadcast receivers (boot, alarms, device admin)
  - `worker/` – WorkManager workers (reminders, watchdog)
  - `util/` – shared utilities (backup, security, domain/time helpers)
  - `widget/` – Glance app widget
- `app/src/main/assets/` – bundled/static content (e.g. curated resources)
- `ios/` – initial iOS SwiftUI scaffold (separate platform implementation, early stage)

## Layers & data flow

1. **UI (Compose screens)** observes state from ViewModels/flows and dispatches user actions.
2. **ViewModels** coordinate UI state, call DAOs/repositories-like data access, and interact with app services/preferences where needed.
3. **Data layer (Room)** persists app data (`regainplus.db`) via entities/DAOs exposed through Hilt and shared singletons where appropriate.
4. **System integration layer** handles blocking, alarms, timers, notifications, widgets, and background checks via services/receivers/workers.

## Key runtime components

- **`RegainPlusApp` (Application):** app startup initialization (DB backup trigger, holder/service sync, alarm/work scheduling). Non-critical startup work is kept off the main thread where safe.
- **Database:** Room database (`AppDatabase`, schema exported to `app/schemas/`) with DAOs provided via Hilt. A shared singleton instance is used to avoid duplicate open DB handles.
- **Blocking/protection:** in-memory holders + persisted settings, `RegainAccessibilityService`, `BlockMonitorService` (UsageStats-based fallback/monitor), overlay/window/activity components, and watchdog/reminder workers.
- **Alarms/timers:** `FocusAlarmScheduler` schedules alarms; `FocusAlarmReceiver` triggers ringing; `AlarmRingingService` manages foreground ringing/notification lifecycle.
- **UI shell:** `MainActivity` + `RegainPlusTheme` + Compose Navigation (`NavGraph`) with onboarding/settings-driven theme/accent preferences.

## Performance considerations

- Avoid unnecessary background wakeups/polling when protection or reminders are disabled.
- Keep startup I/O and scheduling work off the UI thread when not required for first render.
- Prefer Flow/state-driven UI updates; watch for heavy recomposition in large screens.
- Use R8 minification/shrinking for release builds and keep ProGuard rules minimal but safe.

## Planning & maintenance notes

- **Versioning/releases:** Android `versionCode/versionName` in `app/build.gradle.kts`; tagged releases and downloadable artifacts tracked in repo (`releases/`, `CHANGELOG.md`).
- **Database migrations:** schema export is enabled; destructive migration fallback is currently configured—changes to entities/versioning should be handled carefully to protect user data.
- **Testing:** unit tests live in `app/src/test`; prioritize tests for utilities and core logic, and add instrumentation/UI tests as coverage grows.
- **Known platform constraints:** accessibility, overlay, exact alarms, background execution, and notifications vary by OEM/Android version; behavior should degrade gracefully with clear user guidance.

## Collaboration guidelines

See [CONTRIBUTING.md](../CONTRIBUTING.md) for setup, verification, and PR expectations. Keep architecture/docs updated when adding major features or changing system integration behavior.