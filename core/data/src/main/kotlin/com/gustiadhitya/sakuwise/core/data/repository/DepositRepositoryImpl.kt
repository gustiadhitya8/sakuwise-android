package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.DepositAssetDao
import com.gustiadhitya.sakuwise.core.database.dao.DepositSnapshotDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.DepositRepository
import com.gustiadhitya.sakuwise.core.model.DepositAsset
import com.gustiadhitya.sakuwise.core.model.DepositSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DepositRepositoryImpl @Inject constructor(
    private val assetDao: DepositAssetDao,
    private val snapshotDao: DepositSnapshotDao,
) : DepositRepository {

    override fun observeAll(): Flow<List<DepositAsset>> =
        assetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<DepositAsset?> =
        assetDao.observeById(id).map { it?.toDomain() }

    override fun observeActive(): Flow<List<DepositAsset>> =
        assetDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeSnapshots(depositId: String): Flow<List<DepositSnapshot>> =
        snapshotDao.observeByDepositId(depositId).map { list -> list.map { it.toDomain() } }

    override fun observeLatestSnapshot(depositId: String): Flow<DepositSnapshot?> =
        snapshotDao.observeLatest(depositId).map { it?.toDomain() }

    override suspend fun upsert(depositAsset: DepositAsset) = assetDao.upsert(depositAsset.toEntity())

    override suspend fun insertSnapshot(snapshot: DepositSnapshot) = snapshotDao.insert(snapshot.toEntity())

    override suspend fun deleteSnapshot(id: String) = snapshotDao.delete(id)

    override suspend fun delete(id: String) = assetDao.delete(id)
}
