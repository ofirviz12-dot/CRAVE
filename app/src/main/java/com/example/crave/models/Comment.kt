package com.example.crave.models

import com.google.firebase.Timestamp
import java.util.Date

data class Comment(
    var id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Any? = null
) {
    /**
     * פונקציה חסינה שמוציאה זמן כ-Long
     * מטפלת בנתונים מהסקריפט (Long) ובנתונים מהאפליקציה (Timestamp)
     */
    fun getTimestampAsLong(): Long {
        return when (timestamp) {
            is Long -> timestamp
            is Timestamp -> timestamp.seconds * 1000
            is Date -> timestamp.time
            is Map<*, *> -> {
                // הגנה למקרה שפיירבייס מחזיר Map (קורה לפעמים ב-Listeners)
                val seconds = timestamp["seconds"] as? Long ?: 0L
                seconds * 1000
            }
            else -> 0L
        }
    }
}