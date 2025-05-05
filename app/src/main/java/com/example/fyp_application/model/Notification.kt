package com.example.fyp_application.model

data class Notification(
    val name: String,
    val message: String,
    val timeAgo: String,
    val profileImageRes: Int // drawable resource id
)
