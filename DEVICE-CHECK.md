# Device testing checklist

1. Install on Android 8+ and open Settings. Confirm the screen scrolls with the keyboard open, text remains readable, and controls are reachable.
2. Save card suffix, period, cap and notification source apps. Enable capture and grant notification access. Return to Overview and check **Capture enabled**.
3. Paste a **redacted copy** of an existing purchase alert using your configured suffix. Preview the parsed amount, merchant and date before adding. Correct the category and confirm the cap meter updates. Delete the test row if it duplicates a real purchase.
4. Wait for the next ordinary HSBC card purchase alert. Check that exactly one purchase appears, or that an extra source alert is flagged as a possible duplicate. Confirm the amount and date against the SMS. Do not make a purchase merely to test the app.
5. Check that an OTP, failed purchase and card bill payment are not listed as purchases. If a format is not recognized, manually add the purchase. A sanitized alert example can be used to improve the parser later.
6. Check the previous/next period boundaries against your statement, especially a transaction on the closing day. Adjust the ledger date when bank posting moves it to the next statement.
7. For a refund, link it to the correct purchase. Check that the original period's eligible spend reduces and compare the actual bank clawback separately.
8. Export a backup and confirm a JSON file is created. Test restore on a spare device or only after securing the backup; restore requires an empty ledger and keeps capture off.
9. Revoke notification access and confirm Overview reports capture off. Re-enable it if you want capture to continue. Open the app after restarting the phone to confirm its status.
