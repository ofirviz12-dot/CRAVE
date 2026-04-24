package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crave.adapters.FeedAdapter
import com.example.crave.models.Post
import com.example.crave.R
import com.example.crave.databinding.FragmentFeedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        binding.rvFeed.layoutManager = LinearLayoutManager(context)
        (binding.rvFeed.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false

        feedAdapter = FeedAdapter(emptyList(),

            onLikeClicked = { likedPost, isAddingLike ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@FeedAdapter
                val postRef = db.collection("posts").document(likedPost.id)
                if (isAddingLike) {
                    postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
                } else {
                    postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
                }
            },
            onCommentClicked = { clickedPost ->
                if (!isAdded || _binding == null) return@FeedAdapter
                val bottomSheet = CommentsBottomSheetFragment(clickedPost.id)
                bottomSheet.show(parentFragmentManager, "CommentsBottomSheet")
            },
            onNutritionClicked = { clickedPost ->
                if (!isAdded || _binding == null) return@FeedAdapter
                val bottomSheet = NutritionBottomSheetFragment(clickedPost)
                bottomSheet.show(parentFragmentManager, "NutritionBottomSheet")
            },
            onUserClicked = { clickedUserId ->
                val profileFragment = ProfileFragment()
                val bundle = Bundle()
                bundle.putString("userId", clickedUserId)
                profileFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.rvFeed.adapter = feedAdapter

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) return@addSnapshotListener

                if (_binding == null) return@addSnapshotListener

                val postList = mutableListOf<Post>()
                if (value != null) {
                    for (document in value) {
                        val post = document.toObject(Post::class.java).copy(id = document.id)
                        postList.add(post)
                    }
                }

                feedAdapter.updatePosts(postList)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}