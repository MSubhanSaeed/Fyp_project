package com.example.fyp_application.model

data class GeoFriend(
    val userId: Int,
    val name: String,
    var isArrived: Boolean = false,
    var isLeaved: Boolean = false
)
