package com.example.crave.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.R
import com.example.crave.Restaurant
import com.example.crave.RestaurantAdapter
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantFragment : Fragment(R.layout.fragment_restaurant) {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvRestaurants: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private val restaurantList = mutableListOf<Restaurant>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvRestaurants = view.findViewById(R.id.rvRestaurants)
        rvRestaurants.layoutManager = LinearLayoutManager(requireContext())

        adapter = RestaurantAdapter(restaurantList)
        rvRestaurants.adapter = adapter

        fetchRestaurantsFromFirebase()
    }

    private fun fetchRestaurantsFromFirebase() {
        db.collection("restaurants")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FIREBASE_TEST", "Number of restaurants found: ${documents.size()}")
                restaurantList.clear()
                for (document in documents) {
                    Log.d("FIREBASE_TEST", "Restaurant data: ${document.data}")
                    val rest = document.toObject(Restaurant::class.java).copy(id = document.id)
                    restaurantList.add(rest)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting restaurants", exception)
            }
    }
}