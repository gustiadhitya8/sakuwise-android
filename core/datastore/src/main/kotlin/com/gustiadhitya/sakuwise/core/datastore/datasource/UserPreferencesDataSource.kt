package com.gustiadhitya.sakuwise.core.datastore.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val languageCodeKey = stringPreferencesKey("language_code")

    fun observeOnboardingCompleted(): Flow<Boolean> =
        dataStore.data.map { it[onboardingCompletedKey] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[onboardingCompletedKey] = completed }
    }

    fun observeLanguageCode(): Flow<String> =
        dataStore.data.map { it[languageCodeKey] ?: "id" }

    suspend fun setLanguageCode(code: String) {
        dataStore.edit { it[languageCodeKey] = code }
    }
}
