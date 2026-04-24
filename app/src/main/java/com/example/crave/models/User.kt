package com.example.crave.models

data class User(
    val id: String,
    val username: String,
    val bio: String,
    val userAvatar: Int,
    val followers: Int,
    val following: Int,
    val postsCount: Int
)