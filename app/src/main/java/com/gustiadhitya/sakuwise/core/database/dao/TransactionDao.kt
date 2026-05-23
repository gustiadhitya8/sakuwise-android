package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE sourceAccountId = :accountId OR destAccountId = :accountId ORDER BY dateEpochDay DESC")
    fun observeForAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE planItemId = :planItemId ORDER BY dateEpochDay DESC")
    fun observeForPlanItem(planItemId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateEpochDay >= :start AND dateEpochDay <= :end ORDER BY dateEpochDay DESC, createdAt DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'income' AND dateEpochDay BETWEEN :start AND :end")
    fun observeIncomeBetween(start: Long, end: Long): Flow<Long>

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND dateEpochDay BETWEEN :start AND :end")
    fun observeExpenseBetween(start: Long, end: Long): Flow<Long>

    @Query("SELECT * FROM income_categories ORDER BY sortOrder")
    fun observeIncomeCategories(): Flow<List<IncomeCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun seedIncomeCategories(rows: List<IncomeCategoryEntity>)

    data class CategorySpendRow(val categoryName: String, val total: Long)

    @Query(
        """
        SELECT c.name AS categoryName, IFNULL(SUM(t.amount), 0) AS total
        FROM transactions t
        INNER JOIN plan_items pi ON t.planItemId = pi.id
        INNER JOIN categories c ON pi.categoryId = c.id
        WHERE t.type = 'expense' AND t.dateEpochDay BETWEEN :start AND :end
        GROUP BY c.id
        ORDER BY total DESC
        LIMIT :limit
        """,
    )
    fun observeTopExpenseCategories(start: Long, end: Long, limit: Int): Flow<List<CategorySpendRow>>
}
