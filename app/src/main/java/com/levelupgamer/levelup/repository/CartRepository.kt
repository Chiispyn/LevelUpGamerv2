package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.CartDao
import com.levelupgamer.levelup.model.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartDao: CartDao) {

    fun getCartItems(userId: Int): Flow<List<CartItem>> {
        return cartDao.getCartItems(userId)
    }

    suspend fun addToCart(userId: Int, productCode: String, quantity: Int) {
        val existingItem = cartDao.getCartItem(userId, productCode)
        if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
            cartDao.update(updatedItem)
        } else {
            val newItem = CartItem(userId = userId, productCode = productCode, quantity = quantity)
            cartDao.insert(newItem)
        }
    }

    suspend fun removeFromCart(userId: Int, productCode: String) {
        val item = cartDao.getCartItem(userId, productCode)
        if (item != null) {
            cartDao.delete(item)
        }
    }

    suspend fun clearCart(userId: Int) {
        cartDao.clearCart(userId)
    }

    suspend fun updateQuantity(userId: Int, productCode: String, newQuantity: Int) {
        cartDao.updateQuantity(userId, productCode, newQuantity)
    }
}
