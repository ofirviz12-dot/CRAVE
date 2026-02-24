package com.example.crave.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.R
import com.example.crave.Restaurant
import com.example.crave.RestaurantAdapter

class RestaurantFragment : Fragment(R.layout.fragment_restaurant) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurantList = listOf(
            Restaurant(
                id = "1",
                name = "Pasta Basta",
                category = "Italian",
                address = "Dizengoff 300, Tel Aviv",
                rating = 4.8,
                imageUrl = "https://images.unsplash.com/photo-1551183053-bf91a1d81141", // תמונה אמיתית מקישור
                description = "Fresh handmade pasta with variety of sauces."
            ),
            Restaurant(
                id = "2",
                name = "Burger King",
                category = "American",
                address = "Ibn Gabirol 70, Tel Aviv",
                rating = 4.5,
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd",
                description = "Flame-grilled burgers and fries."
            ),
            Restaurant(
                id = "3",
                name = "Sushi Rehavia",
                category = "Japanese",
                address = "Azrieli Center, Tel Aviv",
                rating = 4.9,
                imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
                description = "Authentic sushi and fresh fish dishes."
            ),
            Restaurant(
                id = "4",
                name = "Aroma Espresso Bar",
                category = "Coffee & Bakery",
                address = "Allenby 100, Tel Aviv",
                rating = 4.2,
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93",
                description = "Israel's favorite coffee chain with great sandwiches."
            )
        )

        val rvRestaurants = view.findViewById<RecyclerView>(R.id.rvRestaurants)

        if (rvRestaurants != null) {
            rvRestaurants.layoutManager = LinearLayoutManager(requireContext())
            rvRestaurants.adapter = RestaurantAdapter(restaurantList)
        }
    }
}