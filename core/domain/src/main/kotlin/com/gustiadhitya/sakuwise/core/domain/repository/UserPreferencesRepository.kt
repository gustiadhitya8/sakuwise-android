package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface UserPreferencesRepository {
    fun observe(): Flow<UserProfile>
    suspend fun updateNickname(nickname: String)
    suspend fun updateLanguage(language: String)
    suspend fun updatePlanPeriodStartDay(day: Int)
    suspend fun updateDefaultAllocation(needs: Int, wants: Int, investment: Int)
    suspend fun updateAutoLockMinutes(minutes: Int)
    suspend fun updateGoldPriceGlobal(price: Long?)
    suspend fun updateLastBackupTimestamp(timestamp: Instant?)
    suspend fun markOnboardingCompleted()
}
