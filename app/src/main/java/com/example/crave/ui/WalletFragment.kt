package com.example.crave.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.crave.R
import com.example.crave.databinding.FragmentWalletBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    private lateinit var binding: FragmentWalletBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentUserName = ""

    private var currentCoinsBalance = 0
    private var totalRedeemedCoins = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWalletBinding.bind(view)

        currentUserName = auth.currentUser?.displayName ?: "Unknown"

        android.util.Log.d("WALLET_TEST", "Current logged in user is: '$currentUserName'")

        if (currentUserName == "Unknown" || currentUserName.isEmpty()) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
            return
        }

        calculateWalletData()

        binding.btnRedeem.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun calculateWalletData() {
        db.collection("users").document(currentUserName)
            .addSnapshotListener { userSnapshot, _ ->
                totalRedeemedCoins = userSnapshot?.getLong("redeemedCoins")?.toInt() ?: 0
                android.util.Log.d("WALLET_TEST", "Total redeemed coins: $totalRedeemedCoins")

                db.collection("posts").whereEqualTo("userName", currentUserName)
                    .addSnapshotListener { postsSnapshot, e ->
                        if (e != null) {
                            android.util.Log.e("WALLET_TEST", "Error fetching posts", e)
                            return@addSnapshotListener
                        }

                        var totalLikes = 0

                        if (postsSnapshot != null) {
                            android.util.Log.d("WALLET_TEST", "Found ${postsSnapshot.size()} posts for this user")
                            for (document in postsSnapshot) {

                                val likedByArray = document.get("likedBy") as? List<*>
                                val likesForPost = likedByArray?.size ?: 0

                                totalLikes += likesForPost
                                android.util.Log.d("WALLET_TEST", "Post ${document.id} has $likesForPost likes")
                            }
                        }

                        android.util.Log.d("WALLET_TEST", "GRAND TOTAL LIKES: $totalLikes")

                        totalLikes += 1020

                        val earnedCoins = totalLikes / 10
                        val progressInWheel = totalLikes % 10

                        currentCoinsBalance = earnedCoins - totalRedeemedCoins
                        if (currentCoinsBalance < 0) currentCoinsBalance = 0

                        updateUI(currentCoinsBalance, progressInWheel)
                    }
            }
    }

    private fun showCustomDialog() {
        val context = requireContext()

        val dialogView = layoutInflater.inflate(R.layout.dialog_coupon_code, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etAmount = dialogView.findViewById<EditText>(R.id.etBuyAmount)
        val tvCodeLabel = dialogView.findViewById<TextView>(R.id.tvCodeLabel)
        val tvRandomCode = dialogView.findViewById<TextView>(R.id.tvRandomCode)
        val btnAction = dialogView.findViewById<Button>(R.id.btnAction)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)

        btnAction.setOnClickListener {
            if (btnAction.text == "BUY NOW") {
                val amountString = etAmount.text.toString()

                if (amountString.isNotEmpty()) {
                    val amountToBuy = amountString.toInt()

                    if (amountToBuy <= currentCoinsBalance) {
                        val newRedeemedTotal = totalRedeemedCoins + amountToBuy

                        val updateData = hashMapOf("redeemedCoins" to newRedeemedTotal)
                        db.collection("users").document(currentUserName)
                            .set(updateData, SetOptions.merge())

                        etAmount.visibility = View.GONE

                        val randomNumber = Random.nextInt(100000, 999999)
                        tvRandomCode.text = randomNumber.toString()

                        tvCodeLabel.visibility = View.VISIBLE
                        tvRandomCode.visibility = View.VISIBLE

                        tvTitle.text = "Purchase Successful!"
                        btnAction.text = "CLOSE"

                    } else {
                        Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    etAmount.error = "Please enter amount"
                }
            } else {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun updateUI(coins: Int, likesProgress: Int) {
        binding.tvCoinBalance.text = String.format("%,d", coins)

        binding.progressLikes.progress = likesProgress
        binding.tvLikesCount.text = "$likesProgress/10"
    }
}