package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = AllocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["allocation_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("allocation_id")],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "allocation_id") val allocationId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "planned_amount") val plannedAmount: Long?,
)
