package com.levelupgamer.levelup.ui.rewardsshop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.RewardRepository
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.repository.UserRewardRepository
import com.levelupgamer.levelup.model.Reward
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RewardsShopUiState(
    val rewards: List<Reward> = emptyList(),
    val userPoints: Int = 0
)

class RewardsShopViewModel(
    private val rewardRepository: RewardRepository,
    private val userRepository: UserRepository,
    private val userRewardRepository: UserRewardRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsShopUiState())
    val uiState = _uiState.asStateFlow()

    private val _toastChannel = Channel<String>()
    val toastMessage: Flow<String> = _toastChannel.receiveAsFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            rewardRepository.getAllRewards().collect { rewards ->
                // Filtramos aquí las recompensas sin stock
                val availableRewards = rewards.filter { it.stock == null || it.stock > 0 }
                _uiState.update { it.copy(rewards = availableRewards) }
            }
        }
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId != null && userId != -1) {
                userRepository.getUserById(userId)?.let { user ->
                     _uiState.update { it.copy(userPoints = user.points) }
                }
            }
        }
    }

    fun purchaseReward(reward: Reward) {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId == null || userId == -1) {
                _toastChannel.send("Debes iniciar sesión para canjear recompensas.")
                return@launch
            }

            val user = userRepository.getUserById(userId)
            if (user == null) {
                _toastChannel.send("Error al obtener datos del usuario.")
                return@launch
            }

            if (!user.isActive) {
                _toastChannel.send("Tu cuenta está suspendida. No puedes canjear recompensas.")
                return@launch
            }

            if (user.points < reward.pointsCost) {
                _toastChannel.send("No tienes suficientes puntos.")
                return@launch
            }

            // --- LÓGICA DE STOCK ---
            if (reward.stock != null && reward.stock <= 0) {
                _toastChannel.send("Esta recompensa está agotada.")
                return@launch
            }

            // Procesar la compra
            val updatedUser = user.copy(points = user.points - reward.pointsCost)
            userRepository.update(updatedUser)

            userRewardRepository.addRewardForUser(userId, reward.id)

            // Descontar stock si es limitado
            if (reward.stock != null) {
                val updatedReward = reward.copy(stock = reward.stock - 1)
                rewardRepository.updateReward(updatedReward)
            }

            _uiState.update { it.copy(userPoints = updatedUser.points) }
            _toastChannel.send("¡'${reward.title}' canjeada!")
        }
    }
}
