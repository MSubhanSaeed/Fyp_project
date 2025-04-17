package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityGetStartedBinding

class GetStarted : AppCompatActivity() {

    // Declare the binding variable
    private lateinit var binding: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityGetStartedBinding.inflate(layoutInflater)

        // Set the content view to the root of the binding object
        setContentView(binding.root)

        // Set an OnClickListener for the button using binding
        binding.getstarted.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}
