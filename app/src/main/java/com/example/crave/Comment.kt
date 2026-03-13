package com.example.crave

data class Comment(
    var id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)