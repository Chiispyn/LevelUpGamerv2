package com.levelupgamer.levelup.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val userId: Int,
    val date: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val shippingCost: Double,
    val total: Double,
    val status: String = OrderStatus.PROCESANDO.name
)
