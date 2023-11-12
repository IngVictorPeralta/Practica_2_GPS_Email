package com.vhpg.practica2gpsemail.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vhpg.practica2gpsemail.R
import com.vhpg.practica2gpsemail.data.remote.ProductDetailDto
import com.vhpg.practica2gpsemail.data.remote.ProductDto
import com.vhpg.practica2gpsemail.data.remote.ProductDtoItem
import com.vhpg.practica2gpsemail.databinding.ProductElementBinding


class ProductsAdapter(
    private val products: ProductDto,
    private val onProductClicked: (ProductDtoItem) -> Unit
):RecyclerView.Adapter<ProductsAdapter.ViewHolder>(){
    class ViewHolder(private val binding: ProductElementBinding):RecyclerView.ViewHolder(binding.root){

        val ivFoto = binding.ivIcon
        val ivCat = binding.ivIconCat
        fun bind(product:ProductDtoItem){
            val imageResource = when (product.category) {
                0-> R.drawable.cat0
                1 -> R.drawable.cat1
                2 -> R.drawable.cat2
                3 -> R.drawable.cat3
                4 -> R.drawable.cat4
                5 -> R.drawable.cat5
                6 -> R.drawable.cat6
                7 -> R.drawable.cat7
                else -> R.drawable.cat0
            }


            binding.apply {
                tvName.text = product.name
                tvStock.text = product.stock.toString()
                tvPrice.text = "$ ${product.price.toString()}"
                ivCat.setImageResource(imageResource)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductElementBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val product = products[position]



        holder.bind(product)

        //Con Picasso
        /*
        Picasso.get()
            .load(game.thumbnail)
            .into(holder.ivThumbnal)
        */

        Glide.with(holder.itemView.context)
            .load(product.url)
            .into(holder.ivFoto)

        holder.itemView.setOnClickListener{
            onProductClicked(product)
        }
    }
}