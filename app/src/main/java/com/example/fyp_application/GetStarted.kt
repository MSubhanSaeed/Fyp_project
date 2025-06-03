package com.example.fyp_application

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityGetStartedBinding

class GetStarted : AppCompatActivity() {

    private lateinit var binding: ActivityGetStartedBinding
    private lateinit var sharPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Redirect to HomeScreen if user is already logged in
        if (sharPref.getInt("userid", -1) > 0) {
            redirectToHomeScreen()
            return
        }

        // Inflate the layout using ViewBinding
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to SignUp screen when button is clicked
        binding.getstarted.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }

    private fun redirectToHomeScreen() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
        finish() // Prevent user from returning to this screen
    }
}
