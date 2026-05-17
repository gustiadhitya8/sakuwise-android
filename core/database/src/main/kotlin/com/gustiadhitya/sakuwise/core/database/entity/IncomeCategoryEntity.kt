package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_categories")
data class IncomeCategoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
)
