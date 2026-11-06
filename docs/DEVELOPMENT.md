# Development

## Requirements

- Windows PowerShell for APK builds.
- JDK 17: [Microsoft OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download) or [Eclipse Temurin](https://adoptium.net/).
- [Android SDK](https://developer.android.com/studio) Platform 35 and Build-Tools 35.0.0.

Tools must include `aapt2`, `zipalign`, `d8.jar`, `apksigner.jar` and `core-lambda-stubs.jar`. There are no third-party app dependencies. Android Studio, Gradle and an emulator are not required for compilation.

## Project layout

```text
app/src/main/AndroidManifest.xml   Shared version/app metadata; Notifications manifest
app/src/main/java/in/local/liveplus/
  Engine.java                     Parser, merchant catalog, rewards and periods
  Store.java                      Local database and merchant mappings
  CaptureStore.java               Shared persistence and edition migration
  SmsApps.java                    SMS-only notification-source discovery/migration
  NotificationCapture.java        Pure-Java notification extraction/replay state
  SmsCapture.java                 Pure-Java incoming multipart SMS parsing/identity
  SettingsValues.java             Validated automatic settings input
  MainActivity.java               Shared UI with capture-specific consent/settings
app/src/notifications/java/in/local/liveplus/
  CaptureMode.java                Notifications edition access adapter
  AlertListener.java              Listener; only in Notifications APK
app/src/sms/java/in/local/liveplus/
  CaptureMode.java                SMS edition access adapter
  SmsReceiver.java                Incoming receiver; only in SMS APK
app/src/main/res/                 Shared artwork and Light/Dark resources
tests/EngineTest.java             Parser/calculation/merchant checks
tests/NotificationCaptureTest.java Notification extraction/replay checks
tests/SmsCaptureTest.java         New SMS, multipart, OTP and event-identity checks
tests/SettingsValuesTest.java     Automatic-write validation and card-change protection
tools/Compile.java                Java compiler helper
tools/test.ps1                   SDK-free synthetic tests
tools/build-all.ps1              Both APKs and checksums
tools/verify-apk.ps1              Packaged permission/component checks
build.ps1                        Single-edition build/sign/verification
.github/workflows/tests.yml      Test automation
.github/workflows/release.yml    Two-edition release automation
docs/screenshots/                README images
```

## Build

From the repository root:

```powershell
./tools/build-all.ps1 `
  -JavaHome 'C:/path/to/jdk-17' `
  -AndroidJar 'C:/path/to/Android/Sdk/platforms/android-35/android.jar' `
  -BuildTools 'C:/path/to/Android/Sdk/build-tools/35.0.0'
```

Or set `JAVA_HOME` and `ANDROID_SDK_ROOT` (or `ANDROID_HOME`) and run `./tools/build-all.ps1`. Explicit paths take priority. Outputs: `build/LivePlus-Ledger-Notifications.apk`, `build/LivePlus-Ledger-SMS.apk` and `build/SHA256SUMS.txt`.

Build one using `./build.ps1 -Edition notifications` (default) or `./build.ps1 -Edition sms` with the same tool arguments; `-Output` changes its APK path. Each build runs synthetic tests, compiles resources/shared Java plus exactly one edition's Java, converts with D8, aligns/signs and verifies the APK. Intermediates are under `build/`.

The SMS manifest is derived from the shared Notifications manifest: source-app queries and notification service are removed; only `RECEIVE_SMS` and an exported `SMS_RECEIVED` receiver guarded by the platform's `BROADCAST_SMS` permission are added. Both editions retain the same package/version/key/database. The opposite edition's listener/receiver code is excluded.

Artifact checks enforce exact uses-permissions, delivery components, opposite-component absence in DEX and no SMS inbox URI. These packaged-artifact checks do not replace phone testing or a full security audit. Neither edition may add `READ_SMS`, `SEND_SMS` or `INTERNET` for these features.

## Capture behavior

Notifications uses one SMS-capable package discovered from SMS delivery/composition handlers, with default-SMS fallback. Legacy multi-source selections are reduced to one valid SMS app. Android can redact an OTP-containing notification; no parser can recover withheld text. UI/docs explain clearing that conversation before later alerts and importing missed purchases manually.

SMS listens only for new `SMS_RECEIVED` broadcasts while permission/capture are enabled. It assembles multipart segments from one sender, applies the strict parser and hashes sender/body/network timestamp/subscription into an event identity; raw inputs are not saved. No provider query or historical import exists. Receipt time gates new delivery; an older network timestamp can belong to a newly delivered SMS. The manifest's sender permission prevents ordinary apps from injecting fake delivery broadcasts.

The SMS permission requires installer allowlisting and user grant. OTP protections can still delay/filter messages. Test the actual GitHub/file-manager installation, permission prompt and background delivery on the target phone. Play Store distribution would need the appropriate SMS permission declaration/review. [Google Play policy](https://support.google.com/googleplay/android-developer/answer/10208820)

Edition switching preserves ledger/settings/mappings but turns capture off and clears capture fingerprint caches on first open. Same-edition updates preserve capture settings. Both editions use the same version code per release; older-code downgrades are unsupported. Restore requires an empty ledger and leaves capture off.

## Settings and presentation

Both manifests use the application label Live+ Ledger, including the notification-access service label. The installed UI displays only versionName (for example, 1.0.3). The integer versionCode stays internal for Android update ordering. The UI has no edition names, Save settings control or capture diagnostics. Temporary diagnostic reports from earlier builds are removed on open. Keep download distinctions in README/install/release docs.

Text settings are validated by SettingsValues before writes. Valid changes are debounced for 650 ms and flushed on field exit, navigation, backgrounding and state saving. Invalid drafts keep the last valid stored value. Card changes are rejected for a populated ledger. Switches, merchant-source choice and promotion preference apply immediately. Capture activation validates card inputs and explicitly requests missing access. Returning from notification Settings activates only a user-requested pending capture action. Typing cannot trigger a permission prompt. Transaction/mapping Save actions remain explicit.

## Tests

```powershell
./tools/test.ps1 -JavaHome 'C:/path/to/jdk-17'
```

Tests need a JDK, not the Android SDK. The script also works under PowerShell 7 on Linux/macOS using `JAVA_HOME` or Java on `PATH` when omitted.

Coverage includes strict parser/OTP/decline/payment/card filtering, merchants and overrides, cashback/caps, periods, refunds/review, notification layouts/replay/retry, SMS multipart assembly, redelivery identity, unrelated/OTP rejection, malformed-input bounds and settings validation boundaries. Use synthetic messages only. Android permission UI, source discovery, background broadcasts, edition-switch persistence and Samsung OTP handling require the [device checklist](../DEVICE-CHECK.md).

## Signing and contribution

If absent, the first build creates `.signing/personal-release.p12` and `.signing/key-password.txt`. Preserve them privately: all updates and both editions must use the retained signing identity. Releases additionally check it against `docs/RELEASE_SIGNING_CERTIFICATE.txt`.

`.signing/` and `build/` are Git-ignored. Never commit credentials, real messages, exports or databases. See [releasing](RELEASING.md) and [contributing](../CONTRIBUTING.md). Licensed under the [MIT License](../LICENSE).