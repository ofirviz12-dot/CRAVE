package com.example.crave.adapters

import android.R
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.models.Post
import com.example.crave.utils.loadImage

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
        holder.imageView.loadImage(post.imageUrl, isCircular = false, fallbackResId = R.drawable.ic_menu_gallery)

        holder.itemView.setOnClickListener {
            onPostClicked(post)
        }



        holder.itemView.setOnClickListener {
            onPostClicked(post)
        }
    }

    override fun getItemCount() = posts.size
}