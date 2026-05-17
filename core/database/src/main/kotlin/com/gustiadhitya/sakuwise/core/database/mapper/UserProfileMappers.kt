package com.gustiadhitya.sakuwise.core.database.mapper

import com.gustiadhitya.sakuwise.core.database.entity.UserProfileEntity
import com.gustiadhitya.sakuwise.core.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    nickname = nickname,
    language = language,
    planPeriodStartDay = planPeriodStartDay,
    defaultAllocationNeeds = defaultAllocationNeeds,
    defaultAllocationWants = defaultAllocationWants,
    defaultAllocationInvestment = defaultAllocationInvestment,
    autoLockMinutes = autoLockMinutes,
    goldPriceGlobal = goldPriceGlobal,
    lastBackupTimestamp = lastBackupTimestamp,
    onboardingCompleted = onboardingCompleted,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    nickname = nickname,
    language = language,
    planPeriodStartDay = planPeriodStartDay,
    defaultAllocationNeeds = defaultAllocationNeeds,
    defaultAllocationWants = defaultAllocationWants,
    defaultAllocationInvestment = defaultAllocationInvestment,
    autoLockMinutes = autoLockMinutes,
    goldPriceGlobal = goldPriceGlobal,
    lastBackupTimestamp = lastBackupTimestamp,
    onboardingCompleted = onboardingCompleted,
)
