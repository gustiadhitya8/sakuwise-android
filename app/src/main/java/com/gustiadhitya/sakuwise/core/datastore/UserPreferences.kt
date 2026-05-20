package com.gustiadhitya.sakuwise.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Per Tech Solution §5.5, V1 preferences live in DataStore (plain text — these
 * fields contain no PII; sensitive financial state goes into SQLCipher).
 *
 * Repository interface here in :core:datastore (would normally live in
 * :core:domain in a strict multi-module setup — we'll lift it when the
 * module split happens).
 */
data class UserPreferences(
    val onboardingCompleted: Boolean,
    val userNickname: String,
    val language: String,
    val biometricEnabled: Boolean,
    val usePassphrase: Boolean,
    /** "light", "dark", or "system" — drives SakuwiseTheme(darkTheme=). Default "system". */
    val themeMode: String,
    // Format the CURRENT credential is stored in. Independent of usePassphrase
    // because the toggle changes target format for NEW credentials, while the
    // verify step in ChangePinSheet still needs to render the right input for
    // the existing credential (PIN field if old was PIN, even if new is passphrase).
    val currentCredentialIsPassphrase: Boolean,
    val autoLockMinutes: Int,
    val planPeriodStartDay: Int,
    val needsPct: Int,
    val wantsPct: Int,
    val investPct: Int,
    val goldPriceGlobal: Long,
    val lastBackupTimestamp: Long,
) {
    companion object {
        val DEFAULTS = UserPreferences(
            onboardingCompleted = false,
            userNickname = "",
            language = "id",
            biometricEnabled = true,
            usePassphrase = false,
            themeMode = "system",
            currentCredentialIsPassphrase = false,
            autoLockMinutes = 5,
            planPeriodStartDay = 1,
            needsPct = 50,
            wantsPct = 30,
            investPct = 20,
            goldPriceGlobal = 1_050_000L, // sample default — user can change in Settings
            lastBackupTimestamp = 0L,
        )
    }
}

interface UserPreferencesRepository {
    val prefs: Flow<UserPreferences>
    suspend fun completeOnboarding(
        nickname: String,
        language: String,
        biometricEnabled: Boolean,
    )
    suspend fun setNickname(nickname: String)
    suspend fun setLanguage(language: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setUsePassphrase(enabled: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setCurrentCredentialIsPassphrase(isPassphrase: Boolean)
    suspend fun setAutoLockMinutes(minutes: Int)
    suspend fun setPlanPeriodStartDay(day: Int)
    suspend fun setAllocationPercentages(needs: Int, wants: Int, invest: Int)
    suspend fun setGoldPriceGlobal(pricePerGram: Long)
    suspend fun markBackupNow(epochMs: Long)
    suspend fun setOnboardingIncomplete()
    suspend fun resetAll()
}

internal object PrefKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val USER_NICKNAME = stringPreferencesKey("user_nickname")
    val LANGUAGE = stringPreferencesKey("language")
    val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    val USE_PASSPHRASE = booleanPreferencesKey("use_passphrase")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val CURRENT_CREDENTIAL_IS_PASSPHRASE = booleanPreferencesKey("current_credential_is_passphrase")
    val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
    val PLAN_PERIOD_START_DAY = intPreferencesKey("plan_period_start_day")
    val ALLOC_NEEDS_PCT = intPreferencesKey("alloc_needs_pct")
    val ALLOC_WANTS_PCT = intPreferencesKey("alloc_wants_pct")
    val ALLOC_INVEST_PCT = intPreferencesKey("alloc_invest_pct")
    val GOLD_PRICE_GLOBAL = longPreferencesKey("gold_price_global")
    val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
}

class UserPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val prefs: Flow<UserPreferences> = dataStore.data.map { p ->
        val d = UserPreferences.DEFAULTS
        UserPreferences(
            onboardingCompleted = p[PrefKeys.ONBOARDING_COMPLETED] ?: d.onboardingCompleted,
            userNickname = p[PrefKeys.USER_NICKNAME] ?: d.userNickname,
            language = p[PrefKeys.LANGUAGE] ?: d.language,
            biometricEnabled = p[PrefKeys.BIOMETRIC_ENABLED] ?: d.biometricEnabled,
            usePassphrase = p[PrefKeys.USE_PASSPHRASE] ?: d.usePassphrase,
            themeMode = p[PrefKeys.THEME_MODE] ?: d.themeMode,
            currentCredentialIsPassphrase = p[PrefKeys.CURRENT_CREDENTIAL_IS_PASSPHRASE]
                ?: d.currentCredentialIsPassphrase,
            autoLockMinutes = p[PrefKeys.AUTO_LOCK_MINUTES] ?: d.autoLockMinutes,
            planPeriodStartDay = p[PrefKeys.PLAN_PERIOD_START_DAY] ?: d.planPeriodStartDay,
            needsPct = p[PrefKeys.ALLOC_NEEDS_PCT] ?: d.needsPct,
            wantsPct = p[PrefKeys.ALLOC_WANTS_PCT] ?: d.wantsPct,
            investPct = p[PrefKeys.ALLOC_INVEST_PCT] ?: d.investPct,
            goldPriceGlobal = p[PrefKeys.GOLD_PRICE_GLOBAL] ?: d.goldPriceGlobal,
            lastBackupTimestamp = p[PrefKeys.LAST_BACKUP_TIMESTAMP] ?: d.lastBackupTimestamp,
        )
    }

