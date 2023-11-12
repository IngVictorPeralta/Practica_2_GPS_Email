package com.vhpg.practica2gpsemail.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductsApi
{
    @GET("products/products_list")
    fun getProductsApiary(): Call<ProductDto>

    //games/game_detail/21357
    @GET("products/product_detail/{id}")
    fun getProductDetailApiary(
        @Path("id") id: String?
    ) : Call<ProductDetailDto>
}