# Sakuwise — Developer Handoff Spec v2

**Versi:** 2.0 (akhir Milestone 4b)
**Tanggal:** 16 Mei 2026
**Untuk:** Claude Code / developer Android (Kotlin + Jetpack Compose)
**Sumber visual:** `Sakuwise Prototype.html` + `Sakuwise Brand Identity.html`

> Dokumen ini menerjemahkan setiap keputusan desain ke instruksi konkret yang
> bisa langsung diimplementasi di Compose. Setiap nilai yang ada di sini
> harus dipakai persis seperti tertulis. Kalau ada selisih antara dokumen ini
> dan prototipe HTML, **prototipe yang menang**.

---

## 1. Color Tokens

Token name mengikuti Material 3 convention. Semua hex tersedia di
`brand/tokens.jsx` di repo prototipe. Di Compose: definisikan di `Color.kt`,
lalu wire ke `ColorScheme` via `lightColorScheme()` / `darkColorScheme()`.

### Light Theme

| Token                  | Hex        | Material 3 mapping        |
|------------------------|------------|---------------------------|
| `bg`                   | `#F5F1E8`  | `background`              |
| `surface`              | `#FAF7F0`  | `surface`                 |
| `surfaceElev`          | `#FFFFFF`  | `surfaceContainerHigh`    |
| `ink`                  | `#1A2520`  | `onBackground` / `onSurface` |
| `inkMuted`             | `#5C6963`  | `onSurfaceVariant`        |
| `inkSubtle`            | `#8B948F`  | `outline`                 |
| `border`               | `#E8E0CC`  | `surfaceVariant`          |
| `borderStrong`         | `#D6CDB4`  | `outlineVariant`          |
| `primary`              | `#0F4C3A`  | `primary`                 |
| `primaryHover`         | `#0A3A2C`  | (state-pressed)           |
| `onPrimary`            | `#F5F1E8`  | `onPrimary`               |
| `primaryContainer`     | `#D4E8DC`  | `primaryContainer`        |
| `onPrimaryContainer`   | `#0A2E22`  | `onPrimaryContainer`      |
| `accent`               | `#7BC4A4`  | `secondary`               |
| `accentSoft`           | `#D4E8DC`  | `secondaryContainer`      |
| `success`              | `#2D7A4F`  | (semantic, custom)        |
| `successSoft`          | `#D6EDDC`  | (semantic-container)      |
| `warning`              | `#C68A2E`  | (semantic)                |
| `warningSoft`          | `#F4E4C8`  | (semantic-container)      |
| `danger`               | `#B84545`  | `error`                   |
| `dangerSoft`           | `#F1D6D6`  | `errorContainer`          |
| `info`                 | `#4A6FA5`  | (tertiary)                |
| `infoSoft`             | `#D6E0EE`  | (tertiary-container)      |

### Dark Theme

| Token                  | Hex        |
|------------------------|------------|
| `bg`                   | `#0F1411`  |
| `surface`              | `#1A211D`  |
| `surfaceElev`          | `#232B26`  |
| `ink`                  | `#F0EDE3`  |
| `inkMuted`             | `#A8B0AB`  |
| `inkSubtle`            | `#6B7570`  |
| `border`               | `#2D3631`  |
| `borderStrong`         | `#3D4742`  |
| `primary`              | `#7BC4A4`  |
| `primaryHover`         | `#9DD4BA`  |
| `onPrimary`            | `#0A1F18`  |
| `primaryContainer`     | `#1F3329`  |
| `onPrimaryContainer`   | `#C4E8D4`  |
| `accent`               | `#C4E8D4`  |
| `accentSoft`           | `#1F3329`  |
| `success`              | `#6DC48F`  |
| `successSoft`          | `#1E3526`  |
| `warning`              | `#E0A954`  |
| `warningSoft`          | `#3B2E18`  |
| `danger`               | `#D67373`  |
| `dangerSoft`           | `#3D1F1F`  |
| `info`                 | `#7FA0C7`  |
| `infoSoft`             | `#1E2A3A`  |

### Aturan Pemakaian

