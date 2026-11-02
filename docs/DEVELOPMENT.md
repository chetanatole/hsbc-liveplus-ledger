# Development

## Requirements

- Windows PowerShell for APK builds.
- JDK 17: [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download) or [Eclipse Temurin](https://adoptium.net/).
- [Android SDK](https://developer.android.com/studio) Platform 35 and Build-Tools 35.0.0.

The build tools must include `aapt2`, `zipalign`, `d8.jar`, `apksigner.jar` and `core-lambda-stubs.jar`. The app has no third-party dependencies. SDK packages, Android Studio and Gradle are not included in the repository.

## Project layout

```text
app/src/main/AndroidManifest.xml  Android requirements and components
app/src/main/java/in/local/liveplus/
  Engine.java                    Parsing, rewards and period calculations
  Store.java                     Local database and merchant rules
  AlertListener.java             Notification capture
  MainActivity.java              Native Android interface
app/src/main/res/drawable/        Launcher artwork
app/src/main/res/values/          Light/dark themes and calendar styling
tests/EngineTest.java             Parser and calculation tests
tools/Compile.java               Java compilation helper
tools/test.ps1                   Standalone test command
build.ps1                        APK build, signing and verification
.github/workflows/tests.yml       Test automation
.github/workflows/release.yml     APK release automation
docs/screenshots/                README screenshots
```

## Build

From the repository root:

```powershell
./build.ps1 `
  -JavaHome 'C:/path/to/jdk-17' `
  -AndroidJar 'C:/path/to/Android/Sdk/platforms/android-35/android.jar' `
  -BuildTools 'C:/path/to/Android/Sdk/build-tools/35.0.0'
```

Alternatively set `JAVA_HOME` and `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and run `./build.ps1`. Explicit parameters take priority. Use `-Output` to select another APK path.

The script runs tests, compiles resources and Java, converts classes with D8, aligns the APK, signs it and verifies the result. Output: `build/LivePlus-Ledger.apk`. Build intermediates are written under `build/`.

## Tests

```powershell
./tools/test.ps1 -JavaHome 'C:/path/to/jdk-17'
```

Tests require a JDK but not the Android SDK. The script also works under PowerShell 7 on Linux/macOS and uses `JAVA_HOME` or Java on `PATH` when `-JavaHome` is omitted.

Coverage includes parsing, card filtering, OTP/decline/payment rejection, category suggestions, promotion expiry, caps, period boundaries, duplicates, refunds and review counts. Use the [device checklist](../DEVICE-CHECK.md) for Android-specific behavior.

## Signing

If no signing key exists, the first build creates `.signing/personal-release.p12` and `.signing/key-password.txt`. Preserve these privately: Android updates require the same signing identity.

The `.signing/` and `build/` directories are ignored by Git. Do not commit keys, passwords, personal exports or databases.

See [APK releases](RELEASING.md) to configure release signing and publish a downloadable build.

## Contributing

Follow [CONTRIBUTING.md](../CONTRIBUTING.md). Keep generated files and private data out of commits. The project is licensed under the [MIT License](../LICENSE).
