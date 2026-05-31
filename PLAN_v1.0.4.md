# PLAN v1.0.4 — Hardening & Kesiapan User Eksternal

> Status eksekusi (mode auto). Sumber kebenaran: handoff `Sakuwise v1.0.4 Handoff to Claude Code.md`.
> Aturan emas: (1) tidak ada fitur baru, (2) local-first dipertahankan, (3) data user suci, (4) blast radius minimal.
> Build dari worktree `determined-tharp-7e8ff7`. Test HANYA di `emulator-5554`. JANGAN sentuh Samsung `RRCY705XMMD`.

## Fase 0 — Verifikasi (SELESAI)
Lihat tabel temuan di chat. Ringkas: Item 2 & 8 sudah beres di kode; Item 1,3,4,5,6,7 perlu kerja.

## Fase 2 — Implementasi (urutan eksekusi)

### Item 1 — Room migration framework 🔴 BLOCKER  [TDD]
- **File:** `DatabaseModule.kt`, baru `core/database/Migrations.kt`, `app/build.gradle.kts` (+room-testing), `app/schemas/.../5.json` (commit), androidTest `MigrationTest.kt`.
- **Pendekatan:**
  1. Hapus `.fallbackToDestructiveMigration()`. Pasang `.addMigrations(*SakuwiseMigrations.ALL)`.
  2. `SakuwiseMigrations.ALL = emptyArray()` (belum ada migrasi karena schema tetap v5) + dokumentasi cara nambah Migration(5,6).
  3. **Baseline existing DB:** DB v1.0.3 sudah v5; v1.0.4 tetap v5 → upgrade = no-op schema. Aman, tidak wipe.
  4. Test: `MigrationTestHelper` membuka schema 5 (validateMigration baseline) + scaffold test 5→6 (di-`@Ignore` sampai ada migrasi nyata, supaya harness terbukti jalan).
  5. room-testing dep + `room.schemaLocation` sudah ada → arahkan `assets.srcDirs += schemas` agar test baca JSON.
- **AC:** no fallbackToDestructiveMigration; 5.json commit; migration test lulus; upgrade v1.0.3→v1.0.4 dgn data tidak hilang (uji emulator).
- **Risiko:** salah baseline → crash/wipe. Mitigasi: schema tidak berubah, test + real upgrade.

### Item 2 — Backup format versioning  [TDD]
- **Kode sudah versioned.** Yang perlu: test restore lintas-versi + doc.
- **File:** test `BackupServiceRoundTripTest.kt` (unit, Robolectric atau pure), `docs/BACKUP_FORMAT.md`.
- **AC:** version marker (ada: FORMAT_VERSION/PAYLOAD_VERSION); backup v1.0.3 restore di v1.0.4 (uji); test jalur lama→baru; doc cara nambah versi.

### Item 8 — Export↔import round-trip  [TDD] (kode SELESAI commit a096fd8)
- **File:** test `ExportImportRoundTripTest.kt`.
- **AC:** export→import lossless; uji ID & EN header; doc skema kolom (di CHANGELOG/doc).

### Item 7 — Audit i18n menyeluruh  [BUG]
- **File:** sisir semua `feature/**` + `core/ui`, `core/designsystem` untuk `Text("...")` / label hardcoded. Fokus lapor: chart Aset, menu import.
- **Pendekatan:** grep programatik string literal Indonesia → pindah ke `values/strings.xml` + `values-en/strings.xml`. Aktifkan lint `HardcodedText`.
- **AC:** lint HardcodedText bersih/baseline; key ID+EN lengkap; uji manual EN = nol teks Indonesia; checklist layar.

### Item 3 — Android Vitals + bersih telemetry
- **File:** `gradle/libs.versions.toml` (hapus entri firebase/gms/crashlytics mati), doc rilis.
- **AC:** tidak ada SDK telemetry (verifikasi); audit catch yang menelan crash; catat monitoring=Android Vitals.

### Item 5 — Detekt + ktlint + CI
- **File:** `build.gradle.kts` (plugin detekt+ktlint), `config/detekt/detekt.yml`, baseline, `.github/workflows/ci.yml`.
- **AC:** `./gradlew detekt` & ktlint jalan + baseline; CI build+unitTest+migrationTest+detekt hijau.

### Item 4 — Baseline profile
- **File:** modul `:baselineprofile`, plugin `androidx.baselineprofile`, `settings.gradle.kts`, generate profil di emulator.
- **AC:** baseline-prof.txt terbundel; build hijau. (angka benchmark = bonus)

### Item 6 — QA empty/error states
- **Pendekatan:** wipe-data run di emulator, telusuri tiap tab kosong (akun/transaksi/aset/plan kosong), import file rusak, restore file salah. Perbaiki crash/kebingungan.
- **AC:** checklist layar; nol crash saat data kosong.

## Fase 3 — Review
- `security-review` skill pada perubahan crypto/migration/backup.
- `code-review` skill sebagai gate sebelum AAB.

## Fase 4 — Rilis
- versionCode 4→5, versionName→1.0.4. CHANGELOG.md. release-notes id-ID+en-US. exportAab. smoke test emulator. git tag v1.0.4. Lapor ringkasan + sisa risiko ke Gusti SEBELUM upload Play / install Samsung.

## Definition of Done (gate)
Lihat handoff §5 — checklist 14 butir.
