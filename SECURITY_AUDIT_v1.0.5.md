# Security Audit — Sakuwise v1.0.5

**Date:** 2026-05-31  
**Auditor:** Claude Code (automated + manual review)  
**Scope:** 11 KEEP items from v1.0.5 handoff doc  
**Threat model:** Confidentiality + integrity of a local-first read-only finance recorder. No active attacker stealing funds; guard data secrecy and prevent loss.

---

## Summary

| Severity | Count | Items |
|----------|-------|-------|
| P0 | 0 | — |
| P1 | 6 | A1, A2, A4 (test), A5, B5, .gitignore |
| P2 | 5 | A3 (restore atom), A7, B2, B5 (minor), B9 |
| Pass | 4 | A3 (crypto), A6, B8, partial-B2 |

No P0 found. All P1 items are fixable without UX changes.

---

## P1 Findings

### [A2-P1] data_extraction_rules.xml is empty — Android 12+ cloud backup enabled

**File:** `app/src/main/res/xml/data_extraction_rules.xml`  
**File:** `app/src/main/res/xml/backup_rules.xml`

**Finding:**  
`AndroidManifest.xml` sets `android:allowBackup="false"` and points to `android:dataExtractionRules="@xml/data_extraction_rules"`. On **Android 12+ (API 31+)**, `dataExtractionRules` takes precedence over `allowBackup`. The current `data_extraction_rules.xml` contains only a TODO comment inside `<cloud-backup>` — an empty cloud-backup element means Android backs up **everything** in the app's private storage to Google cloud backup (Auto Backup).

```xml
<!-- Current (BROKEN on API 31+): -->
<data-extraction-rules>
    <cloud-backup>
        <!-- TODO: empty → backs up everything! -->
    </cloud-backup>
</data-extraction-rules>
```

Backed-up files would include: `dek.bin` (EncryptedFile-wrapped DEK), `pin.bin` (EncryptedFile-wrapped Argon2id hash), `sakuwise.db` (SQLCipher DB), `sakuwise_autobackup_pin` SharedPreferences (IV+ciphertext of auto-backup PIN).

**Risk level:** The data is encrypted with Keystore-bound keys and Argon2id, so restoring these files to a different device would yield unreadable blobs. However: (1) encrypted data is being sent to Google without user awareness or consent, violating "no cloud except Drive backup" principle; (2) the `autobackup_pin` SharedPrefs IV+ciphertext blob is sent to Google (minor but unnecessary); (3) if the same device is wiped/restored from Google backup AND the Keystore backup is also restored (some OEM cloud key sync), the combination could allow access — though this is unlikely with hardware-backed keys.

`backup_rules.xml` (for Android < 12) is similarly empty — but `allowBackup="false"` correctly prevents backup on Android < 12.

**Fix required:** Add explicit excludes to `data_extraction_rules.xml` or disable cloud backup entirely. Document what is excluded.

---

### [A1-P1] Migration test validates schema but does NOT test data preservation

**File:** `app/src/androidTest/java/.../MigrationTest.kt`

**Finding:**  
`baseline_v5_createsAndValidates()` creates a v5 database and confirms it has tables, then runs migrations. It does NOT seed any rows before migration and assert they survived. The `migrate_5_to_6_preservesData()` is `@Ignore`d and is just a scaffold with commented-out code.

The A1 AC says: *"test upgrade-with-data hijau; tidak ada jalur destruktif tersisa"*. The current test validates schema structure only — not that user data survives through the migration chain.

**Risk:** The migration chain removed `fallbackToDestructiveMigration()` (correct), but we have not proven that a device upgrading from v4→v5 (or any prior version) keeps its financial data. Since all 5 schema JSON files exist in `app/schemas/`, we CAN seed a v4 DB and validate v5 migration.

**Fix required:** Add a seeded-data migration test: create DB at version 4, insert synthetic accounts/transactions/etc., run migrations to 5, assert all rows survive with correct values.

---

### [A4-P1] No instrumented test for tampered or corrupt backup file

**Finding:**  
`BackupCrypto.decryptBackup()` correctly validates the GCM auth tag and throws `BadPinException` on failure. However, the A4 AC requires a test proving this behavior: *"file di-tamper → ditolak pesan jelas; restore terputus → DB tidak korup/parsial"*. No such test exists in the test suite.

