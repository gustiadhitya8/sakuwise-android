# core/cloud — Google Drive backup (REQ-2)

This package implements `GoogleDriveBackup`, the only network-touching code in
Sakuwise. It uses the **AppDataFolder** OAuth scope
(`https://www.googleapis.com/auth/drive.appdata`), so all uploaded files live
in a hidden per-app folder. The user cannot see them in the standard Drive UI;
uninstalling the app erases them on Google's side as well.

Encrypted `.sakuwise` blobs (produced by `BackupService`) are uploaded as-is.
The PIN-derived KEK never leaves the device, so this remains **zero-knowledge**
with respect to Google.

## Required setup in Google Cloud Console

Because Sakuwise uses Google Sign-In + the Drive REST API on Android, you must
register the app's signing cert with an OAuth 2.0 client:

1. Go to <https://console.cloud.google.com>, create (or reuse) a project.
2. Enable APIs **Google Drive API** and **Google Sign-In** under "APIs &
   Services → Library".
3. Open **APIs & Services → Credentials**.
4. Click **Create credentials → OAuth client ID**, pick **Android**.
5. Application name: `Sakuwise`.
6. Package name:
   - Debug: `com.gustiadhitya.sakuwise.debug`
   - Release: `com.gustiadhitya.sakuwise`
   - You will need ONE OAuth client per package + SHA-1 combination, so plan
     on at least two (one for debug builds, one for release).
7. SHA-1 certificate fingerprint:
   - Debug: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - Release: `keytool -list -v -keystore <release.keystore> -alias <alias>`
8. Click Create. No `google-services.json` / `client_id` file is required for
   the Drive REST API — Google authenticates the calling app via the
   package name + SHA-1 association on the OAuth client.

## OAuth consent screen

While the OAuth app is in **Testing** mode (default for new projects), only
emails listed under "OAuth consent screen → Test users" can sign in. Add the
dev's Google account there. If/when the app is published to production, the
consent screen must be submitted for Google verification because the
`drive.appdata` scope is a **restricted** scope.

## Scope justification (for the verification submission)

> Sakuwise stores user-encrypted personal-finance backup files in the user's
> own Drive AppDataFolder. We never read or write outside the AppDataFolder.
> All backup files are AES-256-GCM encrypted with a PIN-derived key on-device
> before upload — Google's servers never see plaintext data.

## Why no `google-services.json`?

The `play-services-auth` artifact does not require Firebase plumbing for the
basic `GoogleSignIn` + `DriveScopes.DRIVE_APPDATA` flow. As long as the OAuth
client in the Cloud Console matches the running APK's package name + signing
SHA-1, the framework picks up the right client at runtime.

## Threat model recap

- Drive sees only the encrypted `.sakuwise` blob (header + AES-GCM ciphertext).
- The backup PIN never leaves the device.
- Sign-in tokens are managed by Google Play Services — Sakuwise itself never
  stores OAuth tokens, just the user's email for display purposes.
- Signing out calls both `signOut()` AND `revokeAccess()` so the app's grant is
  removed from the user's Google account.
