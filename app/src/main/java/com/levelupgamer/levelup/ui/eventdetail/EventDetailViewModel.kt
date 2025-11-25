package com.levelupgamer.levelup.ui.eventdetail

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

data class EventDetailUiState(
    val event: Event? = null,
    val isUserInscribed: Boolean = false,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

class EventDetailViewModel(
    private val eventId: String,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val userEventRepository: UserEventRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _toastChannel = Channel<String>()
    val toastMessage: Flow<String> = _toastChannel.receiveAsFlow()

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val event = eventRepository.getEventById(eventId)
            if (event == null) {
                _uiState.update { it.copy(isLoading = false, userMessage = "Evento no encontrado.") }
                return@launch
            }

            val userId = UserManager.getLoggedInUserId(context)
            var isUserInscribed = false
            if (userId != null && userId != -1) {
                isUserInscribed = userEventRepository.isUserInscribed(userId, eventId)
            }

            _uiState.update {
                it.copy(
                    event = event,
                    isUserInscribed = isUserInscribed,
                    isLoading = false
                )
            }
        }
    }

    fun inscribeToEvent() {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId == null || userId == -1) {
                _toastChannel.send("Debes iniciar sesión para inscribirte.")
                return@launch
            }

            val user = userRepository.getUserById(userId)
            val event = _uiState.value.event

            if (user == null || event == null) {
                _toastChannel.send("Error al procesar la inscripción.")
                return@launch
            }

            if (_uiState.value.isUserInscribed) {
                _toastChannel.send("Ya estás inscrito en este evento.")
                return@launch
            }

            // Actualizar puntos del usuario y estado de inscripción
            val updatedUser = user.copy(points = user.points + event.inscriptionPoints)
            userRepository.update(updatedUser)
            userEventRepository.inscribeUserToEvent(userId, event.id)

            _uiState.update { it.copy(isUserInscribed = true) }
            _toastChannel.send("¡Inscripción exitosa! +${event.inscriptionPoints} puntos.")
        }
    }
}