`BackupPayloadVersioningTest.kt` exists but tests payload serialization/deserialization — not the encrypted layer or tamper detection.

**Fix required:** Add unit tests (using synthetic fixture):
1. Create a valid encrypted backup, flip bytes in the ciphertext, attempt decrypt → expect `BadPinException`.
2. Create a valid backup, truncate it → expect `IllegalArgumentException`.
3. Verify a mid-restore interruption (simulate) does not leave DB in corrupted/partial state.

---

### [A5-P1] Transfer not atomic — two upserts without @Transaction

**File:** `app/src/main/java/.../usecase/UseCases.kt:207-273` (`AddTransferUseCase`)

**Finding:**  
`AddTransferUseCase` calls `repo.upsert(txn)` for the transfer row, then (when `bookFeeAsExpense == true`) calls `repo.upsert(...)` for the fee expense row — two separate DB operations with no `@Transaction` wrapper. If the second upsert fails (OOM, DB error), the transfer row exists but the fee expense row does not, leaving balances inconsistent.

Even without the fee path, a transfer has only one DB row — no debit+credit split in the schema. The balance SQL in `AccountDao` computes the net from `sourceAccountId` and `destAccountId` reads. This makes a single-row transfer inherently atomic for the main amount. However, the two-upsert fee path is NOT atomic.

**Fix required:**
- Wrap the two-upsert fee path in a `@Transaction` DAO method.
- Add unit tests: transfer with fee succeeds atomically; simulate second-upsert failure doesn't leave orphan transfer.

---

### [B5-P1] Recents thumbnail may expose app content for non-zero auto-lock settings

**File:** `app/src/main/java/.../feature/lock/AppLockController.kt`  
**File:** `app/src/main/java/.../app/AppNavGraph.kt`

**Finding:**  
The lock overlay (opaque `sw.bg` Box) is shown when `_locked.value == true`. The lock is triggered in `onAppBackgrounded()` — which fires from `ProcessLifecycleOwner.onStop()`.

On Android, the Recents/task-switcher thumbnail is typically captured when the activity transitions away (around `onPause()` time, before `onStop()`). For the **"Langsung" (0 min) auto-lock setting**, locking fires immediately in `onStop()`, but the Recents thumbnail might already be captured at `onPause()` before `onStop()` fires, depending on the Android version and OEM.

For **any non-zero auto-lock setting** (e.g., 5 min), the app does not lock on background at all — it only checks on foreground return. This means the Recents preview will always show live app content (dashboard with balances, transactions) regardless of the lock timer.

The handoff explicitly calls this out: *"FLAG_SECURE di-descope; mask-saat-background/Recents adalah satu-satunya yang menjaga preview app-switcher bersih dari nominal."* The current implementation does NOT reliably mask Recents for non-zero auto-lock durations.

