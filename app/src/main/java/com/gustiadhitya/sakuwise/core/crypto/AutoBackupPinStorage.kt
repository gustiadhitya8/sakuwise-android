package com.gustiadhitya.sakuwise.core.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the user's auto-backup PIN encrypted with an Android Keystore
 * AES-256-GCM key. The IV and ciphertext are kept in regular SharedPreferences
 * as Base64 strings.
 *
 * Deliberately avoids Tink / EncryptedFile / EncryptedSharedPreferences to
 * sidestep the known Tink keyset-manager `AEADBadTagException` that occurs
 * when those libraries try to read a stale keyset from SharedPreferences on
 * Samsung devices.
 *
 * Security properties:
 *   - The Keystore key is hardware-backed where available and is bound to
 *     the current device installation. After uninstall the key is destroyed
 *     and the user must re-enable auto-backup.
 *   - The key is NOT user-auth-gated so the WorkManager worker can decrypt
 *     the PIN in the background after the device has been unlocked once.
 */
@Singleton
class AutoBackupPinStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
        if (!ks.containsAlias(KEY_ALIAS)) {
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                .also { it.init(spec) }
                .generateKey()
        }
        return ks.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Encrypt and persist [pin]. The array is NOT zeroed here — caller is
     * responsible for zeroing it after [savePin] returns.
     */
    fun savePin(pin: CharArray) {
        val bytes = ByteArray(pin.size) { pin[it].code.toByte() }
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val ct = cipher.doFinal(bytes)
            prefs.edit()
                .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .putString(PREF_CT, Base64.encodeToString(ct, Base64.NO_WRAP))
                .apply()
        } finally {
            bytes.fill(0)
        }
    }

    /**
     * Decrypt and return the stored PIN, or null if no PIN is stored or
     * decryption fails (e.g. Keystore key was destroyed after reinstall).
     * Caller MUST zero the returned array after use.
     */
    fun loadPin(): CharArray? {
        val ivB64 = prefs.getString(PREF_IV, null) ?: return null
        val ctB64 = prefs.getString(PREF_CT, null) ?: return null
        return try {
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)
            val ct = Base64.decode(ctB64, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LEN_BITS, iv))
            val plain = cipher.doFinal(ct)
            CharArray(plain.size) { plain[it].toInt().toChar() }.also { plain.fill(0) }
        } catch (_: Exception) {
            // Keystore key destroyed (reinstall / wipe) or data corrupted.
            // Clear stale entries so the user can re-enable auto-backup cleanly.
            clearPin()
            null
        }
    }

    /** Returns true if a PIN is currently stored. */
    fun hasPin(): Boolean = prefs.contains(PREF_CT)

    /**
     * Remove the stored PIN and IV. Call when the user disables auto-backup
     * so no credentials are left on disk unnecessarily.
     */
    fun clearPin() {
        prefs.edit().remove(PREF_IV).remove(PREF_CT).apply()
    }

    companion object {
        private const val KEY_ALIAS = "sakuwise_autobackup_pin_v1"
        private const val PREFS_NAME = "sakuwise_autobackup_pin"
        private const val PREF_IV = "iv"
        private const val PREF_CT = "ct"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LEN_BITS = 128
    }
}
