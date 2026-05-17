package com.gustiadhitya.sakuwise.core.data.repository

import com.gustiadhitya.sakuwise.core.database.dao.DebtPaymentDao
import com.gustiadhitya.sakuwise.core.database.mapper.toDomain
import com.gustiadhitya.sakuwise.core.database.mapper.toEntity
import com.gustiadhitya.sakuwise.core.domain.repository.DebtPaymentRepository
import com.gustiadhitya.sakuwise.core.model.DebtPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DebtPaymentRepositoryImpl @Inject constructor(
    private val dao: DebtPaymentDao,
) : DebtPaymentRepository {

    override fun observeByDebtId(debtId: String): Flow<List<DebtPayment>> =
        dao.observeByDebtId(debtId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(payment: DebtPayment) = dao.insert(payment.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)
}
