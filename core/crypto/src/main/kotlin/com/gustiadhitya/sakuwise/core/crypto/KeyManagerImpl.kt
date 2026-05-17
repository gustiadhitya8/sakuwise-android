package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : KeyManager {

    private val dekFile = File(context.filesDir, "dek.bin")

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedFile: EncryptedFile
        get() = EncryptedFile.Builder(
            context,
            dekFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()

    @Volatile
    private var cachedDek: ByteArray? = null

    override fun getDek(): ByteArray {
        return cachedDek ?: loadDek().also { cachedDek = it }
    }

    override fun setupKeyOnFirstLaunch() {
        if (!dekFile.exists()) {
            val dek = ByteArray(32).also { SecureRandom().nextBytes(it) }
            encryptedFile.openFileOutput().use { it.write(dek) }
            dek.fill(0)
        }
    }

    private fun loadDek(): ByteArray {
        return encryptedFile.openFileInput().use { it.readBytes() }
    }
}
