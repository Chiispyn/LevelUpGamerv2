package com.levelupgamer.levelup.data.repository

import com.levelupgamer.levelup.data.local.dao.ProductDao
import com.levelupgamer.levelup.data.remote.GamingApi
import com.levelupgamer.levelup.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao, private val gamingApi: GamingApi) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    suspend fun refreshProducts() {
        try {
            val remoteProducts = gamingApi.getProductos()
            val productEntities = remoteProducts.map {
                Product(
                    code = it.id.toString(),
                    name = it.nombre,
                    price = it.precio.toDouble().toInt(),
                    description = it.descripcion,
                    imageUrl = it.imagen, // La API ya devuelve la URL completa
                    category = it.categoriaNombre,
                    quantity = it.stock,
                    imageResId = null, // MUY IMPORTANTE: Nulificar el ID de recurso local
                    averageRating = 0f
                )
            }
            productDao.insertAll(productEntities)
        } catch (e: Exception) {
            // Manejar el error
        }
    }

    suspend fun getProductByCode(code: String): Product? {
        return productDao.getProductByCode(code)
    }

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }
}
