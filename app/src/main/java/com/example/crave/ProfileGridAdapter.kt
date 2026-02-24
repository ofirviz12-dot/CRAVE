package com.example.crave

import android.util.Base64
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // חובה לייבא את זה

class ProfileGridAdapter(
    private val posts: List<Post>,
    private val onPostClicked: (Post) -> Unit
) : RecyclerView.Adapter<ProfileGridAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setPadding(4, 4, 4, 4)
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        if (post.imageUrl.isNotEmpty()) {
            try {
                if (post.imageUrl.startsWith("http")) {
                    Glide.with(context)
                        .load(post.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.imageView)
                } else {
                    val imageBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                    Glide.with(context)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(holder.imageView)
                }
            } catch (e: Exception) {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onPostClicked(post)
        }
    }

    override fun getItemCount() = posts.size
}