- **Hero card** background: `primary` di Light, **tetap** `primary` di Dark (mint sage di Dark masih jadi primary).
- Untuk konteks "Wants" (alokasi 30%), bg hero pakai `accent`. Text di atas mint **harus** pakai fixed dark `#0A2820` (kontras WCAG AA, terlepas dari theme).
- **Status semantic** (success/warning/danger/info) jangan dipakai sebagai brand color — itu reservoir untuk feedback (toast, badge, banner).

---

## 2. Typography

Font family: **Figtree** (Google Fonts). Fallback: `system-ui, -apple-system, sans-serif`.
Tabular nums (`fontFeatureSettings = "tnum"`) **wajib** dipakai untuk **semua nominal Rupiah**.

| Style       | Size | Line  | Weight | Tracking   | Pemakaian              |
|-------------|------|-------|--------|------------|------------------------|
| Display L   | 40   | 48    | 700    | −0.02em    | Hero splash            |
| Display M   | 32   | 40    | 700    | −0.02em    | Section headers        |
| H1          | 26   | 34    | 700    | −0.01em    | Screen titles          |
| H2          | 20   | 28    | 600    | −0.005em   | Card titles            |
| H3          | 17   | 24    | 600    | 0          | List headers           |
| Body L      | 16   | 24    | 400    | 0          | Default body           |
| Body        | 14   | 20    | 400    | 0          | Secondary text         |
| Caption     | 12   | 16    | 500    | +0.01em    | Meta, labels           |
| Amount XL   | 36   | 40    | 700    | −0.02em    | Dashboard hero amount  |
| Amount L    | 22   | 28    | 600    | −0.01em    | Card amount            |
| Amount      | 16   | 22    | 600    | 0          | List item amount       |

Section label (uppercase pill): 11px, weight 700, tracking +0.08em, `inkSubtle`.

---

## 3. Spacing & Radii

### Spacing scale (dp)
`4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 / 64`

Card padding default = `16`. Page horizontal padding = `20`. Phone vertical content padding-bottom = `100` (untuk clear tab bar).

### Border radii (dp)

| Token      | Value | Pemakaian                       |
|-----------|-------|---------------------------------|
| `xs`      | 4     | Tag, badge                      |
| `sm`      | 8     | Small button, chip pill         |
| `md`      | 12    | Field, icon container           |
| `lg`      | 16    | Card                            |
| `xl`      | 20    | Large card                      |
| `2xl`     | 22-28 | Hero card, sheet top corners    |
| `full`    | 9999  | Pill, FAB                       |

Phone bezel radius = 36-40. App icon squircle radius = `size × 0.235` (≈ 25.4dp on 108dp adaptive).

### Elevation / Shadow

- **Card default:** `0 0 0 1px var(--border)` (hairline border, no shadow)
- **FAB / sticky CTA:** `0 8px 20px rgba(15,76,58,0.25)` di Light; `0 8px 20px rgba(123,196,164,0.25)` di Dark
- **Sheet:** `0 -8px 30px rgba(0,0,0,0.18)`
- **Phone frame (preview):** `0 30px 80px rgba(15,76,58,0.18)` Light; `0 30px 80px rgba(0,0,0,0.55)` Dark

---

## 4. Animation Tokens

| Nama            | Durasi  | Easing                          | Pakai untuk                  |
|-----------------|---------|----------------------------------|------------------------------|
| `quick`         | 120ms   | linear                          | Hover/press feedback         |
| `default`       | 200ms   | `cubic-bezier(.2,.7,.3,1)`      | State toggle, sheet open     |
| `medium`        | 280ms   | `cubic-bezier(.2,.7,.3,1)`      | Screen transition slide-in   |
| `slow`          | 400ms   | `cubic-bezier(.2,.7,.3,1)`      | Progress bar fill            |
| `splash-fade`   | 600ms   | `cubic-bezier(.2,.7,.3,1)`      | Splash entrance staggered    |

Press feedback (`sw-press` di prototipe): scale `0.97`, opacity `0.85`, durasi 100ms ease.

Target framerate **60fps** minimum. Avoid layout thrashing dengan animasi yang `transform` saja (jangan `width`/`height` selain progress bars).

---

## 5. Screens → Compose Composables

