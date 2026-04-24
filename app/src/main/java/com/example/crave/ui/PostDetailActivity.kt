package com.example.crave

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.crave.databinding.ItemFeedPostBinding
import com.example.crave.models.Post
import com.example.crave.ui.CommentsBottomSheetFragment
import com.example.crave.ui.NutritionBottomSheetFragment
import com.example.crave.utils.loadImage
import com.example.crave.utils.showCustomPopup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ItemFeedPostBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ItemFeedPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postId = intent.getStringExtra("postId") ?: ""
        val userName = intent.getStringExtra("userName") ?: "Unknown"
        val description = intent.getStringExtra("description") ?: ""
        val restaurantName = intent.getStringExtra("restaurantName") ?: ""
        val formattedRestaurantName = restaurantName.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val postUserId = intent.getStringExtra("userId") ?: ""
        val userAvatar = intent.getStringExtra("userAvatar") ?: ""
        val timeAgo = intent.getStringExtra("timeAgo") ?: ""

        binding.tvUserName.text = userName
        binding.tvCaption.text = description
        binding.tvRestaurantName.text = formattedRestaurantName
        binding.ivUserAvatar.setOnClickListener {
            goToUserProfile(postUserId)
        }
        binding.tvUserName.setOnClickListener {
            goToUserProfile(postUserId)
        }
        binding.tvTimeAgo.text = if (timeAgo.isNotEmpty()) timeAgo else "Just now"

        binding.ivPostImage.loadImage(imageUrl, isCircular = false, fallbackResId = android.R.drawable.ic_menu_gallery)
        binding.ivUserAvatar.loadImage(userAvatar, isCircular = true, fallbackResId = R.drawable.person_ic)

        if (postId.isNotEmpty()) {

            binding.btnComment.setOnClickListener {
                val bottomSheet = CommentsBottomSheetFragment(postId)
                bottomSheet.show(supportFragmentManager, "CommentsBottomSheet")
            }

            db.collection("posts").document(postId).addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val post = snapshot.toObject(Post::class.java)

                if (post != null) {
                    val currentUserId = auth.currentUser?.uid ?: ""

                    binding.tvLikesCount.text = post.likedBy.size.toString()
                    binding.tvCommentsCount.text = post.commentsCount.toString()

                    if (post.likedBy.contains(currentUserId)) {
                        binding.btnLike.setImageResource(R.drawable.like_red_icon)
                    } else {
                        binding.btnLike.setImageResource(R.drawable.like_empty_icon)
                    }

                    binding.btnLike.setOnClickListener {
                        val postRef = db.collection("posts").document(postId)
                        if (post.likedBy.contains(currentUserId)) {
                            postRef.update("likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
                        } else {
                            postRef.update("likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
                            binding.lottieAnimation.visibility = View.VISIBLE
                            binding.lottieAnimation.playAnimation()
                        }
                    }
                    binding.btnNutrition.setOnClickListener {
                        val bottomSheet = NutritionBottomSheetFragment(post)
                        bottomSheet.show(supportFragmentManager, "NutritionBottomSheet")
                    }
                }
            }
        }
        binding.btnBack.visibility = View.VISIBLE

        binding.btnBack.setOnClickListener {
            finish()
        }

    }
    private fun goToUserProfile(userId: String) {
        if (userId.isNotEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openUserProfile", true)
            intent.putExtra("userId", userId)
            startActivity(intent)
        } else {
            showCustomPopup("ERROR")
        }
    }
}