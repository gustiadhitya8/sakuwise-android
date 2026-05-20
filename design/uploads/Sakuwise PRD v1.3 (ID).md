# Sakuwise — Product Requirements Document

**Versi:** 1.3
**Tanggal:** 16 Mei 2026
**Penulis:** Gusti Adhitya (bersama Claude sebagai ko-penulis)
**Status:** Draft — menunggu fase desain
**Bahasa:** Bahasa Indonesia (versi utama untuk iterasi ke depan; versi Inggris di `Sakuwise PRD v1.3.md` sebagai backup)

---

## Riwayat Revisi

| Versi | Tanggal | Penulis | Perubahan |
|---|---|---|---|
| 1.1 | (sebelumnya) | Gusti | PRD awal (PDF) |
| 1.2 | (sebelumnya) | Gusti | Iterasi PRD (PDF) |
| 1.3 (EN) | 16 Mei 2026 | Gusti + Claude | Diskusi gap-analysis: scope V1 dikunci, data model didefinisikan, model enkripsi, linkage hutang, tanggal mulai bulan yang configurable, entitas transfer, starter template, onboarding diramping. Versi Inggris. |
| 1.3 (ID) | 16 Mei 2026 | Gusti + Claude | Terjemahan ke Bahasa Indonesia sebagai versi utama untuk iterasi ke depan. |

---

## 1. Ringkasan Eksekutif

Sakuwise adalah aplikasi Android yang bersifat local-first untuk perencanaan anggaran dan pelacakan pengeluaran personal, ditargetkan untuk pengguna Indonesia yang melacak keuangan dalam Rupiah. Aplikasi ini menggantikan workflow spreadsheet manual (yang memerlukan laptop dan sulit di-edit di ponsel) dengan aplikasi phone-native yang mendukung ritual rencana-dan-lacak yang sama yang sudah dijalankan pengguna, ditambah pelacakan investasi, hutang, dan total kekayaan secara terintegrasi.

Produk ini dirancang untuk pemakaian founder lebih dulu. Setelah stabil, dapat dirilis ke pasar Indonesia yang lebih luas melalui Google Play, dengan iOS menyusul.

Prinsip utama produk: **local-first** (tidak bergantung cloud, tanpa telemetry), **terenkripsi at-rest** (data keuangan sensitif terlindungi bahkan dari ekstraksi level device), **portable** (satu file backup terenkripsi yang dikendalikan pengguna), dan **berkonteks Indonesia** (Bahasa Indonesia primary, IDR-only, kategori sesuai pola pengeluaran kelas menengah Indonesia).

## 2. Tujuan dan Non-Tujuan

### 2.1 Tujuan (V1)

- Menggantikan workflow Plan → Track bulanan founder yang berbasis spreadsheet ke mobile.
- Mencakup pelacakan keuangan personal kelas menengah Indonesia secara end-to-end: rencana anggaran dengan alokasi 50/30/20 yang configurable, capture pemasukan dan pengeluaran harian, pelacakan saldo multi-akun, pelacakan aset investasi (emas, tanah/properti, deposito/pensiun), dokumentasi hutang, dan dashboard total kekayaan terpadu.
- Memberikan privacy yang kuat: tidak ada dependency internet untuk fungsi inti; enkripsi at-rest; backup terenkripsi yang dimiliki pengguna.
- Menyediakan V1 yang lengkap dan fungsional sehingga founder bisa langsung pakai sebagai pengguna utama (dogfooding).

### 2.2 Non-Tujuan (V1)

Hal-hal berikut secara eksplisit di luar scope V1 dan diparkir di backlog V2: sinkronisasi cloud, dead-man's-switch / notifikasi emergency contact, mata uang asing, OCR untuk struk pemasukan, pelacakan pembayaran pinjaman besar dengan bunga dan amortisasi, kalender payday di luar konfigurasi tanggal mulai bulan tunggal, sharing multi-user / keluarga, iOS, dan semua bentuk telemetry atau analytics.

## 3. Target Pengguna dan Persona

### 3.1 Persona Utama — Gusti (Founder, Pengguna Pertama)

Profesional bergaji asal Indonesia yang tinggal di Jakarta dengan kewajiban keluarga di Tegal. Saat ini memelihara rencana keuangan bulanan yang detail di Google Sheets, menggunakan alokasi 50/30/20 (Needs / Wants / Investment) yang eksplisit, dengan budgeting per-baris Price × Qty dan pelacakan aktual. Pain point: spreadsheet sulit di-edit di ponsel, memerlukan laptop untuk update yang berarti, sehingga sering lupa input; tidak ada view terintegrasi antara investasi, emas, tanah, dan hutang dengan cashflow bulanan; tidak ada cara praktis untuk melampirkan foto struk.

### 3.2 Persona Sekunder — Pengguna Indonesia Lain

Individu Indonesia dengan penghasilan reguler (gaji atau freelance) yang ingin memulai rencana keuangan bulanan terstruktur tapi menganggap spreadsheet rumit. Mereka diuntungkan oleh template starter satu-ketuk dengan kategori Indonesia yang familiar.

## 4. Ringkasan Scope Fitur V1

Rilis V1 mencakup tiga belas modul fitur: Onboarding, Akun, Plan dan Alokasi, Transaksi (Pemasukan / Pengeluaran / Transfer), Item Recurring, Dashboard, Investasi Emas, Investasi Tanah/Properti, Investasi Deposito/Pensiun, Pelacakan Hutang, OCR Receipt Capture, Backup dan Restore Terenkripsi, Pengingat, Donasi, dan Pengaturan.

Detail masing-masing di Section 7.

## 5. Tidak Termasuk V1 (Backlog V2)

