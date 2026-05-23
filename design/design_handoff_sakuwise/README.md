# Sakuwise — Developer Handoff Package
**Versi:** 1.0 · Akhir Fase Desain (Milestone 6)  
**Tanggal:** 16 Mei 2026  
**Untuk:** Developer Android (Claude Code + Android Studio)  
**Dibuat oleh:** Gusti Adhitya (founder) + Claude (design phase)

---

## ⚡ Mulai dari Sini

1. Buka `Sakuwise Prototype.html` di browser — ini prototipe clickable 33+ layar dengan semua state, dark/light mode, dan animasi.
2. Baca `Sakuwise Handoff Spec.md` untuk token → Compose mapping yang konkret.
3. Baca `Sakuwise Technical Solution v1.1.md` untuk arsitektur, module structure, dan stack.
4. Dokumen ini (README) adalah **titik temu** yang menyatukan ketiga sumber di atas + Accessibility Audit menjadi instruksi implementasi tunggal.

> **Penting:** File HTML di paket ini adalah **design reference** — prototipe yang menunjukkan tampilan dan perilaku yang diinginkan. Tugas developer adalah **merekrasi desain ini di Android native (Kotlin + Jetpack Compose + Material 3)**, bukan menyalin HTML ke produksi.

---

## 1. Ringkasan Proyek

**Sakuwise** adalah aplikasi personal money tracker Android yang:
- **Local-first** — tidak ada permission `INTERNET`. Data 100% di device.
- **Encrypted at rest** — SQLite via SQLCipher + Android Keystore.
- **Bahasa Indonesia primary, IDR-only** — format Rupiah, locale `id-ID`.
- **Hi-fi pixel-perfect** — prototipe HTML adalah spec visual final.
- **Clean Architecture multi-module** — ~13 Gradle module, reusable sebagai template untuk client work.

**App ID:** `com.gustiadhitya.sakuwise`  
**Min SDK:** 26 (Android 8.0) · **Target SDK:** 35 (Android 15)  
**Tagline:** *Rencanakan. Catat. Tenang.*

---

## 2. Target Stack

| Layer | Tech | Versi |
|---|---|---|
| Bahasa | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BoM 2025.10 |
| DI | Hilt | 2.52 |
| Database | Room + SQLCipher | Room 2.6.1 / SQLCipher 4.6.1 |
| KDF | Argon2id (argon2kt) | 1.5.0 |
| OCR | ML Kit Text Recognition | 16.0.x |
| Image | Coil | 2.7.0 |
| Async | Coroutines + Flow | 1.9.0 |
| Background | WorkManager | 2.10.0 |
| Nav | Compose Navigation (type-safe) | 2.8.x |
| Build | Gradle Kotlin DSL + version catalog + composite build | — |

---

## 3. Module Architecture

```
:app                          # Entry point, navigation host, DI bootstrap
:core:common                  # Utilities, dispatchers, ID locale formatter
:core:model                   # Plain Kotlin data classes (Account, Plan, etc.)
:core:domain                  # Repository interfaces + ~70-80 UseCases
:core:data                    # Repository implementations
:core:database                # Room database, DAOs, SQLCipher wiring
:core:datastore               # DataStore wrappers (non-sensitive prefs)
:core:crypto                  # DEK/KEK management, backup encrypt/decrypt
:core:designsystem            # Theme tokens, SW* components, RupiahText
:core:ui                      # UI helpers, state composables
:core:testing                 # Test fixtures, fakes (test-only)
:feature:onboarding           # Splash, 4-step onboarding
:feature:dashboard            # DashboardScreen
:feature:plan                 # PlanScreen
:feature:transaction          # Expense/Income/Transfer forms + OCR
:feature:asset                # AccountsHub, Gold, Land, Deposit, Debt
:feature:settings             # Settings hub + 15 sub-screens + Backup + Reconciliation
:feature:donation             # DonateScreen
build-logic/                  # Convention plugins (NowInAndroid pattern)
```

**Aturan dependency (di-enforce via Gradle):**
- `:feature:*` hanya depend ke `:core:designsystem`, `:core:ui`, `:core:common`, `:core:model`, `:core:domain`
- `:feature:*` **TIDAK** depend ke `:core:data`, `:core:database`, atau feature lain
- `:core:domain` adalah pure Kotlin — no Android dependency

---

## 4. Design Tokens

### 4.1 Color — Light Theme

