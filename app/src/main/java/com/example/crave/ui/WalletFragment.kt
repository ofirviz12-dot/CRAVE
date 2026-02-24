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
import kotlin.random.Random

class WalletFragment : Fragment(R.layout.fragment_wallet) {

    private lateinit var binding: FragmentWalletBinding

    private var currentCoins = 1250
    private var totalRedeemed = 500

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWalletBinding.bind(view)
        updateUI()

        binding.btnRedeem.setOnClickListener {
            showCustomDialog()
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
                    val amount = amountString.toInt()

                    if (amount <= currentCoins) {
                        currentCoins -= amount
                        totalRedeemed += amount
                        updateUI()


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

    private fun updateUI() {
        binding.tvCoinBalance.text = String.format("%,d", currentCoins)
        binding.tvTotalRedeemed.text = String.format("%,d", totalRedeemed)
    }
}