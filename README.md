# Sakuwise

> A local-first personal finance Android app built for Indonesian users — replaces the laptop-and-spreadsheet routine with a phone-native budgeting, expense, and net-worth tracker.

[![Release](https://img.shields.io/badge/release-v1.0.5-1F8A4C)](https://github.com/gustiadhitya8/sakuwise-android/releases/tag/v1.0.5)
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

- **INTERNET permission** is requested solely for the optional Google Drive auto-backup feature (AppData scope, private to this app). Every other feature — budgeting, tracking, investments, exports — works fully offline with zero network access.
- The SQLite database is encrypted at rest via **SQLCipher** with a 256-bit AES key wrapped by Android Keystore (hardware-backed where available).
- Backups produce a single `.sakuwise` file: AES-256-GCM ciphertext keyed off a user-set PIN/passphrase via **Argon2id** (~64 MB memory, ~1 s on a mid-range Android). The user picks where it goes (local storage, USB, or Google Drive).
- Android OS Auto Backup is explicitly disabled for all sensitive files (`sakuwise.db`, `dek.bin`, `pin.bin`) — no financial data is ever silently uploaded to Google's backup servers.
- The app collects **zero** personal identifiers — no email, no phone, no NIK. Just a nickname the user picks at onboarding.
- No analytics SDK, no crash reporting SDK, no third-party telemetry. Crash/ANR data is read from Android Vitals in Play Console — zero code change required.

## Feature Overview

| Module | Highlights |
|---|---|
| **Onboarding** | < 30 second flow: language, nickname + PIN, biometric toggle, privacy notice, first account |
| **Accounts** | Cash / Bank / E-Wallet types, hybrid auto-balance + monthly reconciliation snapshots |
| **Plan & Allocations** | Three-tier tree (Allocation → Category → Plan Item), configurable 50/30/20 split, recurring items auto-roll into the next period |
| **Transactions** | Income / Expense / Transfer with backdating, optional receipt photo (encrypted JPEG BLOB), debt linkage |
| **Dashboard** | Greeting + period · allocation progress · income vs expense · daily-remaining budget · top categories · account balances · net worth · recent transactions · backup banner |
| **Gold** | Per-batch buy date, weight (physical or digital), serial, purchase price; global sell-price input drives live valuation + profit/loss |
| **Property / Land** | Name, location, SHM ID, size, buy date + price, optional current value, PBB tax payment sub-records |
| **Deposits / Pension** | DPLK, BPJSTK JHT, time deposits — monthly balance snapshots with line chart |
| **Debt** | Two-way (I-owe / owed-to-me) with payment history; optional account linkage that creates real cash-flow transactions |
| **OCR Receipts** | On-device ML Kit Text Recognition (no upload) — camera, gallery, or Android share intent → pre-filled expense draft |
| **Backup & Restore** | One encrypted file, restore on a new device with PIN/passphrase; automatic daily Google Drive backup (PIN stored in Keystore); rolling 3-copy local backup; 30-day yellow banner, 60-day blocking modal |
| **Reminders** | WorkManager-scheduled recurring expense reminders (opt-in, requires POST_NOTIFICATIONS) |
| **Lock Screen** | PIN or biometric (fingerprint/face) unlock; configurable auto-lock timer; content masked in Recents/task-switcher |
| **Settings** | Language (ID/EN), biometric, auto-lock (instant/1/5/15/30 min), period start day (1–28), default allocations, global gold sell price, dark/light/system theme, backup management |

## Screenshots

**Beranda** — hero card SISA ANGGARAN dengan anggaran harian, progress alokasi Kebutuhan/Keinginan/Investasi, strip Aset & Kekayaan.  
**Plan** — periode aktif, expected income, breakdown alokasi 50/30/20, per-kategori planned vs aktual.  
**Aset & Kekayaan** — total net worth hero, tren kekayaan, empat kelas aset (Akun, Emas, Properti, Deposito).  
**Saya** — profil, PIN & Biometrik, Auto-lock, Backup & Data, Ekspor PDF/CSV.  
**Transaksi terbaru** — transaksi tersortir dengan label kategori plan, +/− color coding.  
**Riwayat Transaksi** — month picker, ringkasan Pemasukan/Pengeluaran/Saldo, filter chips, search, pengeluaran per kategori.  
**Lock Screen** — PIN keypad kustom; biometric shortcut; konten juga ter-mask di Recents/app-switcher.

Screenshots diambil dalam Bahasa Indonesia.

## Technical Architecture

- **Platform:** Android 8.0+ (API 26), single-activity, Kotlin.
- **UI:** Jetpack Compose + Material 3, a small in-house design system (`SwTheme`, `SwType`, `SwButton`, `SwField`, …) tuned to the prototype.
- **Storage:** Room over SQLCipher 4.x — full DB encryption with a Keystore-wrapped DEK. Photos stored as compressed JPEG BLOBs inside the encrypted DB (~200 KB target).
- **DI:** Hilt.
- **OCR:** Android ML Kit Text Recognition v2 (on-device).
- **Background work:** WorkManager for the daily net-worth snapshot, Drive auto-backup, and reminder notifications.
- **Crypto:** AES-256 (SQLCipher + GCM for backup), Argon2id for PIN/passphrase-to-key derivation, Android Keystore for DEK and auto-backup PIN storage.
- **Localization:** `values/` (id) + `values-en/` (English). Per-app locale via `AppCompatDelegate.setApplicationLocales`.
- **Static analysis:** detekt + ktlint run on every CI push (GitHub Actions); a baseline file freezes pre-existing legacy findings so only new issues block the build.

## Building

Requirements: Android Studio Hedgehog+, JDK 17, Android SDK.

```bash
git clone https://github.com/gustiadhitya8/sakuwise-android.git
cd sakuwise-android
./gradlew :app:assembleDebug
./gradlew :app:installDebug   # install on a connected device/emulator
```

Debug APK lands in `app/build/outputs/apk/debug/`. The debug build uses `applicationIdSuffix = ".debug"` so it coexists with the release build on device.

## Project Status

**Current release: v1.0.5 (versionCode 6) — in closed testing on Google Play.**

V1 is in active use by the author as the primary personal finance tool. All major modules are implemented, exercised on emulator and physical device, and live in production.

### What's in v1.0.5 — Security & Verification Pass

- **Aset Hub chart accuracy** — Net worth trend chart now correctly includes gold, land, and deposit totals (not just account balances). Snapshots with recorded non-account data use the snapshot value directly; older snapshots without it blend the account history with current non-account totals.
- **Gold icon** — Gold asset entries now display a gold ingot icon instead of a diamond gem.
- **Recents/task-switcher privacy** — App content is masked whenever Sakuwise is in the background, so the system thumbnail never shows financial data regardless of auto-lock setting.
- **Android OS backup hardened** — Sensitive files (`sakuwise.db`, `dek.bin`, `pin.bin`, auto-backup PIN) are now explicitly excluded from Android Auto Backup on all API levels. The previous empty `dataExtractionRules` was silently enabling full cloud backup on API 31+.
- **Atomic transfer writes** — A transfer + fee-expense pair is now written in a single Room `@Transaction`, preventing a partial write if the second insert failed.
- **Notification lock-screen privacy** — Payment reminders no longer expose content on the lock screen (explicit `VISIBILITY_PRIVATE` + neutral public version).
- **Backup tamper-detection tests** — Instrumented tests prove that a modified or corrupted `.sakuwise` file is rejected before any data is touched.
- **Migration data-preservation test** — Instrumented test seeds records at schema v4, migrates to v5, and asserts every row survived with correct values.
- **Room schema JSONs committed** — Schemas 1–5 are now tracked in the repo so `MigrationTestHelper` can read them in CI.

### What's in v1.0.4 — Hardening & External-User Readiness

- **Safe database migrations** — `fallbackToDestructiveMigration` removed; a tested migration chain covers schema versions 1–5.
- **Backup format versioning** — `.sakuwise` files carry an explicit version marker; older backups still restore on newer app versions.
- **CSV/XLSX round-trip** — Export column names match the importer exactly; a unit test covers both locale header variants.
- **Complete English localisation** — All previously hardcoded Indonesian strings now live in `strings.xml` / `strings-en.xml`.
- **Static analysis & CI** — detekt + ktlint + GitHub Actions on every push.
- **Faster cold start** — Baseline Profile bundled.

### What's in v1.0.3

- **Mandiri Drive auto-backup** — Daily Google Drive backup without requiring a prior manual backup. The encryption PIN is stored in Android Keystore.
- **Live account balance in pickers** — Computed balance (not seed balance) shown in transaction forms.
- **Dashboard reactivity fix** — Changing a transaction date now immediately updates the dashboard.

### V1.1 backlog

- Biometric enrollment invalidation — new fingerprint enrollment should require PIN re-confirmation before unlocking (needs UX design)
- Account detail — bank account number, branch, notes fields
- Restore atomicity — two-phase commit so a failed file-swap during restore doesn't leave the app in an unrecoverable state
- Auto-fire recurring-income worker (UseCase already implemented, not yet scheduled)

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
│   │   └── work/           # NetWorthSnapshotWorker, DriveAutoBackupWorker, ReminderWorker
│   └── feature/
│       ├── onboarding/     # 4-step flow + locale picker
│       ├── dashboard/      # main screen
│       ├── plan/           # plan tree CRUD, allocation card, period picker
│       ├── transaction/    # Expense/Income/Transfer forms + OCR
│       ├── asset/          # accounts, gold, land, deposit, debt
│       ├── settings/       # hub + sub-screens (backup, PIN, export, …)
│       └── lock/           # PIN/biometric unlock + Recents masking
├── schemas/                # Room schema JSON exports (v1–v5) for MigrationTestHelper
└── src/main/res/
    ├── values/             # Bahasa Indonesia strings (default)
    └── values-en/          # English strings
design/                     # PRD, design concept, handoff spec, prototype screens
```

## License & Attribution

Personal project by Gusti Adhitya. Sakuwise is free; an in-app donation link routes to external platforms (Saweria / Trakteer / QRIS) — no payment processing happens inside the app.

The PRD was co-authored with Anthropic's Claude. Application code is co-authored with Claude Code under the author's direction.
