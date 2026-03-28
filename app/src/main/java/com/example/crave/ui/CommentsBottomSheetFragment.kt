package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crave.Comment
import com.example.crave.CommentAdapter
import com.example.crave.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentsBottomSheetFragment(private val postId: String) : BottomSheetDialogFragment() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var rvComments: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvComments = view.findViewById(R.id.rvComments)
        val etNewComment = view.findViewById<EditText>(R.id.etNewComment)
        val btnSendComment = view.findViewById<ImageView>(R.id.btnSendComment)

        commentAdapter = CommentAdapter(emptyList())
        rvComments.layoutManager = LinearLayoutManager(context)
        rvComments.adapter = commentAdapter

        loadComments()

        btnSendComment.setOnClickListener {
            val text = etNewComment.text.toString().trim()
            if (text.isNotEmpty()) {
                postComment(text)
                etNewComment.text.clear()
            }
        }
    }

    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(context, "Error loading comments", Toast.LENGTH_SHORT).show()
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
                    rvComments.post {
                        rvComments.smoothScrollToPosition(commentsList.size - 1)
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
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("comments").add(newComment)
            .addOnSuccessListener {
                db.collection("posts").document(postId)
                    .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send comment", Toast.LENGTH_SHORT).show()
            }
    }
}