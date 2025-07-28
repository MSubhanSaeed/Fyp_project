package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityEditProfileScreenBinding
import com.example.fyp_application.dto.UserEntity
import com.example.fyp_application.model.UserDto
import com.example.fyp_application.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfile_Screen : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileScreenBinding
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID from SharedPreferences
        val sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userId = sharPref.getInt("userid", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load user data
        loadUserData()

        // Set up click listeners
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnUpdate.setOnClickListener {
            updateUserProfile()
        }

        binding.btnDelete.setOnClickListener {
            deleteUserProfile()
        }
    }

    private fun loadUserData() {
        RetrofitClient.getInstance().userApiService.getUserById(userId).enqueue(
            object : Callback<UserEntity> {
                override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        // Populate the fields with user data
                        binding.etUsername.setText(user.name)
                        binding.etEmail.setText(user.email)
                        binding.etPassword.setText(user.password)

                        // Update the static uId if needed
                        UserEntity.uId = user.userId
                    } else {
                        Toast.makeText(
                            this@EditProfile_Screen,
                            "Failed to load user data: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserEntity>, t: Throwable) {
                    Toast.makeText(
                        this@EditProfile_Screen,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun updateUserProfile() {
        val name = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = UserDto().apply {
            this.name = name
            this.email = email
            this.password = password
        }

        RetrofitClient.getInstance().userApiService.updateUserById(userId, updatedUser).enqueue(
            object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful && response.body() == true) {
                        Toast.makeText(
                            this@EditProfile_Screen,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@EditProfile_Screen,
                            "Failed to update profile: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(
                        this@EditProfile_Screen,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun deleteUserProfile() {
        RetrofitClient.getInstance().userApiService.deleteUserById(userId).enqueue(
            object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful && response.body() == true) {
                        // Clear SharedPreferences and redirect to login
                        val sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        sharPref.edit().clear().apply()

                        // Reset static uId
                        UserEntity.uId = -1

                        Toast.makeText(
                            this@EditProfile_Screen,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirect to login screen
                        val intent = Intent(this@EditProfile_Screen, LoginScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@EditProfile_Screen,
                            "Failed to delete account: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(
                        this@EditProfile_Screen,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}