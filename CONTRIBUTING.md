# Contributing

Keep changes focused on offline spending/reward tracking. Explain the user-visible behavior and meaningful checks.

For parser/calculation fixes, reproduce the issue with a synthetic or thoroughly sanitized message, add a relevant case to `tests/EngineTest.java`, and run `tools/test.ps1` with JDK 17. For Android changes also run `build.ps1` and check the affected phone behavior when possible using `DEVICE-CHECK.md`.

Never include real OTPs, card digits, balances, references, credentials, exports or databases in tests/issues. Replace example suffixes consistently with synthetic digits such as `1234`.

Follow [screenshot instructions](docs/screenshots/README.md). Only real reviewed images should be embedded in the README.

Before committing, review `git status` and `git diff --cached`. Keep signing material, APKs, local build wrappers and personal records excluded. CI runs parser and calculation tests. Use the device checklist for Android behavior.

Licensed under the [MIT License](LICENSE).
