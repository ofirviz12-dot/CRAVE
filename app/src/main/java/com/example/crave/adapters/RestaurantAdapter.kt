package com.example.crave.adapters

import android.content.Intent
import android.graphics.Color
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crave.R
import com.example.crave.RestaurantProfileActivity
import com.example.crave.models.Restaurant
import java.util.Calendar

class RestaurantAdapter(private val restaurants: List<Restaurant>) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRestImage: ImageView = itemView.findViewById(R.id.ivRestImage)
        val tvRestName: TextView = itemView.findViewById(R.id.tvRestName)
        val tvCuisine: TextView = itemView.findViewById(R.id.tvCuisine)
        val tvRating: TextView? = itemView.findViewById(R.id.tvRating)
        val tvStatus: TextView? =itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        val context = holder.itemView.context
        val isOpen = isRestaurantOpen(restaurant.openTime, restaurant.closeTime)

        holder.tvRestName.text = restaurant.name
        holder.tvCuisine.text = restaurant.description


        holder.tvRating?.text = restaurant.rating.toString()

        if (restaurant.imageUrl.isNotEmpty()) {
            try {
                if (restaurant.imageUrl.startsWith("http")) {
                    Glide.with(context)
                        .load(restaurant.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivRestImage)
                } else {
                    val cleanBase64 = if (restaurant.imageUrl.contains("base64,")) {
                        restaurant.imageUrl.substringAfter("base64,")
                    } else {
                        restaurant.imageUrl
                    }

                    val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    Glide.with(context)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivRestImage)
                }
            } catch (e: Exception) {
                holder.ivRestImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        } else {
            holder.ivRestImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        if (isOpen) {
            holder.tvStatus?.text = "OPEN NOW"
            holder.tvStatus?.setTextColor(Color.parseColor("#7CB98A"))
        } else {
            holder.tvStatus?.text = "CLOSED"
            holder.tvStatus?.setTextColor(Color.parseColor("#F44336"))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RestaurantProfileActivity::class.java)

            intent.putExtra("restId", restaurant.id)
            intent.putExtra("restName", restaurant.name)
            intent.putExtra("restCategory", restaurant.category)
            intent.putExtra("restAddress", restaurant.address)
            intent.putExtra("restImage", restaurant.imageUrl)
            intent.putExtra("restRating", restaurant.rating)
            intent.putExtra("restDescription", restaurant.description)
            intent.putExtra("openTime", restaurant.openTime)
            intent.putExtra("closeTime", restaurant.closeTime)

            context.startActivity(intent)
        }
    }
    private fun isRestaurantOpen(openTime: String, closeTime: String): Boolean {
        if (openTime.isEmpty() || closeTime.isEmpty()) return false

        try {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentTimeInMinutes = (currentHour * 60) + currentMinute

            val openParts = openTime.split(":")
            val openInMinutes = (openParts[0].toInt() * 60) + openParts[1].toInt()

            val closeParts = closeTime.split(":")
            val closeInMinutes = (closeParts[0].toInt() * 60) + closeParts[1].toInt()

            return if (closeInMinutes > openInMinutes) {
                currentTimeInMinutes in openInMinutes..closeInMinutes
            } else {
                currentTimeInMinutes >= openInMinutes || currentTimeInMinutes <= closeInMinutes
            }
        } catch (e: Exception) {
            return false
        }
    }

    override fun getItemCount() = restaurants.size
}