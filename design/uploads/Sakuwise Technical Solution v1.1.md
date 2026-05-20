# Sakuwise — Technical Solution v1.1

**Versi:** 1.1 (Draft kedua — semua keputusan utama terkunci dari review v1.0)
**Tanggal:** 16 Mei 2026
**Penulis:** Gusti Adhitya + Claude
**Untuk:** Acuan arsitektur dan stack teknis untuk fase coding (Android Studio + Claude Code CLI)
**Companion dokumen:** Sakuwise PRD v1.3 (ID), Sakuwise Handoff Spec, Sakuwise Accessibility Audit, Sakuwise Prototype.html

> Dokumen ini adalah keputusan teknis konkret yang menerjemahkan PRD requirement
> dan Design v1.0 jadi instruksi engineering siap implementasi. Versi 1.1 ini
> meng-update v1.0 setelah review founder: **Clean Architecture multi-module**
> (bukan lagi Lean MVVM single-module) demi reusability template untuk client
> work di masa depan. App ID di-update ke `com.gustiadhitya.sakuwise`. Crash
> reporting cukup Play Console Android Vitals (no SDK). Self-critique ada di §21.

---

## Riwayat Revisi

| Versi | Tanggal | Penulis | Perubahan |
|---|---|---|---|
| 1.0 | 16 Mei 2026 | Gusti + Claude | Draft pertama. Lean MVVM single-module. App ID `com.sakuwise.app`. Crash reporting via local log. |
| 1.1 | 16 Mei 2026 | Gusti + Claude | Switch ke **Clean Architecture multi-module** (rasional: reusability framework untuk client work). Strict UseCase pattern. App ID → `com.gustiadhitya.sakuwise`. Crash reporting → Play Console Android Vitals (no SDK). 8 open items dari v1.0 resolved. |

---

## 0. Daftar Isi

