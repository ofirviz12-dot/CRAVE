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

        val targetUserId = arguments?.getString("TARGET_USER_ID")
        val currentUserId = auth.currentUser?.uid

        val userIdToShow = targetUserId ?: currentUserId

        if (userIdToShow != null) {
            loadUserProfile(userIdToShow)

            if (userIdToShow == currentUserId) {
                binding.btnEditProfile.visibility = View.VISIBLE
                binding.btnEditProfile.setOnClickListener {
                    val intent = Intent(context, EditProfileActivity::class.java)
                    startActivity(intent)
                }
            } else {
                binding.btnEditProfile.visibility = View.GONE
            }

        } else {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile(userId: String) {

        db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val userPostsList = mutableListOf<Post>()
                var totalLikes = 0

                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    userPostsList.add(post)
                    totalLikes += post.likedBy.size
                }

                userPostsList.sortByDescending { it.timestamp }

                binding.tvPostsCount.text = userPostsList.size.toString()
                binding.tvLikesCount.text = totalLikes.toString()

                setupGrid(userPostsList)

                if (userPostsList.isNotEmpty()) {
                    val firstPost = userPostsList.first()
                    binding.tvUsername.text = firstPost.userName
                    if (firstPost.userAvatar.isNotEmpty() && isAdded && context != null) {
                        Glide.with(this).load(firstPost.userAvatar).circleCrop().into(binding.ivUserProfile)
                    }
                } else if (binding.tvUsername.text.isEmpty()) {
                    binding.tvUsername.text = "Crave User"
                }
            }
            .addOnFailureListener {
                if (isAdded && context != null) {
                    Toast.makeText(context, "Could not load posts", Toast.LENGTH_SHORT).show()
                }
            }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val bio = document.getString("bio") ?: "Foodie & App Developer \uD83D\uDCF1"
                    binding.tvBio.text = bio

                    val realName = document.getString("name")
                    if (!realName.isNullOrEmpty()) {
                        binding.tvUsername.text = realName
                    }

                    val profileImageStr = document.getString("profileImage") ?: ""
                    if (profileImageStr.isNotEmpty()) {
                        try {
                            if (profileImageStr.startsWith("http")) {
                                if (isAdded && context != null) {
                                    Glide.with(this).load(profileImageStr).circleCrop().into(binding.ivUserProfile)
                                }
                            } else {
                                val imageBytes = Base64.decode(profileImageStr, Base64.DEFAULT)
                                if (isAdded && context != null) {
                                    Glide.with(this).asBitmap().load(imageBytes).circleCrop().into(binding.ivUserProfile)
                                }
                            }
                        } catch (e: Exception) {
                            loadDefaultProfileImage()
                        }
                    } else {
                        loadDefaultProfileImage()
                    }
                } else {
                    binding.tvBio.text = "Food Lover \uD83C\uDF54"
                    loadDefaultProfileImage()
                }
            }
    }

    private fun setupGrid(posts: List<Post>) {
        if (!isAdded || context == null) return
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
    private fun loadDefaultProfileImage() {
        if (!isAdded || context == null) return
        val defaultAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
        Glide.with(this).load(defaultAvatar).circleCrop().into(binding.ivUserProfile)
    }
}