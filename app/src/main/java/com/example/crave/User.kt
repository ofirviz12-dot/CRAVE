package com.example.crave

data class User(
    val id: String,
    val username: String,
    val bio: String,
    val profileImageResId: Int,
    val followers: Int,
    val following: Int,
    val postsCount: Int
)