package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.LandAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandAssetDao {
    @Query("SELECT * FROM land_assets ORDER BY name ASC")
    fun observeAll(): Flow<List<LandAssetEntity>>

    @Query("SELECT * FROM land_assets WHERE id = :id")
    fun observeById(id: String): Flow<LandAssetEntity?>

    @Query("SELECT * FROM land_assets WHERE status = 'HELD' ORDER BY name ASC")
    fun observeHeld(): Flow<List<LandAssetEntity>>

    @Upsert
    suspend fun upsert(landAsset: LandAssetEntity)

    @Query("DELETE FROM land_assets WHERE id = :id")
    suspend fun delete(id: String)
}
