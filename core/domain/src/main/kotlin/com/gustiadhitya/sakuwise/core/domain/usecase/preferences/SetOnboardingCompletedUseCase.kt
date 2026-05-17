package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository

class SetOnboardingCompletedUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(completed: Boolean) = repository.setOnboardingCompleted(completed)
}
