package com.example.fyp_application

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivityCreateGroupScreenBinding
import com.example.fyp_application.request.GroupRequest
import com.example.fyp_application.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateGroupScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupScreenBinding
    private var isPublic = true  // Correctly track public/private

    private var defaultBackground: Drawable? = null
    private var defaultTextColor: ColorStateList? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Save initial button styles
        defaultBackground = binding.privateButton.background
        defaultTextColor = binding.privateButton.textColors

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        updatePrivacyButtons()

        binding.publicButton.setOnClickListener {
            isPublic = true     // Correct logic
            updatePrivacyButtons()
        }

        binding.privateButton.setOnClickListener {
            isPublic = false    // Correct logic
            updatePrivacyButtons()
        }

        binding.createGroupButton.setOnClickListener {
            val groupName = binding.groupNameInput.text.toString().trim()
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.createGroupButton.setOnClickListener {
                startActivity(Intent(this, InvitecodeScreen::class.java))
            }

            val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val userId = sharedPrefs.getInt("userid", -1)

            val groupCode = generateGroupCode()

            val request = GroupRequest(
                groupName,
                groupCode,
                !isPublic,
                userId
            )

            RetrofitClient.getInstance().getUserApiService().createGroup(request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CreateGroupScreen, "Group created!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@CreateGroupScreen, InvitecodeScreen::class.java)
                            intent.putExtra("Group_Code", groupCode)
                            startActivity(intent)

                            finish()
                        } else {
                            Toast.makeText(this@CreateGroupScreen, "Failed to create group", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@CreateGroupScreen, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun updatePrivacyButtons() {
        if (isPublic) {
            setSelectedStyle(binding.publicButton)
            setUnselectedStyle(binding.privateButton)
        } else {
            setSelectedStyle(binding.privateButton)
            setUnselectedStyle(binding.publicButton)
        }
    }

    private fun setSelectedStyle(button: AppCompatButton) {
        button.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.Light_red))
    }

    private fun setUnselectedStyle(button: AppCompatButton) {
        button.background = defaultBackground
        button.setTextColor(defaultTextColor)
    }

    private fun generateGroupCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
