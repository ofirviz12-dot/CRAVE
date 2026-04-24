package com.example.crave.models

data class MenuItem(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Any? = 0.0
) {
    fun getFormattedPrice(): String {
        val p = when (price) {
            is Number -> price.toDouble()
            is String -> price.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
        return if (p % 1 == 0.0) p.toInt().toString() else p.toString()
    }
}