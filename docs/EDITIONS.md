# Choose your capture edition

Live+ Ledger has two APK editions. Both appear on the phone as **Live+ Ledger**, without edition labels. Download names distinguish the access methods. Choose the access method you are comfortable granting; tracking, cashback estimates, merchant mappings, review, manual entry, Paste alert and backups work in both.

| | Notifications edition | SMS edition |
| --- | --- | --- |
| APK | [LivePlus-Ledger-Notifications.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-Notifications.apk) | [LivePlus-Ledger-SMS.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-SMS.apk) |
| Access requested | Android notification access | Android `RECEIVE_SMS` permission |
| Source | New notifications from one selected SMS messaging app | New incoming SMS delivered by Android while capture is enabled |
| Reads existing SMS / scans inbox | **No** | **No** |
| Requests `READ_SMS` / sends SMS | **No / No** | **No / No** |
| Needs notification access | Yes | **No** |
| App selection | One installed SMS-capable messaging app; default preselected when available | None; your usual SMS app stays unchanged |
| Bank-app notifications | Not monitored | Not monitored |
| Internet / uploads | None | None |

## If you prefer not to grant SMS permission

Choose **Notifications**. It requests no SMS permission and cannot query your stored messages. Notification access is itself broad: Android can deliver notifications from many apps, but Live+ Ledger filters to the one selected SMS app before inspecting content, then checks HSBC and your saved card suffix. It saves transaction details and discards other content.

Android 15+ may hide notification content from notification listeners when an OTP is detected. A later purchase can be hidden while it is grouped with that unread OTP. Clear notifications for that conversation before subsequent alerts, keep transaction notification text enabled, and use Paste alert for an already missed purchase after checking for an existing entry. Clearing notifications does not recover a missed purchase automatically. This is an Android content restriction, not a parser or battery setting. [Android OTP redaction](https://developer.android.com/about/versions/15/behavior-changes-all#otp-redaction)

## If you are comfortable granting new-SMS access

Choose **SMS**. After you enable capture and approve the permission, Android delivers new incoming SMS directly to the receiver. Android's permission covers incoming SMS from all senders; the app's HSBC/card filtering happens locally after receipt. OTPs, unrelated messages and failed-transaction patterns are discarded. Only recognized transaction fields and a hashed event identity are saved.

**There is no `READ_SMS` permission, inbox query, existing-message scan, historical import, SMS sending or notification listener in this edition.** It does not become your default SMS app. Opening the app or granting permission does not import messages already stored on the phone. Capture off or permission denied means incoming messages are not processed. Manual entry and Paste alert still work without automatic capture access.

Direct SMS delivery avoids grouping purchase and OTP notifications into one unread conversation. It does not remove Android's SMS restrictions: the installer must allowlist the restricted `RECEIVE_SMS` permission before the user can grant it. Depending on Android/message format/target SDK, OTP-bearing SMS broadcasts can be delayed or filtered; the app ignores OTPs even when delivered. Ordinary transaction SMS are expected to be separate, but phone testing is required. This receiver does not receive RCS, MMS, chat-app messages or bank-app push notifications. [SMS permission](https://developer.android.com/reference/android/Manifest.permission#RECEIVE_SMS), [Android 17 SMS protection](https://developer.android.com/about/versions/17/behavior-changes-all#sms-otp-protection)

## Settings

Settings apply automatically. Switches and SMS-app selection take effect immediately. Valid card/period/cap edits apply after a short pause or when leaving the field. Invalid input shows an inline error and keeps the previous valid value. There is no Save settings button. Enabling capture still requires its access permission and valid card settings.

## Switching editions and updates

Both editions use `in.local.liveplus`, the same release version/code and the same signing identity. Only one can be installed at a time. Install the other edition from the same or a newer compatible release over the existing app; do not uninstall. Transactions, settings and merchant mappings stay on the device. Capture is disabled on an edition change and must be explicitly enabled again with the new access method. Old capture fingerprint caches are cleared on the switch.

Export a backup first. JSON backups include transactions and card/reward settings, but not merchant mappings, theme or source selection; installing a compatible update preserves those local values without a backup round trip. Android normally prevents downgrades to a lower version code. If an installer asks you to uninstall or reports a signing conflict, stop and check the APK/version instead of deleting your ledger.

Installing the same edition from a newer release preserves its capture setting where permission remains available. Open the app after installation or switching editions. No release can guarantee capture of messages Android does not deliver, and missed messages are not backfilled from the inbox.

For step-by-step setup and permission troubleshooting, see [Installation](INSTALLATION.md). For stored fields, exports and deletion, see [Privacy](../PRIVACY.md).