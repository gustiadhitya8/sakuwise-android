package com.gustiadhitya.sakuwise.core.data.di

import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.account.UpsertAccountUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.CompleteOnboardingUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.GetOnboardingCompletedUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetBiometricEnabledUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetDevicePinUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetLanguageUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetNicknameUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetOnboardingCompletedUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideGetOnboardingCompletedUseCase(
        repository: UserPreferencesRepository,
    ): GetOnboardingCompletedUseCase = GetOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetOnboardingCompletedUseCase(
        repository: UserPreferencesRepository,
    ): SetOnboardingCompletedUseCase = SetOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetLanguageUseCase(
        repository: UserPreferencesRepository,
    ): SetLanguageUseCase = SetLanguageUseCase(repository)

    @Provides
    fun provideSetNicknameUseCase(
        repository: UserPreferencesRepository,
    ): SetNicknameUseCase = SetNicknameUseCase(repository)

    @Provides
    fun provideSetBiometricEnabledUseCase(
        repository: UserPreferencesRepository,
    ): SetBiometricEnabledUseCase = SetBiometricEnabledUseCase(repository)

    @Provides
    fun provideSetDevicePinUseCase(
        repository: UserPreferencesRepository,
    ): SetDevicePinUseCase = SetDevicePinUseCase(repository)

    @Provides
    fun provideUpsertAccountUseCase(
        repository: AccountRepository,
    ): UpsertAccountUseCase = UpsertAccountUseCase(repository)

    @Provides
    fun provideCompleteOnboardingUseCase(
        setLanguage: SetLanguageUseCase,
        setNickname: SetNicknameUseCase,
        setBiometricEnabled: SetBiometricEnabledUseCase,
        setDevicePin: SetDevicePinUseCase,
        setOnboardingCompleted: SetOnboardingCompletedUseCase,
        upsertAccount: UpsertAccountUseCase,
    ): CompleteOnboardingUseCase = CompleteOnboardingUseCase(
        setLanguage = setLanguage,
        setNickname = setNickname,
        setBiometricEnabled = setBiometricEnabled,
        setDevicePin = setDevicePin,
        setOnboardingCompleted = setOnboardingCompleted,
        upsertAccount = upsertAccount,
    )
}
