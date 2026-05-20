package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.SecureRandom

/**
 * KeyManager — manages the Data Encryption Key (DEK).
 *
 * Per Tech Solution §6.2:
 *   - DEK is 256-bit AES, generated once on first launch.
 *   - DEK is stored as an encrypted blob in `filesDir/dek.bin` via
 *     androidx.security.crypto.EncryptedFile.
 *   - The EncryptedFile master key lives in Android Keystore under alias
 *     `sakuwise_master_v1` (hardware-backed where available).
 *   - On daily unlock, biometric/credential unlocks Keystore → EncryptedFile
 *     decrypts → DEK is in memory → SQLCipher opens DB.
 *   - DEK ByteArray is zeroed via [zeroize] after the DB closes.
 */
class KeyManager(private val context: Context) {

    private val dekFile: File get() = File(context.filesDir, DEK_FILENAME)

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, MASTER_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    /** Load existing DEK; generate + store one on first call. */
    fun getOrCreateDek(): ByteArray {
        val file = dekFile
        if (!file.exists()) {
            val fresh = ByteArray(DEK_SIZE).also { SecureRandom().nextBytes(it) }
            persist(fresh)
            return fresh
        }
        val ef = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
        ef.openFileInput().use { return it.readBytes() }
    }

    /** Returns true if a DEK has been provisioned (i.e., not first launch). */
    fun hasDek(): Boolean = dekFile.exists()

    /**
     * Overwrite the persisted DEK with [newDek]. Used by restore-from-backup —
     * the .sakuwise file carries the original install's DEK alongside the
     * SQLCipher-encrypted DB bytes, so we have to install it here before
     * SQLCipher opens the restored sakuwise.db on the next process start.
     */
    fun installDek(newDek: ByteArray) {
        require(newDek.size == DEK_SIZE) { "DEK must be $DEK_SIZE bytes, got ${newDek.size}" }
        persist(newDek)
    }

    /** Zero a key in place — call after the DB closes. */
    fun zeroize(key: ByteArray) {
        key.fill(0)
    }

    private fun persist(dek: ByteArray) {
        if (dekFile.exists()) dekFile.delete()
        val ef = EncryptedFile.Builder(
            context,
            dekFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
        ef.openFileOutput().use { it.write(dek) }
    }

    companion object {
        private const val MASTER_ALIAS = "sakuwise_master_v1"
        private const val DEK_FILENAME = "dek.bin"
        private const val DEK_SIZE = 32 // 256-bit
    }
}
