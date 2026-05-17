package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountSnapshotDao {
    @Query("SELECT * FROM account_snapshots WHERE account_id = :accountId ORDER BY snapshot_date DESC")
    fun observeByAccountId(accountId: String): Flow<List<AccountSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: AccountSnapshotEntity)
}
