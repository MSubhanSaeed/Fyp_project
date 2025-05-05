package com.example.fyp_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityRequestLocationProfileBinding
import com.example.fyp_application.databinding.ActivityStopLocationProfileBinding

class RequestLocation_profile : AppCompatActivity() {
    private lateinit var binding: ActivityRequestLocationProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLocationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}