package com.example.crave

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crave.databinding.ActivityRestaurantProfileBinding
import com.example.crave.ui.AddPostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        val restId = intent.getStringExtra("restId") ?: ""
        val openTime = intent.getStringExtra("openTime") ?: ""
        val closeTime = intent.getStringExtra("closeTime") ?: ""
        val isOpen = isRestaurantOpenProfile(openTime, closeTime)

        if (isOpen) {
            binding.tvStatus.text = "Open now"
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#6DBF82"))
        } else {
            binding.tvStatus.text = "Closed"
            binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"))
        }

        binding.tvProfileName.text = restName
        binding.tvProfileCuisine.text = "$restCategory • $restAddress"

        loadImage(restImage)
        setupRecyclerView(restName)

        android.util.Log.d("MENU_TEST", "The received Restaurant ID is: $restId")

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnMenu.setOnClickListener {
            showMenuBottomSheet(restId)
        }

        binding.AddPost.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openAddPost", true)
            intent.putExtra("PREFILLED_RESTAURANT_NAME", restName)
            startActivity(intent)
        }
    }

    private fun loadImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            try {
                if (imageUrl.startsWith("http")) {
                    Glide.with(this).load(imageUrl).into(binding.ivHeaderImage)
                } else {
                    val cleanBase64 = if (imageUrl.contains("base64,")) {
                        imageUrl.substringAfter("base64,")
                    } else {
                        imageUrl
                    }

                    val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
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
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    postList.add(post)
                }

                val adapter = ProfileGridAdapter(postList) { selectedPost ->
                    val intent = Intent(this, PostDetailActivity::class.java)
                    intent.putExtra("userName", selectedPost.userName)
                    intent.putExtra("description", selectedPost.caption)
                    intent.putExtra("imageUrl", selectedPost.imageUrl)
                    intent.putExtra("postId", selectedPost.id)
                    intent.putExtra("restaurantName", selectedPost.restaurantName)
                    intent.putExtra("userId", selectedPost.userId)
                    intent.putExtra("userAvatar", selectedPost.userAvatar)
                    intent.putExtra("timeAgo", selectedPost.timeAgo)

                    startActivity(intent)
                }
                binding.rvUserPhotos.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not load photos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMenuBottomSheet(restaurantId: String) {
        if (restaurantId.isEmpty()) {
            android.util.Log.e("MENU_TEST", "Restaurant ID is empty! Cannot fetch menu.")
            return
        }

        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)

        val rvMenu = view.findViewById<RecyclerView>(R.id.rvBottomSheetMenu)
        rvMenu.layoutManager = LinearLayoutManager(this)

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        db.collection("restaurants").document(restaurantId).collection("menu")
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("MENU_TEST", "Number of menu items found: ${documents.size()}")

                val menuList = mutableListOf<MenuItem>()
                for (document in documents) {
                    val item = document.toObject(MenuItem::class.java).copy(id = document.id)
                    menuList.add(item)
                }

                val menuAdapter = MenuAdapter(menuList)
                rvMenu.adapter = menuAdapter
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("MENU_TEST", "Error loading menu", exception)
                Toast.makeText(this, "error loading menu", Toast.LENGTH_SHORT).show()
            }
    }
    private fun isRestaurantOpenProfile(openTime: String, closeTime: String): Boolean {
        if (openTime.isBlank() || closeTime.isBlank()) return false
        try {
            val calendar = java.util.Calendar.getInstance()
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentTimeInMinutes = (currentHour * 60) + currentMinute

            val cleanOpen = openTime.trim()
            val cleanClose = closeTime.trim()

            val openParts = cleanOpen.split(":")
            val openInMinutes = (openParts[0].toInt() * 60) + openParts[1].toInt()

            val closeParts = cleanClose.split(":")
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
}