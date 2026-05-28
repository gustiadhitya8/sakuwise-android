# Sakuwise — Project Handoff
> Dokumen ini adalah sumber kebenaran tunggal untuk Claude Cowork dan Claude Design.
> Selalu update setelah setiap rilis. Versi terakhir: **1.0.3 (versionCode 4)** — 2026-05-27.

---

## 1. Ringkasan Proyek

**Sakuwise** adalah aplikasi keuangan pribadi Android berbasis **local-first + encrypted** untuk pengguna Indonesia. Semua data disimpan di perangkat, tidak ada server backend, tidak ada INTERNET permission pada runtime (Drive sync adalah fitur opsional, tidak wajib).

| Atribut | Detail |
|---|---|
| Platform | Android (Kotlin + Jetpack Compose) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| Mata uang | IDR (Rupiah) — only |
| Bahasa UI | Indonesia (default) + English |
| App ID | `com.gustiadhitya.sakuwise` |
| Debug App ID | `com.gustiadhitya.sakuwise.debug` |
| Versi saat ini | **1.0.3 (versionCode 4)** |
| Status | **Live di Play Store** |

---

## 2. Tech Stack

| Layer | Library / Tool | Versi |
|---|---|---|
| Build | AGP | 8.13.2 |
| Build | Gradle | 8.13 |
| Language | Kotlin | 2.0.21 |
| DI | Hilt | 2.52 |
| DI codegen | KSP | 2.0.21-1.0.26 |
| UI | Jetpack Compose BOM | 2025.01 |
| UI | Material 3 | (via BOM) |
| Database | Room | 2.6.1 |
| DB encryption | SQLCipher | 4.6.1 |
| Keystore | androidx.security-crypto | 1.1.0-alpha06 |
| PIN hashing | argon2kt | 1.5.0 |
| Background work | WorkManager | 2.10.0 |
| OCR | ML Kit Text Recognition | 16.0.1 |
| Preferences | DataStore Preferences | 1.1.1 |
| Biometric | biometric-ktx | 2.0.0-alpha05 |
| Cloud backup | Google Drive AppDataFolder API | (bundled) |
| Proto arbiter | `design/Sakuwise Prototype.html` | — |

**Aturan keras:** Tidak ada INTERNET permission. Drive backup opsional dan tidak mengganggu fungsi utama.

---

## 3. Arsitektur

### Struktur Package
```
app/src/main/java/com/gustiadhitya/sakuwise/
├── app/                    # Application, MainActivity, MainViewModel, NavGraph
├── core/
│   ├── common/             # Extensions (toRupiah, toRelativeOrAbsolute, dsb)
│   ├── cloud/              # GoogleDriveBackup, DriveBackupEntry
│   ├── crypto/             # BackupService, BackupCrypto, KeyManager, AutoBackupPinStorage, PinStore
│   ├── data/               # Repository implementations + Hilt bindings
│   ├── database/           # Room DB, DAOs, Entities, Migrations
│   ├── datastore/          # UserPreferences, UserPreferencesRepository
│   ├── designsystem/       # SwTheme, SwType, SwColors, komponen UI reusable
│   ├── domain/             # Model, Repository interfaces, UseCases (pure Kotlin)
│   ├── ui/                 # RupiahText, shared composables
│   └── work/               # WorkManager workers (DriveAutoBackupWorker, NetWorthSnapshotWorker, dll)
└── feature/
    ├── asset/              # Akun, Gold, Land, Deposit, Debt + viewmodel
    ├── dashboard/          # Beranda tab + viewmodel
    ├── lock/               # LockScreen, AppLockController
    ├── notification/       # Reminder setup
    ├── onboarding/         # 4-step flow + viewmodel
    ├── plan/               # Plan tab + viewmodel
    ├── settings/           # Saya tab: semua sub-screen + backup/export
    └── transaction/        # Income/Expense/Transfer forms + OCR + picker sheets + viewmodel
```

