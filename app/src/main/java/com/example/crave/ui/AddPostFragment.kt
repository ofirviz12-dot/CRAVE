package com.example.crave.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.crave.utils.showCustomPopup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.crave.databinding.FragmentAddPostBinding
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import com.example.crave.BuildConfig
import com.example.crave.R
import com.example.crave.utils.loadImage

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivPostImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            binding.btnSelectImage.visibility = View.GONE

            com.bumptech.glide.Glide.with(this)
                .load(uri)
                .into(binding.ivPostImage)


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

        loadCurrentUserInfo()

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnPost.setOnClickListener {
            uploadPost()
        }
        val prefilledName = requireActivity().intent.getStringExtra("PREFILLED_RESTAURANT_NAME")

        if (!prefilledName.isNullOrEmpty()) {
            binding.etRestaurantName.setText(prefilledName)
            requireActivity().intent.removeExtra("PREFILLED_RESTAURANT_NAME")
        }


    }

    private fun uploadPost() {
        val caption = binding.etCaption.text.toString().trim()
        val restaurantInput = binding.etRestaurantName.text.toString().trim()
        val restaurant = restaurantInput.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
        val user = auth.currentUser

        if (user == null) {
            requireContext().showCustomPopup("You must be logged in")
            return
        }

        if (selectedImageUri == null) {
            requireContext().showCustomPopup("Please select an image first")
            return
        }
        if(restaurant.isEmpty()){
            requireContext().showCustomPopup("Please enter a restaurant name")
            return
        }

        binding.btnPost.isEnabled = false
        binding.btnPost.text = "UPLOADING..."

        lifecycleScope.launch(Dispatchers.IO) {
            var originalBitmap: Bitmap? = null
            var safeBitmap: Bitmap? = null
            try {
                originalBitmap = getBitmapFromUri(selectedImageUri!!)
                if (originalBitmap == null) throw Exception("Failed to load image")

                val maxWidth = 800f
                val maxHeight = 800f
                val ratio = Math.min(maxWidth / originalBitmap.width, maxHeight / originalBitmap.height)
                val width = Math.round(ratio * originalBitmap.width)
                val height = Math.round(ratio * originalBitmap.height)

                safeBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

                val baos = ByteArrayOutputStream()
                safeBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                val apiKey = BuildConfig.GEMINI_API_KEY
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )

                val prompt = """
                    Analyze this food image. Return ONLY a valid JSON object (no markdown, no code blocks).
                    Structure:
                    {
                      "detectedDish": "Dish Name",
                      "calories": "0 kcal",
                      "protein": "0g",
                      "carbs": "0g",
                      "fat": "0g",
                      "ingredients": ["Item 1", "Item 2"],
                      "dietLabels": ["Vegetarian", "Keto Friendly"]
                    }
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        image(originalBitmap)
                        text(prompt)
                    }
                )

                val responseText = response.text ?: "{}"
                val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
                val jsonObject = JSONObject(cleanJson)

                withContext(Dispatchers.Main) {
                    savePostToFirestore(user, caption, restaurant, imageBase64, jsonObject)
                }

            } catch (e: Exception) {
                Log.e("AI_ERROR", "Failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    val fallbackBase64 = safeBitmap?.let {
                        val baos = ByteArrayOutputStream()
                        it.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                        Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    } ?: ""
                    savePostToFirestore(user, caption, restaurant, fallbackBase64, null)

                }
            } finally {
                originalBitmap?.recycle()
                if (safeBitmap != originalBitmap) {
                    safeBitmap?.recycle()
                }
            }
        }
    }

    private fun savePostToFirestore(user: FirebaseUser, caption: String, restaurant: String, imageBase64: String, aiData: JSONObject?) {
        if (_binding == null) return

        db.collection("users").document(user.uid).get().addOnSuccessListener { document ->

            val savedAvatar = document?.getString("profileImage") ?: ""
            val updatedName = document?.getString("name") ?: user.displayName ?: "Anonymous"

            val newPost = hashMapOf<String, Any>(
                "userId" to user.uid,
                "userName" to updatedName,
                "userAvatar" to savedAvatar,
                "restaurantName" to restaurant,
                "caption" to caption,
                "imageUrl" to imageBase64,
                "timestamp" to java.util.Date(),
                "likedBy" to ArrayList<String>(),
                "commentsCount" to 0
            )

            if (aiData != null) {
                newPost["hasFoodAnalysis"] = true
                newPost["detectedDish"] = aiData.optString("detectedDish", "Unknown Dish")
                newPost["calories"] = aiData.optString("calories", "N/A")
                newPost["protein"] = aiData.optString("protein", "N/A")
                newPost["carbs"] = aiData.optString("carbs", "N/A")
                newPost["fat"] = aiData.optString("fat", "N/A")

                val ingredientsList = ArrayList<String>()
                val ingredientsArray = aiData.optJSONArray("ingredients")
                if (ingredientsArray != null) {
                    for (i in 0 until ingredientsArray.length()) {
                        ingredientsList.add(ingredientsArray.getString(i))
                    }
                }
                newPost["ingredients"] = ingredientsList

                val dietList = ArrayList<String>()
                val dietArray = aiData.optJSONArray("dietLabels")
                if (dietArray != null) {
                    for (i in 0 until dietArray.length()) {
                        dietList.add(dietArray.getString(i))
                    }
                }
                newPost["dietLabels"] = dietList
            } else {
                newPost["hasFoodAnalysis"] = false
            }

            db.collection("posts")
                .add(newPost)
                .addOnSuccessListener {
                    if (_binding != null) {
                        binding.etCaption.text.clear()
                        binding.etRestaurantName.text.clear()
                        binding.ivPostImage.setImageDrawable(null)

                        binding.btnSelectImage.visibility = View.GONE
                        binding.btnPost.isEnabled = true
                        binding.btnPost.text = "POST"
                        binding.lottieSuccess.visibility = View.VISIBLE
                        binding.lottieSuccess.playAnimation()

                        binding.lottieSuccess.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
                            override fun onAnimationStart(animation: android.animation.Animator) {}
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                binding.lottieSuccess.visibility = View.GONE
                                binding.btnSelectImage.visibility = View.VISIBLE
                            }
                            override fun onAnimationCancel(animation: android.animation.Animator) {}
                            override fun onAnimationRepeat(animation: android.animation.Animator) {}
                        })
                    }
                }
                .addOnFailureListener { e ->
                    if (_binding != null) {
                        requireContext().showCustomPopup("Error: ${e.message}")
                        binding.btnPost.isEnabled = true
                        binding.btnPost.text = "POST"
                    }
                }

        }.addOnFailureListener { e ->
            if (_binding != null) {
                requireContext().showCustomPopup("Failed to load user profile")
                binding.btnPost.isEnabled = true
                binding.btnPost.text = "POST"
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            options.inSampleSize = calculateInSampleSize(options, 500, 500)
            options.inJustDecodeBounds = false

            inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

      private fun loadCurrentUserInfo() {
        val user = auth.currentUser ?: return

        binding.tvUserName.text = user.displayName ?: "Loading..."

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: user.displayName ?: "New Craver"
                    binding.tvUserName.text = name

                    val base64Image = document.getString("profileImage") ?: ""
                    binding.ivUserAvatar.loadImage(base64Image, isCircular = true, fallbackResId = R.drawable.person_ic)

                }
            }
    }

    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}