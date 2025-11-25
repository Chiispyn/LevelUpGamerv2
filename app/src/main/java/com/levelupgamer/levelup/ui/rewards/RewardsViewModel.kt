package com.levelupgamer.levelup.ui.rewards

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.EventRepository
import com.levelupgamer.levelup.repository.UserEventRepository
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.model.Event
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RewardsUiState(
    val events: List<Event> = emptyList(),
    val userPoints: Int = 0,
    val inscribedEventIds: Set<String> = emptySet()
)

class RewardsViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val userEventRepository: UserEventRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState = _uiState.asStateFlow()

    private val _toastChannel = Channel<String>()
    val toastMessage: Flow<String> = _toastChannel.receiveAsFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val userId = UserManager.getLoggedInUserId(context)
        if (userId == null || userId == -1) return

        viewModelScope.launch {
            eventRepository.getAllEvents().collect { events ->
                _uiState.update { it.copy(events = events) }
            }
        }

        viewModelScope.launch {
            userRepository.getUserById(userId)?.let { user ->
                _uiState.update { it.copy(userPoints = user.points) }
            }
        }

        viewModelScope.launch {
            userEventRepository.getUserEvents(userId).collect { userEvents ->
                _uiState.update { it.copy(inscribedEventIds = userEvents.map { it.eventId }.toSet()) }
            }
        }
    }

    fun inscribeToEvent(event: Event) {
        viewModelScope.launch {
            // --- COMPROBACIÓN DE INSCRIPCIÓN ---
            if (_uiState.value.inscribedEventIds.contains(event.id)) {
                _toastChannel.send("Ya estás inscrito en este evento.")
                return@launch
            }

            val userId = UserManager.getLoggedInUserId(context)
            if (userId == null || userId == -1) {
                _toastChannel.send("Debes iniciar sesión para participar.")
                return@launch
            }

            val user = userRepository.getUserById(userId)
            if (user == null) {
                _toastChannel.send("Error al obtener datos del usuario.")
                return@launch
            }

            if (!user.isActive) {
                _toastChannel.send("Tu cuenta está suspendida. No puedes participar en eventos.")
                return@launch
            }
            
            val updatedUser = user.copy(points = user.points + event.inscriptionPoints)
            userRepository.update(updatedUser)

            userEventRepository.inscribeUserToEvent(userId, event.id)

            _uiState.update { it.copy(userPoints = updatedUser.points) }
            _toastChannel.send("¡+${event.inscriptionPoints} puntos por participar!")
        }
    }
}
