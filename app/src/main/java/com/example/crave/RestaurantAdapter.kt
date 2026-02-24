package com.example.crave

import android.content.Intent
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crave.RestaurantProfileActivity
class RestaurantAdapter(private val restaurants: List<Restaurant>) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivRestImage: ImageView = itemView.findViewById(R.id.ivRestImage)
        val tvRestName: TextView = itemView.findViewById(R.id.tvRestName)
        val tvCuisine: TextView = itemView.findViewById(R.id.tvCuisine)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        val context = holder.itemView.context

        holder.tvRestName.text = restaurant.name
        holder.tvCuisine.text = restaurant.category

        holder.tvDistance.text = restaurant.address

        if (restaurant.imageUrl.isNotEmpty()) {
            try {
                if (restaurant.imageUrl.startsWith("http")) {
                    Glide.with(context)
                        .load(restaurant.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.ivRestImage)
                } else {
                    val imageBytes = Base64.decode(restaurant.imageUrl, Base64.DEFAULT)
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

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RestaurantProfileActivity::class.java)

            intent.putExtra("restId", restaurant.id)
            intent.putExtra("restName", restaurant.name)
            intent.putExtra("restCategory", restaurant.category)
            intent.putExtra("restAddress", restaurant.address)
            intent.putExtra("restImage", restaurant.imageUrl)
            intent.putExtra("restRating", restaurant.rating)
            intent.putExtra("restDescription", restaurant.description)

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = restaurants.size
}