| Screen                  | Source file                         | Suggested Composable name       |
|------------------------|--------------------------------------|----------------------------------|
| Splash                 | `screens-onboarding.jsx`             | `SplashScreen`                   |
| Onboarding 1 (Bahasa)  | `screens-onboarding.jsx`             | `OnbLanguageScreen`              |
| Onboarding 2 (Identitas) | `screens-onboarding.jsx`           | `OnbIdentityScreen`              |
| Onboarding 3 (Privacy) | `screens-onboarding.jsx`             | `OnbPrivacyScreen`               |
| Onboarding 4 (Akun)    | `screens-onboarding.jsx`             | `OnbFirstAccountScreen`          |
| Dashboard              | `screens-dashboard.jsx`              | `DashboardScreen`                |
| Plan                   | `screens-plan.jsx`                   | `PlanScreen`                     |
| Add Transaction Picker | `screens-addtxn.jsx` `AddTxnPicker`  | `AddTxnPickerSheet`              |
| Expense Form           | `screens-addtxn.jsx` `ExpenseForm`   | `ExpenseFormScreen`              |
| Aset Hub               | `screens-assets.jsx`                 | `AssetsHubScreen`                |
| Akun list              | `screens-assets.jsx`                 | `AccountsListScreen`             |
| Emas list              | `screens-assets.jsx`                 | `GoldListScreen`                 |
| Emas detail            | `screens-assets.jsx`                 | `GoldDetailScreen`               |
| Tanah list             | `screens-assets-detail.jsx`          | `LandListScreen`                 |
| Tanah detail (+pajak)  | `screens-assets-detail.jsx`          | `LandDetailScreen`               |
| Deposito list          | `screens-assets-detail.jsx`          | `DepositListScreen`              |
| Deposito detail (chart)| `screens-assets-detail.jsx`          | `DepositDetailScreen`            |
| Hutang list            | `screens-assets-detail.jsx`          | `DebtListScreen`                 |
| Hutang detail          | `screens-assets-detail.jsx`          | `DebtDetailScreen`               |
| Saya (Settings hub)    | `app.jsx` `MeScreen`                 | `MeScreen`                       |

Detail "Settings", "Reconciliation", "Pemasukan Form", "Transfer Form",
"Backup setup", "Donasi" akan dirilis di Milestone 4b.

---

## 6. Komponen yang Reusable

Lihat `proto/components.jsx`. Mapping suggested ke Compose:

| Prototipe              | Compose                              | Notes                                   |
|------------------------|--------------------------------------|-----------------------------------------|
| `SW_PhoneFrame`        | (n/a — host scaffold)                | Activity / Scaffold native              |
| `SW_StatusBar`         | System status bar                    | `WindowCompat.setDecorFitsSystemWindows = false` |
| `SW_TopBar`            | `TopAppBar` (Material 3)             | Left-aligned title; tidak center        |
| `SW_TabBar`            | `NavigationBar` + custom FAB center  | FAB diangkat -16dp                      |
| `SW_Bar`               | `LinearProgressIndicator`            | Custom — pakai dual segment over-budget |
| `SW_Card`              | `Card` w/ outlined style             | `OutlinedCard` w/ hairline border       |
| `SW_Button`            | `Button` variants                    | Primary / Secondary / Outline / Ghost / Danger |
| `SW_Field`             | `OutlinedTextField`                  | Border 1.5dp                            |
| `SW_Sheet`             | `ModalBottomSheet`                   | Radius 24dp top                         |
| `SW_Chip`              | `FilterChip`                         |                                          |
| `SW_Amount`            | Composable formatter                 | Pakai `LocalNumberFormat` Indonesia     |
| `SW_AccountIcon`       | `Box` w/ tint                        |                                          |
| `SW_CategoryDot`       | `Box` w/ first-letter                |                                          |
| `SW_Toggle`            | `Switch`                             | Track filled when checked               |

---

## 7. Format Konvensi

### Currency (IDR)

- Symbol: `Rp` (spasi setelah, mis. `Rp 1.500.000`)
- Thousands: `.` (titik)
- Decimal: `,` (koma)
- Rupiah short: `Rp 1.5 jt` (jutaan), `Rp 1.5 M` (miliar), `Rp 850rb` (ribu)
- **TIDAK PERNAH** desimal di tampilan biasa (jt/M dengan 1 desimal cukup)

