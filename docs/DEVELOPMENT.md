# Development and GitHub setup

## Repository layout

```text
.github/workflows/tests.yml       Automated parser/calculation checks
app/src/main/AndroidManifest.xml  Android requirements and components
app/src/main/java/in/local/liveplus/
  Engine.java                    Parsing, rewards and period calculations
  Store.java                     Local database and merchant rules
  AlertListener.java             Notification capture
  MainActivity.java              Native Android interface
app/src/main/res/drawable/        Launcher artwork
tests/EngineTest.java             Synthetic test cases
tools/Compile.java               One-shot Android Java compiler helper
tools/test.ps1                   Standalone tests, no Android SDK required
docs/screenshots/                Reviewed, redacted phone screenshots
build.ps1                        Windows APK build/sign/verify
.signing/                        LOCAL ONLY: private update-signing material
build/                           LOCAL ONLY: generated files and APK
```

## Prerequisites

- Windows PowerShell for APK builds.
- JDK 17: [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download) or [Eclipse Temurin](https://adoptium.net/).
- [Android SDK](https://developer.android.com/studio) Platform 35 and Build-Tools 35.0.0, including aapt2, zipalign, d8.jar, apksigner.jar and core-lambda-stubs.jar.

Compilers and SDK packages are not vendored. No Gradle wrapper, Android Studio or emulator is required for the command-line build.

## Build

From the repository root:

```powershell
./build.ps1 `
  -JavaHome 'C:/path/to/jdk-17' `
  -AndroidJar 'C:/path/to/Android/Sdk/platforms/android-35/android.jar' `
  -BuildTools 'C:/path/to/Android/Sdk/build-tools/35.0.0'
```

Alternatively set `JAVA_HOME` and `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and run `./build.ps1`. Explicit parameters take priority. Optional `-Output` sets another APK path.

An optional ignored `build.local.ps1` wrapper can point to locally installed tools. Keep machine-specific paths out of Git.

The build runs tests, compiles resources/Java, converts classes with D8, aligns, signs and verifies the APK. Output: `build/LivePlus-Ledger.apk`. Each build uses a fresh intermediate subdirectory to avoid stale classes.

`tools/Compile.java` owns the file manager for a one-shot compiler process, avoiding a Windows restricted-token ZipFS close issue. Compiler diagnostics and failed compilation still fail the build.

## Tests

```powershell
./tools/test.ps1 -JavaHome 'C:/path/to/jdk-17'
```

Tests need a JDK only. The script also works with PowerShell 7 on Linux/macOS. It uses `JAVA_HOME` or Java on `PATH` if `-JavaHome` is omitted.

Cases cover parsing, card filtering, OTP/decline/payment rejection, suggestions, promotion expiry, cap calculations, period boundaries, duplicates and refunds. They do not prove device notification capture or UI behavior. The ordinary test workflow runs this script without signing secrets. A separate tag-triggered workflow publishes signed APK releases; see [release setup](RELEASING.md).

## Signing and updates

The first build creates `.signing/personal-release.p12` and its local password file if no key exists. The original installed app's signing key was preserved when this project was moved.

Keep a private backup of the complete `.signing/` folder. Future updates to the installed app need its existing signing identity. A fresh clone generates a different key; export app data before uninstalling a differently signed older build.

Never commit keys, passwords, real transaction exports or phone databases. `.gitignore` excludes these. Compiled APKs are also excluded; use reviewed release attachments when ready to distribute one.

## GitHub repository

The repository is hosted at https://github.com/chetanatole/hsbc-liveplus-ledger. The local `main` branch preserves the initial MIT-license commit from the original `master` branch. The working checkout uses the owner's existing GitHub SSH authentication.

For later changes, inspect the diff, run the relevant checks, commit and push:

```powershell
git status
git diff
git add .
git diff --cached --stat
git diff --cached --check
git commit -m "Describe the change"
git push
```

Keep signing material and personal exports excluded. See [release setup](RELEASING.md) for downloadable APK publishing.

The project uses the [MIT License](../LICENSE), with copyright attributed to Chetan Atole.
