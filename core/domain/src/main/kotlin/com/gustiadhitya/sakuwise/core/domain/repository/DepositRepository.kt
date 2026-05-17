package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.DepositAsset
import com.gustiadhitya.sakuwise.core.model.DepositSnapshot
import kotlinx.coroutines.flow.Flow

interface DepositRepository {
    fun observeAll(): Flow<List<DepositAsset>>
    fun observeById(id: String): Flow<DepositAsset?>
    fun observeActive(): Flow<List<DepositAsset>>
    fun observeSnapshots(depositId: String): Flow<List<DepositSnapshot>>
    fun observeLatestSnapshot(depositId: String): Flow<DepositSnapshot?>
    suspend fun upsert(depositAsset: DepositAsset)
    suspend fun insertSnapshot(snapshot: DepositSnapshot)
    suspend fun deleteSnapshot(id: String)
    suspend fun delete(id: String)
}
