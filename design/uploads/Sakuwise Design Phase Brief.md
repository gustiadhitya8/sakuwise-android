# Sakuwise — Design Phase Brief

**Untuk:** Sesi Cowork baru (model Sonnet direkomendasikan, hemat biaya untuk kerja desain iteratif)
**Disiapkan oleh:** Gusti + Claude (sesi sebelumnya, fase requirement)
**Status:** Siap dipakai sebagai prompt awal di Cowork session baru

---

## Cara Pakai Brief Ini

1. Buka Cowork session baru di Claude desktop app.
2. Pastikan model **Sonnet** terpilih (lebih murah untuk iterasi desain).
3. Pastikan folder `202505 Sakuwise` ter-mount sebagai workspace (folder yang sama dengan sesi requirement).
4. Pastikan plugin **`design`** aktif (skill `design-system`, `design-critique`, `accessibility-review`, `design-handoff`, `ux-copy` harus tersedia).
5. Paste seluruh isi brief di bawah ini (dari heading "## Prompt Awal" ke bawah) sebagai pesan pertama ke Sonnet.

---

## Prompt Awal

Halo. Kamu akan menangani **fase desain** untuk aplikasi mobile bernama **Sakuwise**. Requirement sudah final di sesi sebelumnya. Brief ini self-contained — kamu tidak perlu konteks tambahan di luar file di folder ini.

### Tentang Project

- **Nama aplikasi:** Sakuwise
- **Platform:** Android (Kotlin + Jetpack Compose), iOS menyusul nanti
- **Target user:** orang Indonesia, mata uang IDR, Bahasa Indonesia primary
- **Filosofi:** Local-first, terenkripsi, tanpa telemetry, tanpa internet untuk fungsi utama
- **First user:** Gusti (founder), profesional Jakarta dengan keluarga di Tegal

### File yang Wajib Dibaca Lebih Dulu

Di folder `202505 Sakuwise/`:

1. **`Sakuwise PRD v1.3 (ID).md`** — Product Requirements Document versi Bahasa Indonesia. **Ini source of truth.** Baca lengkap sebelum mulai apa pun.
2. **`Sakuwise PRD v1.3.md`** — Versi Bahasa Inggris (backup, untuk cross-reference istilah teknis bila perlu).
3. **`Sakuwise User Requirement.gdoc`** — Requirement awal dari founder (latar belakang).
4. **`Salinan 2026 Money Plan & Tracker.gsheet`** — Spreadsheet manual founder yang akan digantikan aplikasi. Lihat sekilas untuk memahami workflow real.

### Tujuan Fase Ini

Menghasilkan **Sakuwise Design Concept v1.0** — paket lengkap brand identity, design system, mockup key screens, dan handoff spec yang siap dipakai fase coding.

Output bukan sekadar mockup cantik. Output adalah dokumen yang membuat developer (atau Claude Code CLI nanti) bisa langsung implement tanpa bertanya-tanya.

### Deliverables (Harus Ada Semua)

**A. Brand Identity**
- Logo concept (beberapa varian, SVG)
- Wordmark / treatment typography untuk nama "Sakuwise"
- App icon untuk Play Store (adaptive icon Android: foreground + background)
- Tagline pendek dalam Bahasa Indonesia

**B. Visual Language**
- Color palette: primary, secondary, neutral, plus semantic (success, warning, danger, info), untuk light theme dan dark theme
- Typography scale: font family (pastikan support Bahasa Indonesia dan tersedia di Google Fonts), sizes, weights, line-height
- Iconography style guide
- Illustration style (opsional, untuk empty states)

**C. Design Tokens (siap dipakai dev)**
- Color tokens dengan hex values
- Spacing scale (4 / 8 / 16 / 24 dst)
- Border radius scale
- Shadow / elevation scale
- Animation duration & easing curves

Format: JSON atau YAML, lokasi `design/tokens/`.

**D. Key Screens (mockup + annotation)**

Mockup tiap screen utama dalam SVG atau Figma-exportable format. Setiap screen dilengkapi annotation: layout grid, spacing, komponen yang dipakai, interaksi, edge case (loading state, empty state, error state).

Daftar screens:

1. Onboarding 4-langkah (Welcome & bahasa → Nickname & biometric → Privacy notice → Akun pertama)
2. Dashboard (home) dengan semua tile: greeting + plan period, allocation progress bars, income vs expense, daily budget remaining, top 5 spending, accounts summary, net worth, recent transactions
3. Plan view: list rencana per allocation / kategori / item, dengan tombol edit, tambah, copy dari bulan lalu
4. Transaction entry — Pengeluaran (dengan field plan item, akun, foto, link ke hutang opsional)
5. Transaction entry — Pemasukan (dengan field source category, akun, recurring flag)
6. Transaction entry — Transfer (dengan source / destination / fee)
7. Account list & detail (dengan tombol Rekonsiliasi)
8. Reconciliation flow (compare saldo aplikasi vs saldo sebenarnya, konfirmasi adjustment)
9. Gold investment list & detail (dengan snapshot harga global, valuasi current)
10. Land/Property investment list & detail, plus tax payment sub-record
11. Deposit/Retirement investment list & detail dengan chart pertumbuhan
12. Debt list & detail (dengan payment sub-list, tombol Tambah Pembayaran)
13. OCR receipt capture flow (camera → preview → draft review → save)
14. Backup setup & restore flow (set PIN backup → tap Backup → pilih lokasi penyimpanan)
15. Settings (lengkap dengan semua opsi yang disebut di PRD section 7.15)
16. Donation screen (link Saweria/Trakteer + QRIS static)

