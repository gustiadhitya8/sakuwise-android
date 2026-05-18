package com.gustiadhitya.sakuwise.core.database.di

import android.content.Context
import androidx.room.Room
import com.gustiadhitya.sakuwise.core.database.SakuwiseDatabase
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSakuwiseDatabase(
        @ApplicationContext context: Context,
    ): SakuwiseDatabase = Room.databaseBuilder(
        context,
        SakuwiseDatabase::class.java,
        "sakuwise.db",
    ).build()

    @Provides
    fun provideAccountDao(db: SakuwiseDatabase): AccountDao = db.accountDao()
}
