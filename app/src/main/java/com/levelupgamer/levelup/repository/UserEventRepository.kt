package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.UserEventDao
import com.levelupgamer.levelup.model.UserEvent
import kotlinx.coroutines.flow.Flow

class UserEventRepository(private val userEventDao: UserEventDao) {

    fun getUserEvents(userId: Int): Flow<List<UserEvent>> {
        return userEventDao.getUserEvents(userId)
    }

    suspend fun isUserInscribed(userId: Int, eventId: String): Boolean {
        return userEventDao.isUserInscribed(userId, eventId) > 0
    }

    suspend fun inscribeUserToEvent(userId: Int, eventId: String) {
        val userEvent = UserEvent(userId = userId, eventId = eventId)
        userEventDao.insert(userEvent)
    }
}