**E. Component Library**
- Buttons (primary, secondary, text, destructive), dengan semua state (default, pressed, disabled, focused, loading)
- Input fields (text, number/currency, date, picker), dengan state
- Cards & list items
- Modal & bottom sheet
- Banner (info, warning, error)
- Tab bar & top app bar
- Floating Action Button (FAB)
- Chip & tag
- Progress bar (untuk allocation tracking)
- Empty state pattern
- Toast / snackbar

Setiap komponen di-dokumentasikan: props/variants, anatomy, do's & don'ts.

**F. UX Copy (Bahasa Indonesia)**

Microcopy lengkap untuk:
- Onboarding (setiap screen)
- Empty states (tiap screen utama: "Belum ada akun", "Belum ada plan bulan ini", dst.)
- Error messages (validation, network, backup failure, dst.)
- Confirmation dialogs (delete, archive, mark settled, dst.)
- Toast / snackbar messages (saved, deleted, backup success, dst.)
- Tooltip & help text untuk fitur yang non-obvious

Tone: ramah, jelas, tidak corporate-stiff, tidak terlalu santai juga. Pikirkan bahasa percakapan profesional Indonesia.

**G. Accessibility Audit**

Sebelum finalize, jalankan skill `design:accessibility-review` pada semua mockup. Pastikan:
- Color contrast WCAG AA minimum (4.5:1 untuk teks normal, 3:1 untuk teks besar)
- Touch target minimum 48 dp
- Setiap interactive element punya screen reader label
- Color tidak jadi satu-satunya carrier informasi (mis. status pakai icon + warna, bukan warna saja)

Output: `design/accessibility-audit.md` dengan checklist hasil.

**H. Design System Documentation**

Dokumen `Sakuwise Design Concept v1.0.md` (dan .docx) yang menyatukan: filosofi visual, palette, typography, tokens, daftar komponen, daftar pattern. Jadi ini "design system bible" untuk Sakuwise.

**I. Developer Handoff Spec**

File `design/handoff-spec.md` yang menerjemahkan design ke instruksi dev:
- Mapping warna ke nama variable Kotlin / Compose theme
- Spacing dalam dp dengan referensi token
- Komponen ke Composable Compose yang akan dibuat
- Catatan animasi (durasi, easing, target framerate)
- Catatan responsive: minimum width, scaling

Jalankan skill `design:design-handoff` untuk generate ini secara struktur.

### Lokasi Output

Buat subfolder `202505 Sakuwise/design/` (kalau belum ada). Struktur yang diharapkan:

```
202505 Sakuwise/
├── Sakuwise Design Concept v1.0.md          (dokumen utama)
├── Sakuwise Design Concept v1.0.docx        (versi editable)
└── design/
    ├── tokens/
    │   ├── colors.json
    │   ├── spacing.json
    │   ├── typography.json
    │   └── radii.json
    ├── logo/
    │   ├── sakuwise-logo-primary.svg
    │   ├── sakuwise-logo-mono.svg
    │   └── sakuwise-app-icon.svg
    ├── screens/
    │   ├── 01-onboarding-welcome.svg
    │   ├── 02-onboarding-nickname.svg
    │   ├── ... (semua screen di atas)
    ├── components/
    │   └── component-library.md
    ├── accessibility-audit.md
    └── handoff-spec.md
```

### Constraints

- **Material 3 sebagai foundation**, boleh customize tapi jangan menyimpang jauh — ini Android-first dan harus terasa native
- **Mobile-first**, ukuran target Android phone (360–420 dp width)
- **Pertimbangkan iOS portability** — hindari pattern yang sangat Android-specific kalau ada alternatif netral
- **Tone visual:** mengindikasikan privacy/trust, tapi tetap hangat dan personal. Bukan corporate banking, bukan game playful. Lihat referensi: Notion, Linear, Cash App (tone), tapi disesuaikan konteks Indonesia
- **Numeric formatting:** Rupiah dengan thousands separator `.` (Rp 1.500.000), decimal `,`
- **Date format:** `15 Mei 2026` (DD MMM YYYY, Bahasa Indonesia)
- **Tipografi:** harus support karakter Bahasa Indonesia dengan baik, tersedia di Google Fonts, ringan untuk mobile

### Skill yang Tersedia

