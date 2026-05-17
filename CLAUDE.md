# Sakuwise — Strict Rules untuk Claude Code CLI

> **File ini auto-loaded oleh Claude Code CLI per session** (bila CLI version support project-root CLAUDE.md).
> Bila tidak auto-load: di session opening prompt, user akan paste **isi file ini** secara manual (Mode B per Brief v2.0 §6.2).
> File ini di-stage di `/202505 Sakuwise/coding/CLAUDE.md` dan akan di-copy ke `/Users/gustiadhitya/AndroidStudioProjects/Sakuwise/CLAUDE.md` saat M1 Task #13.

---

## 0. Project Context

**Sakuwise** = aplikasi Android **local-first** untuk personal money tracking, targeted Indonesian users. IDR-only, Bahasa Indonesia primary. Founder + first user: Gusti Adhitya (solo dev).

- **App ID:** `com.gustiadhitya.sakuwise`
- **Architecture:** Clean Architecture multi-module (18 Gradle modul: 1 app + 10 core + 7 feature; `build-logic` composite build terpisah). Tech Sol v1.1 sebut "~13" sebagai pendekatan kasar.
- **Tech stack:** Kotlin 2.0+, Jetpack Compose, Material 3, Room+SQLCipher, Hilt, ML Kit OCR, WorkManager
- **Min SDK:** 26 (Android 8.0). **Target SDK:** 35.

### Reference Docs (semua di `/Users/gustiadhitya/Library/CloudStorage/GoogleDrive-gustiadhitya8@gmail.com/My Drive/Software Development Project/202505 Sakuwise/`)

| Doc | Untuk apa |
|---|---|
| `Sakuwise PRD v1.4 (ID).md` | Requirement source of truth (15 modul fitur + cross-cutting) |
| `Sakuwise Technical Solution v1.1.md` | Architecture source of truth (module structure, deps, build, CI/CD) |
| `Sakuwise Coding Phase Brief v2.0.md` | Sub-milestone execution guide (36 sub-milestone, model matrix, workflow) |
| `Sakuwise Coding Restart Plan v1.0.md` | Strategic context — why we restart, lessons learned dari M1-M7 trial |
| `design/Sakuwise Design Concept v1.0.md` | Design system bible |
| `design/Sakuwise Handoff Spec.md` | Token mapping ke Compose (starting point untuk M2) |
| `design/Sakuwise Accessibility Audit.md` | 13 a11y items WAJIB di-implement |
| `design/Sakuwise Prototype.html` | Visual arbiter (interactive) |
| `design/Sakuwise Prototype - Standalone.html` | Bundled prototype, browser-runnable |
| `design/sakuwise-screens/light/{NN}-{name}.png` | Visual ground truth light (69 PNG) |
| `design/sakuwise-screens/dark/{NN}-{name}.png` | Visual ground truth dark (69 PNG) |
| `design/brand/*.jsx`, `design/proto/*.jsx` | Brand & component source untuk port ke Compose |

**Ambiguity resolution order:**
1. PRD v1.4 (requirement)
2. Tech Solution v1.1 (architecture)
3. Handoff Spec + PNG reference (visual)
4. Prototype HTML (visual arbiter saat kontradiksi)
5. Flag ke Gusti bila tetap tidak terjawab

---

## 1. CRITICAL RULES (Non-Negotiable)

### Rule 1 — NO Scope Creep
**Hanya implement task yang explicit listed di current sub-milestone spec** dari Brief v2.0 §12. JANGAN:
- Tambah fitur di luar scope sub-milestone.
- Refactor code yang tidak terkait task ini.
- "Improve" code yang sudah berfungsi.
- Decide unilaterally bila ada ide expansion.

Bila ada ide improvement → STOP dan flag ke user di `Issues / Decisions Needed` section saat gate report.

