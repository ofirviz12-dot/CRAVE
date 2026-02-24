package com.example.crave.ui

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.crave.EditProfileActivity
import com.example.crave.Post
import com.example.crave.PostDetailActivity
import com.example.crave.ProfileGridAdapter
import com.example.crave.R
import com.example.crave.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupUserProfile()
        loadMyPosts()

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserProfile()
    }

    private fun setupUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            binding.tvUsername.text = user.displayName ?: "Crave User"

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val bio = document.getString("bio") ?: "Foodie & App Developer \uD83D\uDCF1"
                        binding.tvBio.text = bio

                        val base64Image = document.getString("profileImage") ?: ""
                        if (base64Image.isNotEmpty()) {
                            try {
                                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                                if (isAdded && context != null) {
                                    Glide.with(this).asBitmap().load(imageBytes).circleCrop().into(binding.ivUserProfile)
                                }
                            } catch (e: Exception) {
                                loadDefaultProfileImage()
                            }
                        } else {
                            loadDefaultProfileImage()
                        }
                    } else {
                        loadDefaultProfileImage()
                    }
                }
                .addOnFailureListener {
                    loadDefaultProfileImage()
                }
        }
    }

    private fun loadDefaultProfileImage() {
        if (!isAdded || context == null) return
        val defaultAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
        Glide.with(this).load(defaultAvatar).circleCrop().into(binding.ivUserProfile)
    }

    private fun loadMyPosts() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val myPostsList = mutableListOf<Post>()
                var totalLikes = 0

                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    myPostsList.add(post)
                    totalLikes += post.likedBy.size
                }

                myPostsList.sortByDescending { it.timestamp }

                binding.tvPostsCount.text = myPostsList.size.toString()
                binding.tvLikesCount.text = totalLikes.toString()

                setupGrid(myPostsList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Could not load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupGrid(posts: List<Post>) {
        binding.rvMyPosts.layoutManager = GridLayoutManager(context, 3)
        binding.rvMyPosts.adapter = ProfileGridAdapter(posts) { clickedPost ->
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("userName", clickedPost.userName)
            intent.putExtra("description", clickedPost.caption)
            intent.putExtra("imageUrl", clickedPost.imageUrl)
            intent.putExtra("likes", clickedPost.likedBy.size)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}