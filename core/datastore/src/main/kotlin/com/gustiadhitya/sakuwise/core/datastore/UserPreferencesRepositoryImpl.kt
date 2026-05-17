package com.gustiadhitya.sakuwise.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override fun observe(): Flow<UserProfile> = dataStore.data.map { prefs ->
        UserProfile(
            nickname = prefs[UserPreferencesKeys.NICKNAME] ?: "",
            language = prefs[UserPreferencesKeys.LANGUAGE] ?: "id",
            planPeriodStartDay = prefs[UserPreferencesKeys.PLAN_PERIOD_START_DAY] ?: 1,
            defaultAllocationNeeds = prefs[UserPreferencesKeys.ALLOCATION_NEEDS] ?: 50,
            defaultAllocationWants = prefs[UserPreferencesKeys.ALLOCATION_WANTS] ?: 30,
            defaultAllocationInvestment = prefs[UserPreferencesKeys.ALLOCATION_INVESTMENT] ?: 20,
            autoLockMinutes = prefs[UserPreferencesKeys.AUTO_LOCK_MINUTES] ?: 5,
            goldPriceGlobal = prefs[UserPreferencesKeys.GOLD_PRICE_GLOBAL],
            lastBackupTimestamp = prefs[UserPreferencesKeys.LAST_BACKUP_TIMESTAMP]?.let { Instant.ofEpochMilli(it) },
            onboardingCompleted = prefs[UserPreferencesKeys.ONBOARDING_COMPLETED] ?: false,
        )
    }

    override suspend fun updateNickname(nickname: String) {
        dataStore.edit { it[UserPreferencesKeys.NICKNAME] = nickname }
    }

    override suspend fun updateLanguage(language: String) {
        dataStore.edit { it[UserPreferencesKeys.LANGUAGE] = language }
    }

    override suspend fun updatePlanPeriodStartDay(day: Int) {
        dataStore.edit { it[UserPreferencesKeys.PLAN_PERIOD_START_DAY] = day }
    }

    override suspend fun updateDefaultAllocation(needs: Int, wants: Int, investment: Int) {
        dataStore.edit {
            it[UserPreferencesKeys.ALLOCATION_NEEDS] = needs
            it[UserPreferencesKeys.ALLOCATION_WANTS] = wants
            it[UserPreferencesKeys.ALLOCATION_INVESTMENT] = investment
        }
    }

    override suspend fun updateAutoLockMinutes(minutes: Int) {
        dataStore.edit { it[UserPreferencesKeys.AUTO_LOCK_MINUTES] = minutes }
    }

    override suspend fun updateGoldPriceGlobal(price: Long?) {
        dataStore.edit {
            if (price != null) it[UserPreferencesKeys.GOLD_PRICE_GLOBAL] = price
            else it.remove(UserPreferencesKeys.GOLD_PRICE_GLOBAL)
        }
    }

    override suspend fun updateLastBackupTimestamp(timestamp: Instant?) {
        dataStore.edit {
            if (timestamp != null) it[UserPreferencesKeys.LAST_BACKUP_TIMESTAMP] = timestamp.toEpochMilli()
            else it.remove(UserPreferencesKeys.LAST_BACKUP_TIMESTAMP)
        }
    }

    override suspend fun markOnboardingCompleted() {
        dataStore.edit { it[UserPreferencesKeys.ONBOARDING_COMPLETED] = true }
    }
}