```kotlin
// Color.kt di :core:designsystem
val Bg              = Color(0xFFF5F1E8)   // background, cream warm
val Surface         = Color(0xFFFAF7F0)   // cards
val SurfaceElev     = Color(0xFFFFFFFF)   // modal, FAB, elevated
val Ink             = Color(0xFF1A2520)   // primary text
val InkMuted        = Color(0xFF5C6963)   // secondary text
val InkSubtle       = Color(0xFF8B948F)   // tertiary — ≥14sp bold atau ≥18sp ONLY
val Border          = Color(0xFFE8E0CC)
val BorderStrong    = Color(0xFFD6CDB4)
val Primary         = Color(0xFF0F4C3A)   // CTA, brand anchor
val PrimaryHover    = Color(0xFF0A3A2C)   // pressed state
val OnPrimary       = Color(0xFFF5F1E8)
val PrimaryContainer    = Color(0xFFD4E8DC)
val OnPrimaryContainer  = Color(0xFF0A2E22)
val Accent          = Color(0xFF7BC4A4)   // mint, Wants alloc
val AccentSoft      = Color(0xFFD4E8DC)
val Success         = Color(0xFF2D7A4F)
val SuccessSoft     = Color(0xFFD6EDDC)
val Warning         = Color(0xFFC68A2E)
val WarningSoft     = Color(0xFFF4E4C8)
val Danger          = Color(0xFFB84545)
val DangerSoft      = Color(0xFFF1D6D6)
val Info            = Color(0xFF4A6FA5)
val InfoSoft        = Color(0xFFD6E0EE)
```

### 4.2 Color — Dark Theme

```kotlin
val BgDark              = Color(0xFF0F1411)
val SurfaceDark         = Color(0xFF1A211D)
val SurfaceElevDark     = Color(0xFF232B26)
val InkDark             = Color(0xFFF0EDE3)
val InkMutedDark        = Color(0xFFA8B0AB)
val InkSubtleDark       = Color(0xFF7A8480)   // ← 0x6B7570 dinaikkan per A11Y-002
val BorderDark          = Color(0xFF2D3631)
val BorderStrongDark    = Color(0xFF3D4742)
val PrimaryDark         = Color(0xFF7BC4A4)   // mint menjadi primary di dark
val PrimaryHoverDark    = Color(0xFF9DD4BA)
val OnPrimaryDark       = Color(0xFF0A1F18)
val PrimaryContainerDark    = Color(0xFF1F3329)
val OnPrimaryContainerDark  = Color(0xFFC4E8D4)
val AccentDark          = Color(0xFFC4E8D4)
val AccentSoftDark      = Color(0xFF1F3329)
val SuccessDark         = Color(0xFF6DC48F)
val SuccessSoftDark     = Color(0xFF1E3526)
val WarningDark         = Color(0xFFE0A954)
val WarningSoftDark     = Color(0xFF3B2E18)
val DangerDark          = Color(0xFFD67373)
val DangerSoftDark      = Color(0xFF3D1F1F)
val InfoDark            = Color(0xFF7FA0C7)
val InfoSoftDark        = Color(0xFF1E2A3A)
```

**Aturan warna:**
- Hero card background: `Primary` di Light, `Primary` (mint) di Dark — keduanya `Primary`.
- Text di atas mint hero (Wants alloc): **fixed `#0A2820`** (tidak ikut theme, WCAG AA 8.4:1).
- `InkSubtle` hanya untuk teks ≥ 14sp bold atau ≥ 18sp regular (A11Y-001/002).
- Text di atas `Warning` amber: harus ≥ 16sp bold (A11Y-003).

### 4.3 Typography — Figtree

Font: **Figtree** dari `res/font/`. **Wajib offline** — jangan runtime download.

```kotlin
// Type.kt di :core:designsystem
object SW_Type {
  val DisplayL  = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-0.02).em)
  val DisplayM  = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-0.02).em)
  val H1        = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-0.01).em)
  val H2        = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = W600,              letterSpacing = (-0.005).em)
  val H3        = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = W600)
  val BodyL     = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
  val Body      = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
  val Caption   = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = W500,              letterSpacing = 0.01.em)
  val AmountXL  = TextStyle(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-0.02).em, fontFeatureSettings = "tnum")
  val AmountL   = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = W600,              letterSpacing = (-0.01).em, fontFeatureSettings = "tnum")
  val Amount    = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = W600,              fontFeatureSettings = "tnum")
  val SectionLabel = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em) // uppercase
}
```

> ⚠️ **WAJIB:** `fontFeatureSettings = "tnum"` di semua nominal Rupiah. Gunakan `RupiahText` composable dari `:core:ui` — sudah enforce ini. Jangan format Rupiah tanpa `tnum`.

### 4.4 Spacing Scale (dp)

`4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 / 64`

- Card padding default: **16 dp**
- Page horizontal padding: **20 dp**
- Bottom content padding-bottom: **100 dp** (clear tab bar)
- Section vertical gap: **14–16 dp**

### 4.5 Border Radii (dp)

