package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.crave.Post
import com.example.crave.databinding.LayoutBottomSheetNutritionBinding // שימי לב לייבוא הזה
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

        binding.tvDishName.text = post.detectedDish
        binding.tvCalories.text = post.calories
        binding.tvProtein.text = post.protein
        binding.tvCarbs.text = post.carbs
        binding.tvFat.text = post.fat

        binding.tvIngredients.text = post.ingredients.joinToString(", ")

        if (post.dietLabels.isNotEmpty()) {
            binding.tvDietLabels.text = post.dietLabels.joinToString("   ") { "$it" }
        } else {
            binding.tvDietLabels.text = "No specific diet detected"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}