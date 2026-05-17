package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingCompletedUseCase(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeOnboardingCompleted()
}
