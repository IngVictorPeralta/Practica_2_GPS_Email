package com.vhpg.practica2gpsemail.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vhpg.practica2gpsemail.R
import com.vhpg.practica2gpsemail.data.ProductRepository
import com.vhpg.practica2gpsemail.databinding.ActivityMainBinding
import com.vhpg.practica2gpsemail.ui.fragments.ProductsListFragment
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var repository: ProductRepository
    private lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Practica2GPSEmail)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductsListFragment())
                .commit()
        }
    }
}