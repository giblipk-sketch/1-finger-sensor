FINGERPRINT ANIMATION — APK BUILD FIX

IMPORTANT: “There was a problem parsing the package” usually means the file you tried to install is not a valid Android APK, is incomplete/corrupt, or targets a newer Android version than the phone supports. A website that merely converts a ZIP/HTML file to APK may produce an invalid APK. Do not install the original ZIP or rename it to .apk.

THIS ZIP IS SOURCE CODE, NOT AN APK. It contains a corrected native Android project.

BUILD THE REAL APK WITH GITHUB ACTIONS
1. Extract this ZIP.
2. Create a GitHub repository and upload the CONTENTS of the android-project folder to the repository root. The root must directly contain app/, build.gradle.kts, settings.gradle.kts, gradle.properties, and .github/.
3. Open Actions → Build APK → Run workflow.
4. After a green/successful run, open the workflow run and download the FingerprintAnimation-debug-apk artifact.
5. Extract the artifact ZIP; install app-debug.apk (do not install the artifact ZIP).

Compatibility: minSdk 26 (Android 8.0+), targetSdk 35. This covers Android 12–14. If Android still rejects the genuine APK, send the exact APK filename, phone Android version, and a screenshot of the error.

Fingerprint animation is a visual simulation, not real fingerprint authentication or a replacement for the secure Android lock screen.
