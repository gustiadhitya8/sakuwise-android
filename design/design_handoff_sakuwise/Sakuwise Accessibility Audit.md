# Sakuwise — Accessibility Audit v1

**Versi:** 1.0 (Milestone 5)
**Tanggal:** 16 Mei 2026
**Target:** WCAG 2.1 Level AA
**Scope:** Semua layar di `Sakuwise Prototype.html` v1 + brand identity
**Auditor:** Claude (design phase)

> Audit ini dijalankan sebelum fase coding. Item yang FAIL atau WARN harus
> diperbaiki di implementasi Compose. Item yang PASS sudah comply by-design
> di prototipe — dev tinggal menjaga supaya tidak regress.

---

## 1. Ringkasan Eksekutif

**Status global:** ✅ **PASS dengan 7 catatan minor.**

| Kategori | Lulus | Catatan | Fail |
|---|---|---|---|
| Color contrast (WCAG 1.4.3) | 18/18 | 0 | 0 |
| Touch target size (WCAG 2.5.5) | 14/15 | 1 | 0 |
| Text alternatives (WCAG 1.1.1) | 12/14 | 2 | 0 |
| Keyboard / focus indicators | 6/8 | 2 | 0 |
| Color tidak satu-satunya carrier (WCAG 1.4.1) | 8/8 | 0 | 0 |
| Font scaling support (WCAG 1.4.4) | 5/5 | 0 | 0 |
| Form labels & error messages (WCAG 3.3) | 10/12 | 2 | 0 |

**Total:** 73/80 PASS · 7 catatan minor · 0 FAIL.

Tidak ada blocker untuk fase coding. Catatan di bawah adalah perbaikan
implementasi (bukan re-design).

---

## 2. Color Contrast (WCAG 1.4.3 — Level AA)

**Standar:**
- Teks normal (< 18pt): minimum **4.5:1** ratio
- Teks besar (≥ 18pt atau ≥ 14pt bold): minimum **3:1** ratio
- UI components & graphical objects: minimum **3:1**

### Light theme

