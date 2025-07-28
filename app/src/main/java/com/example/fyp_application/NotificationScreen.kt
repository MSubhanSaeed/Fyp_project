package com.example.fyp_application

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_application.network.RetrofitClient
import com.example.fyp_application.Adapters.NotificationAdapter
import com.example.fyp_application.databinding.ActivityNotificationScreenBinding
import com.example.fyp_application.model.Notification
import com.example.fyp_application.network.UserApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationScreen : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationScreenBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var userApiService: UserApiService
    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private val pollingInterval = 20000L // 20 seconds
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get userId from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        userId = sharedPref.getInt("userid", 0)

        // Initialize API service
        userApiService = RetrofitClient.getInstance().getUserApiService()

        // Setup RecyclerView
        notificationAdapter = NotificationAdapter(
            emptyList(),
            onItemClick = { notification -> showNotificationDetails(notification) },
            onDeleteClick = { notificationId -> deleteNotification(notificationId) }
        )

        binding.notificationRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationScreen)
            adapter = notificationAdapter
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        // Load notifications
        loadNotifications(userId, showToasts = false)
        startPolling(userId)
    }

    private fun loadNotifications(userId: Int, showToasts: Boolean = true) {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
        binding.errorView.visibility = View.GONE

        userApiService.getNotificationsByUserId(userId).enqueue(object : Callback<List<Notification>> {
            override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    if (notifications.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                    } else {
                        if (showToasts && notificationAdapter.itemCount > 0) {
                            val newNotifications = notifications.filter { newNotif ->
                                notificationAdapter.notifications.none { it.notificationId == newNotif.notificationId }
                            }
                            newNotifications.forEach { showToastNotification(it) }
                        }
                        notificationAdapter.updateNotifications(notifications)
                    }
                }else {
                    showErrorView("Failed to load notifications: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                showErrorView("Network error: ${t.message}")
            }
        })
    }

    private fun markAsRead(notification: Notification) {
        val request = mapOf(
            "notificationId" to notification.notificationId,
            "status" to "Read",
            "type" to notification.type,
            "userId" to userId
        )

        userApiService.markNotificationAsRead(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val updatedNotifications = notificationAdapter.notifications.map {
                        if (it.notificationId == notification.notificationId) {
                            it.copy(status = "Read")
                        } else {
                            it
                        }
                    }
                    notificationAdapter.updateNotifications(updatedNotifications)
                } else {
                    Toast.makeText(this@NotificationScreen, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@NotificationScreen, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteNotification(notificationId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setPositiveButton("Delete") { _, _ ->
                userApiService.deleteNotification(notificationId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            val updatedNotifications = notificationAdapter.notifications.filter {
                                it.notificationId != notificationId
                            }
                            notificationAdapter.updateNotifications(updatedNotifications)

                            if (updatedNotifications.isEmpty()) {
                                binding.emptyView.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this@NotificationScreen, "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@NotificationScreen, "Network error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationDetails(notification: Notification) {
        AlertDialog.Builder(this)
            .setTitle(notification.type ?: "Notification")
            .setMessage("Created: ${notification.createdAt ?: "-"}")
            .setPositiveButton("OK", null)
            .setNeutralButton("Mark as Read") { _, _ ->
                if (notification.status.equals("unread", true)) markAsRead(notification)
            }
            .show()
    }


    private fun showToastNotification(notification: Notification) {
        Toast.makeText(this, notification.type ?: "New Notification", Toast.LENGTH_LONG).show()
    }


    private fun startPolling(userId: Int) {
        stopPolling()
        pollingRunnable = object : Runnable {
            override fun run() {
                if (!isFinishing && !isDestroyed) {
                    loadNotifications(userId, showToasts = true)
                    handler.postDelayed(this, pollingInterval)
                }
            }
        }
        handler.post(pollingRunnable!!)
    }

    private fun stopPolling() {
        pollingRunnable?.let {
            handler.removeCallbacks(it)
            pollingRunnable = null
        }
    }

    private fun showErrorView(message: String) {
        binding.errorView.visibility = View.VISIBLE
        binding.errorText.text = message
        binding.retryButton.setOnClickListener {
            loadNotifications(userId, showToasts = false)
        }
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onResume() {
        super.onResume()
        if (userId != 0) {
            startPolling(userId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }
}