Sinkronisasi cloud (auto-upload backup ke Google Drive / iCloud / OneDrive), dead-man's-switch / notifikasi emergency contact, OCR untuk pemasukan (slip gaji, screenshot transfer), pelacakan pembayaran pinjaman besar dengan bunga dan jadwal amortisasi, periode plan payday-to-payday di luar satu tanggal mulai bulan configurable, dukungan mata uang asing, sharing multi-user atau keluarga, sawah / tanah yang otomatis menghasilkan entry pemasukan, port ke iOS, pengingat berulang untuk pembayaran hutang, dan semua bentuk telemetry.

## 6. Default Out-of-the-Box

Pada install baru, pengguna mendarat di aplikasi yang berfungsi dengan default berikut: bahasa Bahasa Indonesia, unlock biometrik aktif, satu akun "Tunai" default dengan saldo 0, belum ada plan dibuat (empty state dengan banner "Terapkan Template Starter" satu-ketuk), tidak ada investasi, tidak ada hutang. Pengguna disapa dengan nama panggilan (dipilih saat onboarding) lalu ditampilkan dashboard.

## 7. Functional Requirements

### 7.1 Onboarding

Alur first-run sengaja dibuat singkat — di bawah tiga puluh detik jika pengguna menerima semua default. Terdiri dari empat layar:

1. **Sambutan & Bahasa.** Visual brand, pilihan satu-ketuk antara Bahasa Indonesia (default) dan English.
2. **Nama Panggilan & Biometrik.** Satu layar dengan dua field: nama panggilan yang dipilih (dipakai hanya sebagai sapaan), dan toggle "Aktifkan unlock biometrik" yang memicu otentikasi sidik jari atau wajah. PIN 6-digit juga di-set di layar ini sebagai fallback bila biometrik gagal (requirement Android).
3. **Pemberitahuan Privacy.** Satu paragraf singkat yang menegaskan prinsip local-first: "Sakuwise menyimpan semua data Anda hanya di ponsel ini. Tidak ada internet, tidak ada telemetry, tidak ada server. Data Anda terenkripsi di disk. Backup berkala untuk melindungi dari kehilangan ponsel." Satu tombol Lanjutkan.
4. **Akun Pertama.** Akun "Tunai" pre-filled dengan saldo 0. Pengguna boleh rename, ubah saldo, atau langsung tap Lanjutkan.

Pengguna kemudian mendarat di dashboard. Dua banner yang dapat di-dismiss muncul pertama kali:

- **"Terapkan Template Starter yang Disarankan"** — satu ketuk untuk memuat struktur kategori default (Section 12) ke plan bulan ini dengan semua nominal kosong.
- **"Ikuti tur singkat"** — tooltip ringan pada tile dashboard.

PIN backup **tidak** di-set selama onboarding. PIN di-set lazily pertama kali pengguna mengetuk Backup / Export (Section 7.10).

### 7.2 Akun

Akun mewakili kumpulan uang apa pun yang dimiliki pengguna: tunai, bank, e-wallet, atau lainnya. Akun adalah unit rekonsiliasi saldo; setiap transaksi terhubung dengan tepat satu akun (atau dua, untuk transfer).

#### Field

Setiap akun memiliki nama (mis. "Mandiri", "GoPay", "Tunai"), label tipe (Tunai / Bank / E-Wallet / Lainnya — hanya informatif, dipakai untuk icon dan grouping), saldo awal yang dimasukkan saat akun dibuat, warna atau icon opsional, dan status aktif/diarsipkan.

#### Model Rekonsiliasi

Sakuwise memakai **model hibrid** untuk saldo akun. Saldo akun dihitung otomatis dari transaksi: saldo awal ditambah pemasukan, dikurangi pengeluaran, ditambah transfer masuk, dikurangi transfer keluar dan fee. Sekali sebulan, pengguna bisa mengetuk **Rekonsiliasi** pada satu akun, mengetik saldo asli (dari aplikasi bank atau e-wallet), dan kalau ada selisih aplikasi mencatat transaksi Penyesuaian bertag "Rekonsiliasi" sehingga saldo terkomputasi sesuai realita. Pola ini memungkinkan pengguna percaya pada saldo aplikasi sekaligus memaafkan transaksi yang sesekali terlewat.

#### Perilaku Arsip

Akun bisa diarsipkan (status di-flip ke "Diarsipkan") bila tidak dipakai lagi. Akun yang diarsipkan tidak muncul di picker akun aktif tapi histori transaksinya tetap terlihat.

### 7.3 Plan dan Alokasi

Plan mewakili anggaran pengguna untuk satu bulan tertentu. Plan memakai hirarki tiga level:

```
Alokasi (Needs / Wants / Investment, dengan target % yang configurable)
  └── Kategori (mis. Tempat Tinggal, Makanan, Hiburan)
        └── Plan Item (mis. Listrik, Makan Harian, Netflix)
```

Setiap Plan Item punya nominal rencana, parent kategori, alokasi turunan, flag recurrence opsional (one-off / monthly / quarterly / yearly), dan catatan opsional. Kategori punya roll-up total sendiri; pengguna bisa set nominal rencana di level kategori (mewakili budget fleksibel) yang lalu membatasi jumlah anak-anaknya, atau membiarkannya diturunkan murni dari anak. Alokasi punya target persentase yang configurable totalnya 100%, default 50 / 30 / 20.

#### Periode Plan

Satu plan mencakup "bulan plan". Default-nya, bulan plan adalah bulan kalender (tanggal 1 sampai akhir bulan). Pengguna bisa konfigurasi **Tanggal Mulai Periode Plan** antara 1 sampai 28 di Pengaturan; bila di-set ke N, bulan plan berjalan dari tanggal N di satu bulan kalender ke tanggal N-1 di bulan berikutnya.