### Aturan Arsitektur (wajib diikuti)
1. **Domain layer = pure Kotlin** — `core/domain/` tidak boleh ada import `androidx.*` / `android.*` / Compose
2. **ViewModel inject UseCase saja** — tidak boleh inject Repository atau DAO langsung
3. **Feature module tidak boleh saling depend** — komunikasi lewat data layer
4. **Prototype HTML = arbiter visual** — setiap deviasi visual = bug (`design/Sakuwise Prototype.html`)
5. **Angka IDR pakai tabular numerals** — `fontFeatureSettings = "tnum"` wajib untuk semua tampilan Rupiah

---

## 4. Design System

### Tema
File: `core/designsystem/theme/`

```kotlin
SwTheme.colors  // token warna (light + dark)
SwType          // typography tokens
SwSpace         // spacing tokens
```

### Color Tokens

| Token | Light | Dark | Penggunaan |
|---|---|---|---|
| `sw.primary` | `#2D6A4F` | `#4A9B75` | CTA utama, aksen aktif |
| `sw.onPrimary` | `#FFFFFF` | `#FFFFFF` | Teks di atas primary |
| `sw.primaryContainer` | `#D8F3DC` | `#26442F` | Chip bg, card bg ringan |
| `sw.success` | `#2D6A4F` | `#4A9B75` | Income, saldo positif, rekonsiliasi surplus |
| `sw.successSoft` | `#D8F3DC` | `#1A3524` | Background indikator sukses |
| `sw.danger` | `#C0392B` | `#E57373` | Expense, hapus, error |
| `sw.dangerSoft` | `#FADBD8` | `#3D1A1A` | Background indikator bahaya |
| `sw.warning` | `#D4770A` | `#FFB74D` | Backup overdue, peringatan |
| `sw.warningSoft` | `#FEF3E2` | `#3D2A0A` | Background peringatan |
| `sw.info` | `#1565C0` | `#64B5F6` | Transfer |
| `sw.infoSoft` | `#E3F2FD` | `#0D2A4A` | Background info |
| `sw.ink` | `#1A1A1A` | `#F0F0F0` | Teks utama |
| `sw.inkMuted` | `#6B7280` | `#9CA3AF` | Teks sekunder |
| `sw.inkSubtle` | `#9CA3AF` | `#6B7280` | Teks tersier/hint |
| `sw.bg` | `#F5F7F5` | `#0F1411` | Background halaman |
| `sw.surface` | `#FFFFFF` | `#1A2320` | Card / bottom sheet |
| `sw.border` | `#E5E7EB` | `#2D3B35` | Border tipis |
| `sw.borderStrong` | `#D1D5DB` | `#4A5D55` | Border kuat (PIN cells, inputs) |

### Typography Tokens

| Token | Size | Weight | Penggunaan |
|---|---|---|---|
| `SwType.H1` | 28sp | ExtraBold | Hero amount |
| `SwType.H2` | 22sp | Bold | Section title |
| `SwType.LabelStrong` | 14sp | SemiBold | Row label, field label |
| `SwType.LabelSmall` | 12sp | Regular | Subtitle, hint |
| `SwType.Body` | 14sp | Regular | Body text |
| `SwType.Amount` | 16sp | Bold + tnum | Nilai Rupiah inline |
| `SwType.Caption` | 12sp | SemiBold | Label chip, badge |

### Komponen UI Utama

| Komponen | Lokasi | Keterangan |
|---|---|---|
| `SwTheme` | `core/designsystem/theme/` | Root tema |
| `SwCard` | `core/designsystem/components/` | Container card standar, r16 |
| `SwButton` | `core/designsystem/components/` | CTA — variant: Primary, Outline, Ghost; size: Lg, Md, Sm |
| `SwField` | `core/designsystem/components/` | Input teks — support rupiah, password, prefix |
| `SwBar` | `core/designsystem/components/` | Progress bar alokasi |
| `PinInput` | `core/designsystem/components/` | 6-cell PIN input (tersembunyi IME) |
| `RupiahText` | `core/ui/` | Teks Rupiah — sign (+/−/none), short (jt/M) |
| `SwPickerSheet` | `feature/transaction/ui/` | Bottom sheet standar untuk picker |
| `SwTabBar` | `core/designsystem/components/` | Bottom navigation 4 tab |
| `FieldButton` | `feature/transaction/ui/` | Tombol field di form transaksi |
| `TxnFormShell` | `feature/transaction/ui/` | Shell standar form Income/Expense/Transfer |

