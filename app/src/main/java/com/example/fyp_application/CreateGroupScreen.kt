package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityCreateCircleScreenBinding
import com.example.fyp_application.databinding.ActivityCreateGroupScreenBinding

class CreateGroupScreen : AppCompatActivity() {
        private lateinit var binding: ActivityCreateGroupScreenBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityCreateGroupScreenBinding.inflate(layoutInflater)
            setContentView(binding.root)
            // Set up the back arrow click
            binding.backArrow.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        }
}