package com.gustiadhitya.sakuwise.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.database.entity.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SakuwiseDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}
