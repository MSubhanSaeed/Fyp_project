package com.example.fyp_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityEditProfileScreenBinding
import com.example.fyp_application.databinding.ActivitySettingScreenBinding

class EditProfile_Screen : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityEditProfileScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


            binding.btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        }
    }