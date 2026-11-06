# Changelog

## Unreleased

- Refresh the Overview and Activity screenshots and keep the download section focused on APK selection.
- Show only the release version in Settings, with Android’s version code retained internally.

## 1.0.3 — 2026-11-07

- Provide separate Notifications and SMS APK downloads with the same Live+ Ledger name, app identity and retained signing key. Neither reads existing SMS or scans the inbox.
- Add opt-in new incoming-SMS capture using only `RECEIVE_SMS`, with consent, permission-denial handling, multipart parsing and duplicate protection. This APK has no notification listener or `READ_SMS` permission.
- Limit notification capture to one installed SMS messaging app. Explain Android's OTP-related content restriction in both Overview and Settings, including clearing the conversation notification before later alerts and pasting an already missed purchase.
- Improve extraction of readable grouped purchases around OTP entries, and prevent replay of known history through empty/hidden notification updates. Baseline visible history when capture starts and retain event identity when retrying a failed save.
- Apply valid settings edits automatically, with inline validation, protected card changes and explicit permission actions for capture access.
- Show spend and estimated cashback for the 10% and 1.5% groups, respect the accelerated cap and linked refunds, and show excluded 0% spend without a cashback sub-row. Match the total layout to the category breakdown.
- Preserve local ledger/settings/merchant mappings during compatible edition switches, with capture off until the new access method is enabled.
- Publish both APKs with individual checksums and enforce packaged permission/component isolation. Update installation, privacy, edition comparison and release documentation with direct downloads.

## 1.0.2 — 2026-11-03

- Expand built-in suggestions to 119 names/keywords, including business-name aliases, shopping and utilities.
- Match merchant-name boundaries and punctuation, prioritize grocery services over broader dining names, and leave ambiguous payment-platform names for review.
- Add a Settings manager showing only remembered and user-added merchant mappings, with add/edit/rename/delete controls.
- Apply saved merchant mappings consistently to captured alerts and pasted purchases without recategorizing existing transactions.

## 1.0.1 — 2026-11-03

- Add saved Light and Dark themes, with Light as the default and Appearance below card/capture settings.
- Make navigation tabs and the review filter distinct with outlines, selected states and touch/focus feedback.
- Keep Add purchase, Paste alert and Needs review controls visible above the Activity list; highlight the current period's review count.
- Restore scroll positions when returning to the app or recreating the screen, with separate positions for tabs, periods and review filters.
- Select purchase dates with a calendar picker and show 10%, 1.5% or 0% in reward-category labels.
- Reflect remembered merchant categories in the editor; unchecking and saving removes the merchant rule without changing other transactions.
- Simplify Overview, Settings, empty-state and transaction-source labels; hide scrollbars on Overview and Settings.
- Refresh Overview and Activity screenshots.
- Recheck official HSBC reward terms on 3 November 2026; retain the 10%/₹1,200 cap and 1.5% base rate, and mark the expired Myntra offer as historical.

## 1.0 — 2026-10-31

- Offline spending and estimated cashback dashboard.
- Configurable reward periods and accelerated cashback cap.
- Transaction-notification capture, category suggestions and merchant rules.
- Duplicate review and full/partial refund linking.
- Manual purchase entry, pasted-alert preview and JSON backup/restore.