### Rule 2 — Visual Match Prototype, BUKAN Interpret
Output Compose UI harus **pixel-match** dengan PNG reference di `/202505 Sakuwise/design/sakuwise-screens/`. Bila ragu visual:
1. Read PNG file path yang di-list di sub-milestone scope.
2. Compare side-by-side via Paparazzi snapshot (PRIMARY gate, lihat Rule 7).
3. Jangan improvise atau "modernize" desain. Match exact.

### Rule 3 — Token Enforcement (No Hardcoded Styling)
Semua color, spacing, typography, animation, shape **WAJIB** pakai theme token:
- Color: `MaterialTheme.colorScheme.X` atau `SakuwiseTokens.current.X`
- Spacing: `SakuwiseSpacing.X` (xs, s, m, l, xl, xxl, xxxl)
- Typography: `MaterialTheme.typography.X` atau `SakuwiseTypography.X`
- Animation: `SakuwiseAnimation.X` (quick, default, medium, slow, splashFade)
- Shape: `SakuwiseShapes.X` atau `MaterialTheme.shapes.X`

**Tidak boleh:**
- Hex literal di composable code (`Color(0xFF0F4C3A)`). Kecuali di `Color.kt`, `Tokens.kt` definition itself.
- `dp` literal di composable code (`padding(16.dp)`). Kecuali di `Spacing.kt` definition itself.
- Hardcoded `sp` di Text. Pakai typography token.

### Rule 4 — NO Cross-Project Contamination
Project ini self-contained. **JANGAN:**
- Read atau reference files di luar `/Users/gustiadhitya/AndroidStudioProjects/Sakuwise/` (kecuali docs di `/202505 Sakuwise/`).
- Pakai pattern generic dari training data ("money tracker app" template, "expense app" snippet).
- Salin pattern dari project Android lain di filesystem.

Sakuwise punya unique spec — hanya pakai pattern dari companion docs + Sakuwise codebase sendiri.

### Rule 5 — Strict Clean Architecture
- ViewModel inject **UseCase only**, BUKAN Repository langsung.
- Setiap operasi data punya UseCase eksplisit (~70-80 UseCases total V1).
- Module dependency rules (Tech Sol v1.1 §3.2):
  - `:feature:*` TIDAK boleh depend ke `:core:data/database/datastore` langsung.
  - Akses via UseCase di `:core:domain` only.
  - `:core:designsystem` tidak depend ke `:core:domain` (presentation-only).
  - `:core:ui` boleh depend ke `:core:designsystem` + `:core:common`.
- Repository return `Flow<T>` untuk observe, `suspend` untuk write.

### Rule 6 — Indonesian Locale Wajib
- Currency format: pakai `RupiahFormatter` (di `:core:common`) dengan `Locale("id", "ID")`. Format: "Rp 1.500.000" (titik thousand separator).
- Date format: pakai `DateFormatter` dengan Locale id. Format "15 Mei 2026" (DD MMM YYYY Bahasa).
- Date relative: "Hari ini" / "Kemarin" / "{N} hari lalu" → fallback absolute.
- Number format: `NumberFormat.getInstance(Locale("id", "ID"))`.
- **TabularNums** untuk semua Rupiah amount: pakai `RupiahText` helper dengan `fontFeatureSettings = "tnum"`.

### Rule 7 — Visual Gate WAJIB Paparazzi (mulai M5+)
Setiap composable di `:core:designsystem` + key screens di `:feature:*` WAJIB:
1. Punya `@Preview` (light + dark + key variant).
2. Punya Paparazzi screenshot test.
3. Baseline snapshot committed di `{module}/src/test/snapshots/`.
4. Gate: `./gradlew verifyPaparazziDebug` pass.

**Visual gate workflow per composable:**
1. Write composable + @Preview.
2. `./gradlew :{module}:recordPaparazziDebug`.
3. Compare snapshot PNG vs reference PNG di `/sakuwise-screens/light/{NN}-{name}.png`.
4. Self-report deviation dengan **rubrik kategori** (lihat Rule 7.1).
5. Fix bila MINOR, escalate bila MODERATE/SEVERE.