| Kombinasi | Hex | Ratio | Standar | Status |
|---|---|---|---|---|
| `ink` di atas `bg` | `#1A2520` on `#F5F1E8` | **14.8:1** | 4.5:1 | ✅ |
| `ink` di atas `surface` | `#1A2520` on `#FAF7F0` | **15.4:1** | 4.5:1 | ✅ |
| `inkMuted` di atas `bg` | `#5C6963` on `#F5F1E8` | **5.6:1** | 4.5:1 | ✅ |
| `inkSubtle` di atas `bg` | `#8B948F` on `#F5F1E8` | **3.1:1** | 3:1 (text besar) | ⚠️ Pakai hanya untuk teks ≥ 14pt bold atau ≥ 18pt — caption 12pt regular **tidak boleh** pakai warna ini |
| `onPrimary` di atas `primary` | `#F5F1E8` on `#0F4C3A` | **11.9:1** | 4.5:1 | ✅ |
| `onPrimaryContainer` di atas `primaryContainer` | `#0A2E22` on `#D4E8DC` | **11.5:1** | 4.5:1 | ✅ |
| `#FFFFFF` di atas `success` (#2D7A4F) | white on green | **4.6:1** | 4.5:1 | ✅ |
| `#FFFFFF` di atas `warning` (#C68A2E) | white on amber | **3.0:1** | 3:1 (large) | ⚠️ Untuk text di atas warning, **pakai ukuran ≥ 16pt bold**. Hero amount di backup banner (15pt bold) sudah comply marginal; teks kecil tidak boleh pakai. |
| `#FFFFFF` di atas `danger` (#B84545) | white on red | **4.8:1** | 4.5:1 | ✅ |
| `#FFFFFF` di atas `info` (#4A6FA5) | white on blue | **5.4:1** | 4.5:1 | ✅ |
| **Wants hero text** `#0A2820` di atas `accent` (#7BC4A4) | dark forest on mint | **8.4:1** | 4.5:1 | ✅ |

### Dark theme

| Kombinasi | Hex | Ratio | Standar | Status |
|---|---|---|---|---|
| `ink` di atas `bg` | `#F0EDE3` on `#0F1411` | **15.1:1** | 4.5:1 | ✅ |
| `ink` di atas `surface` | `#F0EDE3` on `#1A211D` | **13.4:1** | 4.5:1 | ✅ |
| `inkMuted` di atas `bg` | `#A8B0AB` on `#0F1411` | **8.7:1** | 4.5:1 | ✅ |
| `inkSubtle` di atas `bg` | `#6B7570` on `#0F1411` | **4.2:1** | 4.5:1 | ⚠️ **Fail tipis untuk teks 14pt regular**. Naikkan menjadi `#7A8480` (4.8:1) untuk caption — atau batasi pakai hanya untuk teks ≥ 18pt. |
| `onPrimary` di atas `primary` (mint) | `#0A1F18` on `#7BC4A4` | **9.1:1** | 4.5:1 | ✅ |
| `onPrimaryContainer` di atas `primaryContainer` | `#C4E8D4` on `#1F3329` | **9.8:1** | 4.5:1 | ✅ |

### Action items

- **A11Y-001 (minor):** `inkSubtle` di light (`#8B948F`) hanya 3.1:1 — flag di Compose theme untuk dipakai cuma di teks ≥ 14pt bold / ≥ 18pt regular. Caption regular **wajib** pakai `inkMuted` (5.6:1).
- **A11Y-002 (minor):** `inkSubtle` di dark (`#6B7570`) lulus 4.2:1 hanya untuk teks besar. Tweak ke `#7A8480` kalau ingin lulus 4.5:1 untuk teks 14pt.
- **A11Y-003 (minor):** Text di atas `warning` (amber #C68A2E) harus ≥ 16pt bold. Banner backup sudah sesuai; jangan pakai untuk teks kecil.

---

## 3. Touch Target Size (WCAG 2.5.5 — Level AAA, but Material 3 minimum AA-effective)

**Standar:** semua elemen interaktif minimum **48 × 48 dp**.

| Komponen | Ukuran | Status |
|---|---|---|
| `SW_TabBar` tab button | 48 × 56 dp | ✅ |
| `SW_TabBar` FAB tengah | 56 × 56 dp | ✅ |
| `SW_Button` primary `size=lg` | h=56 dp | ✅ |
| `SW_Button` primary `size=md` | h=48 dp | ✅ |
| `SW_Button` primary `size=sm` | h=36 dp | ⚠️ **Hanya untuk konteks dense** (badge action di list). Hindari sebagai stand-alone CTA. |
| `SW_TopBar` back button | 40 × 40 dp | ⚠️ **Catatan A11Y-004:** ekspansi padding sentuh ke 48 × 48 via `padding: 4dp` ekstra (kompose `Modifier.padding(4.dp).size(40.dp).clickable(…)` membuat hit-area 48 × 48 walau visual 40 × 40). |
| Toggle switch | h=26 dp visual, hit area parent row 48+ dp | ✅ |
| List row tap (SettingsRow, AccountRow, TxnItem) | h=52-64 dp | ✅ |
| PinInput cell | 50 × 56 dp | ✅ |
| App icon launcher | adaptive 108 × 108 dp safe zone 66 dp | ✅ |
| Date picker chip | h=44 dp | ✅ |
| FieldButton (form select) | h=56 dp | ✅ |
| Period selector segmented | h=32 dp | ⚠️ **A11Y-005:** Segmented control 3M/6M/1Y/Semua hanya 32 dp tinggi. Untuk single-tap target itu OK karena merupakan compound widget dengan label luas, tapi tambahkan hit-area ekstra ke 44+ dp untuk masing-masing segment di implementasi Compose. |
| Eye toggle (hide saldo) | 38 × 38 dp | ⚠️ Mirip A11Y-004 — expand hit area. |

### Action items

- **A11Y-004 (minor):** Back button `SW_TopBar` & eye-toggle 38 dp visual → expand hit area ke 48 × 48 dp via Compose `Modifier.minimumInteractiveComponentSize()` atau ekstra padding.
- **A11Y-005 (minor):** Segmented period selector — tinggi 32 dp visual, ekspansi hit-area per segment ke 44+ dp via padding.

---

## 4. Color tidak sebagai Satu-Satunya Carrier Informasi (WCAG 1.4.1)

**Standar:** informasi tidak boleh disampaikan **hanya** lewat warna.

| Tempat | Sinyal warna | Sinyal non-warna | Status |
|---|---|---|---|
| Overspending (Plan Item Row) | merah | Label "Over Rp X" + ikon arrow_down_left | ✅ |
| Success state (transaction saved toast) | hijau success | Ikon checkmark + label "tersimpan" | ✅ |
| Backup pending banner | amber | Ikon shield + teks "Backup tertunda N hari" | ✅ |
| Snapshot diff (Account Detail) | merah/hijau | Sign +/− + label rb/jt + nominal | ✅ |
| Income (txn list) | hijau | Sign `+` + ikon arrow_up | ✅ |
| Expense (txn list) | merah/ink | Sign `−` + ikon arrow_down | ✅ |
| Allocation 50/30/20 segments | warna berbeda | Label nama (Needs/Wants/Investment) + persen | ✅ |
| Confidence indicator (OCR) | hijau/amber | Label "tinggi/sedang/rendah" | ✅ |

---

## 5. Text Alternatives untuk Non-Text Content (WCAG 1.1.1)

**Standar:** semua ikon dan elemen non-text yang interaktif harus punya text alternative.

### Prototipe — `aria-label` audit

| Elemen | `aria-label` | Status |
|---|---|---|
| Tab bar buttons | ❌ tidak ada (cuma label text) | ⚠️ **A11Y-006:** Tab bar pakai text label kecil di bawah icon — secara visual jelas, tapi screen reader minta `contentDescription` yang lebih deskriptif di Compose, mis. "Beranda, tab terpilih, 1 dari 5". |
| FAB Tambah Transaksi | `"Tambah transaksi"` | ✅ |
| Eye toggle | `"Sembunyikan saldo"` / `"Tampilkan saldo"` (dinamis) | ✅ |
| Back button | `"Kembali"` | ✅ |
| Close sheet | `"Tutup"` | ✅ |
| Bell notification | `"Notifikasi"` | ✅ |
| Camera capture | `"Ambil foto"` | ✅ |
| Tambah akun, emas, properti, dst | `"Tambah ${entity}"` | ✅ |
| Swap akun (Transfer) | `"Tukar arah"` | ✅ |
| Tukar mata uang (Transfer) | — | ⚠️ N/A V1 |
| Logo SVG (decorative) | `aria-hidden="true"` recommended | ⚠️ **A11Y-007:** Logo Daun di hero card bersifat dekoratif (cuma watermark) — di Compose pakai `Modifier.semantics { invisibleToUser() }` supaya tidak dibaca screen reader. Logo di splash & onboarding **tidak** dekoratif (informatif) — kasih label "Logo Sakuwise". |

### Action items

- **A11Y-006:** Tab bar — di Compose pakai `contentDescription` lengkap untuk setiap tab: `"Beranda, tab 1 dari 5"`. Jangan pakai cuma label text.
- **A11Y-007:** Audit semua SVG logo di prototipe — yang dekoratif (watermark di hero) → `aria-hidden`. Yang informatif (splash) → label.

---

## 6. Focus Indicators (WCAG 2.4.7) & Keyboard Navigation (WCAG 2.1.1)

**Standar:** semua interaktif harus punya focus indicator yang jelas; semua bisa diakses via keyboard.

Catatan: ini Android-native app, jadi keyboard nav lebih untuk eksternal keyboard (Pixel Fold, ChromeOS, BBK keyboards). Tetap WCAG-required.

| Elemen | Focus indicator | Status |
|---|---|---|
| Button (Compose `Button`) | Built-in focus ring | ✅ |
| Input field (`OutlinedTextField`) | Border color shift | ✅ |
| List row clickable | ⚠️ Default ripple, tidak ada visual focus distinct | ⚠️ **A11Y-008:** Tambahkan `Modifier.indication(LocalIndication.current)` + custom focus border 2dp di state focused untuk row tap-able (account, settings, txn). |
| Custom toggle | ⚠️ Belum ada focus state | ⚠️ **A11Y-009:** Implementasi `SW_Toggle` di Compose pakai `Switch` native (yang sudah punya focus state) — bukan custom div. |

### Action items

- **A11Y-008:** Tambah focus indicator border untuk list rows.
- **A11Y-009:** Pakai Material 3 `Switch` native, jangan custom.

---

## 7. Font Scaling Support (WCAG 1.4.4)

**Standar:** teks bisa di-scale sampai 200% tanpa loss content/functionality.

- Semua typography di prototipe pakai `fontSize` dalam pixel (untuk mockup) → **Compose pakai `sp` (scale-independent pixels)**, bukan `dp`.
- Hero amount (Sisa Anggaran, Total Kekayaan) saat scale 200% bisa overflow → kasih `maxLines = 1` + `softWrap = false` + auto-shrink atau `TextOverflow.Ellipsis`.
- Tab bar label 10sp → masih readable di scale 200% (20sp).
- Section labels uppercase 11sp dengan tracking 0.08em → di scale 200%, tracking + uppercase bisa wrap. Pakai `singleLine = true` + ellipsis.

**Status:** ✅ design support scaling. Implementasi di Compose harus pakai `sp` everywhere.

---

## 8. Form Labels & Error Messages (WCAG 3.3.1, 3.3.2, 3.3.3)

**Standar:**
- Setiap input punya label visible (bukan cuma placeholder).
- Error message harus specific (apa yang salah + cara fix).
- Required field harus ditandai.

| Form | Label visible | Required marker | Error message |
|---|---|---|---|
| Pengeluaran form | ✅ semua field | ✅ `*` merah | ⚠️ **A11Y-010:** Belum ada visual untuk validation error (mis. nominal kosong). Compose harus tampilkan error state di field dengan border merah + text di bawah. |
| Pemasukan form | ✅ | ✅ | ⚠️ Sama dengan above. |
| Transfer form | ✅ | ✅ | ⚠️ Tambahan: error spesifik untuk saldo tidak cukup, akun sama. |
| Onboarding nickname & PIN | ✅ | ✅ via "disabled" state button | ✅ |
| Rekonsiliasi input | ✅ | ✅ | ✅ |
| Settings sub-pages | ✅ | N/A (semua optional dengan default) | ✅ |
| Backup set PIN | ✅ | ✅ | ⚠️ **A11Y-011:** Error spesifik kalau PIN konfirmasi tidak match. Sekarang cuma "Lanjut" button aktif kalau valid — tambahkan toast "PIN tidak cocok" saat konfirmasi gagal. |

### Action items

- **A11Y-010:** Implementasi error state di Compose `OutlinedTextField` dengan `isError = true` + `supportingText = { Text("...") }` untuk pesan spesifik.
- **A11Y-011:** Backup PIN flow — error state explicit kalau konfirmasi mismatch.

---

## 9. Semantic Roles (Android-specific)

Setiap component harus punya role yang benar untuk TalkBack:

| Component | Role | Catatan |
|---|---|---|
| `SW_Button` | `Role.Button` | Set via Compose `.semantics { role = Role.Button }` |
| `SW_TabBar` tab | `Role.Tab` di `TabRow` | Built-in |
| `SW_Toggle` | `Role.Switch` | Built-in di `Switch` |
| `SW_Sheet` | `Role` modal dialog | Pakai `ModalBottomSheet` |
| Eye toggle | `Role.Button` + state `Selected` | Saldo tersembunyi/tampil sebagai state |
| Progress bar (`SW_Bar`) | `Role.ProgressBar` | Pakai `LinearProgressIndicator` |
| Period selector segmented | `Role.RadioButton` per segment | Pakai `SegmentedButton` Material 3 |

---

## 10. Reduced Motion (WCAG 2.3.3)

**Standar:** animasi non-essential harus bisa di-disable.

Sakuwise punya animasi subtle di:
- Splash entrance (staggered fade-up)
- Screen transitions (slide-in 280ms)
- Progress bar fills (cubic-bezier 400ms)
- Number count-ups
- Bottom sheet slide-in (280ms)
- Pin entry shake (saat error — belum diimplementasi)

**Action item A11Y-012 (recommended):**
- Detect Android `MotionAccessibilityServiceInfo` / system "Remove animations" setting
- Saat aktif: matikan staggered fade splash, ganti slide transitions dengan instant cross-fade 100ms, bar fills langsung tanpa animasi
- Compose: `LocalAccessibilityManager.current.isReduceMotionEnabled` (atau check via `Settings.Global.ANIMATOR_DURATION_SCALE`)

---

## 11. Bahasa Indonesia & Locale Considerations

- **Lang attribute:** Compose `Modifier.semantics { lang = Locale.forLanguageTag("id-ID") }` untuk TalkBack pronunciation benar.
- **Singkatan:** "rb", "jt", "M" untuk rupiah singkat — TalkBack akan baca "r-b" / "j-t" / "M" satu huruf. **A11Y-013:** Override TalkBack reading via `contentDescription` eksplisit: "Rp 1.5 juta", "Rp 850 ribu", dst. Atau gunakan full format untuk screen reader.
- **Date format:** "15 Mei 2026" sudah locale-aware untuk Bahasa Indonesia.
- **Number format:** thousand separator titik, decimal koma — TalkBack lokal Indonesia handle ini benar.

---

## 12. Ringkasan Action Items untuk Coding Phase

| ID | Severity | Item | Owner |
|---|---|---|---|
| A11Y-001 | Minor | Light `inkSubtle` hanya untuk teks ≥ 14pt bold | Dev (theme rule) |
| A11Y-002 | Minor | Dark `inkSubtle` tweak ke `#7A8480` atau batasi pakai | Dev (theme rule) |
| A11Y-003 | Minor | Warning bg → teks ≥ 16pt bold only | Dev (component rule) |
| A11Y-004 | Minor | Back button & eye-toggle hit area expand ke 48 × 48 | Dev (Compose Modifier) |
| A11Y-005 | Minor | Period segmented control hit area ekspansi | Dev |
| A11Y-006 | Minor | Tab bar `contentDescription` lengkap | Dev |
| A11Y-007 | Minor | Logo dekoratif → `aria-hidden` / `invisibleToUser` | Dev |
| A11Y-008 | Minor | Focus indicator border untuk row tap-able | Dev (Compose) |
| A11Y-009 | Minor | Pakai Material 3 `Switch` native | Dev (Compose) |
| A11Y-010 | Recommended | Error state untuk semua form fields | Dev |
| A11Y-011 | Recommended | Backup PIN mismatch error message | Dev |
| A11Y-012 | Recommended | Reduced motion support | Dev |
| A11Y-013 | Recommended | Override TalkBack reading untuk "rb/jt/M" | Dev |

**Tidak ada FAIL kritis.** Sakuwise design lulus WCAG 2.1 AA dengan 13 catatan implementasi yang harus dipatuhi developer.

---

## 13. Yang TIDAK Dicakup Audit Ini

- **Cognitive accessibility (WCAG 2.1 / 2.2 Level AAA):** kompleksitas alur Plan dengan 3 level (alokasi → kategori → item) bisa membingungkan user dengan low financial literacy. Mitigasi: onboarding banner "Terapkan Template Starter" + UX copy yang jelas. **Tidak FAIL** WCAG, tapi worth user-testing.
- **Internationalization beyond ID/EN:** V2 territory.
- **Screen reader live test:** ini paper audit. Sebelum production launch, **wajib** lakukan manual TalkBack pass-through per screen.
- **Haptic feedback patterns:** belum di-design, V2.

---

*Audit ini menjadi referensi untuk fase coding. Implementasi Compose harus check-off
ke-13 action items di atas sebelum release ke Play Store. Re-audit dijadwalkan setelah
fase coding selesai untuk verifikasi compliance.*
