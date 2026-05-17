package com.gustiadhitya.sakuwise.core.data.di

import com.gustiadhitya.sakuwise.core.data.repository.UserPreferencesRepositoryImpl
import com.gustiadhitya.sakuwise.core.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository
}
