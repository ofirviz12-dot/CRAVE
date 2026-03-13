package com.example.crave

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class FeedAdapter(
    private var posts: List<Post>,
    private val onPostClicked: (Post) -> Unit,
    private val onLikeClicked: (Post, Boolean) -> Unit,
    private val onCommentClicked: (Post) -> Unit,
    private val onNutritionClicked: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserAvatar: ImageView = itemView.findViewById(R.id.ivUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvRestaurantName: TextView = itemView.findViewById(R.id.tvRestaurantName)
        val tvTimeAgo: TextView = itemView.findViewById(R.id.tvTimeAgo)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvLikesCount: TextView = itemView.findViewById(R.id.tvLikesCount)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.lottieAnimation)
        val btnComment: ImageView = itemView.findViewById(R.id.btnComment)
        val tvCommentsCount: TextView = itemView.findViewById(R.id.tvCommentsCount)
        val btnNutrition: ImageView = itemView.findViewById(R.id.btnNutrition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        holder.tvUserName.text = post.userName
        holder.tvCaption.text = post.caption
        holder.tvTimeAgo.text = if (post.timeAgo.isNotEmpty()) post.timeAgo else "Just now"

        if (post.restaurantName.isNotEmpty()) {
            holder.tvRestaurantName.text = post.restaurantName
            holder.tvRestaurantName.visibility = View.VISIBLE
        } else {
            holder.tvRestaurantName.visibility = View.GONE
        }

        if (post.imageUrl.isNotEmpty()) {
            try {
                if (post.imageUrl.startsWith("http")) {
                    Glide.with(context).load(post.imageUrl).into(holder.ivPostImage)
                } else {
                    val imageBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                    Glide.with(context).asBitmap().load(imageBytes).into(holder.ivPostImage)
                }
            } catch (e: Exception) {
                holder.ivPostImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        val isLikedByMe = post.likedBy.contains(currentUserId)
        holder.tvLikesCount.text = post.likedBy.size.toString()
        holder.tvCommentsCount.text = post.commentsCount.toString()

        if (!holder.lottieAnimation.isAnimating) {
            if (isLikedByMe) {
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#E91E63"))
            } else {
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#333333"))
            }
        }

        if (post.hasFoodAnalysis) {
            holder.btnNutrition.visibility = View.VISIBLE
            holder.btnNutrition.setOnClickListener {
                onNutritionClicked(post)
            }
        } else {
            holder.btnNutrition.visibility = View.GONE
        }

        holder.btnLike.setOnClickListener {
            if (holder.lottieAnimation.isAnimating) return@setOnClickListener

            if (!isLikedByMe) {
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#E91E63"))
                holder.lottieAnimation.playAnimation()
                onLikeClicked(post, true)
            } else {
                holder.btnLike.setColorFilter(android.graphics.Color.parseColor("#333333"))
                onLikeClicked(post, false)
            }
        }

        holder.btnComment.setOnClickListener {
            onCommentClicked(post)
        }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize() = posts.size
            override fun getNewListSize() = newPosts.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = posts[oldPos].id == newPosts[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = posts[oldPos] == newPosts[newPos]
        }
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        posts = newPosts
        diffResult.dispatchUpdatesTo(this)
    }
}