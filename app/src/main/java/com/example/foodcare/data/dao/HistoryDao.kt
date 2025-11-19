package com.example.foodcare.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foodcare.data.model.HistoryEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_events ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<HistoryEvent>>

    @Query("SELECT * FROM history_events WHERE userId = :userId ORDER BY createdAt DESC")
    fun getHistoryByUser(userId: String): Flow<List<HistoryEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: HistoryEvent)

    @Query("DELETE FROM history_events")
    suspend fun deleteAll()
}
