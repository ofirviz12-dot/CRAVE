package com.example.crave.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.crave.databinding.FragmentAddPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivSelectedImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnPost.setOnClickListener {
            uploadPost()
        }
    }

    private fun uploadPost() {
        val caption = binding.etCaption.text.toString().trim()
        val restaurant = binding.etRestaurantName.text.toString().trim()
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(context, "You must be logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPost.isEnabled = false
        Toast.makeText(context, "Posting...", Toast.LENGTH_SHORT).show()

        val imageBase64 = encodeImageToBase64(selectedImageUri!!)

        val newPost = hashMapOf(
            "userId" to user.uid,
            "userName" to (user.displayName ?: "Anonymous"),
            "restaurantName" to restaurant,
            "caption" to caption,
            "imageUrl" to imageBase64,
            "timestamp" to java.util.Date(),
            "likedBy" to ArrayList<String>()
        )

        db.collection("posts")
            .add(newPost)
            .addOnSuccessListener {
                Toast.makeText(context, "Post uploaded successfully! 🍔", Toast.LENGTH_SHORT).show()
                binding.etCaption.text.clear()
                binding.etRestaurantName.text.clear()
                binding.ivSelectedImage.setImageURI(null)
                selectedImageUri = null
                binding.btnPost.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnPost.isEnabled = true
            }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true)
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val bytes = baos.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