| Token | Value | Pakai |
|---|---|---|
| `xs` | 4 | Badge, tag |
| `sm` | 8 | Chip, small button |
| `md` | 12 | Field, icon container |
| `lg` | 16 | Card |
| `xl` | 20 | Large card |
| `2xl` | 22–28 | Hero card, sheet top |
| `full` | 9999 | Pill, FAB |

App icon squircle: `size × 0.235` ≈ 25.4 dp pada adaptive 108 dp.

### 4.6 Shadows / Elevation

```
Card default:      hairline border 1dp var(--border), NO shadow
FAB / CTA sticky:  0 8 20  rgba(15,76,58,0.25)  light
                   0 8 20  rgba(123,196,164,0.25) dark
Sheet:             0 -8 30 rgba(0,0,0,0.18)
Phone frame mock:  0 30 80 rgba(15,76,58,0.18)  light
                   0 30 80 rgba(0,0,0,0.55)      dark
```

### 4.7 Animation Tokens

| Nama | Durasi | Easing | Pakai |
|---|---|---|---|
| `quick` | 120 ms | linear | Hover/press |
| `default` | 200 ms | cubic-bezier(.2,.7,.3,1) | State toggle, sheet open |
| `medium` | 280 ms | cubic-bezier(.2,.7,.3,1) | Screen slide-in |
| `slow` | 400 ms | cubic-bezier(.2,.7,.3,1) | Progress bar fill |
| `splash-fade` | 600 ms | cubic-bezier(.2,.7,.3,1) | Splash entrance stagger |

Press feedback (`sw-press`): `scale = 0.97`, `alpha = 0.85`, 100 ms ease. Semua interactive element wajib punya ini.

**Reduced motion:** Cek `Settings.Global.ANIMATOR_DURATION_SCALE == 0f`. Bila aktif: splash instant, screen transitions fade 100 ms, bar fills instant, sheet fade (A11Y-012).

---

## 5. Component Library

Semua komponen live di `:core:designsystem`. Source prototipe: `proto/components.jsx`. Setiap komponen harus punya Compose Preview light + dark + state variants, dan di-screenshot via **Paparazzi** untuk regression.

### SwButton

```
Variants: primary | secondary | outline | ghost | danger
Sizes: sm (h=36) | md (h=48) | lg (h=56)
Border radius: 14 dp
Font: 13–16 sp, weight 600, tracking -0.005em
States: default | pressed (scale 0.97 + alpha 0.85) | disabled (alpha 0.5)
```

- Primary: bg=`Primary`, fg=`OnPrimary`
- Secondary: bg=`PrimaryContainer`, fg=`OnPrimaryContainer`
- Outline: bg=transparent, border=`BorderStrong`
- Ghost: bg=transparent, fg=`Primary`
- Danger: bg=`Danger`, fg=white

### SwField

```
Height: 52 dp
Border: 1.5 dp, color=Border (error=Danger)
Border radius: 12 dp
Font: 16 sp, weight 500
Prefix/Suffix: inkMuted, 15 sp weight 600
Hint text: 11 sp below field
Error state: isError=true, supportingText spesifik (A11Y-010)
```

Rupiah field: prefix "Rp", `fontFeatureSettings = "tnum"` wajib.

`FieldButton` — read-only field-style untuk picker (akun, tanggal, kategori). Sama styling dengan SwField tapi cursor pointer.

`PinInput` — 6 sel grid, h=56 dp per sel, dot solid saat filled.

### SwCard

```
Background: Surface
Border: 1 dp, color=Border (hairline — NO shadow)
Border radius: 18 dp
Padding: 16 dp default
```

### SwSheet (ModalBottomSheet)

```
Radius top: 24 dp
Drag handle: 44×4 dp, color=BorderStrong
Animation: slide-up 280 ms cubic-bezier(.2,.7,.3,1)
Shadow: 0 -8 30 rgba(0,0,0,0.18)
Max height: 78% viewport
```

### SwTabBar (NavigationBar)

```
5 slots: Beranda | Plan | FAB | Aset | Saya
FAB center: 56×56 dp, radius 18, elevated -16 dp dari bar
Tab height: ~56 dp + 18 dp bottom padding
Active: filled icon variant + Primary color
Inactive: outline icon + InkSubtle
Label: 10 sp, weight 500→700 active
```

### SwBar (Progress Bar)

```
Height: 6–8 dp
Border radius: = height
Fill animation: 400 ms cubic-bezier(.2,.7,.3,1)
Over-budget: dual segment — primary fill ≤100% + Danger overflow
Track bg: light=#EDE5CF  dark=#2A332E
```

### SwTopBar

```
Height: 48–56 dp
Back button: 40×40 dp visual — expand hit area ke 48×48 via minimumInteractiveComponentSize() (A11Y-004)
Title: left-aligned (BUKAN center), 19 sp weight 700
Sticky: zIndex 5, bg=Bg
```

### SwAmount

