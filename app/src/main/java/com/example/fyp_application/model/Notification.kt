package com.example.fyp_application.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("NotificationId") val notificationId: Int,
    @SerializedName("UserId")         val userId: Int,
    @SerializedName("Type")           val type: String?,
    @SerializedName("Status")         val status: String?,
    @SerializedName("CreatedAt")      val createdAt: String?
)
