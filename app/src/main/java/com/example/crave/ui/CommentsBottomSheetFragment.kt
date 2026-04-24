package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crave.models.Comment
import com.example.crave.adapters.CommentAdapter
import com.example.crave.R
import com.example.crave.databinding.LayoutBottomSheetCommentsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentsBottomSheetFragment(private val postId: String) : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetCommentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentAdapter = CommentAdapter(emptyList())
        binding.rvComments.layoutManager = LinearLayoutManager(context)
        binding.rvComments.adapter = commentAdapter

        loadComments()

        binding.btnSendComment.setOnClickListener {
            val text = binding.etNewComment.text.toString().trim()
            if (text.isNotEmpty()) {
                postComment(text)
                binding.etNewComment.text.clear()
            }
        }
    }

    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->

                if (_binding == null) return@addSnapshotListener

                if (error != null) {
                    return@addSnapshotListener
                }

                val commentsList = mutableListOf<Comment>()
                if (value != null) {
                    for (doc in value) {
                        val comment = doc.toObject(Comment::class.java).copy(id = doc.id)
                        commentsList.add(comment)
                    }
                }

                commentAdapter.updateComments(commentsList)

                if (commentsList.isNotEmpty()) {
                    binding.rvComments.post {
                        if (_binding != null) {
                        binding.rvComments.smoothScrollToPosition(commentsList.size - 1)
                        }
                    }
                }
            }
    }

    private fun postComment(text: String) {
        val user = auth.currentUser ?: return

        val newComment = hashMapOf(
            "postId" to postId,
            "userId" to user.uid,
            "userName" to (user.displayName ?: "Anonymous"),
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("comments").add(newComment)
            .addOnSuccessListener {
                db.collection("posts").document(postId)
                    .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}