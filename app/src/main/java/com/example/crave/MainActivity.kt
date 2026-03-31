package com.example.crave

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.crave.databinding.ActivityMainBinding
import com.example.crave.ui.AddPostFragment
import com.example.crave.ui.FeedFragment
import com.example.crave.ui.ProfileFragment
import com.example.crave.ui.RestaurantFragment
import com.example.crave.ui.WalletFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(FeedFragment())
                    true
                }
                R.id.nav_add -> {
                    replaceFragment(AddPostFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.nav_restaurants -> {
                    replaceFragment(RestaurantFragment())
                    true
                }
                R.id.nav_wallet -> {
                    replaceFragment(WalletFragment())
                    true
                }
                else -> false
            }
        }

        val shouldOpenAddPost = intent.getBooleanExtra("openAddPost", false)

        if (shouldOpenAddPost) {
            binding.bottomNavigation.selectedItemId = R.id.nav_add
        } else {
            replaceFragment(FeedFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val shouldOpenAddPost = intent.getBooleanExtra("openAddPost", false)
        if (shouldOpenAddPost) {
            binding.bottomNavigation.selectedItemId = R.id.nav_add
        }
    }
}