package com.mssde.mobilelantern.data.repository

import com.mssde.mobilelantern.data.local.TokenManager
import com.mssde.mobilelantern.data.remote.AuthApi
import com.mssde.mobilelantern.data.remote.dto.LoginRequestDto
import com.mssde.mobilelantern.domain.model.AuthCredentials
import com.mssde.mobilelantern.domain.model.AuthResponse
import com.mssde.mobilelantern.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(credentials: AuthCredentials): Result<AuthResponse> {
        return try {
            val request = LoginRequestDto(
                userName = credentials.userName,
                password = credentials.password
            )
            val response = api.login(request)
            android.util.Log.d("AuthRepositoryImpl", "Login exitoso - userId: ${response.userId}, token: ${response.token.take(20)}...")
            val authResponse = AuthResponse(
                token = response.token,
                userId = response.userId
            )
            // Guardar token, userId y userName
            tokenManager.saveAuthData(response.token, response.userId, response.userName)
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateToken(token: String): Result<Boolean> {
        return try {
            val response = api.validateToken(token)
            when {
                response.isSuccessful -> Result.success(true)
                response.code() == 400 -> Result.success(false)
                else -> Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStoredToken(): Flow<String?> = tokenManager.tokenFlow
    
    override fun getStoredUserId(): Flow<Int?> = tokenManager.userIdFlow
    
    override fun getStoredUserName(): Flow<String?> = tokenManager.userNameFlow
    
    override fun getStoredContext(): Flow<String?> = tokenManager.contextFlow
    
    override fun getStoredAula(): Flow<String?> = tokenManager.aulaFlow

    override fun getLastValidationTime(): Long = tokenManager.getLastValidationTime()

    override suspend fun saveToken(token: String) {
        tokenManager.saveToken(token)
    }
    
    override suspend fun saveContext(context: String) {
        tokenManager.saveContext(context)
    }
    
    override suspend fun saveAula(aula: String?) {
        tokenManager.saveAula(aula)
    }

    override suspend fun saveLastValidationTime(timestampMs: Long) {
        tokenManager.saveLastValidationTime(timestampMs)
    }

    override suspend fun clearToken() {
        tokenManager.clearToken()
    }
} 