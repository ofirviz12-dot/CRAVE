package com.example.crave

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val openTime: String = "",
    val closeTime: String = ""
)