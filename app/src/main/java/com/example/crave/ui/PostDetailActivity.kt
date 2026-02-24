package com.example.crave

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.crave.databinding.ActivityPostDetailBinding

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName") ?: "Unknown"
        val description = intent.getStringExtra("description") ?: ""
        val dishName = intent.getStringExtra("dishName") ?: "My Crave"
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val likes = intent.getIntExtra("likes", 0)

        binding.tvUserName.text = userName
        binding.tvReviewText.text = description
        binding.tvDishName.text = dishName
        //add:
        //binding.tvLikes.text = "$likes Likes"

        if (imageUrl.isNotEmpty()) {
            try {
                if (imageUrl.startsWith("http")) {
                    Glide.with(this).load(imageUrl).into(binding.ivPostImage)
                } else {
                    val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                    Glide.with(this)
                        .asBitmap()
                        .load(imageBytes)
                        .into(binding.ivPostImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}