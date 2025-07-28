package com.example.fyp_application.model

sealed class FriendItem {
    data class Friend(val id:Int,val name: String, var isTrackingAllowed: Boolean) : FriendItem()
}

