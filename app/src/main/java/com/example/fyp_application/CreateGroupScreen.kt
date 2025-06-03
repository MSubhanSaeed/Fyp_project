package com.example.fyp_application

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityCreateGroupScreenBinding
import com.example.fyp_application.model.GroupRequest
import com.example.fyp_application.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class CreateGroupScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupScreenBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.createGroupButton.setOnClickListener {
            val groupName = binding.groupNameInput.text.toString().trim()

            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val userId = sharedPrefs.getInt("userid", -1)


            val groupCode = generateGroupCode()
            val createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)


            val request = GroupRequest(
                groupName,
                groupCode,
                true,
                userId,
                createdAt
            )


            RetrofitClient.getInstance().getUserApiService().createGroup(request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CreateGroupScreen, "Group created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CreateGroupScreen, CircleScreen::class.java))
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

    private fun generateGroupCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
