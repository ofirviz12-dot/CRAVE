package com.example.crave

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.adapters.MenuAdapter
import com.example.crave.adapters.ProfileGridAdapter
import com.example.crave.databinding.ActivityRestaurantProfileBinding
import com.example.crave.models.MenuItem
import com.example.crave.models.Post
import com.example.crave.utils.loadImage
import com.example.crave.utils.showCustomPopup
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

        binding.ivHeaderImage.loadImage(restImage, isCircular = false, fallbackResId = android.R.drawable.ic_menu_gallery)
        setupRecyclerView(restName)


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


    private fun setupRecyclerView(restaurantName: String) {
        binding.rvUserPhotos.layoutManager = GridLayoutManager(this, 3)

        val formattedName = restaurantName.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    if (post.restaurantName.equals(restaurantName, ignoreCase = true)) {
                    postList.add(post)
                    }
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
                showCustomPopup("Could not load photos")
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
                showCustomPopup("error loading menu")
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