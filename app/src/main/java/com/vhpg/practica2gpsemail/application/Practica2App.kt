package com.vhpg.practica2gpsemail.application

import android.app.Application
import com.vhpg.practica2gpsemail.data.ProductRepository
import com.vhpg.practica2gpsemail.data.remote.RetrofitHelper

class Practica2App : Application(){

    private val retrofit by lazy{
        RetrofitHelper().getRetrofit()
    }

    val repository by lazy{
        ProductRepository(retrofit)
    }
}