package com.vhpg.practica2gpsemail.data

import com.vhpg.practica2gpsemail.data.remote.ProductDetailDto
import com.vhpg.practica2gpsemail.data.remote.ProductDto
import com.vhpg.practica2gpsemail.data.remote.ProductsApi
import retrofit2.Call
import retrofit2.Retrofit

class ProductRepository(private val retrofit: Retrofit){
    private val productsApi: ProductsApi = retrofit.create(ProductsApi::class.java)

    fun getProducts(): Call<ProductDto> =
        productsApi.getProductsApiary()

    fun getProductsDetail(id: String?): Call<ProductDetailDto> =
        productsApi.getProductDetailApiary(id)

}