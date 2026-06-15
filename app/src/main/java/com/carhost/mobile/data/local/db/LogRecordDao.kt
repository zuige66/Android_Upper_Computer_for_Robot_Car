package com.carhost.mobile.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogRecordDao {
    @Query("SELECT * FROM log_records ORDER BY id DESC LIMIT :limit")
    fun observeRecent(limit: Int = 120): Flow<List<LogRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LogRecordEntity)

    @Query("DELETE FROM log_records")
    suspend fun clear()

    @Query("DELETE FROM log_records WHERE id NOT IN (SELECT id FROM log_records ORDER BY id DESC LIMIT :keep)")
    suspend fun trim(keep: Int = 200)
}
