package com.levelupgamer.levelup.ui.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.CartRepository
import com.levelupgamer.levelup.repository.ProductRepository
import com.levelupgamer.levelup.model.Product
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartScreenUiState(
    val cartItems: List<Pair<Product, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CartViewModel(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartScreenUiState())
    val uiState: StateFlow<CartScreenUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId == null) {
                _uiState.value = CartScreenUiState(error = "Usuario no encontrado", isLoading = false)
                return@launch
            }

            cartRepository.getCartItems(userId)
                .combine(productRepository.getAllProducts()) { cartItems, products ->
                    val productMap = products.associateBy { it.code }
                    val cartProductPairs = cartItems.mapNotNull { cartItem ->
                        productMap[cartItem.productCode]?.let { product ->
                            Pair(product, cartItem.quantity)
                        }
                    }
                    CartScreenUiState(cartItems = cartProductPairs, isLoading = false)
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onProductAdded(productCode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByCode(productCode)
            val userId = UserManager.getLoggedInUserId(context)
            if (product != null && userId != null) {
                val currentCartItem = uiState.value.cartItems.find { it.first.code == productCode }
                val currentQuantity = currentCartItem?.second ?: 0
                if (product.quantity <= 0) {
                    _toastMessage.emit("Este producto no tiene stock")
                } else if (currentQuantity < product.quantity) {
                    cartRepository.addToCart(userId, productCode, 1)
                } else {
                    _toastMessage.emit("Has alcanzado el stock máximo para este producto")
                }
            }
        }
    }

    fun onProductRemoved(productCode: String) {
        viewModelScope.launch {
            UserManager.getLoggedInUserId(context)?.let {
                cartRepository.removeFromCart(it, productCode)
            }
        }
    }

    fun onQuantityChanged(productCode: String, newQuantity: Int) {
        viewModelScope.launch {
            val product = productRepository.getProductByCode(productCode)
            val userId = UserManager.getLoggedInUserId(context)
            if (product != null && userId != null) {
                if (newQuantity > 0 && newQuantity <= product.quantity) {
                    cartRepository.updateQuantity(userId, productCode, newQuantity)
                } else if (newQuantity <= 0) {
                    cartRepository.removeFromCart(userId, productCode)
                } else {
                     _toastMessage.emit("No puedes añadir más, stock máximo alcanzado.")
                }
            }
        }
    }

    fun onCartCleared() {
        viewModelScope.launch {
            UserManager.getLoggedInUserId(context)?.let {
                cartRepository.clearCart(it)
            }
        }
    }
}
