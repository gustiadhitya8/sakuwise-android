package com.gustiadhitya.sakuwise.core.data.repository

import app.cash.turbine.test
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.dao.TransactionDao
import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity
import com.gustiadhitya.sakuwise.core.database.entity.TransactionEntity
import com.gustiadhitya.sakuwise.core.model.Account
import com.gustiadhitya.sakuwise.core.model.AccountStatus
import com.gustiadhitya.sakuwise.core.model.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class AccountRepositoryImplTest {

    private val fakeAccounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    private val fakeTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())

    private val fakeAccountDao = object : AccountDao {
        override fun observeAll(): Flow<List<AccountEntity>> = fakeAccounts
        override fun observeActive(): Flow<List<AccountEntity>> =
            fakeAccounts.map { list -> list.filter { it.status == AccountStatus.ACTIVE } }
        override fun observeById(id: String): Flow<AccountEntity?> =
            fakeAccounts.map { list -> list.find { it.id == id } }
        override suspend fun upsert(account: AccountEntity) {
            val current = fakeAccounts.value.toMutableList()
            current.removeIf { it.id == account.id }
            current.add(account)
            fakeAccounts.value = current
        }
        override suspend fun archive(id: String) {
            fakeAccounts.value = fakeAccounts.value.map {
                if (it.id == id) it.copy(status = AccountStatus.ARCHIVED) else it
            }
        }
    }

    private val fakeTransactionDao = object : TransactionDao {
        override fun observeByDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>> = fakeTransactions
        override fun observeByAccountId(accountId: String): Flow<List<TransactionEntity>> =
            fakeTransactions.map { list -> list.filter { it.sourceAccountId == accountId || it.destinationAccountId == accountId } }
        override fun observeByPlanItemId(planItemId: String): Flow<List<TransactionEntity>> = fakeTransactions
        override fun observeByDebtId(debtId: String): Flow<List<TransactionEntity>> = fakeTransactions
        override fun observeRecent(limit: Int): Flow<List<TransactionEntity>> = fakeTransactions
        override suspend fun insert(transaction: TransactionEntity) {}
        override suspend fun update(transaction: TransactionEntity) {}
        override suspend fun delete(id: String) {}
    }

    private val repository = AccountRepositoryImpl(fakeAccountDao, fakeTransactionDao)

    private fun account(id: String, name: String = "Test") = Account(
        id = id, name = name, type = AccountType.CASH,
        initialBalance = 0L, color = null, icon = null,
        status = AccountStatus.ACTIVE, createdAt = Instant.EPOCH,
    )

    @Test
    fun `insert and observe active accounts round trip`() = runTest {
        repository.upsert(account("acc-1", "Cash"))
        repository.upsert(account("acc-2", "Bank"))

        repository.observeActive().test {
            val accounts = awaitItem()
            assertEquals(2, accounts.size)
            assertTrue(accounts.any { it.id == "acc-1" })
            assertTrue(accounts.any { it.id == "acc-2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `archive removes account from active list`() = runTest {
        repository.upsert(account("acc-1"))
        repository.upsert(account("acc-2"))
        repository.archive("acc-1")

        repository.observeActive().test {
            val accounts = awaitItem()
            assertEquals(1, accounts.size)
            assertEquals("acc-2", accounts[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe all includes archived accounts`() = runTest {
        repository.upsert(account("acc-1"))
        repository.upsert(account("acc-2"))
        repository.archive("acc-1")

        repository.observeAll().test {
            val accounts = awaitItem()
            assertEquals(2, accounts.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
