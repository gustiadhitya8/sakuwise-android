package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.AccountSnapshot
import kotlinx.coroutines.flow.Flow

interface AccountSnapshotRepository {
    fun observeByAccountId(accountId: String): Flow<List<AccountSnapshot>>
    suspend fun insert(snapshot: AccountSnapshot)
}
