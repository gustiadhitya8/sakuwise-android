package com.gustiadhitya.sakuwise.core.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * BackupCrypto — implements the `.sakuwise` backup file format per
 * Tech Solution §6.4.
 *
 * Binary layout (big-endian):
 *   0       4     Magic "SKWS"
 *   4       1     Format version 0x01
 *   5       1     App schema version 0x01
 *   6       2     Reserved 0x00 0x00
 *   8       16    Argon2id salt
 *   24      12    AES-GCM nonce
 *   36      4     Ciphertext length (uint32)
 *   40      N     AES-GCM ciphertext
 *   40+N    16    Auth tag (already appended by Cipher.doFinal in JCE)
 *
 * Note: Java's Cipher with AES/GCM appends the auth tag to the ciphertext, so
 * the on-disk layout has ciphertext-length covering ciphertext + 16 byte tag.
 */
object BackupCrypto {
    private const val MAGIC_0: Byte = 0x53 // 'S'
    private const val MAGIC_1: Byte = 0x4B // 'K'
    private const val MAGIC_2: Byte = 0x57 // 'W'
    private const val MAGIC_3: Byte = 0x53 // 'S'
    private const val FORMAT_VERSION: Byte = 0x01
    private const val APP_SCHEMA_VERSION: Byte = 0x01
    private const val SALT_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128
    private const val KEK_BYTES = 32

    // Argon2id parameters per Tech Solution §6.3
    private const val T_COST = 3
    private const val M_COST_KIB = 65_536 // 64 MB
    private const val PARALLELISM = 1

    /** Encrypt the SQLite payload using a PIN-derived KEK and emit the full `.sakuwise` byte stream. */
    fun encryptBackup(plainPayload: ByteArray, pin: CharArray): ByteArray {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }

        val kek = deriveKek(pin, salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(kek, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            val ctWithTag = cipher.doFinal(plainPayload)

            val out = ByteArrayOutputStream()
            out.write(byteArrayOf(MAGIC_0, MAGIC_1, MAGIC_2, MAGIC_3))
            out.write(byteArrayOf(FORMAT_VERSION, APP_SCHEMA_VERSION, 0x00, 0x00))
            out.write(salt)
            out.write(nonce)
            out.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(ctWithTag.size).array())
            out.write(ctWithTag)
            return out.toByteArray()
        } finally {
            kek.fill(0)
        }
    }

    /** Decrypt a `.sakuwise` file; throws BadPinException on auth failure. */
    fun decryptBackup(fileBytes: ByteArray, pin: CharArray): ByteArray {
        require(fileBytes.size >= 40 + 16) { "Backup too small" }
        require(fileBytes[0] == MAGIC_0 && fileBytes[1] == MAGIC_1 &&
            fileBytes[2] == MAGIC_2 && fileBytes[3] == MAGIC_3) { "Bukan file Sakuwise yang valid." }
        require(fileBytes[4] == FORMAT_VERSION) { "Format file backup tidak didukung." }
        // Schema version check — refuse newer schemas, accept same/older.
        require(fileBytes[5] <= APP_SCHEMA_VERSION) {
            "File backup dibuat dengan versi aplikasi yang lebih baru. Update aplikasi dulu."
        }
        val salt = fileBytes.copyOfRange(8, 24)
        val nonce = fileBytes.copyOfRange(24, 36)
        val ctLen = ByteBuffer.wrap(fileBytes, 36, 4).order(ByteOrder.BIG_ENDIAN).int
        require(ctLen in 1..(fileBytes.size - 40)) { "Ukuran ciphertext tidak valid." }
        val ct = fileBytes.copyOfRange(40, 40 + ctLen)

        val kek = deriveKek(pin, salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(kek, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            return try {
                cipher.doFinal(ct)
            } catch (t: javax.crypto.AEADBadTagException) {
                throw BadPinException("PIN salah atau file rusak.")
            }
        } finally {
            kek.fill(0)
        }
    }

    private fun deriveKek(pin: CharArray, salt: ByteArray): ByteArray {
        val pinBytes = ByteArray(pin.size).also { out ->
            pin.forEachIndexed { i, c -> out[i] = c.code.toByte() }
        }
        try {
            val argon2 = Argon2Kt()
            val res = argon2.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = pinBytes,
                salt = salt,
                tCostInIterations = T_COST,
                mCostInKibibyte = M_COST_KIB,
                parallelism = PARALLELISM,
                hashLengthInBytes = KEK_BYTES,
            )
            return res.rawHashAsByteArray()
        } finally {
            pinBytes.fill(0)
        }
    }

    /** Copy a file into a byte array (caller closes). */
    fun readAll(file: File): ByteArray = file.readBytes()
}

class BadPinException(message: String) : Exception(message)
