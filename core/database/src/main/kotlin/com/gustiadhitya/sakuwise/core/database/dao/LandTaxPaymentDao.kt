package com.gustiadhitya.sakuwise.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gustiadhitya.sakuwise.core.database.entity.LandTaxPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandTaxPaymentDao {
    @Query("SELECT * FROM land_tax_payments WHERE asset_land_id = :landId ORDER BY payment_date DESC")
    fun observeByLandId(landId: String): Flow<List<LandTaxPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: LandTaxPaymentEntity)

    @Query("DELETE FROM land_tax_payments WHERE id = :id")
    suspend fun delete(id: String)
}