### Icon Tab (custom vector, bukan Material Icons)
- `ic_tab_home_outline.xml` / `ic_tab_home_filled.xml`
- `ic_tab_plan_outline.xml` / `ic_tab_plan_filled.xml`
- `ic_tab_assets_outline.xml` / `ic_tab_assets_filled.xml`
- `ic_tab_me_outline.xml` / `ic_tab_me_filled.xml`

---

## 5. Fitur & Status

### 5.1 Navigasi Utama
4 tab di bottom nav:

| Tab | Screen | Status |
|---|---|---|
| 🏠 Beranda | DashboardScreen | ✅ Live |
| 📋 Plan | PlanScreen | ✅ Live |
| 🗃️ Aset | AssetsHubScreen | ✅ Live |
| 👤 Saya | SettingsScreen | ✅ Live |

### 5.2 Onboarding (4 langkah)
| Langkah | Konten | Status |
|---|---|---|
| 1 | Pilih bahasa | ✅ |
| 2 | Nama + PIN 6 digit + opsional biometric | ✅ |
| 3 | Buat akun pertama (nama + tipe + saldo awal) | ✅ |
| 4 | Setup plan (income target + template alokasi 50/30/20) | ✅ |

Data persist ke SQLCipher Room DB + DataStore. Onboarding skip otomatis kalau DB sudah ada.

### 5.3 Dashboard (Beranda)

| Tile | Status |
|---|---|
| Saldo bersih | ✅ Live dari DB |
| Periode plan | ✅ |
| Cashflow bulan ini (Income vs Expense bar) | ✅ |
| Rekap alokasi (Needs/Wants/Invest) | ✅ |
| Transaksi terbaru (5) | ✅ Reactive |
| Strip akun (horizontal scroll, per-account balance) | ✅ |
| Top pengeluaran | ✅ |
| NetWorth trend chart | ✅ Real data dari net_worth_snapshots |
| Daily budget | ✅ |

### 5.4 Plan

| Fitur | Status |
|---|---|
| Hierarki 3-level (Needs/Wants/Invest → Kategori → Item) | ✅ |
| View bulanan dengan period switcher | ✅ |
| Edit income target | ✅ |
| Tambah/edit/hapus item + recurring reminder | ✅ |
| Filter chip (Semua/Needs/Wants/Invest) | ✅ |
| Template starter | ✅ |
| Reset plan | ✅ |
| Per-plan allocation editor | ✅ |
| Sinkron pemasukan berulang (manual trigger) | ✅ |
| Auto-generate next month plan | UseCase ✅, UI belum |

### 5.5 Transaksi

| Tipe | Status | Catatan |
|---|---|---|
| Pengeluaran | ✅ | Plan item picker, account picker (saldo live) |
| Pemasukan | ✅ | Toggle "Pemasukan berulang" |
| Transfer | ✅ | Fee field + summary box |

FAB → AddTxnPickerSheet → pilih tipe → form.

### 5.6 Aset

| Aset | List | Detail | Edit | Extra |
|---|---|---|---|---|
| Akun | ✅ | ✅ | ✅ | Rekonsiliasi 3-stage |
| Emas | ✅ | ✅ | ✅ | Sell flow |
| Tanah/Properti | ✅ | ✅ | ✅ | PBB payment sub-records |
| Deposito | ✅ | ✅ + snapshot chart | ✅ | — |
| Hutang | ✅ | ✅ + outstanding chart | ✅ | Payment linkage UseCase |

### 5.7 Keamanan

