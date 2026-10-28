# Fingerprint Animation (Android Starter)

A starter Android Studio project for a **simulated fingerprint animation** app. It does not authenticate fingerprints and does not replace Android's protected system lock screen.

## Included
- Three optional modes in Settings:
  - Manual app launch
  - Screen wake (best-effort notification/foreground behavior; not guaranteed to display over the system lock screen)
  - Simulated lock-screen experience inside the app
- Neon red fingerprint-style animation (drawn with simple Compose rings; replace with custom vector artwork/Lottie for production)
- Centered scanner by default
- Sound and vibration toggles
- Animation speed control
- Custom PIN gate inside the app (demo-only local PIN; replace with secure storage and proper handling before release)
- Background color selection (starter implementation uses dark background)
- Android permission for vibration
- Red neon fingerprint launcher icon (adaptive icon on Android 8.0+, PNG fallback on older Android)
- GitHub Actions workflow for debug APK and optional signed release APK

## Requirements
- Android Studio (recent stable version)
- JDK 17
- Android Gradle Plugin 8.6.1 / Gradle 8.7 or compatible Android Studio setup
- Android SDK 35

## Open and run
1. Unzip the project.
2. Open the `FingerprintAnimationStarter` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Run on an Android device or emulator.

## Important platform restrictions
- An ordinary app cannot run while the phone is completely powered off.
- Android does not let a regular third-party app replace or dismiss the secure system keyguard just by showing an animation.
- Screen-wake mode is intentionally represented as a setting and informational explanation in this starter. Reliable always-on lock-screen overlays are not guaranteed and may be blocked by Android/OEM power-management policies.
- For real biometrics, use AndroidX BiometricPrompt and the device's supported biometric hardware/credentials. That is separate from this simulated animation.
- This project does not collect or send fingerprint data.

## Build a debug APK with GitHub Actions (no Android Studio needed on your phone)
1. Create a GitHub repository and upload the contents of this project folder (not the outer ZIP folder).
2. Open **Actions** and enable workflows if prompted.
3. Select **Build APK** → **Run workflow**. A push to `main` or `master` also starts it automatically.
4. When it finishes, open the run and download the `FingerprintAnimation-debug-apk` artifact.

## Configure a signed release APK
The workflow always builds and uploads the debug APK. It builds and uploads a signed release APK only after all four GitHub Actions repository secrets are configured:

- `ANDROID_KEYSTORE_BASE64` — your release keystore file encoded as Base64
- `ANDROID_KEYSTORE_PASSWORD` — keystore password
- `ANDROID_KEY_ALIAS` — key alias
- `ANDROID_KEY_PASSWORD` — key password

Create a keystore on a trusted computer with Android Studio's **Build → Generate Signed Bundle / APK** wizard, or use `keytool`. Keep the `.jks` file and all passwords private; back them up securely. Encode the keystore as Base64 (for example, on Linux/macOS: `base64 -w 0 release-keystore.jks`; on macOS, `base64 release-keystore.jks` also works). Add the resulting string and passwords in GitHub: **Settings → Secrets and variables → Actions → New repository secret**. Never commit the keystore or passwords to the repository. Run the workflow again; the signed artifact is named `FingerprintAnimation-signed-release-apk`.

**Important:** Losing the signing key can prevent you from updating an already distributed app under the same application ID. Use one persistent key for all future releases. Do not use a debug key for public distribution.

## Local Android Studio release build
In Android Studio choose **Build → Generate Signed Bundle / APK → APK**, select/create your keystore, and complete the wizard. The signed APK is typically under `app/release/` (or the output path shown by Android Studio). The Gradle environment-variable configuration in `app/build.gradle.kts` can also be used with the same four environment variables, where `ANDROID_KEYSTORE_PATH` points to the keystore file.

## Production checklist
- Replace demo PIN storage with Android Keystore-backed or server-backed secure authentication; do not store a real PIN in plain text.
- Add a privacy policy and clear disclosure that unlock is simulated.
- Add adaptive launcher icons, accessibility labels, localization, and tests.
- If adding background services, comply with Android foreground-service and notification rules; don't attempt to bypass the system lock screen.
