# Installation

## Choose and install an APK

From release **1.0.3**, choose **one** APK from [GitHub Releases](https://github.com/chetanatole/hsbc-liveplus-ledger/releases), or [build it](DEVELOPMENT.md):

- **[LivePlus-Ledger-Notifications.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-Notifications.apk):** choose this if you prefer not to grant SMS permission. It uses notification access for one SMS messaging app.
- **[LivePlus-Ledger-SMS.apk](https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-SMS.apk):** choose this if you accept permission to receive new incoming SMS. It has no notification access or existing-inbox access.

**Neither edition reads existing SMS, scans your inbox or sends messages.** Read the [edition comparison](EDITIONS.md) and [privacy policy](../PRIVACY.md). Older releases use `LivePlus-Ledger.apk` for the notification-based app.

1. Open the chosen APK on an Android 8.0+ phone.
2. If prompted, allow installation from the browser/file manager used to open it. This is called sideloading: installing outside an app store.
3. Open Live+ Ledger after installation and go to Settings. Both downloads use the name Live+ Ledger on the phone. The release version appears in Settings.

Both editions have the same ledger features. Automatic capture is optional; manual entry and Paste alert work without either capture permission.

## Play Protect installation block

Play Protect may display **App blocked to protect your device**, explaining that the app can request sensitive data access. Google documents this block for sideloaded apps requesting sensitive access, including notification listeners and SMS access. [Google's warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance)

If you choose to temporarily disable scanning to install an APK you trust:

1. Open **Google Play Store → profile icon → Play Protect → Settings**.
2. Turn **Scan apps with Play Protect** off, if the device permits it.
3. Install the chosen edition's APK.
4. **Immediately turn scanning back on**, whether installation succeeds or fails, and run a Play Protect scan.

Disabling scanning reduces protection. Google recommends keeping Play Protect enabled. Do not use this workaround for a **Harmful app blocked** detection. Menu labels vary by device/policy. This does not grant notification or SMS permission. [Google's instructions](https://support.google.com/android/answer/2812853?hl=en)

## Configure the card

1. Enter **only your card's last four digits**.
2. Set the **statement closing day** (1–31), or `0` for a calendar month. Do not use the payment due date.
3. Check the accelerated cashback cap against your card's terms.

Settings apply automatically. Valid text edits apply after a short pause or when leaving the field. Switches and app selection apply immediately. Invalid input shows an error and retains the previous valid value. There is no Save settings button.

## Notifications edition setup

1. In **Settings → Automatic capture**, choose **one SMS messaging app**. The installed default is preselected when available; bank apps and unrelated apps are not offered.
2. Turn on **Capture new transaction notifications**.
3. Read the disclosure and enable Live+ Ledger in Android Settings when prompted. You can also open access settings with **Enable Android notification access**. Capture remains off if you cancel or do not grant access.
4. Keep transaction notification text enabled in the selected messaging app. No SMS permission is requested.

If Android restricts notification access, open **Settings → Apps → Live+ Ledger → ⋮ → Allow restricted settings**, then try granting notification access again. This is separate from Play Protect scanning and does not grant SMS permission.

**Unread OTP warning:** Android may hide later transaction notifications grouped with an OTP. Clear notifications for that conversation before the next alert. Add an already missed purchase with Paste alert or manually after checking for an existing entry. Clearing the shade does not backfill a missed purchase. [Android OTP redaction](https://developer.android.com/about/versions/15/behavior-changes-all#otp-redaction)

## SMS edition setup: new incoming SMS only

1. In **Settings → Automatic capture**, turn on **Capture new transaction SMS**.
2. Read **Allow new SMS capture?** and choose **Continue** if you accept incoming-SMS access, then approve Android's permission request. If declined or cancelled, capture stays off.
3. Capture starts after permission is granted. If you granted permission separately using **Enable SMS permission**, turn on the capture switch afterward.

Android's permission covers incoming SMS from all senders. Live+ Ledger filters locally for HSBC and your saved suffix, ignores OTPs/unrelated messages and saves only transaction details. **No `READ_SMS`, existing-message scan, historical import or SMS sending is included.** You do not select a messaging app or grant notification access; your default SMS app is unchanged. Only SMS are supported, not RCS/MMS or bank-app notifications.

`RECEIVE_SMS` is a restricted permission: the installer must allowlist it before the user can grant it. A denied request, missing prompt or unavailable permission can reflect Android/installer/device policy. Check App info → Permissions; the app cannot grant permission itself. If unavailable, use the Notifications edition, Paste alert or manual entry. **Allow restricted settings** for notification access is not a substitute for SMS permission allowlisting. [Android SMS permission](https://developer.android.com/reference/android/Manifest.permission#RECEIVE_SMS)

Direct SMS delivery avoids notification grouping. Android's SMS OTP protection still applies: protected messages can be delayed or filtered, depending on message format and target SDK. OTPs are always ignored by the parser. Verify actual SMS delivery on your phone before relying on it. [Android 17 SMS protection](https://developer.android.com/about/versions/17/behavior-changes-all#sms-otp-protection)

## Using the ledger

**Overview → This period** shows spend and estimated cashback for the 10% and 1.5% groups, excluded 0% spend and totals. Excluded spend has no cashback sub-row. Total spend includes uncategorized and excluded purchases after linked refunds; possible duplicates are left out until confirmed. Accelerated cashback respects your configured cap.

- **Activity → Add purchase:** enter a purchase, choose its date and cashback category.
- **Paste alert:** preview a completed-purchase alert before importing. Check for an existing entry; paste only the purchase text, not the OTP or mixed conversation.
- **Needs review:** shows the selected period's review count and toggles its filter.
- **Remember this merchant's category:** save to create/update the mapping, or uncheck and save to forget it. Cancel leaves it unchanged. Mappings apply to future captured/pasted purchases without changing other existing transactions.
- **Settings → Merchant categories → Manage merchant categories:** add/edit/rename/delete your mappings. Built-in suggestions are not listed there. See [merchant matching](MERCHANTS.md).
- **Settings → Appearance:** choose Light or Dark; Light is the default.

## Capture troubleshooting

- Keep capture enabled and its edition-specific access granted. The app screen does not normally need to remain open. Open it after installation or a force-stop.
- Notifications edition: check the selected SMS app, card suffix and notification text. Already-visible notifications are baselined when capture starts/reconnects, and grouped history is tracked to prevent replay. A repeated identical flat alert may be retained as a possible duplicate for review.
- SMS edition: no notification-source selection is needed. Check SMS permission and distinguish incoming SMS from RCS/MMS. Messages not delivered by Android cannot be recovered by this edition; it never scans the inbox.
- If background capture fails, check **Android Settings → Apps → Live+ Ledger → Battery → Unrestricted / Allow background activity** and sleeping/deep-sleep lists, where available. Unrestricted battery access cannot recover content Android withholds. Menus vary by phone. [Android background restrictions](https://developer.android.com/topic/performance/background-optimization)
- Review amount, merchant, date and category against the original message. Merchant suggestions do not reveal the bank's actual MCC. Add a missed purchase manually or with Paste alert after checking for an existing entry.

## Updates, edition switching and changing phones

Both APKs share the app identity/version/signing key, so only one edition can be installed at a time. Install an update or the other edition from the same or a newer compatible release **over the existing app** to retain transactions, settings and merchant mappings. Do not uninstall to switch editions. Open the app after installation; an edition switch disables capture and clears prior capture fingerprint caches until you enable the new access method. Android normally blocks older version-code downgrades.

Export a JSON backup before switching, uninstalling or changing phones. A differently signed APK may require uninstalling, which deletes local data; check the APK/version before proceeding. Restore requires an empty ledger and leaves capture off. Backups include transactions and card/reward settings, but not merchant mappings, theme or notification-source selection; compatible in-place updates preserve those local values without a restore.