package com.example.fyp_application.model

import com.google.gson.annotations.SerializedName

data class GroupResponse(
    @SerializedName("Group_Id")
    val groupId: Int,
    @SerializedName("GroupName")
    val name: String?,
    @SerializedName("Group_Code")
    val code: String?,
    @SerializedName("CreatedAt")
    val createdAt: String?,
    @SerializedName("IsPrivate")
    val isPrivate: Boolean
)