### Rule 7.1 — Deviation Rubric (BUKAN Percentage)
**JANGAN report "match 87%"** (subjective). Pakai kategori konkret:
- **MATCH** — Paparazzi pass + (bila ada resemble.js) pixel diff <2%.
- **MINOR** — 1-3 specific deviation describable (mis. "padding kurang 4dp"). Fix langsung.
- **MODERATE** — 4-7 deviation atau wrong color/layout direction. Escalate Recovery §9 Level 2.
- **SEVERE** — >7 deviation atau structural wrong. STOP + Recovery §9 Level 3.

### Rule 8 — A11Y Items Wajib Per Modul
Sebelum claim composable done, cek PRD v1.4 §8.X.Aksesibilitas yang relevan untuk modul tersebut, dan implement items dari `Accessibility Audit.md` (A11Y-001 sampai A11Y-013).

Common items:
- A11Y-003/004/005: hit area expand 48dp.
- A11Y-006: tab bar `contentDescription` lengkap.
- A11Y-007: decorative element `invisibleToUser`.
- A11Y-009: pakai Material 3 `Switch` native.
- A11Y-010: form field `isError` + `supportingText`.
- A11Y-012: detect reduce motion, cross-fade fallback.
- A11Y-013: expand "rb/jt/M" → "ribu/juta/miliar" di contentDescription.

### Rule 9 — Time-Box Discipline per Sub-Milestone
- **Target:** ≤ 2.5 jam aktif CLI work.
- **2.5 jam mark:** self-assess scope remaining.
- **3 jam hard cap:** STOP regardless. Escalate ke user untuk split decision.

### Rule 10 — STOP di Sub-Milestone Gate
**Tidak boleh continue** ke sub-milestone berikutnya tanpa explicit user reply `"Lanjut M{N}{letter}"`. Output gate report (format §5.4 di Brief v2.0) dan wait.

---

## 2. CRITICAL ANTI-PATTERNS (Tidak Boleh Dilakukan)

❌ **Add Crashlytics SDK** — V1 = Play Console Vitals only (Tech Sol v1.1 §11).
❌ **Request INTERNET permission** di AndroidManifest — V1 = local-first, no network.
❌ **`dp` literal di composable** — pakai `SakuwiseSpacing.X`.
❌ **Hex literal di composable** — pakai `SakuwiseTokens` / `MaterialTheme.colorScheme`.
❌ **Skip @Preview** di composable — wajib light + dark minimal.
❌ **Skip Paparazzi snapshot test** untuk component baru (M5+) — wajib.
❌ **Direct commit ke `main`** — wajib feature branch + PR.
❌ **Refactor file di luar current sub-milestone scope** — flag improvement bila ada, jangan unilaterally do.
❌ **Auto-merge PR** — user yang merge, bukan CLI.
❌ **"Improve" desain** — match prototype exact, bukan reinterpret.

---

## 3. Visual Verification Workflow (Per Composable)

1. Write composable + @Preview functions (light + dark + variants).
2. Run `./gradlew :{module}:recordPaparazziDebug`.
3. Read snapshot PNG dari `{module}/src/test/snapshots/{path}.png`.
4. Read reference PNG dari `/202505 Sakuwise/design/sakuwise-screens/light/{NN}-{name}.png` (dan dark mirror).
5. Compare:
   - **PRIMARY:** Paparazzi numerical (auto-fail on diff dari baseline).
   - **SECONDARY:** Optional resemble.js / pixelmatch (threshold <2% pixel diff).
   - **TERTIARY:** CLI image perception (coarse mismatch detection only — tidak reliable untuk fine detail).
6. Self-report deviation dengan rubrik §1 Rule 7.1.
7. Action:
   - MATCH → lanjut task berikutnya.
   - MINOR → fix langsung, re-record snapshot, repeat.
   - MODERATE → 2 attempts max → Recovery §9 Level 2.
   - SEVERE → STOP → Recovery §9 Level 3.

