# Installation and phone setup

## Install the APK

1. Build `LivePlus-Ledger.apk` using [the development instructions](DEVELOPMENT.md), or obtain a build from a maintainer you trust.
2. Transfer it to your Android 8.0+ phone and open it. Allow that browser/file manager to install unknown apps if Android asks.
3. If Play Protect blocks installation, read the section below. The unknown-app installation permission and Play Protect scanning are separate settings.
4. After installation, open **Live+ Ledger → Settings**.

## Play Protect installation block

The owner encountered:

> App blocked to protect your device
>
> This app can request access to sensitive data. This can increase the risk of identity theft or financial fraud.

Google documents this warning for internet-sideloaded apps declaring sensitive access, including notification listeners. Live+ Ledger declares a notification listener for transaction capture, so this is the likely trigger. This is distinct from Google's harmful-app detection message; it is not a safety certification either. [Google's warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance)

### Owner-reported workaround: temporarily toggle scanning

The owner reported that turning Play Protect off temporarily, installing the APK, then immediately turning it back on allowed installation on their phone. This is a device-specific report, not a guaranteed fix or Google's recommended installation method.

Google recommends keeping Play Protect enabled. Disabling scanning reduces protection while it is off; review the APK's source and code before choosing this workaround. Do not use these steps in response to a specific **Harmful app blocked** detection. [Google's Play Protect instructions](https://support.google.com/android/answer/2812853?hl=en)

If you choose to reproduce the reported workaround:

1. Open **Google Play Store → profile icon → Play Protect → Settings** (gear icon).
2. Temporarily turn **Scan apps with Play Protect** off and confirm the system prompt, if your device allows it.
3. Install only the intended `LivePlus-Ledger.apk`.
4. **Immediately return and turn Scan apps with Play Protect back on**, whether installation succeeds or fails. Restore any other protection setting you changed.
5. Run a Play Protect scan. If the installed app is flagged as harmful, stop using it and investigate rather than repeatedly disabling protection.

Menu labels and available controls vary by Android version, manufacturer and device/account policy. Re-enabling scanning does not guarantee continued installation, Google approval or freedom from defects. There is no need to leave Play Protect off for normal use.

For development, Android supports installation from a computer through [ADB and USB debugging](https://developer.android.com/studio/run/device); device security checks still apply. A maintainer can review the app and [appeal an incorrect Play Protect classification](https://developers.google.com/android/play-protect/warning-dev-guidance#appeals). This project has not received Google approval.

## Configure the card

1. Enter **only your card's last four digits**. This ledger tracks one card suffix.
2. Enter the **statement closing day** (1–31), or `0` for a calendar month. Do not use the payment due date. Confirm your applicable reward period from the card terms/statement.
3. Check the cap: default ₹1,200, editable if your terms differ.
4. Choose notification sources: normally your SMS app, optionally HSBC's banking app if it sends transaction notifications.
5. Enable **Capture new transaction alerts** and tap **Save settings**.
6. Tap **Enable Android notification access**, then enable **Live+ Ledger** in Android Settings.

Some versions require **Settings → Apps → Live+ Ledger → ⋮ → Allow restricted settings** before notification access can be granted to a sideloaded app. This is separate from the installation-time block. Review [privacy notes](../PRIVACY.md) before granting access.

## Check capture

Keep transaction SMS notifications enabled with their text available to notification listeners. Only new alerts after capture is enabled are processed; the SMS inbox and historical messages are not read.

Wait for an ordinary card transaction; do not make a purchase merely for testing. Check amount, date, merchant and category in Activity. Capture depends on the notification format and Android/OEM background restrictions. Review battery/background settings if your phone suspends the listener.

Add a missed purchase manually or preview a pasted alert locally; check for an existing purchase first. Follow [the device checklist](../DEVICE-CHECK.md). Live capture has not yet been confirmed by the owner.

## Updates and changing phones

Export a JSON backup before uninstalling or changing phones. Updates need the retained signing identity. Differently signed APKs generally require removing the old app first, deleting its local storage. Restore requires an empty ledger and leaves capture off until configured again.
