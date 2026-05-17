package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.datastore.datasource.UserPreferencesDataSource
import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : UserPreferencesRepository {

    override fun observeOnboardingCompleted(): Flow<Boolean> =
        dataSource.observeOnboardingCompleted()

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        dataSource.setOnboardingCompleted(completed)

    override fun observeLanguageCode(): Flow<String> =
        dataSource.observeLanguageCode()

    override suspend fun setLanguageCode(code: String) =
        dataSource.setLanguageCode(code)
}
