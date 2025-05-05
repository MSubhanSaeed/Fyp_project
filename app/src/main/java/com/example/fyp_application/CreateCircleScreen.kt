package com.example.fyp_application

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.fyp_application.databinding.ActivityCreateCircleScreenBinding

class CreateCircleScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCreateCircleScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCircleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Generate and set the invite code
        val generatedCode = generateRandomCode()
        binding.inviteCodeText.text = generatedCode

        // Back arrow click
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Send button click
        binding.sendCodeButton.setOnClickListener {
            val code = generatedCode  // Make sure generatedCode is defined and holds your invite code

            // Copy code to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Invite Code", code)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()

            // Share via WhatsApp
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Join my Friends Circle using this code: $code")
                    setPackage("com.whatsapp") // This ensures it opens specifically in WhatsApp
                }
                startActivity(sendIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "WhatsApp is not installed on your device", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Helper function
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = (1..6)
            .map { chars.random() }
            .joinToString("")
        return code.chunked(3).joinToString("-") // Example: IAF-QGP
    }
}
