package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.crave.models.Post
import com.example.crave.databinding.LayoutBottomSheetNutritionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NutritionBottomSheetFragment(private val post: Post) : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetNutritionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // הצגת הערכים הבסיסיים
        binding.tvDishName.text = post.detectedDish
        binding.tvCalories.text = post.calories
        binding.tvProtein.text = post.protein
        binding.tvCarbs.text = post.carbs
        binding.tvFat.text = post.fat

        // טיפול ברשימת המרכיבים - הפיכה לפורמט קריא יותר
        if (post.ingredients.isNotEmpty()) {
            binding.tvIngredients.text = post.ingredients.joinToString(separator = ", ")
        } else {
            binding.tvIngredients.text = "Information not available"
        }

        // טיפול בתוויות דיאטה
        if (post.dietLabels.isNotEmpty()) {
            // הוספת סימן מפריד יפה בין התוויות
            binding.tvDietLabels.text = post.dietLabels.joinToString("  •  ")
        } else {
            binding.tvDietLabels.text = "General Food"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}