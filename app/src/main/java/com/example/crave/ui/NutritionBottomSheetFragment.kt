package com.example.crave.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.crave.Post
import com.example.crave.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NutritionBottomSheetFragment(private val post: Post) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // חיבור לעיצוב
        val tvDishName = view.findViewById<TextView>(R.id.tvDishName)
        val tvCalories = view.findViewById<TextView>(R.id.tvCalories)
        val tvProtein = view.findViewById<TextView>(R.id.tvProtein)
        val tvCarbs = view.findViewById<TextView>(R.id.tvCarbs)
        val tvFat = view.findViewById<TextView>(R.id.tvFat)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredients)
        val tvDietLabels = view.findViewById<TextView>(R.id.tvDietLabels)

        tvDishName.text = post.detectedDish
        tvCalories.text = post.calories
        tvProtein.text = post.protein
        tvCarbs.text = post.carbs
        tvFat.text = post.fat

        tvIngredients.text = post.ingredients.joinToString(", ")

        if (post.dietLabels.isNotEmpty()) {
            tvDietLabels.text = post.dietLabels.joinToString("   ") { "✔ $it" }
        } else {
            tvDietLabels.text = "No specific diet detected"
        }
    }
}