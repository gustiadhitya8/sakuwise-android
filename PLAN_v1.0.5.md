# PLAN v1.0.5 — Security & Verification Pass

**Created:** 2026-05-31  
**Theme:** Audit-driven fixes — no new features  
**versionCode:** TBD (confirm Play Console live value, then +1)  
**versionName:** 1.0.5

---

## Phase 0 — Audit ✅ DONE → SECURITY_AUDIT_v1.0.5.md

---

## Phase 1 — Fixes (ordered by priority)

### P1-A — Quick hygiene (< 30 min total)

**Step 1: Fix data_extraction_rules.xml (A2)**
- File: `app/src/main/res/xml/data_extraction_rules.xml`
- Add explicit `<exclude>` for: `database/sakuwise.db`, `file/dek.bin`, `file/pin.bin`, `sharedpref/sakuwise_autobackup_pin.xml`
- File: `app/src/main/res/xml/backup_rules.xml` — add same excludes for < API 31 devices
- AC: no sensitive files reach Google's Auto Backup on any API level

**Step 2: Fix .gitignore (handoff §1.5)**
- Add: `*.sakuwise`, `*.csv`, `*.xlsx`, `test-scratch/`, `restore-dumps/`, `*.log`
- Keep: `app/schemas/` gitignore is WRONG — schema JSONs are needed for MigrationTest. Remove `app/schemas/` from .gitignore (or add a `!app/schemas/` negation), and commit the 5 existing schema JSON files.

**Step 3: Notification VISIBILITY_PRIVATE (B2)**
- File: `RecurringPaymentReminderWorker.kt`
- Add `.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)` to the builder
- Add a neutral public version: `.setPublicVersion(NotificationCompat.Builder(ctx, CHANNEL_ID).setSmallIcon(...).setContentTitle("Sakuwise").setContentText("Pengingat").build())`

### P1-B — Transfer atomicity (A5)

**Step 4: Wrap AddTransferUseCase fee path in @Transaction**
- File: `app/src/main/java/.../core/database/dao/TransactionDao.kt`
- Add a new `@Transaction` DAO method `upsertTransferWithFee(transfer: TransactionEntity, fee: TransactionEntity?)` that inserts both rows atomically
- Update `AddTransferUseCase` to call the new DAO method

### P1-C — Recents masking (B5)

**Step 5: Add _backgrounded flag to AppLockController**
- File: `AppLockController.kt`
- Add `private val _backgrounded = MutableStateFlow(false)` 
- Set `true` in `onAppBackgrounded()`, set `false` in `onAppForegrounded()` after the lock check
- Expose as `val backgrounded: StateFlow<Boolean>`

**Step 6: Show privacy overlay when backgrounded**
- File: `AppNavGraph.kt`
- In `SakuwiseApp`, collect `lockController.backgrounded`
- Show a solid `sw.bg` Box overlay whenever `backgrounded == true` OR `showLock == true`
- This ensures the Recents thumbnail always shows the privacy overlay, independent of auto-lock timer
- The PIN-check flow remains: if the timer has NOT expired, dismiss the overlay without PIN on foreground

### P1-D — Tests (A1, A4)

**Step 7: Add seeded migration test (A1)**
- File: `MigrationTest.kt`
- Add `migrate_v4_to_v5_preservesData()`: use `helper.createDatabase(testDb, 4)`, insert synthetic rows into `asset_gold` (with `weightGram` column — schema 4), run migrations to 5, assert rows survive with `weightMilliGram = weightGram * 1000`.
- Also test downgrade attempt: verify Room throws (not silently wipes) on version mismatch.

**Step 8: Add backup tamper/corrupt tests (A4)**
- File: new `BackupCryptoTest.kt` (unit test, synthetic fixture)
- Test 1: encrypt dummy payload with PIN "123456", flip one byte in ciphertext, decrypt → `BadPinException`
- Test 2: truncate file to 39 bytes (below minimum) → `IllegalArgumentException`
- Test 3: wrong PIN → `BadPinException`
- Test 4: valid decrypt round-trip ✓

### P1-E — Restore error handling (A3/A4)

**Step 9: Improve restore atomicity logging**
- File: `BackupService.kt`
- Wrap the file-swap sequence in a try/catch; on failure throw `BackupRestoreException("File swap failed — original DB deleted. Restore the backup again.")` so the UI can show an actionable error
- This doesn't change the fundamental atomicity limit (filesystem-level) but surfaces the failure clearly instead of silently

---

## Phase 2 — Review

- Run `./gradlew detekt ktlintCheck` → fix violations
- Run `./gradlew testDebugUnitTest` (unit tests)
- Run instrumented migration test on emulator-5554
- Run `security-review` skill on the final diff
- Run `code-review` on the final diff

---

## Phase 3 — Release

1. **Confirm versionCode** from Play Console (founder action) → use that + 1
2. Set `versionCode = <confirmed>` and `versionName = "1.0.5"` in `app/build.gradle.kts`
3. Update `CHANGELOG.md` — entry for v1.0.5 with neutral description (no CVE details)
4. Update `app/src/main/play/release-notes/id-ID/default.txt` and `en-US/default.txt`
5. Build release AAB from worktree: `./gradlew :app:bundleRelease`
6. Smoke test on emulator-5554
7. Git tag `v1.0.5`
8. Report findings to founder, await Samsung confirmation before installing to production device

---

## Items flagged to founder (§7 per handoff)

1. **versionCode confirmation**: Cannot safely determine from code — must check Play Console. If the previously held/rolled-back v1.0.5 upload had versionCode 6, we need 7.
2. **B9 biometric invalidation**: Implementing correct invalidation on new enrollment requires a Keystore key migration and UX for "biometric key expired, please re-enable". This is a v1.1 item — scheduling with design confirmation needed.
3. **A2 data already sent to Google?** If any device running v1.0.3/v1.0.4 had Android 12+ and Auto Backup enabled, encrypted snapshots of `dek.bin`, `pin.bin`, and `sakuwise.db` may be in Google's servers. Data is encrypted with Keystore-bound keys (cannot be decrypted without the hardware-backed key on the original device) — risk is very low but founder should be aware.
4. **A3/A4 restore atomicity**: The file-swap during restore is not fully atomic. In the rare event of failure mid-swap, the user would need to re-restore from backup. The Sakuwise backup file is the recovery path — this is already documented but worth surfacing.

---

## P2 deferred (not in v1.0.5 scope)

| Item | Decision |
|------|----------|
| B9 Biometric invalidation | v1.1 — needs design + UX for key re-enrollment prompt |
| A7 Alpha deps upgrade | Monitor; upgrade when biometric 1.2.0-stable and security-crypto 1.1.0-stable ship |
| A5 Unit tests (math edge cases) | v1.0.5 bonus — add if time allows after P1 fixes |
