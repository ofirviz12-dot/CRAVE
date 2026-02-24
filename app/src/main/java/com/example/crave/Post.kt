package com.example.crave

import java.util.Date

data class Post(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Date? = null,
    val likedBy: ArrayList<String> = ArrayList()
) {
    val likes: Int get() = likedBy.size

    val timeAgo: String
        get() {
            if (timestamp == null) return "Just now"

            val now = System.currentTimeMillis()
            val diff = now - timestamp.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                else -> "${days}d ago"
            }
        }
}