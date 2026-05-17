package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
    fun observeActive(): Flow<List<Account>>
    fun observeById(id: String): Flow<Account?>
    fun observeBalance(id: String): Flow<Long>
    suspend fun upsert(account: Account)
    suspend fun archive(id: String)
}
