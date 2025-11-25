package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.OrderDao
import com.levelupgamer.levelup.model.Order
import com.levelupgamer.levelup.model.OrderItem
import com.levelupgamer.levelup.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    fun getOrdersForUser(userId: Int): Flow<List<OrderWithItems>> {
        return orderDao.getOrdersForUser(userId)
    }

    fun getAllOrders(): Flow<List<OrderWithItems>> {
        return orderDao.getAllOrders()
    }

    suspend fun getOrderById(orderId: String): OrderWithItems? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun addOrder(order: Order, items: List<OrderItem>) {
        orderDao.insertOrder(order)
        orderDao.insertOrderItems(items)
    }

    suspend fun update(order: Order) {
        orderDao.update(order)
    }
}
