package com.example.crave.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.FeedAdapter
import com.example.crave.Post
import com.example.crave.PostDetailActivity
import com.example.crave.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var feedAdapter: FeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvFeed = view.findViewById<RecyclerView>(R.id.rvFeed)
        rvFeed.layoutManager = LinearLayoutManager(context)
        val db = FirebaseFirestore.getInstance()

        feedAdapter = FeedAdapter(emptyList(),
            onPostClicked = { selectedPost ->
            },
            onLikeClicked = { likedPost, isAddingLike ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@FeedAdapter
                val postRef = db.collection("posts").document(likedPost.id)
                if (isAddingLike) {
                    postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
                } else {
                    postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
                }
            }
        )
        rvFeed.adapter = feedAdapter

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

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

}