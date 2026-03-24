package com.example.crave

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(private val menuItems: List<MenuItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvItemDesc: TextView = itemView.findViewById(R.id.tvItemDesc)
        val tvItemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]

        holder.tvItemName.text = item.name
        holder.tvItemDesc.text = item.description

        val displayPrice = if (item.price % 1 == 0.0) {
            item.price.toInt().toString()
        } else {
            item.price.toString()
        }

        holder.tvItemPrice.text = "₪${displayPrice}"
    }

    override fun getItemCount() = menuItems.size
}