```
fontFeatureSettings = "tnum" — SELALU
Letter spacing: -0.02 em
Prefix "Rp": size × 0.62, weight 500, opacity 0.78
Sign (+/−): opacity 0.7
```

Gunakan `RupiahText` composable dari `:core:ui` untuk semua tampilan nominal.

### SwChip

```
Height: 36 dp
Padding: 0 14 dp
Active: bg=Primary, fg=OnPrimary
Inactive: bg=Surface, border=Border
Border radius: 18 dp (pill)
Font: 13 sp, weight 600
```

### SwAccountIcon / SwCategoryDot

```
AccountIcon: Square rounded, bg=PrimaryContainer, fg=OnPrimaryContainer
             Size×0.3 radius, Size×0.5 icon
CategoryDot: Circle, bg = colorHash(name)+"20", fg = colorHash(name)
             First letter, size×0.42 font, weight 700
```

### SettingsRow

```
Height: 64 dp
Layout: icon(24) | label+sub | value | chevron
Variants: default | danger (fg=Danger) | warning (fg=Warning)
Divider: 1 dp border kecuali row terakhir
```

### SnapshotChart

```
Line + area fill, dots
Area: linearGradient 22% opacity top → 0% bottom
Dot intermediate: r=2.5  Dot last: r=4
Color: lineColor prop (default Primary)
Height: 110 dp default
Period selector: 3M/6M/1Y/Semua (32 dp visual → expand hit-area ke 44 dp, A11Y-005)
```

---

## 6. Format Konvensi Indonesia

### Currency (IDR)

```kotlin
// RupiahFormatter.kt di :core:common
fun Long.toRupiah(): String = "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(this)

fun Long.toRupiahShort(): String = when {
    this >= 1_000_000_000 -> "Rp ${(this / 1e9).roundTo1}M"
    this >= 1_000_000     -> "Rp ${(this / 1e6).roundTo1} jt"
    this >= 1_000         -> "Rp ${this / 1000}rb"
    else                  -> "Rp $this"
}
```

- Thousands separator: `.` (titik)
- Decimal: `,` (koma)
- Tampilan biasa: **TIDAK ADA** desimal
- Short: `Rp 1.5 jt` / `Rp 850rb` / `Rp 1.5 M`
- TalkBack override: `contentDescription = "Rp 1 juta 500 ribu"` (A11Y-013)

### Tanggal

```
Default:  "15 Mei 2026"  (DD MMM YYYY Bahasa Indonesia)
Relatif:  "Hari ini" / "Kemarin" / "3 hari lalu"  (< 7 hari)
Fallback: absolute setelah 7 hari
```

### Singkatan

`jt` = juta · `M` = miliar · `rb` = ribu · `g` = gram · `m²` = meter persegi

---

## 7. Screen Inventory — 33+ Layar

Navigasi prototype: buka `Sakuwise Prototype.html` → gunakan Tweaks panel (toolbar kanan) untuk dark mode, skip splash, dsb.

### 7.1 Onboarding (first-run)

| # | Screen | Composable | File |
|---|---|---|---|
| 1 | Splash | `SplashScreen` | `screens-onboarding.jsx` |
| 2 | Sambutan + Bahasa | `Onb_Language` | `screens-onboarding.jsx` |
| 3 | Nama panggilan + PIN + Biometrik | `Onb_Identity` | `screens-onboarding.jsx` |
| 4 | Privacy notice | `Onb_Privacy` | `screens-onboarding.jsx` |
| 5 | Akun pertama | `Onb_FirstAccount` | `screens-onboarding.jsx` |

**Splash:** bg=Bg, logo daun fade-up 600ms → wordmark 220ms delay → tagline 440ms delay. Auto-advance 1.6s.

**OnbShell pattern:** Progress dots → hero artwork → title H1 bold → sub body → form content → primary CTA lg + ghost secondary.

### 7.2 Tab: Beranda (Dashboard)

| # | Screen | Composable | File |
|---|---|---|---|
| 6 | Dashboard | `DashboardScreen` | `screens-dashboard.jsx` |

**Layout sections (scrollable, pb=100):**
1. **Header bar**: Logo lockup kiri + avatar circle kanan (initial nama user)
2. **Greeting**: Nama + sapaan waktu + period pill ("Plan Mei · sisa N hari")
3. **Hero card** (bg=Primary): Label "Sisa Anggaran" + Amount XL + eye-toggle + daily budget sub-card + Pemasukan/Pengeluaran row
4. **Alokasi** (Card): 3 rows Needs/Wants/Invest, each: dot + nama + %, SwBar, angka terpakai/plan, over badge
5. **Pengeluaran Teratas**: 5 kategori + mini horizontal bar
6. **Aset & Kekayaan link**: Card tap → navigasi ke tab Aset
7. **Transaksi Terbaru**: List 6 txn, setiap row: CategoryDot + merchant/cat + date+account + amount
8. **Backup banner** (warningSoft): bila backup tertunda >7 hari

