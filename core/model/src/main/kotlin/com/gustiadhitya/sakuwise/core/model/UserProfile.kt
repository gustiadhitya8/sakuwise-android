package com.gustiadhitya.sakuwise.core.model

import java.time.Instant

data class UserProfile(
    val id: String = "user_profile",
    val nickname: String,
    val language: String,
    val planPeriodStartDay: Int,
    val defaultAllocationNeeds: Int,
    val defaultAllocationWants: Int,
    val defaultAllocationInvestment: Int,
    val autoLockMinutes: Int,
    val goldPriceGlobal: Long?,
    val lastBackupTimestamp: Instant?,
    val onboardingCompleted: Boolean,
)
