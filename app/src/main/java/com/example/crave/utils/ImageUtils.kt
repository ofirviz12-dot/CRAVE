package com.example.crave.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.crave.R

fun ImageView.loadImage(
    imageString: String?,
    isCircular: Boolean = false,
    fallbackResId: Int = R.drawable.ic_launcher_background
) {
    if (imageString.isNullOrBlank()) {
        this.setImageResource(fallbackResId)
        return
    }

    val cleanString = if (imageString.contains("base64,")) {
        imageString.substringAfter("base64,")
    } else {
        imageString
    }

    var request = if (cleanString.startsWith("http")) {
        Glide.with(this.context).load(cleanString)
    } else {
        try {
            val imageBytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
            Glide.with(this.context).asBitmap().load(imageBytes)
        } catch (e: Exception) {
            Glide.with(this.context).load(fallbackResId)
        }
    }

    if (isCircular) {
        request = request.circleCrop()
    } else {
        request = request.centerCrop()
    }

    request.into(this)
}