package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.LandAssetDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.LandRepository
import com.gustiadhitya.sakuwise.core.model.LandAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LandRepositoryImpl @Inject constructor(
    private val dao: LandAssetDao,
) : LandRepository {

    override fun observeAll(): Flow<List<LandAsset>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<LandAsset?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeHeld(): Flow<List<LandAsset>> =
        dao.observeHeld().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(landAsset: LandAsset) = dao.upsert(landAsset.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
