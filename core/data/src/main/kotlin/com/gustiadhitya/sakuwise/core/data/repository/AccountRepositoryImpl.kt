package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
) : AccountRepository {

    override fun observeAll(): Flow<List<Account>> =
        accountDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Account>> =
        accountDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Account?> =
        accountDao.observeById(id).map { it?.toDomain() }

    override fun observeBalance(id: String): Flow<Long> = combine(
        accountDao.observeById(id),
        transactionDao.observeByAccountId(id),
    ) { account, transactions ->
        val initial = account?.initialBalance ?: 0L
        transactions.fold(initial) { balance, tx ->
            when (tx.type) {
                TransactionType.INCOME, TransactionType.DEBT_INFLOW -> balance + tx.amount
                TransactionType.EXPENSE, TransactionType.DEBT_OUTFLOW -> balance - tx.amount
                TransactionType.RECONCILIATION -> balance
                TransactionType.TRANSFER ->
                    if (tx.sourceAccountId == id) balance - tx.amount - (tx.feeAmount ?: 0L)
                    else balance + tx.amount
            }
        }
    }

    override suspend fun upsert(account: Account) = accountDao.upsert(account.toEntity())

    override suspend fun archive(id: String) = accountDao.archive(id)
}
