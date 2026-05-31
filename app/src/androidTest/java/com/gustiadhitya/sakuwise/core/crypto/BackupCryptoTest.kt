package com.gustiadhitya.sakuwise.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A4 (v1.0.5) — backup tamper detection and format robustness.
 *
 * All tests use synthetic fixtures — no real user data.
 * Must run as an instrumented test because Argon2Kt requires the native library.
 */
@RunWith(AndroidJUnit4::class)
class BackupCryptoTest {

    private val correctPin = "123456".toCharArray()
    private val wrongPin   = "999999".toCharArray()
    private val plaintext  = "SYNTHETIC_DB_PAYLOAD_0123456789".toByteArray()

    private fun encrypt(pin: CharArray = correctPin, payload: ByteArray = plaintext): ByteArray =
        BackupCrypto.encryptBackup(payload, pin)

    @Test
    fun roundTrip_succeeds() {
        val encrypted = encrypt()
        val decrypted = BackupCrypto.decryptBackup(encrypted, correctPin)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun wrongPin_throwsBadPinException() {
        val encrypted = encrypt()
        assertThrows(BadPinException::class.java) {
            BackupCrypto.decryptBackup(encrypted, wrongPin)
        }
    }

    @Test
    fun tamperedCiphertext_throwsBadPinException() {
        val encrypted = encrypt().copyOf()
        // Flip a byte in the ciphertext region (after the 40-byte header)
        encrypted[50] = (encrypted[50].toInt() xor 0xFF).toByte()
        assertThrows(BadPinException::class.java) {
            BackupCrypto.decryptBackup(encrypted, correctPin)
        }
    }

    @Test
    fun truncatedFile_throwsIllegalArgumentException() {
        val truncated = encrypt().copyOfRange(0, 39) // below 40+16 minimum
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decryptBackup(truncated, correctPin)
        }
    }

    @Test
    fun wrongMagicBytes_throwsIllegalArgumentException() {
        val encrypted = encrypt().copyOf()
        encrypted[0] = 0x00 // corrupt magic 'S'
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decryptBackup(encrypted, correctPin)
        }
    }

    @Test
    fun tamperedAuthTag_throwsBadPinException() {
        val encrypted = encrypt().copyOf()
        // Flip last byte of the auth tag (appended by GCM at the end)
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1].toInt() xor 0xFF).toByte()
        assertThrows(BadPinException::class.java) {
            BackupCrypto.decryptBackup(encrypted, correctPin)
        }
    }

    @Test
    fun everyEncryption_producesDifferentNonce() {
        val a = encrypt()
        val b = encrypt()
        // Nonces are at bytes 24..35. Two consecutive encryptions must use different nonces.
        val nonceA = a.copyOfRange(24, 36)
        val nonceB = b.copyOfRange(24, 36)
        assert(!nonceA.contentEquals(nonceB)) { "Nonce reuse detected!" }
    }
}
