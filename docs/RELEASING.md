# APK releases on GitHub

The repository includes a tag-triggered release workflow at `.github/workflows/release.yml`. No APK release has been published yet; the tag-triggered workflow has not been run.

A successful version-tag run builds/tests the APK, verifies the signature and alignment, checks its signing-certificate fingerprint, generates a checksum and publishes these release assets:

- `LivePlus-Ledger.apk` — the Android installer.
- `SHA256SUMS.txt` — SHA-256 checksum.

GitHub also supplies source archives automatically; those are not installers. Public-repository release assets are downloadable by visitors. Private-repository downloads require access.

## One-time repository setup

Push the project to your GitHub repository first. In **Settings → Secrets and variables → Actions**, configure:

| Secret | Contents |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded bytes of the existing `.signing/personal-release.p12` |
| `ANDROID_KEYSTORE_PASSWORD` | Contents of the existing `.signing/key-password.txt` |

Use the original key if releases should update the owner's existing installed app. Do not commit these values, paste them into issues/chat, or put them in workflow YAML. The workflow deliberately fails if they are missing.

You can configure secrets with an authenticated GitHub CLI from a trusted local PowerShell session without printing their values:

```powershell
$repo = 'chetanatole/hsbc-liveplus-ledger'
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $PWD '.signing/personal-release.p12'))) | gh secret set ANDROID_KEYSTORE_BASE64 --repo $repo
Get-Content -LiteralPath '.signing/key-password.txt' -Raw | gh secret set ANDROID_KEYSTORE_PASSWORD --repo $repo
```

Check each command succeeds. Base64 is an encoding, not encryption; keep any temporary copy private. GitHub stores the submitted values as Actions secrets. The workflow restores temporary key/password files on its runner and removes them even after failure.

Only trusted maintainers should be able to publish release tags or change release workflow code. Do not supply signing secrets to untrusted workflows. Protect your private backup of the signing key. A fork using its own signing identity must update the public fingerprint file deliberately.

`docs/RELEASE_SIGNING_CERTIFICATE.txt` records the expected public certificate fingerprint, not a secret. It prevents publishing an APK signed with an accidentally substituted key.

## Publish a release

1. Check the release commit and phone-test status. Update `versionName` and increment `versionCode` in the Android manifest for subsequent installed-app updates. Update the changelog/release notes.
2. Commit and push the changes.
3. Create and push a version tag:

```powershell
git tag -a v1.0.0-beta.1 -m "Live+ Ledger 1.0.0 beta 1"
git push origin v1.0.0-beta.1
```

The initial manifest version `1.0` is accepted as base version `1.0.0`. Tag bases must match the manifest version; the workflow checks that. Tags with a suffix such as `-beta.1` publish a **prerelease**. Plain tags such as `v1.0.0` publish a normal release. The first release is best treated as a preview while live notification capture remains unconfirmed.

Open the repository's **Actions** tab to inspect the run, then **Releases** to inspect the uploaded APK/checksum. SDK installation requires network access on GitHub's hosted Windows runner. A missing secret, failed test/build/signature check, or fingerprint mismatch stops publication. This workflow uses the built-in repository token, not a separate personal access token.

Do not reuse tags or overwrite already published assets casually. Rerunning a published tag fails at release creation instead of silently replacing the APK.

## Direct download link

After a normal release is published, a stable download link has this form:

```text
https://github.com/chetanatole/hsbc-liveplus-ledger/releases/latest/download/LivePlus-Ledger.apk
```

This direct link becomes usable after the first normal release is published. GitHub's latest-release route does not select a prerelease; link an initial preview through its specific release page/tag instead.

Publishing on GitHub does not remove Android's sideload/Play Protect checks.

References: [GitHub release CLI](https://cli.github.com/manual/gh_release_create), [Actions secrets](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).
