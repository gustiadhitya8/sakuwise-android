package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PinStore — stores an Argon2id hash of the user's app-unlock PIN in an
 * EncryptedFile, then verifies entered PINs by re-hashing with the stored salt.
 *
 * File layout (binary, EncryptedFile-wrapped):
 *   1B  version (0x01)
 *   2B  salt length (BE u16)
 *   2B  hash length (BE u16)
 *   N   salt
 *   M   hash
 *
 * Argon2id parameters mirror BackupCrypto §6.3 (m=64MiB, t=3, p=1).
 */
@Singleton
class PinStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val file: File get() = File(context.filesDir, "pin.bin")
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, "sakuwise_master_v1")
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun isPinSet(): Boolean = file.exists()

    /** Hash and persist `pin` (6 digits expected). Overwrites existing. */
    fun setPin(pin: CharArray) {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)
        try {
            writeBlob(salt, hash)
        } finally {
            hash.fill(0)
        }
    }

    /** Verify `pin` against the stored hash. Returns false if no PIN is set. */
    fun verifyPin(pin: CharArray): Boolean {
        if (!file.exists()) return false
        val (salt, expected) = readBlob() ?: return false
        val actual = hash(pin, salt)
        return try {
            constantTimeEquals(expected, actual)
        } finally {
            actual.fill(0)
            expected.fill(0)
        }
    }

    /** Wipe — used by Reset App. */
    fun clear() { if (file.exists()) file.delete() }

    private fun hash(pin: CharArray, salt: ByteArray): ByteArray {
        val pinBytes = ByteArray(pin.size).also { out ->
            pin.forEachIndexed { i, c -> out[i] = c.code.toByte() }
        }
        try {
            return Argon2Kt().hash(
                mode = Argon2Mode.ARGON2_ID,
                password = pinBytes,
                salt = salt,
                tCostInIterations = T_COST,
                mCostInKibibyte = M_COST_KIB,
                parallelism = PARALLELISM,
                hashLengthInBytes = HASH_BYTES,
            ).rawHashAsByteArray()
        } finally {
            pinBytes.fill(0)
        }
    }

    private fun writeBlob(salt: ByteArray, hash: ByteArray) {
        if (file.exists()) file.delete()
        val ef = EncryptedFile.Builder(
            context, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
        ef.openFileOutput().use { out ->
            out.write(byteArrayOf(VERSION))
            out.write(ByteBuffer.allocate(2).putShort(salt.size.toShort()).array())
            out.write(ByteBuffer.allocate(2).putShort(hash.size.toShort()).array())
            out.write(salt)
            out.write(hash)
        }
    }

    private fun readBlob(): Pair<ByteArray, ByteArray>? {
        val ef = EncryptedFile.Builder(
            context, file, masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
        val bytes = ef.openFileInput().use { it.readBytes() }
        if (bytes.size < 5) return null
        if (bytes[0] != VERSION) return null
        val saltLen = ByteBuffer.wrap(bytes, 1, 2).short.toInt()
        val hashLen = ByteBuffer.wrap(bytes, 3, 2).short.toInt()
        if (bytes.size != 5 + saltLen + hashLen) return null
        val salt = bytes.copyOfRange(5, 5 + saltLen)
        val hash = bytes.copyOfRange(5 + saltLen, 5 + saltLen + hashLen)
        return salt to hash
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }

    companion object {
        private const val VERSION: Byte = 0x01
        private const val SALT_BYTES = 16
        private const val HASH_BYTES = 32
        private const val T_COST = 3
        private const val M_COST_KIB = 65_536
        private const val PARALLELISM = 1
    }
}
