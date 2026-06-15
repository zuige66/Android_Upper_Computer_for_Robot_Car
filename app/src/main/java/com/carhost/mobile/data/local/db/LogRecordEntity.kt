package com.carhost.mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.carhost.mobile.data.model.LogEntry

@Entity(tableName = "log_records")
data class LogRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val level: String,
    val message: String,
) {
    fun toModel(): LogEntry = LogEntry(
        id = id,
        timestamp = timestamp,
        level = level,
        message = message,
    )
}
