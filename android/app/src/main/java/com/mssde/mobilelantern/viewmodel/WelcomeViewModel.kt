package com.mssde.mobilelantern.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mssde.mobilelantern.data.local.TokenManager
import com.mssde.mobilelantern.domain.repository.AuthRepository
import com.mssde.mobilelantern.ui.welcome.WelcomeSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _sessionState = MutableStateFlow<WelcomeSessionState>(WelcomeSessionState.NoCredentials)
    val sessionState = _sessionState.asStateFlow()

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    private var validationJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.getStoredToken().distinctUntilChanged().collect { token ->
                if (token == null) {
                    validationJob?.cancel()
                    if (_sessionState.value != WelcomeSessionState.Unauthenticated) {
                        _sessionState.value = WelcomeSessionState.NoCredentials
                    }
                }
            }
        }
    }

    fun refreshSession() {
        validationJob?.cancel()
        validationJob = viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token == null) {
                _sessionState.value = WelcomeSessionState.NoCredentials
                return@launch
            }
            val lastValidationTime = authRepository.getLastValidationTime()
            val now = System.currentTimeMillis()
            if (lastValidationTime > 0L && now - lastValidationTime < TOKEN_VALIDATION_CACHE_WINDOW_MS) {
                _sessionState.value = WelcomeSessionState.Ready
                return@launch
            }
            _sessionState.value = WelcomeSessionState.Validating
            authRepository.validateToken(token).fold(
                onSuccess = { valid ->
                    if (valid) {
                        _sessionState.value = WelcomeSessionState.Ready
                        authRepository.saveLastValidationTime(System.currentTimeMillis())
                    } else {
                        _sessionState.value = WelcomeSessionState.Unauthenticated
                        authRepository.clearToken()
                        _sessionExpired.emit(Unit)
                    }
                },
                onFailure = { e ->
                    when {
                        isUnauthorizedHttp(e) -> {
                            _sessionState.value = WelcomeSessionState.Unauthenticated
                            authRepository.clearToken()
                            _sessionExpired.emit(Unit)
                        }
                        else -> {
                            _sessionState.value = WelcomeSessionState.ValidationUnavailable
                        }
                    }
                }
            )
        }
    }

    fun onContinueRequested(onProceed: () -> Unit) {
        viewModelScope.launch {
            when (_sessionState.value) {
                WelcomeSessionState.Ready -> onProceed()
                WelcomeSessionState.ValidationUnavailable -> {
                    val token = tokenManager.getToken() ?: return@launch
                    authRepository.validateToken(token).fold(
                        onSuccess = { valid ->
                            if (valid) {
                                _sessionState.value = WelcomeSessionState.Ready
                                authRepository.saveLastValidationTime(System.currentTimeMillis())
                                onProceed()
                            } else {
                                _sessionState.value = WelcomeSessionState.Unauthenticated
                                authRepository.clearToken()
                                _sessionExpired.emit(Unit)
                            }
                        },
                        onFailure = { e ->
                            if (isUnauthorizedHttp(e)) {
                                _sessionState.value = WelcomeSessionState.Unauthenticated
                                authRepository.clearToken()
                                _sessionExpired.emit(Unit)
                            } else {
                                onProceed()
                            }
                        }
                    )
                }
                else -> Unit
            }
        }
    }

    private fun isUnauthorizedHttp(e: Throwable): Boolean {
        val http = e as? HttpException ?: return false
        return http.code() == 401 || http.code() == 403
    }

    private companion object {
        const val TOKEN_VALIDATION_CACHE_WINDOW_MS = 2 * 60 * 60 * 1000L
    }
}
