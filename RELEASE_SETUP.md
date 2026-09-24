# PDFNova — Release & Google Play Signing Setup

This guide explains how to configure production signing and build release artifacts for **PDFNova**.

---

## 1. Google Play Requirement: Android App Bundle (.aab)

> **Important**: Google Play requires new applications to be published as an **Android App Bundle (.aab)** using `bundleRelease`, rather than an APK. 
> 
> APKs are suitable for direct distribution, internal testing, and sideloading (`assembleDebug` or `assembleRelease`), while Google Play uses the App Bundle to dynamically generate and sign optimized APKs for each user's device configuration and screen density.

---

## 2. Generating a Production Upload Keystore

To create your production keystore locally:

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS
```

Safely store:
- Keystore file: `my-upload-key.jks`
- Key alias: `upload`
- Keystore password
- Key password

> ⚠️ **Never commit your `.jks` file or passwords to version control.**

---

## 3. Configuring GitHub Secrets for CI/CD

To automate release builds with GitHub Actions, encode your keystore to base64:

```bash
base64 -w 0 my-upload-key.jks > upload_keystore_base64.txt
```

In your GitHub repository:
1. Navigate to **Settings** → **Secrets and variables** → **Actions**.
2. Click **New repository secret** and add:
   - `KEYSTORE_BASE64`: The full base64 string from `upload_keystore_base64.txt`
   - `STORE_PASSWORD`: Keystore password
   - `KEY_PASSWORD`: Key password
   - `KEY_ALIAS`: `upload`

---

## 4. Building Locally

### Debug APK (Testing)
```bash
./gradlew assembleDebug
# Generated at: app/build/outputs/apk/debug/app-debug.apk
```

### Production Release App Bundle (Google Play)
```bash
export KEYSTORE_PATH="/path/to/my-upload-key.jks"
export STORE_PASSWORD="your-store-password"
export KEY_PASSWORD="your-key-password"

./gradlew bundleRelease
# Generated at: app/build/outputs/bundle/release/app-release.aab
```

### Production Release APK (Direct Sideloading)
```bash
./gradlew assembleRelease
# Generated at: app/build/outputs/apk/release/app-release.apk
```