### 7.3 Tab: Plan

| # | Screen | Composable | File |
|---|---|---|---|
| 7 | Plan | `PlanScreen` | `screens-plan.jsx` |

**Layout:**
- Month picker pill (calendar icon + label + chevron-down) → buka MonthPickerSheet
- Summary card: pemasukan diharapkan + total SwBar + terpakai/plan
- Filter chips: Semua / Needs / Wants / Invest
- Per allocation: header row (dot + nama + %) → per CategoryCard
  - CategoryCard: header (expand/collapse) + per PlanItem row
  - PlanItem row: CategoryDot + nama + recurring icon + SwBar + amount + over badge

**Action sheet** (⋯ button): Ubah Pemasukan / Copy dari Bulan Lalu / Template / Export PDF

### 7.4 Tab: + (Tambah Transaksi)

| # | Screen | Composable | File |
|---|---|---|---|
| 8 | Picker sheet | `AddTxnPickerSheet` | `screens-addtxn.jsx` |
| 9 | Pengeluaran | `ExpenseFormScreen` | `screens-addtxn.jsx` |
| 10 | Pemasukan | `IncomeFormScreen` | `screens-txn-forms.jsx` |
| 11 | Transfer | `TransferFormScreen` | `screens-txn-forms.jsx` |
| 12 | OCR Capture | `OcrFlowScreen` | `screens-backup-misc.jsx` |

**Picker sheet:** 3 besar tiles (Pengeluaran bg=Danger soft / Pemasukan bg=Success soft / Transfer bg=Info soft) + OCR receipt option bawah.

**Form pattern (semua tipe):** TopBar (Cancel + Title + Simpan) → Hero card (warna sesuai tipe) → field list → toggle optional → Save button sticky bawah.

**ExpenseForm:** hero=DangerSoft, fields: Jumlah (Rp prefix, tnum) + Tanggal + Akun picker + Kategori + Plan Item picker + Catatan + foto struk. Toggle: recurring.

**IncomeForm:** hero=SuccessSoft (hijau), kategori sumber: Gaji/Bonus/THR/Sampingan/Lainnya, recurring toggle.

**TransferForm:** hero=InfoSoft (biru), from→to akun picker + swap button, nominal + fee field, summary card bawah.

**OcrFlow:** 3 stage — camera view → processing spinner → review extracted fields (merchant/tanggal/total pre-filled, editable).

### 7.5 Tab: Aset

| # | Screen | Composable | File |
|---|---|---|---|
| 13 | Aset Hub | `AssetsHubScreen` | `screens-assets.jsx` |
| 14 | Net Worth Trend | `NetWorthTrendCard` | `screens-assets.jsx` |
| 15 | Akun list | `AccountsListScreen` | `screens-assets.jsx` |
| 16 | Akun detail | `AccountDetailScreen` | `screens-assets.jsx` |
| 17 | Emas list | `EmasListScreen` | `screens-assets.jsx` |
| 18 | Emas detail | `EmasDetailScreen` | `screens-assets.jsx` |
| 19 | Tanah/Properti list | `LandListScreen` | `screens-assets-detail.jsx` |
| 20 | Tanah detail + pajak | `LandDetailScreen` | `screens-assets-detail.jsx` |
| 21 | Deposito list | `DepositoListScreen` | `screens-assets-detail.jsx` |
| 22 | Deposito detail + chart | `DepositoDetailScreen` | `screens-assets-detail.jsx` |
| 23 | Hutang list | `DebtListScreen` | `screens-assets-detail.jsx` |
| 24 | Hutang detail + chart | `DebtDetailScreen` | `screens-assets-detail.jsx` |

**AssetsHub layout:**
- Hero card (Primary): Total Kekayaan + eye-toggle + stacked bar + legend labels + % delta YTD
- NetWorthTrendCard: SnapshotChart line+area, 3M/6M/1Y/Semua selector
- 2×2 grid AssetCard: Akun / Emas / Properti / Deposito (icon + nama + count/weight + value + delta %)
- Hutang row card (DangerSoft bg icon)

**AccountDetail:** hero saldo + tombol Rekonsiliasi + Edit → Snapshot History table (tanggal · saldo asli · selisih, colored) → Transaksi per akun (expandable) → ⋯ action sheet (edit/export/arsipkan).

**Pattern List→Detail→ActionSheet:**
1. List screen (tap row → Detail)
2. Detail screen (hero summary + sub-sections + Edit + ⋯)
3. ⋯ sheet: rename / archive / export / delete (dengan confirmation)

### 7.6 Tab: Saya (Settings)

