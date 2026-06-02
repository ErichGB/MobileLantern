package com.mssde.mobilelantern.domain.model

data class AuthCredentials(
    val userName: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: Int
) 