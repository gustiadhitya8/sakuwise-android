package com.gustiadhitya.sakuwise.core.crypto.di

import com.gustiadhitya.sakuwise.core.crypto.BackupCryptoService
import com.gustiadhitya.sakuwise.core.crypto.BackupCryptoServiceImpl
import com.gustiadhitya.sakuwise.core.crypto.KeyManager
import com.gustiadhitya.sakuwise.core.crypto.KeyManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoModule {
    @Binds @Singleton
    abstract fun bindKeyManager(impl: KeyManagerImpl): KeyManager

    @Binds @Singleton
    abstract fun bindBackupCryptoService(impl: BackupCryptoServiceImpl): BackupCryptoService
}