Label plan mengikuti konvensi **bulan-akhir**: periode plan yang berakhir di Mei diberi label "Plan Mei 2026", terlepas dari kapan dimulai. Untuk tanggal mulai = 1, ini menghasilkan bulan kalender normal. Untuk tanggal mulai = 25, "Plan Mei 2026" mencakup 25 April 2026 sampai 24 Mei 2026.

Bila tanggal mulai yang dikonfigurasi melebihi jumlah hari dalam bulan tertentu (mis. 30 di Februari), periode plan jatuh ke hari terakhir bulan tersebut. Mengubah tanggal mulai di tengah-tengah pemakaian hanya mempengaruhi plan ke depan; plan yang sudah ada mempertahankan tanggal aslinya.

#### Edit di Tengah Bulan

Semua plan item, kategori, dan persentase alokasi bisa di-edit kapan saja selama periode plan aktif. Menambah plan item baru di tengah bulan berlaku retroaktif untuk transaksi yang sudah tercatat di periode tersebut bila pengguna meng-assign ulang. Persentase alokasi dihitung ulang secara live.

#### Overspending

Bila pengeluaran aktual melebihi anggaran pada satu plan item, tiga sinyal visual muncul dalam warna merah: baris plan item, total kategori parent, dan progress bar alokasi parent. Overspending diperbolehkan (aplikasi tidak memblokir input); tujuannya adalah awareness, bukan enforcement.

#### Leftover (Sisa)

Bila total pengeluaran satu bulan kurang dari total pemasukan, sisanya ditampilkan sebagai "Sisa" di dashboard dan plan view. Tidak ada aritmatika carry-over otomatis; uang dianggap tetap berada di akun mana pun yang menyimpannya. Pengguna boleh memindahkannya secara manual (lewat Transfer ke akun tabungan, atau dengan menyesuaikan plan bulan depan).

### 7.4 Transaksi

Tiga tipe transaksi: **Pemasukan**, **Pengeluaran**, dan **Transfer**.

#### Pemasukan

Field: tanggal, nominal, kategori sumber (lookup: Gaji Pokok, Bonus, THR, Penghasilan Sampingan, Lainnya — dapat diedit), flag recurring, akun tujuan, catatan opsional. Transaksi pemasukan menambah saldo akun tujuan.

"Pemasukan yang Diharapkan" di level plan adalah angka yang dimasukkan manual saat plan dibuat; nilainya tidak diturunkan otomatis dari transaksi pemasukan. Ini sesuai workflow spreadsheet pengguna di mana gaji 25 Mei dicatat dengan tanggal 25 Mei tapi mentally "untuk" plan Juni.

#### Pengeluaran

Field: tanggal, nominal, plan item (**wajib** — aplikasi memaksa setiap pengeluaran terhubung ke plan item yang sudah ada; bila perlu, pengguna boleh menambah plan item baru dari alur entry), akun yang dibayarkan, catatan opsional, foto struk opsional (JPEG terkompresi disimpan sebagai BLOB terenkripsi di dalam database), link "Tautkan ke hutang" opsional (Section 7.8). Transaksi pengeluaran mengurangi saldo akun sumber.

Transaksi pengeluaran tunggal tidak punya field quantity. Model Price × Qty dari spreadsheet adalah alat *planning* saja; pengeluaran aktual diinput sebagai satu nominal dengan tanggal apa pun yang dipilih pengguna.

#### Transfer

Transfer memindahkan uang antara dua akun yang dimiliki pengguna. Field: tanggal, akun sumber, akun tujuan, nominal yang ditransfer, fee opsional, assignment plan-item untuk fee (di-suggest otomatis "Biaya Transfer" atau kategori pengeluaran lain pilihan pengguna), catatan opsional. Transfer mendebit akun sumber sebesar (nominal + fee), mengkredit akun tujuan sebesar nominal, dan fee terhitung sebagai pengeluaran terhadap plan item yang di-assign.

#### Transaksi Tanggal Lampau

Pengguna boleh mengisi transaksi dengan tanggal kapan pun, lampau atau sekarang. Transaksi dengan tanggal masa depan diperbolehkan tapi ditandai "Scheduled" di timeline; transaksi tersebut tidak mempengaruhi saldo akun sampai tanggalnya tiba.

### 7.5 Item Recurring dan Template Plan

#### Flag Recurrence Per-Item

Setiap plan item dan sumber pemasukan punya flag recurrence: `one-off`, `monthly`, `quarterly`, atau `yearly`.

#### Auto-Generate Plan Baru

Saat pengguna membuat periode plan baru (baik dengan menekan "Mulai bulan baru" atau otomatis saat periode sekarang berakhir), aplikasi menghasilkan draft plan baru yang sudah berisi semua item `monthly` dari periode sebelumnya, plus item `quarterly` atau `yearly` yang jatuh tempo di periode baru. Nominal rencana ikut terbawa. Pengguna lalu meng-edit, menambah, atau menghapus sesuai kebutuhan sebelum menyimpan draft.

Item one-off tidak terbawa ke depan.

#### Template Starter yang Disarankan

Pada aplikasi yang masih baru, view plan kosong menampilkan satu banner dismissable: "Terapkan Template Starter yang Disarankan." Satu ketuk memuat struktur di Section 12. Pengguna bisa edit atau hapus setelahnya. Bila dilewati, plan kosong.

### 7.6 Dashboard

Dashboard adalah layar utama dan permukaan paling penting di produk. Menampilkan tile-tile berikut, urut, bisa di-scroll vertikal:

1. **Sapaan + Periode Plan.** "Selamat pagi, Gusti — Plan Mei 2026, sisa 16 hari."
2. **Progress Alokasi.** Tiga progress bar horizontal (Needs / Wants / Investment) menampilkan aktual vs rencana, dengan segmen overflow merah saat over budget. Bisa di-tap untuk drill ke detail alokasi.
3. **Pemasukan vs Pengeluaran (periode ini).** Dua angka besar dan satu baris sisa: "Pemasukan Rp X · Pengeluaran Rp Y · Sisa Rp Z."
4. **Sisa Anggaran Harian.** Nilai terkomputasi: (anggaran tersisa) / (hari tersisa di periode). Membantu pengguna pacing.
5. **5 Kategori Pengeluaran Teratas.** Daftar terurut kategori dengan aktual tertinggi periode ini.
6. **Ringkasan Akun.** Setiap akun aktif dengan saldo terkomputasi saat ini; tap untuk drill ke detail. Total ditampilkan di bawah.
7. **Total Kekayaan.** Jumlah semua saldo akun + nilai emas (di harga jual saat ini) + tanah/properti di nilai saat ini + saldo deposito/pensiun − hutang outstanding (di mana arahnya = saya berhutang).
8. **Transaksi Terbaru.** 10 transaksi terakhir lintas akun, dengan icon tipe.
9. **Banner.** Backup tertunda (>30 hari kuning, >60 hari modal saat cold launch), template starter yang disarankan (hanya first-run), tur (hanya first-run).

### 7.7 Investasi — Emas

Satu record Aset Emas mewakili satu batang atau satu batch pembelian. Field: tanggal pembelian, berat dalam gram, nomor seri (opsional), harga beli total, catatan opsional, foto opsional (BLOB terenkripsi), status (dipegang / dijual), dan kalau dijual: tanggal jual dan harga jual.

**Harga Jual Emas Global** terpisah di-set di Pengaturan (input manual; harga yang di-fetch cloud ditunda ke V2). Saat di-set, aplikasi menghitung nilai saat ini tiap aset emas yang dipegang (berat × harga global) dan profit/loss vs harga beli, baik dalam IDR maupun persentase. Sebuah tile ringkasan menjumlahkan semua nilai emas yang dipegang, ditampilkan di layar Investasi dan dimasukkan ke Total Kekayaan di dashboard.

Emas yang dijual tetap di history (status = Dijual) dan tidak dihitung di nilai saat ini; transaksi harga-jual dapat ditautkan ke entry pemasukan ke akun pilihan.

### 7.8 Investasi — Tanah / Properti

Satu record Aset Tanah/Properti mewakili sebidang tanah, sawah, atau bangunan. Field: nama, lokasi, ID tanah/SHM, luas dalam meter persegi, harga beli, nilai estimasi saat ini opsional (di-update manual), catatan opsional, foto opsional (BLOB terenkripsi), status (dipegang / dijual), tanggal jual, harga jual.

#### Sub-Record Pembayaran Pajak

Setiap aset tanah punya daftar Pembayaran Pajak. Field per pembayaran: tanggal, nominal, foto bukti PBB opsional (JPEG terkompresi, BLOB terenkripsi di dalam database, target ~200 KB setelah kompresi), dan akun yang membayar (opsional, membuat transaksi pengeluaran tertaut).

#### Properti di Total Kekayaan

Tanah/Properti berkontribusi nilai estimasi saat ini (bila di-set) atau harga beli (fallback) ke tile Total Kekayaan.

### 7.9 Investasi — Snapshot Deposito / Pensiun

Untuk DPLK, BPJSTK JHT, deposito waktu (Deposito), atau akun akumulasi sejenis di mana pengguna ingin melacak saldo dari waktu ke waktu.

Field record aset: nama (mis. "BPJSTK JHT", "DPLK Mandiri"), label tipe (DPLK / BPJSTK / Deposito / Lainnya), info institusi/akun (free text), catatan opsional, status (aktif / tutup).

Setiap aset punya daftar **Snapshot**: tanggal dan saldo. Pengguna mengisi snapshot kapan saja diinginkan — biasanya bulanan. Aplikasi menampilkan grafik garis pertumbuhan dari waktu ke waktu.

Backfill snapshot lama diperbolehkan kapan saja. Aplikasi tidak mewajibkan entry bulanan dan tidak menandai bulan yang terlewat.

Saldo deposito/pensiun masuk ke Total Kekayaan menggunakan snapshot terbaru per aset.

### 7.10 Pelacakan Hutang

Modul hutang bersifat documentation-first dengan tautan akun manual.

#### Record Hutang

Field: nama counterparty (lawan transaksi), arah (saya berhutang / mereka berhutang ke saya), nominal pokok, tanggal mulai, tanggal jatuh tempo opsional, status (terbuka / lunas), catatan opsional.

#### Alur Pembuatan

Saat hutang dibuat, aplikasi bertanya: "Apakah hutang ini mempengaruhi salah satu akun Anda?" Tiga opsi: Ya (pilih akun → aplikasi membuat transaksi inflow atau outflow ter-tag debt yang meng-update saldo akun tapi **tidak** terhitung di statistik pemasukan/pengeluaran bulanan), Tidak, Nanti. Pengguna bisa menambahkan link akun belakangan dengan mengedit hutang.

#### Pelacakan Pembayaran

Setiap record hutang punya sub-list Pembayaran. Field per pembayaran: tanggal, nominal, akun pembayar / penerima, catatan opsional. Setiap pembayaran otomatis membuat transaksi yang sesuai di akun tertaut — dan transaksi ini **terhitung** ke plan bulanan, karena cicilan pinjaman di dunia nyata adalah arus kas riil.

Ada dua jalur entry:

- **Jalur A — dari layar Hutang.** Buka hutang → tap "Tambah Pembayaran" → isi nominal, tanggal, akun, opsional pilih plan item (mis. "Cicilan KPR"). Aplikasi membuat baik record pembayaran maupun transaksi terhitung-plan yang tertaut.
- **Jalur B — dari layar pengeluaran.** Tambah pengeluaran seperti biasa, dengan plan item "Cicilan KPR" dan akun Mandiri. Sebelum simpan, toggle "Tautkan ke hutang?" dan pilih hutang yang dimaksud. Aplikasi men-link transaksi yang sama ke daftar pembayaran hutang.

