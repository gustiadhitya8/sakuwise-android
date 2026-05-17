package com.gustiadhitya.sakuwise.core.data.di

import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.GetOnboardingCompletedUseCase
import com.gustiadhitya.sakuwise.core.domain.usecase.preferences.SetLanguageUseCase
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
}
