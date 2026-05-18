package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository

class SetBiometricEnabledUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setBiometricEnabled(enabled)
}
