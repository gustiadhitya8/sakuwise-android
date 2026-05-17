package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.DepositAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositAssetDao {
    @Query("SELECT * FROM deposit_assets ORDER BY name ASC")
    fun observeAll(): Flow<List<DepositAssetEntity>>

    @Query("SELECT * FROM deposit_assets WHERE id = :id")
    fun observeById(id: String): Flow<DepositAssetEntity?>

    @Query("SELECT * FROM deposit_assets WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun observeActive(): Flow<List<DepositAssetEntity>>

    @Upsert
    suspend fun upsert(depositAsset: DepositAssetEntity)

    @Query("DELETE FROM deposit_assets WHERE id = :id")
    suspend fun delete(id: String)
}
