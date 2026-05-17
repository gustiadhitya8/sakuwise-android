package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository

class SetLanguageUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(code: String) = repository.setLanguageCode(code)
}
