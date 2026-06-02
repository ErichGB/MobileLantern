package com.mssde.mobilelantern.data.remote

import com.mssde.mobilelantern.data.remote.dto.LoginRequestDto
import com.mssde.mobilelantern.data.remote.dto.LoginResponseDto
import com.mssde.mobilelantern.data.remote.dto.ValidateTokenResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("ia/checkin")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("auth/validate")
    suspend fun validateToken(@Header("Token") token: String): Response<ValidateTokenResponseDto>
} 