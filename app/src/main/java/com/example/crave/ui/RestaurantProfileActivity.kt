package com.example.crave

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.crave.databinding.ActivityRestaurantProfileBinding // וודאי שהשם תואם לשם קובץ ה-XML שלך
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantProfileBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRestaurantProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val restName = intent.getStringExtra("restName") ?: "Unknown"
        val restCategory = intent.getStringExtra("restCategory") ?: "Food"
        val restAddress = intent.getStringExtra("restAddress") ?: ""
        val restImage = intent.getStringExtra("restImage") ?: ""

        binding.tvProfileName.text = restName
        binding.tvProfileCuisine.text = "$restCategory • $restAddress"

        loadImage(restImage)

        setupRecyclerView(restName)

        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.fabAddPost.setOnClickListener {
            Toast.makeText(this, "Add a review for $restName", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, MainActivity::class.java)
            // intent.putExtra("openAddPost", true)
            // startActivity(intent)
        }
    }

    private fun loadImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            try {
                if (imageUrl.startsWith("http")) {
                    Glide.with(this).load(imageUrl).into(binding.ivHeaderImage)
                } else {
                    val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(imageBytes).into(binding.ivHeaderImage)
                }
            } catch (e: Exception) {
                binding.ivHeaderImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun setupRecyclerView(restaurantName: String) {
        binding.rvUserPhotos.layoutManager = GridLayoutManager(this, 3)

        db.collection("posts")
            .whereEqualTo("restaurantName", restaurantName)
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }

                val adapter = ProfileGridAdapter(postList) { selectedPost ->
                    val intent = Intent(this, PostDetailActivity::class.java)
                    intent.putExtra("userName", selectedPost.userName)
                    intent.putExtra("description", selectedPost.caption)
                    intent.putExtra("imageUrl", selectedPost.imageUrl)
                    intent.putExtra("likes", selectedPost.likes)
                    startActivity(intent)
                }
                binding.rvUserPhotos.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load photos", Toast.LENGTH_SHORT).show()
            }
    }
}