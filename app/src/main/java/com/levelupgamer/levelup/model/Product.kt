package com.levelupgamer.levelup.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val code: String,
    val name: String,
    val category: String,
    val price: Int,
    val description: String = "",
    val quantity: Int = 1,
    val imageUrl: String? = null,      // URL para imágenes de la API
    @DrawableRes val imageResId: Int? = null, // ID para imágenes locales (ahora persistido y anulable)
    val averageRating: Float = 0f
)
