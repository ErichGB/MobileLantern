package com.mssde.mobilelantern.domain.model

sealed interface UiState {
    object Initial : UiState
    object Loading : UiState
    data class Success(val message: String) : UiState
    data class Error(val errorMessage: String) : UiState
} 