package com.levelupgamer.levelup.ui.checkout

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.data.repository.*
import com.levelupgamer.levelup.model.*
import com.levelupgamer.levelup.util.ShippingManager
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class CheckoutUiState(
    val addresses: List<UserAddress> = emptyList(),
    val activeRewards: List<Reward> = emptyList(),
    val selectedAddress: UserAddress? = null,
    val selectedReward: Reward? = null,
    val selectedPaymentMethod: String? = null,
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val discountAmount: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val orderPlacedId: String? = null,
    val pointsEarned: Int = 0,
    val error: String? = null
)

class CheckoutViewModel(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val addressRepository: AddressRepository,
    private val rewardRepository: RewardRepository,
    private val userRewardRepository: UserRewardRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var currentCartItems: List<Pair<Product, Int>> = emptyList()

    init {
        loadInitialData()
    }

    fun initCartItems(items: List<Pair<Product, Int>>) {
        currentCartItems = items
        calculateTotals()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context) ?: return@launch

            // Load Addresses
            addressRepository.getAddressesForUser(userId).collect { addresses ->
                _uiState.update { it.copy(addresses = addresses, selectedAddress = addresses.find { addr -> addr.isPrimary }) }
                calculateTotals()
            }
        }
        
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context) ?: return@launch
             // Load Rewards
            userRewardRepository.getUserRewards(userId).combine(rewardRepository.getAllRewards()) { userRewards, allRewards ->
                val userRewardIds = userRewards.map { it.rewardId }.toSet()
                allRewards.filter { it.id in userRewardIds }
            }.collect { rewards ->
                _uiState.update { it.copy(activeRewards = rewards) }
            }
        }
    }

    fun onAddressSelected(address: UserAddress) {
        _uiState.update { it.copy(selectedAddress = address) }
        calculateTotals()
    }

    fun onRewardSelected(reward: Reward?) {
        _uiState.update { it.copy(selectedReward = reward) }
        calculateTotals()
    }

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    private fun calculateTotals() {
        val state = _uiState.value
        val subtotal = currentCartItems.sumOf { (product, quantity) -> product.price * quantity }.toDouble()
        
        val shippingInfo = state.selectedAddress?.let { ShippingManager.getShippingInfo(it.region) }
        val baseShippingCost = shippingInfo?.cost?.toDouble() ?: 0.0
        
        val discountAmount = when (state.selectedReward?.type) {
            RewardType.DISCOUNT_PERCENTAGE.name -> subtotal * ((state.selectedReward?.value ?: 0.0) / 100.0)
            RewardType.DISCOUNT_AMOUNT.name -> state.selectedReward?.value ?: 0.0
            else -> 0.0
        }

        val finalShippingCost = if (state.selectedReward?.type == RewardType.FREE_SHIPPING.name) 0.0 else baseShippingCost
        val total = (subtotal - discountAmount).coerceAtLeast(0.0) + finalShippingCost

        _uiState.update { 
            it.copy(
                subtotal = subtotal,
                shippingCost = finalShippingCost,
                discountAmount = discountAmount,
                total = total
            ) 
        }
    }

    fun onPlaceOrderClick(onCartCleared: () -> Unit) {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            val state = _uiState.value
            
            if (userId != null && state.selectedAddress != null && state.selectedPaymentMethod != null) {
                _uiState.update { it.copy(isLoading = true) }
                
                try {
                    val currentUser = userRepository.getUserById(userId)
                    if (currentUser?.isActive == false) {
                         _uiState.update { it.copy(isLoading = false, error = "Cuenta suspendida") }
                         return@launch
                    }

                    val newOrderId = UUID.randomUUID().toString()
                    val newOrder = Order(
                        id = newOrderId,
                        userId = userId,
                        subtotal = state.subtotal,
                        shippingCost = state.shippingCost,
                        total = state.total
                    )
                    
                    val orderItems = currentCartItems.map { (product, quantity) ->
                        OrderItem(orderId = newOrderId, productCode = product.code, quantity = quantity)
                    }

                    orderRepository.addOrder(newOrder, orderItems)

                    currentCartItems.forEach { (product, quantity) ->
                        val newStock = product.quantity - quantity
                        productRepository.update(product.copy(quantity = newStock))
                    }

                    state.selectedReward?.let { 
                        userRewardRepository.delete(UserReward(userId, it.id)) 
                    }

                    val pointsEarned = (state.subtotal / 1000).toInt() * 10
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(points = currentUser.points + pointsEarned)
                        userRepository.update(updatedUser)
                    }

                    onCartCleared()
                    _uiState.update { it.copy(isLoading = false, orderPlacedId = newOrderId, pointsEarned = pointsEarned) }
                    
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
}
