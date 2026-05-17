package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.LandTaxPaymentDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.LandTaxPaymentRepository
import com.gustiadhitya.sakuwise.core.model.LandTaxPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LandTaxPaymentRepositoryImpl @Inject constructor(
    private val dao: LandTaxPaymentDao,
) : LandTaxPaymentRepository {

    override fun observeByLandId(landId: String): Flow<List<LandTaxPayment>> =
        dao.observeByLandId(landId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(payment: LandTaxPayment) = dao.insert(payment.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
