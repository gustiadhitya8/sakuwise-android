package com.gustiadhitya.sakuwise.core.data.di

import com.gustiadhitya.sakuwise.core.data.repository.AccountRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.DebtRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.DepositRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.GoldRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.LandRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.PlanRepositoryImpl
import com.gustiadhitya.sakuwise.core.data.repository.TransactionRepositoryImpl
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.domain.repository.PlanRepository
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
    @Binds @Singleton
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository
    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    @Binds @Singleton
    abstract fun bindGoldRepository(impl: GoldRepositoryImpl): GoldRepository
    @Binds @Singleton
    abstract fun bindLandRepository(impl: LandRepositoryImpl): LandRepository
    @Binds @Singleton
    abstract fun bindDepositRepository(impl: DepositRepositoryImpl): DepositRepository
    @Binds @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository
}
