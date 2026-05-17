package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustiadhitya.sakuwise.core.database.entity.DepositSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepositSnapshotDao {
    @Query("SELECT * FROM deposit_snapshots WHERE asset_deposit_id = :depositId ORDER BY snapshot_date DESC")
    fun observeByDepositId(depositId: String): Flow<List<DepositSnapshotEntity>>

    @Query("SELECT * FROM deposit_snapshots WHERE asset_deposit_id = :depositId ORDER BY snapshot_date DESC LIMIT 1")
    fun observeLatest(depositId: String): Flow<DepositSnapshotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: DepositSnapshotEntity)

    @Query("DELETE FROM deposit_snapshots WHERE id = :id")
    suspend fun delete(id: String)
}
