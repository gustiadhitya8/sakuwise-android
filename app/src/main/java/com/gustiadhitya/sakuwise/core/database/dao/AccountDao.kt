package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.AccountSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE archived = 0 ORDER BY createdAt")
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY archived, createdAt")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun observeById(id: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): AccountEntity?

    @Upsert
    suspend fun upsert(account: AccountEntity)

    @Query("UPDATE accounts SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: String)

    // Computed balance: initialBalance + sum(income to this account)
    //                                 - sum(expense from this account)
    //                                 + sum(transfer in)
    //                                 - sum(transfer out + fees from this account)
    @Query(
        """
        SELECT a.initialBalance
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'income'), 0)
            - IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'expense'), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.destAccountId = a.id AND t.type = 'transfer'), 0)
            - IFNULL((SELECT SUM(t.amount + IFNULL(t.transferFee, 0)) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'transfer'), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'debt_inflow'), 0)
            - IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'debt_outflow'), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       WHERE t.sourceAccountId = a.id AND t.type = 'reconciliation'), 0)
        FROM accounts a WHERE a.id = :id
        """,
    )
    fun observeBalance(id: String): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: AccountSnapshotEntity)

    @Query("SELECT * FROM account_snapshots WHERE accountId = :accountId ORDER BY snapshotEpochDay DESC")
    fun observeSnapshots(accountId: String): Flow<List<AccountSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSnapshot(snapshot: AccountSnapshotEntity)

    @Query("DELETE FROM account_snapshots WHERE id = :id")
    suspend fun deleteSnapshot(id: String)

    /**
     * Total balance across all active accounts. Computed in SQL to avoid the
     * fan-out cost of per-account flows on the dashboard.
     */
    @Query(
        """
        SELECT IFNULL(SUM(a.initialBalance), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.sourceAccountId
                       WHERE a2.archived = 0 AND t.type = 'income'), 0)
            - IFNULL((SELECT SUM(t.amount) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.sourceAccountId
                       WHERE a2.archived = 0 AND t.type = 'expense'), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.destAccountId
                       WHERE a2.archived = 0 AND t.type = 'transfer'), 0)
            - IFNULL((SELECT SUM(t.amount + IFNULL(t.transferFee, 0)) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.sourceAccountId
                       WHERE a2.archived = 0 AND t.type = 'transfer'), 0)
            + IFNULL((SELECT SUM(t.amount) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.sourceAccountId
                       WHERE a2.archived = 0 AND t.type IN ('debt_inflow','reconciliation')), 0)
            - IFNULL((SELECT SUM(t.amount) FROM transactions t
                       JOIN accounts a2 ON a2.id = t.sourceAccountId
                       WHERE a2.archived = 0 AND t.type = 'debt_outflow'), 0)
        FROM accounts a WHERE a.archived = 0
        """,
    )
    fun observeTotalBalance(): Flow<Long>
}
