package com.vhpg.practica2gpsemail.data.remote

import com.google.gson.annotations.SerializedName

data class ProductDtoItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("category")
    val category: Int,
    @SerializedName("spot")
    val spot: String,

)