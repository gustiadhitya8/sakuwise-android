package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.DebtDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.DebtRepository
import com.gustiadhitya.sakuwise.core.model.Debt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DebtRepositoryImpl @Inject constructor(
    private val dao: DebtDao,
) : DebtRepository {

    override fun observeAll(): Flow<List<Debt>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Debt?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeOpen(): Flow<List<Debt>> =
        dao.observeOpen().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(debt: Debt) = dao.upsert(debt.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
