# Sakuwise

> A local-first personal finance Android app built for Indonesian users — replaces the laptop-and-spreadsheet routine with a phone-native budgeting, expense, and net-worth tracker.

[![Release](https://img.shields.io/badge/release-v1.0.4-1F8A4C)](https://github.com/gustiadhitya8/sakuwise-android/releases/tag/v1.0.4)
[![CI](https://github.com/gustiadhitya8/sakuwise-android/actions/workflows/ci.yml/badge.svg)](https://github.com/gustiadhitya8/sakuwise-android/actions/workflows/ci.yml)
[![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84)](https://developer.android.com/about/versions/oreo)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/privacy-local--first%20%C2%B7%20no%20telemetry-1F8A4C)](#privacy)

---

## Background

The author maintains a detailed monthly Google Sheet for personal finance — 50/30/20 allocations, per-line Price × Qty budgets, parallel actuals, multiple accounts, gold, land, retirement deposits, and debts all tracked in one workbook. The model works, but spreadsheets are painful to edit on a phone: meaningful updates need a laptop, so daily expense capture slips. The integrated view of investments, properties, and cashflow also doesn't survive the move to mobile.

Sakuwise rebuilds that exact workflow as a phone-native app — same Plan → Track ritual, same allocation buckets, same end-of-month reconciliation — without giving up data ownership.

## Goals

Sakuwise V1 aims to:

1. **Replace the monthly spreadsheet workflow** end-to-end: budget planning with configurable 50/30/20 allocations, daily income & expense capture, multi-account balance tracking, investment tracking (gold, land/property, deposits/pension), debt documentation, and a unified net-worth dashboard.
2. **Cover the Indonesian middle-class money picture** in one place: Rupiah-only, Bahasa Indonesia primary with English secondary, categories that match how people actually spend here (kos, BPJSTK, PBB, THR, mudik, kondangan, etc.).
3. **Guarantee strong privacy**: no internet dependency for core features, encrypted at rest, user-controlled encrypted backups.
4. **Ship a complete V1** that the author can dogfood as the primary user before any wider release.

### Non-goals (V1)

Cloud sync, dead-man's-switch / emergency contact, foreign currencies, income-slip OCR, full loan amortization, multi-user/family sharing, iOS, and any form of telemetry or analytics. All parked in the V2 backlog.

## Privacy

Sakuwise is **local-first** by design:

- The Android manifest does **not** request `INTERNET` permission. No data ever leaves the device automatically.
- The SQLite database is encrypted at rest via **SQLCipher** with a 256-bit AES key wrapped by Android Keystore (hardware-backed where available).
- Backups produce a single `.sakuwise` file: AES-256-GCM ciphertext keyed off a user-set PIN/passphrase via **Argon2id** (~64 MB memory, ~1s on a mid-range Android). The user picks where it goes (local storage, USB, manual upload to a drive of their choice).
- The app collects **zero** personal identifiers — no email, no phone, no NIK. Just a nickname the user picks at onboarding.
- No analytics SDK, no crash reporting SDK, no third-party telemetry.

## Feature Overview

| Module | Highlights |
|---|---|
| **Onboarding** | < 30 second flow: language, nickname + PIN, biometric toggle, privacy notice, first account |
| **Accounts** | Cash / Bank / E-Wallet types, hybrid auto-balance + monthly reconciliation snapshots |
| **Plan & Allocations** | Three-tier tree (Allocation → Category → Plan Item), configurable 50/30/20 split, recurring items auto-roll into the next period |
| **Transactions** | Income / Expense / Transfer with backdating, optional receipt photo (encrypted JPEG BLOB), debt linkage |
| **Dashboard** | Greeting + period · allocation progress · income vs expense · daily-remaining budget · top categories · account balances · net worth · recent transactions · backup banner |
| **Gold** | Per-batch buy date, weight, serial, purchase price; global sell-price input drives live valuation + profit/loss |
| **Property / Land** | Name, location, SHM ID, size, buy date + price, optional current value, PBB tax payment sub-records |
| **Deposits / Pension** | DPLK, BPJSTK JHT, time deposits — monthly balance snapshots with line chart |
| **Debt** | Two-way (I-owe / owed-to-me) with payment history; optional account linkage that creates real cash-flow transactions |
| **OCR Receipts** | On-device ML Kit Text Recognition (no upload) — camera, gallery, or Android share intent → pre-filled expense draft |
| **Backup & Restore** | One encrypted file, restore on a new device with PIN/passphrase; automatic daily Google Drive backup (PIN stored in Keystore); rolling 3-copy local backup; 30-day yellow banner, 60-day blocking modal |
| **Reminders** | WorkManager-scheduled recurring expense reminders (opt-in, requires POST_NOTIFICATIONS) |
| **Settings** | Language, biometric, auto-lock (1/5/15/30 min), period start day (1–28), default allocations, global gold sell price, backup management |

A full feature spec lives in [`design/uploads/Sakuwise PRD v1.3 (ID).md`](design/uploads/Sakuwise%20PRD%20v1.3%20(ID).md).

## Screenshots

<p align="center">
  <img src="docs/screenshots/01-home.png" alt="Beranda / Dashboard" width="22%">
  <img src="docs/screenshots/02-plan.png" alt="Plan" width="22%">
  <img src="docs/screenshots/03-assets.png" alt="Aset & Kekayaan" width="22%">
  <img src="docs/screenshots/05-me.png" alt="Saya / Settings" width="22%">
</p>

<p align="center">
  <img src="docs/screenshots/04-history.png" alt="Riwayat Transaksi" width="22%">
  <img src="docs/screenshots/06-backup.png" alt="Backup & Pemulihan" width="22%">
</p>

From left to right: **Beranda** (SISA ANGGARAN hero · anggaran harian · Semua Riwayat Transaksi card · transaksi terbaru · backup banner) · **Plan** (period chip · expected-income row · allocation filter chips · empty state with template shortcut) · **Aset** (TOTAL KEKAYAAN hero + trend chart + four asset-class cards: Akun, Emas, Properti, Deposito) · **Saya** (profile card · Plan / Keamanan / Backup & Data settings sections). Below: **Riwayat Transaksi** (month picker · Pemasukan/Pengeluaran/Saldo summary · transaction filter chips · search) · **Backup & Pemulihan** (Cloud Backup/Google Drive section + local backup flow + how-it-works explainer).

Screenshots captured in Bahasa Indonesia locale.

## Technical Architecture

- **Platform:** Android 8.0+ (API 26), single-activity, Kotlin.
- **UI:** Jetpack Compose + Material 3, a small in-house design system (`SwTheme`, `SwType`, `SwButton`, `SwField`, …) tuned to the prototype.
- **Storage:** Room over SQLCipher 4.x — full DB encryption with a Keystore-wrapped DEK. Photos stored as compressed JPEG BLOBs inside the encrypted DB (~200 KB target).
- **DI:** Hilt.
- **OCR:** Android ML Kit Text Recognition v2 (on-device).
- **Background work:** WorkManager for the daily net-worth snapshot and reminder notifications.
- **Crypto:** AES-256 (SQLCipher + GCM for backup), Argon2id for PIN/passphrase-to-key derivation.
- **Localization:** `values/` (id) + `values-en/` (English). Per-app locale via `AppCompatDelegate.setApplicationLocales` (API 33+); a startup reconciler keeps `prefs.language` in sync with whichever side was changed last (in-app picker or system Settings → App info → Language).
- **Static analysis:** detekt + ktlint run on every CI push (GitHub Actions); a baseline file freezes pre-existing legacy findings so only new issues block the build.
- **No analytics, no crash reporting SDK, no internet permission.** Crash/ANR data is read from Android Vitals in Play Console — zero code change required.

## Default Out-of-the-Box

A fresh install lands the user on a working app with: Bahasa Indonesia, biometric unlock enabled, one "Tunai" account with Rp 0 balance, no plan yet (empty state with a one-tap "Apply Starter Template" banner), no investments, no debts. Greeted by chosen nickname, then the dashboard.

## Building

Requirements:

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK with platform-tools

Then:

```bash
git clone https://github.com/gustiadhitya8/sakuwise-android.git
cd sakuwise-android
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # to install on a connected device/emulator
```

Debug APK lands in `app/build/outputs/apk/debug/`. The debug build's `applicationIdSuffix` is `.debug`, so the package on device is `com.gustiadhitya.sakuwise.debug` and the release build can coexist.

## Project Status

**Current release: v1.0.4 (versionCode 5) — live on Google Play Store.**

V1 is in active use by the author as the primary personal finance tool. All major modules are implemented, exercised on emulator and physical device, and live in production.

### What's in v1.0.4 — Hardening & External-User Readiness
- **Safe database migrations** — `fallbackToDestructiveMigration` removed; a tested migration chain now covers schema versions 1–5. An unmigrated schema fails loudly at open time rather than silently wiping user data. Schema JSON is exported and committed as a baseline.
- **Backup format versioning** — `.sakuwise` files now carry an explicit version marker. Older backups still restore on newer app versions; a version-too-new error is surfaced clearly if the file is newer than the app.
- **CSV/XLSX round-trip** — Export column order and names now match the import format exactly. A round-trip unit test covers both id-ID and en-US header variants.
- **Complete English localisation** — All previously hardcoded Indonesian strings (PDF export, backup error messages, transaction history, OCR screen, pickers, dashboard) are now in `strings.xml` and `strings-en.xml`. Zero leaks verified by manual walkthrough across all four tabs.
- **Faster cold start** — Baseline Profile bundled; startup method hints generated on emulator and compiled into the APK.
- **Static analysis & CI** — detekt + ktlint wired in with a legacy baseline; GitHub Actions runs build + unit tests + detekt on every push and pull request.
- **Crash monitoring** — No SDK added. Crash and ANR data is read from Android Vitals in Play Console, keeping the no-telemetry promise intact.

### What's in v1.0.3
- **Auto-backup Drive mandiri** — Google Drive auto-backup now creates a fresh encrypted backup daily without requiring a prior manual backup. The encryption PIN is stored securely in Android Keystore (AES-256-GCM) and retrieved by the WorkManager background job.
- **Live account balance in pickers** — Account picker and transaction form subtitles now show the live computed balance (income + transfers − expenses − fees + reconciliation), not the stale seed balance.
- **Dashboard reactivity fix** — Changing a transaction date in history now immediately updates the dashboard. Root cause: `Transaction.equals()` was id-only, causing StateFlow deduplication to swallow field-only edits.
- **Reconciliation display** — Positive reconciliation (user has more cash than computed) now displays with a green up-arrow, not a red expense icon.
- **Backup status colour fix** — The backup row on the Me screen now shows warning yellow only when no backup exists or the last backup is older than 30 days; green/neutral when recent.

### V1.1 backlog
- Auto-fire recurring-income worker (UseCase already implemented, not yet scheduled)
- Account detail — bank account number, branch, notes fields
- Full dark-mode visual walkthrough (onboarding verified; detail screens not eye-checked)
- Inline logo in exported PDF
- V2: cloud sync, foreign currencies, iOS, family sharing

## Repository Layout

```
app/
├── src/main/java/com/gustiadhitya/sakuwise/
│   ├── app/                # Application, MainActivity, AppNavGraph, lock controller
│   ├── core/
│   │   ├── common/         # date/rupiah formatters, locale-aware helpers
│   │   ├── crypto/         # PinStore, KeyManager, BackupService, AutoBackupPinStorage
│   │   ├── data/           # repository impls + Mappers
│   │   ├── database/       # Room entities, DAOs, SakuwiseDatabase, migrations
│   │   ├── datastore/      # UserPreferencesRepository (DataStore)
│   │   ├── designsystem/   # SwTheme, SwType, SwButton, SwField, SwCard, …
│   │   ├── domain/         # models + repository interfaces + UseCases
│   │   └── work/           # NetWorthSnapshotWorker, DriveAutoBackupWorker
│   └── feature/
│       ├── onboarding/     # 4-step flow + locale picker
│       ├── dashboard/      # main screen
│       ├── plan/           # plan tree CRUD, allocation card, period picker
│       ├── transaction/    # Expense/Income/Transfer forms + OCR
│       ├── asset/          # accounts, gold, land, deposit, debt
│       ├── settings/       # hub + sub-screens (backup, PIN, export, …)
│       └── lock/           # PIN/biometric unlock
└── src/main/res/
    ├── values/             # Bahasa Indonesia strings (default)
    └── values-en/          # English strings
design/                     # PRD, design concept, handoff spec, prototype screens
```

## License & Attribution

Personal project by Gusti Adhitya. Sakuwise is free; an in-app donation link routes to external platforms (Saweria / Trakteer / QRIS) — no payment processing happens inside the app.

The PRD was co-authored with Anthropic's Claude. Application code is co-authored with Claude Code under the author's direction.
