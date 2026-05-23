package com.gustiadhitya.sakuwise.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val PREFS_NAME = "sakuwise_user_prefs"
private val Context.userPrefsStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userPrefsStore

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataStore: DataStore<Preferences>,
    ): UserPreferencesRepository = UserPreferencesRepositoryImpl(dataStore)
}
