package com.gustiadhitya.sakuwise.core.crypto

interface BackupCryptoService {
    fun encryptBackup(payload: ByteArray, pin: String): ByteArray
    fun decryptBackup(data: ByteArray, pin: String): Result<ByteArray>
}
