package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.DebtPayment
import kotlinx.coroutines.flow.Flow

interface DebtPaymentRepository {
    fun observeByDebtId(debtId: String): Flow<List<DebtPayment>>
    suspend fun insert(payment: DebtPayment)
    suspend fun delete(id: String)
}
