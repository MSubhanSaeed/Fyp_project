package com.example.fyp_application

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityEditProfileScreenBinding
import com.example.fyp_application.databinding.ActivityNotificationScreenBinding

class NotificationScreen : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityNotificationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}