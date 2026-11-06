# Privacy and data handling

Live+ Ledger is an independent offline Android app requiring no bank login or full card number. Both editions store the ledger locally and have no internet permission, analytics, ads or cloud database.

## Choose the access you are comfortable granting

| Edition | Access requested | Existing SMS / inbox scan |
| --- | --- | --- |
| Notifications | Android notification access; no SMS permissions | **Never** |
| SMS | `RECEIVE_SMS` for new incoming messages; no notification access | **Never** |

If you prefer not to grant SMS access, choose the **Notifications edition**. If you are comfortable granting incoming-SMS access, choose the **SMS edition**. Both have the same tracking features. Automatic capture is optional: manual entry and Paste alert work without granting either access. See [edition comparison](docs/EDITIONS.md).

### Notifications edition

Android notification access is broad: the OS can deliver notifications from many apps. The app checks the one selected SMS messaging-app package before inspecting content, then looks for HSBC and the configured last four card digits. Bank-app notifications are not monitored. It has no `READ_SMS`, `RECEIVE_SMS` or SMS inbox access.

Android may hide OTP-containing conversation notifications, including later purchase alerts in the same unread group. Clearing the conversation notification may make later alerts readable; already missed purchases require manual entry or Paste alert. Notification access does not grant an exemption from Android's sensitive-content protection.

### SMS edition: new incoming messages only

The app asks for consent before requesting `RECEIVE_SMS`. Android grants incoming-SMS access across senders; the app cannot request an OS permission limited to HSBC. While capture is enabled and permission is granted, it receives incoming SMS broadcasts, checks for HSBC/card matches locally and discards OTPs, unrelated content and failed-transaction patterns. Multipart segments are assembled transiently before parsing.

**This edition has no `READ_SMS` permission, SMS provider query, existing-inbox scan, historical import, SMS sending or notification listener.** It does not replace your default messaging app. Granting permission, opening the app or enabling capture does not read messages already stored on the phone. It does not capture RCS, MMS, chat-app messages or bank-app push notifications.

Android/installer restrictions may prevent granting SMS access, and Android's OTP protections can delay or withhold protected SMS. The app ignores OTP messages even if delivered. If permission is unavailable or declined, capture stays off and manual/Paste alert entry remains available.

Valid settings changes are applied automatically. Invalid text does not replace the previous valid value. Capture permission is requested only after an explicit action to enable access.

## Stored data

Saved transaction fields include amount, merchant, date, category, review/duplicate state, refund links, receipt time, source description and a SHA-256 event fingerprint for duplicates. Preferences include the card suffix, period, cap, edition/capture state, theme and (for Notifications) the selected SMS-app package. Exact merchant-category mappings can be remembered.

**Raw notification/SMS bodies, SMS sender addresses and OTPs are not saved or uploaded.** Parsing is local and transient. The Notifications edition keeps a bounded local cache of hashed transaction-message fingerprints, times and processing flags to avoid replaying grouped history. Empty/hidden updates retain existing fingerprints. Unmatched and OTP-only messages do not create transaction-message fingerprints or ledger records. SMS fingerprints include a hash of delivery identity/content; raw inputs are not retained.

Capture diagnostic reports are not recorded. Any reports left by earlier test builds are cleared when the updated app opens.

Transactions and merchant mappings use an app-private SQLite database; preferences use app-private storage. Android app backup is disabled. The app does not implement an additional encryption layer. Local parsing cannot guarantee every message format; review captured records and report sanitized errors.

**Erase local data** clears ledger records, merchant mappings, settings and fingerprint caches. Uninstalling removes local storage. Revoking access stops automatic capture without erasing the ledger. Installing the other compatible edition preserves local ledger data but disables capture and clears old capture fingerprint caches until you configure the new access method.

## Exports and external links

Opening the HSBC service-guide link launches an external browser with its own data handling. No bank login is requested by the app.

JSON exports use Android's document picker. Selecting a cloud provider can upload the chosen export there. Exports contain transactions and card/reward settings, including the suffix; keep them private. Theme, notification source, learned merchant mappings and raw messages are not included. Restore requires an empty ledger and leaves capture off until explicitly reconfigured.

## Repository and screenshots

Do not publish real backups, raw bank messages, databases, card digits, balances, OTPs, references or account identifiers. Tests use synthetic examples. Redact financial/personal details before adding screenshots.

Signing keys/passwords are local files under the Git-ignored `.signing/` directory.