| # | Screen | Composable | File |
|---|---|---|---|
| 25 | Settings Hub | `SettingsHub` | `screens-settings.jsx` |
| 26 | Profil (nickname) | `ProfileSettings` | `screens-settings.jsx` |
| 27 | Default Alokasi | `AllocationEditor` | `screens-settings.jsx` |
| 28 | Tanggal Mulai Periode | `PeriodStartSettings` | `screens-settings.jsx` |
| 29 | PIN & Biometrik | `PinSettings` | `screens-settings.jsx` |
| 30 | Auto-lock | `AutoLockSettings` | `screens-settings.jsx` |
| 31 | Harga Emas Global | `GoldPriceSettings` | `screens-settings.jsx` |
| 32 | Bahasa | `LanguageSettings` | `screens-settings.jsx` |
| 33 | Tentang | `AboutScreen` | `screens-settings.jsx` |
| 34 | Export & Reset | `ExportResetSettings` | `screens-settings.jsx` |
| 35 | Donasi | `DonateScreen` | `screens-settings.jsx` |
| 36 | Backup & Pemulihan hub | `BackupSettings` | `screens-backup-misc.jsx` |
| 37 | Set PIN Backup | `BackupSetPinFlow` | `screens-backup-misc.jsx` |
| 38 | Backup Now | `BackupNowFlow` | `screens-backup-misc.jsx` |
| 39 | Restore | `RestoreFlow` | `screens-backup-misc.jsx` |
| 40 | Rekonsiliasi | `ReconciliationFlow` | `screens-backup-misc.jsx` |

**SettingsHub:** Profile card hero (Primary bg, nama + tanggal bergabung) → grouped SettingsRow list (5 groups: Preferensi / Keamanan / Data / Info / Donasi).

**AllocationEditor:** 3 slider Needs/Wants/Invest, validasi total = 100%, error banner bila tidak.

**PeriodStartSettings:** 28-day grid picker, catatan fallback Februari.

**AutoLock:** 5 radio opsi: Langsung / 1m / 5m / 15m / 30m.

**BackupSettings:** Status banner (✅ / ⚠️ tertunda N hari) + 4-step how-it-works + tombol Backup Sekarang + Pulihkan.

**BackupSetPinFlow:** 2-stage: masuk PIN → konfirmasi PIN. Error bila mismatch (A11Y-011).

**BackupNowFlow:** 3-stage: encrypting... → pilih lokasi simpan → done ✅.

**ReconciliationFlow:** 3-stage: input saldo asli → konfirmasi selisih → done. Selisih ≠ 0 → auto-buat transaksi reconciliation.

### 7.7 Export Laporan PDF

| # | Screen | Composable |
|---|---|---|
| 41 | Export PDF A4 | `Sakuwise Export PDF Sample.html` |

Template A4 portrait: Header brand + period → 4-col hero → Alokasi breakdown → Top 10 kategori (rank) → Akun + Aset Investasi → Tabel Transaksi → Footer (versi + SHA-256 hash).

---

## 8. Navigation Architecture

```kotlin
// Type-safe routes via Kotlin Serialization
@Serializable object SplashRoute
@Serializable object OnboardingRoute
@Serializable object HomeRoute
@Serializable data class AccountDetailRoute(val accountId: String)
@Serializable data class GoldDetailRoute(val goldId: String)
@Serializable data class TransactionDetailRoute(val transactionId: String)
// ... dst.

// Per-feature NavGraphBuilder extensions (NowInAndroid pattern)
fun NavGraphBuilder.onboardingScreen(onComplete: () -> Unit) {
    composable<OnboardingRoute> { OnboardingScreen(onComplete = onComplete) }
}
```

**Bottom sheets:** TIDAK di NavGraph. Dihandle via `remember { mutableStateOf<SheetState?>(null) }` + `ModalBottomSheet`. Ada 40+ sheets.

**Screen transitions:** slide-in kanan + fade 280ms. Reduced motion: fade-only 100ms.

---

## 9. State Management

```kotlin
// Pattern per ViewModel
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
    private val computeNetWorthUseCase: ComputeNetWorthUseCase,
    // hanya inject UseCase, TIDAK inject Repository langsung
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()
}
```

- **ViewModel hanya inject UseCase** — tidak pernah Repository
- Write ops: `viewModelScope.launch`
- One-shot events: `SharedFlow`
- Form draft + wizard step: `SavedStateHandle`

---

## 10. Encryption Architecture

```
Android Keystore (hardware-backed)
    ↓ wraps Daily KEK
DEK (256-bit AES, random, generated once at first launch)
    ↓ opens
SQLCipher database (sakuwise.db)

User PIN (input saat Backup/Restore)
    ↓ Argon2id (memory=64MB, t=3, parallelism=1)
Backup KEK (256-bit AES, derived fresh — never cached)
    ↓ AES-256-GCM encrypt
.sakuwise backup file
```

