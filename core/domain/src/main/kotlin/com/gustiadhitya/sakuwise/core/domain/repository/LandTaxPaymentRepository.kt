package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.LandTaxPayment
import kotlinx.coroutines.flow.Flow

interface LandTaxPaymentRepository {
    fun observeByLandId(landId: String): Flow<List<LandTaxPayment>>
    suspend fun insert(payment: LandTaxPayment)
    suspend fun delete(id: String)
}
