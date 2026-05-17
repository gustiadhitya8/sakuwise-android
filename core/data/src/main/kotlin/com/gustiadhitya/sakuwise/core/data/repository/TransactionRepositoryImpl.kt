package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.TransactionRepository
import com.gustiadhitya.sakuwise.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun observeByDateRange(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        dao.observeByDateRange(start.toEpochDay(), end.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun observeByAccountId(accountId: String): Flow<List<Transaction>> =
        dao.observeByAccountId(accountId).map { list -> list.map { it.toDomain() } }

    override fun observeByPlanItemId(planItemId: String): Flow<List<Transaction>> =
        dao.observeByPlanItemId(planItemId).map { list -> list.map { it.toDomain() } }

    override fun observeByDebtId(debtId: String): Flow<List<Transaction>> =
        dao.observeByDebtId(debtId).map { list -> list.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction) = dao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) = dao.update(transaction.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
