package com.gustiadhitya.sakuwise.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object UserPreferencesKeys {
    val NICKNAME = stringPreferencesKey("nickname")
    val LANGUAGE = stringPreferencesKey("language")
    val PLAN_PERIOD_START_DAY = intPreferencesKey("plan_period_start_day")
    val ALLOCATION_NEEDS = intPreferencesKey("allocation_needs")
    val ALLOCATION_WANTS = intPreferencesKey("allocation_wants")
    val ALLOCATION_INVESTMENT = intPreferencesKey("allocation_investment")
    val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
    val GOLD_PRICE_GLOBAL = longPreferencesKey("gold_price_global")
    val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}