Kedua jalur menghasilkan data yang identik: satu transaksi dengan foreign key `debt_id` opsional, DAN satu record pembayaran di hutang.

**Outstanding** dihitung: pokok − jumlah(pembayaran). Status auto-flip ke Lunas saat outstanding mencapai nol, atau pengguna bisa manual menandai Lunas dengan write-off sisa.

#### Yang Tidak Dicover V1

Tidak ada perhitungan bunga, tidak ada jadwal amortisasi, tidak ada split principal-vs-bunga per pembayaran, tidak ada pengingat pembayaran otomatis. Semuanya V2.

### 7.11 OCR Receipt Capture

Pengguna bisa melampirkan struk ke transaksi pengeluaran via OCR. Alurnya:

1. Dari layar entry pengeluaran, tap icon kamera.
2. Pilih **Kamera** (ambil foto baru), **Galeri** (pilih gambar yang sudah ada), atau via Android **Share** intent (aplikasi lain men-share gambar ke Sakuwise).
3. Gambar dijalankan ke OCR on-device via Android ML Kit Text Recognition.
4. Aplikasi mengisi draft pengeluaran: tanggal (di-extract), nominal (angka terbesar berformat mata uang di struk), nama merchant (baris teks paling atas), dengan pengguna memilih plan item dan akun.
5. Pengguna mengoreksi dan menyimpan. Foto-nya disimpan sebagai BLOB JPEG terkompresi di dalam database terenkripsi (≈ 200 KB setelah kompresi quality 70%, max long edge 1600 px).

OCR sepenuhnya berjalan on-device. Tidak ada gambar yang keluar dari ponsel. ML Kit Text Recognition beroperasi tanpa koneksi internet di Android.

OCR untuk struk **pemasukan** (slip gaji, screenshot transfer) adalah V2 — akurasi OCR engine saat ini terlalu inkonsisten untuk screenshot aplikasi bank Indonesia untuk berguna di V1.

### 7.12 Backup dan Restore

#### Model Enkripsi

Sakuwise memakai model dua-kunci.

**Data Encryption Key (DEK).** Kunci AES 256-bit yang di-generate random, dibuat saat first launch. DEK meng-enkripsi database SQLite via SQLCipher. DEK tidak pernah keluar dari device dalam bentuk plaintext.

**Key Encryption Key (KEK).** Dua KEK ada:

- **Device KEK** berada di Android Keystore, hardware-backed di mana ponsel mendukungnya. Di-unlock oleh biometrik atau credential device (PIN). Ini yang meng-unlock aplikasi setiap hari.
- **Backup KEK** diturunkan dari PIN 6-digit yang di-set pengguna (default; pengguna bisa beralih ke passphrase di Pengaturan → Advanced). Diturunkan via Argon2id (memory ≥ 64 MB, iterations di-tune untuk ~1 detik di device Android mid-range). Dipakai untuk meng-enkripsi file backup.

#### Setup PIN Backup (Lazy)

Tidak ada PIN backup yang di-set selama onboarding. Pertama kali pengguna mengetuk Backup / Export, aplikasi minta PIN 6-digit, dua kali. PIN disimpan (juga di-wrap oleh device Keystore) sehingga export berikutnya berjalan tanpa prompt ulang. Pengguna boleh mengubah PIN backup kapan saja di Pengaturan.

Pengguna tidak bisa export tanpa men-set PIN. Tidak ada opsi export plain-text — riwayat keuangan lengkap pengguna harus selalu terenkripsi sebelum keluar dari device.

#### File Backup

Mengetuk **Backup** menghasilkan satu file `sakuwise-backup-YYYY-MM-DD-HHmm.sakuwise` berisi payload SQLite terenkripsi (BLOB) dan header kecil (versi format, parameter KDF, DEK yang di-wrap). Pengguna memilih tempat menyimpan — storage lokal, USB, manual upload ke Google Drive / iCloud / Dropbox.

#### Restore di Device Baru

Install Sakuwise → di first launch, alih-alih "Setup baru" tap "Restore dari file" → pilih file `.sakuwise` → masukkan PIN backup. Aplikasi men-generate device Keystore key baru untuk unlock harian, men-decrypt backup, dan pengguna masuk dengan semua data utuh.

#### Pengingat Backup

Dashboard menampilkan banner kuning "Backup tertunda" bila backup sukses terakhir lebih dari 30 hari yang lalu. Setelah 60 hari, modal blocking muncul saat aplikasi dibuka, memerlukan dismiss eksplisit.

#### Yang Tidak Dicover V1

Tidak ada auto-upload cloud. Pengguna bertanggung jawab memindahkan file `.sakuwise` ke lokasi aman. Auto-upload ke Google Drive / iCloud / OneDrive adalah V2.

### 7.13 Pengingat dan Notifikasi

Tiga tipe notifikasi didukung di V1, semuanya opt-in:

1. **Pengingat pembayaran pengeluaran berulang.** Per plan item, pengguna bisa mengaktifkan pengingat ("Ingatkan saya tanggal 1 setiap bulan"). Diimplementasikan via Android `WorkManager`. Pengguna harus memberikan izin POST_NOTIFICATIONS Android 13+, di-prompt pertama kali pengingat dibuat.
2. **Backup tertunda.** Banner in-app (kuning di 30 hari, modal di 60 hari). Tidak memakai notifikasi sistem.
3. **Peringatan overspending.** Saat pengeluaran aktual pada satu alokasi mencapai 100% di tengah bulan, banner in-app muncul saat next dashboard view.

