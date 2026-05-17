package com.gustiadhitya.sakuwise.core.crypto

import java.nio.ByteBuffer
import java.nio.ByteOrder

object BackupFileFormat {
    // Magic: "SKWS"
    private val MAGIC = byteArrayOf(0x53, 0x4B, 0x57, 0x53.toByte())
    private const val FORMAT_VERSION: Byte = 0x01
    const val SALT_SIZE = 16
    const val NONCE_SIZE = 12
    const val TAG_SIZE = 16

    fun buildHeader(
        appSchemaVersion: Byte,
        salt: ByteArray,
        nonce: ByteArray,
        ciphertextLength: Int,
    ): ByteArray {
        require(salt.size == SALT_SIZE)
        require(nonce.size == NONCE_SIZE)
        val buf = ByteBuffer.allocate(40).order(ByteOrder.BIG_ENDIAN)
        buf.put(MAGIC)                    // 4 bytes
        buf.put(FORMAT_VERSION)           // 1 byte
        buf.put(appSchemaVersion)         // 1 byte
        buf.putShort(0)                   // 2 bytes reserved
        buf.put(salt)                     // 16 bytes
        buf.put(nonce)                    // 12 bytes
        buf.putInt(ciphertextLength)      // 4 bytes
        return buf.array()
    }

    data class ParsedHeader(
        val appSchemaVersion: Byte,
        val salt: ByteArray,
        val nonce: ByteArray,
        val ciphertextLength: Int,
    )

    fun parseHeader(data: ByteArray): Result<ParsedHeader> {
        if (data.size < 40) return Result.failure(IllegalArgumentException("File too short"))
        val magic = data.copyOfRange(0, 4)
        if (!magic.contentEquals(MAGIC)) return Result.failure(IllegalArgumentException("Invalid magic bytes"))
        val formatVersion = data[4]
        if (formatVersion != FORMAT_VERSION) return Result.failure(IllegalArgumentException("Unsupported format version $formatVersion"))
        val appSchemaVersion = data[5]
        val salt = data.copyOfRange(8, 24)
        val nonce = data.copyOfRange(24, 36)
        val ciphertextLength = ByteBuffer.wrap(data, 36, 4).order(ByteOrder.BIG_ENDIAN).int
        return Result.success(ParsedHeader(appSchemaVersion, salt, nonce, ciphertextLength))
    }
}
