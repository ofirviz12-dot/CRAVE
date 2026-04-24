package com.example.crave.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.crave.databinding.ActivityEditProfileBinding
import com.example.crave.utils.loadImage
import com.example.crave.utils.showCustomPopup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var imageUri: Uri? = null
    private var base64ImageString: String = ""

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            Glide.with(this).load(uri).circleCrop().into(binding.ivEditProfileImage)
            base64ImageString = encodeImageToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser

        if (user != null) {
            binding.etEditName.setText(user.displayName)

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val savedBio = document.getString("bio") ?: ""
                        binding.etEditBio.setText(savedBio)

                        if (document.contains("profileImage")) {
                            val savedBase64 = document.getString("profileImage") ?: ""
                            if (savedBase64.isNotEmpty()) {
                                base64ImageString = savedBase64
                                binding.ivEditProfileImage.loadImage(savedBase64, isCircular = true)
                            } else {
                                loadDefaultImage()
                            }
                        } else {
                            loadDefaultImage()
                        }
                    } else {
                        loadDefaultImage()
                    }
                }
        }

        binding.ivEditProfileImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.tvChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnSaveProfile.setOnClickListener {
            val newName = binding.etEditName.text.toString().trim()
            val newBio = binding.etEditBio.text.toString().trim()

            if (newName.isEmpty()) {
                showCustomPopup("Name cannot be empty!")
                return@setOnClickListener
            }

            val userMap = hashMapOf(
                "name" to newName,
                "bio" to newBio,
                "profileImage" to base64ImageString
            )

            val uid = user?.uid
            if (uid != null) {
                showCustomPopup("saving")

                db.collection("users").document(uid)
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build()

                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            showCustomPopup("Profile Saved")
                            finish()
                        }
                    }
                    .addOnFailureListener { e ->
                        showCustomPopup("Error: ${e.message}")
                        Log.e("EditProfile", "Error saving profile", e)
                    }
            } else {
                showCustomPopup("Error: User not found")
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadDefaultImage() {
        val defaultAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
        Glide.with(this).load(defaultAvatar).circleCrop().into(binding.ivEditProfileImage)
    }

    private fun encodeImageToBase64(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)

            val imageBytes = baos.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}