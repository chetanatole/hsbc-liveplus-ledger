Choose **one** APK. Both editions track the same spend, cashback, categories and refunds, and support manual entry, Paste alert and backups. Requires Android 8.0 or newer.

| Download | Capture access | Choose this if… |
| --- | --- | --- |
| **[LivePlus-Ledger-Notifications.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/download/v1.0.3/LivePlus-Ledger-Notifications.apk)** | Notification access; no SMS permission | You prefer not to grant SMS permission. Select one SMS messaging app. |
| **[LivePlus-Ledger-SMS.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/download/v1.0.3/LivePlus-Ledger-SMS.apk)** | `RECEIVE_SMS`; no notification access or `READ_SMS` | You are comfortable granting access to new incoming SMS. No app selection is needed. |

**Neither edition reads existing SMS or scans your inbox.** The SMS edition checks only incoming SMS delivered while capture is enabled, discards OTPs/unrelated messages and saves matching transaction details locally. No SMS sending or internet permission is included.

Android may hide notification text while a conversation contains an unread OTP. In the Notifications edition, clear that conversation's notifications before later alerts; paste an already missed purchase after checking for an existing entry. The SMS edition avoids notification grouping, but Android SMS permission/OTP restrictions still apply. RCS messages are not delivered through its SMS receiver.

## What's new in 1.0.3

- Separate Notifications and SMS downloads with clearly explained access choices. Both appear on the phone as Live+ Ledger, without edition labels.
- Settings apply automatically after valid changes. Capture permission still requires an explicit action.
- One SMS messaging-app selection in the Notifications edition; no bank-app source selection.
- New-SMS-only capture with consent, permission-denial handling and multipart assembly in the SMS edition.
- Improvements to readable grouped-notification parsing and replay protection.
- Spend and estimated cashback breakdowns for 10% and 1.5% categories, excluded spend and consistent total layout.

Both APKs share the app identity, version and retained signing key. Install one edition at a time; a compatible update or edition switch preserves the ledger and saved merchant mappings. Switching editions turns capture off until you enable the new access method. Open the app after installation. Export a backup first; do not uninstall to switch editions. Older versions cannot normally replace newer versions.

The installer must allow the restricted SMS permission before Android can grant it. If SMS permission is unavailable, use the Notifications edition or manual/Paste alert entry. Capture also depends on Android permission and background settings. See the installation guide for troubleshooting.

**SHA256SUMS.txt** contains a separate checksum for each APK. See [edition choice and setup](https://github.com/chetanatole/hsbc-liveplus-ledger/blob/v1.0.3/docs/INSTALLATION.md), [privacy](https://github.com/chetanatole/hsbc-liveplus-ledger/blob/v1.0.3/PRIVACY.md) and [reward assumptions](https://github.com/chetanatole/hsbc-liveplus-ledger/blob/v1.0.3/docs/REWARDS.md).