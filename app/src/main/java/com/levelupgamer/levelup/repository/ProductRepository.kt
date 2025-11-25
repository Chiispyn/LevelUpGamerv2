package com.levelupgamer.levelup.repository

import com.levelupgamer.levelup.data.local.dao.ProductDao
import com.levelupgamer.levelup.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
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
