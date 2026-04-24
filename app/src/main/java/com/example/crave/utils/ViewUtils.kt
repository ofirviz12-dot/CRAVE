package com.example.crave.utils

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.crave.R


fun Context.showCustomPopup(message: String) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.custom_popup)

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    val tvMessage = dialog.findViewById<TextView>(R.id.tvPopupMessage)
    tvMessage.text = message

    dialog.show()

    Handler(Looper.getMainLooper()).postDelayed({
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        } catch (e: Exception) {
        }
    }, 2000)
}