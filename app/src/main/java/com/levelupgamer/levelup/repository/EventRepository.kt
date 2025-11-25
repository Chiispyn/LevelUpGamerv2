package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.EventDao
import com.levelupgamer.levelup.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun getEventById(eventId: String): Event? = eventDao.getEventById(eventId)

    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.update(event)
    }

    suspend fun delete(event: Event) {
        eventDao.delete(event)
    }
}
