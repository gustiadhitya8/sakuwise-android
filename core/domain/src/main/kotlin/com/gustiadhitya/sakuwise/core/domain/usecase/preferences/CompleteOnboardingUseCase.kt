package com.gustiadhitya.sakuwise.core.domain.usecase.preferences

import com.gustiadhitya.sakuwise.core.domain.usecase.account.UpsertAccountUseCase
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.AccountType

class CompleteOnboardingUseCase(
    private val setLanguage: SetLanguageUseCase,
    private val setNickname: SetNicknameUseCase,
    private val setBiometricEnabled: SetBiometricEnabledUseCase,
    private val setDevicePin: SetDevicePinUseCase,
    private val setOnboardingCompleted: SetOnboardingCompletedUseCase,
    private val upsertAccount: UpsertAccountUseCase,
) {
    suspend operator fun invoke(
        languageCode: String,
        nickname: String,
        biometricEnabled: Boolean,
        devicePin: String,
        accountName: String,
        accountType: AccountType,
        initialBalance: Long,
    ) {
        setLanguage(languageCode)
        setNickname(nickname)
        setBiometricEnabled(biometricEnabled)
        setDevicePin(devicePin)
        upsertAccount(
            Account(
                name = accountName,
                type = accountType,
                balance = initialBalance,
            )
        )
        setOnboardingCompleted(true)
    }
}
