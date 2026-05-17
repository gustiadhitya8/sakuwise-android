package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.DepositAssetStatus
import com.gustiadhitya.sakuwise.core.model.DepositAssetType

@Entity(tableName = "deposit_assets")
data class DepositAssetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type_label") val typeLabel: DepositAssetType,
    @ColumnInfo(name = "institution_info") val institutionInfo: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "status") val status: DepositAssetStatus,
)