| Fitur | Status |
|---|---|
| PIN 6 digit (Argon2id hash) | ✅ |
| Passphrase mode (≥8 karakter) | ✅ |
| Biometric unlock | ✅ |
| Auto-lock (1/2/5/10/30 menit) | ✅ |
| SQLCipher encrypted DB | ✅ |
| DEK via Android Keystore | ✅ |

### 5.8 Backup & Restore

| Fitur | Status | Catatan |
|---|---|---|
| Backup lokal (file `.sakuwise`) | ✅ | Enkripsi Argon2id + AES-256-GCM |
| Restore dari file lokal | ✅ | |
| Google Drive backup manual | ✅ | PIN per backup |
| Google Drive restore | ✅ | |
| **Auto-backup Drive harian** | ✅ v1.0.3 | PIN disimpan Keystore, worker mandiri |
| Rolling local backup (max 3) | ✅ v1.0.3 | Auto-prune |
| Status di halaman Saya | ✅ v1.0.3 | Warna fixed |

**Format file backup:** `.sakuwise` — binary v2: magic `SKWS` + Argon2id salt + AES-GCM nonce + DEK + settings JSON + SQLCipher DB bytes.

### 5.9 OCR

| Fitur | Status |
|---|---|
| Scan struk via kamera | ✅ |
| Pilih gambar dari galeri | ✅ |
| Parser teks Indonesia (keyword scoring, robust) | ✅ |
| Review + edit sebelum submit | ✅ |
| Prefill form Pengeluaran | ✅ |

### 5.10 Settings (Halaman Saya)

| Sub-screen | Status |
|---|---|
| Profil (ganti nama) | ✅ |
| Bahasa (Indonesia/English) | ✅ |
| PIN & Biometric | ✅ |
| Auto-lock | ✅ |
| Periode plan (hari mulai) | ✅ |
| Alokasi default (50/30/20) | ✅ |
| Harga emas (inline di menu Emas) | ✅ |
| Backup & Restore | ✅ |
| Ekspor PDF | ✅ |
| Ekspor CSV/XLSX | ✅ |
| Import CSV | ✅ |
| Donasi | ✅ |
| Tentang Aplikasi | ✅ |
| Reset Aplikasi | ✅ + konfirmasi |
| Lihat onboarding lagi | ✅ |
| Tema (Light/Dark/System) | ✅ |

---

## 6. Apa yang Baru di v1.0.3

### Bug Fixes
1. **Dashboard date-change** — `Transaction.equals()` sebelumnya hanya compare `id`, sehingga StateFlow mendeduplicate perubahan tanggal. Fix: full structural equality.

2. **Saldo akun live di picker & form** — `TxnFormViewModel` sekarang punya `accountBalances: StateFlow<Map<String, Long>>` via `flatMapLatest + combine` atas `AccountDao.observeBalance()`.

3. **Rekonsiliasi positif tampil merah** — Display sign-aware: surplus → success (hijau, ↑, `+`), defisit → danger (merah, ↓, `−`, abs value). Fix di DashboardScreen dan TransactionHistoryScreen.

4. **Warna backup di halaman Saya** — Logic `backupWarn = lastBackupTimestamp != 0L` terbalik. Fix: warning hanya saat belum backup / > 30 hari. Label pakai `maxOf(localTs, driveTs)`.

### Fitur Baru
5. **Auto-backup Drive mandiri** — Worker harian membuat backup baru dari DB langsung. PIN enkripsi disimpan di `AutoBackupPinStorage` (Android Keystore AES-256-GCM, tanpa Tink). Setup PIN sekali lewat dialog saat toggle ON. Worker: load PIN → `BackupService.backup()` → upload Drive → prune local (max 3).

---

## 7. Data Flow Kritis

### Balance Akun
```
AccountDao.observeBalance(id)
  = initialBalance + income_in - expense_out + transfer_in
    - transfer_out_with_fees + debt_inflow - debt_outflow
    + reconciliation (signed)
  → Flow<Long>

TxnFormViewModel.accountBalances
  = flatMapLatest(accounts) { combine(each observeBalance) }
  → StateFlow<Map<String, Long>>
```

