package com.gustiadhitya.sakuwise.core.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BackupCryptoServiceTest {

    private lateinit var service: BackupCryptoServiceImpl

    @BeforeEach
    fun setUp() {
        // argon2kt uses JNI — skip on JVM host (runs on-device via androidTest)
        try {
            Argon2Kt()
            service = BackupCryptoServiceImpl()
        } catch (e: UnsatisfiedLinkError) {
            assumeTrue(false, "argon2kt native library not available in JVM host environment")
        }
    }

    @Test
    fun `encrypt then decrypt returns original payload`() {
        val payload = "Sakuwise backup test payload".toByteArray()
        val pin = "mySecurePin123"

        val encrypted = service.encryptBackup(payload, pin)
        val decrypted = service.decryptBackup(encrypted, pin)

        assertTrue(decrypted.isSuccess)
        assertArrayEquals(payload, decrypted.getOrThrow())
    }

    @Test
    fun `decrypt with wrong pin fails`() {
        val payload = "sensitive data".toByteArray()
        val encrypted = service.encryptBackup(payload, "correctPin")

        val result = service.decryptBackup(encrypted, "wrongPin")

        assertTrue(result.isFailure)
    }

    @Test
    fun `each encryption produces different ciphertext`() {
        val payload = "same payload".toByteArray()
        val pin = "samePin"

        val enc1 = service.encryptBackup(payload, pin)
        val enc2 = service.encryptBackup(payload, pin)

        assertTrue(!enc1.contentEquals(enc2), "Each encryption should produce unique ciphertext due to random salt/nonce")
    }
}
