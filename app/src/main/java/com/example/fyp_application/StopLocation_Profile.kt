package com.example.fyp_application

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityCreateCircleScreenBinding
import com.example.fyp_application.databinding.ActivityStopLocationProfileBinding

class StopLocation_Profile : AppCompatActivity() {
    private lateinit var binding: ActivityStopLocationProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopLocationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}