### Auto-Backup Flow
```
DriveAutoBackupWorker.doWork() [WorkManager, 1x/hari]:
  1. Check driveBackupEnabled + driveAccountEmail → skip if false/null
  2. AutoBackupPinStorage.loadPin()   ← AES-256-GCM decrypt (Keystore)
     → null? skip (user perlu re-enable auto-backup)
  3. BackupService.backup(pin, localDir)
     → WAL checkpoint → read DB → read DEK → pack payload v2
     → AES-GCM encrypt → write .sakuwise
  4. pruneLocalBackups(max 3)
  5. GoogleDriveBackup.upload(file)
  6. markDriveBackupNow() + markBackupNow()
  7. pin.fill(' ')  ← clear from memory
```

### Rekonsiliasi
```
ReconcileAccountUseCase(accountId, observedBalance):
  delta = observedBalance - AccountDao.computeBalance(accountId)  ← SIGNED
  if delta == 0 → tidak tulis transaksi
  else → write Transaction(type=Reconciliation, amount=delta)

Display:
  delta > 0 → success (hijau, ↑, prefix +)
  delta < 0 → danger (merah, ↓, prefix −, display abs(amount))
```

---

## 8. File & Path Penting

| File | Path | Keterangan |
|---|---|---|
| Prototype HTML | `design/Sakuwise Prototype.html` | **Visual arbiter** |
| Design handoff | `design/design_handoff_sakuwise/` | Per-screen specs |
| PRD | Google Drive: `Sakuwise PRD v1.4 (ID).md` | Requirements |
| Tech Solution | Google Drive: `Sakuwise Technical Solution v1.1.md` | Arsitektur detail |
| DB schema | `app/schemas/` | Room auto-generated |
| Release AAB | `app/build/outputs/bundle/release/app-release.aab` | Output tetap, via `./gradlew exportAab` |
| Release notes | `app/src/main/play/release-notes/` | `id-ID/default.txt`, `en-US/default.txt` |

---

## 9. Backlog V1.2

| Item | Prioritas |
|---|---|
| Auto-fire recurring-income worker | Medium — UseCase sudah ada |
| RegenerateNextPlanUseCase → UI ("Buat Plan Bulan Baru") | Medium |
| Room Migration 1→2 yang proper | Tinggi sebelum scale |
| Dark mode walkthrough lengkap (tab screens + detail) | Medium |
| Inline logo di PDF | Low |

---

## 10. Konvensi Dev

### Build & Deploy
```bash
# Selalu dari worktree (BUKAN main project root)
cd /Users/gustiadhitya/AndroidStudioProjects/Sakuwise/.claude/worktrees/determined-tharp-7e8ff7/

./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (signed)
./gradlew exportAab              # release AAB → lokasi tetap

# Install emulator (BUKAN Samsung RRCY705XMMD kecuali diminta eksplisit)
~/Library/Android/sdk/platform-tools/adb -s emulator-5554 install -r \
  app/build/outputs/apk/debug/app-debug.apk

# Install Samsung (production — konfirmasi user dulu)
~/Library/Android/sdk/platform-tools/adb -s RRCY705XMMD install -r \
  app/build/outputs/apk/release/app-release.apk
```

### Version Bump (setiap rilis)
`app/build.gradle.kts`:
- `versionCode` — integer, selalu naik
- `versionName` — semantic (major.minor.patch)

### Signing
Credentials di `~/.gradle/gradle.properties` (tidak di-commit):
```
SAKUWISE_KEYSTORE_PATH=.../Keystore/gusti_keystore.jks
SAKUWISE_KEY_ALIAS=gaka_digilabs
```

### Testing Rules
- **Emulator only** untuk development dan testing
- **Samsung RRCY705XMMD** = production data — install HANYA setelah user konfirmasi eksplisit
- Tail logcat: `adb logcat | grep -E "FATAL|AndroidRuntime|sakuwise"`

---

*Dokumen ini di-maintain oleh Claude Code. Update terakhir: 2026-05-27 untuk rilis v1.0.3.*
