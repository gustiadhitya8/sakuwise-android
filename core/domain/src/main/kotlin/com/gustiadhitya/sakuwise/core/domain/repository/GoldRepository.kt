package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.GoldAsset
import kotlinx.coroutines.flow.Flow

interface GoldRepository {
    fun observeAll(): Flow<List<GoldAsset>>
    fun observeById(id: String): Flow<GoldAsset?>
    fun observeHeld(): Flow<List<GoldAsset>>
    suspend fun upsert(goldAsset: GoldAsset)
    suspend fun delete(id: String)
}
