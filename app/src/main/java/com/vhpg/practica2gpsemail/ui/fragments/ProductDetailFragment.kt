package com.vhpg.practica2gpsemail.ui.fragments

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vhpg.practica2gpsemail.R
import com.vhpg.practica2gpsemail.application.Practica2App
import com.vhpg.practica2gpsemail.data.ProductRepository
import com.vhpg.practica2gpsemail.data.remote.ProductDetailDto
import com.vhpg.practica2gpsemail.databinding.FragmentProductDetailBinding
import com.vhpg.practica2gpsemail.databinding.FragmentProductsListBinding
import com.vhpg.practica2gpsemail.util.Constants
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response

private const val PRODUCT_ID = "product_id"

class ProductDetailFragment : Fragment(), OnMapReadyCallback {

    private var productId: String? = null

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!



    private lateinit var repository: ProductRepository
    private lateinit var map: GoogleMap

    private var long :Double = 0.0
    private var lat: Double = 0.0
    private var title:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getString(PRODUCT_ID)
            var logMessage = getString(R.string.id_Recived)
            Log.d(Constants.LOGTAG, "$logMessage $productId")


            repository = (requireActivity().application as Practica2App).repository

            lifecycleScope.launch{
                productId?.let{id ->
                    val call: Call<ProductDetailDto> = repository.getProductsDetail(id)
                    call.enqueue(object: Callback<ProductDetailDto>{
                        override fun onResponse(
                            call: Call<ProductDetailDto>,
                            response: Response<ProductDetailDto>
                        ) {

                            val imageResource = when (response.body()?.category) {
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

                            val mc = MediaController(requireContext())

                            binding.apply {
                                pbLoading.visibility = View.GONE
                                tvTitle.text = response.body()?.name
                                ivIconCat.setImageResource(imageResource)
                                tvLongDesc.text = response.body()?.description
                                var priceResp = response.body()?.price
                                tvPrice.text = "$ ${priceResp.toString()}"
                                var costResp = response.body()?.cost
                                tvCost.text = "$ ${response.body()?.cost.toString()}"
                                var profitResp = priceResp!! - costResp!!
                                tvProfit.text = "$ ${profitResp.toString()}"
                                tvStock.text = response.body()?.stock.toString()
                                tvRestockDate.text = response.body()?.lastRestockDate.toString()
                                title = response.body()?.name!!
                                long = response.body()?.longitud!!
                                lat = response.body()?.latitud!!
                                Glide.with(requireContext())
                                    .load(response.body()?.url)
                                    .into(ivImage)
                                vvVideo.setVideoURI(Uri.parse(response.body()?.spot))
                                mc.setAnchorView(vvVideo)
                                vvVideo.setMediaController(mc)
                                vvVideo.start()
                            }

                            var mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                            mapFragment.getMapAsync(this@ProductDetailFragment)
                        }

                        override fun onFailure(call: Call<ProductDetailDto>, t: Throwable) {
                            binding.pbLoading.visibility = View.GONE
                            var errorMessage = getString(R.string.Error)
                            Toast.makeText(requireContext(), "$errorMessage : ${t.message}", Toast.LENGTH_SHORT).show()
                        }

                    })

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProductDetailBinding.inflate(inflater,container,false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {

        @JvmStatic
        fun newInstance(productId: String) =
            ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(PRODUCT_ID, productId)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createMarker()



    }

    private fun createMarker(){
        val coordinates = LatLng(lat,long)
        val marker = MarkerOptions()
            .position(coordinates)
            .title(title)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.cat0))

        map.addMarker(marker)

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates,18f),
            4000,
            null
        )
    }

}