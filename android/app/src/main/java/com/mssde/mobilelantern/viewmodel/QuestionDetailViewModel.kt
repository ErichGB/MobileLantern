package com.mssde.mobilelantern.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
import com.mssde.mobilelantern.domain.usecase.ObserveQuestionDetailsUseCase
import com.mssde.mobilelantern.domain.usecase.RefreshQuestionDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionDetailUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val interactions: List<QuestionInteractionEntity> = emptyList(),
    val questionId: Int = -1
)

@HiltViewModel
class QuestionDetailViewModel @Inject constructor(
    private val observeQuestionDetailsUseCase: ObserveQuestionDetailsUseCase,
    private val refreshQuestionDetailsUseCase: RefreshQuestionDetailsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuestionDetailUiState())
    val uiState: StateFlow<QuestionDetailUiState> = _uiState.asStateFlow()
    
    fun loadQuestionDetails(questionId: Int) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            questionId = questionId
        )
        
        viewModelScope.launch {
            observeQuestionDetailsUseCase(questionId).collect { interactions ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    interactions = interactions
                )
            }
        }
        
        viewModelScope.launch {
            refreshQuestionDetailsUseCase(questionId)
                .onFailure { error ->
                    if (_uiState.value.interactions.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Error desconocido al cargar los detalles"
                        )
                    }
                }
        }
    }
    
    fun refreshQuestionDetails() {
        val currentQuestionId = _uiState.value.questionId
        if (currentQuestionId == -1) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )
            
            refreshQuestionDetailsUseCase(currentQuestionId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = error.message ?: "Error al actualizar"
                    )
                }
        }
    }
    
    fun retry() {
        val currentQuestionId = _uiState.value.questionId
        if (currentQuestionId != -1) {
            loadQuestionDetails(currentQuestionId)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