1. [Ringkasan Eksekutif](#1-ringkasan-eksekutif)
2. [Konteks & Constraints](#2-konteks--constraints)
3. [Keputusan Arsitektur Utama](#3-keputusan-arsitektur-utama)
4. [Tech Stack Detail](#4-tech-stack-detail)
5. [Data Layer](#5-data-layer)
6. [Encryption Architecture](#6-encryption-architecture)
7. [UI Layer (Compose + Material 3)](#7-ui-layer-compose--material-3)
8. [Navigation Architecture](#8-navigation-architecture)
9. [State Management](#9-state-management)
10. [Background Work (WorkManager)](#10-background-work-workmanager)
11. [OCR Pipeline](#11-ocr-pipeline)
12. [Image Handling](#12-image-handling)
13. [Build System (Multi-Module + Convention Plugins)](#13-build-system-multi-module--convention-plugins)
14. [Testing Strategy](#14-testing-strategy)
15. [CI/CD](#15-cicd)
16. [Crash Reporting & Diagnostics](#16-crash-reporting--diagnostics)
17. [Release & Distribution](#17-release--distribution)
18. [Project Structure](#18-project-structure)
19. [Dependencies Lengkap](#19-dependencies-lengkap)
20. [Open Issues / To Be Confirmed](#20-open-issues--to-be-confirmed)
21. [Glossary](#21-glossary)
22. [Catatan Kritis Penulis (Self-Critique)](#22-catatan-kritis-penulis-self-critique)

---

## 1. Ringkasan Eksekutif

**Stack:** Kotlin 2.0+ · Jetpack Compose · Material 3 · Room + SQLCipher · Hilt · Coroutines + Flow · ML Kit Text Recognition · WorkManager · Coil · Timber. Build pakai Gradle Kotlin DSL dengan version catalog dan convention plugins lewat composite build. CI/CD GitHub Actions. Min SDK 26 (Android 8.0 Oreo), Target SDK 35.

**App ID:** `com.gustiadhitya.sakuwise` (personal namespace yang akan jadi root untuk multi-aplikasi di masa depan — strategi memungkinkan family-of-apps pattern).

**Arsitektur:** **Clean Architecture multi-module** (~13 Gradle module). Struktur: `:app` (entry point, navigation) + `:core:*` (8 modul shared: model, domain, data, database, datastore, crypto, designsystem, ui, common, testing) + `:feature:*` (5 modul fitur user-facing: onboarding, dashboard, plan, transaction, asset, settings, donation). Strict UseCase pattern — setiap operasi data punya UseCase eksplisit, walaupun trivial. Rasional: codebase ini dirancang jadi **template reusable** untuk client work future, jadi proper Clean Architecture sebagai foundation yang konsisten dari hari satu lebih bernilai daripada speed-to-first-screen.

**Database:** Single SQLite database, encrypted at-rest via SQLCipher 4.x, dibuka dengan Room. Key (DEK) random 256-bit AES, di-generate first launch, wrapped via Android Keystore. Daily unlock biometric/PIN.

**Backup format:** File `.sakuwise` = magic bytes + version + KDF params (Argon2id) + salt + nonce + AES-256-GCM ciphertext SQLite payload.

**Crash reporting:** **Google Play Console Android Vitals saja** — tidak ada SDK pihak ketiga di V1. Vitals built-in di Play Store memberikan crash & ANR reports dari semua user yang install via Play, plus deobfuscated stack traces bila R8 mapping di-upload. Sufficient untuk solo dev. Firebase Crashlytics defer ke V2 jika diperlukan.

**Distribution:** Google Play Store, free, no analytics SDK. Privacy policy hosted di GitHub Pages. App signing pakai Play App Signing. Distribusi AAB (Android App Bundle).

**Pixel-match guarantee:** Compose theme + token tables langsung dari Handoff Spec; component library mirror dari `proto/components.jsx`; Paparazzi screenshot regression test untuk lock visual.

---

## 2. Konteks & Constraints

**Konteks proyek.** Sakuwise adalah aplikasi personal money tracker Android yang di-spec lengkap di PRD v1.3 (ID) dan di-design lengkap di Design v1.0 (output claude.ai/design). Founder Gusti adalah first user dan solo developer.

**Tujuan strategis berdampak teknis:** Project ini sengaja diperlakukan sebagai **trial framework untuk client work future**. Kalau berhasil, Gusti akan kembangkan aplikasi lain (mantan-bos request) + cari klien dengan project lebih besar. Konsekuensi: codebase harus **layak di-fork sebagai template** — modular, separation jelas, mudah customize per-domain klien. Ini mendorong keputusan Clean Architecture multi-module di V1, walaupun untuk Sakuwise sendiri (skala dan kompleksitas-nya) Lean MVVM single-module akan cukup.

**Constraints utama:**

1. **Local-first.** Tidak ada permission `INTERNET` di manifest V1. Tidak ada SDK analytics. Tidak ada cloud sync. Backup file local dipindah manual oleh user.
2. **Encrypted at rest.** Database + foto + file backup semuanya terenkripsi. Defeat ADB / forensic.
3. **Pixel-perfect ke design.** Hasil koding harus sama persis dengan prototype HTML.
4. **Bahasa Indonesia primary, IDR-only.** Locale `id-ID`, format Rupiah dengan tabular nums.
5. **Solo developer + first-user-founder.** Tidak boleh over-engineering tanpa alasan jelas. Tapi reusability template diprioritaskan.
6. **Stabilitas dari hari satu.** Bug yang sampai user akhir = bad UX untuk fitur intim seperti money tracking.
7. **Framework future.** Codebase modular, mudah di-fork per modul untuk client.

**Yang TIDAK di-constrain:**
- Cloud backup (V2)
- iOS port (V2 atau V3)
- Multi-user (V2)
- Foreign currency (V2)
- Telemetry / crash reporting kompleks (V2 dengan opt-in jika perlu)

---

## 3. Keputusan Arsitektur Utama

### 3.1 Pola Arsitektur: Clean Architecture (Strict)

**Pilihan:** Clean Architecture dengan 3 layer eksplisit — Domain, Data, Presentation — dan UseCase wajib untuk setiap operasi data, termasuk yang trivial.

**Yang ditolak:**

- **Lean MVVM dengan UseCase pragmatic** (UseCase hanya untuk orkestrasi multi-repo). Alasan: lebih simpel, tapi kurang konsisten — beberapa operasi via UseCase, beberapa langsung repo. Untuk template yang akan di-fork ke domain berbeda, konsistensi pola membantu reusability. Plus, eksplisit UseCase membantu testing isolation (tiap UseCase bisa di-mock dari ViewModel).
- **MVI (Model-View-Intent) ketat** dengan reducer. Tidak fit dengan ergonomic Compose ViewModel + StateFlow pattern. MVI lebih cocok untuk app dengan side-effect kompleks paralel.

**Layer breakdown:**

**Domain layer.** Pure Kotlin, no Android dependency. Berisi:
- **Models** — `Account`, `Plan`, `PlanItem`, `Transaction`, `Debt`, `GoldAsset`, dst. Plain data classes.
- **Repository interfaces** — abstract contracts. Tidak ada implementation di sini.
- **UseCases** — satu kelas per operasi. Mis. `GetAccountByIdUseCase`, `UpsertAccountUseCase`, `ObserveAccountBalanceUseCase`, `ReconcileAccountUseCase`, `CreatePlanUseCase`, `AddTransactionUseCase`, `ApplyDebtPaymentUseCase`, dst. Estimasi total UseCase di V1: **70-80 kelas**.

**Data layer.** Implementasi repository, akses ke Room + SQLCipher + DataStore + Keystore. Memetakan antara Room entity dan domain model lewat extension functions. Tidak ada UI atau ViewModel reference.

**Presentation layer.** Compose UI + ViewModel + UI state classes. Inject UseCase via Hilt, tidak inject Repository langsung. Ini memaksa proper layer separation (Presentation tidak tahu cara data diambil, cuma tahu apa yang bisa dilakukan).

**UseCase pattern (template per file):**

```kotlin
class GetAccountByIdUseCase @Inject constructor(
  private val repository: AccountRepository,
) {
  operator fun invoke(id: String): Flow<Account?> = repository.observeById(id)
}

class UpsertAccountUseCase @Inject constructor(
  private val repository: AccountRepository,
) {
  suspend operator fun invoke(account: Account): Result<Unit> = runCatching {
    repository.upsert(account)
  }
}

class ReconcileAccountUseCase @Inject constructor(
  private val accountRepository: AccountRepository,
  private val transactionRepository: TransactionRepository,
  private val snapshotRepository: AccountSnapshotRepository,
) {
  suspend operator fun invoke(
    accountId: String,
    observedBalance: Long,
    note: String?,
  ): Result<Long> = runCatching {
    val computed = accountRepository.observeBalance(accountId).first()
    val diff = observedBalance - computed
    snapshotRepository.insert(
      AccountSnapshot(
        accountId = accountId,
        observedBalance = observedBalance,
        computedBalance = computed,
        diff = diff,
        note = note,
      )
    )
    if (diff != 0L) {
      transactionRepository.insert(
        Transaction.reconciliation(accountId, diff, note)
      )
    }
    diff
  }
}
```

**Naming convention:** `VerbObjectUseCase`. Verb dari `Get`, `Observe`, `Create`, `Update`, `Delete`, `Upsert`, `Apply`, `Compute`, `Reconcile`, `Restore`, `Backup`, dst. Result wrapper `Result<T>` untuk operasi yang bisa fail; raw `Flow<T>` untuk observation.

**Trade-off yang sengaja diterima:**
- Boilerplate ~30% lebih tinggi dari Lean MVVM
- File count membengkak (~70-80 UseCase classes)
- Setiap perubahan API repository = update 1 UseCase, walaupun beberapa hanya satu baris pass-through

**Manfaat yang dijual:**
- Test isolation: ViewModel test cuma mock UseCase, tidak perlu setup repository chain
- Konsistensi: pattern sama untuk operasi simple dan kompleks
- Reusability: UseCase bisa direuse antar ViewModel atau dipindah ke project lain dengan minimal modifikasi
- Documentation: setiap UseCase = unit operasi tertulis dengan single responsibility

### 3.2 Module Structure: Multi-Module Gradle

**Pilihan:** ~13 modul Gradle (1 `:app` + 8 `:core:*` + 5 `:feature:*`).

**Yang ditolak:**

- **Single module `:app`** — saya rekomendasi awal di v1.0. Lebih simpel untuk solo dev, tapi tidak match dengan tujuan "framework untuk client work". Multi-module memaksa boundary lewat Gradle dependency graph, bukan hanya konvensi package.
- **Hyper-modular** (split `:feature:asset` jadi `:feature:account`, `:feature:gold`, `:feature:land`, `:feature:deposit`, `:feature:debt` — total ~17 module). Marginal benefit untuk reusability (bila klien butuh "hanya tracking emas", ambil :feature:asset tetap reasonable), cost: 4 file Gradle ekstra + intermodule wiring lebih banyak.

**Daftar modul lengkap:**

```
:app                          (entry point, navigation host, DI bootstrap)
:core:common                  (utilities: dispatchers, extensions, lokal formatter Locale("id","ID"))
:core:model                   (plain Kotlin data classes — Account, Plan, etc.)
:core:domain                  (repository interfaces + cross-feature use cases)
:core:data                    (repository implementations + bridging Room entities <-> domain models)
:core:database                (Room database, DAOs, SQLCipher wiring, migration classes)
:core:datastore               (DataStore wrappers untuk preferences non-sensitive)
:core:crypto                  (DEK/KEK management, backup encrypt/decrypt service)
:core:designsystem            (theme tokens, SwButton, SwField, SwCard, semua SW_* component, RupiahText)
:core:ui                      (UI helpers di luar designsystem: state composables, navigation utils)
:core:testing                 (test fixtures, fakes — test-only library)
:feature:onboarding           (splash, onboarding 4-step)
:feature:dashboard            (DashboardScreen + ViewModel + UseCase orchestration)
:feature:plan                 (PlanScreen, CategoryCard, MonthPicker, ActionSheet)
:feature:transaction          (Expense/Income/Transfer forms + AddTxnPicker + OcrFlow)
:feature:asset                (combined: AccountsHub, AccountDetail, Gold, Land, Deposit, Debt — semua aset)
:feature:settings             (MeScreen + 15 sub-settings + Backup + Restore + Reconciliation)
:feature:donation             (DonateScreen — small enough untuk modul tersendiri)
build-logic/                  (Gradle composite build: convention plugins, see §13)
```

Total: **13 production module + 1 build-logic composite build**.

**Module dependency rules (di-enforce via Gradle):**

```
:app
 ├── depends on every :feature:*
 ├── depends on :core:designsystem (for theme bootstrap)
 ├── depends on :core:data (for DI installation)
 └── depends on :core:datastore, :core:crypto, :core:database (for DI)

:feature:* 
 ├── depends on :core:designsystem, :core:ui, :core:common, :core:model, :core:domain
 ├── DOES NOT depend on :core:data, :core:database (akses via UseCase only)
 └── DOES NOT depend on other :feature:*

:core:domain
 └── depends on :core:model only (pure Kotlin)

:core:data
 ├── depends on :core:domain (impl interfaces)
 ├── depends on :core:database, :core:datastore (data sources)
 └── depends on :core:crypto (for encrypted persistence)

:core:database, :core:datastore, :core:crypto
 └── depends on :core:common, :core:model (no domain — they are data sources)

:core:designsystem
 └── depends on :core:common only (leaf for UI tokens)

:core:ui
 └── depends on :core:designsystem, :core:common, :core:model
```

**Dependency anti-patterns yang di-enforce:**
- ❌ Feature tidak boleh depend ke feature lain (avoid coupling)
- ❌ :core:domain tidak boleh depend ke Android (paksa pure Kotlin)
- ❌ :feature:* tidak boleh depend ke :core:database / :core:data / :core:datastore langsung (paksa via UseCase)

Enforcement awal: code review (saya yang review saat pakai Claude Code CLI). Tahap V1.1: tambah custom Lint rule atau Detekt rule untuk auto-detect violation.

### 3.3 Dependency Injection: Hilt

**Pilihan:** Hilt (Google's DI on top of Dagger).

**Yang ditolak:** Koin (runtime DI, lighter weight tapi tidak ada compile-time validation), Manual factory pattern (untuk multi-module bikin boilerplate ekstrem).

**Cakupan Hilt di multi-module:**

- `@HiltAndroidApp` hanya di `:app`'s `SakuwiseApplication` (one entry point untuk seluruh app).
- `@AndroidEntryPoint` di `:app`'s `MainActivity` saja.
- `@HiltViewModel` di setiap ViewModel per `:feature:*` module.
- `@Module @InstallIn(SingletonComponent::class)` per layer/module untuk binding interfaces ke implementations:
  - `:core:data` → `DataModule` (binds `AccountRepository` ke `AccountRepositoryImpl`, etc.)
  - `:core:database` → `DatabaseModule` (provides `SakuwiseDatabase`, DAOs)
  - `:core:datastore` → `DataStoreModule` (provides `DataStore<Preferences>`)
  - `:core:crypto` → `CryptoModule` (provides `CryptoService`, `KeyManager`)
- `hiltViewModel()` di Compose Navigation per feature.

KSP digunakan untuk Hilt annotation processing (lebih cepat dari KAPT, support Kotlin 2.0+).

**Multi-module Hilt gotcha:** Setiap module yang punya `@Module` harus apply Hilt plugin. Convention plugin di build-logic akan automate ini.

### 3.4 Convention Plugins via Composite Build

**Pilihan:** Gradle composite build pattern dengan `build-logic/` direktori (NowInAndroid pattern).

**Yang ditolak:** `buildSrc/` (lebih tua, ada cache invalidation issues untuk multi-module).

Convention plugins shared antara semua modul untuk:
- Standard Android library config (compileSdk, minSdk, targetSdk, JDK target)
- Compose enablement
- Hilt enablement
- KSP setup
- Test dependencies bundle

Contoh penggunaan di per-module `build.gradle.kts`:

```kotlin
plugins {
  alias(libs.plugins.sakuwise.android.library)
  alias(libs.plugins.sakuwise.android.library.compose)
  alias(libs.plugins.sakuwise.hilt)
}

android {
  namespace = "com.gustiadhitya.sakuwise.feature.dashboard"
}

dependencies {
  implementation(projects.core.designsystem)
  implementation(projects.core.domain)
  implementation(projects.core.ui)
  implementation(projects.core.model)
}
```

Detail convention plugin sources di §13.

---

## 4. Tech Stack Detail

| Kategori | Pilihan | Versi (target) | Justifikasi singkat |
|---|---|---|---|
| Bahasa | Kotlin | 2.0.21 | Standard Android, support null-safety, coroutines, K2 compiler stable |
| Build tool | Gradle Kotlin DSL | 8.10 | Tipe-safe build config, version catalog support |
| Android Gradle Plugin | AGP | 8.6.x | Stable terbaru saat dokumen ditulis |
| Composite build | build-logic | — | NowInAndroid pattern untuk convention plugins multi-module |
| UI framework | Jetpack Compose | BoM 2025.10 | Imperatif → declarative, animasi mudah, mature di 2026 |
| Design System | Material 3 | 1.3.x | Match design system token convention |
| DI | Hilt | 2.52 | Compile-time validation, support ViewModel di Compose, multi-module mature |
| Annotation processing | KSP | 2.0.21-1.0.26 | Lebih cepat dari KAPT, support Kotlin 2.0 |
| Async | Coroutines + Flow | 1.9.0 | Standard idiomatic async di Kotlin |
| Database ORM | Room | 2.6.1 | Standard Android ORM, support coroutines + Flow |
| Encryption DB | SQLCipher Android | 4.6.1 (net.zetetic) | Standard SQLite encryption library |
| Security crypto | androidx.security | 1.1.0-alpha06 | EncryptedFile untuk Keystore wrap |
| Argon2 KDF | argon2kt | 1.5.0 | Pure-Kotlin Argon2id |
| Preferences | DataStore Preferences | 1.1.1 | Modern SharedPreferences replacement |
| Image loading | Coil | 2.7.0 | Compose-native, lightweight |
| OCR | ML Kit Text Recognition | 16.0.x | On-device, free |
| Background work | WorkManager | 2.10.0 | Standard scheduled tasks Android |
| Navigation | Compose Navigation | 2.8.x | Type-safe routes via Kotlin Serialization |
| Logging | Timber | 5.0.1 | Standard logging, replaceable tree |
| Testing — JUnit | JUnit 5 (Jupiter) | 5.11 | Modern test runner |
| Testing — mocks | MockK | 1.13 | Kotlin-idiomatic mocking |
| Testing — assertions | Kotest assertions | 5.9 | Readable assertion DSL |
| Testing — flow | Turbine | 1.2.0 | Flow testing dengan timeout dan assertion |
| Testing — DB | Room in-memory | (Room 2.6.1) | Built-in untuk repo tests |
| Testing — UI | Compose UI Test | (Compose BoM) | Standard Compose UI testing |
| Testing — screenshot | Paparazzi | 1.3.5 | CI-friendly screenshot regression tanpa emulator |
| Min SDK | 26 (Android 8.0 Oreo) | — | ~98% Indonesian device base |
| Target SDK | 35 (Android 15) | — | Required oleh Play Store 2026 |
| JDK | 17 | — | Required oleh AGP 8.x |
| Distribution | Android App Bundle (AAB) | — | Required oleh Play Store sejak 2021 |
| Crash reporting | Google Play Console Android Vitals | (built-in) | No SDK, no compliance burden, sufficient untuk solo dev |

Dependencies lengkap dengan TOML version catalog di §19.

---

## 5. Data Layer

### 5.1 Database — Room + SQLCipher di `:core:database`

Single SQLite database file `sakuwise.db`. Dibuka via Room dengan `SupportFactory` dari SQLCipher.

`:core:database` module berisi:
- `SakuwiseDatabase.kt` — Room database class dengan semua entity terdaftar
- `entity/*.kt` — Room `@Entity` classes (terisolasi dari domain models)
- `dao/*.kt` — DAO interfaces per entity
- `converter/*.kt` — TypeConverters (Date, BLOB, Enum)
- `migration/*.kt` — Migration classes (V1→V2, dst.)
- `di/DatabaseModule.kt` — Hilt module provides Database + DAOs

**Schema versioning policy:**
- V1.0.0 ship dengan database `version = 1`.
- Setiap perubahan schema break (kolom baru required, kolom dihapus, FK baru) → naikkan `version` dan tulis `Migration(from, to)`.
- `exportSchema = true` aktif, schema JSON di-commit ke `:core:database/schemas/`.
- **Tidak ada destructive migration** di production.

### 5.2 Repository Pattern — Interface di Domain, Implementation di Data

Setiap entity domain punya repository interface di `:core:domain/repository/` dan implementation di `:core:data/repository/`.

**Domain side (`:core:domain`):**

```kotlin
package com.gustiadhitya.sakuwise.core.domain.repository

interface AccountRepository {
  fun observeAll(): Flow<List<Account>>
  fun observeById(id: String): Flow<Account?>
  fun observeBalance(id: String): Flow<Long>
  suspend fun upsert(account: Account)
  suspend fun archive(id: String)
}
```

**Data side (`:core:data`):**

```kotlin
package com.gustiadhitya.sakuwise.core.data.repository

@Singleton
internal class AccountRepositoryImpl @Inject constructor(
  private val accountDao: AccountDao,
  private val transactionDao: TransactionDao,
) : AccountRepository {
  override fun observeAll(): Flow<List<Account>> =
    accountDao.observeAll().map { entities -> entities.map { it.toDomain() } }
  
  override fun observeBalance(id: String): Flow<Long> =
    combine(
      accountDao.observeById(id),
      transactionDao.observeByAccountId(id),
    ) { account, transactions ->
      computeBalance(account.initialBalance, transactions)
    }
  
  // ... other methods
}
```

**Binding di Hilt module (`:core:data/di/DataModule.kt`):**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
  // ... other bindings
}
```

`internal` keyword di `AccountRepositoryImpl` memastikan tidak bocor di luar `:core:data` — hanya di-akses via interface.

### 5.3 UseCase Layer — Per-Feature di Domain Sub-Packages

Karena strict Clean Architecture, **setiap operasi data** dibungkus UseCase. UseCase tinggal di `:core:domain/usecase/` dengan sub-package per area:

```
:core:domain/src/main/kotlin/com/gustiadhitya/sakuwise/core/domain/
├── model/
│   ├── Account.kt
│   ├── Plan.kt
│   ├── Transaction.kt
│   └── ...
├── repository/
│   ├── AccountRepository.kt
│   ├── PlanRepository.kt
│   └── ...
├── usecase/
│   ├── account/
│   │   ├── ObserveAllAccountsUseCase.kt
│   │   ├── ObserveAccountByIdUseCase.kt
│   │   ├── ObserveAccountBalanceUseCase.kt
│   │   ├── UpsertAccountUseCase.kt
│   │   ├── ArchiveAccountUseCase.kt
│   │   └── ReconcileAccountUseCase.kt
│   ├── plan/
│   │   ├── ObserveCurrentPlanUseCase.kt
│   │   ├── ObservePlanByPeriodUseCase.kt
│   │   ├── CreatePlanFromTemplateUseCase.kt
│   │   ├── CopyPlanFromPreviousMonthUseCase.kt
│   │   └── ...
│   ├── transaction/
│   │   ├── ObserveTransactionsByPlanUseCase.kt
│   │   ├── AddExpenseUseCase.kt
│   │   ├── AddIncomeUseCase.kt
│   │   ├── AddTransferUseCase.kt
│   │   ├── DeleteTransactionUseCase.kt
│   │   └── ...
│   ├── debt/
│   │   ├── ApplyDebtPaymentUseCase.kt
│   │   ├── MarkDebtSettledUseCase.kt
│   │   └── ...
│   ├── networth/
│   │   ├── ComputeNetWorthUseCase.kt
│   │   └── ComputeNetWorthTrendUseCase.kt
│   └── backup/
│       ├── CreateBackupUseCase.kt
│       └── RestoreFromBackupUseCase.kt
```

Estimasi: ~70-80 UseCase files. Konsekuensinya cukup besar di kompilasi awal — tapi tiap file ringan (10-30 baris).

ViewModel di `:feature:*` inject UseCase via Hilt, tidak inject Repository langsung. Ini paksa proper dependency: Presentation → Domain → Data.

### 5.4 Computed Values

Beberapa nilai tidak disimpan tapi di-compute via SQL view atau Flow combine. UseCase yang return `Flow<T>` untuk computed value:

- `ObserveAccountBalanceUseCase` → `Flow<Long>`
- `ObservePlanItemUsedAmountUseCase` → `Flow<Long>`
- `ComputeNetWorthUseCase` → `Flow<NetWorthBreakdown>` (combine multiple repository flows)
- `ObserveDebtOutstandingUseCase` → `Flow<Long>`

Implementasi pakai Room SQL aggregate query + Kotlin Flow combine.

### 5.5 DataStore Preferences di `:core:datastore`

Module terpisah karena preferences = different storage tech (file vs SQLite) dan beda enkripsi (DataStore plain text karena no PII):

- `theme_mode`: light / dark / system
- `language`: id / en
- `auto_lock_minutes`: 1/5/15/30/0
- `plan_period_start_day`: 1-28
- `default_allocation_percentages`: jsonString
- `gold_price_global`: long
- `last_backup_timestamp`: long
- `onboarding_completed`: boolean
- `user_nickname`: string

Repository interface `UserPreferencesRepository` di `:core:domain`, implementation di `:core:datastore`. UseCases wrap setiap field.

---

## 6. Encryption Architecture

(Tidak berubah dari v1.0. Section ini self-contained dan terpusat di `:core:crypto` module.)

### 6.1 Threat Model Recap

**Dilindungi dari:**
- Ekstraksi data lewat ADB (`adb backup`, `adb shell` dengan rooted device)
- Forensik device-level (Cellebrite, dll.) terhadap stored SQLite file
- Kebocoran via file backup yang ter-share tidak sengaja
- Phone curian dalam keadaan unlock-but-screen-off (mitigasi via auto-lock)

**TIDAK dilindungi dari:**
- OS Android yang ter-compromise (root malware, modified ROM)
- User yang sukarela bagi file backup + PIN-nya
- Screen-reading malware saat aplikasi sedang ter-unlock

### 6.2 Key Hierarchy

```
┌─────────────────────────────────────┐
│ User PIN/Passphrase (input)         │
│   ↓ Argon2id (memory=64MB, t=3)     │
│ Backup KEK (256-bit AES)            │
└─────────────────────────────────────┘
            │
            ├──── encrypt → .sakuwise file (AES-GCM)
            
┌─────────────────────────────────────┐
│ Android Keystore (hardware-backed)  │
│   wraps                             │
│ Daily KEK (256-bit AES, in Keystore)│
└─────────────────────────────────────┘
            │
            ├──── unwraps → 
            
┌─────────────────────────────────────┐
│ DEK (Data Encryption Key)           │
│   256-bit AES, random, generated    │
│   once at first launch              │
└─────────────────────────────────────┘
            │
            └──── opens → SQLCipher database
```

**DEK:** Stored as encrypted ByteArray inside `EncryptedFile` (androidx.security.crypto) at `filesDir/dek.bin`. Master key dari Android Keystore, hardware-backed bila tersedia.

**Daily unlock:** Biometric prompt unlocks Keystore master → EncryptedFile decrypts → DEK in memory → SQLCipher opens DB dengan DEK as passphrase. DEK di-zero di memory setelah DB close.

**Backup KEK:** Derived fresh dari user PIN via Argon2id setiap kali Backup/Restore. Never cached.

### 6.3 Argon2id Parameters

```kotlin
val argon2 = Argon2Kt()
val hash = argon2.hash(
  mode = Argon2Mode.ARGON2_ID,
  password = pin.toByteArray(),
  salt = salt,             // 16 bytes random per backup
  tCostInIterations = 3,
  mCostInKibibyte = 65_536, // 64 MB
  parallelism = 1,
  hashLengthInBytes = 32,   // 256-bit KEK
)
```

Pada mid-range Android (Snapdragon 6-series), parameter ini ~1 detik derivation. Brute force infeasible.

### 6.4 Backup File Format `.sakuwise`

Binary layout, big-endian:

```
Offset  Size  Field
0       4     Magic bytes: 0x53 0x4B 0x57 0x53  ("SKWS")
4       1     Format version (currently 0x01)
5       1     App schema version (currently 0x01)
6       2     Reserved (0x00 0x00)
8       16    Argon2id salt
24      12    AES-GCM nonce
36      4     Ciphertext length (uint32)
40      N     AES-GCM ciphertext (encrypted SQLite payload)
40+N    16    AES-GCM authentication tag
```

**Restore validation steps:**
1. Read magic bytes, verify match
2. Check format version compatible
3. Check schema version (run migration if older, refuse if newer)
4. Read salt + nonce
5. Prompt user for PIN, derive KEK
6. Decrypt + verify GCM auth tag. If invalid → "PIN salah atau file rusak."
7. Write decrypted SQLite to temp file, validate Room schema
8. Replace existing `sakuwise.db` atomically
9. Re-bind SQLCipher dengan new DEK

### 6.5 Memory Hygiene

- DEK ByteArray di-`fill(0)` sebelum out-of-scope
- PIN string di-clear setelah Argon2 derivation
- Backup KEK di-clear setelah crypto operation selesai

### 6.6 Keystore Alias Naming

Single Keystore master alias: `sakuwise_master_v1`. Versi di nama untuk forward-compatibility.

---

## 7. UI Layer (Compose + Material 3)

(Mostly unchanged dari v1.0. Theme dan komponen kini live di `:core:designsystem`.)

### 7.1 Theme di `:core:designsystem`

```kotlin
package com.gustiadhitya.sakuwise.core.designsystem.theme

@Composable
fun SakuwiseTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = SakuwiseTypography,
    shapes = SakuwiseShapes,
  ) {
    CompositionLocalProvider(
      LocalSakuwiseTokens provides if (darkTheme) DarkSakuwiseTokens else LightSakuwiseTokens,
      LocalRupiahFormatter provides RupiahFormatter(),
      content = content,
    )
  }
}
```

`LocalSakuwiseTokens` menyimpan token di luar Material 3 standard (`inkSubtle`, `borderStrong`, `successSoft`, dst.). Diakses lewat `SakuwiseTokens.current.inkMuted`.

### 7.2 Token → ColorScheme Mapping

Sesuai Handoff Spec §1 dengan adjustment a11y (A11Y-002: `inkSubtle` di dark → `#7A8480`).

### 7.3 Typography & Tabular Nums

Figtree dari `res/font/` (downloaded files, bukan runtime download untuk offline-first). RupiahText helper di `:core:ui` untuk enforce tabular nums:

```kotlin
@Composable
fun RupiahText(value: Long, style: TextStyle = ..., prefix: Boolean = true, modifier: Modifier = Modifier) {
  val formatted = LocalRupiahFormatter.current.format(value)
  Text(
    text = if (prefix) "Rp $formatted" else formatted,
    style = style.copy(fontFeatureSettings = "tnum"),
    modifier = modifier,
  )
}
```

### 7.4 Component Library di `:core:designsystem`

Port setiap `SW_*` composable dari prototype JSX ke Compose. Naming: `SwButton`, `SwField`, `SwCard`, `SwSheet`, `SwTabBar`, `SwBar`, `SwAmount`, `SwAccountIcon`, `SwCategoryDot`, `SwChip`, `SwToggle`, `SwSettingsRow`, `SwSnapshotChart`, `PinInput`.

Tiap komponen punya **Compose Preview** dengan light + dark variant + state variants. Preview di-render ke screenshot regression via Paparazzi.

### 7.5 Animation Implementation

Animation tokens (dari Handoff Spec §4) jadi `AnimationSpec` constants di `:core:designsystem/animation/`.

Reduced motion support (A11Y-012):

```kotlin
val LocalReduceMotion = compositionLocalOf { false }

@Composable
fun ReducedMotionProvider(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val reduce = remember {
    val scale = Settings.Global.getFloat(
      context.contentResolver,
      Settings.Global.ANIMATOR_DURATION_SCALE, 1f
    )
    scale == 0f
  }
  CompositionLocalProvider(LocalReduceMotion provides reduce, content = content)
}
```

---

## 8. Navigation Architecture

### 8.1 Per-Feature NavGraph Extensions

NowInAndroid pattern: setiap `:feature:*` module expose extension function untuk register routes-nya ke NavHost.

```kotlin
// :feature:onboarding
fun NavGraphBuilder.onboardingScreen(onComplete: () -> Unit) {
  composable<OnboardingRoute> {
    OnboardingScreen(onComplete = onComplete)
  }
}

@Serializable object OnboardingRoute

// :app
NavHost(navController, startDestination = SplashRoute) {
  splashScreen(onComplete = { /* nav to home or onboarding */ })
  onboardingScreen(onComplete = { navController.navigate(HomeRoute) })
  homeNavGraph(...)
  // ... other features
}
```

### 8.2 Type-Safe Routes

Compose Navigation 2.8+ dengan Kotlin Serialization:

```kotlin
@Serializable object SplashRoute
@Serializable object OnboardingRoute
@Serializable object HomeRoute
@Serializable data class AccountDetailRoute(val accountId: String)
@Serializable data class GoldDetailRoute(val goldId: String)
@Serializable data class TransactionDetailRoute(val transactionId: String)
// ... dst.
```

### 8.3 Bottom Tab Navigation

Bottom tab (Beranda, Plan, Aset, Saya) + center FAB pakai nested NavHost di `HomeRoute`. State preserved per tab via `rememberSaveable`.

### 8.4 Bottom Sheets — Composition-Level State

Sheets banyak (40+) dan most contextual ke layar parent. Di-handle via `remember { mutableStateOf<SheetState?>(null) }` + conditional `ModalBottomSheet`. **Tidak** terdaftar di NavGraph.

### 8.5 Transitions

Default: slide-in dari kanan + fade (mirror `sw-slide-in` di prototype). Reduced motion: fade 100ms only.

---

## 9. State Management

### 9.1 ViewModel + StateFlow per Screen

```kotlin
// :feature:dashboard
data class DashboardUiState(
  val greeting: String = "",
  val periodLabel: String = "",
  val daysLeft: Int = 0,
  // ... full state
  val isLoading: Boolean = true,
  val hideAmounts: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
  private val observeCurrentPlanUseCase: ObserveCurrentPlanUseCase,
  private val computeNetWorthUseCase: ComputeNetWorthUseCase,
  private val observeRecentTransactionsUseCase: ObserveRecentTransactionsUseCase,
  // ... only UseCases, no Repository
) : ViewModel() {
  
  private val _uiState = MutableStateFlow(DashboardUiState())
  val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
  
  private val _events = MutableSharedFlow<DashboardEvent>()
  val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()
  
  init {
    combine(
      observeCurrentPlanUseCase(),
      computeNetWorthUseCase(),
      observeRecentTransactionsUseCase(limit = 10),
    ) { plan, netWorth, recent -> 
      // assemble UiState
    }.launchIn(viewModelScope)
  }
  
  fun onToggleHideAmounts() {
    _uiState.update { it.copy(hideAmounts = !it.hideAmounts) }
  }
}
```

**ViewModel hanya inject UseCase** — enforce Clean layer separation.

### 9.2 Side Effects

`viewModelScope.launch` untuk write operations. Events untuk one-shot via `SharedFlow`.

### 9.3 Process Death & Configuration Change

`SavedStateHandle` untuk form draft + wizard step. ViewModel survive rotation via Hilt's `@HiltViewModel`.

---

## 10. Background Work (WorkManager)

### 10.1 Workers di `:app` Module

Single Worker class di V1: `RecurringPaymentReminderWorker`. Lives in `:app` (no separate `:work` module — overkill untuk 1 worker).

WorkManager pakai `HiltWorkerFactory` untuk dependency injection ke Worker class.

### 10.2 Reminder Scheduling

User enable reminder di plan item detail → ViewModel call `ScheduleRecurringReminderUseCase(planItemId, dayOfMonth)`. UseCase di-implement di `:core:domain/usecase/reminder/`, bridged ke WorkManager via `ReminderScheduler` interface (di `:core:domain`) + `ReminderSchedulerImpl` (di `:app`).

`PeriodicWorkRequest` dengan tag = plan item ID untuk cancellation.

### 10.3 Locked Device Handling

WorkManager-triggered notification: tidak bisa akses encrypted DB karena device kunci. Solusi: **pre-compute reminder content saat scheduling**. Simpan reminder metadata (plan item name, period label) di DataStore (plain, not sensitive). Worker fetch dari DataStore, bukan DB.

Konsekuensi: bila user ubah nama plan item, reminder masih tampil nama lama sampai re-schedule. Acceptable trade-off untuk V1.

### 10.4 Notification Permission (Android 13+)

`POST_NOTIFICATIONS` permission diminta pertama kali user enable reminder. Channel ID: `sakuwise_reminder`.

---

## 11. OCR Pipeline

(Unchanged dari v1.0.)

### 11.1 ML Kit Setup

`com.google.mlkit:text-recognition:16.0.0`. Latin recognizer (sufficient untuk Bahasa Indonesia).

### 11.2 Pipeline

```
Camera/Gallery/Share intent input
  ↓ Bitmap (rotated per EXIF)
  ↓ ML Kit Text Recognition
  ↓ Raw OCR output (Text + bounding boxes)
  ↓ IndonesianReceiptParser (di :feature:transaction)
  ↓ ReceiptDraft(merchant, date, totalAmount, confidence)
  ↓ UI: pre-fill expense form
  ↓ User confirm/correct
  ↓ Saved as Transaction with photo BLOB
```

### 11.3 IndonesianReceiptParser

Hand-written parser di `:feature:transaction/internal/`. Regex untuk total amount (`total`, `tunai`, `bayar`, `jumlah`), date (Indonesian formats), merchant (top text lines).

Confidence: High (total + date both keyword-matched), Medium (total only), Low (fallback).

### 11.4 Fallback

OCR gagal total → tampilkan struk image, user input manual. Photo tetap attached.

---

## 12. Image Handling

(Unchanged dari v1.0.)

Foto disimpan sebagai BLOB di Room. Compression: JPEG quality 70, max 1600px long edge, target ~200KB. Display via Coil di Compose dengan in-memory cache only (disk cache off untuk avoid plaintext path).

---

## 13. Build System (Multi-Module + Convention Plugins)

### 13.1 Composite Build Pattern

Root project includes `build-logic/` as composite build:

`settings.gradle.kts`:

```kotlin
includeBuild("build-logic")

include(":app")
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:crypto")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")
include(":feature:onboarding")
include(":feature:dashboard")
include(":feature:plan")
include(":feature:transaction")
include(":feature:asset")
include(":feature:settings")
include(":feature:donation")
```

### 13.2 Convention Plugins di `build-logic/convention/`

Convention plugins handle shared Gradle setup. Plugin classes (Kotlin):

```kotlin
// build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt
class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.apply("com.android.library")
    pluginManager.apply("org.jetbrains.kotlin.android")
    
    extensions.configure<LibraryExtension> {
      compileSdk = 35
      defaultConfig {
        minSdk = 26
      }
      compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
      }
    }
    
    extensions.configure<KotlinAndroidProjectExtension> {
      compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
      }
    }
  }
}
```

Plugin classes terregister di `build-logic/convention/build.gradle.kts`:

```kotlin
gradlePlugin {
  plugins {
    register("androidLibrary") {
      id = "sakuwise.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "sakuwise.android.library.compose"
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("hilt") {
      id = "sakuwise.hilt"
      implementationClass = "HiltConventionPlugin"
    }
    // ... etc
  }
}
```

Convention plugin set lengkap:
- `sakuwise.android.application` — untuk `:app`
- `sakuwise.android.library` — untuk semua `:core:*` dan `:feature:*` Android library
- `sakuwise.android.library.compose` — tambahkan Compose support
- `sakuwise.hilt` — Hilt annotation processing
- `sakuwise.jvm.library` — untuk `:core:model` dan `:core:domain` (pure Kotlin, no Android deps)
- `sakuwise.android.feature` — meta-plugin untuk feature modules (auto-apply library + compose + hilt + nav)

### 13.3 Per-Module `build.gradle.kts` (Singkat)

Contoh `:feature:dashboard/build.gradle.kts`:

```kotlin
plugins {
  alias(libs.plugins.sakuwise.android.feature)
}

android {
  namespace = "com.gustiadhitya.sakuwise.feature.dashboard"
}

dependencies {
  implementation(projects.core.designsystem)
  implementation(projects.core.ui)
  implementation(projects.core.domain)
  implementation(projects.core.model)
  
  testImplementation(projects.core.testing)
}
```

Sangat singkat berkat convention plugin. Tidak ada repetisi `compileSdk`, `minSdk`, Compose enablement, dst.

### 13.4 R8 / ProGuard Rules

Rules per module (kalau diperlukan) di `consumer-rules.pro`. Aggregated di `:app` saat release build.

Rules kritis:

```
# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }

# Argon2kt
-keep class com.lambdapioneer.argon2kt.** { *; }

# Hilt (mostly handled by AGP)
-keep class * extends androidx.lifecycle.ViewModel
```

### 13.5 Build Variants

V1: `debug` + `release`. App ID di-debug suffix: `com.gustiadhitya.sakuwise.debug` (allow install side-by-side dengan release).

### 13.6 Estimated Build Times

- Clean build (all modules): ~2-3 menit di laptop modern
- Incremental rebuild (single module changed): ~10-20 detik
- Test only one module: ~5-10 detik

Multi-module benefit terlihat di incremental build dengan ubah satu `:feature:*`.

---

## 14. Testing Strategy

### 14.1 Unit Tests Per Module

Setiap `:core:*` dan `:feature:*` module punya `src/test/` untuk unit tests.

**Cakupan:**
- `:core:domain` — semua UseCase (paling penting untuk dengan strict Clean)
- `:core:data` — repository implementations (pakai Room in-memory)
- `:core:crypto` — encrypt/decrypt round-trip
- `:core:common` — format helpers (RupiahFormatter, DateFormatter dengan Locale id)
- `:feature:transaction` — IndonesianReceiptParser
- `:feature:*` — ViewModels (mock UseCases via MockK)

Framework: JUnit 5 + Kotest assertions + MockK + Turbine (untuk Flow testing).

Target coverage: 70% di `:core:domain`. Tidak ada coverage gate di CI yang gagalin build — pragmatic only.

### 14.2 `:core:testing` Module

Test-only library yang share-able antara semua module:
- `FakeAccountRepository`, `FakePlanRepository`, dst. (fake implementations untuk ViewModel test tanpa Hilt setup)
- Test data builders (`createTestAccount(...)`)
- Test dispatcher rule
- Compose UI test helpers

### 14.3 UI Tests (Compose)

Per `:feature:*`, key flows:
- Onboarding 1→4 → home
- Backup setup PIN flow
- Expense form validation
- Reconciliation flow

Framework: `androidx.compose.ui.test:ui-test-junit4`. Run di emulator. Tidak run di CI (terlalu lambat untuk free tier).

### 14.4 Screenshot Regression (Paparazzi)

`:core:designsystem` punya Paparazzi test untuk setiap component dengan light + dark + variant states.

Key screen di `:feature:*` (Dashboard, Plan, Aset hub, semua form) punya Paparazzi test juga.

Run di CI on PR. Snapshot mismatch → fail.

### 14.5 Manual Test Checklist

Sebelum release ke Internal track:
- TalkBack pass-through semua screen (Bahasa Indonesia)
- Font scaling 200% (cek overflow)
- Reduce motion (cek animasi)
- Backup encrypt → restore di device beda
- Onboarding skip optional → cek defaults
- Plan period start day diubah ke 25 → cek label

---

## 15. CI/CD

### 15.1 GitHub Actions Workflow

`.github/workflows/build.yml`:

```yaml
name: Build & Test

on:
  pull_request:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v3
      
      - name: Build debug
        run: ./gradlew assembleDebug
      
      - name: Unit tests (all modules)
        run: ./gradlew testDebugUnitTest
      
      - name: Screenshot tests
        run: ./gradlew verifyPaparazziDebug
      
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: '**/build/reports/'
```

Multi-module benefit: Gradle akan parallelize task execution across modules saat `--parallel` aktif.

### 15.2 Branch Strategy

`main` — release-ready. `feature/M{N}-{name}` per milestone. Direct PR feature → main (solo dev, self-review discipline).

### 15.3 Release Workflow

Manual via Android Studio build → Play Console upload → Internal track. Signing keystore lokal, tidak commit.

---

## 16. Crash Reporting & Diagnostics

### 16.1 Strategi V1: Google Play Console Android Vitals

Untuk V1 Sakuwise pakai **hanya** Android Vitals built-in di Play Console. Tidak ada SDK pihak ketiga di V1.

**Cara kerja:**
- Saat aplikasi crash atau ANR, Android OS (di device yang punya Play Store) automatic catat dan kirim ke Google.
- Stack trace tersedia di Play Console → Quality → Android Vitals.
- Bila R8 mapping file di-upload (manual via UI atau auto via Play Developer API Gradle plugin), stack trace di-deobfuscate jadi class/method name aslinya.
- Aggregated metrics: crash rate %, affected users, affected versions, top crash signatures.

**Cakupan & limitasi:**
- ✅ Crashes + ANRs dari Play Store install (production, beta, internal track)
- ✅ Deobfuscated stack trace (with mapping upload)
- ✅ Time-series per version
- ❌ Crashes dari sideload debug build tidak ter-capture
- ❌ Tidak ada breadcrumb / user action trace (cuma stack trace + thread state)
- ❌ Tidak ada custom keys / tags
- ❌ Tidak real-time (delay 1-24 jam ke dashboard)

**Untuk solo dev + first user + beta tester ~10 orang, cakupan ini sufficient.** Sebagian besar crash kritis akan muncul di Android Vitals dalam 24 jam, cukup waktu untuk hotfix.

### 16.2 Mapping File Upload (R8)

Setiap release build, R8 generate `mapping.txt`. Upload ke Play Console:

**Option A — Manual** (V1 default): Upload via Play Console UI saat upload AAB. Cukup untuk solo dev.

**Option B — Automated via Play Publisher Gradle plugin**: `com.github.triplet.play` plugin auto-upload mapping saat release. Setup ~1 jam. Worth kalau release frequency tinggi.

V1 pilih Option A — simple, no extra Gradle config.

### 16.3 Local Crash Log (Optional, V1.1)

Bila Android Vitals tidak memberikan info cukup untuk crash tertentu (mis. butuh tahu apa yang user lakukan sebelum crash), tambahan local crash log file di V1.1:

- `Thread.setDefaultUncaughtExceptionHandler` catat exception ke file `cache/crashes.log` (encrypted dengan Keystore-wrapped key)
- Settings → Diagnostik → tombol "Bagikan log error" → share intent (user kirim manual via WA/email ke developer)
- Log retention: 30 hari, auto-purge

Tidak diimplementasi di V1.0 — tambah hanya kalau Android Vitals terbukti tidak cukup.

### 16.4 V2 Path (Bila Diperlukan)

Bila app scale ke 100+ users dan Android Vitals + manual log share tidak cukup:
- **Self-hosted Sentry** di server Indonesia (Biznet/Cloudkilat) dengan explicit user opt-in dialog. Compliant UU PDP karena data tidak cross-border.
- **Firebase Crashlytics** dengan explicit opt-in. Compliance: butuh privacy policy update + consent flow.

V1 tidak ada implementasi maupun stub untuk ini — clean lean V1.

### 16.5 Privacy Implications Android Vitals

Penting untuk privacy policy: **Android Vitals adalah Google Play Services-level**, bukan SDK kita. User sudah consent ke Play TOS saat install via Play Store. Kita sebagai data controller tidak collect, tidak process, tidak share. Google yang process — kita hanya read aggregated reports dari Console.

Privacy policy Sakuwise tetap akurat klaim "tidak ada data collected oleh Sakuwise" — Android Vitals tidak melanggar klaim ini.

---

## 17. Release & Distribution

### 17.1 App Signing

- Generate upload keystore: `keytool -genkey -v -keystore sakuwise-upload.jks -keyalg RSA -keysize 2048 -validity 25000 -alias upload`
- Disimpan **lokal**, **never** committed. Backup terpisah ke USB / password manager.
- Play App Signing aktif → Google handle signing key final.
- Bila keystore hilang: Play Console support reset via identity verification.

### 17.2 Versioning

Semantic versioning: `MAJOR.MINOR.PATCH`. `versionCode = MAJOR * 10000 + MINOR * 100 + PATCH`.

V1 first launch: `versionName = "1.0.0"`, `versionCode = 10000`.

### 17.3 Release Tracks

- **Internal testing** — first upload tiap version. Tester: Gusti only.
- **Closed alpha** — 5-10 teman invite (setelah V1 dogfood 1 bulan)
- **Open beta** — public beta (setelah closed alpha clear P1)
- **Production** — V1.0 stable

Estimasi: Internal → Beta ~6 minggu, Beta → Prod ~4 minggu.

### 17.4 Privacy Policy

Hosted di GitHub Pages dari public repo `sakuwise-web`. URL akhir: `https://gustiadhitya.github.io/sakuwise-web/privacy/`.

Isi:
- Data dikumpulkan: tidak ada (Sakuwise tidak collect)
- Network: tidak ada (no INTERNET permission)
- Permission: kamera (opsional), notifikasi (opsional), biometric (opsional)
- Data sharing: tidak ada
- Cookies / analytics: tidak ada
- Note Android Vitals: Play Services-level, di luar control Sakuwise
- Contact: email Gusti

### 17.5 Play Store Listing

- App icon (512×512 PNG)
- Feature graphic (1024×500 PNG)
- Screenshots: 2-8 per device type
- Short description (80 char): "Pelacak uang lokal terenkripsi untuk pengguna Indonesia."
- Full description (4000 char): elaborasi fitur + privacy promise

---

## 18. Project Structure

```
sakuwise-android/                              # repo root, private GitHub
├── .github/
│   └── workflows/
│       └── build.yml
├── .gitignore
├── README.md
├── build.gradle.kts                            # root, plugins block only
├── settings.gradle.kts                         # includes all modules
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml                      # version catalog
│   └── wrapper/
├── build-logic/                                # composite build
│   ├── settings.gradle.kts
│   └── convention/
│       ├── build.gradle.kts
│       └── src/main/kotlin/
│           ├── AndroidApplicationConventionPlugin.kt
│           ├── AndroidLibraryConventionPlugin.kt
│           ├── AndroidLibraryComposeConventionPlugin.kt
│           ├── AndroidFeatureConventionPlugin.kt
│           ├── JvmLibraryConventionPlugin.kt
│           └── HiltConventionPlugin.kt
├── app/                                         # entry point
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml                  # NO internet permission
│       ├── kotlin/com/gustiadhitya/sakuwise/
│       │   ├── SakuwiseApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── navigation/
│       │   │   └── SakuwiseNavGraph.kt
│       │   ├── work/
│       │   │   ├── RecurringPaymentReminderWorker.kt
│       │   │   └── ReminderSchedulerImpl.kt
│       │   └── di/
│       │       └── AppModule.kt
│       └── res/
│           ├── values/strings.xml               # English fallback
│           ├── values-id/strings.xml            # Bahasa Indonesia primary
│           ├── mipmap-anydpi-v26/               # adaptive icon
│           └── xml/
│               ├── backup_rules.xml             # NO Auto Backup
│               └── data_extraction_rules.xml
├── core/
│   ├── common/                                  # utilities
│   ├── model/                                   # pure Kotlin data classes
│   ├── domain/                                  # interfaces + UseCases
│   ├── data/                                    # repository implementations
│   ├── database/                                # Room + SQLCipher
│   ├── datastore/                               # DataStore preferences
│   ├── crypto/                                  # encryption services
│   ├── designsystem/                            # theme + SW components
│   ├── ui/                                      # shared UI helpers
│   └── testing/                                 # test-only fixtures
├── feature/
│   ├── onboarding/
│   ├── dashboard/
│   ├── plan/
│   ├── transaction/                             # forms + OCR
│   ├── asset/                                   # accounts + gold + land + deposit + debt
│   ├── settings/                                # 15 sub-screens + backup + reconciliation
│   └── donation/
└── docs/                                        # in-repo doc summaries
    └── README.md                                # link to GDrive full docs
```

**Module internal structure pattern (sama untuk semua):**

```
:feature:dashboard/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   └── kotlin/com/gustiadhitya/sakuwise/feature/dashboard/
    │       ├── DashboardScreen.kt              # Composable
    │       ├── DashboardViewModel.kt
    │       ├── DashboardUiState.kt
    │       ├── DashboardEvent.kt
    │       ├── navigation/
    │       │   └── DashboardNavigation.kt      # NavGraphBuilder extension
    │       ├── internal/                       # private to feature, not exposed
    │       │   ├── DashboardGreeting.kt        # sub-composable
    │       │   ├── DashboardHero.kt
    │       │   ├── DashboardAllocation.kt
    │       │   └── ...
    │       └── di/
    │           └── DashboardModule.kt          # only if needed
    └── test/
        └── kotlin/com/gustiadhitya/sakuwise/feature/dashboard/
            └── DashboardViewModelTest.kt
```

---

## 19. Dependencies Lengkap

`gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.6.1"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.26"
hilt = "2.52"
hilt-navigation-compose = "1.2.0"
hilt-work = "1.2.0"
room = "2.6.1"
sqlcipher = "4.6.1"
security-crypto = "1.1.0-alpha06"
argon2kt = "1.5.0"
compose-bom = "2025.10.00"
nav-compose = "2.8.4"
coroutines = "1.9.0"
work = "2.10.0"
mlkit-text = "16.0.0"
coil = "2.7.0"
datastore = "1.1.1"
timber = "5.0.1"
serialization = "1.7.3"
junit5 = "5.11.0"
mockk = "1.13.13"
kotest = "5.9.1"
turbine = "1.2.0"
paparazzi = "1.3.5"
core-ktx = "1.15.0"
lifecycle = "2.8.7"
activity-compose = "1.9.3"
biometric = "1.2.0-alpha05"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
paparazzi = { id = "app.cash.paparazzi", version.ref = "paparazzi" }

# Convention plugins (defined in build-logic/)
sakuwise-android-application = { id = "sakuwise.android.application", version = "unspecified" }
sakuwise-android-library = { id = "sakuwise.android.library", version = "unspecified" }
sakuwise-android-library-compose = { id = "sakuwise.android.library.compose", version = "unspecified" }
sakuwise-android-feature = { id = "sakuwise.android.feature", version = "unspecified" }
sakuwise-jvm-library = { id = "sakuwise.jvm.library", version = "unspecified" }
sakuwise-hilt = { id = "sakuwise.hilt", version = "unspecified" }

[libraries]
# AndroidX core
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
androidx-biometric = { module = "androidx.biometric:biometric", version.ref = "biometric" }

# Compose
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }

# Navigation
androidx-nav-compose = { module = "androidx.navigation:navigation-compose", version.ref = "nav-compose" }
kotlin-serialization = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-nav-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
androidx-hilt-work = { module = "androidx.hilt:hilt-work", version.ref = "hilt-work" }

# Coroutines
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

# Room
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-room-testing = { module = "androidx.room:room-testing", version.ref = "room" }

# SQLCipher + Security
sqlcipher-android = { module = "net.zetetic:sqlcipher-android", version.ref = "sqlcipher" }
androidx-security-crypto = { module = "androidx.security:security-crypto", version.ref = "security-crypto" }
argon2kt = { module = "com.lambdapioneer.argon2kt:argon2kt", version.ref = "argon2kt" }

# DataStore
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# WorkManager
androidx-work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }

# ML Kit
mlkit-text-recognition = { module = "com.google.mlkit:text-recognition", version.ref = "mlkit-text" }

# Coil
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }

# Timber
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }

# Testing
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit5" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
kotest-assertions = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Compose testing
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }

# Gradle plugins (for use in convention plugins)
android-gradle-plugin = { module = "com.android.tools.build:gradle", version.ref = "agp" }
kotlin-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
ksp-gradle-plugin = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }

[bundles]
compose = [
  "androidx-compose-ui",
  "androidx-compose-ui-graphics",
  "androidx-compose-ui-tooling-preview",
  "androidx-compose-material3",
]
room = [
  "androidx-room-runtime",
  "androidx-room-ktx",
]
testing-unit = [
  "junit-jupiter",
  "mockk",
  "kotest-assertions",
  "kotlinx-coroutines-test",
  "turbine",
]
```

---

## 20. Open Issues / To Be Confirmed

Semua 8 item dari v1.0 sudah resolved per review founder. Yang masih perlu confirm/decide saat coding mulai:

1. **JDK 17 installed?** Wajib untuk AGP 8.x. Verifikasi: `java --version` di terminal.
2. **Android Studio version?** Latest stable (Ladybug/Meerkat or newer). Required untuk Kotlin 2.0 + Compose Compiler.
3. **Claude Code CLI installed?** `claude --version`. Untuk fase coding.
4. **GitHub account & private repo create.** Repo name `sakuwise-android` (per konfirmasi).
5. **GitHub Pages repo create.** Repo name `sakuwise-web` (public). Akan host privacy policy.
6. **Mapping file upload strategy.** V1 = manual via Play Console UI. Confirm OK atau auto via Gradle plugin.
7. **Local crash log V1.1 trigger.** Kapan kita decide tambah local log? Setelah Android Vitals ternyata gak cukup. Saat ini tidak diimplementasi di V1.0.
8. **Lint rule untuk dependency graph enforcement.** Currently manual review. V1.1 add custom Detekt/Lint rule? Worth ditelaah saat codebase grow.

Hal-hal yang akan terjawab saat coding (tidak block PRD/Tech Solution finalize):

- Exact mapping per layar → Composable name (akan didokumentasikan di per-module README saat coding)
- WorkManager test strategy (uji reminder fire timing tanpa benar-benar nunggu sebulan — pakai WorkManager test helpers)
- ProGuard rules tuning per release build cycle

---

## 21. Glossary

- **AGP:** Android Gradle Plugin
- **AAB:** Android App Bundle (replacement APK untuk Play Store)
- **AES-GCM:** Advanced Encryption Standard, Galois/Counter Mode (authenticated encryption)
- **Android Vitals:** Built-in crash/ANR reporting di Play Console
- **Argon2id:** Memory-hard key derivation function, OWASP-recommended
- **BoM:** Bill of Materials (dependency version bundle)
- **DEK:** Data Encryption Key (encrypts the database directly)
- **KDF:** Key Derivation Function
- **KEK:** Key Encryption Key (encrypts the DEK; user-derived untuk backup)
- **KSP:** Kotlin Symbol Processing (replacement KAPT)
- **MVVM:** Model-View-ViewModel
- **MLKit:** Google's on-device ML SDK
- **NowInAndroid:** Google's sample Android app yang demonstrate modern best practices
- **ProGuard / R8:** Android code shrinker + obfuscator
- **Room:** Google's SQLite ORM
- **SQLCipher:** Open-source SQLite encryption library
- **StateFlow:** Hot Coroutines flow untuk state observation
- **TalkBack:** Android screen reader
- **UseCase:** Single unit of business operation di Clean Architecture
- **WorkManager:** Jetpack library untuk deferred background work
- **UU PDP:** Undang-Undang Perlindungan Data Pribadi (UU 27/2022) Indonesia

---

## 22. Catatan Kritis Penulis (Self-Critique)

Bagian ini saya tulis jujur tentang keputusan-keputusan v1.1 yang masih bisa diperdebatkan.

### 22.1 Clean Architecture + Multi-Module: Locked-In Trade-Off

Saya v1.0 rekomendasi Lean MVVM. Setelah review, kamu pilih Clean + multi-module dengan rasional "trial untuk framework client work future". Saya hormati pilihan ini dan v1.1 patuhi sepenuhnya.

**Yang saya khawatirkan jujur:**

- **Time-to-first-screen** mungkin lebih lama 2-3 minggu daripada single-module. Setup convention plugins + 13 modul Gradle + Hilt multi-module wiring = ~1 minggu pure setup time sebelum tulis fitur pertama. Bila kamu kehilangan momentum (atau prioritas lain muncul) saat fase setup ini, project bisa stall.
- **Strict UseCase = 70-80 file class** untuk operasi yang sebagian besarnya trivial. Saat coding fase awal akan terasa "kenapa banyak banget file". Saya pegang: ini cost reusability — kalau project sukses dan kamu fork untuk client, UseCase yang sudah dipisah jadi big win.

**Mitigasi yang sudah saya bake:**
- Modul `:core:testing` untuk test infrastructure shared
- Convention plugins kurangi build.gradle.kts boilerplate per modul
- UseCase template singkat (mostly 10-20 LOC each)
- Per-feature `:feature:asset` digabung untuk mengurangi modul count

**Bila perlu downgrade later:** Refactor multi-module → single-module bisa dilakukan dengan effort moderate (~3-5 hari). Refactor single → multi jauh lebih mahal (sebulan). Jadi go-big-now adalah salah satu arah yang lebih aman.

### 22.2 Locked Device + WorkManager Reminder

Saya solve dengan pre-compute reminder content ke DataStore. Tapi konsekuensinya: bila user rename plan item ("Listrik" → "Listrik Rumah Jakarta"), reminder masih tampil nama lama sampai user open app + re-sync (next time scheduling occurs).

**Solusi alternative yang saya tolak:**
- Decrypt DB di Worker → user harus authenticate setiap reminder fire. Intolerable UX.
- Cache plan item name yang lebih sophisticated dengan invalidation. Over-engineering V1.

**Yang user perlu tahu:** Reminder content "frozen" di nama plan item saat reminder di-enable. Edit nama plan item tidak langsung update reminder text. Acceptable trade-off untuk V1.

### 22.3 Android Vitals — Cukup?

Saya rekomendasi Android Vitals saja, defer Crashlytics. Risk: bila ada crash silent yang tidak ter-detect, kamu (sebagai founder + first user) mungkin tidak tahu sampai user lapor langsung.

**Worst case:** Bug di non-critical path (mis. share PDF report) crash silently, user kira "fitur belum ada", uninstall. Android Vitals capture, tapi ratingnya already turun.

**Mitigasi:**
- Beta track panjang sebelum production (capture crash di tester base dulu)
- Monitor Android Vitals dashboard mingguan
- Logcat profiling per release sebelum upload (manual self-testing)

Bila trend ratusan ribu users tahun pertama (unlikely Y1), tambah Crashlytics dengan opt-in di V1.1 atau V2.

### 22.4 BLOB vs File untuk Foto — Reaffirmed

Sama dengan v1.0. Sengaja accept BLOB-bloat trade-off untuk simplicity backup atomic. Bila ke depan user banyak attach foto (>100/tahun), tambah fitur "kompres/hapus foto lama" di Settings → Storage.

### 22.5 Convention Plugins via Composite Build — Worth It?

NowInAndroid pattern adopt-saya. Hampir semua tutorial Android multi-module modern pakai ini. Pro: drastis kurangi per-module build.gradle.kts repetition. Con: tambahan composite build = tambahan layer kompleksitas Gradle yang harus dipelajari.

**Alternative yang saya tolak:** `buildSrc/`. Older pattern, ada cache invalidation issues, deprecated arah secara komunitas.

**Bila ribet:** Bisa skip composite build di V1 setup, pakai inline `plugins { ... }` block panjang di setiap modul. Refactor ke composite build kemudian. Saya prefer set right from start karena migrasi lebih mahal.

### 22.6 App ID Personal Namespace

`com.gustiadhitya.sakuwise` — strategis untuk family-of-apps (future apps under `com.gustiadhitya.*`). Tapi kalau di masa depan kamu ingin spin-off Sakuwise jadi entity terpisah (PT atau brand baru), app ID tidak bisa diubah setelah upload pertama ke Play.

**Mitigasi:** Bila Sakuwise sukses jadi standalone brand, daftar app baru dengan brand-specific ID (`com.sakuwise.app`) sebagai "Sakuwise 2.0" atau "Sakuwise Pro". App ID lama tetap ada di Play untuk legacy user.

### 22.7 Tidak Spec'd di V1 — Sengaja

Untuk transparansi, hal-hal yang saya skip walaupun pertimbangkan:

- **Detekt + ktlint static analysis.** Bagus untuk maintain code quality consistent. Tambah saat codebase mature (V1.1).
- **Compose stability metrics + Compose compiler reports.** Untuk optimize recomposition. V1.1 saat ada perf issue actual.
- **Baseline profile generation.** Untuk startup speed. Worth saat Android 9+ supaya cold start cepat. V1.1.
- **Macrobenchmark.** Performance regression testing. V2 saat scale.

Semua ini valid additions tapi tidak block V1 ship.

### 22.8 Yang Saya Masih Bisa Keliru

- **Versi exact library**: Mei 2026 saat dokumen ditulis. Bila implementasi mulai 1-2 bulan kemudian, refresh ke latest stable. Don't hardcode versi di dokumen.
- **Convention plugin class structure**: NowInAndroid pattern berubah-ubah versi ke versi. Verifikasi exact pattern saat setup actual.
- **Kotlin Serialization untuk type-safe Compose Navigation**: API ini relatively new (Compose Nav 2.8). Worth-check sample setup dari Google docs saat implement.
- **Hilt multi-module**: ada beberapa "best practice" yang masih evolving di komunitas. Saat ada konflik antara doc ini dan AOSP NowInAndroid current state, ikuti AOSP.

---

*Akhir Tech Solution v1.1. Locked dan siap jadi acuan PRD v1.4 + fase coding.*

🔧 **Clean Architecture multi-module — proper foundation untuk Sakuwise V1 dan template framework future.**
