package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupService — orchestrates `.sakuwise` create / restore.
 *
 * **Backup payload layout (inside the AES-GCM-encrypted body):**
 * ```
 *   4B  payload version (BE u32 = 1)
 *   4B  DEK length (BE u32; must be 32 for AES-256)
 *   N   DEK bytes
 *   M   SQLCipher-encrypted sakuwise.db bytes
 * ```
 * The PIN-derived KEK in [BackupCrypto] wraps this whole body.
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
) {

    private val dbFile: File get() = context.getDatabasePath("sakuwise.db")

    /** Create a `.sakuwise` backup file and return its absolute path. */
    suspend fun backup(pin: CharArray, destDir: File): File = withContext(Dispatchers.IO) {
        // 1. Force WAL checkpoint so the .db file is a complete copy.
        //    PRAGMA wal_checkpoint returns rows, so SQLCipher requires query() — not execSQL().
        runCatching {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        }
        // 2. Read raw SQLite bytes (still SQLCipher-encrypted at this layer)
        val rawDb = dbFile.readBytes()
        // 3. Read the current DEK (this is the key that decrypts the SQLCipher payload)
        val dek = keyManager.getOrCreateDek()
        try {
            // 4. Pack [4B version][4B dekLen][DEK][DB] into the inner payload
            val payload = packPayload(dek, rawDb)
            try {
                // 5. Encrypt the whole payload under the PIN-derived KEK
                val encrypted = BackupCrypto.encryptBackup(payload, pin)
                // 6. Write to destination
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
     * Restore from a `.sakuwise` file. Replaces the current DB AND the DEK.
     * The caller MUST restart the process (force-stop) after this returns so
     * Room/SQLCipher re-open the swapped files cleanly.
     */
    suspend fun restore(pin: CharArray, sourceFile: File) = withContext(Dispatchers.IO) {
        val ciphertext = sourceFile.readBytes()
        val payload = BackupCrypto.decryptBackup(ciphertext, pin)
        try {
            val (dek, dbBytes) = unpackPayload(payload)
            try {
                // 1. Close the current Room/SQLCipher connection so we can swap the file.
                database.close()
                // 2. Install the original DEK so SQLCipher can decrypt the restored DB.
                keyManager.installDek(dek)
                // 3. Swap the DB file atomically-ish.
                val tmp = File(dbFile.parentFile, "sakuwise.db.restore")
                tmp.writeBytes(dbBytes)
                if (dbFile.exists()) dbFile.delete()
                // Also clear WAL/SHM sidecar files — stale checkpoint state breaks SQLCipher.
                File(dbFile.parentFile, "sakuwise.db-wal").takeIf { it.exists() }?.delete()
                File(dbFile.parentFile, "sakuwise.db-shm").takeIf { it.exists() }?.delete()
                tmp.renameTo(dbFile)
            } finally {
                dek.fill(0)
            }
        } finally {
            payload.fill(0)
        }
    }

    private fun packPayload(dek: ByteArray, db: ByteArray): ByteArray {
        require(dek.size in 16..64) { "DEK length out of range: ${dek.size}" }
        val out = ByteArray(HEADER_LEN + dek.size + db.size)
        val buf = ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN)
        buf.putInt(PAYLOAD_VERSION)
        buf.putInt(dek.size)
        buf.put(dek)
        buf.put(db)
        return out
    }

    private fun unpackPayload(payload: ByteArray): Pair<ByteArray, ByteArray> {
        require(payload.size > HEADER_LEN) { "Payload too small" }
        val buf = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
        val version = buf.int
        require(version == PAYLOAD_VERSION) {
            "Backup payload version $version not supported (expected $PAYLOAD_VERSION)"
        }
        val dekLen = buf.int
        require(dekLen in 16..64) { "Invalid DEK length in payload: $dekLen" }
        require(payload.size > HEADER_LEN + dekLen) { "Payload truncated" }
        val dek = ByteArray(dekLen).also { buf.get(it) }
        val dbLen = payload.size - HEADER_LEN - dekLen
        val db = ByteArray(dbLen).also { buf.get(it) }
        return dek to db
    }

    private fun defaultFilename(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.ROOT).format(Date())
        return "sakuwise-backup-$ts.sakuwise"
    }

    private companion object {
        const val PAYLOAD_VERSION = 1
        const val HEADER_LEN = 8 // 4B version + 4B dekLen
    }
}
