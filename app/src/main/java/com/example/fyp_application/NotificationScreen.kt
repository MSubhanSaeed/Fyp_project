package com.example.fyp_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.adapter.NotificationAdapter
import com.example.fyp_application.databinding.ActivityNotificationScreenBinding
import com.example.fyp_application.model.Notification

class NotificationScreen : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notifications = listOf(
            Notification("Subhan", "Shared Location with You", "1m ago", R.drawable.profile_subhan),
            Notification("Abu Bakar", "Leave Place at Faizabad", "1m ago", R.drawable.profile_abu_bakar),
            Notification("Subhan", "Arrives at Rehmanabad", "1m ago", R.drawable.profile_subhan),
            Notification("Dawood", "Leave Place BIIT", "10 Hrs ago", R.drawable.profile_dawood),
            Notification("Amna", "Arrived at Metro Station,Murree road", "15 Hrs ago", R.drawable.profile_amna)
        )

        val adapter = NotificationAdapter(notifications)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notificationRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
