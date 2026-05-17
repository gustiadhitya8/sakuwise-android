package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.LandAsset
import kotlinx.coroutines.flow.Flow

interface LandRepository {
    fun observeAll(): Flow<List<LandAsset>>
    fun observeById(id: String): Flow<LandAsset?>
    fun observeHeld(): Flow<List<LandAsset>>
    suspend fun upsert(landAsset: LandAsset)
    suspend fun delete(id: String)
}