### 3.1 Untuk Variant TIDAK Ada di 139 PNG
Bila composable implement variant tanpa PNG (mis. error state, loading skeleton, ripple frame):
1. Flag di Discovery block: `"Need PNG ref untuk: error-state form expense saat amount kosong"`.
2. User capture via prototype HTML → save ke `/202505 Sakuwise/design/sakuwise-screens/variants/{date}-{NN}-{screen}-{variant}.png`.
3. CLI lanjut visual gate dengan path tersebut.

Lihat Brief v2.0 §10 Manual Screenshot Fallback Workflow.

---

## 4. ESCALATION (When to STOP and Ask)

STOP eksekusi dan flag ke user dalam kasus berikut:

- **Library version incompatibility** yang break build dan tidak bisa di-resolve dengan version bump simple.
- **API surface change** di dependency baru yang impact arsitektur.
- **Pixel-perfect visual deviation** yang tidak bisa dicapai dengan token yang ada (mungkin Handoff Spec ada gap).
- **Architectural ambiguity** yang tidak ter-cover PRD v1.4 atau Tech Sol v1.1.
- **Test failures** yang tidak punya obvious fix dalam 2 attempt.
- **Security implication** yang berdampak ke threat model (encryption, key handling, backup format).
- **Multiple sub-milestone consecutive drift** (3+) — trigger Recovery §9 Level 4.

Untuk ambiguity kecil (implementation detail seperti `String` vs `Long` untuk ID), decide yourself dengan rationale di commit message.

---

## 5. OUTPUT FORMAT (Per Sub-Milestone Gate)

Setelah sub-milestone done, output ke user dengan format ini:

```markdown
## M{N}{letter} — {Nama} — DONE

### Summary
What's done: [concrete list, 3-7 items]
Files modified: [paths, max 20 items, group by directory bila banyak]

### Visual Gate Result
- Build: ✅ / ❌ (`./gradlew assembleDebug`)
- Tests: ✅ X passed / Y total (`./gradlew test`)
- Paparazzi: ✅ / ❌ (`./gradlew verifyPaparazziDebug`)
- PNG ref compare (per screen):
  - {screen-name} (light): MATCH | MINOR | MODERATE | SEVERE — deviations: [list bila tidak MATCH]
  - {screen-name} (dark): MATCH | MINOR | MODERATE | SEVERE — deviations: [...]

### PR
- Branch: feature/M{N}{letter}-{slug}
- PR URL: https://github.com/gustiadhitya/sakuwise-android/pull/{N}

### Test Scenario untuk User
1. [step 1 — concrete, actionable]
2. [step 2]
...

### Issues / Decisions Needed
[bila ada — list konkret pertanyaan untuk user]
[Skip section ini bila tidak ada]

### Time
- Estimated: X jam
- Actual: Y jam
- Variance: ±Z%

STOP. Menunggu konfirmasi user sebelum lanjut M{N+1}{letter}.
```

---

## 6. SESSION OPENING Discovery Block (WAJIB)

Setiap session baru, **sebelum mulai coding**, CLI WAJIB:

1. Read CLAUDE.md (file ini — bila Mode A auto-load, confirm content visible. Bila Mode B, user paste content).
2. Read PNG reference files yang in-scope untuk sub-milestone ini.
3. Read PRD v1.4 §{X relevant} dan Handoff Spec §{Y relevant}.
4. Summarize pemahaman dalam **5-8 kalimat** ke user, termasuk:
   - Output yang diharapkan dari sub-milestone
   - PNG yang akan jadi visual gate
   - Critical rules yang relevan untuk sub ini
   - Estimated effort
5. Tunggu user reply `"OK lanjut"` sebelum mulai coding.

Discovery block ini **bukan opsional** — terbukti reduce drift di M1-M7 trial saat CLI dipaksa summarize dulu.

