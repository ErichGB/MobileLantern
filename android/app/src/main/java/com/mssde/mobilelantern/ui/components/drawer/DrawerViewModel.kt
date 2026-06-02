package com.mssde.mobilelantern.ui.components.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mssde.mobilelantern.data.model.CurrentUser
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.model.UserStats
import com.mssde.mobilelantern.domain.repository.AuthRepository
import com.mssde.mobilelantern.domain.usecase.ClearLocalHistoryUseCase
import com.mssde.mobilelantern.domain.usecase.ObserveHistoryUseCase
import com.mssde.mobilelantern.domain.usecase.RefreshHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DrawerUiState(
    val questions: List<HistoryQuestion> = emptyList(),
    val filteredQuestions: List<HistoryQuestion> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: CurrentUser = CurrentUser(
        id = 0,
        name = "Usuario",
        email = "usuario@universidad.edu",
        questionsCount = 0,
        resolvedCount = 0
    ),
    val userStats: UserStats = UserStats(
        totalQuestions = 0,
        resolvedQuestions = 0,
        pendingQuestions = 0
    )
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val refreshHistoryUseCase: RefreshHistoryUseCase,
    private val clearLocalHistoryUseCase: ClearLocalHistoryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState.asStateFlow()

    private var hasLoadedInitially = false

    init {
        loadUserInfo()
        observeHistory()
    }

    fun onDrawerOpened() {
        if (!hasLoadedInitially && _uiState.value.questions.isEmpty()) {
            loadInitialData()
        }
    }
    
    fun refreshHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            refreshHistoryUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar las preguntas"
                    )
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            hasLoadedInitially = true
            if (_uiState.value.questions.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                refreshHistoryUseCase()
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Error al cargar las preguntas"
                        )
                    }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterQuestions(query)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Cierra la sesión del usuario limpiando el historial local y el token guardado.
     * El historial debe limpiarse antes de invalidar el token, ya que el caso de uso
     * necesita leer userId/aula desde el AuthRepository.
     */
    fun logout() {
        viewModelScope.launch {
            clearLocalHistoryUseCase()
            authRepository.clearToken()
        }
    }
    
    private fun loadUserInfo() {
        viewModelScope.launch {
            val userName = authRepository.getStoredUserName().firstOrNull() ?: "Usuario"
            val userId = authRepository.getStoredUserId().firstOrNull() ?: 0
            
            val updatedUser = _uiState.value.currentUser.copy(
                id = userId,
                name = userName,
                email = "$userName@universidad.edu"
            )
            
            _uiState.value = _uiState.value.copy(currentUser = updatedUser)
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            observeHistoryUseCase().collect { questions ->
                updateQuestionsAndStats(questions)
            }
        }
    }

    private fun updateQuestionsAndStats(questions: List<HistoryQuestion>) {
        val resolvedCount = questions.count { it.isResolved }
        val pendingCount = questions.size - resolvedCount
        
        val updatedStats = UserStats(
            totalQuestions = questions.size,
            resolvedQuestions = resolvedCount,
            pendingQuestions = pendingCount
        )
        
        val updatedUser = _uiState.value.currentUser.copy(
            questionsCount = questions.size,
            resolvedCount = resolvedCount
        )

        _uiState.value = _uiState.value.copy(
            questions = questions,
            userStats = updatedStats,
            currentUser = updatedUser
        )
        
        filterQuestions(_uiState.value.searchQuery)
    }

    private fun filterQuestions(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.questions
        } else {
            _uiState.value.questions.filter { 
                it.questionText.contains(query, ignoreCase = true) 
            }
        }
        _uiState.value = _uiState.value.copy(filteredQuestions = filtered)
    }
} 
