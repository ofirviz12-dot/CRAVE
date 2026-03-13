package com.example.crave

import java.util.Date

data class Post(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val restaurantName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Date? = null,
    val likedBy: ArrayList<String> = ArrayList(),
    val commentsCount: Int = 0,

    val hasFoodAnalysis: Boolean = false, // האם ה-AI ניתח את התמונה הזו?
    val detectedDish: String = "", // איזה אוכל הוא זיהה (למשל: Creamy Pasta)
    val ingredients: List<String> = emptyList(), // רשימת המרכיבים
    val calories: String = "", // קלוריות
    val protein: String = "", // חלבון
    val carbs: String = "", // פחמימות
    val fat: String = "", // שומן
    val dietLabels: List<String> = emptyList()
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