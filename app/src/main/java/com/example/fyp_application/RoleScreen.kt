package com.example.fyp_application

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityRoleScreenBinding
import com.example.fyp_application.databinding.ActivitySettingScreenBinding

class RoleScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRoleScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityRoleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}