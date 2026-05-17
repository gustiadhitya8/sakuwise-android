package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    fun observeByAccountId(accountId: String): Flow<List<Transaction>>
    fun observeByPlanItemId(planItemId: String): Flow<List<Transaction>>
    fun observeByDebtId(debtId: String): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)
}