### Tanggal

- Default: `15 Mei 2026` (DD MMM YYYY, Bahasa Indonesia)
- Relatif: `Hari ini` / `Kemarin` / `3 hari lalu` / lalu fallback ke absolute

### Singkatan

- `jt` = juta, `M` = miliar, `rb` = ribu, `g` = gram, `m²` = meter persegi

---

## 8. Accessibility Notes (Milestone 4b akan lebih lengkap)

- Touch target **minimum 48dp** untuk semua interactive element
- Body text ≥ 14sp, hero number ≥ 30sp
- WCAG AA contrast ratio (4.5:1 untuk teks normal, 3:1 untuk teks ≥18sp)
- Setiap icon-button **wajib** `contentDescription` (Compose) — sudah disediakan via `aria-label` di prototipe
- Color **tidak boleh** jadi satu-satunya carrier informasi:
  - Overspending: warna merah **+** label "Over" + nominal
  - Success: warna hijau **+** icon checkmark
- Support font scaling Android (gunakan `sp` bukan hardcoded `dp` untuk text)

---

## 9. Layar M4b (selesai)

Semua layar berikut sudah diimplementasi di prototipe — Claude Code bisa langsung mengacu:

| Layar | File | Catatan |
|---|---|---|
| Form Pemasukan | `screens-txn-forms.jsx` `IncomeForm` | Hero hijau success, kategori sumber picker (5 default Gaji/Bonus/THR/Sampingan/Lainnya), recurring toggle |
| Form Transfer | `screens-txn-forms.jsx` `TransferForm` | Hero info-blue, from→to picker dengan swap button, fee field, summary di bawah |
| Settings Hub (Saya) | `screens-settings.jsx` `SettingsHub` | Profile card hero + grouped settings list |
| Persentase Alokasi | `screens-settings.jsx` `AllocationEditor` | 3 slider + validasi total=100% |
| Tanggal Mulai Periode | `screens-settings.jsx` `PeriodStartSettings` | Grid 28 hari, fallback note Feb |
| Harga Emas Global | `screens-settings.jsx` `GoldPriceSettings` | Manual update field |
| Auto-lock | `screens-settings.jsx` `AutoLockSettings` | 5 opsi (Langsung/1m/5m/15m/30m) |
| PIN & Biometrik | `screens-settings.jsx` `PinSettings` | Toggle biometrik + ubah PIN |
| Bahasa | `screens-settings.jsx` `LanguageSettings` | ID / EN radio |
| Tentang | `screens-settings.jsx` `AboutScreen` | Logo + versi + lisensi |
| Export & Reset | `screens-settings.jsx` `ExportResetSettings` | Confirmation dialog dengan warning danger |
| Donasi | `screens-settings.jsx` `DonateScreen` | Saweria/Trakteer cards + QRIS placeholder |
| Backup Hub | `screens-backup-misc.jsx` `BackupSettings` | Status banner + 4-step how-it-works |
| Set PIN Backup | `screens-backup-misc.jsx` `BackupSetPinFlow` | 2-stage: enter → confirm |
| Backup Now | `screens-backup-misc.jsx` `BackupNowFlow` | 3-stage: encrypting → pick location → done |
| Restore | `screens-backup-misc.jsx` `RestoreFlow` | File picker + PIN entry |
| Reconciliation | `screens-backup-misc.jsx` `ReconciliationFlow` | 3-stage: input → confirm → done |
| OCR Capture | `screens-backup-misc.jsx` `OcrFlow` | Camera view → processing → review extracted fields |
| Net Worth Chart | `screens-assets.jsx` `NetWorthTrendCard` | Line+area chart, 3M/6M/1Y/Semua selector |
| Hutang Chart | `screens-assets-detail.jsx` `DebtOutstandingChart` | Outstanding line over time, derived dari payments |

## 10. Component patterns baru di M4b

### `SimpleSettingsScreen`
Shell untuk sub-settings: top bar + back + scroll body. Pakai untuk semua settings detail screens.

### `SettingsGroup` + `SettingsRow`
Grouped list pattern. `SettingsRow` accepts `icon | label | value | sub | danger | warning | onClick`. Auto-handles divider (`last` prop).

