package com.example.fyp_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityRequestLocationProfileBinding

class RequestLocation_profile : AppCompatActivity() {
    private lateinit var binding: ActivityRequestLocationProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestLocationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}