**Fix required:**  
Show a privacy overlay (solid `sw.bg` screen) whenever the app goes to background, **independent of auto-lock timer**. This masks the Recents thumbnail. On foreground return, the overlay stays until the PIN/biometric check passes (if auto-lock timer has expired) or hides immediately (if timer hasn't expired). This separates "Recents masking" from "lock-on-return" — both are necessary.

Implementation: add a `_backgrounded` flag in `AppLockController`; show a solid overlay when backgrounded; auto-lock timer still governs whether PIN is required on return.

---

### [.gitignore-P1] Sensitive test artifacts not excluded from git

**File:** `.gitignore`

**Finding:**  
The handoff mandates: *"Tambahkan ke .gitignore: *.sakuwise, *.csv, *.xlsx, dir scratch test, dump/log restore. PIN tidak pernah masuk kode/test/log/commit."*

Current `.gitignore` is missing: `*.sakuwise`, `*.csv`, `*.xlsx`, restore/scratch dirs, `*.log`, and DataStore proto dumps. Committing even a single synthetic backup file would expose the format and potentially fixture data if the test is careless.

**Fix required:** Add the missing patterns.

---

## P2 Findings

### [A3-P2] Restore not fully atomic — DEK installed before DB file swap

**File:** `app/src/main/java/.../crypto/BackupService.kt:102-135`

**Finding:**  
`restore()` sequence:
1. `database.close()` ✓
2. `keyManager.installDek(dek)` — DEK overwritten
3. `tmp.writeBytes(dbBytes)` — new DB written to temp file
4. `dbFile.delete()` — OLD db deleted
5. `tmp.renameTo(dbFile)` — atomic on most filesystems; falls back to `copyTo+delete` on failure

If step 5 fails (e.g., `copyTo` throws, target inaccessible, no space), the original DB is gone and the new one is not in place. The DEK from step 2 corresponds to the new DB, not the deleted one. The app is left in a broken state.

Additionally, WAL/SHM files are cleared (step between 3 and 4) — this is correct, but the ordering could be improved.

**Risk:** Low in practice (failures here are OS-level), but worth logging explicitly and documenting recovery (factory reset / reinstall from backup again).

**Mitigation without UX change:** Add explicit `try/catch` around the file swap and throw a descriptive `BackupRestoreException` so the caller can show an actionable error message. The two-phase commit pattern (rename is atomic; fallback isn't) is the correct approach; keep but add the error handling.

---

### [A7-P2] Dependency CVE check (versions as of 2026-05-31)

Versions in `libs.versions.toml`:

| Library | Version | Status |
|---------|---------|--------|
| SQLCipher | 4.6.1 | ✓ Current stable. No CVEs in 4.6.x. |
| Room | 2.6.1 | ✓ Current stable. |
| Hilt | 2.52 | ✓ Recent. |
| Biometric | 1.2.0-alpha05 | ⚠ Alpha — no stable 1.2.x yet; 1.1.0 is latest stable. Alpha APIs are not final. Not a CVE, but API stability risk. |
| Security Crypto | 1.1.0-alpha06 | ⚠ Alpha — latest stable is 1.0.0. Used for EncryptedFile. The alpha has been stable in practice but not final. |
| WorkManager | 2.10.0 | ✓ Current stable. |
| ML Kit | 16.0.1 | ✓ No known CVEs. |
| Argon2kt | 1.5.0 | ✓ No known CVEs. |
| play-services-auth | 21.4.0 | ✓ Recent. |

**No critical CVEs found** in the listed versions as of the audit date. The alpha dependencies (biometric, security-crypto) carry API stability risk but no known security defects. Monitor for stable releases.

---

### [B2-P2] Notification visibility not explicitly set

**File:** `app/src/main/java/.../core/work/RecurringPaymentReminderWorker.kt`

**Finding:**  
`NotificationCompat.Builder` default visibility is `VISIBILITY_PRIVATE` (hides content on secure lock screen). The current code does not call `.setVisibility()` explicitly. On most Android devices the default private behavior protects the notification content. However:
- Some OEM launchers or notification shade customizations may override visibility defaults.
- The notification body uses `reminder_notif_body_format` with `existing.name` (plan item name like "BPJS" or "Cicilan"). This doesn't expose financial amounts, but names can be sensitive.

**Risk:** Low (default is already PRIVATE), but explicit is better for cross-device reliability.

**Fix (recommended, not blocking):** Add `.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)` and optionally `.setPublicVersion(Builder(...).setContentTitle("Sakuwise").setContentText("Pengingat pembayaran").build())` for the lock screen public version.

---

### [B5-P2] Recents masking timing for "Langsung" mode

Even for the "Langsung" (0 min) setting, `_locked.value = true` fires in `onStop()`. On some Android versions/OEMs, the Recents screenshot is captured at `onPause()` time before `onStop()` runs, so the lock overlay may not be painted in the Recents thumbnail even in "Langsung" mode.

**Fix:** Same as [B5-P1] fix above — unified solution.

---

### [B9-P2] Biometric enrollment invalidation not implemented

**File:** `app/src/main/java/.../feature/lock/LockScreen.kt`

**Finding:**  
`BiometricPrompt.authenticate()` is called without a `CryptoObject`. This means biometric authentication is not tied to a Keystore key with `setInvalidatedByBiometricEnrollment(true)`. If the user adds a new fingerprint on their device, the new fingerprint can unlock the app immediately — the app has no mechanism to detect that enrollment changed.

The A9 AC says: *"kunci ter-invalidasi bila ada enrollment biometrik baru (tangani anggun)"*.

**Risk:** Moderate. An attacker who gains physical access and can add a fingerprint (requires device credential to do so on modern Android) could unlock the app. The PIN fallback is always available and is the stronger protection.

**Fix (recommended):** Create a Keystore key with `setInvalidatedByBiometricEnrollment(true)`, use `CryptoObject`-based authentication. On `KeyPermanentlyInvalidatedException`, disable biometric unlock and prompt user to re-enable with PIN confirmation. This requires a settings migration path.

---

## Pass / Clean

### [A3-Pass] Crypto chain — no nonce reuse, no hardcoded secrets

- `BackupCrypto.encryptBackup()`: fresh salt and nonce via `SecureRandom` on every call ✓
- `AutoBackupPinStorage.savePin()`: IV generated by `Cipher.init(ENCRYPT_MODE, key)` → fresh random IV per encrypt ✓
- `KeyManager`: DEK 32-byte `SecureRandom`; stored in EncryptedFile (AES256_GCM_HKDF_4KB, Keystore-backed) ✓
- `PinStore`: Argon2id t=3 m=64MiB p=1 (adequate); salt 16-byte `SecureRandom`; stored in EncryptedFile ✓
- `PinStore.constantTimeEquals()`: constant-time comparison for hash verification ✓
- `BackupCrypto.decryptBackup()`: validates magic + version before decryption; validates ciphertext length bounds (overflow-safe) ✓
- No hardcoded keys, salts, or passwords found anywhere ✓
- DEK and KEK zeroed after use (`kek.fill(0)`, `keyManager.zeroize()`) ✓

Minor note: `deriveKek()` converts PIN chars to bytes using `c.code.toByte()` — fine for numeric PINs (char codes 48–57). If passphrase mode uses non-ASCII characters, `.code.toByte()` truncates the high byte. This is a latent bug for passphrase mode but does not affect the numeric PIN path.

---

### [A6-Pass] No secrets in logs

Grep of all `Log.*`, `println`, and `Timber.*` calls in `app/src/main/`: **zero results**. Production code is log-silent. R8/ProGuard in release further removes any stray debug logging from dependencies.

---

### [B8-Pass] Manifest exported components are correct

- `MainActivity`: `exported=true` (required for MAIN/LAUNCHER) ✓
- `FileProvider`: `exported=false` ✓
- `DebugSkipOnboardingReceiver`, `DebugDriveTestReceiver`: in `src/debug/AndroidManifest.xml` only, with `exported=true`. These are **debug-build only** — the debug manifest is not merged into release builds. Release APK/AAB contains neither receiver. ✓
- No PendingIntent usage in production code ✓
- No WebView ✓
- No deep-link intent-filters ✓

---

## versionCode — ⚠️ REQUIRES PLAY CONSOLE CONFIRMATION

From `app/build.gradle.kts`: `versionCode = 5, versionName = "1.0.4"`.  
From `CHANGELOG.md`: v1.0.4 is versionCode 5.

**Tentative next:** versionCode **6**, versionName **1.0.5**.

⚠️ FOUNDER MUST confirm the live versionCode in Play Console before generating the AAB. If a previous v1.0.5 attempt (the one that was held/rolled back) had versionCode 6 uploaded (even if not published), Play Console requires 7 or higher. Do NOT guess — verify the highest uploaded versionCode and use **that + 1**.

---

## Items not in scope (descoped per handoff §3)

- A8 StrictMode/LeakCanary — skipped
- B1 FLAG_SECURE — MUST NOT add; screenshots intentionally allowed
- B3 PIN field hardening — moot (custom numeric keypad)
- B4 Clipboard hygiene — v1.1
- B6 Autofill exclusion — v1.1
- B7 Screen-reader masking — dropped

---

## Recommended fix order (Phase 2)

1. **A2** — Fix `data_extraction_rules.xml` + `backup_rules.xml` + document exclusions. (5 min)
2. **.gitignore** — Add missing patterns. (2 min)
3. **B5** — Unified Recents masking: show `_backgrounded` overlay independent of auto-lock timer. (30 min)
4. **A5** — Wrap two-upsert fee path in `@Transaction`. (15 min)
5. **A1** — Add seeded migration test (v4→v5 data preservation). (45 min)
6. **A4** — Add tamper/corrupt backup tests with synthetic fixture. (45 min)
7. **B2** — Explicitly set `VISIBILITY_PRIVATE` on notification. (5 min)
8. **A3/A4** — Improve restore error handling. (20 min)
9. **B9** — Document biometric invalidation gap; schedule for v1.1 (needs design).
10. **A7** — Monitor alpha deps; upgrade when stable versions ship.
