package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.GoldAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoldAssetDao {
    @Query("SELECT * FROM gold_assets ORDER BY purchase_date DESC")
    fun observeAll(): Flow<List<GoldAssetEntity>>

    @Query("SELECT * FROM gold_assets WHERE id = :id")
    fun observeById(id: String): Flow<GoldAssetEntity?>

    @Query("SELECT * FROM gold_assets WHERE status = 'HELD' ORDER BY purchase_date DESC")
    fun observeHeld(): Flow<List<GoldAssetEntity>>

    @Upsert
    suspend fun upsert(goldAsset: GoldAssetEntity)

    @Query("DELETE FROM gold_assets WHERE id = :id")
    suspend fun delete(id: String)
}
