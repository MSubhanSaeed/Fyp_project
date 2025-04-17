package com.example.fyp_application

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityCreateCircleScreenBinding
import com.example.fyp_application.databinding.ActivityGeofenceScreenBinding

class GeofenceScreen : AppCompatActivity() {
    private lateinit var binding: ActivityGeofenceScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}