# Sakuwise — Design Concept v1.0

**Versi:** 1.0 (final · akhir Milestone 6)
**Tanggal:** 16 Mei 2026
**Author:** Gusti Adhitya (founder) + Claude (design phase)
**Status:** ✅ Final — siap untuk fase coding

> Dokumen ini adalah **design system bible** untuk Sakuwise. Berisi semua keputusan
> brand, visual, komponen, pola, dan microcopy yang harus diikuti developer.
> Untuk implementasi konkret di Kotlin/Compose, lihat `Sakuwise Handoff Spec.md`.
> Untuk hasil audit WCAG, lihat `Sakuwise Accessibility Audit.md`.

---

## 0. Daftar Isi

1. [Filosofi & Posisi](#1-filosofi--posisi)
2. [Brand Identity](#2-brand-identity)
3. [Design Tokens](#3-design-tokens)
4. [Typography](#4-typography)
5. [Iconography](#5-iconography)
6. [Component Library](#6-component-library)
7. [Pattern Library](#7-pattern-library)
8. [Voice & Tone (Microcopy)](#8-voice--tone-microcopy)
9. [Screens — Index Lengkap](#9-screens--index-lengkap)
10. [Animation & Motion](#10-animation--motion)
11. [iOS Portability](#11-ios-portability)
12. [File Deliverables](#12-file-deliverables)

---

## 1. Filosofi & Posisi

### 1.1 Pertanyaan inti yang dijawab desain

> "Bagaimana app keuangan personal yang lokal-first, terenkripsi, dan tanpa cloud
> bisa terasa **hangat** — bukan corporate banking, bukan game playful — tempat
> data uang seseorang tinggal dengan tenang?"

### 1.2 Tone visual

Sakuwise berdiri di tengah dua kutub yang biasanya mendominasi finance app Indonesia:

| Bukan | Sakuwise | Bukan |
|---|---|---|
| Corporate banking (biru, dingin, jargon, intimidatif) | **Hangat, tenang, manusiawi** | Game playful (warna meriah, emoji, gamifikasi) |

Referensi tone: **Cash App, Notion, Linear** — disesuaikan konteks Indonesia.

### 1.3 Prinsip Desain

1. **Privacy by visibility.** Eye-toggle untuk semua tampilan saldo (Dashboard + Aset). User memilih kapan angka muncul.
2. **Ritual, bukan dashboard.** Plan → Catat → Review. Bukan KPI overload. Mengikuti workflow spreadsheet user, dijadikan native mobile.
3. **No data slop.** Setiap angka, ikon, chart, badge harus membantu keputusan. Kalau cuma dekorasi → hapus.
4. **Lokal sebagai konteks, bukan gimmick.** IDR format, tanggal Bahasa, kategori familiar (Kos, Mudik, THR, PBB) — tapi **bukan** batik tile, dll. Hangat lewat warna & tone, bukan motif.
5. **Token-first.** Semua keputusan visual abstract — siap port iOS tanpa redesign.

---

## 2. Brand Identity

### 2.1 Nama & Tagline

- **Nama:** Sakuwise (gabungan "saku" — kantong dlm Bahasa Indonesia + "wise" — bijak)
- **Tagline:** *Rencanakan. Catat. Tenang.*
- **Pengucapan:** sa-ku-wais

### 2.2 Logo Sistem — Konsep "Daun"

Mark utama: **kantong/saku dengan motif daun di dalamnya** — saku tempat tumbuh.

- **Mark** (squircle, brand bg primary, isi daun cream + vein hijau): `brand/logos.jsx` → `LogoA_Daun`
- **Wordmark:** "Saku" hitam + "wise" hijau primary, Figtree weight 700, tracking −0.025em
- **Lockup horizontal:** mark + wordmark, gap = mark size × 0.35
- **Lockup vertical:** mark di atas, wordmark di bawah, gap = mark size × 0.4

### 2.3 App Icon — Adaptive Android

108dp canvas, 66dp safe zone (61.1%).

- **Background layer** (`ic_launcher_background.xml`): solid `#0F4C3A` (primary)
- **Foreground layer** (`ic_launcher_foreground.xml`): Daun mark warna `#F5F1E8` (onPrimary), centered di safe zone 66dp

OEM mask varian: Pixel squircle (radius `size × 0.235`), Samsung circle, OnePlus rounded square — semua test-able di `Sakuwise Brand Identity.html` artboard "App Icon".

### 2.4 Brand color philosophy

**Primary forest green + cream** — earthly, organic, calming. Bukan biru korporat. Bukan oranye fintech aggressive.

---

## 3. Design Tokens

### 3.1 Color — Light theme (lengkap)

```
bg                  #F5F1E8   /* page background, cream warm */
surface             #FAF7F0   /* cards */
surfaceElev         #FFFFFF   /* modal, FAB, elevated */
ink                 #1A2520   /* primary text */
inkMuted            #5C6963   /* secondary text */
inkSubtle           #8B948F   /* tertiary — ≥14pt bold or ≥18pt only */
border              #E8E0CC
borderStrong        #D6CDB4
primary             #0F4C3A   /* CTA, brand anchor */
primaryHover        #0A3A2C
onPrimary           #F5F1E8
primaryContainer    #D4E8DC   /* soft state, chip bg */
onPrimaryContainer  #0A2E22
accent              #7BC4A4   /* secondary mint, Wants alloc */
accentSoft          #D4E8DC
success             #2D7A4F
successSoft         #D6EDDC
warning             #C68A2E
warningSoft         #F4E4C8
danger              #B84545
dangerSoft          #F1D6D6
info                #4A6FA5
infoSoft            #D6E0EE
```

### 3.2 Color — Dark theme (lengkap)

```
bg                  #0F1411
surface             #1A211D
surfaceElev         #232B26
ink                 #F0EDE3
inkMuted            #A8B0AB
inkSubtle           #6B7570   /* re-tune to #7A8480 per A11Y-002 */
border              #2D3631
borderStrong        #3D4742
primary             #7BC4A4   /* mint becomes primary on dark */
primaryHover        #9DD4BA
onPrimary           #0A1F18
primaryContainer    #1F3329
onPrimaryContainer  #C4E8D4
accent              #C4E8D4
accentSoft          #1F3329
success             #6DC48F
successSoft         #1E3526
warning             #E0A954
warningSoft         #3B2E18
danger              #D67373
dangerSoft          #3D1F1F
info                #7FA0C7
infoSoft            #1E2A3A
```

### 3.3 Spacing scale

`4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 / 64` (dp)

- Card padding default: 16
- Page horizontal padding: 20
- Bottom content padding-bottom: 100 (clear tab bar)
- Section vertical gap: 14-16

### 3.4 Border radii

| Token | Value | Pemakaian |
|---|---|---|
| xs | 4 | Tag, badge kecil |
| sm | 8 | Small button, pill chip |
| md | 12 | Field, icon container |
| lg | 16 | Card |
| xl | 20 | Large card |
| 2xl | 22-28 | Hero card, sheet top corners |
| full | 9999 | Pill, FAB |

App icon squircle: `size × 0.235` ≈ 25.4dp on 108dp adaptive.

### 3.5 Elevation / Shadow

- **Card default:** hairline border `0 0 0 1px var(--border)`, no shadow
- **FAB / CTA sticky:** `0 8px 20px rgba(15,76,58,0.25)` light · `0 8px 20px rgba(123,196,164,0.25)` dark
- **Sheet:** `0 -8px 30px rgba(0,0,0,0.18)`
- **Phone frame mockup:** `0 30px 80px rgba(15,76,58,0.18)` light · `0 30px 80px rgba(0,0,0,0.55)` dark

---

## 4. Typography

### 4.1 Font

**Figtree** (Google Fonts) — humanist sans, full Latin Extended, support Bahasa Indonesia.
Fallback: `system-ui, -apple-system, sans-serif`.

Monospace: **JetBrains Mono** — untuk caption/label kecil yang butuh tabular alignment.

**Wajib `font-feature-settings: 'tnum'` di semua nominal Rupiah.**

### 4.2 Type scale lengkap

| Style | Size | Line | Weight | Tracking | Use |
|---|---|---|---|---|---|
| Display L | 40 | 48 | 700 | −0.02em | Hero splash |
| Display M | 32 | 40 | 700 | −0.02em | Section headers |
| H1 | 26 | 34 | 700 | −0.01em | Screen titles |
| H2 | 20 | 28 | 600 | −0.005em | Card titles |
| H3 | 17 | 24 | 600 | 0 | List headers |
| Body L | 16 | 24 | 400 | 0 | Default body |
| Body | 14 | 20 | 400 | 0 | Secondary text |
| Caption | 12 | 16 | 500 | +0.01em | Meta, labels |
| Amount XL | 36 | 40 | 700 | −0.02em | Dashboard hero |
| Amount L | 22 | 28 | 600 | −0.01em | Card amount |
| Amount | 16 | 22 | 600 | 0 | List amount |

Section label (uppercase pill): 11px, weight 700, tracking +0.08em, color `inkSubtle`.

### 4.3 Aturan wajib

- Nominal Rupiah → tabular-nums **selalu**
- Display & H1 → `letter-spacing: -0.02em` untuk warmth (jangan over-tight)
- Body Bahasa Indonesia → minimum 14sp (mobile reading)
- Compose pakai `sp` bukan `dp` untuk text (font scaling support)

---

## 5. Iconography

Sistem: **24×24 viewBox, 1.75 stroke, round caps**, monoline. Pure SVG, scale via `currentColor`.

Total ikon di prototipe: **45+**. Source: `proto/icons.jsx`.

### 5.1 Kategori ikon

| Kategori | Ikon |
|---|---|
| Tab bar | home, plan, plus, assets, me (+ filled variants untuk active state) |
| Navigation | back, close, more, search, chevron_right, chevron_down, chevron_up |
| Actions | edit, trash, copy, check, camera, calendar, filter, bell, shield |
| Transaction types | expense, income, transfer |
| Account types | cash, bank, wallet |
| Asset types | gold, land, deposit |
| Misc | link, receipt, arrow_up_right, arrow_down_left, swap, leaf, warning, info, sparkle, eye, eye_off |

### 5.2 Filled vs outline

- Tab bar inactive: **outline** (1.75 stroke)
- Tab bar active: **filled** variant
- Buttons & list items: **outline** consistent
- Status badge (success/warning/danger): **filled** untuk strength

---

## 6. Component Library

> Setiap component di prototipe di `proto/components.jsx` & spesifik file screens.
> Compose mapping detail di `Sakuwise Handoff Spec.md` §6.

### 6.1 Buttons

`SW_Button` — variants × sizes × states.

**Variants:**
- `primary` — bg `primary`, fg `onPrimary` (CTA utama, 1 per screen)
- `secondary` — bg `primaryContainer`, fg `onPrimaryContainer` (action sekunder)
- `outline` — bg transparent, border `borderStrong` (tertiary)
- `ghost` — bg transparent, fg `primary` (text link style)
- `danger` — bg `danger`, fg white (destructive)

**Sizes:** sm (h=36), md (h=48), lg (h=56). Default = md.

**States:** default, pressed (scale 0.97 + opacity 0.85), disabled (opacity 0.5), focused (Compose ring).

### 6.2 Inputs

`SW_Field` — outlined text field, border 1.5dp, height 52, radius 12.
- `prefix` (mis. "Rp") di kiri
- `suffix` (mis. "/ gram") di kanan
- `hint` text di bawah
- `error` boolean → border + hint warna `danger`

`FieldButton` — read-only field-style button untuk picker (plan item, akun, tanggal).

`PinInput` — 6-cell grid, h=56 per cell.

### 6.3 Cards

`SW_Card` — hairline border, no shadow, radius 16-18 default, padding 16 default.

### 6.4 Bottom Sheet

`SW_Sheet` — slide-in 280ms cubic-bezier(.2,.7,.3,1), radius 24dp top corners, drag handle 44×4 dp, optional title bar dengan close button.

### 6.5 Tab Bar

`SW_TabBar` — 5 slot (Beranda, Plan, +FAB, Aset, Saya). FAB tengah elevated −16dp, 56×56 radius 18.

### 6.6 Top Bar

`SW_TopBar` — height 48-56, back button left-aligned, title left-aligned (bukan center), optional right action.

### 6.7 Progress Bar

`SW_Bar` — height 6-8, radius matches height, fill animated 400ms. Over-budget → dual segment (primary fill ≤ 100%, danger overflow segment).

### 6.8 Banner

Info / warning / danger — bg `*Soft`, border `*33`, padding 14, icon + text + optional action button.

### 6.9 Chip

`SW_Chip` — pill 36 dp, active = primary bg, inactive = surface + border. Untuk filter alokasi, period selector, account type, dll.

### 6.10 Amount

`SW_Amount` — komponen formatter Rupiah:
- `size` (px), `weight` (400-800), `color`, `prefix` (default "Rp"), `sign` (+/−)
- Tabular nums **always**
- Letter-spacing −0.02em untuk warmth

### 6.11 Account icon

`SW_AccountIcon` — square rounded dengan icon dalam (cash/bank/wallet) bg `primaryContainer`, color `onPrimaryContainer`.

### 6.12 Category dot

`SW_CategoryDot` — circle dengan huruf pertama nama kategori, warna deterministic dari hash nama.

### 6.13 Toggle

`SW_Toggle` — switch 44×26, knob 20 putih dengan shadow. **Compose: pakai Material 3 `Switch` native** per A11Y-009.

### 6.14 Settings row

`SettingsRow` — h=64 dp, icon kiri + label/sub + value + chevron. Variants: default, danger, warning.

### 6.15 Snapshot chart

`SnapshotChart` — line + area + dots, accepts `[{date, balance|value}]`. Configurable lineColor + height. Linear gradient area fill (22% opacity top → 0% bottom).

### 6.16 Toast

Floating notification bottom, bg `ink`, fg `bg`, radius 14, icon checkmark di kiri, auto-dismiss 2.2s.

---

## 7. Pattern Library

### 7.1 Hero card pattern

Top of screen, large primary-colored card dengan:
- Section label (uppercase 11pt, opacity 0.78)
- Big amount (32-36pt, weight 800)
- Secondary line (12pt, opacity 0.78)
- Optional eye-toggle di kanan-atas untuk privacy
- Watermark logo Daun di sudut bawah-kanan (opacity 0.10-0.14)

Dipakai di: Dashboard hero, Aset hero, Form Pengeluaran/Pemasukan/Transfer hero, Detail screens.

### 7.2 Eye-toggle privacy

Tap mata di hero card → semua nominal di screen jadi `••••••`. State independen per tab (Dashboard punya state sendiri, Aset punya state sendiri).

### 7.3 List → Detail → Action sheet

Pattern navigation 3-level:
1. **List screen** (list of entities)
2. Tap row → **Detail screen** (hero summary + sub-sections + Edit + ... menu)
3. Tap ⋯ → **Action sheet** (rename, archive, export, delete dengan confirmation)

Dipakai di: Akun, Emas, Tanah, Deposito, Hutang.

### 7.4 Form layout

Top bar (Cancel + Title + Save) → Hero amount card (warna sesuai alokasi/tipe) → Field list (FieldButton untuk picker, SW_Field untuk text input) → Toggle optional → Save button bottom.

### 7.5 Confirmation dialog

Bottom sheet dengan:
- Icon hero 64dp (warning/info bg sesuai tone)
- Title bold center
- Copy explainer center
- Primary action button
- Ghost cancel button

Dipakai untuk: Delete, Reset, Mark Lunas, Logout.

### 7.6 Stepper / Wizard

Onboarding 4-step, Backup setup 2-stage, Reconciliation 3-stage:
- Progress dots di atas
- Hero artwork di tengah
- Title + sub copy
- Form content
- Primary action + secondary ghost

### 7.7 Empty state (implicit)

List screens otomatis kasih FAB plus. Untuk zero-data state, pakai PostIt-style card: icon kontekstual + headline + copy + action button.

### 7.8 Chart pattern

Hanya 3 chart di V1, semua menjawab pertanyaan keputusan:
- **Deposito snapshot growth** — "Tabungan saya bertumbuh nggak?"
- **Net worth trend** — "Kekayaan total bertumbuh nggak?"
- **Hutang outstanding** — "Sisa cicilan saya turun nggak?"

Pattern: line + area + dots, period selector 3M/6M/1Y/Semua di bawah (kalau applicable), delta percentage di atas-kanan.

---

## 8. Voice & Tone (Microcopy)

### 8.1 Tone matrix

| | Yes | No |
|---|---|---|
| Sapaan | "Gusti" | "User", "Pengguna" |
| Kata ganti | tanpa, atau kasual | "Anda", "Bapak/Ibu" |
| Verb | imperatif ramah ("Mulai", "Catat") | "Silakan", "Mohon" |
| Length | pendek, langsung | bertele-tele |
| Number | familiar (28 ribu, 1.5 juta) | full decimal (Rp 1.500.000,00) di tampilan biasa |

### 8.2 Library starter (snapshot)

**Sapaan Dashboard**
- "Selamat pagi, Gusti. Plan Mei 2026, sisa 16 hari."

**Empty state**
- "Belum ada plan bulan ini. Mulai dari template starter?"
- "Belum ada akun. Tambah akun pertamamu?"

**Overspending**
- "Kategori Makanan sudah lewat anggaran Rp 120.000."

**Backup berhasil**
- "Backup tersimpan. File ada di Download."

**Konfirmasi hapus**
- "Hapus transaksi ini? Tidak bisa dibatalkan."

**Reset app**
- "Hapus semua data? Semua akun, plan, transaksi, aset, hutang, dan foto struk akan dihapus permanen. Tindakan ini tidak bisa dibatalkan."

**Backup tertunda**
- "Backup tertunda 34 hari. Amankan data uangmu — backup sekarang."

**Onboarding privacy**
- "Data kamu tinggal di sini. Tidak ada server. Tidak ada telemetry. Tidak ada permintaan akses internet."

**Donasi**
- "Sakuwise gratis untuk semua. Dikembangkan sendiri, sebagai sapaan untuk komunitas. Kalau kamu merasa terbantu, traktir kopi sebagai apresiasi."

### 8.3 Format konvensi

- **Currency:** `Rp 1.500.000` (spasi setelah Rp, titik thousand)
- **Short currency:** `Rp 1.5 jt` / `Rp 850rb` / `Rp 1.5 M`
- **Date:** `15 Mei 2026` (DD MMM YYYY Bahasa)
- **Date relative:** `Hari ini` / `Kemarin` / `3 hari lalu` (jika < 7), lalu absolute
- **Percentage:** `12.4%` (titik decimal, mengikuti konvensi UI Indonesia modern)

---

## 9. Screens — Index Lengkap

**Total 33 layar/flow di V1.** Sumber: `Sakuwise Prototype.html`.

### 9.1 Per tab

**Beranda (Dashboard)**
1. Dashboard hero + greeting + alokasi + top spending + assets link + recent txn + backup banner — `screens-dashboard.jsx` `DashboardScreen`

**Plan**
2. Plan view + filter chips + category cards + action sheet (Aksi Plan) — `screens-plan.jsx` `PlanScreen`

**+ Tambah Transaksi**
3. Picker bottom sheet — `screens-addtxn.jsx` `AddTxnPicker`
4. Pengeluaran form — `screens-addtxn.jsx` `ExpenseForm`
5. Pemasukan form — `screens-txn-forms.jsx` `IncomeForm`
6. Transfer form — `screens-txn-forms.jsx` `TransferForm`
7. OCR receipt capture flow (camera → processing → review) — `screens-backup-misc.jsx` `OcrFlow`

**Aset**
8. Aset hub (net worth + 4 asset cards + debt) — `screens-assets.jsx` `AssetsHubScreen`
9. Net worth trend chart (3M/6M/1Y/Semua) — `screens-assets.jsx` `NetWorthTrendCard`
10. Akun list — `screens-assets.jsx` `AccountsListScreen`
11. Akun detail (saldo + snapshot history + transactions + actions) — `screens-assets.jsx` `AccountDetailScreen`
12. Emas list + ubah harga global sheet — `screens-assets.jsx` `EmasListScreen`
13. Emas detail (per batch) — `screens-assets.jsx` `EmasDetailScreen`
14. Tanah/Properti list — `screens-assets-detail.jsx` `LandListScreen`
15. Tanah detail + Pembayaran Pajak sub-list + Tambah Pembayaran sheet — `screens-assets-detail.jsx` `LandDetailScreen`
16. Deposito/Pensiun list — `screens-assets-detail.jsx` `DepositoListScreen`
17. Deposito detail (chart snapshot + history + tambah snapshot sheet) — `screens-assets-detail.jsx` `DepositoDetailScreen`
18. Hutang list (I owe + owed to me) — `screens-assets-detail.jsx` `DebtListScreen`
19. Hutang detail (outstanding + chart + payments + Tambah Pembayaran sheet) — `screens-assets-detail.jsx` `DebtDetailScreen`

**Saya (Settings)**
20. Settings hub (profile card + grouped settings) — `screens-settings.jsx` `SettingsHub`
21. Profil (nickname) — `ProfileSettings`
22. Default Alokasi (3 slider + validasi total=100%) — `AllocationEditor`
23. Tanggal Mulai Periode (grid 28 hari) — `PeriodStartSettings`
24. PIN & Biometrik — `PinSettings`
25. Auto-lock (5 opsi) — `AutoLockSettings`
26. Backup & Pemulihan hub — `screens-backup-misc.jsx` `BackupSettings`
27. Set PIN backup (2-stage: enter → confirm) — `BackupSetPinFlow`
28. Backup Now (encrypting → pick location → done) — `BackupNowFlow`
29. Restore dari file — `RestoreFlow`
30. Rekonsiliasi flow (input → confirm → done) — `ReconciliationFlow`
31. Bahasa — `LanguageSettings`
32. Tentang Sakuwise — `AboutScreen`
33. Donasi (Saweria + Trakteer + QRIS) — `DonateScreen`
34. Export & Reset + confirmation — `ExportResetSettings`

**Onboarding (first-run)**
35. Splash — `screens-onboarding.jsx` `SplashScreen`
36. Sambutan + Bahasa — `Onb_Language`
37. Nama panggilan + PIN + Biometrik — `Onb_Identity`
38. Privacy notice — `Onb_Privacy`
39. Akun pertama — `Onb_FirstAccount`

### 9.2 Export laporan PDF

40. Template laporan bulanan A4 portrait — `Sakuwise Export PDF Sample.html` (standalone HTML).

---

## 10. Animation & Motion

### 10.1 Tokens

| Nama | Durasi | Easing | Pakai |
|---|---|---|---|
| quick | 120ms | linear | Hover/press feedback |
| default | 200ms | cubic-bezier(.2,.7,.3,1) | State toggle, sheet open |
| medium | 280ms | cubic-bezier(.2,.7,.3,1) | Screen transition slide-in |
| slow | 400ms | cubic-bezier(.2,.7,.3,1) | Progress bar fill |
| splash-fade | 600ms | cubic-bezier(.2,.7,.3,1) | Splash entrance staggered |

### 10.2 Press feedback

Universal `.sw-press` class: scale `0.97`, opacity `0.85`, durasi 100ms ease. Diterapkan ke semua interactive (button, card, row, FAB, toggle, dst).

### 10.3 Reduced motion

Per A11Y-012, hormati `Settings.Global.ANIMATOR_DURATION_SCALE` Android. Saat user disable animasi system-wide, ganti:
- Splash staggered → instant
- Screen slide-in → cross-fade 100ms
- Progress bar fill animated → instant
- Bottom sheet slide-up → cross-fade

---

## 11. iOS Portability

Sakuwise didesain supaya port iOS minimal friction. Pattern yang kompatibel:

| ✅ Pakai langsung | ⚠️ Adaptasi |
|---|---|
| Bottom tab bar | FAB tengah → ganti dengan toolbar action di top atau iOS-style large compose button |
| Bottom sheet (UISheetPresentationController) | Material ripple → ganti opacity/scale press (sudah |
| Outlined text fields | Material adaptive icon → iOS Asset Catalog single-image |
| Color token system | — |
| Figtree Google Fonts | — |
| WCAG audit & spacing | — |

**Strategi:** definisikan platform protocol di Compose Multiplatform / KMP — UI shared, platform-specific chrome (status bar, navigation gestures, share intent) di expect/actual.

Detail di `Sakuwise Handoff Spec.md` §10.

---

## 12. File Deliverables

Project workspace berisi:

| File | Isi |
|---|---|
| `Sakuwise Design Concept v1.0.md` | **(dokumen ini)** Master design system bible |
| `Sakuwise Brand Identity.html` | Canvas presentasi brand — logo, type, color, app icon, voice, brand di konteks |
| `Sakuwise Prototype.html` | Clickable prototipe 33+ layar — Dashboard, Plan, Aset, Saya, semua forms & flows |
| `Sakuwise Export PDF Sample.html` | Template laporan bulanan A4 portrait (printable) |
| `Sakuwise Handoff Spec.md` | Token mapping ke Compose, animasi, screen→Composable mapping, format konvensi |
| `Sakuwise Accessibility Audit.md` | WCAG 2.1 AA audit + 13 action items minor untuk dev |
| `proto/*.jsx` | Source code prototipe (komponen, screens, data, app shell) |
| `brand/*.jsx` | Brand identity components (logos, tokens, lockup) |

Untuk fase coding, **mulai dari** `Sakuwise Handoff Spec.md` yang berisi instruksi konkret per layar + token mapping. Reference visual: `Sakuwise Prototype.html` (jalankan di browser dan navigasi via Tweaks panel).

---

## 13. Definition of Done — Checklist

- [x] Brand identity (logo Daun, wordmark, app icon adaptive) → `Sakuwise Brand Identity.html`
- [x] Color palette light + dark dengan semantic states → `brand/tokens.jsx`
- [x] Typography scale + tabular nums rule → §4
- [x] Iconography system 45+ ikon → `proto/icons.jsx`
- [x] Tagline + voice & tone → §8
- [x] 33+ key screens hi-fi clickable → `Sakuwise Prototype.html`
- [x] Component library + variants & states → §6
- [x] Pattern library → §7
- [x] Animation tokens → §10
- [x] Bottom sheets, modals, confirmation dialogs → §7
- [x] OCR receipt capture preview → screen 7
- [x] Backup setup + restore flows → screens 27-29
- [x] Reconciliation flow + AccountSnapshot history → screen 11, 30
- [x] Charts (Net Worth, Deposito, Hutang) → §7.8
- [x] Settings lengkap per PRD §7.15 → screens 20-34
- [x] Donasi + QRIS → screen 33
- [x] Export laporan PDF format → `Sakuwise Export PDF Sample.html`
- [x] UX copy starter Bahasa Indonesia → §8.2
- [x] Accessibility audit WCAG AA → `Sakuwise Accessibility Audit.md`
- [x] Developer handoff spec → `Sakuwise Handoff Spec.md`
- [x] iOS portability notes → §11 + Handoff §10

**Fase desain Sakuwise: ✅ SELESAI.**

Siap untuk fase coding dengan Claude Code / Android Studio.

---

*Ditulis bersama oleh Gusti Adhitya (founder, first user) dan Claude (design phase).
Lokal-first · terenkripsi · hangat · tanpa cloud · tanpa telemetry.*

🌱 **Rencanakan. Catat. Tenang.**
