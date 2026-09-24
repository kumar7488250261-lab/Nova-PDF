# PDFNova

[![PDFNova APK Build](https://github.com/OWNER/REPOSITORY/actions/workflows/build-apk.yml/badge.svg)](https://github.com/OWNER/REPOSITORY/actions/workflows/build-apk.yml)

**PDFNova** — The modern, privacy-first, professional all-in-one PDF utility studio for Android. Built with Jetpack Compose, Kotlin Coroutines, Material 3, and Room.

---

## Build APK

The Android APK builds automatically on GitHub via GitHub Actions whenever changes are pushed to `main` or triggered manually.

### How to download the built APK:
1. Push code to `main` (or run manually via **Run workflow**).
2. Open your repository on **GitHub**.
3. Go to the **Actions** tab.
4. Select the latest run under **PDFNova APK Build**.
5. Under **Artifacts**, download **`PDFNova-debug-apk`**.
6. Extract the zip archive to retrieve **`PDFNova-debug.apk`** and install it on your Android device.

---

## Local Development & Build

### Prerequisites
- JDK 17 (or JDK 21)
- Android SDK (API 36, minimum API 24)

### Build Debug APK
```bash
./gradlew assembleDebug --stacktrace
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Run Unit & Robolectric Tests
```bash
./gradlew testDebugUnitTest
```

---

## Release & Google Play Publishing

For production release signing instructions and generating Google Play Android App Bundles (`.aab`), see [RELEASE_SETUP.md](RELEASE_SETUP.md).