---

## 7. Git Workflow per Sub-Milestone

```bash
# Start
git checkout main
git pull
git checkout -b feature/M{N}{letter}-{slug}
git tag pre-M{N}{letter}
git push --tags

# Execute tasks, commit per logical unit
git add .
git commit -m "feat(module): description"  # Conventional Commits

# End
git push -u origin feature/M{N}{letter}-{slug}
gh pr create --base main --head feature/M{N}{letter}-{slug} \
  --title "M{N}{letter}: {Nama}" \
  --body "Implements sub-milestone M{N}{letter} per Brief v2.0.

## What's done
[...]

## Visual gate
[...]

## Test scenario
[...]
"
```

User merge PR setelah verify. CLI **TIDAK auto-merge**.

---

## 8. Reference Quick Cards

### 8.1 Token Cheat
```kotlin
// Colors
MaterialTheme.colorScheme.primary       // forest green #0F4C3A light / mint #7BC4A4 dark
MaterialTheme.colorScheme.background    // cream #F5F1E8 light / dark #0F1411 dark
SakuwiseTokens.current.inkMuted         // body muted text
SakuwiseTokens.current.successSoft      // success bg soft

// Spacing
SakuwiseSpacing.xs   // 4 dp
SakuwiseSpacing.s    // 8 dp
SakuwiseSpacing.m    // 12 dp
SakuwiseSpacing.l    // 16 dp
SakuwiseSpacing.xl   // 20 dp
SakuwiseSpacing.xxl  // 24 dp
SakuwiseSpacing.xxxl // 32 dp

// Animation
SakuwiseAnimation.quick      // 120ms linear
SakuwiseAnimation.default    // 200ms
SakuwiseAnimation.medium     // 280ms cubic-bezier(.2,.7,.3,1)
SakuwiseAnimation.slow       // 400ms
SakuwiseAnimation.splashFade // 600ms

// Shapes
SakuwiseShapes.xs    // 4 dp
SakuwiseShapes.sm    // 8 dp
SakuwiseShapes.md    // 12 dp
SakuwiseShapes.lg    // 16 dp
SakuwiseShapes.xl    // 20 dp
```

### 8.2 Format Conventions
- Currency: `Rp 1.500.000` (spasi setelah Rp, titik thousand, koma decimal)
- Short: `Rp 1.5 jt` / `Rp 850rb` / `Rp 1.5 M`
- Date: `15 Mei 2026` (DD MMM YYYY Bahasa)
- Date relative: "Hari ini" / "Kemarin" / "3 hari lalu" → fallback absolute
- Percentage: `12.4%` (titik decimal)

### 8.3 Module Dependency Quick Reference (Tech Sol v1.1 §3.2)
```
:feature:*           → :core:designsystem, :core:ui, :core:domain, :core:common
                      (NOT :core:data/database/datastore directly)
:core:domain         → :core:model
:core:data           → :core:domain, :core:database, :core:datastore, :core:crypto
:core:database       → :core:model, :core:crypto (for SQLCipher passphrase)
:core:designsystem   → :core:common (for formatters)
:core:ui             → :core:designsystem, :core:common
```

### 8.4 Performance Budget
- Cold start <2 detik di Snapdragon 6-series, 4GB RAM
- Dashboard query <500ms dengan setahun riwayat
- Frame rate 60fps untuk semua animation
- APK debug <50MB, AAB release <30MB after R8

---

## 9. Final Reminders

1. **Read Brief v2.0 §12** untuk current sub-milestone scope sebelum coding.
2. **Discovery block** mandatory di session start (§6).
3. **Visual gate** mandatory sebelum claim done (§3).
4. **Time-box 2.5/3 jam** strict (§1 Rule 9).
5. **Output format** §5 per gate report.
6. **STOP** di gate. User yang decide lanjut atau tidak.

🔧 *Build twice as fast by going slow at the start.*
