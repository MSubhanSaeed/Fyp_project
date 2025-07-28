package com.example.fyp_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityStopLocationProfileBinding

class StopLocation_Profile : AppCompatActivity() {
    private lateinit var binding: ActivityStopLocationProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopLocationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}