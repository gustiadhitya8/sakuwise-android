package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.GoldAssetDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.GoldRepository
import com.gustiadhitya.sakuwise.core.model.GoldAsset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GoldRepositoryImpl @Inject constructor(
    private val dao: GoldAssetDao,
) : GoldRepository {

    override fun observeAll(): Flow<List<GoldAsset>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<GoldAsset?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeHeld(): Flow<List<GoldAsset>> =
        dao.observeHeld().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(goldAsset: GoldAsset) = dao.upsert(goldAsset.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
