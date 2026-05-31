# Sakuwise v1.1 — Handoff ke Cowork: 3 Security Items Deferred dari v1.0.5

**Dibuat:** 2026-05-31 · **Dari:** Claude Code (eksekusi v1.0.5) · **Untuk:** Cowork (planning v1.1)
**Sifat:** Context document — bukan task execution. Cowork perlu baca ini sebelum planning v1.1.

---

## Latar Belakang

v1.0.5 adalah security & verification pass untuk Sakuwise. Semua P0/P1 sudah selesai. Tiga item di bawah ini di-defer ke v1.1 karena ketiganya butuh keputusan desain / UX dari founder, bukan sekadar kode patch. Tanpa keputusan itu, implementasinya bisa keliru arah.

**Threat model Sakuwise:** app pencatatan finansial lokal — bukan payment gateway. Yang dijaga = kerahasiaan + integritas + tidak kehilangan data. PIN dan biometrik adalah kunci akses, bukan kunci transaksi.

---

## Item 1 — B9: Biometric Enrollment Invalidation

### Masalah
Saat ini `LockScreen.kt` memanggil `BiometricPrompt.authenticate()` tanpa `CryptoObject`. Artinya:

- Jika user **menambahkan fingerprint baru** ke device (Settings → Biometrics), fingerprint baru itu **langsung bisa membuka Sakuwise** tanpa konfirmasi apapun.
- Sakuwise tidak punya mekanisme untuk mendeteksi bahwa enrollment biometrik berubah.

Implikasi konkret: seseorang yang punya akses fisik ke HP + bisa masuk ke Settings device (misal: tau PIN device), bisa daftarkan fingerprintnya sendiri lalu buka Sakuwise.

### Mengapa di-defer
Fix yang benar butuh dua hal yang keduanya menyentuh UX:
1. **Keystore key dengan `setInvalidatedByBiometricEnrollment(true)`** — saat enrollment berubah, key di-invalidate. Biometric auth gagal.
2. **UX untuk "biometric expired"** — app perlu tunjukkan layar/dialog yang bilang: "Kunci biometrik tidak valid (enrollment berubah). Aktifkan lagi dengan PIN." User masuk via PIN, lalu boleh re-enable biometric.

Tanpa UX yang didesain, ini bisa terasa buggy atau mengejutkan user.

### Yang perlu diputuskan Founder/Design sebelum Cowork mulai
- Teks pesan "biometric expired" dalam Bahasa Indonesia dan English
- Apakah re-enable biometric langsung dari layar error, atau harus masuk Settings dulu?
- Apakah ada visual indicator di Settings bahwa biometric perlu di-aktifkan ulang?

### File yang akan disentuh
```
feature/lock/LockScreen.kt          ← promptBiometric(), canUseBiometric()
feature/lock/LockViewModel.kt       ← tambah isBiometricKeyValid()
core/crypto/BiometricKeyManager.kt  ← file BARU: Keystore key khusus biometrik
feature/settings/sub/PinSettingsScreen.kt ← UI re-enable biometric
```

### Referensi teknis untuk Cowork
- `KeyGenParameterSpec.Builder.setInvalidatedByBiometricEnrollment(true)`
- `BiometricPrompt.authenticate(CryptoObject(cipher), ...)` — perlu wrap Cipher dengan Keystore key
- Catch `KeyPermanentlyInvalidatedException` → set flag `biometricKeyInvalidated = true` di DataStore → tampilkan prompt re-enable

---

## Item 2 — A7: Upgrade Alpha Dependencies

### Masalah
Dua dependency di-pin ke versi **alpha** karena stable-nya belum ready saat milestone itu dikerjakan:

| Library | Versi sekarang | Status |
|---------|---------------|--------|
| `androidx.biometric:biometric-ktx` | `1.2.0-alpha05` | Alpha — API belum final |
| `androidx.security:security-crypto` | `1.1.0-alpha06` | Alpha — digunakan untuk EncryptedFile (dek.bin, pin.bin) |

**Biometric** alpha dibutuhkan karena `biometric-ktx` (Kotlin extension) baru ada di 1.2.x. Versi stable 1.1.0 ada tapi tanpa `biometric-ktx` extensions — bisa fallback ke Java API.

**Security-crypto** alpha digunakan karena `EncryptedFile` di 1.0.0 (stable) ada known issue dengan beberapa device. 1.1.0-alpha06 lebih stabil di practice.

### Mengapa di-defer
Ini **monitoring task**, bukan bug fix. Tidak ada CVE di versi yang dipakai. Yang diperlukan adalah:
1. Cek apakah stable release sudah keluar saat v1.1 dimulai
2. Kalau ada, upgrade + regression test `PinStore`, `KeyManager`, `AutoBackupPinStorage`, dan lock screen biometric

### Yang perlu dilakukan Cowork di v1.1
```kotlin
// gradle/libs.versions.toml
biometric = "1.2.0-alpha05"    // → cek androidx.biometric terbaru
securityCrypto = "1.1.0-alpha06" // → cek androidx.security:security-crypto terbaru
```

