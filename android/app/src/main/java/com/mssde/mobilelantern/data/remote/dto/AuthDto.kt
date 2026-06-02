package com.mssde.mobilelantern.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponseDto(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("userName")
    val userName: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("context")
    val context: String?
) 

data class ValidateTokenResponseDto(
    @SerializedName("message")
    val message: String
)