Pakai promiscuously, tidak perlu hemat:

- `design:design-system` — dokumentasi komponen & variants
- `design:design-critique` — self-review sebelum tunjukkan ke user
- `design:accessibility-review` — audit WCAG AA
- `design:design-handoff` — generate dev spec
- `design:ux-copy` — semua microcopy
- `design:user-research` — opsional, kalau ada assumption yang ingin divalidasi
- `docx`, `pdf`, `pptx` — untuk dokumentasi final
- `visualize` (`show_widget`) — preview mockup inline ke user saat diskusi

### Cara Kerja yang Diharapkan

1. **Baca dulu.** Mulai dengan baca `Sakuwise PRD v1.3 (ID).md` lengkap. Jangan loncat.
2. **Konfirmasi pemahaman.** Summarize ke user dalam 5–8 kalimat: apa produknya, siapa user-nya, apa filosofinya, dan apa scope desain ini.
3. **Tanya brand direction.** Ajukan 3–5 pertanyaan klarifikasi pakai tool `AskUserQuestion` tentang preferensi visual user — mis. mood (warm vs cool), inspirasi yang dia suka, level playfulness, attitude terhadap dark theme. **Jangan mulai desain sebelum dapat jawaban.**
4. **Iteratif, milestone-based:**
   - Milestone 1: brand identity (logo + palette + typography) → user review
   - Milestone 2: design tokens + component library → user review
   - Milestone 3: key screens (dimulai dari Dashboard + Plan + Transaction entry → user review)
   - Milestone 4: sisa screens (investment, debt, settings, dll.) → user review
   - Milestone 5: UX copy lengkap → user review
   - Milestone 6: accessibility audit + handoff spec → final review
5. **Setiap milestone:** tampilkan via `mcp__visualize__show_widget` agar user bisa lihat langsung. Minta feedback sebelum lanjut.
6. **Konfirmasi sebelum overwrite** file yang sudah ada.
7. **Setiap session pakai TaskCreate** untuk track milestone, TaskUpdate untuk progress.

### User & Tone

User adalah **Gusti Adhitya**, founder dan first user aplikasi ini. Sifatnya: skeptical, suka di-push back kalau ada gap, prefer ringkasan jelas vs over-formatting, dan menghargai concrete recommendation daripada jawaban "tergantung".

**Bahasa diskusi: Bahasa Indonesia.** PRD versi Inggris tetap ada untuk backup; semua dokumen baru di-output Bahasa Indonesia.

User tidak punya background desain formal. Hindari jargon desain yang berat tanpa penjelasan. Jelaskan trade-off dengan analogi konkret.

### Yang TIDAK Perlu Dilakukan

- Tidak perlu coding apapun (no Kotlin, no Compose code)
- Tidak perlu setup Android Studio
- Tidak perlu buat working app
- Tidak perlu re-debate requirement yang sudah final di PRD. Kalau menemukan gap, flag ke user pakai `AskUserQuestion`, jangan ubah PRD sepihak
- Tidak perlu brand identity yang "wow factor luar biasa" — yang penting kohesif, profesional, dan sesuai konteks Indonesia

### Definition of Done

Fase ini selesai ketika:

1. Semua deliverables di section "Deliverables" ada di folder `design/`
2. Accessibility audit lulus WCAG AA
3. User puas dengan brand identity & key screens (eksplisit "OK" dari user)
4. Handoff spec cukup detail untuk fase coding bisa langsung jalan
5. `Sakuwise Design Concept v1.0.md` dan `.docx` final tersimpan di root folder project

### Cara Mulai Sekarang

1. Baca `Sakuwise PRD v1.3 (ID).md` (file di folder yang sama dengan brief ini).
2. Summarize pemahaman ke user dalam 5–8 kalimat.
3. Ajukan 3–5 pertanyaan klarifikasi tentang brand & visual direction pakai `AskUserQuestion`.
4. Setelah jawaban masuk, baru mulai Milestone 1 (brand identity).

Mulai sekarang.

---

## Catatan untuk Gusti

Setelah fase desain selesai (semua DoD checklist di atas terpenuhi), kembali ke sesi Cowork dengan Opus (atau sesi baru) untuk:

1. **Generate PRD v1.4** yang mengintegrasi design concept ke dalam requirement doc (mis. update section 7.1 onboarding dengan reference screen mockup, dst.).
2. **Mulai fase coding** dengan Claude Code CLI + Android Studio. Briefing sesi coding bisa pakai pattern serupa: brief self-contained + reference ke PRD v1.4 + handoff spec.

Selama fase desain, kalau Sonnet di sesi terpisah mendapat blocker yang butuh judgement requirement (bukan judgement desain), Gusti bisa balik ke sesi ini (Opus) untuk minta arahan, lalu balik lagi ke sesi desain.

Estimasi durasi fase desain: 3–6 sesi diskusi, tergantung iterasi feedback. Lebih cepat kalau Gusti dapat memutuskan brand direction cepat di awal.