**Daily unlock:** Biometric → Keystore → EncryptedFile → DEK in memory → SQLCipher open.

**.sakuwise file format:**
```
Offset  Size  Field
0       4     Magic bytes: 0x53 0x4B 0x57 0x53 ("SKWS")
4       1     Format version (0x01)
5       1     App schema version (0x01)
6       2     Reserved (0x00 0x00)
8       16    Argon2id salt
24      12    AES-GCM nonce
36      4     Ciphertext length (uint32)
40      N     AES-GCM ciphertext (encrypted SQLite)
40+N    16    AES-GCM auth tag
```

---

## 11. Accessibility — Semua Action Items

**Tidak ada FAIL kritis.** 13 items yang WAJIB diimplementasikan:

| ID | Item | Cara Implementasi |
|---|---|---|
| A11Y-001 | Light `InkSubtle` hanya ≥14sp bold / ≥18sp | Annotasi di theme KDoc |
| A11Y-002 | Dark `InkSubtle` = `#7A8480` (bukan `#6B7570`) | ✅ Sudah di token di atas |
| A11Y-003 | Text di atas Warning bg → ≥16sp bold | Annotasi di `WarningSoft` usage |
| A11Y-004 | Back button + eye-toggle hit area 48×48 | `Modifier.minimumInteractiveComponentSize()` |
| A11Y-005 | Period selector segment hit area 44+ dp | Extra padding per segment |
| A11Y-006 | Tab bar `contentDescription` lengkap | `"Beranda, tab 1 dari 5"` |
| A11Y-007 | Logo dekoratif → `invisibleToUser()` | Watermark di hero card |
| A11Y-008 | Focus indicator border untuk list rows | Custom `focusedBorder` modifier |
| A11Y-009 | Toggle pakai Material 3 `Switch` native | Jangan custom div |
| A11Y-010 | Error state di semua form fields | `isError=true` + `supportingText` |
| A11Y-011 | Backup PIN mismatch → pesan error explicit | Toast "PIN tidak cocok" |
| A11Y-012 | Reduced motion support | Cek `ANIMATOR_DURATION_SCALE` |
| A11Y-013 | TalkBack override untuk "rb/jt/M" | `contentDescription = "Rp 1 juta 500 ribu"` |

**Pre-release checklist:**
- [ ] TalkBack pass-through semua screen (Bahasa Indonesia)
- [ ] Font scaling 200% tanpa overflow (pakai `sp`, bukan `dp` untuk text)
- [ ] Reduce motion: semua animasi diganti fade 100ms
- [ ] Backup encrypt → restore di device berbeda
- [ ] Plan period start day diubah ke 25 → cek label

---

## 12. DataStore Preferences

```kotlin
// Non-sensitive preferences di :core:datastore
theme_mode                // light / dark / system
language                  // id / en  
auto_lock_minutes         // 1 / 5 / 15 / 30 / 0
plan_period_start_day     // 1-28
default_allocation_percentages  // jsonString {needs:50, wants:30, invest:20}
gold_price_global         // long (per gram, IDR)
last_backup_timestamp     // long (epoch ms)
onboarding_completed      // boolean
user_nickname             // string
```

---

## 13. Background Work

**RecurringPaymentReminderWorker** (satu-satunya worker di V1):
- `PeriodicWorkRequest` dengan tag = plan item ID
- **Konten pre-computed** saat scheduling (simpan di DataStore) — karena DB terenkripsi tidak bisa dibuka saat device terkunci
- Permission: `POST_NOTIFICATIONS` (Android 13+)
- Channel ID: `sakuwise_reminder`

---

## 14. OCR Pipeline

```
Camera / Gallery / Share intent
    ↓ Bitmap (rotated per EXIF)
    ↓ ML Kit Text Recognition (Latin recognizer)
    ↓ IndonesianReceiptParser (:feature:transaction)
        - Regex total: "total" / "tunai" / "bayar" / "jumlah"
        - Regex date: Indonesian date formats
        - Merchant: top text lines
    ↓ ReceiptDraft(merchant, date, totalAmount, confidence: High/Medium/Low)
    ↓ Pre-fill expense form
    ↓ User confirm/correct
    ↓ Transaction + photo BLOB
```

Fallback: OCR gagal → tampilkan struk, user input manual. Foto tetap attached.

---

## 15. Testing

| Layer | Framework | Coverage target |
|---|---|---|
| UseCase (`:core:domain`) | JUnit 5 + MockK + Turbine | 70% minimum |
| Repository (`:core:data`) | Room in-memory | Key flows |
| ViewModel (`:feature:*`) | MockK UseCase fakes | Key states |
| Crypto | Round-trip test | Encrypt↔Decrypt |
| IndonesianReceiptParser | Unit test | Sample receipts |
| Component screenshots | Paparazzi | Light + dark + states |
| UI flows | Compose UI Test | Onboarding, Backup, Expense form |

