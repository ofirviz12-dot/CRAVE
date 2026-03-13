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
import android.widget.Toast
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

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivSelectedImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

            // מפעילים את הכיווץ ברקע כדי שהתמונה תוצג על המסך בלי לקרוס!
            lifecycleScope.launch(Dispatchers.IO) {
                val safeBitmap = getBitmapFromUri(uri)
                withContext(Dispatchers.Main) {
                    if (safeBitmap != null) {
                        binding.ivSelectedImage.setImageBitmap(safeBitmap)
                    } else {
                        Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
        Toast.makeText(context, "Analyzing food & Posting... 🍔🤖", Toast.LENGTH_LONG).show()

        lifecycleScope.launch(Dispatchers.IO) {
            var bitmap: Bitmap? = null
            try {
                // 1. טוענים תמונה מוקטנת מראש למניעת קריסת זיכרון
                bitmap = getBitmapFromUri(selectedImageUri!!)
                if (bitmap == null) throw Exception("Failed to load image")

                // 2. דחיסה לפיירבייס תוך שמירה על יחס הממדים (מניעת מתיחה!)
                val imageBase64 = encodeImageToBase64(bitmap)

                val apiKey = "YOUR_API_KEY"
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
                        image(bitmap)
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
                    // הגנה: אם ה-AI קרס, עדיין נעלה את התמונה כדי לא לתקוע את המשתמש
                    val fallbackBase64 = bitmap?.let { encodeImageToBase64(it) } ?: ""
                    savePostToFirestore(user, caption, restaurant, fallbackBase64, null)
                }
            } finally {
                // קריטי למניעת קריסות: מחיקת התמונה מהזיכרון ברגע שסיימנו איתה!
                bitmap?.recycle()
            }
        }
    }

    private fun savePostToFirestore(user: FirebaseUser, caption: String, restaurant: String, imageBase64: String, aiData: JSONObject?) {
        if (_binding == null) return // הגנה מקריסה במקרה שהמשתמש יצא מהמסך

        val newPost = hashMapOf<String, Any>(
            "userId" to user.uid,
            "userName" to (user.displayName ?: "Anonymous"),
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
                    Toast.makeText(context, "Post uploaded successfully! 🍔", Toast.LENGTH_SHORT).show()
                    binding.etCaption.text.clear()
                    binding.etRestaurantName.text.clear()
                    binding.ivSelectedImage.setImageURI(null)
                    selectedImageUri = null
                    binding.btnPost.isEnabled = true
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnPost.isEnabled = true
                }
            }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true // בודק מימדים בלי להעמיס על הזיכרון
            var inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // מחשב פי כמה להקטין
            options.inSampleSize = calculateInSampleSize(options, 500, 500)
            options.inJustDecodeBounds = false // עכשיו טוען באמת

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

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        return try {
            val maxWidth = 600f
            val maxHeight = 600f
            val ratio = Math.min(maxWidth / bitmap.width, maxHeight / bitmap.height)
            val width = Math.round(ratio * bitmap.width)
            val height = Math.round(ratio * bitmap.height)

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            val baos = ByteArrayOutputStream()

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val bytes = baos.toByteArray()

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }

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