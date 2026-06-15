package com.carhost.mobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LogRecordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logRecordDao(): LogRecordDao
}
