package com.example.crave.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crave.R
import com.example.crave.Restaurant
import com.example.crave.RestaurantAdapter
import com.example.crave.databinding.FragmentRestaurantBinding
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantFragment : Fragment(R.layout.fragment_restaurant) {

    private var _binding: FragmentRestaurantBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: RestaurantAdapter

    private val originalRestaurantList = mutableListOf<Restaurant>()
    private val displayRestaurantList = mutableListOf<Restaurant>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRestaurantBinding.bind(view)

        binding.etSearch.clearFocus()

        binding.rvRestaurants.layoutManager = LinearLayoutManager(requireContext())
        adapter = RestaurantAdapter(displayRestaurantList)
        binding.rvRestaurants.adapter = adapter

        setupSearch()
        fetchRestaurantsFromFirebase()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterRestaurants(s.toString())
            }
        })
    }

    private fun filterRestaurants(query: String) {
        displayRestaurantList.clear()

        if (query.isEmpty()) {
            displayRestaurantList.addAll(originalRestaurantList)
        } else {
            val filteredList = originalRestaurantList.filter { restaurant ->
                restaurant.name.contains(query, ignoreCase = true)
            }
            displayRestaurantList.addAll(filteredList)
        }

        if (displayRestaurantList.isEmpty()) {
            binding.tvNoResults.visibility = View.VISIBLE
            binding.rvRestaurants.visibility = View.GONE
        } else {
            binding.tvNoResults.visibility = View.GONE
            binding.rvRestaurants.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
    }

    private fun fetchRestaurantsFromFirebase() {
        db.collection("restaurants")
            .get()
            .addOnSuccessListener { documents ->
                if (_binding == null) return@addOnSuccessListener
                originalRestaurantList.clear()
                displayRestaurantList.clear()

                for (document in documents) {
                    val rest = document.toObject(Restaurant::class.java).copy(id = document.id)
                    originalRestaurantList.add(rest)
                    displayRestaurantList.add(rest)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting restaurants", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}