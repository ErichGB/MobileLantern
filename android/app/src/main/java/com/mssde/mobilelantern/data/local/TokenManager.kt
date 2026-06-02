package com.mssde.mobilelantern.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow<String?>(null)
    val tokenFlow: Flow<String?> = _tokenFlow
    
    private val _userIdFlow = MutableStateFlow<Int?>(null)
    val userIdFlow: Flow<Int?> = _userIdFlow
    
    private val _userNameFlow = MutableStateFlow<String?>(null)
    val userNameFlow: Flow<String?> = _userNameFlow
    
    private val _contextFlow = MutableStateFlow<String?>(null)
    val contextFlow: Flow<String?> = _contextFlow
    
    private val _aulaFlow = MutableStateFlow<String?>(null)
    val aulaFlow: Flow<String?> = _aulaFlow

    init {
        _tokenFlow.value = sharedPreferences.getString(KEY_TOKEN, null)
        _userIdFlow.value = sharedPreferences.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        _userNameFlow.value = sharedPreferences.getString(KEY_USER_NAME, null)
        _contextFlow.value = sharedPreferences.getString(KEY_CONTEXT, null)
        _aulaFlow.value = sharedPreferences.getString(KEY_AULA, null)
    }

    fun getToken(): String? = _tokenFlow.value
    
    fun getUserId(): Int? {
        val userId = _userIdFlow.value
        return userId
    }
    
    fun getUserName(): String? = _userNameFlow.value

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
        _tokenFlow.value = token
    }
    
    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
        _userIdFlow.value = userId
    }
    
    fun saveAuthData(token: String, userId: Int, userName: String) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .apply()
        _tokenFlow.value = token
        _userIdFlow.value = userId
        _userNameFlow.value = userName
    }
    
    fun saveContext(context: String?) {
        sharedPreferences.edit()
            .putString(KEY_CONTEXT, context)
            .apply()
        _contextFlow.value = context
    }
    
    fun getAula(): String? = _aulaFlow.value
    
    fun saveAula(aula: String?) {
        sharedPreferences.edit()
            .putString(KEY_AULA, aula)
            .apply()
        _aulaFlow.value = aula
    }

    fun getLastValidationTime(): Long =
        sharedPreferences.getLong(KEY_LAST_VALIDATION_MS, 0L)

    fun saveLastValidationTime(timestampMs: Long) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_VALIDATION_MS, timestampMs)
            .apply()
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_CONTEXT)
            .remove(KEY_AULA)
            .remove(KEY_LAST_VALIDATION_MS)
            .apply()
        _tokenFlow.value = null
        _userIdFlow.value = null
        _userNameFlow.value = null
        _contextFlow.value = null
        _aulaFlow.value = null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_CONTEXT = "question_context"
        private const val KEY_AULA = "user_aula"
        private const val KEY_LAST_VALIDATION_MS = "last_validation_ms"
    }
} 