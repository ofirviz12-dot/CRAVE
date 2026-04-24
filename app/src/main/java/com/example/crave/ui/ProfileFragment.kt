package com.example.crave.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.crave.ui.EditProfileActivity
import com.example.crave.models.Post
import com.example.crave.PostDetailActivity
import com.example.crave.adapters.ProfileGridAdapter
import com.example.crave.R
import com.example.crave.databinding.FragmentProfileBinding
import com.example.crave.utils.loadImage
import com.example.crave.utils.showCustomPopup
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

        val targetUserId = arguments?.getString("userId")
        val currentUserId = auth.currentUser?.uid

        val userIdToShow = targetUserId ?: currentUserId

        if (userIdToShow != null) {
            loadUserProfile(userIdToShow)

            if (userIdToShow == currentUserId) {
                binding.btnEditProfile.visibility = View.VISIBLE
                binding.ivHeaderImage.visibility = View.VISIBLE
                binding.btnBackProfile.visibility = View.GONE
                binding.btnEditProfile.setOnClickListener {
                    val intent = Intent(context, EditProfileActivity::class.java)
                    startActivity(intent)
                }
            } else {
                binding.btnEditProfile.visibility = View.GONE
                binding.ivHeaderImage.visibility = View.INVISIBLE
                binding.btnBackProfile.visibility = View.VISIBLE
                binding.btnBackProfile.setOnClickListener {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

        } else {
            requireContext().showCustomPopup("User not found")
        }
    }

    private fun loadUserProfile(userId: String) {

        db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (_binding == null) return@addOnSuccessListener
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
                    binding.ivUserProfile.loadImage(firstPost.userAvatar, isCircular = true, fallbackResId = R.drawable.person_ic)
                } else if (binding.tvUsername.text.isEmpty()) {
                    binding.tvUsername.text = "Crave User"
                }
            }
            .addOnFailureListener {
                if (isAdded && context != null) {
                    requireContext().showCustomPopup("Could not load posts")
                }
            }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                if (document != null && document.exists()) {
                    val bio = document.getString("bio")
                    if (!bio.isNullOrBlank()) {
                         binding.tvBio.text = bio
                    }else {
                    binding.tvBio.text = "Food Lover \uD83C\uDF54"
                }

                    val realName = document.getString("name")
                    if (!realName.isNullOrEmpty()) {
                        binding.tvUsername.text = realName
                    }
                    val profileImageStr = document.getString("profileImage") ?: ""
                    if (profileImageStr.isNotEmpty()) {
                        binding.ivUserProfile.loadImage(profileImageStr, isCircular = true, fallbackResId = R.drawable.person_ic)
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
            intent.putExtra("postId", clickedPost.id)
            intent.putExtra("restaurantName", clickedPost.restaurantName)
            intent.putExtra("userId", clickedPost.userId)
            intent.putExtra("userAvatar", clickedPost.userAvatar)
            intent.putExtra("timeAgo", clickedPost.timeAgo)
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