### `PinInput`
6-digit boxed input. Filled state pakai solid dot. Tap to fill (prototype-only — production pakai numpad).

### Chart pattern (SnapshotChart)
Generic line+area chart, accepts:
- `snapshots: [{date, balance | value}]`
- `lineColor` (default `c.primary`)
- `height` (default 110)

Pakai `<linearGradient>` untuk area fill (22% opacity di top → 0% di bottom). Marker `r=2.5` (intermediate), `r=4` (last).

### Confirmation dialog pattern
Pakai `SW_Sheet` dengan icon hero (warning/info), title bold, copy explainer, primary action + ghost cancel. Lihat `ExportResetSettings` untuk reference.

### Empty state pattern (implicit)
List screens (Akun, Emas, Tanah, Deposito, Hutang) sudah punya FAB plus untuk tambah. Empty state real (zero data) bisa pakai PostIt-style card dengan icon + copy + action button. Implementasikan saat zero data terdeteksi.

---

## 10. iOS Portability Catatan

Desain ini disusun supaya bisa di-port ke iOS dengan friction minimal. Pattern yang
kompatibel cross-platform:

- ✅ Bottom tab bar — sesuai `UITabBarController` iOS HIG
- ✅ Bottom sheet — sesuai `UISheetPresentationController` iOS 15+
- ✅ Inline form fields — sesuai gaya iOS native
- ✅ Token-based color system — bisa di-port langsung ke iOS asset catalog
- ✅ Figtree tersedia di iOS via Google Fonts SDK / SwiftUI custom font
- ⚠️ FAB di tab bar — di iOS pakai prominent action di nav bar atau toolbar (bukan center FAB Material)
- ⚠️ Material-specific ripple — di iOS ganti opacity/scale press feedback (sudah diimplementasi via `sw-press`)

---

## 11. Brand Asset Files

Logo, app icon, dan wordmark: lihat `Sakuwise Brand Identity.html` (canvas).
Logo terpilih: **Konsep A — Daun** (saku/kantong dengan motif daun untuk
pertumbuhan).

App icon adaptive (Android 108dp):
- `ic_launcher_background.xml` = solid `primary` (#0F4C3A)
- `ic_launcher_foreground.xml` = mark Daun warna `onPrimary` (#F5F1E8), di safe zone 66dp/108dp

---

*Dokumen ini akan di-update di akhir tiap milestone. Versi 3.0 setelah Milestone 4c selesai.*

---

## 11. Layar M4c (selesai)

| Layar | File | Catatan |
|---|---|---|
| Account Detail | `screens-assets.jsx` `AccountDetailScreen` | Hero saldo, action Rekonsiliasi + Edit, **Riwayat Snapshot** (tanggal · saldo asli · selisih), Transaksi per akun (expandable), Aksi sheet (edit/export/arsipkan) |
| Snapshot Row | `screens-assets.jsx` `SnapshotRow` | Sub-component utk tabel snapshot history |

### Catatan migrasi M4b → M4c

- **Harga Emas Global** dipindah dari Settings → Aset → Emas (lebih kontekstual)
- **Persentase Alokasi** dihapus dari Plan "Aksi" sheet, stay di Settings
- **Tombol Rekonsiliasi** dari row Account list → Account Detail action button
- **Account list rows** sekarang tap-able → buka Account Detail

### Sample format export PDF — `Sakuwise Export PDF Sample.html`

Template laporan bulanan A4 portrait, 1 halaman/bulan. Berisi:
- Header: brand mark + period
- Hero 4-kolom: Total Kekayaan, Pemasukan, Pengeluaran, Sisa
- Allocation breakdown
- Top 10 Kategori (rank gold/silver/bronze)
- Ringkasan Akun + Aset Investasi
- Tabel Transaksi
- Footer: versi, SHA-256 hash

**Print:** A4 portrait, margin 16mm. CSS `@page` configured. Buka file → "Cetak / Simpan PDF".

**Compose impl:** `PrintAttributes.MediaSize.ISO_A4` + `PrintedPdfDocument`, atau WebView print bridge dengan HTML template yang sama (lebih cepat).
