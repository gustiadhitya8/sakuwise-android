package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupService — orchestrates `.sakuwise` create / restore.
 *
 * **Payload v1 layout (inside the AES-GCM-encrypted body):**
 * ```
 *   4B  payload version (BE u32 = 1)
 *   4B  DEK length (BE u32; must be 32 for AES-256)
 *   N   DEK bytes
 *   M   SQLCipher-encrypted sakuwise.db bytes
 * ```
 *
 * **Payload v2 layout** (adds settings blob so gold prices / plan config
 * survive a restore without reverting to app defaults):
 * ```
 *   4B  payload version (BE u32 = 2)
 *   4B  DEK length (BE u32)
 *   N   DEK bytes
 *   4B  settings JSON length (BE u32; 0 if absent)
 *   P   settings JSON bytes (UTF-8)
 *   M   SQLCipher-encrypted sakuwise.db bytes
 * ```
 *
 * v1 backups are still restoreable — the settings block is treated as
 * absent (goldPriceGlobal/Digital keep their DataStore value).
 *
 * **Why ship the DEK:** the DEK that encrypted the SQLite file is
 * Keystore-bound on the device. After uninstall / reinstall / `pm clear`,
 * a fresh DEK is provisioned and the restored DB bytes can no longer be
 * decrypted (`file is not a database (code 26)` from SQLCipher). Bundling
 * the original DEK keeps the backup self-contained / zero-knowledge —
 * everything needed to read the data lives behind the user's backup PIN.
 */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: SakuwiseDatabase,
    private val keyManager: KeyManager,
    private val prefsRepo: UserPreferencesRepository,
) {

    private val dbFile: File get() = context.getDatabasePath("sakuwise.db")

    /** Create a `.sakuwise` v2 backup file and return its absolute path. */
    suspend fun backup(pin: CharArray, destDir: File): File = withContext(Dispatchers.IO) {
        // 1. Force WAL checkpoint so the .db file is a complete copy.
        runCatching {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        }
        // 2. Read raw SQLite bytes (still SQLCipher-encrypted at this layer)
        val rawDb = dbFile.readBytes()
        // 3. Read the current DEK
        val dek = keyManager.getOrCreateDek()
        try {
            // 4. Serialize current settings (gold prices, plan config, etc.)
            val prefs = prefsRepo.prefs.first()
            val settingsJson = serializeSettings(prefs)
            val settingsBytes = settingsJson.toByteArray(Charsets.UTF_8)

            // 5. Pack the versioned payload (see BackupPayload for the layout).
            val payload = BackupPayload.pack(dek, settingsBytes, rawDb)
            try {
                // 6. Encrypt the whole payload under the PIN-derived KEK
                val encrypted = BackupCrypto.encryptBackup(payload, pin)
                // 7. Write to destination
                if (!destDir.exists()) destDir.mkdirs()
                val outFile = File(destDir, defaultFilename())
                outFile.writeBytes(encrypted)
                outFile
            } finally {
                payload.fill(0)
            }
        } finally {
            keyManager.zeroize(dek)
        }
    }

    /**
     * Restore from a `.sakuwise` file (v1 or v2). Replaces the current DB,
     * the DEK, and (v2 only) the app settings that affect financial
     * calculations (gold prices, plan config, etc.).
     *
     * The caller MUST restart the process after this returns so Room /
     * SQLCipher re-open the swapped files cleanly.
     */
    suspend fun restore(pin: CharArray, sourceFile: File) = withContext(Dispatchers.IO) {
        val ciphertext = sourceFile.readBytes()
        val payload = BackupCrypto.decryptBackup(ciphertext, pin)
        try {
            val (dek, settingsBytes, dbBytes) = BackupPayload.unpack(payload)
            try {
                // 1. Close Room/SQLCipher so we can swap the file.
                database.close()
                // 2. Install the original DEK so SQLCipher can decrypt the restored DB.
                keyManager.installDek(dek)
                // 3. Swap the DB file atomically-ish.
                val tmp = File(dbFile.parentFile, "sakuwise.db.restore")
                tmp.writeBytes(dbBytes)
                if (dbFile.exists()) dbFile.delete()
                // Clear WAL/SHM sidecar files — stale checkpoint state breaks SQLCipher.
                File(dbFile.parentFile, "sakuwise.db-wal").takeIf { it.exists() }?.delete()
                File(dbFile.parentFile, "sakuwise.db-shm").takeIf { it.exists() }?.delete()
                // renameTo() is atomic on the same filesystem; fall back to
                // copyTo+delete when it fails (cross-partition or FAT quirks).
                // If this block throws, the DB file is gone and the user must
                // re-restore. Caller surfaces the exception as an actionable error.
                if (!tmp.renameTo(dbFile)) {
                    try {
                        tmp.copyTo(dbFile, overwrite = true)
                        tmp.delete()
                    } catch (e: Exception) {
                        throw BackupRestoreException(
                            "File swap failed after original DB was deleted. " +
                                "Restore the backup again to recover.",
                            e,
                        )
                    }
                }
                // 4. Restore settings (v2 only; v1 → settingsBytes is empty → skip)
                if (settingsBytes.isNotEmpty()) {
                    applySettings(settingsBytes)
                }
            } finally {
                dek.fill(0)
            }
        } finally {
            payload.fill(0)
        }
    }

    // ── Settings serialization ────────────────────────────────────────────

    /**
     * Serialize the subset of prefs that are meaningful to restore:
     * - Gold prices (directly affect portfolio valuations)
     * - Plan config (period start, allocation %)
     * - UI preferences (auto-lock, theme, language, balance visibility)
     *
     * Intentionally excluded (device-specific / security-sensitive):
     * - biometricEnabled, PIN/credential, onboardingCompleted, nickname
     * - driveBackupEnabled / driveAccountEmail / timestamps
     */
    private fun serializeSettings(prefs: com.gustiadhitya.sakuwise.core.datastore.UserPreferences): String =
        JSONObject().apply {
            put("goldPriceGlobal", prefs.goldPriceGlobal)
            put("goldPriceDigital", prefs.goldPriceDigital)
            put("planPeriodStartDay", prefs.planPeriodStartDay)
            put("needsPct", prefs.needsPct)
            put("wantsPct", prefs.wantsPct)
            put("investPct", prefs.investPct)
            put("autoLockMinutes", prefs.autoLockMinutes)
            put("themeMode", prefs.themeMode)
            put("language", prefs.language)
            put("balancesHidden", prefs.balancesHidden)
        }.toString()

    private suspend fun applySettings(bytes: ByteArray) {
        val obj = runCatching { JSONObject(bytes.toString(Charsets.UTF_8)) }.getOrNull() ?: return
        if (obj.has("goldPriceGlobal"))
            prefsRepo.setGoldPriceGlobal(obj.getLong("goldPriceGlobal"))
        if (obj.has("goldPriceDigital"))
            prefsRepo.setGoldPriceDigital(obj.getLong("goldPriceDigital"))
        if (obj.has("planPeriodStartDay"))
            runCatching { prefsRepo.setPlanPeriodStartDay(obj.getInt("planPeriodStartDay")) }
        if (obj.has("needsPct") && obj.has("wantsPct") && obj.has("investPct")) {
            val n = obj.getInt("needsPct")
            val w = obj.getInt("wantsPct")
            val i = obj.getInt("investPct")
            if (n + w + i == 100) runCatching { prefsRepo.setAllocationPercentages(n, w, i) }
        }
        if (obj.has("autoLockMinutes"))
            runCatching { prefsRepo.setAutoLockMinutes(obj.getInt("autoLockMinutes")) }
        if (obj.has("themeMode")) {
            val mode = obj.optString("themeMode", "system")
            if (mode in setOf("light", "dark", "system"))
                runCatching { prefsRepo.setThemeMode(mode) }
        }
        if (obj.has("language"))
            runCatching { prefsRepo.setLanguage(obj.getString("language")) }
        if (obj.has("balancesHidden"))
            runCatching { prefsRepo.setBalancesHidden(obj.getBoolean("balancesHidden")) }
    }

    private fun defaultFilename(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.ROOT).format(Date())
        return "sakuwise-backup-$ts.sakuwise"
    }
}
