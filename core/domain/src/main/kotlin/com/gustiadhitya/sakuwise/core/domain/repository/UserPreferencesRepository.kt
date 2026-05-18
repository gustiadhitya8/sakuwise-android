package com.gustiadhitya.sakuwise.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun observeLanguageCode(): Flow<String>
    suspend fun setLanguageCode(code: String)
    suspend fun setNickname(nickname: String)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setDevicePin(pin: String)
}
