package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gustiadhitya.sakuwise.core.database.entity.IncomeCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeCategoryDao {
    @Query("SELECT * FROM income_categories ORDER BY name ASC")
    fun observeAll(): Flow<List<IncomeCategoryEntity>>

    @Upsert
    suspend fun upsert(incomeCategory: IncomeCategoryEntity)

    @Query("DELETE FROM income_categories WHERE id = :id")
    suspend fun delete(id: String)
}
