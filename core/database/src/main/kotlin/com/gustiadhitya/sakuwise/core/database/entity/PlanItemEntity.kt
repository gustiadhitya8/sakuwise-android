package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gustiadhitya.sakuwise.core.model.RecurrenceType

@Entity(
    tableName = "plan_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("category_id")],
)
data class PlanItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "planned_amount") val plannedAmount: Long,
    @ColumnInfo(name = "recurrence") val recurrence: RecurrenceType,
    @ColumnInfo(name = "notes") val notes: String?,
)