Tidak ada push notifikasi sistem untuk backup atau overspending — keduanya hanya dipermukaan sebagai banner in-app.

### 7.14 Donasi

Layar Donasi sederhana yang dapat diakses dari menu Pengaturan dan layar Tentang. Berisi:

- Catatan singkat menjelaskan bahwa aplikasi gratis dan dikembangkan secara lokal.
- Tombol link ke halaman Saweria atau Trakteer founder (URL configurable saat build).
- Gambar QRIS statis ter-embed di aplikasi.

Tidak ada proses pembayaran di dalam Sakuwise. Semua donasi mengalir melalui platform eksternal.

### 7.15 Pengaturan

Layar Pengaturan menampilkan:

- Bahasa (Bahasa Indonesia / English)
- Nama panggilan
- Biometrik on/off
- PIN device (fallback biometrik)
- Durasi auto-lock (default 5 menit; opsi: 1, 5, 15, 30 menit, Langsung)
- Tanggal Mulai Periode Plan (default 1, configurable 1–28)
- Persentase alokasi default (Needs / Wants / Investment, total 100%, default 50 / 30 / 20)
- Harga Jual Emas Global (input manual, dipakai untuk valuasi emas)
- Backup: Timestamp backup terakhir, Backup Sekarang, Ubah PIN Backup, Beralih ke Passphrase (advanced)
- Tentang (versi, link donasi, URL kebijakan privasi)
- Export / Reset (dengan konfirmasi)

## 8. Data Model

Entitas-entitas berikut membentuk data model V1. Semua entitas tersimpan di satu database SQLite terenkripsi via SQLCipher.

**Profil Pengguna** (singleton). Nama panggilan, bahasa, tanggal mulai periode plan, default alokasi, durasi auto-lock, harga jual emas, timestamp backup terakhir, flag onboarding selesai.

**Akun.** id, nama, label tipe, saldo awal, warna/icon, status (aktif / diarsipkan), tanggal dibuat.

**AccountSnapshot.** id, account_id, tanggal snapshot, saldo teramati, saldo terkomputasi saat snapshot, nominal penyesuaian (selisihnya, di-capture sebagai transaksi rekonsiliasi). Satu per event rekonsiliasi.

**Plan.** id, tanggal mulai periode, tanggal akhir periode, label (turunan, mis. "Mei 2026"), pemasukan diharapkan (input manual), catatan.

**Alokasi.** id, plan_id, nama (Needs / Wants / Investment), target persentase. Tiga per plan default tapi configurable.

**Kategori.** id, alokasi_id, nama, nominal rencana (opsional — bisa null bila diturunkan dari anak).

**PlanItem.** id, kategori_id, nama, nominal rencana, recurrence (one-off / monthly / quarterly / yearly), catatan.

**Transaksi.** id, tanggal, nominal, tipe (income / expense / transfer / debt_inflow / debt_outflow / reconciliation), plan_item_id (nullable; wajib untuk tipe=expense), akun_sumber_id, akun_tujuan_id (nullable; untuk transfer saja), nominal_fee (nullable; untuk transfer saja), debt_id (nullable; bila tertaut ke hutang), photo_blob (nullable; JPEG terkompresi), catatan, dibuat_pada.

**IncomeCategory.** id, nama. Tabel lookup yang di-seed dengan Gaji Pokok, Bonus, THR, Penghasilan Sampingan, Lainnya. Setiap Transaksi pemasukan punya tambahan `income_category_id` yang nullable.

**Asset_Gold.** id, tanggal beli, berat_gram, seri, harga_beli, catatan, photo_blob, status, tanggal_jual, harga_jual.

**Asset_Land.** id, nama, lokasi, sertifikat_id, luas_m2, harga_beli, nilai_saat_ini, catatan, photo_blob, status, tanggal_jual, harga_jual.

**LandTaxPayment.** id, asset_land_id, tanggal_bayar, nominal, akun_id (nullable, untuk transaksi tertaut), photo_blob, catatan.

**Asset_Deposit.** id, nama, label_tipe, info_institusi, catatan, status.

**AssetDepositSnapshot.** id, asset_deposit_id, tanggal_snapshot, saldo, catatan.

**Debt.** id, counterparty, arah (saya_berhutang / berhutang_ke_saya), pokok, tanggal_buka, tanggal_tutup_diharapkan, status, catatan.

**DebtPayment.** id, debt_id, tanggal_bayar, nominal, akun_id, transaction_id (link ke Transaksi auto-create).

### Relasi Kunci

Satu Transaksi bisa milik PlanItem (untuk pengeluaran) dan Debt (untuk transaksi tertaut hutang). Transfer mereferensi dua Akun. Flag recurrence Plan Item mengatur auto-generate periode berikutnya. Total Kekayaan di dashboard adalah agregat terkomputasi di atas Akun, Asset_Gold (dipegang saja, di harga jual saat ini), Asset_Land (dipegang saja, di nilai_saat_ini atau harga_beli), AssetDepositSnapshot terbaru per Asset_Deposit, dan Debt dengan arah=saya_berhutang (dikurangkan).

## 9. Arsitektur Teknis

### 9.1 Platform

Hanya Android untuk V1, target API 26 (Android 8.0 Oreo) ke atas untuk mencakup basis device Indonesia praktis. Dibangun dengan Kotlin memakai Jetpack Compose untuk UI. Material 3 sebagai design system baseline (di-refine di fase desain). Arsitektur single-activity.

### 9.2 Storage

SQLite via SQLCipher 4.x untuk enkripsi at-rest. Room sebagai ORM layer di mana kompatibel, atau SQLDelight langsung bila Room menambah friction. Semua foto disimpan sebagai BLOB di dalam database terenkripsi, terkompresi ke JPEG quality 70, max long edge 1600 px, target ~200 KB per foto.

