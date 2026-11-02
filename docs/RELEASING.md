# Generate an APK release

The [release workflow](../.github/workflows/release.yml) builds and publishes a signed APK when a version tag is pushed. A push to `main` runs tests but does not publish a release.

Release assets:

- `LivePlus-Ledger.apk` — Android installer.
- `SHA256SUMS.txt` — APK checksum.

GitHub's automatically generated source archives are not installers.

## 1. Configure signing secrets

Use the same signing key for every update. A local [build](DEVELOPMENT.md#build) creates the signing files if they do not already exist.

In **GitHub → Settings → Secrets and variables → Actions**, add:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded contents of `.signing/personal-release.p12` |
| `ANDROID_KEYSTORE_PASSWORD` | Contents of `.signing/key-password.txt` |

Alternatively, from the repository root with an authenticated GitHub CLI:

```powershell
$repo = 'chetanatole/hsbc-liveplus-ledger'
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $PWD '.signing/personal-release.p12'))) | gh secret set ANDROID_KEYSTORE_BASE64 --repo $repo
Get-Content -LiteralPath '.signing/key-password.txt' -Raw | gh secret set ANDROID_KEYSTORE_PASSWORD --repo $repo
gh secret list --repo $repo
```

Keep the key and password out of source control. The workflow restores them on the build runner and removes the temporary signing files after the job.

The public certificate fingerprint in `docs/RELEASE_SIGNING_CERTIFICATE.txt` must match the signing key. If setting up a different signing identity, update that fingerprint before publishing.

## 2. Set the version

In `app/src/main/AndroidManifest.xml`:

- Set `android:versionName` to the release version.
- Increase `android:versionCode` for each subsequent APK update.
- Update `CHANGELOG.md` and `.github/RELEASE_NOTES.md`.

The tag must match the manifest's base version. Manifest version `1.0` is accepted as `1.0.0`.

## 3. Push a release tag

Commit and push the release changes, then create the tag:

```powershell
git tag -a v1.0.2 -m "Live+ Ledger 1.0.2"
git push origin v1.0.2
```

For a preview release, use a suffix such as `v1.0.2-beta.1`. Tags with a suffix publish a GitHub prerelease; plain version tags publish a normal release.

## 4. Check the workflow

Open [GitHub Actions](https://github.com/chetanatole/hsbc-liveplus-ledger/actions). The release job:

1. Checks the tag, manifest version and signing configuration.
2. Installs the required Android SDK packages.
3. Runs tests and builds the APK.
4. Verifies signing identity and ZIP alignment.
5. Generates the checksum.
6. Publishes the APK and checksum under [Releases](https://github.com/chetanatole/hsbc-liveplus-ledger/releases).

A failed test, missing secret, signature mismatch or build error stops publication. Existing release assets are not silently replaced; use a new version/tag for a new release.

## Verify a download

Download the APK and `SHA256SUMS.txt` from the same release. In PowerShell, from that download folder:

```powershell
Get-FileHash -LiteralPath './LivePlus-Ledger.apk' -Algorithm SHA256
Get-Content -LiteralPath './SHA256SUMS.txt'
```

The hashes must match; letter case does not matter. This checksum identifies the APK file. The signing certificate fingerprint identifies the signing identity and normally stays the same across releases.

## Download links

After a normal release is published, the latest APK is available at:

```text
https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger.apk
```

The latest-release link does not select prereleases. Use a prerelease's own release page instead.

APK installation and notification access are described in [INSTALLATION.md](INSTALLATION.md).

References: [GitHub releases](https://cli.github.com/manual/gh_release_create), [Actions secrets](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).
