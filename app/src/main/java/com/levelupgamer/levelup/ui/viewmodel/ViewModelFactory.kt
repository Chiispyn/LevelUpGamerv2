package com.levelupgamer.levelup.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.levelupgamer.levelup.MyApp
import com.levelupgamer.levelup.address.AddressViewModel
import com.levelupgamer.levelup.data.remote.RetrofitInstance
import com.levelupgamer.levelup.data.repository.*
import com.levelupgamer.levelup.ui.address.*
import com.levelupgamer.levelup.ui.auth.LoginViewModel
import com.levelupgamer.levelup.ui.auth.RegisterViewModel
import com.levelupgamer.levelup.ui.cart.CartViewModel
import com.levelupgamer.levelup.ui.reviews.ReviewViewModel
import com.levelupgamer.levelup.ui.rewards.RewardsViewModel
import com.levelupgamer.levelup.ui.rewardsshop.RewardsShopViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext as MyApp

        // Create a single instance of the repositories that are used in multiple ViewModels
        val productRepository = ProductRepository(app.database.productDao(), RetrofitInstance.api)
        val userRepository = UserRepository(app.database.userDao())

        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository, context) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                val cartRepository = CartRepository(app.database.cartDao())
                CartViewModel(cartRepository, productRepository, context) as T
            }
            modelClass.isAssignableFrom(ReviewViewModel::class.java) -> {
                val reviewRepository = ReviewRepository(app.database.reviewDao())
                ReviewViewModel(reviewRepository, productRepository, userRepository, context) as T
            }
            modelClass.isAssignableFrom(RewardsViewModel::class.java) -> {
                val eventRepository = EventRepository(app.database.eventDao())
                val userEventRepository = UserEventRepository(app.database.userEventDao())
                RewardsViewModel(eventRepository, userRepository, userEventRepository, context) as T
            }
            modelClass.isAssignableFrom(RewardsShopViewModel::class.java) -> {
                val rewardRepository = RewardRepository(app.database.rewardDao())
                val userRewardRepository = UserRewardRepository(app.database.userRewardDao())
                RewardsShopViewModel(rewardRepository, userRepository, userRewardRepository, context) as T
            }
            modelClass.isAssignableFrom(AddressViewModel::class.java) -> {
                val addressRepository = AddressRepository(app.database.addressDao())
                AddressViewModel(addressRepository, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}