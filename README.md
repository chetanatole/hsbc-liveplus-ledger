# Live+ Ledger

An offline Android app for tracking spending and estimated cashback on the **HSBC Live+ credit card issued in India**.

See accelerated-category spend, estimated cashback and remaining room under your configured cap. No bank login or full card number is required.

**Android 8.0+ · Native Java · On-device storage · No internet permission**

Independent project; not affiliated with HSBC. Merchant classifications, posting dates and card terms determine actual cashback.

[APK releases](https://github.com/chetanatole/hsbc-liveplus-ledger/releases) · [Installation](docs/INSTALLATION.md) · [MIT License](LICENSE)

## Download

Starting with **1.0.3**, releases provide two APKs. Choose the access method you are comfortable granting:

| Download | Access | How capture works |
| --- | --- | --- |
| **[LivePlus-Ledger-Notifications.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-Notifications.apk)** | Notification access; **no SMS permission** | Checks new notifications from one selected SMS messaging app. Choose this if you prefer not to grant SMS access. |
| **[LivePlus-Ledger-SMS.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-SMS.apk)** | `RECEIVE_SMS`; **no notification access** | Checks new incoming SMS delivered after capture is enabled. Choose this if you are comfortable granting incoming-SMS access. |

**Neither edition reads existing SMS, scans your inbox or sends messages.** The SMS edition does not request `READ_SMS`; incoming access covers all senders, with HSBC/card filtering performed locally. OTPs and unrelated messages are discarded, and only transaction details are saved. Both editions have the same ledger features and no internet permission.

Notification capture can miss purchase alerts grouped with an unread OTP because Android hides the content. Clear that conversation's notifications before later alerts, or paste a missed purchase. Direct SMS avoids notification grouping, but Android SMS permission and OTP restrictions still apply; RCS is not supported by the SMS receiver.

Both APKs appear on your phone as **Live+ Ledger**. The names above distinguish downloads only. Settings apply automatically after valid edits.

Install **one edition at a time**. Both share the app identity and signing key; a compatible edition switch preserves local data and turns capture off until you enable the new method. See [edition details](docs/EDITIONS.md) and [step-by-step installation](docs/INSTALLATION.md). Older releases use `LivePlus-Ledger.apk` for the notification-based app.

## Features

- Track accelerated spending, estimated cashback and remaining room under the cashback cap.
- View spending history by calendar month or statement period.
- Automatically capture matching HSBC purchases through your chosen Notifications or SMS edition.
- Suggest spending categories and manage your own merchant-to-category mappings.
- Review uncertain transactions and possible duplicates, and link full or partial refunds.
- Add purchases manually or import pasted transaction alerts.
- Export and restore JSON backups.
- Light and dark mode support.

## Screenshots

| Overview | Activity |
| --- | --- |
| <img src="docs/screenshots/overview-v1.0.1.jpg" width="280" alt="Overview with the statement period, estimated cashback and remaining eligible spend"> | <img src="docs/screenshots/activity-v1.0.1.jpg" width="280" alt="Activity with Add purchase, Paste alert, Needs review and cashback-rate labels"> |

## Install and set up

Download your chosen APK from a [release](https://github.com/chetanatole/hsbc-liveplus-ledger/releases), or build it from source.

1. Install the chosen edition on Android 8.0 or newer, then open it.
2. Enter only your card's last four digits and configure the reward period/cashback cap.
3. **Notifications:** choose one SMS messaging app, turn on capture and grant notification access when prompted.
4. **SMS:** turn on capture, read the disclosure and approve Android's incoming-SMS permission. No source-app selection or notification access is needed.

Automatic capture is optional. Manual entry and Paste alert work without either capture permission. See [installation and troubleshooting](docs/INSTALLATION.md) for permission denial, installer restrictions, OTP redaction and Play Protect prompts.

## Build and test

APK builds require Windows PowerShell, JDK 17, Android SDK Platform 35 and Build-Tools 35.0.0.

```powershell
./tools/build-all.ps1 `
  -JavaHome 'C:/path/to/jdk-17' `
  -AndroidJar 'C:/path/to/Android/Sdk/platforms/android-35/android.jar' `
  -BuildTools 'C:/path/to/Android/Sdk/build-tools/35.0.0'
```

Outputs: `build/LivePlus-Ledger-Notifications.apk`, `build/LivePlus-Ledger-SMS.apk` and `build/SHA256SUMS.txt`. To build one edition, use `./build.ps1 -Edition notifications` or `./build.ps1 -Edition sms` with the same tool paths. Android Studio, an emulator and Gradle are not required.

Run parser and calculation tests with a JDK:

```powershell
./tools/test.ps1 -JavaHome 'C:/path/to/jdk-17'
```

See [development](docs/DEVELOPMENT.md), [device testing](DEVICE-CHECK.md) and [APK releases](docs/RELEASING.md).

## Rewards and privacy

The default accelerated cashback cap is ₹1,200. See [reward assumptions](docs/REWARDS.md) for categories, exclusions and the applicable reference date.

Suggested categories are provisional. Unknown categories and possible duplicates are excluded until reviewed. Refunds require linking to an original purchase.

Both editions store data on the device and have no internet permission, analytics, advertising SDKs or SMS inbox access. Read [PRIVACY.md](PRIVACY.md) for each edition's access, new-SMS-only handling, stored fields, backups and deletion.

## Contributing and license

See [CONTRIBUTING.md](CONTRIBUTING.md). Licensed under the [MIT License](LICENSE).