    override suspend fun completeOnboarding(
        nickname: String,
        language: String,
        biometricEnabled: Boolean,
    ) {
        dataStore.edit { p ->
            p[PrefKeys.ONBOARDING_COMPLETED] = true
            p[PrefKeys.USER_NICKNAME] = nickname
            p[PrefKeys.LANGUAGE] = language
            p[PrefKeys.BIOMETRIC_ENABLED] = biometricEnabled
        }
    }

    override suspend fun setNickname(nickname: String) {
        dataStore.edit { it[PrefKeys.USER_NICKNAME] = nickname }
    }

    override suspend fun setLanguage(language: String) {
        dataStore.edit { it[PrefKeys.LANGUAGE] = language }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setUsePassphrase(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.USE_PASSPHRASE] = enabled }
    }

    override suspend fun setCurrentCredentialIsPassphrase(isPassphrase: Boolean) {
        dataStore.edit { it[PrefKeys.CURRENT_CREDENTIAL_IS_PASSPHRASE] = isPassphrase }
    }

    override suspend fun setThemeMode(mode: String) {
        require(mode in setOf("light", "dark", "system")) { "themeMode must be light/dark/system" }
        dataStore.edit { it[PrefKeys.THEME_MODE] = mode }
    }

    override suspend fun setAutoLockMinutes(minutes: Int) {
        dataStore.edit { it[PrefKeys.AUTO_LOCK_MINUTES] = minutes }
    }

    override suspend fun setPlanPeriodStartDay(day: Int) {
        require(day in 1..28) { "plan_period_start_day must be 1..28" }
        dataStore.edit { it[PrefKeys.PLAN_PERIOD_START_DAY] = day }
    }

    override suspend fun setAllocationPercentages(needs: Int, wants: Int, invest: Int) {
        require(needs + wants + invest == 100) { "allocation must total 100%" }
        dataStore.edit {
            it[PrefKeys.ALLOC_NEEDS_PCT] = needs
            it[PrefKeys.ALLOC_WANTS_PCT] = wants
            it[PrefKeys.ALLOC_INVEST_PCT] = invest
        }
    }

    override suspend fun setGoldPriceGlobal(pricePerGram: Long) {
        dataStore.edit { it[PrefKeys.GOLD_PRICE_GLOBAL] = pricePerGram }
    }

    override suspend fun markBackupNow(epochMs: Long) {
        dataStore.edit { it[PrefKeys.LAST_BACKUP_TIMESTAMP] = epochMs }
    }

    override suspend fun setOnboardingIncomplete() {
        dataStore.edit { it[PrefKeys.ONBOARDING_COMPLETED] = false }
    }

    override suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}
