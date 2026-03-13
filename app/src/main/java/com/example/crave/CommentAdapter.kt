package com.example.crave

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentUserName: TextView = itemView.findViewById(R.id.tvCommentUserName)
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvCommentUserName.text = comment.userName
        holder.tvCommentText.text = comment.text
    }

    override fun getItemCount() = comments.size

    // פונקציה לעדכון הרשימה כשיש תגובות חדשות
    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}