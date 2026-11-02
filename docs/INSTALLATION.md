# Installation

## Install the APK

1. Download `LivePlus-Ledger.apk` from [GitHub Releases](https://github.com/chetanatole/hsbc-liveplus-ledger/releases), or [build it from source](DEVELOPMENT.md).
2. Open the APK on an Android 8.0+ phone.
3. If prompted, allow installation from the browser/file manager used to open it.
4. Open **Live+ Ledger → Settings**.

## Play Protect installation block

Play Protect may display:

> App blocked to protect your device
>
> This app can request access to sensitive data. This can increase the risk of identity theft or financial fraud.

Google documents this block for sideloaded apps requesting sensitive access, including notification listeners. Live+ Ledger uses a notification listener to capture transaction alerts. [Google's warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance)

### Temporary installation workaround

If you choose to temporarily disable scanning to install an APK you trust:

1. Open **Google Play Store → profile icon → Play Protect → Settings**.
2. Turn **Scan apps with Play Protect** off, if the device permits it.
3. Install `LivePlus-Ledger.apk`.
4. **Immediately turn Scan apps with Play Protect back on**, whether installation succeeds or fails.
5. Run a Play Protect scan.

Disabling scanning reduces protection. Google recommends keeping Play Protect enabled. Do not use this workaround for a **Harmful app blocked** detection. Menu labels and available controls vary by device and policy. [Google's Play Protect instructions](https://support.google.com/android/answer/2812853?hl=en)

## Configure the card

1. Enter **only your card's last four digits**.
2. Set the **statement closing day** (1–31), or `0` for a calendar month. Do not use the payment due date.
3. Check the accelerated cashback cap against your card's terms.
4. Select your SMS app and any banking app that sends transaction notifications.
5. Enable **Capture new transaction alerts** and tap **Save settings**.
6. Tap **Enable Android notification access** and enable **Live+ Ledger** in Android Settings.

If Android restricts notification access, open **Settings → Apps → Live+ Ledger → ⋮ → Allow restricted settings**, then try granting access again. This setting is separate from the installation-time Play Protect block.

Read [PRIVACY.md](../PRIVACY.md) before granting notification access.

## Using the ledger

- **Activity → Add purchase** opens the purchase editor. Tap the date to choose it from the calendar; the category list shows the estimated cashback rate.
- **Paste alert** previews a transaction alert before importing it. Check for an existing purchase to avoid duplicates.
- **Needs review** highlights the number of items needing attention in the selected period. Tap it to filter the list; tap it again to show all transactions. These controls remain visible while the transaction list scrolls.
- In the purchase editor, **Remember this merchant’s category** is checked when a saved rule matches the displayed category. Check and save to create or update the rule; uncheck and save to forget it. The rule applies to future captured alerts and pasted purchases for that merchant and does not recategorize other existing transactions. Cancel leaves the rule unchanged.
- **Settings → Merchant categories → Manage merchant categories** shows your remembered and manually added mappings. Add a mapping or tap one to edit or delete it. Saved mappings override built-in suggestions; existing transactions keep their categories. See [merchant category matching](MERCHANTS.md).
- **Settings → Appearance** offers **Light** and **Dark**, with Light as the default. The choice is saved automatically.

## Notification troubleshooting

- Keep transaction notifications enabled with their text available to notification listeners.
- Only new alerts after capture is enabled are processed. Historical SMS messages are not read.
- Check the selected source apps and card suffix if a transaction is missed.
- Review battery/background settings if the phone suspends capture.
- Review amount, date, merchant and category in Activity. Merchant suggestions cannot determine the bank's actual MCC.
- Add a missed purchase manually or preview a pasted alert. Check for an existing purchase before importing.

## Updates and changing phones

Install an update APK over the existing app to retain local transactions and settings. Export a JSON backup before uninstalling or changing phones. Updates require the same signing key. A differently signed APK may require uninstalling the existing app, which removes its local data.

Restore requires an empty ledger and leaves capture off until reconfigured. Exports include transactions and card/reward settings; appearance, notification source selection and remembered merchant rules are not included.
