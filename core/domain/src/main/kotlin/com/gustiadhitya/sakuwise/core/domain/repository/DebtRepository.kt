package com.gustiadhitya.sakuwise.core.domain.repository

import com.gustiadhitya.sakuwise.core.model.Debt
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun observeAll(): Flow<List<Debt>>
    fun observeById(id: String): Flow<Debt?>
    fun observeOpen(): Flow<List<Debt>>
    suspend fun upsert(debt: Debt)
    suspend fun delete(id: String)
}
