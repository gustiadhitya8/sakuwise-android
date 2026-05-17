package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.AccountSnapshotDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.AccountSnapshotRepository
import com.gustiadhitya.sakuwise.core.model.AccountSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountSnapshotRepositoryImpl @Inject constructor(
    private val dao: AccountSnapshotDao,
) : AccountSnapshotRepository {

    override fun observeByAccountId(accountId: String): Flow<List<AccountSnapshot>> =
        dao.observeByAccountId(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(snapshot: AccountSnapshot) = dao.insert(snapshot.toEntity())
}
