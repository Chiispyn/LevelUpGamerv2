package com.levelupgamer.levelup.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.levelupgamer.levelup.model.UserEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userEvent: UserEvent)

    @Query("SELECT * FROM user_events WHERE userId = :userId")
    fun getUserEvents(userId: Int): Flow<List<UserEvent>>

    @Query("SELECT COUNT(*) FROM user_events WHERE userId = :userId AND eventId = :eventId")
    suspend fun isUserInscribed(userId: Int, eventId: String): Int
}