### 9.3 Kriptografi

- **Enkripsi DB at-rest:** SQLCipher dengan key AES 256-bit (DEK).
- **Wrapping DEK:** AES-GCM yang terikat Android Keystore. Hardware-backed di mana tersedia.
- **Enkripsi file backup:** AES-256-GCM, key diturunkan dari PIN/passphrase pengguna via Argon2id (memory 64 MB, iterations 3, parallelism 1, salt random 16 B). Wrapping key ditulis ke header file backup bersama parameter KDF.

### 9.4 OCR

Android ML Kit Text Recognition v2 (on-device, gratis, tidak bergantung Play Services di sebagian besar versi Android saat ini; degradasi anggun bila tidak ada).

### 9.5 Pengingat

`WorkManager` untuk notifikasi lokal terjadwal; `NotificationChannel` di-set saat pengingat pertama.

### 9.6 Build & Distribusi

Google Play Store. Aplikasi gratis dengan link donasi in-app keluar opsional. Penandatanganan aplikasi via Play App Signing. Tidak ada SDK analytics; tidak ada SDK crash reporting di V1 (dapat diterima untuk fase founder-as-user; pertimbangkan crash reporting opt-in privacy-respecting seperti Sentry on-prem di V2).

### 9.7 Tidak Ada Permission Internet

Manifest Android **tidak meminta** permission `INTERNET` di V1. Ini adalah jaminan terkuat yang mungkin diberikan ke pengguna bahwa tidak ada data yang meninggalkan device. (Link donasi membuka di browser eksternal, yang punya akses network sendiri.) Saat sinkronisasi cloud ditambahkan di V2, permission INTERNET akan diminta dengan penjelasan eksplisit.

## 10. Keamanan dan Privasi

### 10.1 Threat Model

Produk melindungi dari: ekstraksi data tingkat device (ADB, alat forensik), device curian dalam keadaan unlock (sedikit — biometrik auto-lock default 5 menit), dan kebocoran data tidak sengaja via file backup (dimitigasi dengan enkripsi wajib semua export).

Produk **tidak** melindungi dari: OS Android itu sendiri yang ter-compromise (tidak ada pertahanan yang mungkin di lapisan aplikasi), pengguna yang secara sukarela membagi file backup dan PIN-nya, atau malware yang membaca layar saat aplikasi sedang ter-unlock.

### 10.2 Penanganan Data

Tidak ada data pengguna yang meninggalkan device secara otomatis. Aplikasi tidak meminta permission INTERNET. Hanya alur keluar yang bisa dipicu pengguna:

- Manual menyimpan file `.sakuwise` ke storage lokal atau folder yang ter-sinkronisasi cloud.
- Manual membuka link donasi, yang meninggalkan Sakuwise via Intent sistem.

### 10.3 Kebijakan Privasi

Kebijakan privasi singkat akan di-host di URL stabil (wajib oleh Google Play). Isinya: tidak ada data dikumpulkan, tidak ada analytics, tidak ada akses network yang diminta, sumber penuh dari semua data pengguna adalah input pengguna sendiri, backup dikendalikan pengguna.

### 10.4 Data Personal yang Tersimpan

Aplikasi tidak menyimpan identifier personal apa pun di luar nama panggilan yang dipilih pengguna. Tidak ada email, tidak ada telepon, tidak ada NIK, tidak ada tanggal lahir. Data pengguna murni keuangan: nominal, kategori, tanggal, nama akun, catatan, foto struk opsional.

## 11. Non-Functional Requirements

**Performa.** App cold start di bawah 2 detik di device mid-range (Snapdragon 6-series, 4 GB RAM). Query database untuk dashboard kembali dalam di bawah 500 ms dengan setahun riwayat transaksi.

**Lokalisasi.** Bahasa Indonesia sebagai primary, English sebagai fallback. Semua string UI di-externalize ke `strings.xml` (`values-id/`, `values/`). Format mata uang `Rp 1.500.000,00` (konvensi Indonesia — `.` sebagai pemisah ribuan, `,` sebagai desimal). Tanggal default `DD MMM YYYY` dalam Bahasa (mis. `15 Mei 2026`).

**Aksesibilitas.** Dukung font scaling Android. Kontras warna memenuhi WCAG AA. Touch target memenuhi minimum Material 3 (48 dp).

**Footprint Storage.** App + media tetap di bawah 60 MB. Ukuran database scale dengan pemakaian; setahun pemakaian tipikal menghasilkan di bawah 50 MB bahkan dengan foto struk.

**Reliability.** Penulisan database di-wrap dalam transaction. Tidak ada kehilangan data saat aplikasi crash. WorkManager menjamin delivery pengingat dalam jendela yang diijinkan sistem Android.

## 12. Isi Template Starter Default

Saat pengguna mengetuk "Terapkan Template Starter yang Disarankan," struktur berikut dimuat ke plan saat ini dengan semua nominal dibiarkan kosong. Pengguna mengisi nominal dan/atau menghapus item yang tidak dipakai.

**Pemasukan (kategori yang disarankan):** Gaji Pokok (recurring bulanan), Bonus (one-off), THR (recurring tahunan), Penghasilan Sampingan (one-off), Lainnya (one-off).

**Alokasi Needs — 50%:**

- *Tempat Tinggal:* Kos/Sewa/Cicilan Rumah; Listrik; Air PAM; Gas LPG; Internet; Air Galon.
- *Makanan:* Makan Harian; Belanja Bulanan.
- *Transportasi:* BBM; Transportasi Online; Transportasi Umum; Tiket Mudik.
- *Kendaraan:* Servis; Pajak Kendaraan (tahunan); Asuransi Kendaraan (tahunan).
- *Kesehatan:* BPJS Kesehatan; Asuransi Kesehatan; Obat-obatan.
- *Komunikasi:* Pulsa; Paket Data.
- *Pajak & Iuran:* PBB (tahunan); Iuran RT/RW; Sampah.
- *Sosial:* Sedekah/Zakat; Acara Keluarga / Kondangan.

