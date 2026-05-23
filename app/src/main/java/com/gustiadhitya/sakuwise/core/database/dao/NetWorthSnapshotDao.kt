package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustiadhitya.sakuwise.core.database.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetWorthSnapshotDao {
    @Query("SELECT * FROM net_worth_snapshots ORDER BY epochDay ASC")
    fun observeAll(): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots WHERE epochDay >= :sinceEpochDay ORDER BY epochDay ASC")
    fun observeSince(sinceEpochDay: Long): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots ORDER BY epochDay DESC LIMIT 1")
    suspend fun latest(): NetWorthSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: NetWorthSnapshotEntity)

    @Query("DELETE FROM net_worth_snapshots WHERE epochDay < :olderThanEpochDay")
    suspend fun prune(olderThanEpochDay: Long)
}
