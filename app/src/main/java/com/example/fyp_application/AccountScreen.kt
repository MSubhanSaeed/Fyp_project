package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityAccountScreenBinding

class AccountScreen : AppCompatActivity() {
    private lateinit var binding: ActivityAccountScreenBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

    }

    private fun setupNavigation() {

        binding.creategrp.setOnClickListener {
            startActivity(Intent(this, CreateGroupScreen::class.java))
        }

        binding.settingsIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.RoleOption.setOnClickListener {
            startActivity(Intent(this, RoleScreen::class.java))
        }

        binding.leavecircle.setOnClickListener {
            showLeaveCircleDialog()
        }
    }

    private fun showLeaveCircleDialog() {
        AlertDialog.Builder(this)
            .setMessage("You will no longer see or share locations with this circle. Are you sure you want to leave?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                Toast.makeText(this, "You have left the circle.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
