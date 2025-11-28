package com.levelupgamer.levelup.data.remote

import com.levelupgamer.levelup.data.remote.dto.ProductoDto
import retrofit2.http.GET

interface GamingApi {

    @GET("api/gaming/productos")
    suspend fun getProductos(): List<ProductoDto>

}
