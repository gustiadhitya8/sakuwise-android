package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository

class SetNicknameUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(nickname: String) = repository.setNickname(nickname)
}
