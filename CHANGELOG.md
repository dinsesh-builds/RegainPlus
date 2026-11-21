# Changelog

All notable changes to RegainPlus are documented here.

## [Unreleased]

### Added
- Contribution, architecture, security, and code-of-conduct documentation.
- GitHub CI workflow and issue/PR templates for collaboration and maintenance.
- Additional unit tests for alarm scheduling data serialization.

### Changed
- Improved app startup performance by moving non-critical backup/scheduling work off the main thread.
- Unified Room database singleton usage across DI and direct access paths.
- Enabled R8 minification and resource shrinking for release builds.
- Updated version metadata to `2.0.0` (`versionCode` 2) in preparation for ongoing releases.
- Screen time stats now load off the main thread with a loading state.

### Fixed
- Potential duplicate database instance usage between Hilt-provided and manually accessed database singletons.
- Blocked-item add/delete now updates the correct block/site/study holder state.
- Accessibility service now recycles accessibility nodes to reduce leak/perf risk during blocking checks.
- Added required `specialUse` foreground service subtype properties for Android 14+ manifest compliance.

### Added
- Continuous alarm ringing service with dismiss action for scheduled focus alarms.
- Curated in-app educational YouTube resources (allowlisted catalog and restricted playback).
- Initial iOS app scaffold (SwiftUI) in `ios/`.
- Versioned downloadable artifacts in `releases/` and git tags for Android/iOS versions.
- Project documentation for architecture, contributing, and CI workflow.

### Changed
- Improved alarm reliability (alarm clock scheduling) and 12/24-hour time display handling.
- Updated home widget colors to match the app theme.
- UI/UX polish across onboarding, home/schedule, study, block, wardrobe, and dark theme surfaces.
- App startup work reduced by moving non-critical backup/scheduling tasks off the main thread.
- Release builds now use R8 minification/shrinking; app version bumped to `2.0.0`.

### Fixed
- Overlapping alarm sounds.
- Schedule time display issues in 12-hour format.
- Several layout/alignment inconsistencies in schedule and related screens.

## [1.0.0] - 2026-11-19

### Added
- Initial Android release of RegainPlus (offline-first, on-device).
- Focus/blocking, study tools, body tracking, wardrobe, skills, mind tools, dashboard, settings/backup, and widget.
- Initial project README and license.