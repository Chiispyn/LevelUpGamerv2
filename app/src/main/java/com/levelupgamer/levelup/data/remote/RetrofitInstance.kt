package com.levelupgamer.levelup.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    internal const val BASE_URL = "https://api-dfs2-dm-production.up.railway.app/"

    val api: GamingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GamingApi::class.java)
    }
}
