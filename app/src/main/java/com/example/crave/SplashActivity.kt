package com.example.crave

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.example.crave.databinding.ActivitySplashBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showView(binding.imgIcon)
    }

    fun showViewSlideDown(view: View) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        view.y = (-height / 2).toFloat()
        view.scaleX = 0.0f
        view.scaleY = 0.0f
        view.animate().scaleY(1.0f).scaleX(1.0f).translationY(0f).setDuration(1400)
            .setInterpolator(LinearOutSlowInInterpolator())
    }

    private fun showView(view: View) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.scaleX = 0.0f
        view.scaleY = 0.0f
        view.animate()
            .scaleY(1.75f)
            .scaleX(1.75f)
            .translationY(0f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animator: Animator) {
                    checkUser()
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
    }


    private fun checkUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startMainActivity()
        } else {
            startLogin()
        }
    }

    private fun onSignInResult(res: FirebaseAuthUIAuthenticationResult) {
        if (res.resultCode == RESULT_OK) {
            android.util.Log.d("MY_DEBUG", "register succseed")
            val user = FirebaseAuth.getInstance().currentUser
            startMainActivity()
        } else {
            android.util.Log.e("MY_DEBUG", "register failed ${res.idpResponse?.error?.errorCode}")        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            // AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.cravelogo)
            //.setTheme(R.style.Theme_Crave)
            .build()

        signInLauncher.launch(signInIntent)
    }
}