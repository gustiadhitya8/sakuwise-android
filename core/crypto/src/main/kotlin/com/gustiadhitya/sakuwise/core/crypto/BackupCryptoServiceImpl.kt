package com.gustiadhitya.sakuwise.core.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupCryptoServiceImpl @Inject constructor() : BackupCryptoService {

    private val argon2 = Argon2Kt()
    private val random = SecureRandom()

    override fun encryptBackup(payload: ByteArray, pin: String): ByteArray {
        val salt = ByteArray(BackupFileFormat.SALT_SIZE).also { random.nextBytes(it) }
        val nonce = ByteArray(BackupFileFormat.NONCE_SIZE).also { random.nextBytes(it) }

        val kek = deriveKek(pin, salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(kek, "AES"), GCMParameterSpec(128, nonce))
            val ciphertext = cipher.doFinal(payload)
            // GCM tag is appended to ciphertext by JCE — ciphertext already includes 16-byte tag
            val ciphertextBody = ciphertext.copyOfRange(0, ciphertext.size - BackupFileFormat.TAG_SIZE)
            val tag = ciphertext.copyOfRange(ciphertext.size - BackupFileFormat.TAG_SIZE, ciphertext.size)

            val header = BackupFileFormat.buildHeader(
                appSchemaVersion = 0x01,
                salt = salt,
                nonce = nonce,
                ciphertextLength = ciphertextBody.size,
            )
            return header + ciphertextBody + tag
        } finally {
            kek.fill(0)
        }
    }

    override fun decryptBackup(data: ByteArray, pin: String): Result<ByteArray> = runCatching {
        val header = BackupFileFormat.parseHeader(data).getOrThrow()
        val ciphertextWithTag = data.copyOfRange(40, 40 + header.ciphertextLength + BackupFileFormat.TAG_SIZE)

        val kek = deriveKek(pin, header.salt)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(kek, "AES"), GCMParameterSpec(128, header.nonce))
            cipher.doFinal(ciphertextWithTag)
        } finally {
            kek.fill(0)
        }
    }

    private fun deriveKek(pin: String, salt: ByteArray): ByteArray {
        val pinBytes = pin.toByteArray(Charsets.UTF_8)
        try {
            val result = argon2.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = pinBytes,
                salt = salt,
                tCostInIterations = 3,
                mCostInKibibyte = 65_536,
                parallelism = 1,
                hashLengthInBytes = 32,
            )
            return result.rawHashAsByteArray()
        } finally {
            pinBytes.fill(0)
        }
    }
}
