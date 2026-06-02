package com.mssde.mobilelantern.domain.repository

import com.mssde.mobilelantern.domain.model.AuthCredentials
import com.mssde.mobilelantern.domain.model.AuthResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: AuthCredentials): Result<AuthResponse>
    suspend fun validateToken(token: String): Result<Boolean>
    fun getStoredToken(): Flow<String?>
    fun getStoredUserId(): Flow<Int?>
    fun getStoredUserName(): Flow<String?>
    fun getStoredContext(): Flow<String?>
    fun getStoredAula(): Flow<String?>
    fun getLastValidationTime(): Long
    suspend fun saveToken(token: String)
    suspend fun saveContext(context: String)
    suspend fun saveAula(aula: String?)
    suspend fun saveLastValidationTime(timestampMs: Long)
    suspend fun clearToken()
} 