Setelah upgrade:
- Test `PinStore.setPin()` + `verifyPin()` (EncryptedFile wraps pin.bin)
- Test `KeyManager.getOrCreateDek()` (EncryptedFile wraps dek.bin)
- Test backup create + restore flow end-to-end
- Test lock screen biometric prompt

### File yang akan disentuh
```
gradle/libs.versions.toml   ← bump versi
```
Jika ada breaking API: `core/crypto/PinStore.kt`, `KeyManager.kt`, `feature/lock/LockScreen.kt`

---

## Item 3 — A3: Restore Fully Atomic

### Masalah
`BackupService.restore()` melakukan file swap dalam urutan ini:

```
1. database.close()
2. keyManager.installDek(dek)      ← DEK baru ditulis
3. tmp.writeBytes(dbBytes)          ← DB baru ditulis ke temp file
4. dbFile.delete()                  ← DB lama DIHAPUS
5. tmp.renameTo(dbFile)             ← rename (atomic di same filesystem)
   └── fallback: tmp.copyTo(dbFile) ← TIDAK atomic
```

Jika step 5 gagal (cross-partition, no space, permission error), kondisinya:
- DEK sudah diganti (step 2) — tapi DB lama sudah terhapus (step 4)
- App tidak punya DB yang valid
- User harus restore ulang dari awal

Ini di-handle sekarang dengan melempar `BackupRestoreException` yang memberi pesan jelas ke user. Tapi idealnya DB lama dipertahankan dulu sampai swap berhasil.

### Mengapa di-defer
Fix yang benar = **two-phase commit**:
1. Tulis `sakuwise.db.new` (DB baru)
2. Tulis `dek.bin.new` (DEK baru)
3. Rename keduanya secara atomic (atau dalam satu operasi yang bisa di-rollback)
4. Hapus file lama

Masalahnya: `rename()` di Android tidak bisa rename dua file sekaligus secara atomic. Solusi yang lebih robust adalah **backup dulu file lama** sebelum dihapus:

```kotlin
// Pattern yang lebih aman:
val dbBackup = File(dbFile.parent, "sakuwise.db.bak")
dbFile.renameTo(dbBackup)   // simpan lama sebagai .bak
tmp.renameTo(dbFile)         // pasang baru
dbBackup.delete()            // baru hapus .bak
// Jika rename(tmp→db) gagal → rename(bak→db) untuk rollback
```

Ini butuh perubahan yang cukup besar di `BackupService.restore()` dan perlu ditest dengan edge cases (no space, interrupted mid-rename, dll.). Lebih tepat untuk sprint dedicated.

### Yang perlu diputuskan sebelum Cowork mulai
- Apakah mau implement full two-phase commit, atau cukup "backup .bak sebelum delete"?
- Kalau restore gagal dan ada `.bak`, apakah app auto-rollback ke `.bak` atau tunjukkan prompt ke user?

### File yang akan disentuh
```
core/crypto/BackupService.kt       ← restore() method, baris 102–134
core/crypto/BackupCrypto.kt        ← BackupRestoreException sudah ada
feature/settings/sub/BackupScreen.kt ← error handling UI
```

### Test yang perlu ditambah
- Simulate step-5 failure → assert DB lama tetap intact (rollback berhasil)
- Simulate no-disk-space during copyTo → assert app masih bisa buka DB lama

---

## Checklist untuk Cowork sebelum mulai v1.1

- [ ] **B9**: Konfirmasi teks "biometric expired" dari founder (ID + EN)
- [ ] **B9**: Konfirmasi flow re-enable: from error screen atau from Settings?
- [ ] **A7**: Cek versi terbaru `androidx.biometric` dan `androidx.security:security-crypto` di maven
- [ ] **A3**: Konfirmasi strategi: two-phase commit vs. bak-then-delete vs. acceptable-risk-with-clear-error (sudah ada sekarang)

---

## Context tambahan yang Cowork perlu tahu

- **Tidak ada FLAG_SECURE** di app ini — sengaja di-descope agar user bisa screenshot. Jangan menambahkan.
- **Recents masking** sudah di-handle via `_backgrounded` flag di `AppLockController` (v1.0.5). Tidak perlu disentuh.
- **AGP harus tetap 8.13.2** — jangan upgrade ke 9.x, Hilt 2.56 belum compatible (lihat memory `feedback_agp_hilt_compat.md`).
- **Build dari worktree** — jangan build dari main project root kalau ada perubahan yang belum di-pull.
- **Repo:** `github.com/gustiadhitya8/sakuwise-android` (public). Jangan commit data real atau PIN ke repo.

---

*Sumber kebenaran: `SECURITY_AUDIT_v1.0.5.md` (temuan + severity lengkap) → `PLAN_v1.0.5.md` → kode aktual di repo.*
