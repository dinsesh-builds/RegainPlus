# RegainPlus iOS (initial version)

This folder adds an initial iOS version of **RegainPlus** to the same repository as the Android app.

## Status

- **Initial/scaffold SwiftUI app** with core sections and RegainPlus branding/colors.
- Not yet feature-parity with Android. Some Android capabilities (system-wide app blocking via Accessibility/overlay, usage stats monitoring, background behavior, and exact alarm/ringing flows) work differently or are limited on iOS and require platform-specific implementations and entitlements.
- Intended as a starting point to grow into a fuller iOS app while keeping Android and iOS in the same repo.

## Structure

- `RegainPlusIOS/RegainPlusIOSApp.swift` – app entry point
- `RegainPlusIOS/ContentView.swift` – main tab UI (Home, Focus, Study, Body, Wardrobe, Skills, Mind, Settings)
- `RegainPlusIOS/Theme.swift` – shared brand colors

## Run (Xcode)

1. Open Xcode on macOS and create a new **iOS App** project named `RegainPlusIOS` (SwiftUI, Swift).
2. Replace/add the generated Swift files with the files in this folder (or add this folder’s sources to the target).
3. Set deployment target as needed (iOS 16+ recommended for these SwiftUI APIs) and run on a simulator/device.

> Note: This environment did not include Xcode, so a full `.xcodeproj` is not generated here. The source files are provided as the initial iOS app version.