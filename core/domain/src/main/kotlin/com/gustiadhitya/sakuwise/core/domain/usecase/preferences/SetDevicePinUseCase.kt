package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository

class SetDevicePinUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(pin: String) = repository.setDevicePin(pin)
}
