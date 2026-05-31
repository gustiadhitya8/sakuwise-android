# Changelog

All notable changes to Sakuwise are documented here. Dates are local (WIB).
Versioning is `versionName (versionCode)`.

## 1.0.5 (6) — 2026-05-31

**Theme: security & verification pass.** No new user-facing features.

### Security & correctness
- **Android OS backup exclusion hardened.** Sensitive app files (encrypted
  database, key material) are now explicitly excluded from Android Auto Backup
  on all API levels. The previous configuration left a gap on Android 12+.
- **Transfer fee writes are now atomic.** Recording a transfer with a fee
  now uses a single database transaction, preventing a partial write if the
  fee row failed.
- **Recents/task-switcher privacy.** App content is masked whenever the
  app is in the background, so the task switcher thumbnail never shows
  financial data regardless of the auto-lock setting.
- **Notification lock-screen privacy.** Payment reminders no longer show
  their content on the lock screen — only a neutral placeholder is shown.
- **Backup tamper detection tests.** Added instrumented tests proving that
  a modified or corrupted backup file is rejected with a clear error.
- **Migration data-preservation test.** Added instrumented test verifying
  that financial records survive the database migration from schema v4 → v5.
- **Restore error handling improved.** A failed file swap during restore
  now surfaces a clear, actionable error message.

## 1.0.4 (5) — 2026-05-30

**Theme: hardening & readiness for external users.** No new user-facing
features; foundation, safety, and quality work.

### Data safety
- **Room migrations are now explicit and tested.** Removed
  `fallbackToDestructiveMigration()`, which would silently wipe the entire
  database on a future schema bump. The 1→5 migration chain now lives in a
  single tested `SakuwiseMigrations.ALL`, and an unmigrated version bump fails
  loudly instead of destroying data. Added an instrumented migration test
  (v5 baseline + 5→6 scaffold).
- **Backup format versioning is provably forward/backward compatible.**
  Extracted the inner payload codec into a pure, unit-tested `BackupPayload`
  (v1 + v2). Backups made by older versions still restore; restoring an
  old-schema backup is brought forward by the normal migration path on open.
  Hardened payload parsing against malformed/oversized length fields
  (overflow-safe bounds, clean errors). Added `docs/BACKUP_FORMAT.md`.

### Bug fixes
- **Export ↔ import parity.** CSV/XLSX export now uses the exact column schema
  the importer expects (`Tanggal/Tipe/Kategori/Item/Akun/Jumlah/Catatan`), with
  real plan-category & item names instead of a note-prefix hack — export → import
  is lossless. Verified round-trip in both ID and EN. Single canonical header
  source prevents future drift.
- **Bilingual (i18n) leaks fixed.** Swept all user-facing strings to resources;
  English mode no longer leaks Indonesian text (dashboard, assets chart, import/
  export, plan, OCR, settings, auto-backup PIN sheet). ID/EN string sets balanced.

### Quality & performance
- **CI + static analysis.** Added detekt (with ktlint formatting rules) and a
  GitHub Actions workflow (build + unit tests + detekt; migration test on an
  emulator). Legacy issues frozen in a baseline so only new issues fail.
- **Baseline Profile.** Bundled an AOT startup profile (+ProfileInstaller) for
  faster cold start on entry-level devices.
- **Crash/ANR monitoring via Android Vitals** (Play Console) — no telemetry SDK.
  Removed dead `google-services` catalog entries. Documented in `docs/MONITORING.md`.
- **QA pass** on empty/first-run states (no crashes on empty data); see
  `docs/QA_EMPTY_STATES_v1.0.4.md`.

## 1.0.3 (4) — 2026-05-27
- Self-contained daily Google Drive auto-backup (PIN stored in Android Keystore;
  no prior manual backup required); rolling 3-copy local backup.
- Fixes: dashboard updates on transaction-date edits (`Transaction.equals`);
  live account balance in pickers/forms; sign-aware reconciliation display;
  correct backup-status colour on the Me screen.

## 1.0.2 (3) — 2026-05-26
- Backup status uses max(local, Drive) timestamp; Drive backup saves a local
  copy for the daily worker; Plan i18n + real-time planned/remaining; OCR retry
  buttons; Reset-app i18n; onboarding nav-bar inset fix.
