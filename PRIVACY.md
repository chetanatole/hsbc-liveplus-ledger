# Privacy and data handling

Live+ Ledger is an independent offline Android app requiring no bank login or full card number.

## Notification access

Android's notification-listener access is broad: the OS can deliver notifications from many apps. The app checks its selected source-app allowlist before inspecting content, then looks for HSBC context and the configured last four card digits.

Unmatched content is discarded. OTP/failed-transaction patterns are rejected. Local parsing cannot guarantee every message format; review captured records and report sanitized errors.

## Stored data

Stored fields include amount, merchant, date, category, review/duplicate state, refund links, receipt time, source description and a SHA-256 event fingerprint for duplicates. Preferences store the card suffix, period, cap, source-app selection and other settings; exact merchant-category rules can be remembered.

Raw notification bodies are not saved. The app does not read the SMS inbox or automatically import historical messages.

Transactions/rules use an app-private SQLite database; preferences use app-private storage. Android app backup is disabled. The app does not implement an additional encryption layer.

**Erase local data** clears records, merchant rules and settings. Uninstalling removes local storage. Revoking notification access stops capture without erasing existing records.

## Network and exports

No internet permission, analytics, ads or cloud database are included. Opening the service-guide link launches an external browser with its own data handling.

JSON exports use Android's document picker. Selecting a cloud provider can upload the chosen export there. Backups contain financial data and the card suffix; keep them private. Restore currently restores transactions/settings, not learned merchant rules. Capture remains off until reconfigured.

## Repository and screenshots

Do not publish real backups, raw bank messages, databases, card digits, balances, OTPs, references or account identifiers. Tests should use synthetic examples. Redact financial/personal details before adding screenshots.

Signing keys/passwords are local files under the Git-ignored `.signing/` directory.
