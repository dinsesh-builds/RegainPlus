# RegainPlus

RegainPlus is a personal, offline-first app I’m building to keep focus, study, gym, wardrobe, skills, and a few mental-health tools in one place. The main app today is **Android** (on-device, local data, no account/cloud setup). This repo now also includes an **initial iOS version** (SwiftUI scaffold) in [`ios/`](ios/).

This repo is shared as a study/open-source project. You’re welcome to read the code, try it, and change it for your own use.

## What it does

- **Focus & blocking:** app/website blocking, study mode (allowlist), short-form blocking (Shorts/Reels-style surfaces), focus timer, schedules, and streak tracking. Blocking uses Android accessibility/overlay/usage-access permissions where needed.
- **Study:** focus sessions, subjects/notes, study goals, tasks, flashcards (spaced repetition), and saved resources.
- **Body:** gym logging (sets/reps/weight, PRs/history), calories/macros, water, weight tracking, and progress photos.
- **Wardrobe:** closet items, outfits, capsules, “dress me” suggestions, style references, and declutter tracking.
- **Skills:** track a few active skills, log practice time, and keep simple self-ratings.
- **Mind:** mood check-ins, breathing (4-7-8 with animation/countdown), grounding, and practical notes on sleep/boundaries.
- **Home & tools:** daily dashboard, customizable schedule slots with alarms, weekly view, widget, backup/restore (JSON), and theme settings.

Not everything is perfect, and behavior can vary by device/OEM (especially accessibility, background limits, and overlays on MIUI and similar skins).

## Platforms

### Android
- Kotlin + Jetpack Compose (Material 3)
- Room, Hilt, Navigation Compose, WorkManager, DataStore, Glance (widget)
- Accessibility/overlay/usage-access based focus & blocking features (device/OEM behavior varies)
- Target/compile SDK 34, min SDK 26
- Gradle 8.9, JDK 17+ (project has been built with JDK 21)

### iOS (initial)
- SwiftUI app scaffold in `ios/RegainPlusIOS/` with core sections and RegainPlus branding
- Early version / work in progress (not feature-parity with Android; some Android-only capabilities need iOS-specific implementations)
- See [`ios/README.md`](ios/README.md) for setup notes

## Build (Android)

You’ll need Android SDK (platform 34, build-tools 34.0.0), JDK 17+ (21 recommended), and Gradle 8.9.

```bash
# debug APK
gradle :app:assembleDebug

# release APK
gradle :app:assembleRelease
```

Install to a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` is not included — point it to your local Android SDK path.

## iOS

See [`ios/README.md`](ios/README.md) for the initial iOS version and Xcode setup notes.

## Versions & downloads

Source history and version tags are kept in this repo (older versions are not removed/replaced).

| Version | Platform | Tag | Notes | Download |
|---|---|---|---|---|
| v1.0.0 | Android (initial) | `android-v1.0.0` (`db2db4c`) | First Android app version | [`releases/RegainPlus-android-v1.0.0-debug.apk`](releases/RegainPlus-android-v1.0.0-debug.apk) |
| v2.0.0 | Android (updated) | `android-v2.0.0` (`4aa32de`) | Alarm/widget fixes, UI improvements, curated study resources, and more | [`releases/RegainPlus-android-v2.0.0-debug.apk`](releases/RegainPlus-android-v2.0.0-debug.apk) |
| v1.0.0 | iOS (initial) | `ios-v1.0.0` (`4aa32de`) | Initial SwiftUI source version (Xcode/macOS needed to build) | [`releases/RegainPlus-ios-v1.0.0-source.zip`](releases/RegainPlus-ios-v1.0.0-source.zip) |

> If GitHub Releases are created for these tags, the same files can be attached there for easier downloads.

## Project docs

- [CHANGELOG.md](CHANGELOG.md) – release/version history
- [CONTRIBUTING.md](CONTRIBUTING.md) – development and contribution guidelines
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) – architecture and maintenance notes
- CI: [Android workflow](.github/workflows/android-ci.yml) builds and runs unit tests on push/PR to `main`

## Permissions & privacy

Depending on what you enable, the app may ask for accessibility access, display-over-other-apps, usage access, notification access, notification posting, alarms, or battery optimization exemption. These are used for on-device features (blocking, timers, reminders, stats). Data is kept locally by default; export/import is manual.

## License

This project is shared for personal use, study, and modification. Commercial use is not allowed without prior written permission, credit to **RegainPlus**, and any royalty terms agreed in writing. See [LICENSE](LICENSE) for details.

## Notes

- Personal project, not a commercial product.
- Don’t use it to monitor or control other people’s devices without their consent.
- If you fork it, please keep attribution to RegainPlus and don’t present your fork as the original project.
