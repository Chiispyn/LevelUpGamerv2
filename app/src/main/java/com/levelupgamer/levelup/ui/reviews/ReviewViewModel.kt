package com.levelupgamer.levelup.ui.reviews

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelupgamer.levelup.repository.ProductRepository
import com.levelupgamer.levelup.repository.ReviewRepository
import com.levelupgamer.levelup.repository.UserRepository
import com.levelupgamer.levelup.model.Review
import com.levelupgamer.levelup.util.UserManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f
)

class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    private val _toastChannel = Channel<String>()
    val toastMessage: Flow<String> = _toastChannel.receiveAsFlow()

    fun loadReviews(productCode: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsForProduct(productCode).collect { reviews ->
                val avg = if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else 0f
                _uiState.update { it.copy(reviews = reviews, averageRating = avg) }
            }
        }
    }

    fun addReview(productCode: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val userId = UserManager.getLoggedInUserId(context)
            if (userId == null || userId == -1) {
                _toastChannel.send("Debes iniciar sesión para dejar una review.")
                return@launch
            }

            val user = userRepository.getUserById(userId)
            if (user == null) {
                _toastChannel.send("Error al cargar los datos del usuario.")
                return@launch
            }

            if (user.isActive == false) {
                _toastChannel.send("Tu cuenta está suspendida y no puedes dejar reviews.")
                return@launch
            }

            val review = Review(
                userId = userId,
                productCode = productCode,
                userName = user.name, // Añadido el nombre del usuario
                rating = rating,
                comment = comment
            )
            reviewRepository.addReview(review)
            updateProductAverageRating(productCode)
        }
    }

    private suspend fun updateProductAverageRating(productCode: String) {
        val reviews = reviewRepository.getReviewsForProduct(productCode).first()
        val avg = if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else 0f
        val product = productRepository.getProductByCode(productCode)
        product?.let {
            productRepository.update(it.copy(averageRating = avg))
        }
    }
}
