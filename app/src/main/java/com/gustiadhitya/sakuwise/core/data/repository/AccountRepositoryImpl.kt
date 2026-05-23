package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.domain.model.Account
import com.gustiadhitya.sakuwise.core.domain.model.AccountSnapshot
import com.gustiadhitya.sakuwise.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao,
) : AccountRepository {
    override fun observeActive(): Flow<List<Account>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Account>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Account?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeBalance(id: String): Flow<Long> =
        dao.observeBalance(id).map { it ?: 0L }

    override fun observeTotalBalance(): Flow<Long> = dao.observeTotalBalance()

    override suspend fun upsert(account: Account) = dao.upsert(account.toEntity())
    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived)
    override suspend fun delete(id: String) = dao.delete(id)

    override fun observeSnapshots(accountId: String): Flow<List<AccountSnapshot>> =
        dao.observeSnapshots(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSnapshot(snapshot: AccountSnapshot) =
        dao.insertSnapshot(snapshot.toEntity())

    override suspend fun upsertSnapshot(snapshot: AccountSnapshot) =
        dao.upsertSnapshot(snapshot.toEntity())

    override suspend fun deleteSnapshot(snapshotId: String) =
        dao.deleteSnapshot(snapshotId)
}