---

## 16. CI/CD

```yaml
# .github/workflows/build.yml
- assembleDebug
- testDebugUnitTest
- verifyPaparazziDebug   # screenshot regression
- upload test reports
```

---

## 17. Release

- **Distribution:** Google Play Store, AAB, free
- **Crash reporting:** Google Play Console Android Vitals only (no SDK) — sufficient untuk solo dev
- **App ID:** `com.gustiadhitya.sakuwise`
- **Debug suffix:** `com.gustiadhitya.sakuwise.debug` (install side-by-side)
- **Signing:** Play App Signing (upload keystore local, never commit)
- **Versioning:** `MAJOR.MINOR.PATCH`, versionCode = MAJOR×10000 + MINOR×100 + PATCH

---

## 18. Brand Assets

| Asset | Lokasi |
|---|---|
| Logo (Konsep A — Daun) | `brand/logos.jsx` → `LogoA_Daun` |
| App icon adaptive (108 dp) | bg=`#0F4C3A` solid, fg=Daun mark `#F5F1E8` |
| Wordmark | "Saku" dark + "wise" Primary green, Figtree 700, tracking -0.025em |
| Color tokens | `brand/tokens.jsx` |
| Typography specimen | `brand/typography.jsx` |
| Brand canvas HTML | `Sakuwise Brand Identity.html` |

---

## 19. File Inventory

```
design_handoff_sakuwise/
├── README.md                              ← dokumen ini
├── Sakuwise Prototype.html               ← BUKA INI DULUAN — 33+ layar clickable
├── Sakuwise Handoff Spec.md              ← token → Compose mapping konkret
├── Sakuwise Design Concept v1.0.md       ← design system bible (filosofi, pattern, copy)
├── Sakuwise Accessibility Audit.md       ← WCAG 2.1 AA audit + 13 action items
├── Sakuwise Technical Solution v1.1.md   ← arsitektur, stack, module structure
├── Sakuwise Export PDF Sample.html       ← template laporan bulanan A4
├── Sakuwise Brand Identity.html          ← canvas logo, type, color, app icon
├── proto/
│   ├── components.jsx                    ← semua SW_* component source
│   ├── icons.jsx                         ← 45+ ikon SVG
│   ├── data.jsx                          ← sample data, SW_FORMAT helpers
│   ├── app.jsx                           ← app shell, routing, theme
│   ├── screens-dashboard.jsx             ← DashboardScreen
│   ├── screens-plan.jsx                  ← PlanScreen
│   ├── screens-addtxn.jsx                ← AddTxnPicker, ExpenseForm
│   ├── screens-txn-forms.jsx             ← IncomeForm, TransferForm
│   ├── screens-assets.jsx                ← AssetsHub, Accounts, Emas
│   ├── screens-assets-detail.jsx         ← Land, Deposito, Hutang
│   ├── screens-settings.jsx              ← Settings hub + 15 sub-screens
│   ├── screens-backup-misc.jsx           ← Backup, Restore, Rekon, OCR
│   └── screens-onboarding.jsx            ← Splash, 4-step onboarding
└── brand/
    ├── logos.jsx                          ← LogoA_Daun, Wordmark, Lockup
    ├── tokens.jsx                         ← SW_LIGHT, SW_DARK color objects
    ├── app-icon.jsx                       ← app icon adaptive
    ├── typography.jsx                     ← type specimen
    └── swatches.jsx                       ← color swatch grid
```

---

## 20. Instruksi untuk Claude Code

Ketika memulai coding dengan paket ini:

```
Baca semua file di design_handoff_sakuwise/ sebelum mulai coding.
Urutan baca:
1. README.md (dokumen ini) — gambaran menyeluruh
2. Sakuwise Prototype.html — buka di browser, eksplorasi semua layar
3. Sakuwise Handoff Spec.md — token mapping konkret
4. Sakuwise Technical Solution v1.1.md — arsitektur dan module structure
5. proto/components.jsx — spek komponen yang akan di-port

Prinsip implementasi:
- Pixel-perfect ke prototype HTML = spec. Kalau ada konflik antara dokumen 
  dan prototype, PROTOTYPE YANG MENANG.
- Semua nominal Rupiah WAJIB fontFeatureSettings = "tnum"
- Gunakan sp (bukan dp) untuk semua text size
- ViewModel hanya inject UseCase, tidak pernah Repository langsung
- Tidak ada INTERNET permission di AndroidManifest.xml V1
- Semua 13 A11Y action items wajib check-off sebelum release
```

---

*Paket ini dibuat di akhir Fase Desain Sakuwise (Milestone 6).  
Lokal-first · terenkripsi · hangat · tanpa cloud · tanpa telemetry.*  
🌱 **Rencanakan. Catat. Tenang.**
