# Planning & Roadmap

This document tracks current priorities and near-term planning for RegainPlus. It is intentionally lightweight and should evolve as the project grows.

## Current focus

- **Reliability:** alarms, reminders, accessibility/overlay behavior, and permission flows across devices/OEMs.
- **Performance & battery:** minimize background work/polling when features are off; keep startup and UI responsive.
- **UX quality:** consistent layouts, clear permission/onboarding flows, accessible controls, and predictable settings behavior.
- **Maintainability:** improve tests, documentation, and code organization for safer iteration.

## Near-term priorities

### Architecture & maintenance
- [ ] Continue reducing large/complex screens into smaller composables and clearer state boundaries.
- [ ] Add more unit tests for core logic (scheduling, blocking rules, data transformations).
- [ ] Introduce instrumentation/UI smoke tests for critical flows (onboarding, alarms, blocking toggles).
- [ ] Review Room schema/migration strategy to avoid destructive migrations for user data.

### Performance & scaling
- [ ] Profile startup and background services on low/mid-range devices.
- [ ] Audit accessibility/monitoring loops for unnecessary work and redundant state reads.
- [ ] Optimize Compose recomposition hotspots in large screens (e.g. Home/Study/Block).
- [ ] Keep release builds lean and verify R8/proguard behavior for key flows.

### Product & UX
- [ ] Improve alarm setup/diagnostics (permissions, OEM restrictions, test alarm flow).
- [ ] Refine curated educational content management and playback UX.
- [ ] Continue dark/light theme consistency and accessibility improvements.

### Collaboration
- [ ] Keep `CHANGELOG.md`, docs, and issue/PR templates up to date.
- [ ] Use CI results to gate regressions (tests/build) on contributions.
- [ ] Define release process/checklist for versioning, tags, and downloadable artifacts.

## Release process (suggested)

1. Update version in `app/build.gradle.kts` (`versionCode`/`versionName`).
2. Verify: `gradle :app:testDebugUnitTest :app:assembleDebug` (and release build when applicable).
3. Update `CHANGELOG.md` and docs.
4. Commit, tag, and push.
5. Build/upload versioned artifacts (APK/source bundles) and update release notes.