**Alokasi Wants — 30%:**

- *Hiburan:* Streaming (Netflix, Spotify, dll); Bioskop; Gaming.
- *Hobi:* Gadget; Olahraga; Buku.
- *Makan di Luar:* Kopi/Kafe; Restoran; Jajan.
- *Self Care:* Skincare; Salon / Barbershop.
- *Belanja:* Pakaian; Elektronik.

**Alokasi Investment — 20%:**

- *Tabungan:* Dana Darurat; Tabungan Reguler.
- *Investasi:* Emas; Reksa Dana / Saham; DPLK Tambahan; Properti.
- *Pendidikan:* Kursus; Sertifikasi; Buku Belajar.

Pengguna bisa menghapus item yang tidak dibutuhkan (mis. hapus Pajak Kendaraan bila tidak punya kendaraan), menambah yang baru, atau menghapus seluruh template dan mulai dari nol.

## 13. Backlog V2

Hal-hal berikut secara formal ditunda untuk V2 atau lebih jauh, dalam urutan prioritas kasar. Masing-masing telah dipertimbangkan dan dengan sengaja dikeluarkan dari V1 untuk menjaga scope tetap fokus:

1. **Auto-sinkronisasi cloud backup.** Upload otomatis file `.sakuwise` ke Google Drive / iCloud / OneDrive dengan jadwal yang dapat dikonfigurasi. Memerlukan permission INTERNET dan alur OAuth.
2. **Pelacakan pembayaran pinjaman besar.** Di luar model V1 yang sederhana "outstanding = pokok − jumlah(pembayaran)": perhitungan bunga, jadwal amortisasi, split principal-vs-bunga per pembayaran, pengingat terjadwal.
3. **OCR untuk struk pemasukan.** Slip gaji dan OCR screenshot transfer aplikasi bank.
4. **Port iOS.** Implementasi Swift / SwiftUI dengan data model dan feature set yang sama, hanya berbagi format file.
5. **Dukungan mata uang asing.** Tag mata uang per-transaksi, kurs FX (manual atau di-fetch), konversi default ke mata uang home.
6. **Sharing keluarga / multi-user.** Berbagi satu plan antar anggota rumah tangga, dengan conflict-free merge.
7. **Sawah / tanah auto-generate pemasukan.** Saat aset tanah dikonfigurasi sebagai income-producing, aplikasi memprompt log pemasukan panen sesuai jadwal.
8. **Notify-if-Die / Emergency contact.** Baik check-in sisi-client dengan dead-man's switch yang didukung cloud, atau bundel terenkripsi pre-shared yang dititipkan ke kontak terpercaya.
9. **Kalender payday-week custom** di luar tanggal mulai bulan tunggal yang configurable. Mis. siklus payday dua-mingguan.
10. **Harga emas yang di-fetch cloud.** Integrasi feed harga Pegadaian / Antam.
11. **Pengingat pembayaran hutang berulang.**
12. **Valuasi properti saat ini yang di-fetch cloud** (mis. via sumber data pasar).
13. **Berbagi transaksi individual atau laporan** via export PDF.

## 14. Keputusan Terbuka / Dituntaskan di Fase Desain

Berikut sengaja dibiarkan untuk fase desain (sesi terpisah dengan Sonnet):

- Brand identity (logo, treatment nama).
- Color palette dan typography.
- Iconography untuk akun, kategori, transaksi.
- Ilustrasi empty-state.
- Visual layar onboarding.
- Desain visual tile dashboard.
- Microcopy dan tone of voice.
- Tema light vs dark.
- Icon aplikasi untuk Play Store.

Deliverable "Sakuwise Design Concept" dari sesi desain akan masuk balik ke v1.4 PRD ini, mengintegrasikan asset desain final dan material untuk fase development.

## 15. Glosarium

Istilah keuangan Indonesia pilihan yang dipakai di dokumen ini dan di kategori default aplikasi.

- **BPJSTK JHT** — Jaminan Hari Tua di bawah lembaga jaminan sosial pemerintah Indonesia BPJS Ketenagakerjaan; tabungan pensiun wajib.
- **DPLK** — Dana Pensiun Lembaga Keuangan; dana pensiun sukarela yang ditawarkan lembaga keuangan.
- **PBB** — Pajak Bumi dan Bangunan; pajak properti tahunan di Indonesia.
- **SHM** — Sertifikat Hak Milik; sertifikat tanah hak milik penuh, bentuk kepemilikan tanah terkuat di Indonesia.
- **THR** — Tunjangan Hari Raya; bonus hari raya wajib yang dibayar oleh perusahaan Indonesia, biasanya sebesar satu bulan gaji sebelum Idul Fitri.
- **QRIS** — Quick Response Code Indonesian Standard; standar pembayaran QR terpadu di Indonesia.
- **Cicilan** — Pembayaran angsuran.
- **Kos** — Tempat sewa kamar kosan.
- **Mudik** — Perjalanan pulang kampung tahunan, terutama saat Idul Fitri.
- **Sawah** — Ladang padi.
- **Kondangan** — Menghadiri acara pernikahan atau perayaan, biasanya dengan amplop hadiah.
- **Sedekah / Zakat** — Sumbangan sukarela / amal wajib dalam Islam.
- **Saweria / Trakteer** — Platform tip kreator Indonesia yang umum digunakan untuk donasi personal.

---

*Akhir dari PRD v1.3 (ID). Langkah berikutnya: fase desain. Setelah desain, v1.4 akan mengintegrasi design concept dan penyesuaian requirement lebih lanjut.*
