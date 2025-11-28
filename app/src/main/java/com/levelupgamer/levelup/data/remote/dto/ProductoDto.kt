package com.levelupgamer.levelup.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductoDto(
    val id: Int,
    val nombre: String,
    val precio: String,
    val descripcion: String,
    val imagen: String,
    val stock: Int,
    @SerializedName("categoria_nombre")
    val categoriaNombre: String
)
