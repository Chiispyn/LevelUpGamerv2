package com.levelupgamer.levelup.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.levelupgamer.levelup.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun update(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Delete
    suspend fun delete(event: Event)
}
