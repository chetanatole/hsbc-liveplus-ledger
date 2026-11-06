# Generate an APK release

The [release workflow](../.github/workflows/release.yml) builds and publishes both signed APK editions when a version tag is pushed. Pushing to `main` runs tests but does not publish a release.

Release assets from 1.0.3:

- `LivePlus-Ledger-Notifications.apk` — one selected SMS app's notifications; no SMS permission.
- `LivePlus-Ledger-SMS.apk` — new incoming SMS through `RECEIVE_SMS`; no notification access or inbox scan.
- `SHA256SUMS.txt` — one checksum line for each APK.

Users choose one edition according to the access they are comfortable granting. Both share the app identity/version/signing key and cannot be installed side by side. Neither reads existing SMS. Older releases use `LivePlus-Ledger.apk` for the notification-based app. GitHub source archives are not installers. See [edition comparison](EDITIONS.md).

## 1. Configure signing secrets

Use the retained signing key for both editions and every update. A first local [build](DEVELOPMENT.md#build) creates signing files only if absent.

In **GitHub → Settings → Secrets and variables → Actions**, configure:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 contents of `.signing/personal-release.p12` |
| `ANDROID_KEYSTORE_PASSWORD` | Contents of `.signing/key-password.txt` |

With an authenticated GitHub CLI, from the repository root:

```powershell
$repo = 'chetanatole/hsbc-liveplus-ledger'
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $PWD '.signing/personal-release.p12'))) | gh secret set ANDROID_KEYSTORE_BASE64 --repo $repo
Get-Content -LiteralPath '.signing/key-password.txt' -Raw | gh secret set ANDROID_KEYSTORE_PASSWORD --repo $repo
gh secret list --repo $repo
```

Keep keys/passwords private and out of source control. The workflow restores them temporarily and removes them afterward. Both APKs must match the public SHA-256 certificate fingerprint in `docs/RELEASE_SIGNING_CERTIFICATE.txt`. Do not generate a new key for an existing app update.

## 2. Validate and set the version

Update `android:versionName` and increase `android:versionCode` in `app/src/main/AndroidManifest.xml`. Both editions derive their version from it and must use identical values. Update `CHANGELOG.md` and `.github/RELEASE_NOTES.md` with both download choices and exact permission behavior.

Run `tools/build-all.ps1` with the [tool paths](DEVELOPMENT.md#build). It builds/signs/verifies both APKs, enforces permission/component isolation and creates both checksums. Test both editions using [DEVICE-CHECK.md](../DEVICE-CHECK.md), especially SMS installer allowlisting/user grant, OTP → purchase background delivery, no historical import, and in-place edition switching. Do not describe phone behavior as verified solely because compilation passed.

## 3. Push a release tag

Commit and push reviewed changes, then create a tag matching the manifest base version:

```powershell
git tag -a v1.0.3 -m "Live+ Ledger 1.0.3"
git push origin v1.0.3
```

A suffix such as `v1.0.3-beta.1` publishes a prerelease; a plain tag publishes a normal release. Manifest `1.0` is accepted as base tag `1.0.0`. Use a new version/tag for a new release; existing assets are not silently replaced.

## 4. Check the workflow

Open [GitHub Actions](https://github.com/chetanatole/hsbc-liveplus-ledger/actions). The job:

1. Checks tag/version/signing configuration and installs SDK packages.
2. Runs synthetic tests and builds both editions using separate capture sources.
3. Verifies both signatures, ZIP alignment, exact permissions and component isolation.
4. Checks each certificate against the retained public fingerprint.
5. Generates two checksum lines and publishes both APKs plus `SHA256SUMS.txt`.

A failed test, missing secret, signature mismatch, permission/component leak or build failure stops publication. Workflow configuration is not proof of successful publication: check the actual run and release assets.

## Verify a download

Download your chosen APK and `SHA256SUMS.txt` from the same release. In PowerShell:

```powershell
Get-FileHash -LiteralPath './LivePlus-Ledger-SMS.apk' -Algorithm SHA256
Get-Content -LiteralPath './SHA256SUMS.txt'
```

Compare with the line for that exact filename; use the Notifications filename if you chose it. Letter case does not matter. File checksums differ between editions/builds; the signing certificate identifies the retained app signing identity.

## Download links

After a normal two-edition release is published:

```text
https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-Notifications.apk
https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger-SMS.apk
```

These links work only once the latest normal release contains the named assets. They do not select prereleases; use the prerelease page directly. The old `LivePlus-Ledger.apk` asset name is not published by the new workflow.

Explain compatible in-place edition switching: export first, install one same/newer compatible edition without uninstalling, open the app and enable the new capture method. Existing local merchant mappings survive in-place updates but are not included in JSON backups. See [installation](INSTALLATION.md) and [privacy](../PRIVACY.md).

References: [GitHub releases](https://cli.github.com/manual/gh_release_create), [Actions secrets](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).