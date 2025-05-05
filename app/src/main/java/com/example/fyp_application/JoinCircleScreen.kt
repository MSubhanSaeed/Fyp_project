package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivityJoinCircleScreenBinding

class JoinCircleScreen : AppCompatActivity() {

    private lateinit var binding: ActivityJoinCircleScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize binding and set the content view
        binding = ActivityJoinCircleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
