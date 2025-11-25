package com.levelupgamer.levelup.ui.eventdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.repository.EventRepository
import com.levelupgamer.levelup.repository.UserEventRepository
import com.levelupgamer.levelup.repository.UserRepository

class EventDetailViewModelFactory(
    private val eventId: String,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
            val app = context.applicationContext as MyApp
            val eventRepository = EventRepository(app.database.eventDao())
            val userRepository = UserRepository(app.database.userDao())
            val userEventRepository = UserEventRepository(app.database.userEventDao())
            return EventDetailViewModel(eventId, eventRepository, userRepository, userEventRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
