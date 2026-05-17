package com.gustiadhitya.sakuwise.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_profile",
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "language") val language: String,
    @ColumnInfo(name = "plan_period_start_day") val planPeriodStartDay: Int,
    @ColumnInfo(name = "default_allocation_needs") val defaultAllocationNeeds: Int,
    @ColumnInfo(name = "default_allocation_wants") val defaultAllocationWants: Int,
    @ColumnInfo(name = "default_allocation_investment") val defaultAllocationInvestment: Int,
    @ColumnInfo(name = "auto_lock_minutes") val autoLockMinutes: Int,
    @ColumnInfo(name = "gold_price_global") val goldPriceGlobal: Long?,
    @ColumnInfo(name = "last_backup_timestamp") val lastBackupTimestamp: Instant?,
    @ColumnInfo(name = "onboarding_completed") val onboardingCompleted: Boolean,
)
